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
package org.spongepowered.common.mixin.core.world.storage;

import net.minecraft.world.storage.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.util.Constants;

import java.util.UUID;

@Mixin(net.minecraft.world.storage.MapDecoration.class)
public class MapDecorationMixin implements MapDecorationBridge {

	// If should save to disk
	private boolean impl$isPersistent;
	private String impl$key = Constants.Map.DECORATION_KEY_PREFIX + UUID.randomUUID().toString();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void impl$setPersistenceOnInit(final MapDecoration.Type typeIn, final byte xIn, final byte yIn, final byte rotationIn,
			final CallbackInfo ci) {
		// All of the below types have no reason to be saved to disk
		// This is because they can/should be calculated when needed
		// Furthermore if a sponge plugin adds a MapDecoration, isPersistent
		// should also be changed to true
		switch (typeIn) {
			case PLAYER:
			case PLAYER_OFF_MAP:
			case PLAYER_OFF_LIMITS:
			case FRAME: {
				this.impl$isPersistent = false;
				break;
			}
			default: {
				this.impl$isPersistent = true;
				break;
			}

		}
	}

	@Override
	public void bridge$setPersistent(final boolean persistent) {
		this.impl$isPersistent = persistent;
	}

	@Override
	public boolean bridge$isPersistent() {
		return this.impl$isPersistent;
	}

	@Override
	public void bridge$setKey(final String key) {
		this.impl$key = key;
	}

	@Override
	public String bridge$getKey() {
		return this.impl$key;
	}
}
