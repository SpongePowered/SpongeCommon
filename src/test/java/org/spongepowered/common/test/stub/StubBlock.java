package org.spongepowered.common.test.stub;

import com.google.common.collect.ImmutableList;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.block.BlockSoundGroup;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.state.StateProperty;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.common.data.holder.SpongeImmutableDataHolder;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Supplier;

public class StubBlock implements SpongeImmutableDataHolder<BlockType>, BlockType {

    private static final Vector3i INVALID_STUB_POSITION = Vector3i.from(
        Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    private final BlockState air;
    public final Vector3i deducedPos;
    public StubBlock(final ResourceKey key) {
        final Vector3i deducedPos;
        final String value = key.value();
        if (value.startsWith("volumetest")) {
            final String replace = value.replace("volumetest{", "")
                .replace("}", "");
            final String[] split = replace.split(",");
            if (split.length == 3) {
                final int x = Integer.parseInt(split[0].replace(" ", ""));
                final int y = Integer.parseInt(split[1].replace(" ", ""));
                final int z = Integer.parseInt(split[2].replace(" ", ""));
                deducedPos = new Vector3i(x, y, z);
            } else {
                deducedPos = StubBlock.INVALID_STUB_POSITION;
            }
        } else {
            deducedPos = StubBlock.INVALID_STUB_POSITION;
        }
        this.air = new StubState(this, key, deducedPos);
        this.deducedPos = deducedPos;
    }

    @Override
    public Optional<ItemType> item() {
        return Optional.empty();
    }

    @Override
    public boolean doesUpdateRandomly() {
        return false;
    }

    @Override
    public void setUpdateRandomly(final boolean updateRandomly) {

    }

    @Override
    public BlockSoundGroup soundGroup() {
        return null;
    }

    @Override
    public boolean isAnyOf(final Supplier<? extends BlockType>... types) {
        return false;
    }

    @Override
    public boolean isAnyOf(final BlockType... types) {
        return false;
    }

    @Override
    public @NotNull Component asComponent() {
        return null;
    }

    @Override
    public ImmutableList<BlockState> validStates() {
        return ImmutableList.of(this.air);
    }

    @Override
    public BlockState defaultState() {
        return this.air;
    }

    @Override
    public Collection<StateProperty<?>> stateProperties() {
        return Collections.emptyList();
    }

    @Override
    public Optional<StateProperty<?>> findStateProperty(final String name) {
        return Optional.empty();
    }

    @Override
    public TagType<BlockType> tagType() {
        return null;
    }

    @Override
    public Collection<Tag<BlockType>> tags() {
        return Collections.emptyList();
    }
}
