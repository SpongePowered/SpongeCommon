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
package org.spongepowered.common.data.manipulator.mutable.tileentity;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.tileentity.ImmutableBrewingStandData;
import org.spongepowered.api.data.manipulator.mutable.tileentity.BrewingStandData;
import org.spongepowered.api.data.value.mutable.MutableBoundedValue;
import org.spongepowered.common.data.manipulator.immutable.tileentity.ImmutableSpongeBrewingStandData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractBoundedComparableData;
import org.spongepowered.common.data.util.ComparatorUtil;

public class SpongeBrewingStandData extends AbstractBoundedComparableData<Integer, BrewingStandData, ImmutableBrewingStandData> implements BrewingStandData {

    public SpongeBrewingStandData() {
        this(400);
    }

    public SpongeBrewingStandData(int value) {
        this(value, 0, Integer.MAX_VALUE);
    }

    // For reflection
    public SpongeBrewingStandData(int value, int minimum, int maximum) {
        super(BrewingStandData.class, value, Keys.REMAINING_BREW_TIME, ComparatorUtil.intComparator(), ImmutableSpongeBrewingStandData.class, minimum, maximum, 400);
    }

    @Override
    public MutableBoundedValue<Integer> remainingBrewTime() {
        return getValueGetter();
    }
}
