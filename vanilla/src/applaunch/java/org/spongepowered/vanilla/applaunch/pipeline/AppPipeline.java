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
package org.spongepowered.vanilla.applaunch.pipeline;

import cpw.mods.modlauncher.Launcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.config.core.SpongeConfigs;
import org.spongepowered.plugin.PluginEnvironment;
import org.spongepowered.plugin.PluginKeys;
import org.spongepowered.vanilla.applaunch.VanillaCommandLine;
import org.spongepowered.vanilla.applaunch.util.ArgumentList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class AppPipeline {

    protected final Logger logger;
    private final PluginEnvironment pluginEnvironment;

    public AppPipeline() {
        this.logger = LogManager.getLogger("App Launch");
        this.pluginEnvironment = new PluginEnvironment();
    }

    public void prepare() throws Exception {
        this.logger.info("Inspecting libraries, please wait...");
        final String implementationVersion = PluginEnvironment.class.getPackage().getImplementationVersion();

        this.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.VERSION, () -> implementationVersion == null ? "dev" : implementationVersion);
        this.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.BASE_DIRECTORY, () -> VanillaCommandLine.gameDirectory);

        SpongeConfigs.initialize(this.pluginEnvironment);
        final Path modsDirectory = VanillaCommandLine.gameDirectory.resolve("mods");
        if (Files.notExists(modsDirectory)) {
            Files.createDirectories(modsDirectory);
        }
        this.pluginEnvironment.getBlackboard().getOrCreate(PluginKeys.PLUGIN_DIRECTORIES, () -> Arrays.asList(modsDirectory, VanillaCommandLine
                .gameDirectory.resolve("plugins")));
    }

    public void start(final String[] args) {
        this.logger.info("Transitioning to ModLauncher, please wait...");
        final ArgumentList lst = ArgumentList.from(args);
        Launcher.main(lst.getArguments());
    }

    public PluginEnvironment getPluginEnvironment() {
        return this.pluginEnvironment;
    }
}