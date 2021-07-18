package org.spongepowered.common.world.volume.archetype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.entity.BlockEntityArchetype;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.fluid.FluidState;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.archetype.entity.EntityArchetypeEntry;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumeStream;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ArchetypeVolumeTest {

    public static Stream<Arguments> testLogicalCenterIsCorrect() {
        return Stream.of(
                // should be max at 1, 1, 1, block is assumed from 0.5 to 1.5, centre is 1, 1, 1
                Arguments.of(new Vector3i(0, 0, 0), new Vector3i(2, 2, 2), new Vector3d(1, 1, 1)),
                Arguments.of(new Vector3i(0, 0, 0), new Vector3i(3, 3, 3), new Vector3d(1.5, 1.5, 1.5)),
                Arguments.of(new Vector3i(0, 0, 0), new Vector3i(4, 4, 4), new Vector3d(2, 2, 2))
        );
    }

    @ParameterizedTest
    @MethodSource
    final void testLogicalCenterIsCorrect(final Vector3i min, final Vector3i size, final Vector3d expectedCenter) {
        final TestArchetypeVolume sut = new TestArchetypeVolume(min, size);
        Assertions.assertEquals(expectedCenter, sut.logicalCenter(), "Logical center is wrong!");
    }

    public static final class TestArchetypeVolume implements ArchetypeVolume {

        private final Vector3i min;
        private final Vector3i max;
        private final Vector3i size;

        TestArchetypeVolume(final Vector3i min, final Vector3i size) {
            this.min = min;
            this.size = size;
            this.max = this.min.add(this.size).sub(Vector3i.ONE);
        }

        @Override
        public Vector3i blockMin() {
            return this.min;
        }

        @Override
        public Vector3i blockMax() {
            return this.max;
        }

        @Override
        public Vector3i blockSize() {
            return this.size;
        }

        @Override
        public boolean containsBlock(final int x, final int y, final int z) {
            return false;
        }

        @Override
        public boolean isAreaAvailable(final int x, final int y, final int z) {
            return false;
        }

        @Override
        public ArchetypeVolume transform(final Transformation transformation) {
            return null;
        }

        @Override
        public Optional<BlockEntityArchetype> blockEntityArchetype(final int x, final int y, final int z) {
            return Optional.empty();
        }

        @Override
        public Map<Vector3i, BlockEntityArchetype> blockEntityArchetypes() {
            return null;
        }

        @Override
        public VolumeStream<ArchetypeVolume, BlockEntityArchetype> blockEntityArchetypeStream(
                final Vector3i min, final Vector3i max, final StreamOptions options) {
            return null;
        }

        @Override
        public void addBlockEntity(final int x, final int y, final int z, final BlockEntityArchetype archetype) {

        }

        @Override
        public void removeBlockEntity(final int x, final int y, final int z) {

        }

        @Override
        public Collection<EntityArchetype> entityArchetypes() {
            return null;
        }

        @Override
        public Collection<EntityArchetypeEntry> entityArchetypesByPosition() {
            return null;
        }

        @Override
        public Collection<EntityArchetype> entityArchetypes(final Predicate<EntityArchetype> filter) {
            return null;
        }

        @Override
        public VolumeStream<ArchetypeVolume, EntityArchetype> entityArchetypeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
            return null;
        }

        @Override
        public Stream<EntityArchetypeEntry> entitiesByPosition() {
            return null;
        }

        @Override
        public void addEntity(final EntityArchetypeEntry entry) {

        }

        @Override
        public Biome biome(final int x, final int y, final int z) {
            return null;
        }

        @Override
        public VolumeStream<ArchetypeVolume, Biome> biomeStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
            return null;
        }

        @Override
        public boolean setBiome(final int x, final int y, final int z, final Biome biome) {
            return false;
        }

        @Override
        public BlockState block(final int x, final int y, final int z) {
            return null;
        }

        @Override
        public FluidState fluid(final int x, final int y, final int z) {
            return null;
        }

        @Override
        public int highestYAt(final int x, final int z) {
            return 0;
        }

        @Override
        public VolumeStream<ArchetypeVolume, BlockState> blockStateStream(final Vector3i min, final Vector3i max, final StreamOptions options) {
            return null;
        }

        @Override
        public boolean setBlock(final int x, final int y, final int z, final BlockState block) {
            return false;
        }

        @Override
        public boolean removeBlock(final int x, final int y, final int z) {
            return false;
        }
    }

}
