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

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * Clan Hall Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/clanhall.properties",
	"classpath:config/clanhall.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface ClanHallConfiguration extends Config, Reloadable {
	
	@Key("TeleportFunctionFeeRatio")
	Long getTeleportFunctionFeeRatio();
	
	@Key("TeleportFunctionFeeLvl1")
	Integer getTeleportFunctionFeeLvl1();
	
	@Key("TeleportFunctionFeeLvl2")
	Integer getTeleportFunctionFeeLvl2();
	
	@Key("SupportFunctionFeeRatio")
	Long getSupportFunctionFeeRatio();
	
	@Key("SupportFeeLvl1")
	Integer getSupportFeeLvl1();
	
	@Key("SupportFeeLvl2")
	Integer getSupportFeeLvl2();
	
	@Key("SupportFeeLvl3")
	Integer getSupportFeeLvl3();
	
	@Key("SupportFeeLvl4")
	Integer getSupportFeeLvl4();
	
	@Key("SupportFeeLvl5")
	Integer getSupportFeeLvl5();
	
	@Key("SupportFeeLvl6")
	Integer getSupportFeeLvl6();
	
	@Key("SupportFeeLvl7")
	Integer getSupportFeeLvl7();
	
	@Key("SupportFeeLvl8")
	Integer getSupportFeeLvl8();
	
	@Key("MpRegenerationFunctionFeeRatio")
	Long getMpRegenerationFunctionFeeRatio();
	
	@Key("MpRegenerationFeeLvl1")
	Integer getMpRegenerationFeeLvl1();
	
	@Key("MpRegenerationFeeLvl2")
	Integer getMpRegenerationFeeLvl2();
	
	@Key("MpRegenerationFeeLvl3")
	Integer getMpRegenerationFeeLvl3();
	
	@Key("MpRegenerationFeeLvl4")
	Integer getMpRegenerationFeeLvl4();
	
	@Key("MpRegenerationFeeLvl5")
	Integer getMpRegenerationFeeLvl5();
	
	@Key("HpRegenerationFunctionFeeRatio")
	Long getHpRegenerationFunctionFeeRatio();
	
	@Key("HpRegenerationFeeLvl1")
	Integer getHpRegenerationFeeLvl1();
	
	@Key("HpRegenerationFeeLvl2")
	Integer getHpRegenerationFeeLvl2();
	
	@Key("HpRegenerationFeeLvl3")
	Integer getHpRegenerationFeeLvl3();
	
	@Key("HpRegenerationFeeLvl4")
	Integer getHpRegenerationFeeLvl4();
	
	@Key("HpRegenerationFeeLvl5")
	Integer getHpRegenerationFeeLvl5();
	
	@Key("HpRegenerationFeeLvl6")
	Integer getHpRegenerationFeeLvl6();
	
	@Key("HpRegenerationFeeLvl7")
	Integer getHpRegenerationFeeLvl7();
	
	@Key("HpRegenerationFeeLvl8")
	Integer getHpRegenerationFeeLvl8();
	
	@Key("HpRegenerationFeeLvl9")
	Integer getHpRegenerationFeeLvl9();
	
	@Key("HpRegenerationFeeLvl10")
	Integer getHpRegenerationFeeLvl10();
	
	@Key("HpRegenerationFeeLvl11")
	Integer getHpRegenerationFeeLvl11();
	
	@Key("HpRegenerationFeeLvl12")
	Integer getHpRegenerationFeeLvl12();
	
	@Key("HpRegenerationFeeLvl13")
	Integer getHpRegenerationFeeLvl13();
	
	@Key("ExpRegenerationFunctionFeeRatio")
	Long getExpRegenerationFunctionFeeRatio();
	
	@Key("ExpRegenerationFeeLvl1")
	Integer getExpRegenerationFeeLvl1();
	
	@Key("ExpRegenerationFeeLvl2")
	Integer getExpRegenerationFeeLvl2();
	
	@Key("ExpRegenerationFeeLvl3")
	Integer getExpRegenerationFeeLvl3();
	
	@Key("ExpRegenerationFeeLvl4")
	Integer getExpRegenerationFeeLvl4();
	
	@Key("ExpRegenerationFeeLvl5")
	Integer getExpRegenerationFeeLvl5();
	
	@Key("ExpRegenerationFeeLvl6")
	Integer getExpRegenerationFeeLvl6();
	
	@Key("ExpRegenerationFeeLvl7")
	Integer getExpRegenerationFeeLvl7();
	
	@Key("ItemCreationFunctionFeeRatio")
	Long getItemCreationFunctionFeeRatio();
	
	@Key("ItemCreationFunctionFeeLvl1")
	Integer getItemCreationFunctionFeeLvl1();
	
	@Key("ItemCreationFunctionFeeLvl2")
	Integer getItemCreationFunctionFeeLvl2();
	
	@Key("ItemCreationFunctionFeeLvl3")
	Integer getItemCreationFunctionFeeLvl3();
	
	@Key("CurtainFunctionFeeRatio")
	Long getCurtainFunctionFeeRatio();
	
	@Key("CurtainFunctionFeeLvl1")
	Integer getCurtainFunctionFeeLvl1();
	
	@Key("CurtainFunctionFeeLvl2")
	Integer getCurtainFunctionFeeLvl2();
	
	@Key("FrontPlatformFunctionFeeLvl1")
	Integer getFrontPlatformFunctionFeeLvl1();
	
	@Key("FrontPlatformFunctionFeeLvl2")
	Integer getFrontPlatformFunctionFeeLvl2();
	
	@Key("FrontPlatformFunctionFeeRatio")
	Long getFrontPlatformFunctionFeeRatio();
	
	@Key("MpBuffFree")
	Boolean mpBuffFree();
	
	@Key("MinClanLevel")
	Integer getMinClanLevel();
	
	@Key("MaxAttackers")
	Integer getMaxAttackers();
	
	@Key("MaxFlagsPerClan")
	Integer getMaxFlagsPerClan();
	
	@Key("EnableFame")
	Boolean enableFame();
	
	@Key("FameAmount")
	Integer getFameAmount();
	
	@Key("FameFrequency")
	Integer getFameFrequency();
}