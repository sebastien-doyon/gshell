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

package org.sonatype.gshell.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.gshell.command.descriptor.CommandDescriptor;
import org.sonatype.gshell.command.descriptor.CommandSetDescriptor;
import org.sonatype.gshell.command.descriptor.CommandsDescriptor;
import org.sonatype.gshell.command.descriptor.io.xpp3.CommandsXpp3Reader;
import org.sonatype.gshell.io.Closer;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

/**
 * Support for {@link CommandRegistrar} implementations
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public abstract class CommandRegistrarSupport
    implements CommandRegistrar
{
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private String descriptorLocation = DEFAULT_DESCRIPTOR_LOCATION;

    private List<CommandSetDescriptor> descriptors = new LinkedList<CommandSetDescriptor>();

    public String getDescriptorLocation() {
        return descriptorLocation;
    }

    public void setDescriptorLocation(final String path) {
        assert path != null;
        this.descriptorLocation = path;
    }

    public List<CommandSetDescriptor> getDescriptors() {
        return descriptors;
    }

    public void registerCommands() throws Exception {
        List<CommandsDescriptor> descriptors = discoverDescriptors();
        List<CommandSetDescriptor> commandSets = new ArrayList<CommandSetDescriptor>();
        for (CommandsDescriptor d : descriptors) {
            commandSets.addAll(d.getCommandSets());
        }

        if (!commandSets.isEmpty()) {
            Collections.sort(commandSets);

            for (CommandSetDescriptor config : commandSets) {
                if (!config.isEnabled()) {
                    log.debug("Skipping disabled commands: {}", config);
                    continue;
                }

                log.debug("Registering commands for: {}", config);

                // FIXME: Should expand on the API that exposes descriptors, including those that are not enabled
                this.descriptors.add(config);

                for (CommandDescriptor command : config.getCommands()) {
                    if (command.isEnabled()) {
                        String type = command.getAction();
                        String name = command.getName();

                        try {
                            if (name == null) {
                                registerCommand(type);
                            }
                            else {
                                registerCommand(name, type);
                            }
                        }
                        catch (Exception e) {
                            log.error("Failed to register command: " + type, e);
                        }
                    }
                    else {
                        log.debug("Skipping disabled command: {}", command);
                    }
                }
            }
        }
    }

    protected List<CommandsDescriptor> discoverDescriptors() throws Exception {
        String location = getDescriptorLocation();
        log.debug("Discovering commands descriptors; location={}", location);

        List<CommandsDescriptor> list = new LinkedList<CommandsDescriptor>();

        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        Enumeration<URL> resources = cl.getResources(location);
        if (resources != null && resources.hasMoreElements()) {
            log.debug("Discovered:");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                log.debug("    {}", url);
                CommandsXpp3Reader reader = new CommandsXpp3Reader();
                InputStream input = url.openStream();
                try {
                    CommandsDescriptor config = reader.read(input);
                    list.add(config);
                }
                finally {
                    Closer.close(input);
                }
            }
        }

        return list;
    }
}