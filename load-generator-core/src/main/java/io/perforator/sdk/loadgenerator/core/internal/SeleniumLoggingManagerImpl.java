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

class SeleniumLoggingManagerImpl implements SeleniumLoggingManager {
    
    private static final String SELENIUM_PACKAGE_NAME = "org.openqa.selenium";
    private static final java.util.logging.Logger JUL_LOGGER = java.util.logging.Logger.getLogger(SELENIUM_PACKAGE_NAME);
    private static final org.slf4j.Logger SLF4J_LOGGER = org.slf4j.LoggerFactory.getLogger(SELENIUM_PACKAGE_NAME);

    @Override
    public synchronized void onLoadGeneratorStarted(long timestamp, LoadGeneratorContextImpl loadGeneratorContext) {
        java.util.logging.Level julLevel;
        
        if(SLF4J_LOGGER.isTraceEnabled()) {
            julLevel = java.util.logging.Level.FINEST;
        } else if(SLF4J_LOGGER.isDebugEnabled()) {
            julLevel = java.util.logging.Level.FINE;
        } else if(SLF4J_LOGGER.isInfoEnabled()) {
            julLevel = java.util.logging.Level.INFO;
        } else if(SLF4J_LOGGER.isWarnEnabled()) {
            julLevel = java.util.logging.Level.WARNING;
        } else if(SLF4J_LOGGER.isErrorEnabled()) {
            julLevel = java.util.logging.Level.SEVERE;
        } else {
            julLevel = java.util.logging.Level.OFF;
        }
        
        for (java.util.logging.Handler handler : JUL_LOGGER.getHandlers()) {
            JUL_LOGGER.removeHandler(handler);
        }
        
        JulToSlf4jHandler julHandler = new JulToSlf4jHandler();
        julHandler.setLevel(julLevel);
        
        JUL_LOGGER.setUseParentHandlers(false);
        JUL_LOGGER.addHandler(julHandler);
        JUL_LOGGER.setLevel(julLevel);
    }
    
    private static class JulToSlf4jHandler extends java.util.logging.Handler {
        
        private static final int TRACE_THRESHOLD = java.util.logging.Level.FINEST.intValue();
        private static final int DEBUG_THRESHOLD = java.util.logging.Level.FINE.intValue();
        private static final int INFO_THRESHOLD = java.util.logging.Level.INFO.intValue();
        private static final int WARN_THRESHOLD = java.util.logging.Level.WARNING.intValue();
        private static final int ERROR_THRESHOLD = java.util.logging.Level.SEVERE.intValue();

        @Override
        public void publish(java.util.logging.LogRecord record) {
            if(record.getMessage() == null || record.getMessage().isBlank()) {
                return;
            }
            
            int recordLevel = record.getLevel().intValue();
            org.slf4j.Logger slf4jLogger = org.slf4j.LoggerFactory.getLogger(record.getLoggerName());
            
            if(recordLevel <= TRACE_THRESHOLD) {
                slf4jLogger.trace(record.getMessage(), record.getThrown());
            } else if(recordLevel <= DEBUG_THRESHOLD) {
                slf4jLogger.debug(record.getMessage(), record.getThrown());
            } else if(recordLevel <= INFO_THRESHOLD) {
                slf4jLogger.info(record.getMessage(), record.getThrown());
            } else if(recordLevel <= WARN_THRESHOLD) {
                slf4jLogger.warn(record.getMessage(), record.getThrown());
            } else if(recordLevel <= ERROR_THRESHOLD) {
                slf4jLogger.error(record.getMessage(), record.getThrown());
            }
        }

        @Override
        public void flush() {
            
        }

        @Override
        public void close() throws SecurityException {
            
        }
        
    }
    
}
