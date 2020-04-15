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

import java.util.Set;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.Minutes2MillisecondsConverter;
import com.l2jserver.gameserver.config.converter.Seconds2MillisecondsConverter;
import com.l2jserver.gameserver.config.converter.ServerListTypeConverter;
import com.l2jserver.gameserver.enums.IllegalActionPunishmentType;

/**
 * General Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/general.properties",
	"classpath:config/general.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface GeneralConfiguration extends Config, Mutable, Reloadable {
	
	@Key("EverybodyHasAdminRights")
	Boolean everybodyHasAdminRights();
	
	@Key("ServerListBrackets")
	Boolean getServerListBrackets();
	
	@Key("ServerListType")
	@ConverterClass(ServerListTypeConverter.class)
	Integer getServerListType();
	
	@Key("ServerListAge")
	Integer getServerListAge();
	
	@Key("ServerGMOnly")
	Boolean serverGMOnly();
	
	@Key("GMHeroAura")
	Boolean gmHeroAura();
	
	@Key("GMStartupInvulnerable")
	Boolean gmStartupInvulnerable();
	
	@Key("GMStartupInvisible")
	Boolean gmStartupInvisible();
	
	@Key("GMStartupSilence")
	Boolean gmStartupSilence();
	
	@Key("GMStartupAutoList")
	Boolean gmStartupAutoList();
	
	@Key("GMStartupDietMode")
	Boolean gmStartupDietMode();
	
	@Key("GMItemRestriction")
	Boolean gmItemRestriction();
	
	@Key("GMSkillRestriction")
	Boolean gmSkillRestriction();
	
	@Key("GMTradeRestrictedItems")
	Boolean gmTradeRestrictedItems();
	
	@Key("GMRestartFighting")
	Boolean gmRestartFighting();
	
	@Key("GMShowAnnouncerName")
	Boolean gmShowAnnouncerName();
	
	@Key("GMShowCritAnnouncerName")
	Boolean gmShowCritAnnouncerName();
	
	@Key("GMGiveSpecialSkills")
	Boolean gmGiveSpecialSkills();
	
	@Key("GMGiveSpecialAuraSkills")
	Boolean gmGiveSpecialAuraSkills();
	
	@Key("GameGuardEnforce")
	Boolean gameGuardEnforce();
	
	@Key("GameGuardProhibitAction")
	Boolean gameGuardProhibitAction();
	
	@Key("LogChat")
	Boolean logChat();
	
	@Key("LogAutoAnnouncements")
	Boolean logAutoAnnouncements();
	
	@Key("LogItems")
	Boolean logItems();
	
	@Key("LogItemsSmallLog")
	Boolean logItemsSmallLog();
	
	@Key("LogItemEnchants")
	Boolean logItemEnchants();
	
	@Key("LogSkillEnchants")
	Boolean logSkillEnchants();
	
	@Key("GMAudit")
	Boolean gmAudit();
	
	@Key("SkillCheckEnable")
	Boolean skillCheckEnable();
	
	@Key("SkillCheckRemove")
	Boolean skillCheckRemove();
	
	@Key("SkillCheckGM")
	Boolean skillCheckGM();
	
	@Key("ThreadPoolSizeEffects")
	Integer getThreadPoolSizeEffects();
	
	@Key("ThreadPoolSizeGeneral")
	Integer getThreadPoolSizeGeneral();
	
	@Key("ThreadPoolSizeEvents")
	Integer getThreadPoolSizeEvents();
	
	@Key("UrgentPacketThreadCoreSize")
	Integer getUrgentPacketThreadCoreSize();
	
	@Key("GeneralPacketThreadCoreSize")
	Integer getGeneralPacketThreadCoreSize();
	
	@Key("GeneralThreadCoreSize")
	Integer getGeneralThreadCoreSize();
	
	@Key("AiMaxThread")
	Integer getAiMaxThread();
	
	@Key("EventsMaxThread")
	Integer getEventsMaxThread();
	
	@Key("DeadLockDetector")
	Boolean deadLockDetector();
	
	@Key("DeadLockCheckInterval")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getDeadLockCheckInterval();
	
	@Key("RestartOnDeadlock")
	Boolean restartOnDeadlock();
	
	@Key("ClientPacketQueueSize")
	Integer getClientPacketQueueSize();
	
	@Key("ClientPacketQueueMaxBurstSize")
	Integer getClientPacketQueueMaxBurstSize();
	
	@Key("ClientPacketQueueMaxPacketsPerSecond")
	Integer getClientPacketQueueMaxPacketsPerSecond();
	
	@Key("ClientPacketQueueMeasureInterval")
	Integer getClientPacketQueueMeasureInterval();
	
	@Key("ClientPacketQueueMaxAveragePacketsPerSecond")
	Integer getClientPacketQueueMaxAveragePacketsPerSecond();
	
	@Key("ClientPacketQueueMaxFloodsPerMin")
	Integer getClientPacketQueueMaxFloodsPerMin();
	
	@Key("ClientPacketQueueMaxOverflowsPerMin")
	Integer getClientPacketQueueMaxOverflowsPerMin();
	
	@Key("ClientPacketQueueMaxUnderflowsPerMin")
	Integer getClientPacketQueueMaxUnderflowsPerMin();
	
	@Key("ClientPacketQueueMaxUnknownPerMin")
	Integer getClientPacketQueueMaxUnknownPerMin();
	
	@Key("AllowDiscardItem")
	Boolean allowDiscardItem();
	
	@Key("AutoDestroyDroppedItemAfter")
	Integer getAutoDestroyDroppedItemAfter();
	
	@Key("AutoDestroyHerbTime")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getAutoDestroyHerbTime();
	
	@Key("ProtectedItems")
	Set<Integer> getProtectedItems();
	
	@Key("DatabaseCleanUp")
	Boolean databaseCleanUp();
	
	@Key("ConnectionCloseTime")
	Long getConnectionCloseTime();
	
	@Key("CharacterDataStoreInterval")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getCharacterDataStoreInterval();
	
	@Key("LazyItemsUpdate")
	Boolean lazyItemsUpdate();
	
	@Key("UpdateItemsOnCharStore")
	Boolean updateItemsOnCharStore();
	
	@Key("DestroyPlayerDroppedItem")
	Boolean destroyPlayerDroppedItem();
	
	@Key("DestroyEquipableItem")
	Boolean destroyEquipableItem();
	
	@Key("SaveDroppedItem")
	Boolean saveDroppedItem();
	
	@Key("EmptyDroppedItemTableAfterLoad")
	Boolean emptyDroppedItemTableAfterLoad();
	
	@Key("SaveDroppedItemInterval")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getSaveDroppedItemInterval();
	
	@Key("ClearDroppedItemTable")
	Boolean clearDroppedItemTable();
	
	@Key("AutoDeleteInvalidQuestData")
	Boolean autoDeleteInvalidQuestData();
	
	@Key("PreciseDropCalculation")
	Boolean preciseDropCalculation();
	
	@Key("MultipleItemDrop")
	Boolean multipleItemDrop();
	
	@Key("ForceInventoryUpdate")
	Boolean forceInventoryUpdate();
	
	@Key("LazyCache")
	Boolean lazyCache();
	
	@Key("CacheCharNames")
	Boolean cacheCharNames();
	
	@Key("MinNPCAnimation")
	Integer getMinNPCAnimation();
	
	@Key("MaxNPCAnimation")
	Integer getMaxNPCAnimation();
	
	@Key("MinMonsterAnimation")
	Integer getMinMonsterAnimation();
	
	@Key("MaxMonsterAnimation")
	Integer getMaxMonsterAnimation();
	
	@Key("MoveBasedKnownList")
	Boolean moveBasedKnownList();
	
	@Key("KnownListUpdateInterval")
	Long getKnownListUpdateInterval();
	
	@Key("CheckKnownList")
	Boolean checkKnownList();
	
	@Key("GridsAlwaysOn")
	Boolean gridsAlwaysOn();
	
	@Key("GridNeighborTurnOnTime")
	Integer getGridNeighborTurnOnTime();
	
	@Key("GridNeighborTurnOffTime")
	Integer getGridNeighborTurnOffTime();
	
	@Key("EnableFallingDamage")
	Boolean enableFallingDamage();
	
	@Key("PeaceZoneMode")
	Integer getPeaceZoneMode();
	
	@Key("GlobalChat")
	String getGlobalChat();
	
	@Key("TradeChat")
	String getTradeChat();
	
	@Key("AllowWarehouse")
	Boolean allowWarehouse();
	
	@Key("WarehouseCache")
	Boolean warehouseCache();
	
	@Key("WarehouseCacheTime")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getWarehouseCacheTime();
	
	@Key("AllowRefund")
	Boolean allowRefund();
	
	@Key("AllowMail")
	Boolean allowMail();
	
	@Key("AllowAttachments")
	Boolean allowAttachments();
	
	@Key("AllowWear")
	Boolean allowWear();
	
	@Key("WearDelay")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getWearDelay();
	
	@Key("WearPrice")
	Integer getWearPrice();
	
	@Key("RestorePlayerInstance")
	Boolean restorePlayerInstance();
	
	@Key("AllowSummonInInstance")
	Boolean allowSummonInInstance();
	
	@Key("EjectDeadPlayerTime")
	Integer getEjectDeadPlayerTime();
	
	@Key("InstanceFinishTime")
	Integer getInstanceFinishTime();
	
	@Key("AllowRace")
	Boolean allowRace();
	
	@Key("AllowWater")
	Boolean allowWater();
	
	@Key("AllowRentPet")
	Boolean allowRentPet();
	
	@Key("AllowFishing")
	Boolean allowFishing();
	
	@Key("AllowBoat")
	Boolean allowBoat();
	
	@Key("BoatBroadcastRadius")
	Integer getBoatBroadcastRadius();
	
	@Key("AllowCursedWeapons")
	Boolean allowCursedWeapons();
	
	@Key("AllowPetWalkers")
	Boolean allowPetWalkers();
	
	@Key("ShowServerNews")
	Boolean showServerNews();
	
	@Key("EnableCommunityBoard")
	Boolean enableCommunityBoard();
	
	@Key("BBSDefault")
	String getBBSDefault();
	
	@Key("UseChatFilter")
	Boolean useChatFilter();
	
	@Key("ChatFilterChars")
	String getChatFilterChars();
	
	@Key("ChatFilter")
	Set<String> getChatFilter();
	
	@Key("BanChatChannels")
	Set<Integer> getBanChatChannels();
	
	@Key("AllowManor")
	Boolean allowManor();
	
	@Key("ManorRefreshTime")
	Integer getManorRefreshTime();
	
	@Key("ManorRefreshMin")
	Integer getManorRefreshMin();
	
	@Key("ManorApproveTime")
	Integer getManorApproveTime();
	
	@Key("ManorApproveMin")
	Integer getManorApproveMin();
	
	@Key("ManorMaintenanceMin")
	Integer getManorMaintenanceMin();
	
	@Key("ManorSaveAllActions")
	Boolean manorSaveAllActions();
	
	@Key("ManorSavePeriodRate")
	Integer getManorSavePeriodRate();
	
	@Key("AllowLottery")
	Boolean allowLottery();
	
	@Key("LotteryPrize")
	Long getLotteryPrize();
	
	@Key("LotteryTicketPrice")
	Long getLotteryTicketPrice();
	
	@Key("Lottery5NumberRate")
	Float getLottery5NumberRate();
	
	@Key("Lottery4NumberRate")
	Float getLottery4NumberRate();
	
	@Key("Lottery3NumberRate")
	Float getLottery3NumberRate();
	
	@Key("Lottery2and1NumberPrize")
	Long getLottery2and1NumberPrize();
	
	@Key("ItemAuctionEnabled")
	Boolean itemAuctionEnabled();
	
	@Key("ItemAuctionExpiredAfter")
	Integer getItemAuctionExpiredAfter();
	
	@Key("ItemAuctionTimeExtendsOnBid")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getItemAuctionTimeExtendsOnBid();
	
	@Key("RiftMinPartySize")
	Integer getRiftMinPartySize();
	
	@Key("MaxRiftJumps")
	Integer getMaxRiftJumps();
	
	@Key("RiftSpawnDelay")
	Integer getRiftSpawnDelay();
	
	@Key("AutoJumpsDelayMin")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getAutoJumpsDelayMin();
	
	@Key("AutoJumpsDelayMax")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getAutoJumpsDelayMax();
	
	@Key("BossRoomTimeMultiply")
	Float getBossRoomTimeMultiply();
	
	@Key("RecruitCost")
	Integer getRecruitCost();
	
	@Key("SoldierCost")
	Integer getSoldierCost();
	
	@Key("OfficerCost")
	Integer getOfficerCost();
	
	@Key("CaptainCost")
	Integer getCaptainCost();
	
	@Key("CommanderCost")
	Integer getCommanderCost();
	
	@Key("HeroCost")
	Integer getHeroCost();
	
	@Key("TimeOfAttack")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getTimeOfAttack();
	
	@Key("TimeOfCoolDown")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getTimeOfCoolDown();
	
	@Key("TimeOfEntry")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getTimeOfEntry();
	
	@Key("TimeOfWarmUp")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getTimeOfWarmUp();
	
	// TODO(Zoey76): Move this four sepulchers to own configuration file.
	@Key("NumberOfNecessaryPartyMembers")
	Integer getNumberOfNecessaryPartyMembers();
	
	@Key("DefaultPunish")
	IllegalActionPunishmentType getDefaultPunish();
	
	@Key("DefaultPunishParam")
	@ConverterClass(Seconds2MillisecondsConverter.class)
	Integer getDefaultPunishParam();
	
	@Key("OnlyGMItemsFree")
	Boolean onlyGMItemsFree();
	
	@Key("JailIsPvp")
	Boolean jailIsPvp();
	
	@Key("JailDisableChat")
	Boolean jailDisableChat();
	
	@Key("JailDisableTransaction")
	Boolean jailDisableTransaction();
	
	@Key("NormalEnchantCostMultipiler")
	Integer getNormalEnchantCostMultipiler();
	
	@Key("SafeEnchantCostMultipiler")
	Integer getSafeEnchantCostMultipiler();
	
	@Key("CustomSpawnlistTable")
	Boolean customSpawnlistTable();
	
	@Key("SaveGmSpawnOnCustom")
	Boolean saveGmSpawnOnCustom();
	
	@Key("CustomNpcData")
	Boolean customNpcData();
	
	@Key("CustomTeleportTable")
	Boolean customTeleportTable();
	
	@Key("CustomNpcBufferTables")
	Boolean customNpcBufferTables();
	
	@Key("CustomSkillsLoad")
	Boolean customSkillsLoad();
	
	@Key("CustomItemsLoad")
	Boolean customItemsLoad();
	
	@Key("CustomMultisellLoad")
	Boolean customMultisellLoad();
	
	@Key("CustomBuyListLoad")
	Boolean customBuyListLoad();
	
	@Key("BirthdayGift")
	Integer getBirthdayGift();
	
	@Key("BirthdayMailSubject")
	String getBirthdayMailSubject();
	
	@Key("BirthdayMailText")
	String getBirthdayMailText();
	
	@Key("EnableBlockCheckerEvent")
	Boolean enableBlockCheckerEvent();
	
	@Key("BlockCheckerMinTeamMembers")
	Integer getBlockCheckerMinTeamMembers();
	
	@Key("HBCEFairPlay")
	Boolean isHBCEFairPlay();
	
	@Key("HellboundWithoutQuest")
	Boolean hellboundWithoutQuest();
	
	@Key("EnableBotReportButton")
	Boolean enableBotReportButton();
	
	@Key("BotReportPointsResetHour")
	String getBotReportPointsResetHour();
	
	@Key("BotReportDelay")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getBotReportDelay();
	
	@Key("AllowReportsFromSameClanMembers")
	Boolean allowReportsFromSameClanMembers();
	
	@Key("Debug")
	Boolean debug();
	
	@Key("InstanceDebug")
	Boolean instanceDebug();
	
	@Key("HtmlActionCacheDebug")
	Boolean htmlActionCacheDebug();
	
	@Key("PacketHandlerDebug")
	Boolean packetHandlerDebug();
	
	@Key("Developer")
	Boolean developer();
	
	@Key("NoHandlers")
	Boolean noHandlers();
	
	@Key("NoQuests")
	Boolean noQuests();
	
	@Key("NoSpawns")
	Boolean noSpawns();
	
	@Key("ShowQuestsLoadInLogs")
	Boolean showQuestsLoadInLogs();
	
	@Key("ShowScriptsLoadInLogs")
	Boolean showScriptsLoadInLogs();
}