/*
 * Copyright (C) 2009 the original author(s).
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

package org.sonatype.gshell.command;

/**
 * Resolves commands.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.5
 */
public interface CommandResolver
{
    String ROOT = "/";

    String PARENT = "..";

    String CURRENT = ".";

    String SEPARATOR = "/";

    CommandAction resolveCommand(String name) throws CommandException;

    // TODO: Add resolution of a group of commands
    // Collection<Command> resolveCommands(String name) throws CommandException;
}