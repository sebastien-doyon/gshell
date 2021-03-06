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
package com.planet57.gshell.commands.artifact;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import org.eclipse.aether.repository.LocalRepository;

import com.planet57.gshell.command.Command;
import com.planet57.gshell.command.CommandActionSupport;
import com.planet57.gshell.command.CommandContext;
import com.planet57.gshell.repository.RepositoryAccess;
import com.planet57.gshell.util.cli2.Argument;

import java.io.File;

/**
 * Set the local repository.
 *
 * @since 3.0
 */
@Command(name="artifact/local-repository", description = "Set the local-repository")
public class LocalRepositoryAction
  extends CommandActionSupport
{
  @Inject
  private RepositoryAccess repositoryAccess;

  @Nullable
  @Argument(description = "Repository location", token = "DIR")
  private File dir;

  @Override
  public Object execute(@Nonnull final CommandContext context) throws Exception {
    if (dir == null) {
      context.getIo().println(repositoryAccess.getLocalRepository());
    }
    else {
      LocalRepository repository = new LocalRepository(dir);
      repositoryAccess.setLocalRepository(repository);
    }
    return null;
  }
}
