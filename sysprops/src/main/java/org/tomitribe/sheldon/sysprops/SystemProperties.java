/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.sheldon.sysprops;

import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.PrintString;

import java.util.Properties;
import java.util.regex.Pattern;

public class SystemProperties {

    @Command
    public String properties(@Option({"pattern", "p"}) @Default(".*") Pattern pattern) {
        final PrintString out = new PrintString();

        final Properties properties = System.getProperties();

        for (final String name : properties.stringPropertyNames()) {
            if (pattern.matcher(name).matches()) {
                out.printf("\u001B[2m\u001B[36m%s\u001B[0m = %s%n", name, properties.getProperty(name));
            }
        }

        return out.toString();
    }
}
