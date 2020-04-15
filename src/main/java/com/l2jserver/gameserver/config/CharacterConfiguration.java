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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.ClassMasterSetting;
import com.l2jserver.gameserver.config.converter.ClassMasterSettingConverter;
import com.l2jserver.gameserver.config.converter.MapIntegerIntegerConverter;
import com.l2jserver.gameserver.config.converter.PatternConverter;
import com.l2jserver.gameserver.config.converter.Seconds2MillisecondsConverter;

/**
 * Character Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/character.properties",
	"classpath:config/character.properties"
})
@HotReload(value = 5, unit = MINUTES, type = ASYNC)
public interface CharacterConfiguration extends Config, Reloadable {
	
	// Statistics
	@Key("Delevel")
	Boolean delevel();
	
	@Key("DecreaseSkillOnDelevel")
	Boolean decreaseSkillOnDelevel();
	
	@Key("WeightLimit")
	Integer getWeightLimit();
	
	@Key("RunSpeedBoost")
	Integer getRunSpeedBoost();
	
	@Key("DeathPenaltyChance")
	Integer getDeathPenaltyChance();
	
	@Key("RespawnRestoreCP")
	Double getRespawnRestoreCP();
	
	@Key("RespawnRestoreHP")
	Double getRespawnRestoreHP();
	
	@Key("RespawnRestoreMP")
	Double getRespawnRestoreMP();
	
	@Key("HpRegenMultiplier")
	Double getHpRegenMultiplier();
	
	@Key("MpRegenMultiplier")
	Double getMpRegenMultiplier();
	
	@Key("CpRegenMultiplier")
	Double getCpRegenMultiplier();
	
	// Skills & Effects
	
	@Key("ModifySkillDuration")
	Boolean modifySkillDuration();
	
	@Key("SkillDuration")
	@ConverterClass(MapIntegerIntegerConverter.class)
	Map<Integer, Integer> getSkillDuration();
	
	@Key("ModifySkillReuse")
	Boolean modifySkillReuse();
	
	@Key("SkillReuse")
	@ConverterClass(MapIntegerIntegerConverter.class)
	Map<Integer, Integer> getSkillReuse();
	
	@Key("AutoLearnSkills")
	Boolean autoLearnSkills();
	
	@Key("AutoLearnForgottenScrollSkills")
	Boolean autoLearnForgottenScrollSkills();
	
	@Key("AutoLootHerbs")
	Boolean autoLootHerbs();
	
	@Key("MaxBuffAmount")
	Integer getMaxBuffAmount();
	
	@Key("MaxTriggeredBuffAmount")
	Integer getMaxTriggeredBuffAmount();
	
	@Key("MaxDanceAmount")
	Integer getMaxDanceAmount();
	
	@Key("DanceCancelBuff")
	Boolean danceCancelBuff();
	
	@Key("DanceConsumeAdditionalMP")
	Boolean danceConsumeAdditionalMP();
	
	@Key("StoreDances")
	Boolean storeDances();
	
	@Key("AutoLearnDivineInspiration")
	Boolean autoLearnDivineInspiration();
	
	@Key("CancelByHit")
	String cancelByHit();
	
	default Boolean cancelBow() {
		return cancelByHit().equalsIgnoreCase("all") || cancelByHit().equalsIgnoreCase("bow");
	}
	
	default Boolean cancelCast() {
		return cancelByHit().equalsIgnoreCase("all") || cancelByHit().equalsIgnoreCase("cast");
	}
	
	@Key("MagicFailures")
	Boolean magicFailures();
	
	@Key("PlayerFakeDeathUpProtection")
	Integer getPlayerFakeDeathUpProtection();
	
	@Key("StoreSkillCooltime")
	Boolean storeSkillCooltime();
	
	@Key("SubclassStoreSkillCooltime")
	Boolean subclassStoreSkillCooltime();
	
	@Key("ShieldBlocks")
	Boolean shieldBlocks();
	
	@Key("PerfectShieldBlockRate")
	Integer getPerfectShieldBlockRate();
	
	@Key("EffectTickRatio")
	Integer getEffectTickRatio();
	
	// Class, Sub-class and skill learning
	
	@Key("AllowClassMasters")
	Boolean allowClassMasters();
	
	@Key("ConfigClassMaster")
	@ConverterClass(ClassMasterSettingConverter.class)
	ClassMasterSetting getClassMaster();
	
	@Key("AllowEntireTree")
	Boolean allowEntireTree();
	
	@Key("AlternateClassMaster")
	Boolean alternateClassMaster();
	
	@Key("LifeCrystalNeeded")
	Boolean lifeCrystalNeeded();
	
	@Key("EnchantSkillSpBookNeeded")
	Boolean enchantSkillSpBookNeeded();
	
	@Key("DivineInspirationSpBookNeeded")
	Boolean divineInspirationSpBookNeeded();
	
	@Key("SkillLearn")
	Boolean skillLearn();
	
	@Key("SubclassWithoutQuests")
	Boolean subclassWithoutQuests();
	
	@Key("SubclassEverywhere")
	Boolean subclassEverywhere();
	
	@Key("TransformationWithoutQuest")
	Boolean transformationWithoutQuest();
	
	@Key("FeeDeleteTransferSkills")
	Integer getFeeDeleteTransferSkills();
	
	@Key("FeeDeleteSubClassSkills")
	Integer getFeeDeleteSubClassSkills();
	
	// Summons
	
	@Key("SummonStoreSkillCooltime")
	Boolean summonStoreSkillCooltime();
	
	@Key("RestoreServitorOnReconnect")
	Boolean restoreServitorOnReconnect();
	
	@Key("RestorePetOnReconnect")
	Boolean restorePetOnReconnect();
	
	// Limits
	
	@Key("MaxExpBonus")
	Double getMaxExpBonus();
	
	@Key("MaxSpBonus")
	Double getMaxSpBonus();
	
	@Key("MaxRunSpeed")
	Integer getMaxRunSpeed();
	
	@Key("MaxPCritRate")
	Integer getMaxPCritRate();
	
	@Key("MaxMCritRate")
	Integer getMaxMCritRate();
	
	@Key("MaxPAtkSpeed")
	Integer getMaxPAtkSpeed();
	
	@Key("MaxMAtkSpeed")
	Integer getMaxMAtkSpeed();
	
	@Key("MaxEvasion")
	Integer getMaxEvasion();
	
	@Key("MinAbnormalStateSuccessRate")
	Integer getMinAbnormalStateSuccessRate();
	
	@Key("MaxAbnormalStateSuccessRate")
	Integer getMaxAbnormalStateSuccessRate();
	
	@Key("MaxPlayerLevel")
	Integer getMaxPlayerLevel();
	
	@Key("MaxPetLevel")
	Integer getMaxPetLevel();
	
	@Key("MaxSubclass")
	Integer getMaxSubclass();
	
	@Key("BaseSubclassLevel")
	Integer getBaseSubclassLevel();
	
	@Key("MaxSubclassLevel")
	Integer getMaxSubclassLevel();
	
	@Key("MaxPvtStoreSellSlotsDwarf")
	Integer getMaxPvtStoreSellSlotsDwarf();
	
	@Key("MaxPvtStoreSellSlotsOther")
	Integer getMaxPvtStoreSellSlotsOther();
	
	@Key("MaxPvtStoreBuySlotsDwarf")
	Integer getMaxPvtStoreBuySlotsDwarf();
	
	@Key("MaxPvtStoreBuySlotsOther")
	Integer getMaxPvtStoreBuySlotsOther();
	
	@Key("MaximumSlotsForNoDwarf")
	Integer getMaximumSlotsForNoDwarf();
	
	@Key("MaximumSlotsForDwarf")
	Integer getMaximumSlotsForDwarf();
	
	@Key("MaximumSlotsForGMPlayer")
	Integer getMaximumSlotsForGMPlayer();
	
	@Key("MaximumSlotsForQuestItems")
	Integer getMaximumSlotsForQuestItems();
	
	@Key("MaximumWarehouseSlotsForDwarf")
	Integer getMaximumWarehouseSlotsForDwarf();
	
	@Key("MaximumWarehouseSlotsForNoDwarf")
	Integer getMaximumWarehouseSlotsForNoDwarf();
	
	@Key("MaximumWarehouseSlotsForClan")
	Integer getMaximumWarehouseSlotsForClan();
	
	@Key("MaximumFreightSlots")
	Integer getMaximumFreightSlots();
	
	@Key("FreightPrice")
	Integer getFreightPrice();
	
	@Key("NpcTalkBlockingTime")
	Integer getNpcTalkBlockingTime();
	
	// Enchanting
	
	@Key("EnchantChanceElementStone")
	Integer getEnchantChanceElementStone();
	
	@Key("EnchantChanceElementCrystal")
	Integer getEnchantChanceElementCrystal();
	
	@Key("EnchantChanceElementJewel")
	Integer getEnchantChanceElementJewel();
	
	@Key("EnchantChanceElementEnergy")
	Integer getEnchantChanceElementEnergy();
	
	@Key("EnchantBlacklist")
	Set<Integer> getEnchantBlacklist();
	
	// Augmenting
	
	@Key("AugmentationNGSkillChance")
	Integer getAugmentationNGSkillChance();
	
	@Key("AugmentationMidSkillChance")
	Integer getAugmentationMidSkillChance();
	
	@Key("AugmentationHighSkillChance")
	Integer getAugmentationHighSkillChance();
	
	@Key("AugmentationTopSkillChance")
	Integer getAugmentationTopSkillChance();
	
	@Key("AugmentationAccSkillChance")
	Integer getAugmentationAccSkillChance();
	
	@Key("AugmentationBaseStatChance")
	Integer getAugmentationBaseStatChance();
	
	@Key("AugmentationNGGlowChance")
	Integer getAugmentationNGGlowChance();
	
	@Key("AugmentationMidGlowChance")
	Integer getAugmentationMidGlowChance();
	
	@Key("AugmentationHighGlowChance")
	Integer getAugmentationHighGlowChance();
	
	@Key("AugmentationTopGlowChance")
	Integer getAugmentationTopGlowChance();
	
	@Key("RetailLikeAugmentation")
	Boolean retailLikeAugmentation();
	
	@Key("RetailLikeAugmentationNoGradeChance")
	List<Integer> getRetailLikeAugmentationNoGradeChance();
	
	@Key("RetailLikeAugmentationMidGradeChance")
	List<Integer> getRetailLikeAugmentationMidGradeChance();
	
	@Key("RetailLikeAugmentationHighGradeChance")
	List<Integer> getRetailLikeAugmentationHighGradeChance();
	
	@Key("RetailLikeAugmentationTopGradeChance")
	List<Integer> getRetailLikeAugmentationTopGradeChance();
	
	@Key("RetailLikeAugmentationAccessory")
	Boolean retailLikeAugmentationAccessory();
	
	@Key("AugmentationBlacklist")
	Set<Integer> getAugmentationBlacklist();
	
	@Key("AllowAugmentPvPItems")
	Boolean allowAugmentPvPItems();
	
	// Karma
	
	@Key("KarmaPlayerCanBeKilledInPeaceZone")
	Boolean karmaPlayerCanBeKilledInPeaceZone();
	
	@Key("KarmaPlayerCanUseGK")
	Boolean karmaPlayerCanUseGK();
	
	@Key("KarmaPlayerCanTeleport")
	Boolean karmaPlayerCanTeleport();
	
	@Key("KarmaPlayerCanShop")
	Boolean karmaPlayerCanShop();
	
	@Key("KarmaPlayerCanTrade")
	Boolean karmaPlayerCanTrade();
	
	@Key("KarmaPlayerCanUseWareHouse")
	Boolean karmaPlayerCanUseWareHouse();
	
	// Fame
	
	@Key("MaxPersonalFamePoints")
	Integer getMaxPersonalFamePoints();
	
	@Key("FortressZoneFameTaskFrequency")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getFortressZoneFameTaskFrequency();
	
	@Key("FortressZoneFameAquirePoints")
	Integer getFortressZoneFameAquirePoints();
	
	@Key("CastleZoneFameTaskFrequency")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getCastleZoneFameTaskFrequency();
	
	@Key("CastleZoneFameAquirePoints")
	Integer getCastleZoneFameAquirePoints();
	
	@Key("FameForDeadPlayers")
	Boolean fameForDeadPlayers();
	
	// Crafting
	
	@Key("Crafting")
	Boolean crafting();
	
	@Key("CraftMasterwork")
	Boolean craftMasterwork();
	
	@Key("DwarfRecipeLimit")
	Integer getDwarfRecipeLimit();
	
	@Key("CommonRecipeLimit")
	Integer getCommonRecipeLimit();
	
	@Key("AlternativeCrafting")
	Boolean alternativeCrafting();
	
	@Key("CraftingSpeed")
	Double getCraftingSpeed();
	
	@Key("CraftingXpRate")
	Double getCraftingXpRate();
	
	@Key("CraftingSpRate")
	Double getCraftingSpRate();
	
	@Key("CraftingRareXpRate")
	Double getCraftingRareXpRate();
	
	@Key("CraftingRareSpRate")
	Double getCraftingRareSpRate();
	
	@Key("BlacksmithUseRecipes")
	Boolean blacksmithUseRecipes();
	
	@Key("StoreRecipeShopList")
	Boolean storeRecipeShopList();
	
	// Clan
	
	@Key("ClanLeaderDateChange")
	Integer getClanLeaderDateChange();
	
	@Key("ClanLeaderHourChange")
	String getClanLeaderHourChange();
	
	@Key("ClanLeaderInstantActivation")
	Boolean clanLeaderInstantActivation();
	
	@Key("DaysBeforeJoinAClan")
	Integer getDaysBeforeJoinAClan();
	
	@Key("DaysBeforeCreateAClan")
	Integer getDaysBeforeCreateAClan();
	
	@Key("DaysToPassToDissolveAClan")
	Integer getDaysToPassToDissolveAClan();
	
	@Key("DaysBeforeJoiningAllianceAfterLeaving")
	Integer getDaysBeforeJoiningAllianceAfterLeaving();
	
	@Key("DaysBeforeJoinAllyWhenDismissed")
	Integer getDaysBeforeJoinAllyWhenDismissed();
	
	@Key("DaysBeforeAcceptNewClanWhenDismissed")
	Integer getDaysBeforeAcceptNewClanWhenDismissed();
	
	@Key("DaysBeforeCreateNewAllyWhenDissolved")
	Integer getDaysBeforeCreateNewAllyWhenDissolved();
	
	@Key("MaxNumOfClansInAlly")
	Integer getMaxNumOfClansInAlly();
	
	@Key("MembersCanWithdrawFromClanWH")
	Boolean membersCanWithdrawFromClanWH();
	
	@Key("RemoveCastleCirclets")
	Boolean removeCastleCirclets();
	
	@Key("ClanMembersForWar")
	Integer getClanMembersForWar();
	
	// Party
	
	@Key("PartyRange")
	Integer getPartyRange();
	
	@Key("PartyRange2")
	Integer getPartyRange2();
	
	@Key("LeavePartyLeader")
	Boolean leavePartyLeader();
	
	// Initial
	
	@Key("InitialEquipmentEvent")
	Boolean initialEquipmentEvent();
	
	@Key("StartingAdena")
	Integer getStartingAdena();
	
	@Key("StartingLevel")
	Integer getStartingLevel();
	
	@Key("StartingSP")
	Integer getStartingSP();
	
	// Other
	
	@Key("MaxAdena")
	Long getMaxAdena();
	
	@Key("AutoLoot")
	Boolean autoLoot();
	
	@Key("AutoLootRaids")
	Boolean autoLootRaids();
	
	@Key("RaidLootRightsInterval")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getRaidLootRightsInterval();
	
	@Key("RaidLootRightsCCSize")
	Integer getRaidLootRightsCCSize();
	
	@Key("UnstuckInterval")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getUnstuckInterval();
	
	@Key("TeleportWatchdogTimeout")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getTeleportWatchdogTimeout();
	
	@Key("PlayerSpawnProtection")
	Integer getPlayerSpawnProtection();
	
	@Key("PlayerSpawnProtectionAllowedItems")
	Set<Integer> getPlayerSpawnProtectionAllowedItems();
	
	@Key("PlayerTeleportProtection")
	Integer getPlayerTeleportProtection();
	
	@Key("RandomRespawnInTown")
	Boolean randomRespawnInTown();
	
	@Key("OffsetOnTeleport")
	Boolean offsetOnTeleport();
	
	@Key("MaxOffsetOnTeleport")
	Integer getMaxOffsetOnTeleport();
	
	@Key("PetitioningAllowed")
	Boolean petitioningAllowed();
	
	@Key("MaxPetitionsPerPlayer")
	Integer getMaxPetitionsPerPlayer();
	
	@Key("MaxPetitionsPending")
	Integer getMaxPetitionsPending();
	
	@Key("FreeTeleporting")
	Boolean freeTeleporting();
	
	@Key("DeleteCharAfterDays")
	Integer getDeleteCharAfterDays();
	
	@Key("ExponentXp")
	Integer getExponentXp();
	
	@Key("ExponentSp")
	Integer getExponentSp();
	
	@Key("PartyXpCutoffMethod")
	String getPartyXpCutoffMethod();
	
	@Key("PartyXpCutoffPercent")
	Double getPartyXpCutoffPercent();
	
	@Key("PartyXpCutoffLevel")
	Integer getPartyXpCutoffLevel();
	
	@Key("PartyXpCutoffGaps")
	@ConverterClass(MapIntegerIntegerConverter.class)
	Map<Integer, Integer> getPartyXpCutoffGaps();
	
	@Key("PartyXpCutoffGapPercent")
	List<Integer> getPartyXpCutoffGapPercent();
	
	@Key("Tutorial")
	Boolean tutorial();
	
	@Key("ExpertisePenalty")
	Boolean expertisePenalty();
	
	@Key("StoreUISettings")
	Boolean storeUISettings();
	
	@Key("SilenceModeExclude")
	Boolean silenceModeExclude();
	
	@Key("ValidateTriggerSkills")
	Boolean validateTriggerSkills();
	
	@Key("PlayerNameTemplate")
	@ConverterClass(PatternConverter.class)
	Pattern getPlayerNameTemplate();
	
	@Key("PetNameTemplate")
	@ConverterClass(PatternConverter.class)
	Pattern getPetNameTemplate();
	
	@Key("ForbiddenNames")
	Set<String> getForbiddenNames();
	
	@Key("CharMaxNumber")
	Integer getCharMaxNumber();
}