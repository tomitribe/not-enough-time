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
package org.tomitribe.sheldon.core;

import org.tomitribe.crest.Main;
import org.tomitribe.crest.api.StreamingOutput;
import org.tomitribe.crest.cmds.Cmd;
import org.tomitribe.crest.cmds.CommandFailedException;
import org.tomitribe.crest.cmds.processors.Commands;
import org.tomitribe.crest.contexts.SystemPropertiesDefaultsContext;
import org.tomitribe.crest.environments.Environment;
import org.tomitribe.crest.environments.SystemEnvironment;
import org.tomitribe.util.IO;
import org.tomitribe.util.Join;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class SingleCommand {

    private final Cmd cmd;

    private SingleCommand() throws Exception {
        final SystemPropertiesDefaultsContext defaultsContext = new SystemPropertiesDefaultsContext();

        final ClassLoader loader = this.getClass().getClassLoader();

        final URL resource = loader.getResource("META-INF/services/command");
        if (resource == null) {
            throw new IllegalStateException("Misconfigured environment. No Command found");
        }

        final String clazzName = IO.slurp(resource).trim();

        final Class<?> clazz = loader.loadClass(clazzName);

        final Map<String, Cmd> commands = Commands.get(clazz, defaultsContext);

        if (commands.values().size() != 1) {
            throw new IllegalStateException("Misconfigured environment. Only one command allowed.  Found: \n" +
                    Join.join("\n", commands.keySet()));
        }

        cmd = commands.values().iterator().next();
    }

    public static void main(final String... args) throws Exception {
        new SingleCommand().run(args);
    }

    private void run(String... args) {
        try {
            final Environment env = new SystemEnvironment();
            Environment.ENVIRONMENT_THREAD_LOCAL.set(env);

            final List<String> list = Main.processSystemProperties(args);

            args = list.toArray(new String[list.size()]);

            for (String arg : args) {
                if ("-h".equals(arg) || "--help".equals(arg)) {
                    cmd.help(env.getOutput());
                    System.exit(-1);
                }
            }

            final Object result = cmd.exec(args);

            if (result instanceof StreamingOutput) {
                ((StreamingOutput) result).write(env.getOutput());
            } else if (result instanceof String) {
                env.getOutput().println(result);
                final String string = (String) result;
                if (!string.endsWith("\n")) {
                    env.getOutput().println();
                }
            }
        } catch (final CommandFailedException e) {
            e.getCause().printStackTrace();
            System.exit(-1);
        } catch (final Exception alreadyHandled) {
            System.exit(-1);
        }
    }

}
