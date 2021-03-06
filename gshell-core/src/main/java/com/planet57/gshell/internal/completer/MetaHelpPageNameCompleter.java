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
import com.planet57.gshell.event.EventAware;
import com.planet57.gshell.help.HelpPage;
import com.planet57.gshell.help.HelpPageManager;
import com.planet57.gshell.help.MetaHelpPageAddedEvent;
import com.planet57.gshell.util.jline.DynamicCompleter;
import com.planet57.gshell.util.jline.StringsCompleter2;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.planet57.gshell.util.jline.Candidates.candidate;

/**
 * {@link Completer} for meta help page names.
 * Keeps up to date automatically by handling meta-page-related events.
 *
 * @since 2.5
 */
@Named("meta-help-page-name")
@Singleton
public class MetaHelpPageNameCompleter
  extends DynamicCompleter
  implements EventAware
{
  private final StringsCompleter2 delegate = new StringsCompleter2();

  private final HelpPageManager helpPages;

  @Inject
  public MetaHelpPageNameCompleter(final HelpPageManager helpPages) {
    this.helpPages = checkNotNull(helpPages);
  }

  @Override
  protected void init() {
    helpPages.getMetaPages().forEach(this::add);
  }

  @Override
  protected Collection<Candidate> getCandidates() {
    return delegate.getCandidates();
  }

  @Subscribe
  void on(final MetaHelpPageAddedEvent event) {
    add(event.getPage());
  }

  private void add(final HelpPage page) {
    String name = page.getName();
    delegate.add(name, candidate(name, page.getDescription()));
  }
}
