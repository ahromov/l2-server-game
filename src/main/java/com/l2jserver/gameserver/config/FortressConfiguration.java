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

import com.l2jserver.gameserver.config.converter.Minutes2MillisecondsConverter;

/**
 * Fortress Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/fortress.properties",
	"classpath:config/fortress.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface FortressConfiguration extends Config, Reloadable {
	
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
	
	@Key("MpRegenerationFunctionFeeRatio")
	Long getMpRegenerationFunctionFeeRatio();
	
	@Key("MpRegenerationFeeLvl1")
	Integer getMpRegenerationFeeLvl1();
	
	@Key("MpRegenerationFeeLvl2")
	Integer getMpRegenerationFeeLvl2();
	
	@Key("HpRegenerationFunctionFeeRatio")
	Long getHpRegenerationFunctionFeeRatio();
	
	@Key("HpRegenerationFeeLvl1")
	Integer getHpRegenerationFeeLvl1();
	
	@Key("HpRegenerationFeeLvl2")
	Integer getHpRegenerationFeeLvl2();
	
	@Key("ExpRegenerationFunctionFeeRatio")
	Long getExpRegenerationFunctionFeeRatio();
	
	@Key("ExpRegenerationFeeLvl1")
	Integer getExpRegenerationFeeLvl1();
	
	@Key("ExpRegenerationFeeLvl2")
	Integer getExpRegenerationFeeLvl2();
	
	@Key("PeriodicUpdateFrequency")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getPeriodicUpdateFrequency();
	
	@Key("BloodOathCount")
	Integer getBloodOathCount();
	
	@Key("MaxSupplyLevel")
	Integer getMaxSupplyLevel();
	
	@Key("FeeForCastle")
	Integer getFeeForCastle();
	
	@Key("MaxKeepTime")
	Integer getMaxKeepTime();
}