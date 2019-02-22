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
package org.spongepowered.common.registry.type.world;

import static com.google.common.base.Preconditions.checkNotNull;

import org.spongepowered.api.registry.util.RegisterCatalog;
import org.spongepowered.api.world.DimensionTypes;
import org.spongepowered.common.registry.AbstractCatalogRegistryModule;
import org.spongepowered.common.registry.SpongeAdditionalCatalogRegistryModule;

@RegisterCatalog(DimensionTypes.class)
public final class DimensionTypeRegistryModule extends AbstractCatalogRegistryModule<org.spongepowered.api.world.DimensionType>
    implements SpongeAdditionalCatalogRegistryModule<org.spongepowered.api.world.DimensionType> {

    public static DimensionTypeRegistryModule getInstance() {
        return Holder.instance;
    }

    @Override
    public boolean allowsApiRegistration() {
        return false;
    }

    @Override
    public void registerAdditionalCatalog(org.spongepowered.api.world.DimensionType dimensionType) {
        checkNotNull(dimensionType);
        this.map.put(dimensionType.getKey(), dimensionType);
    }

    private DimensionTypeRegistryModule() {
    }

    private static final class Holder {
        static final DimensionTypeRegistryModule instance = new DimensionTypeRegistryModule();
    }
}
