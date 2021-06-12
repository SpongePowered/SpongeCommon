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
package org.spongepowered.common.tag;

import org.spongepowered.api.registry.DefaultedRegistryType;
import org.spongepowered.api.tag.Tag;
import org.spongepowered.api.tag.TagType;
import org.spongepowered.api.tag.Taggable;

public class SpongeTagType<T extends Taggable> implements TagType<T> {

    private final String id;
    private final DefaultedRegistryType<T> taggableRegistry;
    private final DefaultedRegistryType<Tag<T>> tagRegistry;

    public SpongeTagType(String id, DefaultedRegistryType<T> taggableRegistry, DefaultedRegistryType<Tag<T>> tagRegistry) {
        this.id = id;
        this.taggableRegistry = taggableRegistry;
        this.tagRegistry = tagRegistry;
    }

    @Override
    public String id() {
        return this.id;
    }

    @Override
    public DefaultedRegistryType<T> taggableRegistry() {
        return this.taggableRegistry;
    }

    @Override
    public DefaultedRegistryType<Tag<T>> tagRegistry() {
        return this.tagRegistry;
    }
}
