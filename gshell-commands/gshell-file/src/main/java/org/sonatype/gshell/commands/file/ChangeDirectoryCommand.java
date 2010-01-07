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

package org.sonatype.gshell.commands.file;

import com.google.inject.Inject;
import org.sonatype.gshell.command.Command;
import org.sonatype.gshell.command.CommandActionSupport;
import org.sonatype.gshell.command.CommandContext;
import org.sonatype.gshell.command.IO;
import org.sonatype.gshell.console.completer.FileNameCompleter;
import org.sonatype.gshell.file.FileSystemAccess;
import org.sonatype.gshell.util.FileAssert;
import org.sonatype.gshell.util.cli2.Argument;
import org.sonatype.gshell.vars.Variables;

import java.io.File;

import static org.sonatype.gshell.vars.VariableNames.SHELL_USER_DIR;

/**
 * Changes the current directory.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
@Command(name="cd")
public class ChangeDirectoryCommand
    extends CommandActionSupport
{
    private final FileSystemAccess fileSystem;
    
    @Argument
    private String path;

    @Inject
    public ChangeDirectoryCommand(final FileSystemAccess fileSystem) {
        assert fileSystem != null;
        this.fileSystem = fileSystem;
    }

    @Inject
    public ChangeDirectoryCommand installCompleters(final FileNameCompleter c1) {
        assert c1 != null;
        setCompleters(c1, null);
        return this;
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        IO io = context.getIo();
        Variables vars = context.getVariables();

        File file;
        if (path == null) {
            file = fileSystem.getUserHomeDir();
        }
        else {
            file = fileSystem.resolveFile(path);
        }

        new FileAssert(file).exists().isDirectory();

        vars.set(SHELL_USER_DIR, file.getPath());
        io.verbose(file.getPath());

        return Result.SUCCESS;
    }
}