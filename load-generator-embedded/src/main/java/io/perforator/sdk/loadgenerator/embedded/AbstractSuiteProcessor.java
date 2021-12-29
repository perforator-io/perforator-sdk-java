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
package io.perforator.sdk.loadgenerator.embedded;

import io.perforator.sdk.loadgenerator.core.Perforator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * Abstract suite processor which automatically opens 
 * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} 
 * session before suite instance processing starts and closes it once 
 * processing is completed.
 * <br>
 * 
 * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} is opened 
 * for every suite instance, so the amount of open 
 * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} sessions 
 * at any given moment of time is equal to the target concurrency of the 
 * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig EmbeddedSuiteConfig}.
 * <br>
 * 
 * Target class extending 
 * {@link io.perforator.sdk.loadgenerator.embedded.AbstractSuiteProcessor AbstractSuiteProcessor} 
 * should implement only one method 
 * {@link io.perforator.sdk.loadgenerator.embedded.AbstractSuiteProcessor#processSuite(long, java.lang.String, io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig, org.openqa.selenium.remote.RemoteWebDriver) processSuite},
 * which is invoked using a reference to currently processed 
 * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig EmbeddedSuiteConfig} 
 * and a reference to automatically opened 
 * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}.
 * <br>
 * 
 * <b>Important:</b> please don't manually open/close 
 * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}, since it 
 * is managed automatically. If you would like to have more granular control 
 * on when to open/close {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} 
 * - please implement your suite processor directly from the 
 * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteProcessor EmbeddedSuiteProcessor} interface.
 */
public abstract class AbstractSuiteProcessor implements EmbeddedSuiteProcessor {
    
    private static final ConcurrentHashMap<String, RemoteWebDriver> REMOTE_WEB_DRIVERS = new ConcurrentHashMap<>();
    
    /**
     * Abstract method which is responsible for suite instance processing.<br>
     * It is executed when a new selenium session(
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}) is created 
     * for the currently processed suite instance.<br>
     * 
     * <b>Important:</b> please don't manually open/close 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver}, since 
     * it is managed automatically. If you would like to have more granular control 
     * on when to open/close 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} - please 
     * implement your suite processor directly from the 
     * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteProcessor EmbeddedSuiteProcessor} 
     * interface.
     * 
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig a reference to the 
     * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig configuration} 
     * of the currently executed suite instance.
     * @param remoteWebDriver a reference to 
     * {@link org.openqa.selenium.remote.RemoteWebDriver RemoteWebDriver} which 
     * is automatically opened before suite instance processing starts and 
     * automatically closed once suite instance processing is completed.
     */
    protected abstract void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, RemoteWebDriver remoteWebDriver);
    
    /**
     * Build additional capabilities to be applied on {@link RemoteWebDriver}.
     * 
     * By default this method returns null, so no additional capabilities are applied,
     * but overriding methods can return something more specific.
     * 
     * This method is called internally when preparing new {@link RemoteWebDriver}
     * instance as a part of 
     * {@link AbstractSuiteProcessor#onBeforeSuite(long, java.lang.String, io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig) onBeforeSuite}
     * method invocation.
     * 
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig a reference to the 
     * {@link io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig configuration} 
     * of the currently executed suite instance.
     * @return additional {@link Capabilities} to be applied on {@link RemoteWebDriver}
     */
    protected Capabilities buildDefaultCapabilities(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig) {
        return null;
    }

    /**
     * Automatically opens a new {@link RemoteWebDriver} session before processing
     * suite instance.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     */
    @Override
    public final void onBeforeSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig) {
        transactionally(
                Perforator.OPEN_WEB_DRIVER_TRANSACTION_NAME, 
                () -> REMOTE_WEB_DRIVERS.put(
                        suiteInstanceID, 
                        Perforator.startRemoteWebDriver(
                                buildDefaultCapabilities(iterationNumber, suiteInstanceID, suiteConfig)
                        )
                )
        );
    }

    /**
     * Invokes overloaded {@link AbstractSuiteProcessor#processSuite(long, java.lang.String, io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig, org.openqa.selenium.remote.RemoteWebDriver) processSuite}
     * method using already opened {@link RemoteWebDriver}.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     */
    @Override
    public final void processSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig) {
        processSuite(iterationNumber, suiteInstanceID, suiteConfig, REMOTE_WEB_DRIVERS.get(suiteInstanceID));
    }

    /**
     * Automatically terminates {@link RemoteWebDriver} session at the end of the
     * suite instance processing.
     * @param iterationNumber suite iteration number. It starts from 0 incrementing 
     * for every new suite instance execution. Such counter is maintained on a 
     * suite level, so counters from different suites are incremented independently.
     * @param suiteInstanceID ID of the invoked suite instance.
     * @param suiteConfig {@link EmbeddedSuiteConfig configuration} of the invoked suite instance.
     * @param optionalSuiteProcessingError optional error if invoked
     * {@link EmbeddedSuiteProcessor#processSuite(long, java.lang.String, io.perforator.sdk.loadgenerator.embedded.EmbeddedSuiteConfig) processSuite}
     * method resulted with exception.
     */
    @Override
    public final void onAfterSuite(long iterationNumber, String suiteInstanceID, EmbeddedSuiteConfig suiteConfig, Throwable optionalSuiteProcessingError) {
        RemoteWebDriver driver = REMOTE_WEB_DRIVERS.remove(suiteInstanceID);

        if (driver != null) {
            transactionally(
                    Perforator.CLOSE_WEB_DRIVER_TRANSACTION_NAME,
                    () -> driver.quit()
            );
        }
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
    protected final String startTransaction(String transactionName) {
        return Perforator.startTransaction(transactionName);
    }
    
    
    /**
     * Completes existing transaction and marks it as successful.
     * @param transactionId id of the transaction.
     */
    protected final void finishTransaction(String transactionId) {
        Perforator.finishTransaction(transactionId);
    }
    
    /**
     * Completes existing transaction.
     * <br>
     * Transaction is marked as failed if transactionError is not null.
     * @param transactionId id of the transaction.
     * @param transactionError optional error happened as a part of transaction 
     * processing.
     */
    protected final void finishTransaction(String transactionId, Throwable transactionError) {
        Perforator.finishTransaction(transactionId, transactionError);
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
    protected final void transactionally(String transactionName, Runnable transactional) {
        Perforator.transactionally(transactionName, transactional);
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
    protected final <T> void transactionally(String transactionName, T arg, Consumer<T> consumer) {
        Perforator.transactionally(transactionName, arg, consumer);
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
    protected final <T> T transactionally(String transactionName, Supplier<T> supplier) {
        return Perforator.transactionally(transactionName, supplier);
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
    protected final <T, R> R transactionally(String transactionName, T arg, Function<T, R> function) {
        return Perforator.transactionally(transactionName, arg, function);
    }
    
}
