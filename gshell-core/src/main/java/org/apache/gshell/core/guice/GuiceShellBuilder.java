/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.gshell.core.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import jline.Completor;
import org.apache.gshell.Branding;
import org.apache.gshell.Shell;
import org.apache.gshell.Variables;
import org.apache.gshell.console.Console;
import org.apache.gshell.core.ShellImpl;
import org.apache.gshell.execute.CommandExecutor;
import org.apache.gshell.io.IO;
import org.apache.gshell.registry.CommandRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link org.apache.maven.shell.Shell} instances.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @since 2.0
 */
public class GuiceShellBuilder
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Injector injector;

    private Branding branding;

    private IO io;

    private Variables variables;

    private boolean registerCommands = true;

    private Console.Prompter prompter;

    private Console.ErrorHandler errorHandler;

    private final List<Completor> completers = new ArrayList<Completor>();

    protected Injector createInjector() {
        return Guice.createInjector(Stage.PRODUCTION, new CoreModule());
    }

    public Injector getInjector() {
        if (injector == null) {
            injector = createInjector();
        }
        return injector;
    }

    public GuiceShellBuilder setInjector(final Injector injector) {
        assert injector != null;
        this.injector = injector;
        return this;
    }

    public GuiceShellBuilder setBranding(final Branding branding) {
        this.branding = branding;
        return this;
    }

    public GuiceShellBuilder setIo(final IO io) {
        this.io = io;
        return this;
    }

    public GuiceShellBuilder setVariables(final Variables variables) {
        this.variables = variables;
        return this;
    }

    public GuiceShellBuilder setRegisterCommands(final boolean flag) {
        this.registerCommands = flag;
        return this;
    }

    public GuiceShellBuilder setPrompter(final Console.Prompter prompter) {
        this.prompter = prompter;
        return this;
    }

    public GuiceShellBuilder setErrorHandler(final Console.ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
        return this;
    }

    public GuiceShellBuilder addCompleter(final Completor completer) {
        assert completer != null;
        completers.add(completer);
        return this;
    }

    public Shell create() throws Exception {
        if (branding == null) {
            throw new IllegalStateException("Missing branding");
        }

        Injector injector = getInjector();

        // Create the shell instance
        CommandExecutor executor = injector.getInstance(CommandExecutor.class);
        ShellImpl shell = new ShellImpl(branding, executor, io, variables);
        shell.setPrompter(prompter);
        shell.setErrorHandler(errorHandler);
        shell.setCompleters(completers);

        // Maybe register default commands
        if (registerCommands) {
            injector.getInstance(CommandRegistrar.class).registerCommands();
        }

        log.debug("Created shell: {}", shell);

        return shell;
    }
}