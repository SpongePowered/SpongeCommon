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
package org.spongepowered.vanilla.generator;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

public class TagGenerator implements Generator {

    private static final RegistryScope SCOPE = RegistryScope.GAME;

    private final String registryName;
    private final String registryTypeName;
    private final Supplier<List<? extends Tag.Named<?>>> tagSupplier;
    private final TypeName typeName;
    private final String relativePackageName;
    private final String targetClassSimpleName;

    public TagGenerator(final String registryName,
                        final String registryTypeName,
                        final Supplier<List<? extends Tag.Named<?>>> tagSupplier,
                        final TypeName typeName,
                        final String relativePackageName,
                        final String targetClassSimpleName) {
        this.registryName = registryName;
        this.registryTypeName = registryTypeName;
        this.tagSupplier = tagSupplier;
        this.typeName = typeName;
        this.relativePackageName = relativePackageName;
        this.targetClassSimpleName = targetClassSimpleName;
    }

    @Override
    public String name() {
        return "elements of tag registry " + registryName;
    }

    @Override
    public void generate(final Context ctx) throws IOException {
        final var clazz = Types.utilityClass(
                this.targetClassSimpleName,
                "<!-- This file is automatically generated. Any manual changes will be overwritten. -->"
        );
        clazz.addAnnotation(Types.suppressWarnings("unused"));
        clazz.addAnnotation(TagGenerator.SCOPE.registryScopeAnnotation());


        final var valueType = ParameterizedTypeName.get(Types.TAG, typeName);
        final var fieldType = ParameterizedTypeName.get(Types.DEFAULTED_REGISTRY_REFERENCE, valueType);
        final var factoryMethod = TagGenerator.SCOPE.registryReferenceFactory(this.registryTypeName, valueType);

        this.tagSupplier.get().stream()
                .map(Tag.Named::getName)
                .sorted(Comparator.naturalOrder())
                .map(v -> this.makeField(this.targetClassSimpleName, fieldType, factoryMethod, v))
                .forEachOrdered(clazz::addField);

        clazz.addMethod(factoryMethod);

        ctx.write(this.relativePackageName, clazz.build());

        // Then fix up before/after comments
        final var cu = ctx.compilationUnit(this.relativePackageName, this.targetClassSimpleName);
        final TypeDeclaration<?> type = cu.getPrimaryType().get();

        final var fields = type.getFields();
        if (!fields.isEmpty()) {
            fields.get(0).setLineComment("@formatter:off");
        }

        final var constructors = type.getConstructors();
        if (!constructors.isEmpty()) {
            constructors.get(0).setLineComment("@formatter:on");
        }
    }

    private FieldSpec makeField(final String ownType, final TypeName fieldType, final MethodSpec factoryMethod, final ResourceLocation element) {
        return FieldSpec.builder(fieldType, Types.keyToFieldName(element.getPath()), Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.$N($L)", ownType, factoryMethod, Types.resourceKey(element))
                .build();
    }
}
