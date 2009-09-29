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

package org.apache.gshell.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Bootstrap {@link Configuration} implementation.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 *
 * @since 2.0
 */
public class ConfigurationImpl
    implements Configuration
{
    private static final String DEFAULT_PROPERTIES = "default.properties";

    private static final String BOOTSTRAP_PROPERTIES = "bootstrap.properties";
    
    private static final int SUCCESS_EXIT_CODE = 0;

    private static final int FAILURE_EXIT_CODE = 100;

    private Properties props;

    private File homeDir;

    private File libDir;

    private File etcDir;

    private String programName;

    private String mainName;

    private String version;

    private final Pattern pattern;

    public ConfigurationImpl() {
        this.pattern = Pattern.compile("\\$\\{([^}]+)\\}");
    }

    private void setSystemProperty(final String name, final String value) {
        assert name != null;
        assert value != null;

        Log.debug(name, ": ", value);
        System.setProperty(name, value);
    }

    private Properties loadProperties() throws Exception {
        Properties props = new Properties();

        URL defaults = getClass().getResource(DEFAULT_PROPERTIES);
        if (defaults == null) {
            // Should never happen
            throw new Error("Missing resource: " + DEFAULT_PROPERTIES);
        }
        mergeProperties(props, defaults);

        File bootstrap = new File(detectBootDir(), BOOTSTRAP_PROPERTIES);
        if (!bootstrap.exists()) {
            throw new Error("Missing file: " + BOOTSTRAP_PROPERTIES);
        }
        mergeProperties(props, bootstrap.toURI().toURL());

        return props;
    }

    private void mergeProperties(final Properties props, final URL url) throws IOException {
        Log.debug("Merging properties from: ", url);

        InputStream input = url.openStream();
        try {
            props.load(input);
        }
        finally {
            input.close();
        }
    }

    private File detectBootDir() throws Exception {
        String path = getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "UTF-8");
        File file = new File(path);
        return file.getParentFile().getCanonicalFile();
    }

    /**
     * Detect the home directory, which is expected to be <tt>../../</tt> from the location of the jar containing this class.
     */
    private File detectHomeDir() throws Exception {
        return detectBootDir().getParentFile().getParentFile().getCanonicalFile();
    }

    private String evaluate(String input) {
        assert input != null;

        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            Object rep = props.get(matcher.group(1));
            if (rep != null) {
                input = input.replace(matcher.group(0), rep.toString());
                matcher.reset(input);
            }
        }

        return input;
    }

    /**
     * Get the value of a property, checking system properties, then configuration properties and evaluating the result.
     */
    private String getProperty(final String name) {
        assert name != null;
        assert props != null;

        String value = System.getProperty(name, props.getProperty(name));
        return evaluate(value);
    }

    private File getPropertyAsFile(final String name) {
        String path = getProperty(name);
        if (path != null) {
            return new File(path);
        }
        return null;
    }

    public void configure() throws Exception {
        Log.debug("Configuring");

        props = loadProperties();
        props.setProperty(SHELL_HOME_DETECTED, detectHomeDir().getAbsolutePath());

        if (Log.DEBUG) {
            Log.debug("Properties:");
            for (Map.Entry entry : props.entrySet()) {
                Log.debug("    ",  entry.getKey(), "=", entry.getValue());
            }
        }

        // Export some configuration
        setSystemProperty(SHELL_HOME, getHomeDir().getAbsolutePath());
        setSystemProperty(SHELL_PROGRAM, getProgramName());
        setSystemProperty(SHELL_VERSION, getVersion());
    }

    private void ensureConfigured() {
        if (props == null) {
            throw new IllegalStateException("Not configured");
        }
    }

    public File getHomeDir() {
        ensureConfigured();

        if (homeDir == null) {
            homeDir = getPropertyAsFile(SHELL_HOME);
        }

        return homeDir;
    }

    public File getLibDir() {
        ensureConfigured();

        if (libDir == null) {
            libDir = getPropertyAsFile(SHELL_LIB);
        }

        return libDir;
    }

    public File getEtcDir() {
        ensureConfigured();

        if (etcDir == null) {
            etcDir = getPropertyAsFile(SHELL_ETC);
        }

        return etcDir;
    }

    public String getProgramName() {
        ensureConfigured();

        if (programName == null) {
            programName = getProperty(SHELL_PROGRAM);
        }
        
        return programName;
    }

    public String getVersion() {
        ensureConfigured();

        if (version == null) {
            version = getProperty(SHELL_VERSION);
        }

        return version;
    }

    public List<URL> getClassPath() throws Exception {
        ensureConfigured();
        List<URL> classPath = new ArrayList<URL>();

        classPath.add(getEtcDir().toURI().toURL());

        Log.debug("Finding jars under: ", getLibDir());

        File[] files = getLibDir().listFiles(new FileFilter() {
            public boolean accept(final File file) {
                return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
            }
        });

        if (files == null) {
            throw new Error("No jars found under: " + getLibDir());
        }

        for (File file : files) {
            classPath.add(file.toURI().toURL());
        }

        return classPath;
    }

    public String getMainClass() {
        ensureConfigured();

        if (mainName == null) {
            mainName = getProperty(SHELL_MAIN);
        }

        return mainName;
    }

    public int getSuccessExitCode() {
        return SUCCESS_EXIT_CODE;
    }

    public int getFailureExitCode() {
        return FAILURE_EXIT_CODE;
    }
}