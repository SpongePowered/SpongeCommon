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
package org.spongepowered.common.data.processor.value.entity;

import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.api.data.DataTransactionBuilder;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.Career;
import org.spongepowered.api.data.type.Careers;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.common.data.processor.common.AbstractSpongeValueProcessor;
import org.spongepowered.common.data.util.DataConstants;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.interfaces.entity.IMixinVillager;

import java.util.Optional;

public class CareerValueProcessor extends AbstractSpongeValueProcessor<EntityVillager, Career, Value<Career>> {

    public CareerValueProcessor() {
        super(EntityVillager.class, Keys.CAREER);
    }

    @Override
    public Value<Career> constructValue(Career defaultValue) {
        return new SpongeValue<>(Keys.CAREER, DataConstants.CAREER_DEFAULT, defaultValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionBuilder.failNoData();
    }

    @Override
    protected boolean set(EntityVillager container, Career value) {
        ((IMixinVillager) container).setCareer(value);
        return true;
    }

    @Override
    protected Optional<Career> getVal(EntityVillager container) {
        return Optional.of(((IMixinVillager) container).getCareer());
    }

    @Override
    protected ImmutableValue<Career> constructImmutableValue(Career value) {
        return ImmutableSpongeValue.cachedOf(Keys.CAREER, DataConstants.CAREER_DEFAULT, value);
    }
}
