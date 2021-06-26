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
package org.spongepowered.common.applaunch.config.core;

import org.spongepowered.common.applaunch.AppLaunch;
import org.spongepowered.common.applaunch.plugin.PluginPlatform;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holder for a string value that is parsed for environment variables.
 */
public final class TokenHoldingString {

    static final ScalarSerializer<TokenHoldingString> SERIALIZER = new Serializer();

    private static final Pattern TOKEN_MATCH = Pattern.compile("\\$\\{([^}]+)}");

    private static final Map<String, Function<PluginPlatform, String>> TOKENS = new HashMap<>();

    static {
        TokenHoldingString.register("CANONICAL_GAME_DIR", platform -> platform.baseDirectory().toString());
        TokenHoldingString.register("CANONICAL_MODS_DIR", platform -> {
            // TODO: this is wrong, need to decide what configurability will be provided.
            return platform.pluginDirectories().get(0).toString();
        });
    }

    /**
     * Create and parse a string.
     *
     * @param input Input value maybe with tokens
     * @return holder
     */
    public static TokenHoldingString of(final String input) {
        return new TokenHoldingString(input, TokenHoldingString.parsePlaceholders(input));
    }

    private static void register(final String token, final Function<PluginPlatform, String> getter) {
        TokenHoldingString.TOKENS.put(token.toLowerCase(Locale.ROOT), getter);
    }

    /**
     * Parse an environment variable-style placeholder syntax.
     *
     * <p>Variable names are case-insensitive, and detected with the
     * {@link #TOKEN_MATCH} expression.</p>
     *
     * @param input input string
     * @return string with placeholders replaced
     */
    private static String parsePlaceholders(final String input) {
        final Matcher matcher = TokenHoldingString.TOKEN_MATCH.matcher(input);
        if (!matcher.find()) {
            return input;
        }
        final StringBuffer result = new StringBuffer();
        do {
            final String token = matcher.group(1);
            final Function<PluginPlatform, String> replacer = TokenHoldingString.TOKENS.get(token.toLowerCase());
            final String replaced = replacer == null ? "" : replacer.apply(AppLaunch.pluginPlatform());
            matcher.appendReplacement(result, replaced == null ? "" : replaced);
        } while (matcher.find());
        matcher.appendTail(result);
        return result.toString();
    }

    private final String plainValue;
    private final String parsedValue;

    private TokenHoldingString(final String plain, final String parsed) {
        this.plainValue = plain;
        this.parsedValue = parsed;
    }

    public String getPlain() {
        return this.plainValue;
    }

    public String getParsed() {
        return this.parsedValue;
    }

    static final class Serializer extends ScalarSerializer<TokenHoldingString> {

        Serializer() {
            super(TokenHoldingString.class);
        }

        @Override
        public TokenHoldingString deserialize(final Type type, final Object obj) {
            return TokenHoldingString.of(obj.toString());
        }

        @Override
        public Object serialize(final TokenHoldingString item, final Predicate<Class<?>> typeSupported) {
            return item.getPlain();
        }

    }

}
