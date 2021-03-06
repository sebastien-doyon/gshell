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
package com.planet57.gshell.util.pref;

import java.util.prefs.Preferences;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Some simple tests to validate basic functionality.
 */
public class NameTest
    extends PreferenceProcessorTestSupport
{
  private Simple bean;

  @Override
  protected Object createBean() {
    bean = new Simple();
    return bean;
  }

  @Test
  public void test1() throws Exception {
    Preferences prefs = Preferences.userNodeForPackage(Simple.class);
    prefs.put("bar", "foo");

    processor.process();

    assertEquals("foo", bean.name);
  }

  private static class Simple
  {
    @Preference(name = "bar")
    String name;
  }
}
