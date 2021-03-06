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
package com.planet57.gshell.internal.completer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import com.google.common.eventbus.Subscribe;
import com.planet57.gshell.command.CommandRegisteredEvent;
import com.planet57.gshell.command.CommandRegistry;
import com.planet57.gshell.command.CommandRemovedEvent;
import com.planet57.gshell.event.EventAware;
import com.planet57.gshell.util.jline.DynamicCompleter;
import com.planet57.gshell.util.jline.StringsCompleter2;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.planet57.gshell.util.jline.Candidates.candidate;

/**
 * {@link Completer} for command names.
 * Keeps up to date automatically by handling command-related events.
 *
 * @since 2.5
 */
@Named("command-name")
@Singleton
public class CommandNameCompleter
  extends DynamicCompleter
  implements EventAware
{
  private final StringsCompleter2 delegate = new StringsCompleter2();

  private final CommandRegistry commands;

  @Inject
  public CommandNameCompleter(final CommandRegistry commands) {
    this.commands = checkNotNull(commands);
  }

  @Override
  protected void init() {
    commands.getCommands().forEach(action -> {
      String name = action.getName();
      delegate.add(name, candidate(name, action.getDescription()));
    });
  }

  @Override
  protected Collection<Candidate> getCandidates() {
    return delegate.getCandidates();
  }

  @Subscribe
  void on(final CommandRegisteredEvent event) {
    String name = event.getName();
    delegate.add(name, candidate(name, event.getCommand().getDescription()));
  }

  @Subscribe
  void on(final CommandRemovedEvent event) {
    delegate.remove(event.getName());
  }
}
