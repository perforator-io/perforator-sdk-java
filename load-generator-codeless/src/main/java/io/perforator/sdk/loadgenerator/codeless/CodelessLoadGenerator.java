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
import io.perforator.sdk.loadgenerator.codeless.actions.IgnoreRemainingActionsActionInstance;
import io.perforator.sdk.loadgenerator.codeless.actions.IgnoreRemainingStepsActionInstance;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessConfigFactory;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessLoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessStepConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfig;
import io.perforator.sdk.loadgenerator.codeless.config.CodelessSuiteConfigValidator;
import io.perforator.sdk.loadgenerator.core.AbstractLoadGenerator;
import io.perforator.sdk.loadgenerator.core.Perforator;
import io.perforator.sdk.loadgenerator.core.context.RemoteWebDriverContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteConfigContext;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.openqa.selenium.remote.RemoteWebDriver;

public class CodelessLoadGenerator extends AbstractLoadGenerator {

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
        super(mediator, loadGeneratorConfig, (List) CodelessSuiteConfigValidator.validate(loadGeneratorConfig, suites));
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
                ProceedingContext proceedingContext = processStep(
                        suiteInstanceContext, 
                        stepConfig, 
                        formatter, 
                        driver
                );
                
                if(!proceedingContext.proceedSteps) {
                    break;
                }
            }
        } finally {
            quiteRemoteWebDriver(driver, suiteInstanceContext);
        }
    }

    private ProceedingContext processStep(
            SuiteInstanceContext suiteInstanceContext,
            CodelessStepConfig stepConfig,
            FormattingMap formatter,
            RemoteWebDriver driver
    ) {
        if (shouldBeFinished()) {
            return ProceedingContext.SKIP_ACTIONS_AND_STEPS;
        }
        
        String suiteName = suiteInstanceContext.getSuiteConfigContext().getSuiteConfig().getName();
        String stepName = formatter.format(stepConfig.getName());
        TransactionContext transactionContext = startTransaction(
                suiteInstanceContext,
                "step - " + stepName
        );
        RuntimeException error = null;

        logStepStarted(suiteInstanceContext, suiteName, stepName);

        try {
            for (ActionConfig actionConfig : stepConfig.getActions()) {
                ActionProcessor actionProcessor = ActionProcessorsRegistry.INSTANCE.getActionProcessorByName(
                        actionConfig.getActionName()
                );

                ProceedingContext proceedingContext = processAction(
                        suiteInstanceContext,
                        stepName,
                        actionConfig,
                        actionProcessor,
                        driver,
                        formatter
                );
                
                if(!proceedingContext.proceedSteps) {
                    return ProceedingContext.SKIP_ACTIONS_AND_STEPS;
                } else if(!proceedingContext.proceedActions) {
                    break;
                }
            }
            
            return ProceedingContext.PROCEED_ALL;
        } catch (RuntimeException e) {
            error = e;
            throw e;
        } finally {
            finishTransaction(transactionContext, error);
            logStepFinished(suiteInstanceContext, suiteName, stepName, error);
        }
    }

    private ProceedingContext processAction(
            SuiteInstanceContext suiteInstanceContext,
            String stepName,
            ActionConfig actionConfig,
            ActionProcessor processor,
            RemoteWebDriver driver,
            FormattingMap formatter
    ) {
        if (shouldBeFinished()) {
            return ProceedingContext.SKIP_ACTIONS_AND_STEPS;
        }

        CodelessSuiteConfig suite = (CodelessSuiteConfig) suiteInstanceContext.getSuiteConfigContext().getSuiteConfig();

        ActionInstance actionInstance = processor.buildActionInstance(
                (CodelessLoadGeneratorConfig) getLoadGeneratorConfig(),
                suite,
                formatter,
                actionConfig
        );
        
        if(actionInstance instanceof IgnoreRemainingActionsActionInstance) {
            if(actionInstance.isEnabled()) {
                return ProceedingContext.SKIP_ACTIONS_ONLY;
            }
        }
        
        if(actionInstance instanceof IgnoreRemainingStepsActionInstance) {
            if(actionInstance.isEnabled()) {
                return ProceedingContext.SKIP_ACTIONS_AND_STEPS;
            }
        }

        if(!actionInstance.isEnabled()) {
            return ProceedingContext.PROCEED_ALL;
        }
        
        logActionStarted(suiteInstanceContext, stepName, actionInstance);

        try {
            processor.processActionInstance(
                    driver,
                    actionInstance
            );
            
            logActionFinished(
                    suiteInstanceContext,
                    stepName, 
                    actionInstance, 
                    null
            );
            
            return ProceedingContext.PROCEED_ALL;
        } catch (RuntimeException e) {
            logActionFinished(
                    suiteInstanceContext, 
                    stepName, 
                    actionInstance, 
                    e
            );
            
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
            
            if(!isCancelled() && (isLogSteps(suiteInstanceContext) || isLogActions(suiteInstanceContext))) {
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
            
            if(!isCancelled() && (isLogSteps(suiteInstanceContext) || isLogActions(suiteInstanceContext))) {
                logger.error("There was a problem terminating RemoteWebDriver", e);
            }
            
            throw e;
        } finally {
            finishTransaction(transactionContext, webDriverException);
        }
    }

    private void logStepStarted(SuiteInstanceContext suiteInstanceContext, String suiteName, String stepName) {
        if (!isLogSteps(suiteInstanceContext)) {
            return;
        }

        logger.info(
                "Suite '{}' has started a new step '{}'",
                suiteName,
                stepName
        );
    }

    private void logStepFinished(SuiteInstanceContext suiteInstanceContext, String suiteName, String stepName, Throwable stepError) {
        if (!isLogSteps(suiteInstanceContext)) {
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

    private void logActionStarted(SuiteInstanceContext suiteInstanceContext, String stepName, ActionInstance actionInstance) {
        if (!isLogActions(suiteInstanceContext)) {
            return;
        }

        logger.info(
                "Step '{}' has started a new action '{}': {}",
                stepName,
                actionInstance.getConfig().getActionName(),
                actionInstance.toLoggingDetails()
        );
    }

    private void logActionFinished(SuiteInstanceContext suiteInstanceContext, String stepName, ActionInstance actionInstance, Throwable actionError) {
        if (!isLogActions(suiteInstanceContext)) {
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
    
    private boolean isLogActions(SuiteInstanceContext suiteInstanceContext) {
        return ((CodelessSuiteConfig)suiteInstanceContext.getSuiteConfigContext().getSuiteConfig()).isLogActions();
    }
    
    private boolean isLogSteps(SuiteInstanceContext suiteInstanceContext) {
        return ((CodelessSuiteConfig)suiteInstanceContext.getSuiteConfigContext().getSuiteConfig()).isLogSteps();
    }
    
    private static enum ProceedingContext {
        
        SKIP_ACTIONS_AND_STEPS(false,false),
        SKIP_ACTIONS_ONLY(false,true),
        PROCEED_ALL(true,true);
        
        private final boolean proceedActions;
        private final boolean proceedSteps;

        private ProceedingContext(boolean proceedActions, boolean proceedSteps) {
            this.proceedActions = proceedActions;
            this.proceedSteps = proceedSteps;
        }
        
    }
}
