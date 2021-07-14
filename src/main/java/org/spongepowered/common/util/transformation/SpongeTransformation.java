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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.math.GenericMath;
import org.spongepowered.math.matrix.Matrix4d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector4d;

public final class SpongeTransformation implements Transformation {

    private final Vector3d origin;
    private final Matrix4d transformation;
    private final Matrix4d directionTransformation;
    private final boolean performRounding;

    public SpongeTransformation(final Vector3d origin, final Matrix4d transformation, final Matrix4d directionTransformation,
            final boolean performRounding) {
        this.origin = origin;
        this.transformation = transformation;
        this.directionTransformation = directionTransformation;
        this.performRounding = performRounding;
    }

    @Override
    public boolean performsRounding() {
        return this.performRounding;
    }

    @Override
    public @NonNull Vector3d transformPosition(final @NonNull Vector3d original) {
        final Vector4d transformed = this.transformation.transform(original.toVector4(1));
        if (this.performRounding) {
            return new Vector3d(
                    GenericMath.round(transformed.x(), 14),
                    GenericMath.round(transformed.y(), 14),
                    GenericMath.round(transformed.z(), 14)
            );
        } else {
            return transformed.toVector3();
        }
    }

    @Override
    public @NonNull Vector3d transformDirection(final @NonNull Vector3d original) {
        final Vector4d transformed = this.directionTransformation.transform(original.normalize().toVector4(1));
        final Vector3d result;
        if (this.performRounding) {
            result = new Vector3d(
                    GenericMath.round(transformed.x(), 14),
                    GenericMath.round(transformed.y(), 14),
                    GenericMath.round(transformed.z(), 14)
            );
        } else {
            result = transformed.toVector3();
        }
        return result.normalize();
    }

    @Override
    public @NonNull Matrix4d positionTransformationMatrix() {
        return this.transformation;
    }

    @Override
    public @NonNull Matrix4d directionTransformationMatrix() {
        return this.directionTransformation;
    }

    @Override
    public @NonNull Vector3d origin() {
        return this.origin;
    }

}
