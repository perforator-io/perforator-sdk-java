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

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import org.openqa.selenium.remote.Command;
import org.openqa.selenium.remote.DriverCommand;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.Response;

final class RemoteWebDriverCommandExecutor extends HttpCommandExecutor {
    
    private static final Field COMMAND_CODEC_FIELD = getCommandCodecField();

    private final TimeProvider timeProvider;
    private final EventsRouter eventsRouter;
    private final SuiteInstanceContextImpl suiteInstanceContext;

    public RemoteWebDriverCommandExecutor(TimeProvider timeProvider, EventsRouter eventsRouter, SuiteInstanceContextImpl suiteInstanceContext) {
        super(
                Collections.EMPTY_MAP, 
                suiteInstanceContext.getLoadGeneratorContext().getBrowserCloudContext().getSeleniumHubURL(),
                new RemoteWebDriverHttpClientFactory(suiteInstanceContext)
        );
        this.timeProvider = timeProvider;
        this.eventsRouter = eventsRouter;
        this.suiteInstanceContext = suiteInstanceContext;
    }

    @Override
    public Response execute(Command command) throws IOException {
        IOException commandExecutionError = null;

        try {
            return super.execute(command);
        } catch (IOException e) {
            commandExecutionError = e;
        } finally {
            if (command.getName().equals(DriverCommand.QUIT)) {
                String sessionID = command.getSessionId().toString();

                RemoteWebDriverContextImpl remoteWebDriverContext = suiteInstanceContext.getDrivers().remove(
                        sessionID
                );

                if (remoteWebDriverContext != null) {
                    eventsRouter.onRemoteWebDriverFinished(
                            timeProvider.getCurrentTime(),
                            remoteWebDriverContext,
                            commandExecutionError
                    );
                }
            } else if(command.getName().equals(DriverCommand.NEW_SESSION) && commandExecutionError == null) {
                try {
                    COMMAND_CODEC_FIELD.set(
                            this, 
                            new RemoteWebDriverCommandCodec()
                    );
                } catch(ReflectiveOperationException e) {
                    throw new RuntimeException(
                            "Can't propagate " + COMMAND_CODEC_FIELD.getName(), 
                            e
                    );
                }
            }
        }
        
        throw commandExecutionError;
    }
    
    private static Field getCommandCodecField() {
        try {
            Field result = HttpCommandExecutor.class.getDeclaredField("commandCodec");
            result.setAccessible(true);
            return result;
        } catch(ReflectiveOperationException e) {
            throw new RuntimeException("Can't expose commandCodec", e);
        }
    }

}
