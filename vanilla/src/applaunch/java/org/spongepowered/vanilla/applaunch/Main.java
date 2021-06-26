/*
 * This file is part of Sponge, licensed under the MIT License (MIT).
 *
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.vanilla.applaunch;

import cpw.mods.modlauncher.Launcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.fusesource.jansi.AnsiConsole;
import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.vanilla.applaunch.plugin.VanillaPluginPlatform;
import org.spongepowered.vanilla.applaunch.util.ArgumentList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main {

    static {
        AnsiConsole.systemInstall();
    }

    private final Logger logger;
    private final VanillaPluginPlatform pluginPlatform;

    public Main() {
        this.logger = LogManager.getLogger("App Launch");
        this.pluginPlatform = AppLaunch.setPluginPlatform(new VanillaPluginPlatform(new PluginEnvironment()));
    }

    public static void main(final String[] args) throws Exception {
        AppCommandLine.configure(args);
        new Main().run();
    }

    public void run() throws IOException {
        final String implementationVersion = PluginEnvironment.class.getPackage().getImplementationVersion();

        this.pluginPlatform.setVersion(implementationVersion == null ? "dev" : implementationVersion);
        this.pluginPlatform.setBaseDirectory(AppCommandLine.gameDirectory);

        final Path modsDirectory = AppCommandLine.gameDirectory.resolve("mods");
        if (Files.notExists(modsDirectory)) {
            Files.createDirectories(modsDirectory);
        }
        final Path pluginsDirectory = AppCommandLine.gameDirectory.resolve("plugins");
        final List<Path> pluginDirectories = new ArrayList<>();
        pluginDirectories.add(modsDirectory);
        if (Files.exists(pluginsDirectory)) {
            pluginDirectories.add(pluginsDirectory);
        }
        this.pluginPlatform.setPluginDirectories(pluginDirectories);

        this.logger.info("Transitioning to ModLauncher, please wait...");
        final ArgumentList lst = ArgumentList.from(AppCommandLine.RAW_ARGS);
        Launcher.main(lst.getArguments());
    }
}
