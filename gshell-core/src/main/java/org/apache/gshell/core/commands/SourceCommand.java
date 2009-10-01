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

package org.apache.gshell.core.commands;

import jline.Completor;
import org.apache.gshell.Shell;
import org.apache.gshell.core.completer.FileNameCompleter;
import org.apache.gshell.cli.Argument;
import org.apache.gshell.command.Command;
import org.apache.gshell.command.CommandActionSupport;
import org.apache.gshell.command.CommandContext;
import org.apache.gshell.io.Closer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import com.google.inject.Inject;

/**
 * Read and execute commands from a file in the current shell environment.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @since 2.0
 */
@Command
public class SourceCommand
    extends CommandActionSupport
{
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Argument(required=true)
    private String path;

    @Inject
    public void installCompleters(final FileNameCompleter c1) {
        assert c1 != null;
        setCompleters(c1, null);
    }

    public Object execute(final CommandContext context) throws Exception {
        assert context != null;
        Shell shell = context.getShell();

        URL url;
        try {
            url = new URL(path);
        }
        catch (MalformedURLException e) {
            url = new File(path).toURI().toURL();
        }

        BufferedReader reader = openReader(url);
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                shell.execute(line);
            }
        }
        finally {
            Closer.close(reader);
        }

        return Result.SUCCESS;
    }

    private BufferedReader openReader(final Object source) throws IOException {
        BufferedReader reader;

        if (source instanceof File) {
            File file = (File)source;
            log.info("Using source file: {}", file);

            reader = new BufferedReader(new FileReader(file));
        }
        else if (source instanceof URL) {
            URL url = (URL)source;
            log.info("Using source URL: {}", url);

            reader = new BufferedReader(new InputStreamReader(url.openStream()));
        }
        else {
            String tmp = String.valueOf(source);

            // First try a URL
            try {
                URL url = new URL(tmp);
                log.info("Using source URL: {}", url);

                reader = new BufferedReader(new InputStreamReader(url.openStream()));
            }
            catch (MalformedURLException e) {
                // They try a file
                File file = new File(tmp);
                log.info("Using source file: {}", file);

                reader = new BufferedReader(new FileReader(tmp));
            }
        }

        return reader;
    }
}
