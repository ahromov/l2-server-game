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

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.MapIntegerIntegerConverter;

/**
 * NPC Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/npc.properties",
	"classpath:config/npc.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface NPCConfiguration extends Config, Reloadable {
	
	@Key("AnnounceMammonSpawn")
	Boolean announceMammonSpawn();
	
	@Key("MobAggroInPeaceZone")
	Boolean mobAggroInPeaceZone();
	
	@Key("AttackableNpcs")
	Boolean attackableNpcs();
	
	@Key("ViewNpc")
	Boolean viewNpc();
	
	@Key("MaxDriftRange")
	Integer getMaxDriftRange();
	
	@Key("ShowNpcLevel")
	Boolean showNpcLevel();
	
	@Key("ShowCrestWithoutQuest")
	Boolean showCrestWithoutQuest();
	
	@Key("RandomEnchantEffect")
	Boolean randomEnchantEffect();
	
	@Key("MinNPCLevelForDmgPenalty")
	Integer getMinNPCLevelForDmgPenalty();
	
	@Key("DmgPenaltyForLvLDifferences")
	List<Double> getDmgPenaltyForLvLDifferences();
	
	@Key("CritDmgPenaltyForLvLDifferences")
	List<Double> getCritDmgPenaltyForLvLDifferences();
	
	@Key("SkillDmgPenaltyForLvLDifferences")
	List<Double> getSkillDmgPenaltyForLvLDifferences();
	
	@Key("MinNPCLevelForMagicPenalty")
	Integer getMinNPCLevelForMagicPenalty();
	
	@Key("SkillChancePenaltyForLvLDifferences")
	List<Double> getSkillChancePenaltyForLvLDifferences();
	
	// Monsters
	
	@Key("DecayTimeTask")
	Integer getDecayTimeTask();
	
	@Key("DefaultCorpseTime")
	Integer getDefaultCorpseTime();
	
	@Key("SpoiledCorpseExtendTime")
	Integer getSpoiledCorpseExtendTime();
	
	@Key("CorpseConsumeSkillAllowedTimeBeforeDecay")
	Integer getCorpseConsumeSkillAllowedTimeBeforeDecay();
	
	// Guards
	
	@Key("GuardAttackAggroMob")
	Boolean guardAttackAggroMob();
	
	// Pets
	
	@Key("AllowWyvernUpgrader")
	Integer allowWyvernUpgrader();
	
	@Key("PetRentNPCs")
	Set<Integer> getPetRentNPCs();
	
	@Key("MaximumSlotsForPet")
	Integer getMaximumSlotsForPet();
	
	@Key("PetHpRegenMultiplier")
	Double getPetHpRegenMultiplier();
	
	@Key("PetMpRegenMultiplier")
	Double getPetMpRegenMultiplier();
	
	// Raid Bosses
	
	@Key("RaidHpRegenMultiplier")
	Double getRaidHpRegenMultiplier();
	
	@Key("RaidMpRegenMultiplier")
	Double getRaidMpRegenMultiplier();
	
	@Key("RaidPDefenceMultiplier")
	Double getRaidPDefenceMultiplier();
	
	@Key("RaidMDefenceMultiplier")
	Double getRaidMDefenceMultiplier();
	
	@Key("RaidPAttackMultiplier")
	Double getRaidPAttackMultiplier();
	
	@Key("RaidMAttackMultiplier")
	Double getRaidMAttackMultiplier();
	
	@Key("RaidMinRespawnMultiplier")
	Double getRaidMinRespawnMultiplier();
	
	@Key("RaidMaxRespawnMultiplier")
	Double getRaidMaxRespawnMultiplier();
	
	@Key("RaidMinionRespawnTime")
	Long getRaidMinionRespawnTime();
	
	@Key("CustomMinionsRespawnTime")
	@ConverterClass(MapIntegerIntegerConverter.class)
	Map<Integer, Integer> getCustomMinionsRespawnTime();
	
	@Key("RaidCurse")
	Boolean raidCurse();
	
	@Key("RaidChaosTime")
	Integer getRaidChaosTime();
	
	@Key("GrandChaosTime")
	Integer getGrandChaosTime();
	
	@Key("MinionChaosTime")
	Integer getMinionChaosTime();
	
	// Drops
	
	@Key("UseDeepBlueDropRules")
	Boolean useDeepBlueDropRules();
	
	@Key("UseDeepBlueDropRulesRaid")
	Boolean useDeepBlueDropRulesRaid();
	
	@Key("DropAdenaMinLevelDifference")
	Integer getDropAdenaMinLevelDifference();
	
	@Key("DropAdenaMaxLevelDifference")
	Integer getDropAdenaMaxLevelDifference();
	
	@Key("DropAdenaMinLevelGapChance")
	Integer getDropAdenaMinLevelGapChance();
	
	@Key("DropItemMinLevelDifference")
	Integer getDropItemMinLevelDifference();
	
	@Key("DropItemMaxLevelDifference")
	Integer getDropItemMaxLevelDifference();
	
	@Key("DropItemMinLevelGapChance")
	Integer getDropItemMinLevelGapChance();
	
	@Key("MaxAggroRange")
	Integer getMaxAggroRange();
}