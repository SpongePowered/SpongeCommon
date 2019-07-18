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
package org.spongepowered.common.bridge.world.chunk.storage;

import net.minecraft.world.chunk.storage.RegionFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;

@Mixin(RegionFile.class)
public interface RegionFileAccessor {

    @Accessor("fileName") File accessor$getFileName();

    @Accessor("fileName") void accessor$setFileName(File fileName);

    @Accessor("dataFile") RandomAccessFile accessor$getDataFile();

    @Accessor("dataFile") void accessor$setDataFile(RandomAccessFile dataFile);

    @Accessor("offsets") int[] accessor$getOffsets();

    @Accessor("offsets") void accessor$setOffsets(int[] offsets);

    @Accessor("chunkTimestamps") int[] accessor$getChunkTimestamps();

    @Accessor("chunkTimestamps") void accessor$setChunkTimestamps(int[] chunkTimestamps);

    @Accessor("sectorFree") List<Boolean> accessor$getSectorFree();

    @Accessor("sectorFree") void accessor$setSectorFree(List<Boolean> sectorFree);

    @Accessor("sizeDelta") int accessor$getSizeDelta();

    @Accessor("sizeDelta") void accessor$setSizeDelta(int sizeDelta);

    @Accessor("lastModified") long accessor$getLastModified();

    @Accessor("lastModified") void accessor$setLastModified(long lastModified);

}
