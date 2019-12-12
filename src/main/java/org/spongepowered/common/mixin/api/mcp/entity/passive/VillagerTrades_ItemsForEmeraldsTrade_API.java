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
package org.spongepowered.common.mixin.api.mcp.entity.passive;

import static com.google.common.base.Preconditions.checkNotNull;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MerchantOffer;
import org.spongepowered.api.item.merchant.Merchant;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.api.item.merchant.TradeOfferGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Random;

// Note that these mixins will not have to exist once mixing into interfaces is
// added as the only thing needing to be done is a simple default implementation
// with an empty MerchantRecipeList and diff the list with an empty one and
// provide the resulting diff'ed MerchantRecipe (TradeOffer) as the result.
//
// i509VCB: Yes the classes which implement ITrade, like ItemsForEmeraldsAndItemsTrade are package private
// For clarification this trade is Emeralds -> Item(s) such as a generic Emeralds -> Bread trade.\
// TODO: These need a new home
@Mixin(targets = "net/minecraft/entity/merchant/villager/VillagerTrades$ItemsForEmeraldsTrade")
public class VillagerTrades_ItemsForEmeraldsTrade_API implements TradeOfferGenerator {
    @Shadow public ItemStack field_221208_a; // Item being sold to the customer
    @Shadow public int emeraldCount; // Amount of emeralds required to be bought.
    @Shadow public int field_221210_c; // Amount of item sold
    @Shadow public int field_221211_d; // Max uses
    @Shadow public int field_221212_e; // Exp rewarded from trade
    @Shadow public float field_221213_f; // Price multiplier

    @Override
    public TradeOffer apply(Random random) {
        checkNotNull(random, "Random cannot be null!");
        ItemStack emeraldItemStack;
        ItemStack sellingItem;

        if (emeraldCount < 0) {
            emeraldItemStack = new ItemStack(Items.EMERALD);
            sellingItem = new ItemStack(this.field_221208_a.getItem(), -field_221210_c);
            sellingItem.setTag(this.field_221208_a.getTag().copy());
        } else {
            emeraldItemStack = new ItemStack(Items.EMERALD, emeraldCount);
            sellingItem = new ItemStack(this.field_221208_a.getItem(), 1);
            sellingItem.setTag(this.field_221208_a.getTag().copy());
        }

        return (TradeOffer) new MerchantOffer(emeraldItemStack, sellingItem, field_221211_d, field_221212_e, field_221213_f);
    }


    @Override
    public void accept(Merchant owner, List<TradeOffer> tradeOffers, Random random) {
        tradeOffers.add(this.apply(random));
    }
}