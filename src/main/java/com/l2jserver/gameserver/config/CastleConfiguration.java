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

import static java.util.concurrent.TimeUnit.HOURS;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;

import java.util.List;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * Castle Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/castle.properties",
	"classpath:config/castle.properties"
})
@HotReload(value = 1, unit = HOURS, type = ASYNC)
public interface CastleConfiguration extends Config, Reloadable {
	
	@Key("SiegeHourList")
	List<Integer> getSiegeHourList();
	
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
	
	@Key("OuterDoorUpgradePriceLvl2")
	Integer getOuterDoorUpgradePriceLvl2();
	
	@Key("OuterDoorUpgradePriceLvl3")
	Integer getOuterDoorUpgradePriceLvl3();
	
	@Key("OuterDoorUpgradePriceLvl5")
	Integer getOuterDoorUpgradePriceLvl5();
	
	@Key("InnerDoorUpgradePriceLvl2")
	Integer getInnerDoorUpgradePriceLvl2();
	
	@Key("InnerDoorUpgradePriceLvl3")
	Integer getInnerDoorUpgradePriceLvl3();
	
	@Key("InnerDoorUpgradePriceLvl5")
	Integer getInnerDoorUpgradePriceLvl5();
	
	@Key("WallUpgradePriceLvl2")
	Integer getWallUpgradePriceLvl2();
	
	@Key("WallUpgradePriceLvl3")
	Integer getWallUpgradePriceLvl3();
	
	@Key("WallUpgradePriceLvl5")
	Integer getWallUpgradePriceLvl5();
	
	@Key("TrapUpgradePriceLvl1")
	Integer getTrapUpgradePriceLvl1();
	
	@Key("TrapUpgradePriceLvl2")
	Integer getTrapUpgradePriceLvl2();
	
	@Key("TrapUpgradePriceLvl3")
	Integer getTrapUpgradePriceLvl3();
	
	@Key("TrapUpgradePriceLvl4")
	Integer getTrapUpgradePriceLvl4();
	
	@Key("AllowRideWyvernAlways")
	Boolean allowRideWyvernAlways();
	
	@Key("AllowRideWyvernDuringSiege")
	Boolean allowRideWyvernDuringSiege();
}