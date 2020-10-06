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
package org.spongepowered.common.data.manipulator.immutable.item;


import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapInfoItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapInfoItemData;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.common.data.manipulator.immutable.common.AbstractImmutableSingleData;
import org.spongepowered.common.data.manipulator.mutable.item.SpongeMapInfoItemData;
import org.spongepowered.common.data.value.immutable.ImmutableSpongeValue;

public class ImmutableSpongeMapInfoItemData extends AbstractImmutableSingleData<MapInfo, ImmutableMapInfoItemData, MapInfoItemData> implements ImmutableMapInfoItemData {
    public ImmutableSpongeMapInfoItemData() {
        this(SpongeMapInfoItemData.getDefaultMapInfo());
    }

    public ImmutableSpongeMapInfoItemData(MapInfo mapInfo) {
        super(ImmutableMapInfoItemData.class, mapInfo, Keys.MAP_INFO);
    }

    @Override
    public ImmutableValue<MapInfo> mapInfo() {
        return new ImmutableSpongeValue<>(Keys.MAP_INFO, SpongeMapInfoItemData.getDefaultMapInfo(), this.getValue());
    }

    @Override
    protected ImmutableValue<?> getValueGetter() {
        return this.mapInfo();
    }

    @Override
    public MapInfoItemData asMutable() {
        return new SpongeMapInfoItemData(this.getValue());
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Keys.MAP_INFO.getQuery(), this.getValue());
    }

}
