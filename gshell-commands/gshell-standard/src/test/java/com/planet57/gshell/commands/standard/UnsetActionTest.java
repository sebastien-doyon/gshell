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
package com.planet57.gshell.commands.standard;

import com.planet57.gshell.testharness.CommandTestSupport;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for the {@link UnsetAction}.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class UnsetActionTest
    extends CommandTestSupport
{
  public UnsetActionTest() {
    super(UnsetAction.class);
  }

  @Test
  public void testUndefineVariable() throws Exception {
    variables.set("foo", "bar");
    assertTrue(variables.contains("foo"));
    Object result = executeWithArgs("foo");
    assertEqualsSuccess(result);
    assertFalse(variables.contains("foo"));
  }

  @Test
  public void testUndefineUndefinedVariable() throws Exception {
    assertFalse(variables.contains("foo"));
    Object result = executeWithArgs("foo");

    // Unsetting undefined should not return any errors
    assertEqualsSuccess(result);
  }
}