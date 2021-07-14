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
package org.spongepowered.common.world.volume.buffer.archetype;

import net.minecraft.nbt.CompoundTag;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.event.cause.entity.SpawnType;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.Palette;
import org.spongepowered.api.world.schematic.PaletteTypes;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.block.entity.BlockEntityArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeApplicators;
import org.spongepowered.api.world.volume.stream.VolumeCollectors;
import org.spongepowered.api.world.volume.stream.VolumeElement;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.common.block.entity.SpongeBlockEntityArchetype;
import org.spongepowered.common.entity.SpongeEntityArchetype;
import org.spongepowered.common.event.tracking.PhaseContext;
import org.spongepowered.common.event.tracking.PhaseTracker;
import org.spongepowered.common.event.tracking.phase.plugin.PluginPhase;
import org.spongepowered.common.util.DirectionUtil;
import org.spongepowered.common.world.volume.SpongeVolumeStream;
import org.spongepowered.common.world.volume.VolumeStreamUtils;
import org.spongepowered.common.world.volume.buffer.AbstractVolumeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.blockentity.MutableMapBlockEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.archetype.entity.ObjectArrayMutableEntityArchetypeBuffer;
import org.spongepowered.common.world.volume.buffer.biome.ByteArrayMutableBiomeBuffer;
import org.spongepowered.common.world.volume.buffer.block.ArrayMutableBlockBuffer;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SpongeArchetypeVolume extends AbstractVolumeBuffer implements ArchetypeVolume {

    private final ByteArrayMutableBiomeBuffer biomes;
    private final ArrayMutableBlockBuffer blocks;
    private final BlockEntityArchetypeVolume.Mutable blockEntities;
    private final ObjectArrayMutableEntityArchetypeBuffer entities;

    public SpongeArchetypeVolume(final Vector3i start, final Vector3i size, final RegistryHolder registries) {
        super(start, size);
        final ArrayMutableBlockBuffer blocks = new ArrayMutableBlockBuffer(start, size);
        this.blocks = blocks;
        this.blockEntities = new MutableMapBlockEntityArchetypeBuffer(blocks);
        this.biomes = new ByteArrayMutableBiomeBuffer(
            PaletteTypes.BIOME_PALETTE.get().create(registries, RegistryTypes.BIOME),
            start,
            size
        );
        this.entities = new ObjectArrayMutableEntityArchetypeBuffer(start, size);
    }

    private SpongeArchetypeVolume(final Vector3i start, final Vector3i size, final Palette<Biome, Biome> biomePalette) {
        super(start, size);
        final ArrayMutableBlockBuffer blocks = new ArrayMutableBlockBuffer(start, size);
        this.blocks = blocks;
        this.blockEntities = new MutableMapBlockEntityArchetypeBuffer(blocks);
        this.biomes = new ByteArrayMutableBiomeBuffer(
            biomePalette.asImmutable().asMutable(Sponge.server().registries()),
            start,
            size
        );
        this.entities = new ObjectArrayMutableEntityArchetypeBuffer(start, size);
    }

    @Override
    public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
        return Optional.empty();
    }

    @Override
    public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
        return this.blockEntities.blockEntityArchetypes();
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(final Vector3i min, final Vector3i max,
        final StreamOptions options
    ) {

        final Vector3i blockMin = this.blockMin();
        final Vector3i blockMax = this.blockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, BlockEntityArchetype>> stateStream = this.blockEntities.blockEntityArchetypeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes() {
        return this.entities.entityArchetypes();
    }

    @Override
    public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
        return this.entities.entityArchetypesByPosition();
    }

    @Override
    public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
        return this.entities.entityArchetypes(filter);
    }

    @Override
    public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(
        final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.blockMin();
        final Vector3i blockMax = this.blockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, EntityArchetype>> stateStream = this.entities.entityArchetypeStream(min, max, options).toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public Stream<EntityArchetypeEntry> entitiesByPosition() {
        return this.entities.entitiesByPosition();
    }

    @Override
    public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
        return this.blockEntities.setBlock(x, y, z, block);
    }

    @Override
    public boolean removeBlock(final int x, final int y, final int z) {
        return this.blockEntities.removeBlock(x, y, z);
    }

    @Override
    public BlockState block(final int x, final int y, final int z) {
        return this.blocks.block(x, y, z);
    }

    @Override
    public FluidState fluid(final int x, final int y, final int z) {
        return this.blocks.fluid(x, y, z);
    }

    @Override
    public int highestYAt(final int x, final int z) {
        return this.blocks.highestYAt(x, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options
    ) {
        final Vector3i blockMin = this.blockMin();
        final Vector3i blockMax = this.blockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final ArrayMutableBlockBuffer buffer;
        if (options.carbonCopy()) {
            buffer = this.blocks.copy();
        } else {
            buffer = this.blocks;
        }
        final Stream<VolumeElement<ArchetypeVolume, BlockState>> stateStream = IntStream.range(blockMin.x(), blockMax.x() + 1)
            .mapToObj(x -> IntStream.range(blockMin.z(), blockMax.z() + 1)
                .mapToObj(z -> IntStream.range(blockMin.y(), blockMax.y() + 1)
                    .mapToObj(y -> VolumeElement.of((ArchetypeVolume) this, () -> buffer.block(x, y, z), new Vector3i(x, y, z)))
                ).flatMap(Function.identity())
            ).flatMap(Function.identity());
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {
        this.blockEntities.addBlockEntity(x, y, z, archetype);
    }

    @Override
    public void removeBlockEntity(final int x, final int y, final int z) {
        this.blockEntities.removeBlockEntity(x, y, z);
    }

    public Palette<BlockState, BlockType> getBlockPalette() {
        return this.blocks.getPalette();
    }

    public Palette<Biome, Biome> getBiomePalette() {
        return this.biomes.getPalette();
    }

    @Override
    public void addEntity(final EntityArchetypeEntry entry) {
        this.entities.addEntity(entry);
    }

    @Override
    public Biome biome(final int x, final int y, final int z) {
        return this.biomes.biome(x, y, z);
    }

    @Override
    public VolumeStream<ArchetypeVolume, Biome> biomeStream(
        final Vector3i min,
        final Vector3i max,
        final StreamOptions options
    ) {
        final Vector3i blockMin = this.blockMin();
        final Vector3i blockMax = this.blockMax();
        VolumeStreamUtils.validateStreamArgs(min, max, blockMin, blockMax, options);
        final Stream<VolumeElement<ArchetypeVolume, Biome>> stateStream = this.biomes.biomeStream(min, max, options)
            .toStream()
            .map(element -> VolumeElement.of(this, element::type, element.position()));
        return new SpongeVolumeStream<>(stateStream, () -> this);
    }

    @Override
    public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
        return this.biomes.setBiome(x, y, z, biome);
    }

    @Override
    public SpongeArchetypeVolume rotate(final Rotation rotation) {
        final SpongeArchetypeVolume copy = new SpongeArchetypeVolume(this.start, this.size, this.biomes.getPalette());
        // todo - math rotations are pain, there's two aspects needing to be taken care of:
        //   - rotating the volume but retaining the same coordinate boundaries
        //   - applying rotations to archetypes (as already partially done below)
        final Vector3d center = this.start.toDouble().add(this.size.toDouble().div(2));
        this.blockStateStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                copy,
                VolumePositionTranslators.rotateBlocksOn(this.start, center, rotation),
                VolumeApplicators.applyBlocks()
            ));
        this.blockEntityArchetypeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                copy,
                VolumePositionTranslators.rotateOn(this.start, center, rotation, (position, e) -> {
                    if (e instanceof SpongeBlockEntityArchetype && ((SpongeBlockEntityArchetype) e).getCompound().contains("Rot")) {
                        final int rot = ((SpongeBlockEntityArchetype) e).getCompound().getInt("Rot");
                        final Direction direction = DirectionUtil.fromRotation(rot);
                        final Direction newDirection = Direction.closest(position.add(direction.asBlockOffset()).toDouble());
                        final SpongeBlockEntityArchetype newOne = ((SpongeBlockEntityArchetype) e).copy();
                        newOne.getCompound().putByte("Rot", (byte) DirectionUtil.toRotation(newDirection));
                        return newOne;
                    }
                    return e;
                }),
                VolumeApplicators.applyBlockEntityArchetypes()
            ));
        this.biomeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                copy,
                VolumePositionTranslators.rotateOn(this.start, center, rotation, (p, e) -> e),
                VolumeApplicators.applyBiomes()
            ));
        this.entityArchetypeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
            .apply(VolumeCollectors.of(
                copy,
                VolumePositionTranslators.rotateOn(this.start, center, rotation, (p, e) -> {
                    if (e instanceof SpongeEntityArchetype && !((SpongeEntityArchetype) e).getCompound().isEmpty()) {
                        final SpongeEntityArchetype newCopy = ((SpongeEntityArchetype) e).copy();
                        final CompoundTag compound = newCopy.getCompound();
                        // TODO - verify these rotations work
                        if (compound.contains("Leash")) {
                            final CompoundTag leashCompound = compound.getCompound("Leash");
                            leashCompound.putInt("X", p.x());
                            leashCompound.putInt("Y", p.y());
                            leashCompound.putInt("Z", p.z());
                        }
                        final boolean hasTilePosition = compound.contains("TileX") && compound.contains("TileY") && compound.contains("TileZ");
                        final boolean hasFacing = compound.contains("Facing");
                        if (hasTilePosition) {
                            final Vector3i tilePosition = new Vector3i(compound.getInt("TileX"), compound.getInt("TileY"), compound.getInt("TileZ"));
                            final Vector3i newTilePosition = tilePosition.sub(this.start).add(p);
                            compound.putInt("TileX", newTilePosition.x());
                            compound.putInt("TileY", newTilePosition.y());
                            compound.putInt("TileZ", newTilePosition.z());

                            if (hasFacing) {
                                final boolean isPainting = e.type() == EntityTypes.PAINTING.get();
                                final int facing = compound.getInt("Facing");
                                final Direction existing = isPainting ? DirectionUtil.fromHorizontalHanging(facing) : DirectionUtil.fromHanging(facing);
                                final Quaterniond q = Quaterniond.fromAngleDegAxis(rotation.angle().degrees(), 0, 1, 0);
                                final Vector3d v = q.rotate(existing.asBlockOffset().toDouble());
                                final Direction closest = isPainting ? Direction.closestHorizontal(v) : Direction.closest(v);
                                compound.putByte("Facing", (byte) (isPainting ? DirectionUtil.toHorizontalHanging(closest) : DirectionUtil.toHanging(closest)));
                            }
                        }
                        return newCopy;
                    }
                    return e;
                }),
                VolumeApplicators.applyEntityArchetypes()
            ));
        return copy;
    }

    @Override
    public ArchetypeVolume mirror(Mirror mirror) {
        return null;
    }

    @Override
    public void applyToWorld(
        final ServerWorld target, final Vector3i placement, final Supplier<SpawnType> spawnContext
    ) {
        Objects.requireNonNull(target, "Target world cannot be null");
        Objects.requireNonNull(placement, "Target position cannot be null");
        try (final PhaseContext<@NonNull ?> context = PluginPhase.State.VOLUME_STREAM_APPLICATION
            .createPhaseContext(PhaseTracker.SERVER)
            .spawnType(spawnContext)
            .source(this)) {
            context.buildAndSwitch();
            this.blockStateStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBlocks(BlockChangeFlags.DEFAULT_PLACEMENT)
                ));

            this.biomeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBiomes()
                ));
            this.blockEntityArchetypeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyBlockEntityArchetype()
                ));
            this.entityArchetypeStream(this.blockMin(), this.blockMax(), StreamOptions.lazily())
                .apply(VolumeCollectors.of(
                    target,
                    VolumePositionTranslators.relativeTo(placement),
                    VolumeApplicators.applyEntityArchetype()
                ));
        }
    }
}
