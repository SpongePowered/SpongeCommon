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
package org.spongepowered.common.mixin.core.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockFence;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.key.Key;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.ImmutableDataManipulator;
import org.spongepowered.api.data.manipulator.immutable.block.ImmutableConnectedDirectionData;
import org.spongepowered.api.data.value.BaseValue;
import org.spongepowered.api.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.common.data.ImmutableDataCachingUtil;
import org.spongepowered.common.data.manipulator.immutable.block.ImmutableSpongeConnectedDirectionData;
import org.spongepowered.common.util.Constants;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(BlockFence.class)
public abstract class BlockFenceMixin extends BlockMixin {

    private static final Map<Direction, PropertyBool> impl$DIRECTION_TO_PROPERTY_MAPPING;

    static {
        ImmutableMap.Builder<Direction, PropertyBool> directionToPropertyMappingBuilder = ImmutableMap.builder();
        directionToPropertyMappingBuilder.put(Direction.NORTH, BlockFence.NORTH);
        directionToPropertyMappingBuilder.put(Direction.SOUTH, BlockFence.SOUTH);
        directionToPropertyMappingBuilder.put(Direction.WEST, BlockFence.WEST);
        directionToPropertyMappingBuilder.put(Direction.EAST, BlockFence.EAST);
        impl$DIRECTION_TO_PROPERTY_MAPPING = directionToPropertyMappingBuilder.build();
    }

    @SuppressWarnings("RedundantTypeArguments") // some java compilers will not calculate this generic correctly
    @Override
    public ImmutableList<ImmutableDataManipulator<?, ?>> bridge$getManipulators(final IBlockState blockState) {
        return ImmutableList.<ImmutableDataManipulator<?, ?>>of(impl$getConnectedDirectionData(blockState));
    }

    @Override
    public boolean bridge$supports(final Class<? extends ImmutableDataManipulator<?, ?>> immutable) {
        return ImmutableConnectedDirectionData.class.isAssignableFrom(immutable);
    }

    @Override
    public Optional<BlockState> bridge$getStateWithData(final IBlockState blockState, final ImmutableDataManipulator<?, ?> manipulator) {
        if (manipulator instanceof ImmutableConnectedDirectionData) {
            final ImmutableConnectedDirectionData connectedDirectionData = (ImmutableConnectedDirectionData) manipulator;
            return Optional.of((BlockState) Constants.DirectionFunctions.applyConnectedDirections(blockState, impl$DIRECTION_TO_PROPERTY_MAPPING,
                (sourceBlockState, property) -> true, (sourceBlockState, property) -> false, connectedDirectionData.connectedDirections().get()));
        }
        return super.bridge$getStateWithData(blockState, manipulator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<BlockState> bridge$getStateWithValue(final IBlockState blockState, final Key<? extends BaseValue<E>> key, final E value) {
        if (key.equals(Keys.CONNECTED_DIRECTIONS)) {
            return Optional.of((BlockState) Constants.DirectionFunctions.applyConnectedDirections(blockState, impl$DIRECTION_TO_PROPERTY_MAPPING,
                (sourceBlockState, property) -> true, (sourceBlockState, property) -> false, (Set<Direction>) value));
        } else if (key.equals(Keys.CONNECTED_EAST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockFence.EAST, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_NORTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockFence.NORTH, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_SOUTH)) {
            return Optional.of((BlockState) blockState.withProperty(BlockFence.SOUTH, (Boolean) value));
        } else if (key.equals(Keys.CONNECTED_WEST)) {
            return Optional.of((BlockState) blockState.withProperty(BlockFence.WEST, (Boolean) value));
        }
        return super.bridge$getStateWithValue(blockState, key, value);
    }

    private ImmutableConnectedDirectionData impl$getConnectedDirectionData(final IBlockState blockState) {
        final Set<Direction> directions = EnumSet.noneOf(Direction.class);
        final Boolean north = blockState.getValue(BlockFence.NORTH);
        final Boolean east = blockState.getValue(BlockFence.EAST);
        final Boolean west = blockState.getValue(BlockFence.WEST);
        final Boolean south = blockState.getValue(BlockFence.SOUTH);
        if (north) {
            directions.add(Direction.NORTH);
        }
        if (south) {
            directions.add(Direction.SOUTH);
        }
        if (west) {
            directions.add(Direction.WEST);
        }
        if (east) {
            directions.add(Direction.EAST);
        }
        return ImmutableDataCachingUtil.getManipulator(ImmutableSpongeConnectedDirectionData.class, directions);
    }
}
