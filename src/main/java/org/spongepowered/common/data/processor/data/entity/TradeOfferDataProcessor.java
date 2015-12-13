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
package org.spongepowered.common.data.processor.data.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import org.spongepowered.api.data.DataTransactionResult;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.manipulator.immutable.entity.ImmutableTradeOfferData;
import org.spongepowered.api.data.manipulator.mutable.entity.TradeOfferData;
import org.spongepowered.api.data.value.ValueContainer;
import org.spongepowered.api.data.value.immutable.ImmutableValue;
import org.spongepowered.api.data.value.mutable.ListValue;
import org.spongepowered.api.item.merchant.TradeOffer;
import org.spongepowered.common.data.manipulator.mutable.SpongeTradeOfferData;
import org.spongepowered.common.data.processor.dual.common.AbstractSingleTargetDualProcessor;
import org.spongepowered.common.data.value.SpongeValueFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TradeOfferDataProcessor
        extends AbstractSingleTargetDualProcessor<EntityVillager, List<TradeOffer>, ListValue<TradeOffer>, TradeOfferData, ImmutableTradeOfferData> {

    public TradeOfferDataProcessor() {
        super(EntityVillager.class, Keys.TRADE_OFFERS);
    }

    @Override
    protected boolean set(EntityVillager entity, List<TradeOffer> value) {
        MerchantRecipeList list = new MerchantRecipeList();
        list.addAll(value.stream().map(tradeOffer -> (MerchantRecipe) tradeOffer).collect(Collectors.toList()));
        entity.buyingList = list;
        return true;
    }

    @Override
    protected Optional<List<TradeOffer>> getVal(EntityVillager entity) {
        List<TradeOffer> offers = Lists.newArrayList();
        if (entity.buyingList == null) {
            entity.populateBuyingList();
        }
        for (int i = 0; i < entity.buyingList.size(); i++) {
            offers.add((TradeOffer) entity.buyingList.get(i));
        }
        return Optional.of(offers);
    }

    @Override
    protected ImmutableValue<List<TradeOffer>> constructImmutableValue(List<TradeOffer> value) {
        return constructValue(value).asImmutable();
    }

    @Override
    protected TradeOfferData createManipulator() {
        return new SpongeTradeOfferData();
    }

    @Override
    protected ListValue<TradeOffer> constructValue(List<TradeOffer> actualValue) {
        return SpongeValueFactory.getInstance().createListValue(Keys.TRADE_OFFERS, actualValue);
    }

    @Override
    public DataTransactionResult removeFrom(ValueContainer<?> container) {
        return DataTransactionResult.failNoData();
    }
}
