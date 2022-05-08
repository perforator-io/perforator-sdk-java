/*
 * Copyright Perforator, Inc. and contributors. All rights reserved.
 *
 * Use of this software is governed by the Business Source License
 * included in the LICENSE file.
 *
 * As of the Change Date specified in that file, in accordance with
 * the Business Source License, use of this software will be governed
 * by the Apache License, Version 2.0.
 */
package io.perforator.sdk.loadgenerator.codeless;

import io.perforator.sdk.loadgenerator.codeless.actions.ActionConfig;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionInstance;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessor;
import io.perforator.sdk.loadgenerator.codeless.actions.ActionProcessorsRegistry;
import io.perforator.sdk.loadgenerator.codeless.config.*;
import io.perforator.sdk.loadgenerator.core.AbstractLoadGenerator;
import io.perforator.sdk.loadgenerator.core.Perforator;
import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

//TODO: add javadoc
public class CodelessLoadGenerator extends AbstractLoadGenerator {

    private final boolean logSteps;
    private final boolean logActions;

    public CodelessLoadGenerator(Path location) throws IOException {
        this(CodelessConfigFactory.INSTANCE.getCodelessConfig(location));
    }

    public CodelessLoadGenerator(CodelessConfig config) {
        this(config.getLoadGeneratorConfig(), config.getSuiteConfigs());
    }

    public CodelessLoadGenerator(CodelessLoadGeneratorConfig loadGeneratorConfig, List<CodelessSuiteConfig> suites) {
        this(null, loadGeneratorConfig, suites);
    }

    private CodelessLoadGenerator(IntegrationService<SuiteConfigContext, SuiteInstanceContext, TransactionContext, RemoteWebDriverContext> mediator, CodelessLoadGeneratorConfig loadGeneratorConfig, List<CodelessSuiteConfig> suites) {
        super(mediator, loadGeneratorConfig, (List) preprocessConfigs(loadGeneratorConfig, suites));
        this.logSteps = loadGeneratorConfig.isLogSteps();
        this.logActions = loadGeneratorConfig.isLogActions();
    }
    
    private static List<CodelessSuiteConfig> preprocessConfigs(CodelessLoadGeneratorConfig loadGeneratorConfig, List<CodelessSuiteConfig> suites) {
        CodelessSuiteConfigValidator.validate(loadGeneratorConfig, suites);
        
        List<String> propsFiles = new ArrayList<>(suites.size());
        for (int i = 0; i < suites.size(); i++) {
            CodelessSuiteConfig suite = suites.get(i);
            String propsFile = suite.getPropsFile();
            List<FormattingMap> propsFromCSV = CSVUtils.parseToFormattingMapList(propsFile);
            
            propsFiles.add(propsFile);
            suite.getProps().addAll(propsFromCSV);
            suite.setPropsFile(null);
        }
        
        CodelessSuiteConfigValidator.validate(loadGeneratorConfig, suites);
        
        for (int i = 0; i < suites.size(); i++) {
            suites.get(i).setPropsFile(propsFiles.get(i));
        }
        
        return suites;
    }

    @Override
    protected void runSuite(SuiteInstanceContext suiteInstanceContext) {
        if (shouldBeFinished()) {
            return;
        }

        CodelessSuiteConfig suite = (CodelessSuiteConfig) suiteInstanceContext.getSuiteConfigContext().getSuiteConfig();
        List<FormattingMap> formatters = suite.getProps();
        FormattingMap formatter;

        if (formatters == null || formatters.isEmpty()) {
            formatter = FormattingMap.EMPTY;
        } else {
            formatter = formatters.get(
                    (int) (suiteInstanceContext.getIterationNumber() % formatters.size())
            );
        }

        RemoteWebDriver driver = startRemoteWebDriver(suiteInstanceContext);

        try {
            for (CodelessStepConfig stepConfig : suite.getSteps()) {
                processStep(suiteInstanceContext, stepConfig, formatter, driver);
            }
        } finally {
            quiteRemoteWebDriver(driver, suiteInstanceContext);
        }
    }

    private void processStep(
            SuiteInstanceContext suiteInstanceContext,
            CodelessStepConfig stepConfig,
            FormattingMap formatter,
            RemoteWebDriver driver
    ) {
        if (shouldBeFinished()) {
            return;
        }

        String suiteName = suiteInstanceContext.getSuiteConfigContext().getSuiteConfig().getName();
        String stepName = formatter.format(stepConfig.getName());
        TransactionContext transactionContext = startTransaction(
                suiteInstanceContext,
                "step - " + stepName
        );
        RuntimeException error = null;

        logStepStarted(suiteName, stepName);

        try {
            for (ActionConfig actionConfig : stepConfig.getActions()) {
                ActionProcessor actionProcessor = ActionProcessorsRegistry.INSTANCE.getActionProcessorByName(
                        actionConfig.getActionName()
                );

                processAction(
                        suiteInstanceContext,
                        stepName,
                        actionConfig,
                        actionProcessor,
                        driver,
                        formatter
                );
            }
        } catch (RuntimeException e) {
            error = e;
            throw e;
        } finally {
            finishTransaction(transactionContext, error);
            logStepFinished(suiteName, stepName, error);
        }
    }

    private void processAction(
            SuiteInstanceContext suiteInstanceContext,
            String stepName,
            ActionConfig actionConfig,
            ActionProcessor processor,
            RemoteWebDriver driver,
            FormattingMap formatter
    ) {
        if (shouldBeFinished()) {
            return;
        }

        CodelessSuiteConfig suite = (CodelessSuiteConfig) suiteInstanceContext.getSuiteConfigContext().getSuiteConfig();

        ActionInstance actionInstance = processor.buildActionInstance(
                (CodelessLoadGeneratorConfig) getLoadGeneratorConfig(),
                suite,
                formatter,
                actionConfig
        );
        logActionStarted(stepName, actionInstance);

        try {
            processor.processActionInstance(
                    driver,
                    actionInstance
            );
            logActionFinished(stepName, actionInstance, null);
        } catch (RuntimeException e) {
            logActionFinished(stepName, actionInstance, e);
            throw new RuntimeException(
                    "Step '" + stepName
                    + "' has failed action '" + actionInstance.getConfig().getActionName()
                    + "': " + actionInstance.toLoggingDetails(),
                    e
            );
        }
    }

    private RemoteWebDriver startRemoteWebDriver(SuiteInstanceContext suiteInstanceContext) {
        TransactionContext transactionContext = startTransaction(
                suiteInstanceContext,
                Perforator.OPEN_WEB_DRIVER_TRANSACTION_NAME
        );

        RuntimeException webDriverException = null;
        try {
            return getRemoteWebDriverService().startRemoteWebDriver(
                    suiteInstanceContext,
                    null
            ).getRemoteWebDriver();
        } catch (RuntimeException e) {
            webDriverException = e;
            
            if(!isCancelled() && (logSteps || logActions)) {
                logger.error("There was a problem creating RemoteWebDriver", e);
            }
            
            throw e;
        } finally {
            finishTransaction(transactionContext, webDriverException);
        }
    }

    private void quiteRemoteWebDriver(RemoteWebDriver driver, SuiteInstanceContext suiteInstanceContext) {
        if (driver == null) {
            return;
        }

        TransactionContext transactionContext = startTransaction(
                suiteInstanceContext,
                Perforator.CLOSE_WEB_DRIVER_TRANSACTION_NAME
        );

        RuntimeException webDriverException = null;
        try {
            driver.quit();
        } catch (RuntimeException e) {
            webDriverException = e;
            
            if(!isCancelled() && (logSteps || logActions)) {
                logger.error("There was a problem terminating RemoteWebDriver", e);
            }
            
            throw e;
        } finally {
            finishTransaction(transactionContext, webDriverException);
        }
    }

    private void logStepStarted(String suiteName, String stepName) {
        if (!logSteps) {
            return;
        }

        logger.info(
                "Suite '{}' has started a new step '{}'",
                suiteName,
                stepName
        );
    }

    private void logStepFinished(String suiteName, String stepName, Throwable stepError) {
        if (!logSteps) {
            return;
        }

        if (stepError != null) {
            logger.error(
                    "Suite '{}' has failed step '{}'",
                    suiteName,
                    stepName,
                    stepError
            );
        } else {
            logger.info(
                    "Suite '{}' has completed step '{}'",
                    suiteName,
                    stepName
            );
        }
    }

    private void logActionStarted(String stepName, ActionInstance actionInstance) {
        if (!logActions) {
            return;
        }

        logger.info(
                "Step '{}' has started a new action '{}': {}",
                stepName,
                actionInstance.getConfig().getActionName(),
                actionInstance.toLoggingDetails()
        );
    }

    private void logActionFinished(String stepName, ActionInstance actionInstance, Throwable actionError) {
        if (!logActions) {
            return;
        }

        if (actionError != null) {
            logger.error(
                    "Step '{}' has failed action '{}': {}",
                    stepName,
                    actionInstance.getConfig().getActionName(),
                    actionInstance.toLoggingDetails(),
                    actionError
            );
        } else {
            logger.info(
                    "Step '{}' has completed action '{}'",
                    stepName,
                    actionInstance.getConfig().getActionName()
            );
        }
    }
}
