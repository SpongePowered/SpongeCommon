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
package org.spongepowered.common.mixin.core.world.dimension;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.api.CatalogKey;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.common.SpongeImplHooks;
import org.spongepowered.common.interfaces.world.IMixinDimensionType;
import org.spongepowered.common.registry.type.world.DimensionTypeRegistryModule;
import org.spongepowered.common.registry.type.world.dimension.GlobalDimensionType;

@Mixin(DimensionType.class)
public abstract class MixinDimensionType implements IMixinDimensionType {

    private GlobalDimensionType globalDimensionType;

    @Inject(method = "register", at = @At("RETURN"))
    private static void onRegister(String path, DimensionType dimensionType, CallbackInfoReturnable<DimensionType> cir) {
        final PluginContainer container = SpongeImplHooks.getActiveModContainer();
        final CatalogKey key = CatalogKey.of(container.getId(), path);

        final DimensionTypeRegistryModule module = DimensionTypeRegistryModule.getInstance();

        ((IMixinDimensionType) dimensionType).setGlobalDimensionType((GlobalDimensionType) module.get(key).orElseGet(() -> {
            final GlobalDimensionType globalDimensionType = new GlobalDimensionType(key, dimensionType.create().getClass());
            module.registerAdditionalCatalog(globalDimensionType);
            return globalDimensionType;
        }));
    }

    @Override
    public GlobalDimensionType getGlobalDimensionType() {
        return this.globalDimensionType;
    }

    @Override
    public void setGlobalDimensionType(GlobalDimensionType dimensionType) {
        checkNotNull(dimensionType);
        this.globalDimensionType = dimensionType;
    }

    @Override
    public DimensionType asClientDimensionType() {
        return (DimensionType) (Object) this;
    }
}