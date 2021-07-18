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
package org.spongepowered.common.world.schematic;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

public final class ImmutableBimapPalette<T, R> implements Palette.Immutable<T, R> {

    private final ImmutableBiMap<Integer, T> ids;
    private final ImmutableBiMap<T, Integer> idsr;
    private final PaletteType<T, R> paletteType;
    private final int maxId;
    private final Registry<R> registry;

    public ImmutableBimapPalette(
        final PaletteType<T, R> paletteType,
        final Registry<R> registry,
        final BiMap<Integer, T> reference
    ) {
        final ImmutableBiMap.Builder<Integer, T> builder = ImmutableBiMap.builder();
        reference.forEach(builder::put);
        this.ids = builder.build();
        this.idsr = this.ids.inverse();
        this.paletteType = paletteType;
        this.registry = registry;
        int maxId = 0;
        for (final Integer id : this.ids.keySet()) {
            if (maxId < id) {
                maxId = id;
            }
        }
        this.maxId = maxId;
    }

    @Override
    public PaletteType<T, R> type() {
        return this.paletteType;
    }

    @Override
    public int highestId() {
        return this.maxId;
    }

    @Override
    public OptionalInt get(final T state) {
        final Integer value = this.idsr.get(state);
        if (value == null) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(value);
    }

    @Override
    public Optional<PaletteReference<T, R>> get(final int id) {
        return Optional.ofNullable(this.ids.get(id))
            .map(type -> MutableBimapPalette.createPaletteReference(type, this.paletteType, this.registry));
    }

    @Override
    public Stream<T> stream() {
        return this.idsr.keySet().stream();
    }

    @Override
    public Stream<Map.Entry<T, Integer>> streamWithIds() {
        return this.idsr.entrySet().stream();
    }

    @Override
    public Mutable<T, R> asMutable(final RegistryHolder holder) {
        return new MutableBimapPalette<>(this.paletteType, this.registry);
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ImmutableBimapPalette<?, ?> that = (ImmutableBimapPalette<?, ?>) o;
        return this.maxId == that.maxId &&
               this.ids.equals(that.ids) &&
               this.paletteType.equals(that.paletteType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.ids, this.paletteType, this.maxId);
    }
}
