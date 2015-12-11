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
package org.spongepowered.common.mixin.core.ban;

import com.google.gson.JsonObject;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.IPBanEntry;
import org.spongepowered.api.util.ban.Ban;
import org.spongepowered.api.util.ban.BanType;
import org.spongepowered.api.util.ban.BanTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Mixin(IPBanEntry.class)
public abstract class MixinIPBanEntry extends BanEntry implements Ban.Ip {

    public MixinIPBanEntry(Object valueIn, Date startDate, String banner, Date endDate, String banReason) {
        super(valueIn, startDate, banner, endDate, banReason);
    }

    private InetAddress address;

    @Inject(method = "<init>(Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;Ljava/util/Date;Ljava/lang/String;)V", at = @At("RETURN"))
    public void onInit(CallbackInfo ci) {
        this.setAddress();
    }

    @Inject(method = "<init>(Lcom/google/gson/JsonObject;)V", at = @At("RETURN"))
    public void onInit(JsonObject object, CallbackInfo ci) {
        this.setAddress();
    }

    private void setAddress() {
        try {
            this.address = InetAddress.getByName((String) this.value);
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Error parsing Ban IP address!", e);
        }
    }

    @Override
    public BanType getType() {
        return BanTypes.IP;
    }

    @Override
    public InetAddress getAddress() {
        return this.address;
    }
}
