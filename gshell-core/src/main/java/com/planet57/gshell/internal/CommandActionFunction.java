/*
 * Copyright (c) 2009-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.planet57.gshell.internal;

import com.google.common.base.Stopwatch;
import com.planet57.gshell.branding.Branding;
import com.planet57.gshell.command.CommandAction;
import com.planet57.gshell.command.CommandAction.Prototype;
import com.planet57.gshell.command.CommandContext;
import com.planet57.gshell.command.CommandHelper;
import com.planet57.gshell.util.io.IO;
import com.planet57.gshell.shell.Shell;
import com.planet57.gshell.util.cli2.CliProcessor;
import com.planet57.gshell.util.cli2.HelpPrinter;
import com.planet57.gshell.util.cli2.OpaqueArguments;
import com.planet57.gshell.util.io.StreamSet;
import com.planet57.gshell.util.pref.PreferenceProcessor;
import com.planet57.gshell.util.io.StyledIO;
import com.planet57.gshell.variables.Variables;
import com.planet57.gshell.variables.VariablesSupport;
import org.apache.felix.gogo.runtime.CommandSessionImpl;
import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Function;
import org.jline.terminal.Terminal;
import org.sonatype.goodies.common.ComponentSupport;
import org.sonatype.goodies.common.Throwables2;

import javax.annotation.Nonnull;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * GOGO {@link Function} adapting a {@link CommandAction}.
 *
 * @since 3.0
 */
public class CommandActionFunction
  extends ComponentSupport
  implements Function
{
  public static final String SHELL_VAR = ".shell";

  public static final String TERMINAL_VAR = ".terminal";

  private final CommandAction action;

  public CommandActionFunction(final CommandAction action) {
    checkNotNull(action);

    // create copies for actions that implement prototype pattern
    if (action instanceof Prototype) {
      this.action = ((Prototype) action).create();
    }
    else {
      this.action = action;
    }

    log.debug("Action: {}", action);
  }

  @Override
  public Object execute(final CommandSession session, final List<Object> arguments) throws Exception {
    log.debug("Executing ({}): {}", action.getName(), arguments);

    Stopwatch watch = Stopwatch.createStarted();

    try {
      // adapt api to CommandSessionImpl for simplicity
      Object result = doExecute((CommandSessionImpl) session, arguments);
      log.debug("Result: {}; {}", result, watch);
      return result;
    }
    catch (Throwable failure) {
      log.debug("Failure: {}; {}", Throwables2.explain(failure), watch);
      throw failure;
    }
  }

  private Object doExecute(final CommandSessionImpl session, final List<Object> arguments) throws Exception {
    final Shell shell = (Shell) session.get(SHELL_VAR);
    checkState(shell != null);

    final Terminal terminal = (Terminal) session.get(TERMINAL_VAR);
    checkState(terminal != null);

    final ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(action.getClass().getClassLoader());

    // FIXME: Need to provide a means to create non-styled for testing?
    // re-create IO with current streams; which are adjusted by ThreadIO
    final IO io = StyledIO.create("shell", StreamSet.system(), terminal);

    Object result = null;
    try {
      boolean execute = true;

      // Process command preferences
      Branding branding = shell.getBranding();
      PreferenceProcessor pp = new PreferenceProcessor();
      pp.setBasePath(branding.getPreferencesBasePath());
      pp.addBean(action);
      pp.process();

      // Process command arguments unless marked as opaque
      if (!(action instanceof OpaqueArguments)) {
        CommandHelper help = new CommandHelper();
        CliProcessor clp = help.createCliProcessor(action);
        clp.process(arguments);

        // Render command-line usage
        if (help.displayHelp) {
          io.format("%s%n%n", action.getDescription());
          HelpPrinter printer = new HelpPrinter(clp, terminal.getWidth());
          printer.printUsage(io.out, action.getSimpleName());

          // Skip execution
          execute = false;
        }
      }

      // HACK: re-create variables with session as basis
      final Variables variables = new VariablesSupport(session.getVariables());
      VariablesProvider.set(variables);

      if (execute) {
        result = action.execute(new CommandContext()
        {
          @Nonnull
          @Override
          public Shell getShell() {
            return shell;
          }

          @Nonnull
          @Override
          public CommandSessionImpl getSession() {
            return session;
          }

          @Nonnull
          @Override
          public List<?> getArguments() {
            return arguments;
          }

          @Nonnull
          @Override
          public IO getIo() {
            return io;
          }

          @Nonnull
          @Override
          public Variables getVariables() {
            return variables;
          }
        });
      }
    }
    finally {
      Thread.currentThread().setContextClassLoader(cl);
      io.flush();
    }

    return result;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "{" +
      "action=" + action.getName() +
      '}';
  }
}
