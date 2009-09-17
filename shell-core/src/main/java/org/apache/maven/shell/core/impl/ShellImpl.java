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

package org.apache.maven.shell.core.impl;

import jline.Completor;
import org.apache.maven.shell.History;
import org.apache.maven.shell.Shell;
import org.apache.maven.shell.VariableNames;
import org.apache.maven.shell.Variables;
import org.apache.maven.shell.command.CommandExecutor;
import org.apache.maven.shell.console.Console;
import org.apache.maven.shell.console.completer.AggregateCompleter;
import org.apache.maven.shell.core.impl.console.JLineConsole;
import org.apache.maven.shell.io.IO;
import org.apache.maven.shell.notification.ExitNotification;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The default {@link Shell} component.
 *
 * @version $Rev$ $Date$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
@Component(role=Shell.class, instantiationStrategy="per-lookup")
public class ShellImpl
    implements Shell, VariableNames
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String USER_HOME = "user.home";

    private static final String USER_DIR = "user.dir";

    private static final String DOTM2 = ".m2";

    @Requirement
    private CommandExecutor executor;

    @Requirement(role=Completor.class, hints={"alias-name", "commands"})
    private List<Completor> completers;

    private IO io = new IO();

    private Variables variables = new Variables();

    private Console.Prompter prompter;

    private Console.ErrorHandler errorHandler;

    private final JLineHistory history = new JLineHistory();

    private final ScriptLoader scriptLoader = new ScriptLoader(this);

    private boolean opened;

    public IO getIo() {
        return io;
    }

    public void setIo(final IO io) {
        assert io != null;
        this.io = io;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(final Variables variables) {
        assert variables != null;
        this.variables = variables;
    }

    public History getHistory() {
        return history;
    }

    public Console.Prompter getPrompter() {
        return prompter;
    }

    public void setPrompter(final Console.Prompter prompter) {
        this.prompter = prompter;
    }

    public Console.ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(final Console.ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public synchronized boolean isOpened() {
        return opened;
    }

    public synchronized void close() {
        opened = false;
    }

    private synchronized void ensureOpened() {
        if (!opened) {
            try {
                open();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private synchronized void open() throws Exception {
        log.debug("Opening");

        // setUp default variables
        if (!variables.contains(MVNSH_HOME)) {
            variables.set(MVNSH_HOME, System.getProperty(MVNSH_HOME), false);
        }
        if (!variables.contains(MVNSH_VERSION)) {
            variables.set(MVNSH_VERSION, System.getProperty(MVNSH_VERSION), false);
        }
        if (!variables.contains(MVNSH_USER_HOME)) {
            variables.set(MVNSH_USER_HOME, System.getProperty(USER_HOME), false);
        }
        if (!variables.contains(MVNSH_USER_DIR)) {
            variables.set(MVNSH_USER_DIR, System.getProperty(USER_DIR));
        }
        if (!variables.contains(MVNSH_PROMPT)) {
            variables.set(MVNSH_PROMPT, String.format("@|bold %s|:%%{%s}> ", System.getProperty(MVNSH_PROGRAM), MVNSH_USER_DIR));
        }

        // Configure history storage
        if (!variables.contains(MVNSH_HISTORY)) {
            File dir = new File(variables.get(MVNSH_USER_HOME, String.class), DOTM2);
            File file = new File(dir, MVNSH_HISTORY);
            history.setStoreFile(file);
            variables.set(MVNSH_HISTORY, file.getCanonicalFile(), false);
        }
        else {
            File file = new File(variables.get(MVNSH_HISTORY, String.class));
            history.setStoreFile(file);
        }
        
        // Load profile scripts
        scriptLoader.loadProfileScripts();

        opened = true;

        log.debug("Opened");
    }

    public boolean isInteractive() {
        return true;
    }

    // FIXME: History should still be appended if not running inside of a JLineConsole
    
    public Object execute(final String line) throws Exception {
        ensureOpened();
        return executor.execute(this, line);
    }

    public Object execute(final String command, final Object[] args) throws Exception {
        ensureOpened();
        return executor.execute(this, command, args);
    }

    public Object execute(final Object... args) throws Exception {
        ensureOpened();
        return executor.execute(this, args);
    }

    private void setLastResult(final Object result) {
        // result may be null
        getVariables().set(LAST_RESULT, result);
    }

    public void run(final Object... args) throws Exception {
        assert args != null;

        ensureOpened();

        log.debug("Starting interactive console; args: {}", args);

        scriptLoader.loadInteractiveScripts();

        // setUp 2 final refs to allow our executor to pass stuff back to us
        final AtomicReference<ExitNotification> exitNotifHolder = new AtomicReference<ExitNotification>();
        final AtomicReference<Object> lastResultHolder = new AtomicReference<Object>();

        // Whip up a tiny console executor that will execute shell command-lines
        Console.Executor executor = new Console.Executor() {
            public Result execute(final String line) throws Exception {
                assert line != null;

                try {
                    Object result = ShellImpl.this.execute(line);
                    lastResultHolder.set(result);
                    setLastResult(result);
                }
                catch (ExitNotification n) {
                    exitNotifHolder.set(n);

                    return Result.STOP;
                }

                return Result.CONTINUE;
            }
        };

        IO io = getIo();
        
        // Setup the console
        JLineConsole console = new JLineConsole(executor, io);
        console.setHistory(history.getDelegate());

        if (prompter != null) {
            console.setPrompter(prompter);
        }
        if (errorHandler != null) {
            console.setErrorHandler(errorHandler);
        }
        if (completers != null) {
            // Have to use aggregate here to get the completion list to update properly
            console.addCompleter(new AggregateCompleter(completers));
        }

        // Unless the user wants us to shut up, then display a nice welcome banner
        if (!io.isQuiet()) {
            io.out.println("@|bold,red Apache Maven| @|bold Shell|");
            io.out.println(StringUtils.repeat("-", io.getTerminal().getTerminalWidth() - 1));
            io.out.flush();
        }

        // Check if there are args, and run them and then enter interactive
        if (args.length != 0) {
            execute(args);
        }

        // And then spin up the console and go for a jog
        console.run();

        // If any exit notification occurred while running, then puke it up
        ExitNotification n = exitNotifHolder.get();
        if (n != null) {
            throw n;
        }
    }
}