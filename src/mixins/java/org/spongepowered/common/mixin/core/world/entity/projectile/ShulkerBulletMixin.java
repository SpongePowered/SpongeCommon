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
package org.spongepowered.common.mixin.core.world.entity.projectile;

import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.level.LevelBridge;
import org.spongepowered.common.event.SpongeCommonEventFactory;
import org.spongepowered.common.mixin.core.world.entity.EntityMixin;

@Mixin(ShulkerBullet.class)
public abstract class ShulkerBulletMixin extends EntityMixin {

    @Inject(method = "onHit", at = @At("HEAD"), cancellable = true)
    private void onBulletHitBlock(final HitResult result, final CallbackInfo ci) {
        if (!((LevelBridge) this.level).bridge$isFake() && result.getType() != HitResult.Type.MISS
                && SpongeCommonEventFactory.handleCollideImpactEvent(
                (net.minecraft.world.entity.Entity) (Object) this, ((Projectile) this).get(Keys.SHOOTER).orElse(null), result)) {
            this.shadow$remove(); // TODO do we want to kill the bullet here?
            ci.cancel();
        }
    }
}
