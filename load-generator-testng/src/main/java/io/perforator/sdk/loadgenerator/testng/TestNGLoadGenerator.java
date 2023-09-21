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
package io.perforator.sdk.loadgenerator.testng;

import io.perforator.sdk.loadgenerator.core.AbstractLoadGenerator;
import io.perforator.sdk.loadgenerator.core.configs.LoadGeneratorConfig;
import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;
import io.perforator.sdk.loadgenerator.core.context.SuiteInstanceContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import io.perforator.sdk.loadgenerator.core.service.IntegrationService;
import org.testng.*;
import org.testng.xml.Parser;
import org.testng.xml.XmlSuite;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class TestNGLoadGenerator extends AbstractLoadGenerator {

    static {
        System.setProperty("testng.memory.friendly", "true");
    }
    
    public TestNGLoadGenerator(LoadGeneratorConfig loadGeneratorConfig, List<TestNGSuiteConfig> suites) {
        this(null, loadGeneratorConfig, suites);
    }

    private TestNGLoadGenerator(IntegrationService mediator, LoadGeneratorConfig loadGeneratorConfig, List<TestNGSuiteConfig> suites) {
        super(mediator, loadGeneratorConfig, copy(suites));

        if (suites == null || suites.isEmpty()) {
            throw new RuntimeException("suites are required");
        }

        for (TestNGSuiteConfig suite : suites) {
            String suiteLocation = suite.getSuiteXmlFile();
            if (suiteLocation == null || suiteLocation.isBlank()) {
                throw new RuntimeException(
                        SuiteConfig.DEFAULTS_FIELD_PREFIX
                        + "."
                        + TestNGSuiteConfig.Fields.suiteXmlFile
                        + " is required"
                );
            }

            File suiteFile = Path.of(suiteLocation).toFile();
            if (!suiteFile.exists()) {
                throw new RuntimeException(
                        "suite " + suiteLocation + " doesn't exist"
                );
            }

            if (!suiteFile.canRead()) {
                throw new RuntimeException(
                        "suite " + suiteLocation + " can't be read"
                );
            }

            List<XmlSuite> parsedSuites;

            try {
                parsedSuites = new Parser(suiteLocation).parseToList();
            } catch (IOException e) {
                throw new RuntimeException(
                        "Can't parse TestNG suite xml: " + suiteLocation,
                        e
                );
            }
            
            if(logger.isDebugEnabled()) {
                logger.info(
                        "Suite {} has been validated successfully => {}", 
                        suiteLocation,
                        parsedSuites
                );
            }
        }
    }

    @Override
    protected void runSuite(SuiteInstanceContext suiteInstanceContext) {
        TestNGSuiteConfig suiteConfig = (TestNGSuiteConfig) suiteInstanceContext.getSuiteConfigContext().getSuiteConfig();

        TestNG testNG = new TestNG(false);
        testNG.addListener(new TestListener(suiteInstanceContext));
        testNG.addListener(new TestMethodListener(suiteInstanceContext));
        testNG.setTestSuites(
                Collections.singletonList(suiteConfig.getSuiteXmlFile())
        );
        testNG.setSuiteThreadPoolSize(1);
        testNG.setRandomizeSuites(false);
        testNG.setVerbose(0);
        testNG.setParallel(XmlSuite.ParallelMode.NONE);

        testNG.run();

        if (testNG.hasFailure()) {
            throw new RuntimeException("Suite has test failures");
        }
    }

    private class TestMethodListener implements IInvokedMethodListener {

        private final Stack<TransactionContext> transactions = new Stack<>();
        private final SuiteInstanceContext suiteInstanceContext;

        public TestMethodListener(SuiteInstanceContext suiteInstanceContext) {
            this.suiteInstanceContext = suiteInstanceContext;
        }

        @Override
        public void beforeInvocation(IInvokedMethod method, ITestResult testResult, ITestContext context) {
            if (shouldBeFinished()) {
                throw new RuntimeException(TERMINATION_EXCEPTION_MESSAGE);
            }

            StringBuilder transactionNameStb = new StringBuilder(method.isConfigurationMethod() ? "setup - " : "method - ");
            transactionNameStb.append(method.getTestMethod().getQualifiedName());

            if (testResult.getParameters().length > 0) {
                transactionNameStb.append("; params - ")
                        .append(Arrays.toString(testResult.getParameters()));
            }

            String transactionName = transactionNameStb.toString();

            if (transactionName.length() > 1024) {
                transactionName = transactionNameStb.substring(1021) + "...";
            }

            TransactionContext transactionContext = startTransaction(
                    suiteInstanceContext,
                    transactionName
            );
            transactions.push(transactionContext);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Starting new test method {} using transaction {}",
                        method.getTestMethod().getQualifiedName(),
                        transactionContext.getTransactionID()
                );
            }
        }

        @Override
        public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
            TransactionContext transactionContext = null;
            if (!transactions.isEmpty()) {
                transactionContext = transactions.pop();
            }

            if (transactionContext == null) {
                if (!shouldBeFinished()) {
                    logger.warn(
                            "Test method {} has been completed but it doesn't have associated transaction",
                            method.getTestMethod().getQualifiedName()
                    );
                }
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Test {} has been completed using transaction {}",
                        method.getTestMethod().getQualifiedName(),
                        transactionContext.getTransactionID()
                );
            }

            finishTransaction(
                    transactionContext,
                    testResult.getThrowable()
            );
        }
    }

    private class TestListener implements ITestListener {

        private final Stack<TransactionContext> transactions = new Stack();
        private final SuiteInstanceContext suiteInstanceContext;

        public TestListener(SuiteInstanceContext suiteInstanceContext) {
            this.suiteInstanceContext = suiteInstanceContext;
        }

        @Override
        public void onStart(ITestContext context) {
            if (shouldBeFinished()) {
                throw new RuntimeException(TERMINATION_EXCEPTION_MESSAGE);
            }

            TransactionContext transactionContext = startTransaction(
                    suiteInstanceContext,
                    "test - " + context.getName()
            );
            transactions.push(transactionContext);

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Starting new xml test {} using transaction {}",
                        context.getName(),
                        transactionContext.getTransactionID()
                );
            }
        }

        @Override
        public void onFinish(ITestContext context) {
            TransactionContext transactionContext = null;
            if (!transactions.isEmpty()) {
                transactionContext = transactions.pop();
            }
            if (transactionContext == null) {
                if (!shouldBeFinished()) {
                    logger.warn(
                            "Xml test {} has been completed but it doesn't have associated transaction",
                            context.getName()
                    );
                }
                return;
            }

            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Xml test {} has been completed using transaction {}",
                        context.getName(),
                        transactionContext.getTransactionID()
                );
            }

            if (context.getFailedTests() != null && context.getFailedTests().size() > 0) {
                finishTransaction(
                        transactionContext,
                        new RuntimeException("There are " + context.getFailedTests().size() + " failed tests")
                );
            } else {
                finishTransaction(
                        transactionContext,
                        null
                );
            }
        }
    }
    
}
