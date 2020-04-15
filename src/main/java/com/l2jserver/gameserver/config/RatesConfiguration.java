/*
 * Copyright © 2004-2020 L2J Server
 *
 * This file is part of L2J Server.
 *
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;

import java.util.Map;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.MapIntegerFloatConverter;

/**
 * Rates Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/rates.properties",
	"classpath:config/rates.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface RatesConfiguration extends Config, Reloadable {
	
	@Key("DeathDropAmountMultiplier")
	Double getDeathDropAmountMultiplier();
	
	@Key("CorpseDropAmountMultiplier")
	Double getCorpseDropAmountMultiplier();
	
	@Key("HerbDropAmountMultiplier")
	Double getHerbDropAmountMultiplier();
	
	@Key("RaidDropAmountMultiplier")
	Double getRaidDropAmountMultiplier();
	
	@Key("DeathDropChanceMultiplier")
	Double getDeathDropChanceMultiplier();
	
	@Key("CorpseDropChanceMultiplier")
	Double getCorpseDropChanceMultiplier();
	
	@Key("HerbDropChanceMultiplier")
	Double getHerbDropChanceMultiplier();
	
	@Key("RaidDropChanceMultiplier")
	Double getRaidDropChanceMultiplier();
	
	@Key("DropAmountMultiplierByItemId")
	@ConverterClass(MapIntegerFloatConverter.class)
	Map<Integer, Float> getDropAmountMultiplierByItemId();
	
	@Key("DropChanceMultiplierByItemId")
	@ConverterClass(MapIntegerFloatConverter.class)
	Map<Integer, Float> getDropChanceMultiplierByItemId();
	
	@Key("RateXp")
	Float getRateXp();
	
	@Key("RateSp")
	Float getRateSp();
	
	@Key("RatePartyXp")
	Float getRatePartyXp();
	
	@Key("RatePartySp")
	Float getRatePartySp();
	
	@Key("RateDropManor")
	Integer getRateDropManor();
	
	@Key("RateKarmaLost")
	Double getRateKarmaLost();
	
	@Key("RateKarmaExpLost")
	Double getRateKarmaExpLost();
	
	@Key("RateSiegeGuardsPrice")
	Double getRateSiegeGuardsPrice();
	
	@Key("RateExtractable")
	Float getRateExtractable();
	
	@Key("RateHellboundTrustIncrease")
	Float getRateHellboundTrustIncrease();
	
	@Key("RateHellboundTrustDecrease")
	Float getRateHellboundTrustDecrease();
	
	@Key("RateQuestDrop")
	Float getRateQuestDrop();
	
	@Key("RateQuestRewardXP")
	Float getRateQuestRewardXP();
	
	@Key("RateQuestRewardSP")
	Float getRateQuestRewardSP();
	
	@Key("RateQuestRewardAdena")
	Float getRateQuestRewardAdena();
	
	@Key("UseQuestRewardMultipliers")
	Boolean useQuestRewardMultipliers();
	
	@Key("RateQuestReward")
	Float getRateQuestReward();
	
	@Key("RateQuestRewardPotion")
	Float getRateQuestRewardPotion();
	
	@Key("RateQuestRewardScroll")
	Float getRateQuestRewardScroll();
	
	@Key("RateQuestRewardRecipe")
	Float getRateQuestRewardRecipe();
	
	@Key("RateQuestRewardMaterial")
	Float getRateQuestRewardMaterial();
	
	@Key("PlayerDropLimit")
	Integer getPlayerDropLimit();
	
	@Key("PlayerRateDrop")
	Integer getPlayerRateDrop();
	
	@Key("PlayerRateDropItem")
	Integer getPlayerRateDropItem();
	
	@Key("PlayerRateDropEquip")
	Integer getPlayerRateDropEquip();
	
	@Key("PlayerRateDropEquipWeapon")
	Integer getPlayerRateDropEquipWeapon();
	
	@Key("KarmaDropLimit")
	Integer getKarmaDropLimit();
	
	@Key("KarmaRateDrop")
	Integer getKarmaRateDrop();
	
	@Key("KarmaRateDropItem")
	Integer getKarmaRateDropItem();
	
	@Key("KarmaRateDropEquip")
	Integer getKarmaRateDropEquip();
	
	@Key("KarmaRateDropEquipWeapon")
	Integer getKarmaRateDropEquipWeapon();
	
	@Key("PetXpRate")
	Double getPetXpRate();
	
	@Key("PetFoodRate")
	Integer getPetFoodRate();
	
	@Key("SinEaterXpRate")
	Double getSinEaterXpRate();
}