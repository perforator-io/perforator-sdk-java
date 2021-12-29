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
package io.perforator.sdk.loadgenerator.core;

import io.perforator.sdk.loadgenerator.core.context.SuiteContext;
import io.perforator.sdk.loadgenerator.core.context.TransactionContext;
import io.perforator.sdk.loadgenerator.core.service.RemoteWebDriverService;
import io.perforator.sdk.loadgenerator.core.service.TransactionsService;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Helper class which allows easier integration between target performance tests
 * logic and Perforator load generator.
 */
public final class Perforator {

    /**
     * Common name for the transaction which opens a new 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} 
     * session.
     */
    public static final String OPEN_WEB_DRIVER_TRANSACTION_NAME = "Open browser session";
    /**
     * Common name for the transaction which  
     * {@link org.openqa.selenium.remote.RemoteWebDriver#quit() terminates} 
     * existing 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} session.
     */
    public static final String CLOSE_WEB_DRIVER_TRANSACTION_NAME = "Close browser session";
    
    static final ThreadLocal<SuiteContext> SUITE_CONTEXT = new InheritableThreadLocal<>();

    static final ThreadLocal<RemoteWebDriverService> REMOTE_WEBDRIVER_SERVICE = new InheritableThreadLocal<>();

    static final ThreadLocal<TransactionsService> TRANSACTIONS_SERVICE = new InheritableThreadLocal<>();

    static final ThreadLocal<Map<String, TransactionContext>> TRANSACTIONS = new InheritableThreadLocal<>();

    /**
     * Invoking this method starts a new 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} session.
     * <br>
     *
     * The logic of creating a new session behaves differently in a different 
     * environments:
     * <ul>
     *   <li>
     *     A new instance of {@link org.openqa.selenium.chrome.ChromeDriver ChromeDriver} 
     *     is created if the invoker is executed outside of the load generator 
     *     context. It can be automatically started as 
     *     {@link org.openqa.selenium.chrome.ChromeOptions#setHeadless(boolean) headless}
     *     if a system has a {@link System#getProperty(java.lang.String) property}
     *     <br>
     *     <b>
     *     {@value io.perforator.sdk.loadgenerator.core.configs.SuiteConfig#DEFAULTS_FIELD_PREFIX}.chromeMode=headless
     *     </b>
     *   </li>
     *   <li>
     *     <p>
     *       A new instance of {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}
     *       is created if the invoker is executed inside load generator context.
     *       It's mode is determined according to the value of 
     *       {@link io.perforator.sdk.loadgenerator.core.configs.SuiteConfig#webDriverMode webDriverMode}:
     *     </p>
     *     <ul>
     *       <li>
     *         {@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#local local}
     *         - local {@link org.openqa.selenium.chrome.ChromeDriver ChromeDriver} 
     *       </li>
     *       <li>
     *         {@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#cloud cloud}
     *         - cloud based {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * @return a new {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} session.
     */
    public static final RemoteWebDriver startRemoteWebDriver() {
        return startRemoteWebDriver(null);
    }

    /**
     * Invoking this method starts a new 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} session.
     * <br>
     *
     * The logic of creating a new session behaves differently in a different 
     * environments:
     * <ul>
     *   <li>
     *     A new instance of {@link org.openqa.selenium.chrome.ChromeDriver ChromeDriver} 
     *     is created if the invoker is executed outside of the load generator 
     *     context. It can be automatically started as 
     *     {@link org.openqa.selenium.chrome.ChromeOptions#setHeadless(boolean) headless}
     *     if a system has a {@link System#getProperty(java.lang.String) property}
     *     <br>
     *     <b>
     *     {@value io.perforator.sdk.loadgenerator.core.configs.SuiteConfig#DEFAULTS_FIELD_PREFIX}.chromeMode=headless
     *     </b>
     *   </li>
     *   <li>
     *     <p>
     *       A new instance of {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}
     *       is created if the invoker is executed inside load generator context.
     *       It's mode is determined according to the value of 
     *       {@link io.perforator.sdk.loadgenerator.core.configs.SuiteConfig#webDriverMode webDriverMode}:
     *     </p>
     *     <ul>
     *       <li>
     *         {@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#local local}
     *         - local {@link org.openqa.selenium.chrome.ChromeDriver ChromeDriver} 
     *       </li>
     *       <li>
     *         {@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#cloud cloud}
     *         - cloud based {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}
     *       </li>
     *     </ul>
     *   </li>
     * </ul>
     * @param capabilities additional capabilities to be applied while creating 
     * a new {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}
     * @return a new {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} session.
     */
    public static final RemoteWebDriver startRemoteWebDriver(Capabilities capabilities) {
        SuiteContext suiteContext = SUITE_CONTEXT.get();
        
        if (suiteContext == null) {
            return RemoteWebDriverHelper.createLocalChromeDriver(capabilities);
        }

        RemoteWebDriverService remoteWebDriverService = REMOTE_WEBDRIVER_SERVICE.get();
        if (remoteWebDriverService == null) {
            throw new RuntimeException(
                    "Can't create RemoteWebDriver due to integration problem - REMOTE_WEBDRIVER_SERVICE is not propagated"
            );
        }

        return remoteWebDriverService.startRemoteWebDriver(
                suiteContext,
                capabilities
        ).getRemoteWebDriver();
    }

    /**
     * Starts a new transaction.
     * <br>
     * 
     * Transaction data is automatically reported to the Perforator platform,
     * if the currently executed suite instance is started in the 
     * {@link io.perforator.sdk.loadgenerator.core.configs.WebDriverMode#cloud cloud} mode.
     * <br>
     * 
     * Transactions are not reported to the Perforator platform in case the caller
     * is executed outside of the load generator context, for example, as a part of
     * unit tests.
     * 
     * @param transactionName a name of the new transaction.
     * @return transaction id.
     */
    public static final String startTransaction(String transactionName) {
        SuiteContext suiteContext = SUITE_CONTEXT.get();
        
        if (suiteContext == null) {
            return UUID.randomUUID().toString();
        }

        TransactionsService transactionsService = TRANSACTIONS_SERVICE.get();
        if (transactionsService == null) {
            throw new RuntimeException(
                    "Can't start transaction due to integration problem - TRANSACTIONS_SERVICE is not propagated"
            );
        }

        TransactionContext transaction = transactionsService.startTransaction(suiteContext, transactionName);
        TRANSACTIONS.get().put(transaction.getTransactionID(), transaction);
        return transaction.getTransactionID();
    }

    /**
     * Completes existing transaction.
     * <br>
     * Transaction is marked as failed if transactionError is not null.
     * @param transactionId id of the transaction.
     * @param transactionError optional error happened as a part of transaction 
     * processing.
     */
    public static final void finishTransaction(String transactionId, Throwable transactionError) {
        SuiteContext suiteContext = SUITE_CONTEXT.get();
        
        if (suiteContext == null) {
            return;
        }

        TransactionsService transactionsService = TRANSACTIONS_SERVICE.get();
        if (transactionsService == null) {
            throw new RuntimeException(
                    "Can't finish transaction due to integration problem - TRANSACTIONS_SERVICE is not propagated"
            );
        }

        TransactionContext transaction = TRANSACTIONS.get().remove(transactionId);
        if (transaction == null) {
            throw new RuntimeException(
                    "Can't finish transaction " + transactionId + " because it was not started in the current thread"
            );
        }

        transactionsService.finishTransaction(transaction, transactionError);
    }

    /**
     * Completes existing transaction and marks it as successful.
     * @param transactionId id of the transaction.
     */
    public static final void finishTransaction(String transactionId) {
        finishTransaction(transactionId, null);
    }

    /**
     * Executes supplied {@link Runnable} with automatic transaction reporting:
     * <ol>
     *   <li>
     *     Transaction is started before {@link Runnable#run() run} method is invoked.
     *   </li>
     *   <li>
     *     Transaction is finished once {@link Runnable#run() run} method is completed.<br>
     *     Transaction is marked as failed in case of any error happened during 
     *     {@link Runnable#run() run} method call.
     *   </li>
     * </ol>
     * @param transactionName a name of the transaction to be created.
     * @param transactional {@link Runnable#run() } / lambda to be executed.
     */
    public static final void transactionally(String transactionName, Runnable transactional) {
        String transactionID = startTransaction(transactionName);

        try {
            transactional.run();
            finishTransaction(transactionID);
        } catch (RuntimeException e) {
            finishTransaction(transactionID, e);
            throw e;
        }
    }

    /**
     * Executes provided {@link Consumer} and returns its result with automatic 
     * transaction reporting:
     * <ol>
     *   <li>
     *     Transaction is started before {@link Consumer#accept(java.lang.Object) accept} 
     *     method is invoked.
     *   </li>
     *   <li>
     *     Transaction is finished once {@link Consumer#accept(java.lang.Object) accept} 
     *     method is completed.<br>
     *     Transaction is marked as failed in case of any error happened during 
     *     {@link Consumer#accept(java.lang.Object) accept}  method call.
     *   </li>
     * </ol>
     * 
     * @param <T> type of the {@link Consumer} argument.
     * @param transactionName a name of the transaction to be created.
     * @param arg argument to be applied on {@link Consumer}.
     * @param consumer {@link Consumer} to be executed.
     */
    public static final <T> void transactionally(String transactionName, T arg, Consumer<T> consumer) {
        String transactionID = startTransaction(transactionName);

        try {
            consumer.accept(arg);
            finishTransaction(transactionID);
        } catch (RuntimeException e) {
            finishTransaction(transactionID, e);
            throw e;
        }
    }

    /**
     * Executes provided {@link Supplier} and returns its result with automatic 
     * transaction reporting:
     * <ol>
     *   <li>
     *     Transaction is started before {@link Supplier#get() get} method
     *     is invoked.
     *   </li>
     *   <li>
     *     Transaction is finished once {@link Supplier#get() get} method
     *     is completed.<br>
     *     Transaction is marked as failed in case of any error happened during 
     *     {@link Supplier#get() get} method call.
     *   </li>
     * </ol> 
     * 
     * @param <T> type of the returned result.
     * @param transactionName a name of the transaction to be created.
     * @param supplier {@link Supplier} to be executed.
     * @return result of the {@link Supplier#get() } 
     */
    public static final <T> T transactionally(String transactionName, Supplier<T> supplier) {
        String transactionID = startTransaction(transactionName);

        try {
            T result = supplier.get();
            finishTransaction(transactionID);
            return result;
        } catch (RuntimeException e) {
            finishTransaction(transactionID, e);
            throw e;
        }
    }

    /**
     * Executes provided {@link Function} and returns its result with automatic 
     * transaction reporting:
     * <ol>
     *   <li>
     *     Transaction is started before {@link Function#apply(java.lang.Object) apply} 
     *     method is invoked.
     *   </li>
     *   <li>
     *     Transaction is finished once {@link Function#apply(java.lang.Object) apply}
     *     method is completed.<br>
     *     Transaction is marked as failed in case of any error happened during 
     *     {@link Function#apply(java.lang.Object) apply} method call.
     *   </li>
     * </ol> 
     * 
     * @param <T> type of the {@link Function} argument.
     * @param <R> type of the returned result.
     * @param transactionName a name of the transaction to be created.
     * @param arg argument to be passed to {@link Function} call.
     * @param function {@link Function} to be executed
     * @return result of the {@link Function#apply(java.lang.Object) Function#apply} 
     */
    public static final <T, R> R transactionally(String transactionName, T arg, Function<T, R> function) {
        String transactionID = startTransaction(transactionName);

        try {
            R result = function.apply(arg);
            finishTransaction(transactionID);
            return result;
        } catch (RuntimeException e) {
            finishTransaction(transactionID, e);
            throw e;
        }
    }

}
