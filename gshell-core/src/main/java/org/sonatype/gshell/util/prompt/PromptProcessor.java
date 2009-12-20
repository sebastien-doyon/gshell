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

package org.sonatype.gshell.util.prompt;

import org.slf4j.Logger;
import org.sonatype.gossip.Log;
import org.sonatype.gshell.util.setter.SetterFactory;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Processes an object for prompt annotations.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public class PromptProcessor
{
    private static final Logger log = Log.getLogger(PromptProcessor.class);

    private final List<PromptDescriptor> descriptors = new ArrayList<PromptDescriptor>();

    public PromptProcessor() {
    }

    public List<PromptDescriptor> getDescriptors() {
        return descriptors;
    }

    public void addBean(final Object bean) {
        discoverDescriptors(bean);
    }

    //
    // Discovery
    //

    private void discoverDescriptors(final Object bean) {
        assert bean != null;

        // Recursively process all the methods/fields (@Inherited won't work here)
        for (Class<?> type = bean.getClass(); type != null; type = type.getSuperclass()) {
            for (Method method : type.getDeclaredMethods()) {
                discoverDescriptor(bean, method);
            }
            for (Field field : type.getDeclaredFields()) {
                discoverDescriptor(bean, field);
            }
        }
    }

    private void discoverDescriptor(final Object bean, final AnnotatedElement element) {
        assert bean != null;
        assert element != null;

        Prompt prompt = element.getAnnotation(Prompt.class);
        if (prompt != null) {
            log.trace("Discovered prompt configuration for: {}", element);

            //
            // TODO: Will need getter support, to determine if we need to prompt
            //

            PromptDescriptor desc = new PromptDescriptor(prompt, SetterFactory.create(element, bean));
            descriptors.add(desc);
        }
    }

    //
    // Processing
    //

    public void process() throws Exception {
        log.trace("Processing prompt descriptors");
        for (PromptDescriptor desc : descriptors) {
            log.trace("Descriptor: {}", desc);
//            desc.set();
        }
    }
}