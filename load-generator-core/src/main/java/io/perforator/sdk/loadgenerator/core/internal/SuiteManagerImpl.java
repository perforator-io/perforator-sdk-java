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
package io.perforator.sdk.loadgenerator.core.internal;

import io.perforator.sdk.loadgenerator.core.configs.SuiteConfig;

import java.time.Duration;
import java.time.format.DateTimeParseException;

final class SuiteManagerImpl implements SuiteManager {
    
    private final Duration minDuration = buildMinDuration();
    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;

    public SuiteManagerImpl(TimeProvider timeProvider, EventsRouter eventsRouter) {
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
    }

    @Override
    public void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        for (SuiteConfigContextImpl suiteConfig : loadGeneratorContext.getSuiteConfigContexts()) {
            validate(suiteConfig.getSuiteConfig());
        }
    }

    @Override
    public SuiteInstanceContextImpl startSuiteInstance(int workerID, LoadGeneratorContextImpl loadGeneratorContext, SuiteConfigContextImpl suiteConfigContext) {
        if (suiteConfigContext == null) {
            throw new IllegalArgumentException(
                    "suiteConfigContext should not be null"
            );
        }
        if (suiteConfigContext.getSuiteConfig() == null) {
            throw new IllegalArgumentException(
                    "suiteConfig should not be null"
            );
        }
        
        long timestamp = timeProvider.getCurrentTime();
        SuiteInstanceContextImpl result = new SuiteInstanceContextImpl(
                workerID,
                timestamp,
                suiteConfigContext.getConcurrencyContext().getAndIncrementIterationsCounter(),
                loadGeneratorContext,
                suiteConfigContext
        );
        suiteConfigContext.getSuiteInstanceContexts().add(result);
        eventsRouter.onSuiteInstanceStarted(timestamp, result);
        return result;
    }

    @Override
    public void stopSuiteInstance(SuiteInstanceContextImpl suiteContext, Throwable error) {
        stopSuiteInstance(
                timeProvider.getCurrentTime(),
                suiteContext,
                error
        );
    }

    private void stopSuiteInstance(long timestamp, SuiteInstanceContextImpl suiteContext, Throwable error) {
        if (suiteContext == null) {
            throw new IllegalArgumentException(
                    "suiteInstance should not be null"
            );
        }

        if (suiteContext.getSuiteConfigContext().getSuiteInstanceContexts().remove(suiteContext)) {
            eventsRouter.onSuiteInstanceFinished(
                    timestamp,
                    suiteContext,
                    error
            );
        }
    }

    @Override
    public void onLoadGeneratorFinished(long timestamp, LoadGeneratorContextImpl loadGeneratorContext, Throwable error) {
        for (SuiteConfigContextImpl suiteConfigContext : loadGeneratorContext.getSuiteConfigContexts()) {
            for (SuiteInstanceContextImpl suiteInstanceContext: suiteConfigContext.getSuiteInstanceContexts()){
                stopSuiteInstance(
                        timestamp,
                        suiteInstanceContext,
                        error
                );
            }
        }
    }

    private void validate(SuiteConfig suiteConfig) {
        if (suiteConfig == null) {
            throw new IllegalArgumentException("suiteConfig is required");
        }

        if (suiteConfig.getName() == null || suiteConfig.getName().isBlank()) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.name
                            + " should not be blank"
            );
        }

        if (suiteConfig.getConcurrency() < 1) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.concurrency
                            + "(" + suiteConfig.getConcurrency() + ")"
                            + " should not be < 1"
            );
        }

        if (suiteConfig.getDuration() == null) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.duration
                            + " should not be null"
            );
        }

        if (suiteConfig.getDuration().compareTo(minDuration) < 0) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                    + "."
                    + SuiteConfig.Fields.duration
                    + "("
                    + suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")
                    + ")"
                    + " should be >= "
                    + minDuration.toString().toLowerCase().replace("pt", "")
            );
        }

        if (suiteConfig.getDelay() == null) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.delay
                            + " should not be null"
            );
        }

        if (suiteConfig.getDelay().isNegative()) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.delay
                            + "("
                            + suiteConfig.getDelay().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be < 0 seconds"
            );
        }

        if (suiteConfig.getDelay().compareTo(suiteConfig.getDuration()) > 0) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.delay
                            + "("
                            + suiteConfig.getDelay().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be > "
                            + SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.duration
                            + "("
                            + suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")
                            + ")"
            );
        }

        if (suiteConfig.getRampUp() == null) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampUp
                            + " should not be null"
            );
        }

        if (suiteConfig.getRampUp().isNegative()) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampUp
                            + "("
                            + suiteConfig.getRampUp().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be < 0 seconds"
            );
        }

        if (suiteConfig.getRampUp().compareTo(suiteConfig.getDuration()) > 0) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampUp
                            + "("
                            + suiteConfig.getRampUp().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be > "
                            + SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.duration
                            + "("
                            + suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")
                            + ")"
            );
        }

        if (suiteConfig.getRampDown() == null) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampDown
                            + " should not be null"
            );
        }

        if (suiteConfig.getRampDown().isNegative()) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampDown
                            + "("
                            + suiteConfig.getRampDown().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be < 0 seconds"
            );
        }

        if (suiteConfig.getRampDown().compareTo(suiteConfig.getDuration()) >= 0) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampDown
                            + "("
                            + suiteConfig.getRampDown().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should not be >= "
                            + SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.duration
                            + "("
                            + suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")
                            + ")"
            );
        }

        Duration fullConcurrencyDuration = suiteConfig.getDuration().minus(
                suiteConfig.getRampUp().plus(suiteConfig.getRampDown())
        );
        if (fullConcurrencyDuration.isNegative()) {
            throw new IllegalArgumentException(
                    SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.duration
                            + "("
                            + suiteConfig.getDuration().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + " should be >= "
                            + SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampUp
                            + "("
                            + suiteConfig.getRampUp().toString().toLowerCase().replace("pt", "")
                            + ")"
                            + "+"
                            + SuiteConfig.DEFAULTS_FIELD_PREFIX
                            + "."
                            + SuiteConfig.Fields.rampDown
                            + "("
                            + suiteConfig.getRampDown().toString().toLowerCase().replace("pt", "")
                            + ")"
            );
        }
    }
    
    private static Duration buildMinDuration() {
        String propertyName = SuiteManagerImpl.class.getName() + ".minDuration";
        String propertyValue = System.getProperty(propertyName);
        
        if(propertyValue == null || propertyValue.isBlank()) {
            return Duration.ofMinutes(1);
        }
        try {
            return Duration.parse(propertyValue);
        } catch(DateTimeParseException e) {
            return Duration.ofMinutes(1);
        }
    }

}
