/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package com.redhat.devtools.intellij.quarkus.lsp;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.redhat.devtools.intellij.quarkus.lsp4ij.server.ProcessStreamConnectionProvider;

import java.io.File;
import java.util.Arrays;

public class QuarkusServer extends ProcessStreamConnectionProvider {
    public QuarkusServer() {
        IdeaPluginDescriptor descriptor = PluginManager.getPlugin(PluginId.getId("com.redhat.devtools.intellij.quarkus"));
        File lsp4mpServerPath = new File(descriptor.getPath(), "lib/server/org.eclipse.lsp4mp.ls-0.1.0-uber.jar");
        File quarkusServerPath = new File(descriptor.getPath(), "lib/server/com.redhat.quarkus.ls-0.9.0.jar");
        String javaHome = System.getProperty("java.home");
        setCommands(Arrays.asList(javaHome + File.separator + "bin" + File.separator + "java", "-jar",
                lsp4mpServerPath.getAbsolutePath(), "-cp", quarkusServerPath.getAbsolutePath()));
    }
}
