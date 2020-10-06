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
package org.spongepowered.common.data.manipulator.mutable.item;

import net.minecraft.world.storage.MapData;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.item.ImmutableMapInfoItemData;
import org.spongepowered.api.data.manipulator.mutable.item.MapInfoItemData;
import org.spongepowered.api.data.value.mutable.Value;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.common.data.manipulator.immutable.item.ImmutableSpongeMapInfoItemData;
import org.spongepowered.common.data.manipulator.mutable.common.AbstractSingleData;
import org.spongepowered.common.data.value.mutable.SpongeValue;
import org.spongepowered.common.util.Constants;

public class SpongeMapInfoItemData extends AbstractSingleData<MapInfo, MapInfoItemData, ImmutableMapInfoItemData> implements MapInfoItemData {

    public static MapInfo getDefaultMapInfo() {
        return (MapInfo) new MapData(Constants.Map.MAP_PREFIX + "0");
    }

    public SpongeMapInfoItemData() {
        // This means someone has got the default for which map (also used for registration)
        // an ItemStack refers to. So we give them map 0.
        this(SpongeMapInfoItemData.getDefaultMapInfo());
    }

    public SpongeMapInfoItemData(MapInfo mapInfo) {
        super(MapInfoItemData.class, mapInfo, Keys.MAP_INFO);
    }

    @Override
    public Value<MapInfo> mapInfo() {
        return new SpongeValue<>(Keys.MAP_INFO, SpongeMapInfoItemData.getDefaultMapInfo(), this.getValue());
    }

    @Override
    protected Value<?> getValueGetter() {
        return this.mapInfo();
    }

    @Override
    public MapInfoItemData copy() {
        return new SpongeMapInfoItemData(this.getValue());
    }

    @Override
    public ImmutableMapInfoItemData asImmutable() {
        return new ImmutableSpongeMapInfoItemData();
    }

    @Override
    public DataContainer toContainer() {
        return DataContainer.createNew()
                .set(Keys.MAP_INFO.getQuery(), this.getValue());
    }
}
