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
import org.spongepowered.api.util.Axis;
import org.spongepowered.api.util.rotation.Rotation;
import org.spongepowered.api.util.transformation.Transformation;
import org.spongepowered.math.imaginary.Quaterniond;
import org.spongepowered.math.matrix.Matrix4d;
import org.spongepowered.math.vector.Vector3d;
import org.spongepowered.math.vector.Vector4d;

public final class SpongeTransformationBuilder implements Transformation.Builder {

    // x, y, z, w
    private Vector3d origin;
    private Matrix4d transformation;
    private Matrix4d directionTransformation;
    private boolean performRounding;

    public SpongeTransformationBuilder() {
        this.reset();
    }

    @Override
    public @NonNull SpongeTransformationBuilder reset() {
        this.transformation = Matrix4d.IDENTITY;
        this.directionTransformation = Matrix4d.IDENTITY;
        this.origin = Vector3d.ZERO;
        this.performRounding = true;
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder origin(final @NonNull Vector3d origin) {
        this.origin = origin;
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder rotate(final @NonNull Rotation rotation, final @NonNull Axis axis) {
        // We're doing nothing here.
        if (rotation.angle().degrees() == 0) {
            return this;
        }

        final Quaterniond rotationQuaternion = Quaterniond.fromAngleDegAxis(rotation.angle().degrees(), axis.toVector3d());
        this.transformation = this.transformation.rotate(rotationQuaternion);
        this.directionTransformation = this.directionTransformation.rotate(rotationQuaternion);
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder mirror(final @NonNull Axis axis) {
        final Vector4d scale = Vector4d.ONE.sub(axis.toVector3d().toVector4(0).mul(2));
        this.transformation = this.transformation.scale(scale);
        this.directionTransformation = this.directionTransformation.scale(scale);
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder translate(final @NonNull Vector3d translate) {
        this.transformation = this.transformation.translate(translate);
        return this;
    }

    @Override
    public @NonNull SpongeTransformationBuilder performRounding(final boolean round) {
        this.performRounding = round;
        return this;
    }

    @Override
    public @NonNull SpongeTransformation build() {
        return new SpongeTransformation(this.origin,
                this.transformation.mul(Matrix4d.createTranslation(this.origin.mul(-1))).translate(this.origin),
                this.directionTransformation,
                this.performRounding);
    }

}
