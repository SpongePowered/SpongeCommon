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
package org.spongepowered.common.data.provider.block.state;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.api.data.Keys;
import org.spongepowered.common.data.provider.DataProviderRegistrator;

public final class TorchData {

    private TorchData() {
    }

    // @formatter:off
    public static void register(final DataProviderRegistrator registrator) {
        registrator
                .asImmutable(BlockState.class)
                    .create(Keys.IS_ATTACHED)
                        .get(h -> h.getBlock() instanceof WallTorchBlock || h.getBlock() instanceof RedstoneWallTorchBlock)
                        .set((h, v) -> {
                            final Block block = h.getBlock();
                            final boolean isWallBlock = block instanceof WallTorchBlock || block instanceof RedstoneWallTorchBlock;
                            if (v == isWallBlock) {
                                return h;
                            }
                            if (block instanceof RedstoneTorchBlock) {
                                return (isWallBlock ? Blocks.REDSTONE_WALL_TORCH : Blocks.REDSTONE_TORCH).defaultBlockState()
                                        .setValue(RedstoneTorchBlock.LIT, h.getValue(RedstoneTorchBlock.LIT));
                            }
                            return (isWallBlock ? Blocks.WALL_TORCH : Blocks.TORCH).defaultBlockState();
                        })
                        .supports(h -> h.getBlock() instanceof TorchBlock);
    }
    // @formatter:on
}
