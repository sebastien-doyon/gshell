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

package org.sonatype.gshell.pref;

import org.junit.After;
import org.junit.Before;

import static org.junit.Assert.assertEquals;

/**
 * Support for {@link org.sonatype.gshell.pref.Processor} tests.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public abstract class ProcessorTestSupport
{
    protected Processor processor;

    @Before
    public void setUp() {
        processor = new Processor(createBean());
    }

    @After
    public void tearDown() {
        processor = null;
    }

    protected abstract Object createBean();
}