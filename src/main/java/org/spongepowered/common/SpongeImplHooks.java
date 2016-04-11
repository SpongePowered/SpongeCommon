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
package org.spongepowered.common;


import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent;
import org.spongepowered.api.event.message.MessageEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

/**
 * Utility that fires events that normally Forge fires at (in spots). Typically
 * our penultimate goal is to not remove spots where events occur but sometimes
 * it happens (in @Overwrites typically). Normally events that are in Forge are
 * called themselves in SpongeVanilla but when it can't really occur, we fix
 * this issue with Sponge by overwriting this class
 */
public final class SpongeImplHooks {

    public static LoadWorldEvent createLoadWorldEvent(World world) {
        return SpongeEventFactory.createLoadWorldEvent(Cause.of(NamedCause.source(SpongeImpl.getGame().getServer())), world);
    }

    public static ClientConnectionEvent.Join createClientConnectionEventJoin(Cause cause, MessageChannel originalChannel,
            Optional<MessageChannel> channel, MessageEvent.MessageFormatter formatter, Player targetEntity, boolean messageCancelled) {
        return SpongeEventFactory.createClientConnectionEventJoin(cause, originalChannel, channel, formatter, targetEntity, messageCancelled);
    }

    public static RespawnPlayerEvent createRespawnPlayerEvent(Cause cause, Transform<World> fromTransform, Transform<World> toTransform,
            Player targetEntity, boolean bedSpawn) {
        return SpongeEventFactory.createRespawnPlayerEvent(cause, fromTransform, toTransform, targetEntity, bedSpawn);
    }

    public static ClientConnectionEvent.Disconnect createClientConnectionEventDisconnect(Cause cause, MessageChannel originalChannel,
            Optional<MessageChannel> channel, MessageEvent.MessageFormatter formatter, Player targetEntity, boolean messageCancelled) {
        return SpongeEventFactory.createClientConnectionEventDisconnect(cause, originalChannel, channel, formatter, targetEntity, messageCancelled);
    }

    public static boolean blockHasTileEntity(Block block, IBlockState state) {
        return block instanceof ITileEntityProvider;
    }

    public static int getBlockLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.getLightOpacity();
    }

    public static boolean shouldRefresh(TileEntity tile, net.minecraft.world.World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    public static TileEntity createTileEntity(Block block, net.minecraft.world.World world, IBlockState state) {
        if (block instanceof ITileEntityProvider) {
            return ((ITileEntityProvider)block).createNewTileEntity(world, block.getMetaFromState(state));
        }
        return null;
    }

    // TODO - Determine if this is still needed
    @SuppressWarnings("unchecked")
    public static void updateComparatorOutputLevel(net.minecraft.world.World world, BlockPos pos, Block blockIn) {
        Iterator<EnumFacing> iterator = EnumFacing.Plane.HORIZONTAL.iterator();

        while (iterator.hasNext()) {
            EnumFacing enumfacing = iterator.next();
            BlockPos neighborPosition = pos.offset(enumfacing);

            if (world.isBlockLoaded(neighborPosition)) {
                IBlockState iblockstate = world.getBlockState(neighborPosition);

                if (Blocks.unpowered_comparator.isAssociatedBlock(iblockstate.getBlock())) {
                    iblockstate.getBlock().onNeighborBlockChange(world, neighborPosition, iblockstate, blockIn);
                } else if (iblockstate.getBlock().isNormalCube(iblockstate)) {
                    neighborPosition = neighborPosition.offset(enumfacing);
                    iblockstate = world.getBlockState(neighborPosition);

                    if (Blocks.unpowered_comparator.isAssociatedBlock(iblockstate.getBlock())) {
                        iblockstate.getBlock().onNeighborBlockChange(world, neighborPosition, iblockstate, blockIn);
                    }
                }
            }
        }
    }

    public static void addItemStackToListForSpawning(Collection<ItemStack> itemStacks, ItemStack itemStack) {
        // This is the hook that can be overwritten to handle merging the item stack into an already existing item stack
        itemStacks.add(itemStack);
    }
}
