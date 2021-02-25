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
package org.spongepowered.common.mixin.api.mcp.world.storage;

import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.api.map.MapInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.common.SpongeCommon;
import org.spongepowered.common.bridge.world.storage.MapDataBridge;
import org.spongepowered.common.bridge.world.storage.MapDecorationBridge;
import org.spongepowered.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mixin(MapItemSavedData.class)
public abstract class MapDataMixin extends SavedData implements MapDataBridge {
    /*
     * Lots of possible String formats:
     *  - Player name
     *  - '+' for villager treasure maps
     *  - 'frame-' + item frame entity id
     *
     * No decorations are saved to disk. Player/frames are recalculated and treasure maps have nbt on their
     * ItemStacks, therefore, due to our adding of the ability to make maps and add decorations, it could
     * get out of sync quite easily. Therefore we opt to save the ones that would be saved on ItemStacks
     * that aren't ones that are depend on the world surroundings (Players, item frames)
     * (but also leave them there)
     */
    @Final
    @Shadow public Map<String, MapDecoration> decorations;

    @Shadow public abstract void setDirty(int x, int y);

    private int impl$mapId = 0;
    private UUID impl$uuid = UUID.randomUUID();

    public MapDataMixin(final String name) {
        super(name);
    }

    @Override
    public void bridge$updateWholeMap() {
        this.setDirty(0,0);
        this.setDirty(Constants.Map.MAP_MAX_INDEX, Constants.Map.MAP_MAX_INDEX);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public void bridge$setDecorations(final Set<org.spongepowered.api.map.decoration.MapDecoration> newDecorations) {
        this.decorations.clear();
        for (org.spongepowered.api.map.decoration.MapDecoration decoration : newDecorations) {
            this.impl$addDecorationToDecorationsMapIfNotExists((MapDecoration) decoration);
        }
        for (MapDecoration existingDecoration : this.decorations.values()) {
            if (!newDecorations.contains(existingDecoration)) {
                // Removed.
                ((MapDecorationBridge)existingDecoration).notifyRemovedFromMap((MapInfo) this);
                this.setDirty();
            }
        }
    }

    @Override
    public Set<org.spongepowered.api.map.decoration.MapDecoration> bridge$getDecorations() {
        return this.decorations.values().stream()
                .map(mapDecoration -> (org.spongepowered.api.map.decoration.MapDecoration)mapDecoration)
                .collect(Collectors.toSet());
    }

    @Override
    public int bridge$getMapId() {
        return this.impl$mapId;
    }

    @Override
    public void bridge$setMapId(final int mapId) {
        this.impl$mapId = mapId;
    }

    @Nonnull
    @Override
    public UUID bridge$getUniqueId() {
        return this.impl$uuid;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void impl$setMapId(final String mapname, final CallbackInfo ci) {
        final String id = mapname.substring(Constants.Map.MAP_PREFIX.length());
        try {
            this.impl$mapId = Integer.parseInt(id);
        } catch (final NumberFormatException e) {
            SpongeCommon.getLogger().error("Map id could not be got from map name, (" + mapname + ")", e);
        }
    }

    // Save to disk if we don't want to auto explore the map
    // No point saving additional data if its default.
    /*@Inject(method = "write", at = @At("RETURN"))
    public void impl$writeAdditionalNBT(final CompoundTag compound, final CallbackInfoReturnable<CompoundTag> cir) {
        compound.putUUID(Constants.Map.SPONGE_UUID_KEY, this.impl$uuid);
        ListTag nbtList = new ListTag();
        for (final Map.Entry<String, MapDecoration> entry : this.decorations.entrySet()) {
            final org.spongepowered.api.map.decoration.MapDecoration mapDecoration = (org.spongepowered.api.map.decoration.MapDecoration)entry.getValue();
            if (!((MapDecorationBridge) mapDecoration).bridge$isPersistent()) {
                continue;
            }
            final CompoundTag nbt = MapUtil.mapDecorationToNBT(mapDecoration);
            nbt.putString("id", entry.getKey());
            nbtList.add(nbt);
        }
        compound.put(Constants.Map.DECORATIONS_KEY, nbtList);
    }

    @Inject(method = "read", at = @At("RETURN"))
    public void impl$readAdditionalNBT(final CompoundTag nbt, final CallbackInfo ci) {
        if (nbt.hasUUID(Constants.Map.SPONGE_UUID_KEY)) {
            this.impl$uuid = nbt.getUUID(Constants.Map.SPONGE_UUID_KEY);
        }
        final ListTag decorationsList = ((ListTag)nbt.get(Constants.Map.DECORATIONS_KEY));
        if (decorationsList != null) {
            for (int i = 0; i < decorationsList.size(); i++) {
                final CompoundTag decorationNbt = (CompoundTag) decorationsList.get(i);
                this.impl$addDecorationToDecorationsMapIfNotExists(MapUtil.mapDecorationFromNBT(decorationNbt));
            }
        }
    }*/

    public void impl$addDecorationToDecorationsMapIfNotExists(final MapDecoration mapDecoration) {
        MapDecorationBridge bridge = (MapDecorationBridge)mapDecoration;
        bridge.bridge$setPersistent(true); // Anything saved should be persistent so it can save next time too.
        if (this.decorations.containsKey(bridge.bridge$getKey())) {
            return;
        }
        ((MapDecorationBridge)mapDecoration).notifyAddedToMap((MapInfo) this);
        this.decorations.put(bridge.bridge$getKey(), mapDecoration);
        this.setDirty();
    }

    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(method = "tickCarriedBy",
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/world/item/ItemStack;isFramed()Z")),
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"),
            allow = 1)
    public Object impl$setKeyOnValue(final Map map, final Object key, final Object value) {
        ((MapDecorationBridge) value).bridge$setKey((String) key);
        return map.put(key, value);
    }
}
