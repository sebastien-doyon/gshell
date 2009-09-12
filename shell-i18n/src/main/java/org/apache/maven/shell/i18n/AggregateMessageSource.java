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

package org.apache.maven.shell.i18n;

/**
 * A message source which aggregates messages sources in order.
 *
 * @version $Rev$ $Date$
 */
public class AggregateMessageSource
    implements MessageSource
{
    private final MessageSource[] sources;

    public AggregateMessageSource(final MessageSource[] sources) {
        assert sources != null;
        assert sources.length > 1;

        this.sources = sources;
    }

    @Override
    public String getMessage(final String code) {
        String result = null;

        for (MessageSource source : sources) {
            try {
                result = source.getMessage(code);
                if (result != null) break;
            }
            catch (ResourceNotFoundException e) {
                // ignore
            }
        }

        if (result == null) {
            throw new ResourceNotFoundException(code);
        }

        return result;
    }

    @Override
    public String format(final String code, final Object... args) {
        String result = null;

        for (MessageSource source : sources) {
            try {
                result = source.format(code, args);
                if (result != null) break;
            }
            catch (ResourceNotFoundException e) {
                // ignore
            }
        }

        if (result == null) {
            throw new ResourceNotFoundException(code);
        }

        return result;
    }
}