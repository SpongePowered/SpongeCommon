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
package org.spongepowered.common.util.transformation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.spongepowered.api.Game;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.registry.BuilderProvider;
import org.spongepowered.api.registry.FactoryProvider;
import org.spongepowered.api.registry.Registry;
import org.spongepowered.api.registry.RegistryHolder;
import org.spongepowered.api.registry.RegistryKey;
import org.spongepowered.api.registry.RegistryType;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.api.util.Angle;
import org.spongepowered.api.util.mirror.Mirror;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.rotation.Rotations;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.api.world.biome.Biome;
import org.spongepowered.api.world.schematic.PaletteReference;
import org.spongepowered.api.world.schematic.PaletteType;
import org.spongepowered.api.world.volume.archetype.ArchetypeVolume;
import org.spongepowered.api.world.volume.stream.StreamOptions;
import org.spongepowered.api.world.volume.stream.VolumePositionTranslators;
import org.spongepowered.common.registry.SpongeRegistryKey;
import org.spongepowered.common.registry.SpongeRegistryType;
import org.spongepowered.common.test.stub.StubBlock;
import org.spongepowered.common.test.stub.StubBlockStatePaletteType;
import org.spongepowered.common.test.stub.StubKey;
import org.spongepowered.common.test.stub.StubPaletteType;
import org.spongepowered.common.test.stub.StubState;
import org.spongepowered.common.test.stub.StubbedRegistry;
import org.spongepowered.common.world.schematic.SpongePaletteReferenceFactory;
import org.spongepowered.common.world.volume.buffer.archetype.AbstractReferentArchetypeVolume;
import org.spongepowered.common.world.volume.buffer.archetype.SpongeArchetypeVolume;
import org.spongepowered.common.world.volume.stream.SpongeStreamOptionsBuilder;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector3i;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public final class VolumeTransformationTest {

    private static final Vector3i INVALID_STUB_POSITION = Vector3i.from(
        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    @Mock Game game;
    @Mock
    FactoryProvider factoryProvider;
    @Mock
    BuilderProvider builderProvider;
    @Mock
    RegistryHolder testholder;
    private MockedStatic<Sponge> spongeMock;

    @BeforeEach
    void setup() {
        this.spongeMock = Mockito.mockStatic(Sponge.class);
        this.spongeMock.when(Sponge::game)
            .thenReturn(this.game);
        // Set up Rotations
        final Registry<Rotation> rotation = new StubbedRegistry<>(
            () -> RegistryTypes.ROTATION,
            (k) -> {
                final Rotation rot = mock(Rotation.class, withSettings()
                    .defaultAnswer(CALLS_REAL_METHODS)
                );
                final String value = k.value();
                if ("none".equals(value)) {
                    when(rot.angle()).thenReturn(Angle.fromDegrees(0));
                } else if ("clockwise_90".equals(value)) {
                    when(rot.angle()).thenReturn(Angle.fromDegrees(90));
                } else if ("clockwise_180".equals(value)) {
                    when(rot.angle()).thenReturn(Angle.fromDegrees(180));
                } else {
                    when(rot.angle()).thenReturn(Angle.fromDegrees(270));
                }

                final Function<Rotation, Supplier<Rotation>> andFunc = (other) -> {
                    if (other == Rotations.NONE.get()) {
                        return () -> rot;
                    }
                    if (rot == Rotations.NONE.get()) {
                        return () -> other;
                    }
                    if (other == Rotations.CLOCKWISE_90.get()) {
                        if (rot == Rotations.CLOCKWISE_90.get()) {
                            return Rotations.CLOCKWISE_180;
                        } else if (rot == Rotations.CLOCKWISE_180.get()) {
                            return Rotations.COUNTERCLOCKWISE_90;
                        } else if (rot == Rotations.COUNTERCLOCKWISE_90.get()) {
                            return Rotations.NONE;
                        }
                    } else if (other == Rotations.CLOCKWISE_180.get()) {
                        if (rot == Rotations.CLOCKWISE_90.get()) {
                            return Rotations.COUNTERCLOCKWISE_90;
                        } else if (rot == Rotations.CLOCKWISE_180.get()) {
                            return Rotations.NONE;
                        } else if (rot == Rotations.COUNTERCLOCKWISE_90.get()) {
                            return Rotations.CLOCKWISE_90;
                        }
                    } else if (other == Rotations.COUNTERCLOCKWISE_90.get()) {
                        if (rot == Rotations.CLOCKWISE_90.get()) {
                            return Rotations.NONE;
                        } else if (rot == Rotations.CLOCKWISE_180.get()) {
                            return Rotations.CLOCKWISE_90;
                        } else if (rot == Rotations.COUNTERCLOCKWISE_90.get()) {
                            return Rotations.CLOCKWISE_180;
                        }
                    }
                    return Rotations.NONE;
                };
                when(rot.and(any(Rotation.class)))
                    .thenAnswer(i -> {
                        final Rotation other = i.getArgument(0);
                        return andFunc.apply(other).get();
                    });
                return rot;
            }

        );
        // Set up Blocks and BlockState
        final StubbedRegistry<BlockType> blocktypes = new StubbedRegistry<>(
            () -> RegistryTypes.BLOCK_TYPE,
            StubBlock::new
        );
        // Set up biomes
        final Registry<Biome> biomes = new StubbedRegistry<>(
            () -> RegistryTypes.BIOME,
            (key) -> mock(Biome.class)
        );
        // Set up palettes
        final Registry<PaletteType<?, ?>> paletteTypeRegistry = new StubbedRegistry<>(
            () -> RegistryTypes.PALETTE_TYPE,
            (key) -> new StubPaletteType<>()
        );
        when(this.game.factoryProvider()).thenReturn(this.factoryProvider);
        when(this.factoryProvider.provide(RegistryType.Factory.class))
            .thenReturn(new SpongeRegistryType.FactoryImpl());
        when(this.factoryProvider.provide(RegistryKey.Factory.class))
            .thenReturn(new SpongeRegistryKey.FactoryImpl());
        when(this.factoryProvider.provide(PaletteReference.Factory.class))
            .thenReturn(new SpongePaletteReferenceFactory());
        when(this.game.registries()).thenReturn(this.testholder);
        // and finally, set up the resourcekey stuff
        final ResourceKey.Factory resourceKeyFactory = mock(ResourceKey.Factory.class);
        when(resourceKeyFactory.of(any(String.class), any()))
            .thenAnswer((i) -> new StubKey(i.getArgument(0), i.getArgument(1)));
        when(this.factoryProvider.provide(ResourceKey.Factory.class))
            .thenReturn(resourceKeyFactory);
        when(this.testholder.registry(RegistryTypes.ROTATION))
            .thenReturn(rotation);
        when(this.testholder.registry(RegistryTypes.BLOCK_TYPE))
            .thenReturn(blocktypes);
        when(this.testholder.findRegistry(RegistryTypes.BLOCK_TYPE))
            .thenReturn(Optional.of(blocktypes));
        when(this.testholder.registry(RegistryTypes.BIOME))
            .thenReturn(biomes);
        when(this.testholder.registry(RegistryTypes.PALETTE_TYPE))
            .thenReturn(paletteTypeRegistry);
        final StubbedRegistry<Mirror> value = new StubbedRegistry<>(
            () -> RegistryTypes.MIRROR, (k) -> mock(Mirror.class));
        when(this.testholder.registry(RegistryTypes.MIRROR))
            .thenReturn(value);

        when(this.builderProvider.provide(Transformation.Builder.class))
            .thenReturn(new SpongeTransformationBuilder());
        when(this.builderProvider.provide(StreamOptions.Builder.class))
            .thenReturn(new SpongeStreamOptionsBuilder());
        when(this.game.builderProvider())
            .thenReturn(this.builderProvider);
        // And finally, register the blockstatepalette
        paletteTypeRegistry.register(ResourceKey.sponge("block_state_palette"), new StubBlockStatePaletteType());
    }

    @AfterEach
    void closeSponge() {
        this.spongeMock.close();
    }

    private static Stream<Arguments> testTransformationsOfPositions() {
        return Stream.of(
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.ZERO,
                Vector3i.from(1, 1, 1),
                0,
                RotationWanted.WantNone
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.UNIT_X,
                Vector3i.from(1, 1, 1),
                1,
                RotationWanted.Want90
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.from(-1, 1, 4),
                Vector3i.from(1, 1, 1),
                0,
                RotationWanted.WantNone
            ),
            Arguments.of(
                Vector3i.ZERO,
                Vector3i.from(2, 2, 2),
                Vector3i.from(-1, 1, 4),
                Vector3i.from(1, 1, 1),
                1,
                RotationWanted.Want90
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                1,
                RotationWanted.Want90
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                2,
                RotationWanted.Want270
            ),
            Arguments.of(
                Vector3i.from(1, -1, -1),
                Vector3i.from(2, 1, 0),
                Vector3i.ZERO,
                Vector3i.from(1, -1, -1),
                4,
                RotationWanted.Want180
            ),
            Arguments.of(
                Vector3i.from(-4, -1, -5),
                Vector3i.from(10, 7, 9),
                Vector3i.ZERO,
                Vector3i.from(1, -3, -1),
                8,
                RotationWanted.Want90
            ),
            Arguments.of(
                Vector3i.from(-4, -1, -5),
                Vector3i.from(10, 7, 9),
                Vector3i.from(-8, 3, -7),
                Vector3i.from(1, -3, -1),
                8,
                RotationWanted.Want90
            ),
            Arguments.of(
                Vector3i.from(-2, -4, -3),
                Vector3i.from(9, 1, 7),
                Vector3i.from(-6, 2, -4),
                Vector3i.from(1, -3, -1),
                8,
                RotationWanted.Want90
            )
        );
    }
    enum RotationWanted {
        Want90() {
            @Override
            Supplier<Rotation> getRotation() {
                return Rotations.CLOCKWISE_90;
            }
        },
        Want180() {
            @Override
            Supplier<Rotation> getRotation() {
                return Rotations.CLOCKWISE_180;
            }
        },
        Want270() {
            @Override
            Supplier<Rotation> getRotation() {
                return Rotations.COUNTERCLOCKWISE_90;
            }
        },
        WantNone() {
            @Override
            Supplier<Rotation> getRotation() {
                return Rotations.NONE;
            }
        };

        abstract Supplier<Rotation> getRotation();
    }

    @MethodSource("testTransformationsOfPositions")
    @ParameterizedTest
    void testTransformationsOfPositions(
        final Vector3i min, final Vector3i max, final Vector3i origin, final Vector3i testForRoundTrip,
        final int rotationCount, final RotationWanted wanted
    ) {
        final Vector3i rawMin = min.min(max);
        final Vector3i rawMax = max.max(min);
        final Vector3i size = rawMax.sub(rawMin).add(Vector3i.ONE);
        final Vector3i relativeMin = rawMin.sub(origin);


        final SpongeArchetypeVolume volume = new SpongeArchetypeVolume(relativeMin, size, this.testholder);

        final StubbedRegistry<BlockType> blockRegistry = (StubbedRegistry<BlockType>) RegistryTypes.BLOCK_TYPE.get();
        final Vector3i volMax = volume.blockMax().add(Vector3i.ONE);
        IntStream.range(relativeMin.x(), volMax.x()).forEach(x -> {
            IntStream.range(relativeMin.z(), volMax.z()).forEach(z -> {
                IntStream.range(relativeMin.y(), volMax.y()).forEach(y -> {
                    final BlockType block = blockRegistry.createEntry(
                        "minecraft", String.format("volumetest{%d, %d, %d}", x, y, z));
                    final BlockState blockState = block.defaultState();
                    volume.setBlock(x, y, z, blockState);
                });
            });
        });

        final Vector3d center = volume.logicalCenter();

        ArchetypeVolume intermediary = volume;
        for (int i = 0; i < rotationCount; i++) {
            intermediary = intermediary.transform(Transformation.builder()
                .origin(center)
                .rotate(wanted.getRotation().get())
                .build());
        }
        Rotation expected = Rotations.NONE.get();
        for (int i = 0; i < rotationCount; i++) {
            expected = expected.and(wanted.getRotation().get());
        }
        final Transformation expectedTransform = Transformation.builder()
            .origin(center)
            .rotate(expected)
            .build();
        final ArchetypeVolume rotated = intermediary;
        if (rotationCount > 0) {
            final Vector3d preliminaryTransformed = expectedTransform.transformPosition(testForRoundTrip.toDouble());
            Vector3i unTransformed = preliminaryTransformed.round().toInt();
            for (int i = 0; i < rotationCount; i++) {
                unTransformed = ((AbstractReferentArchetypeVolume) rotated).inverseTransform(
                    unTransformed.x(), unTransformed.y(), unTransformed.z());
            }
            Assertions.assertEquals(testForRoundTrip, unTransformed);
        }
        for (int x = 0; x < size.x(); x++) {
            for (int y = 0; y < size.y(); y++) {
                for (int z = 0; z < size.z(); z++) {
                    final int relativeX = x + relativeMin.x();
                    final int relativeY = y + relativeMin.y();
                    final int relativeZ = z + relativeMin.z();
                    final Vector3d rawRelativePosition = new Vector3d(relativeX, relativeY, relativeZ);
                    final BlockState untransformedState = volume.block(relativeX, relativeY, relativeZ);
                    final Vector3i transformedPosition = expectedTransform.transformPosition(rawRelativePosition).toInt();
                    final BlockState transformedState = rotated.block(transformedPosition.x(), transformedPosition.y(), transformedPosition.z());
                    Assertions.assertEquals(untransformedState, transformedState, () -> String.format(
                        "Block Check Failed!\nOriginal(%d, %d, %d): %s\nTransformed(%d, %d, %d): %s\n",
                        relativeX, relativeY, relativeZ, untransformedState,
                        transformedPosition.x(), transformedPosition.y(),
                        transformedPosition.z(), transformedState));
                }
            }
        }
        if (rotationCount < 0) {
            return;
        }
        // At this point, we should have an abstract referent volume at least

        rotated.blockStateStream(rotated.blockMin(), rotated.blockMax(), StreamOptions.lazily())
            .forEach((rotatedRef, type, x, y, z) -> {
                final Vector3d transformedPos = new Vector3d(x, y, z);
                // We have this offset in the stream, so we have to undo it here.
                final Vector3d invertedTransformedPos = expectedTransform.inverse()
                        .transformPosition(transformedPos.add(VolumePositionTranslators.BLOCK_OFFSET))
                        .sub(VolumePositionTranslators.BLOCK_OFFSET);
                final Vector3i invertedBlockPos = invertedTransformedPos.toInt();
                final Vector3i expectedPos;
                Assertions.assertTrue(
                    type instanceof StubState,
                    () -> String.format("expected state to be a stub state for pos: [%f, %f, %f] but got %s", x, y, z,
                        type
                    )
                );
                Assertions.assertNotEquals(((StubState) type).deducedPos,
                    VolumeTransformationTest.INVALID_STUB_POSITION,
                    () -> String.format("expected to have a positioned stub state: [%f, %f, %f] but got %s", x, y, z,
                        type
                    )
                );
                expectedPos = ((StubState) type).deducedPos;
                Assertions.assertEquals(expectedPos, invertedBlockPos,
                    () -> String.format(
                        "expected untransformed position %s for state %s does not match reverse transformed position: %s",
                        expectedPos, type, invertedBlockPos
                    )
                );
                final BlockState block = volume.block(expectedPos.x(), expectedPos.y(), expectedPos.z());

                Assertions.assertEquals(type, block,
                    () -> String.format(
                        "Expected deduced state to be equal from the original target volume but had a mismatch: Original target %s does not match %s",
                        block, type
                    )
                );
            });
    }

}
