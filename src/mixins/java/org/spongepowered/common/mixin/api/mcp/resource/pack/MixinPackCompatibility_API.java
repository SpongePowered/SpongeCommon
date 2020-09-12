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
package org.spongepowered.common.mixin.api.mcp.resource.pack;

import net.minecraft.resources.PackCompatibility;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.resource.pack.PackVersion;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;

@Mixin(PackCompatibility.class)
@Implements(@Interface(iface = PackVersion.class, prefix = "pack$"))
public abstract class MixinPackCompatibility_API implements PackVersion {

    private ResourceKey api$key;
    public abstract boolean shadow$isCompatible();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void api$setKey(String enumName, int ordinal, String directoryNameIn, CallbackInfo ci) {
        this.api$key = ResourceKey.of(SpongeCommon.getActivePlugin(), enumName.toLowerCase());
    }

    @Override
    public ResourceKey getKey() {
        return this.api$key;
    }

    @Intrinsic
    public boolean pack$isCompatible() {
        return shadow$isCompatible();
    }
}
