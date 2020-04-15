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

import org.aeonbits.owner.Accessible;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * Flood Protector Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/floodprotector.properties",
	"classpath:config/floodprotector.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface FloodProtectorConfiguration extends Config, Accessible, Reloadable {
	
	@Key("UseItemInterval")
	Integer getUseItemInterval();
	
	@Key("UseItemLogFlooding")
	Boolean useItemLogFlooding();
	
	@Key("UseItemPunishmentLimit")
	Integer getUseItemPunishmentLimit();
	
	@Key("UseItemPunishmentType")
	String getUseItemPunishmentType();
	
	@Key("UseItemPunishmentTime")
	Integer getUseItemPunishmentTime();
	
	@Key("RollDiceInterval")
	Integer getRollDiceInterval();
	
	@Key("RollDiceLogFlooding")
	Boolean rollDiceLogFlooding();
	
	@Key("RollDicePunishmentLimit")
	Integer getRollDicePunishmentLimit();
	
	@Key("RollDicePunishmentType")
	String getRollDicePunishmentType();
	
	@Key("RollDicePunishmentTime")
	Integer getRollDicePunishmentTime();
	
	@Key("FireworkInterval")
	Integer getFireworkInterval();
	
	@Key("FireworkLogFlooding")
	Boolean fireworkLogFlooding();
	
	@Key("FireworkPunishmentLimit")
	Integer getFireworkPunishmentLimit();
	
	@Key("FireworkPunishmentType")
	String getFireworkPunishmentType();
	
	@Key("FireworkPunishmentTime")
	Integer getFireworkPunishmentTime();
	
	@Key("ItemPetSummonInterval")
	Integer geItemPetSummonInterval();
	
	@Key("ItemPetSummonLogFlooding")
	Boolean itemPetSummonLogFlooding();
	
	@Key("ItemPetSummonPunishmentLimit")
	Integer getItemPetSummonPunishmentLimit();
	
	@Key("ItemPetSummonPunishmentType")
	String getItemPetSummonPunishmentType();
	
	@Key("ItemPetSummonPunishmentTime")
	Integer getItemPetSummonPunishmentTime();
	
	@Key("HeroVoiceInterval")
	Integer getHeroVoiceInterval();
	
	@Key("HeroVoiceLogFlooding")
	Boolean heroVoiceLogFlooding();
	
	@Key("HeroVoicePunishmentLimit")
	Integer getHeroVoicePunishmentLimit();
	
	@Key("HeroVoicePunishmentType")
	String getHeroVoicePunishmentType();
	
	@Key("HeroVoicePunishmentTime")
	Integer getHeroVoicePunishmentTime();
	
	@Key("GlobalChatInterval")
	Integer getGlobalChatInterval();
	
	@Key("GlobalChatLogFlooding")
	Boolean globalChatLogFlooding();
	
	@Key("GlobalChatPunishmentLimit")
	Integer getGlobalChatPunishmentLimit();
	
	@Key("GlobalChatPunishmentType")
	String getGlobalChatPunishmentType();
	
	@Key("GlobalChatPunishmentTime")
	Integer getGlobalChatPunishmentTime();
	
	@Key("SubclassInterval")
	Integer getSubclassInterval();
	
	@Key("SubclassLogFlooding")
	Boolean subclassLogFlooding();
	
	@Key("SubclassPunishmentLimit")
	Integer getSubclassPunishmentLimit();
	
	@Key("SubclassPunishmentType")
	String getSubclassPunishmentType();
	
	@Key("SubclassPunishmentTime")
	Integer getSubclassPunishmentTime();
	
	@Key("DropItemInterval")
	Integer getDropItemInterval();
	
	@Key("DropItemLogFlooding")
	Boolean dropItemLogFlooding();
	
	@Key("DropItemPunishmentLimit")
	Integer getDropItemPunishmentLimit();
	
	@Key("DropItemPunishmentType")
	String getDropItemPunishmentType();
	
	@Key("DropItemPunishmentTime")
	Integer getDropItemPunishmentTime();
	
	@Key("ServerBypassInterval")
	Integer getServerBypassInterval();
	
	@Key("ServerBypassLogFlooding")
	Boolean serverBypassLogFlooding();
	
	@Key("ServerBypassPunishmentLimit")
	Integer getServerBypassPunishmentLimit();
	
	@Key("ServerBypassPunishmentType")
	String getServerBypassPunishmentType();
	
	@Key("ServerBypassPunishmentTime")
	Integer getServerBypassPunishmentTime();
	
	@Key("MultiSellInterval")
	Integer getMultiSellInterval();
	
	@Key("MultiSellLogFlooding")
	Boolean multiSellLogFlooding();
	
	@Key("MultiSellPunishmentLimit")
	Integer getMultiSellPunishmentLimit();
	
	@Key("MultiSellPunishmentType")
	String getMultiSellPunishmentType();
	
	@Key("MultiSellPunishmentTime")
	Integer getMultiSellPunishmentTime();
	
	@Key("TransactionInterval")
	Integer getTransactionInterval();
	
	@Key("TransactionLogFlooding")
	Boolean transactionLogFlooding();
	
	@Key("TransactionPunishmentLimit")
	Integer getTransactionPunishmentLimit();
	
	@Key("TransactionPunishmentType")
	String getTransactionPunishmentType();
	
	@Key("TransactionPunishmentTime")
	Integer getTransactionPunishmentTime();
	
	@Key("ManufactureInterval")
	Integer getManufactureInterval();
	
	@Key("ManufactureLogFlooding")
	Boolean manufactureLogFlooding();
	
	@Key("ManufacturePunishmentLimit")
	Integer getManufacturePunishmentLimit();
	
	@Key("ManufacturePunishmentType")
	String getManufacturePunishmentType();
	
	@Key("ManufacturePunishmentTime")
	Integer getManufacturePunishmentTime();
	
	@Key("ManorInterval")
	Integer getManorInterval();
	
	@Key("ManorLogFlooding")
	Boolean manorLogFlooding();
	
	@Key("ManorPunishmentLimit")
	Integer getManorPunishmentLimit();
	
	@Key("ManorPunishmentType")
	String getManorPunishmentType();
	
	@Key("ManorPunishmentTime")
	Integer getManorPunishmentTime();
	
	@Key("SendMailInterval")
	Integer getSendMailInterval();
	
	@Key("SendMailLogFlooding")
	Boolean sendMailLogFlooding();
	
	@Key("SendMailPunishmentLimit")
	Integer getSendMailPunishmentLimit();
	
	@Key("SendMailPunishmentType")
	String getSendMailPunishmentType();
	
	@Key("SendMailPunishmentTime")
	Integer getSendMailPunishmentTime();
	
	@Key("CharacterSelectInterval")
	Integer getCharacterSelectInterval();
	
	@Key("CharacterSelectLogFlooding")
	Boolean characterSelectLogFlooding();
	
	@Key("CharacterSelectPunishmentLimit")
	Integer getCharacterSelectPunishmentLimit();
	
	@Key("CharacterSelectPunishmentType")
	String getCharacterSelectPunishmentType();
	
	@Key("CharacterSelectPunishmentTime")
	Integer getCharacterSelectPunishmentTime();
	
	@Key("ItemAuctionInterval")
	Integer getItemAuctionInterval();
	
	@Key("ItemAuctionLogFlooding")
	Boolean itemAuctionLogFlooding();
	
	@Key("ItemAuctionPunishmentLimit")
	Integer getItemAuctionPunishmentLimit();
	
	@Key("ItemAuctionPunishmentType")
	String getItemAuctionPunishmentType();
	
	@Key("ItemAuctionPunishmentTime")
	Integer getItemAuctionPunishmentTime();
}