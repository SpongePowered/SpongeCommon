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
package org.spongepowered.common.mixin.api.entity;

import com.mojang.authlib.GameProfile;
import org.spongepowered.api.data.manipulator.DataManipulator;
import org.spongepowered.api.data.manipulator.mutable.entity.SkinData;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.common.entity.living.human.EntityHuman;
import org.spongepowered.common.mixin.api.mcp.entity.EntityCreatureMixin_API;

import java.util.Collection;

@Mixin(value = EntityHuman.class, remap = false)
public abstract class EntityHumanMixin_API extends EntityCreatureMixin_API implements Human {

    @Shadow private GameProfile fakeProfile;

    @Override
    public String getName() {
        return this.fakeProfile.getName();
    }

    @Override
    protected void spongeApi$supplyVanillaManipulators(Collection<? super DataManipulator<?, ?>> manipulators) {
        super.spongeApi$supplyVanillaManipulators(manipulators);

        this.get(SkinData.class).ifPresent(manipulators::add);
    }

}
