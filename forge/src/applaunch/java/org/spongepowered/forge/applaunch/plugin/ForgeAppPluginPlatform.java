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
package org.spongepowered.forge.applaunch.plugin;

import cpw.mods.modlauncher.Environment;
import cpw.mods.modlauncher.api.IEnvironment;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class ForgeAppPluginPlatform implements PluginPlatform {

    private final Environment environment;
    private final Logger logger;
    private final List<Path> pluginDirectories;

    public ForgeAppPluginPlatform(final Environment environment) {
        this.environment = environment;
        this.logger = LogManager.getLogger("App Launch");
        this.pluginDirectories = new ArrayList<>();

        this.pluginDirectories.add(FMLPaths.MODSDIR.get());

        final String s = this.getClass().getClassLoader().toString();
        // TODO We do not have all the arguments passed to Forge from CLI...How do we correctly handle knowing the plugins directory??
    }

    @Override
    public String version() {
        return this.environment.getProperty(IEnvironment.Keys.VERSION.get()).orElse("dev");
    }

    @Override
    public void setVersion(final String version) {
        // NOOP
    }

    @Override
    public Logger logger() {
        return this.logger;
    }

    @Override
    public Path baseDirectory() {
        return this.environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));
    }

    @Override
    public void setBaseDirectory(final Path baseDirectory) {
        // NOOP
    }

    @Override
    public List<Path> pluginDirectories() {
        return this.pluginDirectories;
    }

    @Override
    public void setPluginDirectories(final List<Path> pluginDirectories) {
        // NOOP
    }
}
