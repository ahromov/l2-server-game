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
 * Fort Siege Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/fortsiege.properties",
	"classpath:config/fortsiege.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface FortSiegeConfiguration extends Config, Accessible, Reloadable {
	
	@Key("SiegeLength")
	Long getSiegeLength();
	
	@Key("SuspiciousMerchantRespawnDelay")
	Integer getSuspiciousMerchantRespawnDelay();
	
	@Key("CountDownLength")
	Integer getCountDownLength();
	
	@Key("MaxFlags")
	Integer getMaxFlags();
	
	@Key("SiegeClanMinLevel")
	Integer getSiegeClanMinLevel();
	
	@Key("AttackerMaxClans")
	Integer getAttackerMaxClans();
	
	@Key("JustToTerritory")
	Boolean justToTerritory();
	
	@Key("ShantyCommander1")
	String getShantyCommander1();
	
	@Key("ShantyCommander2")
	String getShantyCommander2();
	
	@Key("ShantyCommander3")
	String getShantyCommander3();
	
	@Key("ShantyFlag1")
	String getShantyFlag1();
	
	@Key("ShantyFlag2")
	String getShantyFlag2();
	
	@Key("ShantyFlag3")
	String getShantyFlag3();
	
	@Key("SouthernCommander1")
	String getSouthernCommander1();
	
	@Key("SouthernCommander2")
	String getSouthernCommander2();
	
	@Key("SouthernCommander3")
	String getSouthernCommander3();
	
	@Key("SouthernCommander4")
	String getSouthernCommander4();
	
	@Key("SouthernFlag1")
	String getSouthernFlag1();
	
	@Key("SouthernFlag2")
	String getSouthernFlag2();
	
	@Key("SouthernFlag3")
	String getSouthernFlag3();
	
	@Key("HiveCommander1")
	String getHiveCommander1();
	
	@Key("HiveCommander2")
	String getHiveCommander2();
	
	@Key("HiveCommander3")
	String getHiveCommander3();
	
	@Key("HiveFlag1")
	String getHiveFlag1();
	
	@Key("HiveFlag2")
	String getHiveFlag2();
	
	@Key("HiveFlag3")
	String getHiveFlag3();
	
	@Key("ValleyCommander1")
	String getValleyCommander1();
	
	@Key("ValleyCommander2")
	String getValleyCommander2();
	
	@Key("ValleyCommander3")
	String getValleyCommander3();
	
	@Key("ValleyCommander4")
	String getValleyCommander4();
	
	@Key("ValleyFlag1")
	String getValleyFlag1();
	
	@Key("ValleyFlag2")
	String getValleyFlag2();
	
	@Key("ValleyFlag3")
	String getValleyFlag3();
	
	@Key("IvoryCommander1")
	String getIvoryCommander1();
	
	@Key("IvoryCommander2")
	String getIvoryCommander2();
	
	@Key("IvoryCommander3")
	String getIvoryCommander3();
	
	@Key("IvoryFlag1")
	String getIvoryFlag1();
	
	@Key("IvoryFlag2")
	String getIvoryFlag2();
	
	@Key("IvoryFlag3")
	String getIvoryFlag3();
	
	@Key("NarsellCommander1")
	String getNarsellCommander1();
	
	@Key("NarsellCommander2")
	String getNarsellCommander2();
	
	@Key("NarsellCommander3")
	String getNarsellCommander3();
	
	@Key("NarsellFlag1")
	String getNarsellFlag1();
	
	@Key("NarsellFlag2")
	String getNarsellFlag2();
	
	@Key("NarsellFlag3")
	String getNarsellFlag3();
	
	@Key("BayouCommander1")
	String getBayouCommander1();
	
	@Key("BayouCommander2")
	String getBayouCommander2();
	
	@Key("BayouCommander3")
	String getBayouCommander3();
	
	@Key("BayouCommander4")
	String getBayouCommander4();
	
	@Key("BayouFlag1")
	String getBayouFlag1();
	
	@Key("BayouFlag2")
	String getBayouFlag2();
	
	@Key("BayouFlag3")
	String getBayouFlag3();
	
	@Key("WhiteSandsCommander1")
	String getWhiteSandsCommander1();
	
	@Key("WhiteSandsCommander2")
	String getWhiteSandsCommander2();
	
	@Key("WhiteSandsCommander3")
	String getWhiteSandsCommander3();
	
	@Key("WhiteSandsFlag1")
	String getWhiteSandsFlag1();
	
	@Key("WhiteSandsFlag2")
	String getWhiteSandsFlag2();
	
	@Key("WhiteSandsFlag3")
	String getWhiteSandsFlag3();
	
	@Key("BorderlandCommander1")
	String getBorderlandCommander1();
	
	@Key("BorderlandCommander2")
	String getBorderlandCommander2();
	
	@Key("BorderlandCommander3")
	String getBorderlandCommander3();
	
	@Key("BorderlandCommander4")
	String getBorderlandCommander4();
	
	@Key("BorderlandFlag1")
	String getBorderlandFlag1();
	
	@Key("BorderlandFlag2")
	String getBorderlandFlag2();
	
	@Key("BorderlandFlag3")
	String getBorderlandFlag3();
	
	@Key("SwampCommander1")
	String getSwampCommander1();
	
	@Key("SwampCommander2")
	String getSwampCommander2();
	
	@Key("SwampCommander3")
	String getSwampCommander3();
	
	@Key("SwampCommander4")
	String getSwampCommander4();
	
	@Key("SwampFlag1")
	String getSwampFlag1();
	
	@Key("SwampFlag2")
	String getSwampFlag2();
	
	@Key("SwampFlag3")
	String getSwampFlag3();
	
	@Key("ArchaicCommander1")
	String getArchaicCommander1();
	
	@Key("ArchaicCommander2")
	String getArchaicCommander2();
	
	@Key("ArchaicCommander3")
	String getArchaicCommander3();
	
	@Key("ArchaicFlag1")
	String getArchaicFlag1();
	
	@Key("ArchaicFlag2")
	String getArchaicFlag2();
	
	@Key("ArchaicFlag3")
	String getArchaicFlag3();
	
	@Key("FloranCommander1")
	String getFloranCommander1();
	
	@Key("FloranCommander2")
	String getFloranCommander2();
	
	@Key("FloranCommander3")
	String getFloranCommander3();
	
	@Key("FloranCommander4")
	String getFloranCommander4();
	
	@Key("FloranFlag1")
	String getFloranFlag1();
	
	@Key("FloranFlag2")
	String getFloranFlag2();
	
	@Key("FloranFlag3")
	String getFloranFlag3();
	
	@Key("CloudMountainCommander1")
	String getCloudMountainCommander1();
	
	@Key("CloudMountainCommander2")
	String getCloudMountainCommander2();
	
	@Key("CloudMountainCommander3")
	String getCloudMountainCommander3();
	
	@Key("CloudMountainCommander4")
	String getCloudMountainCommander4();
	
	@Key("CloudMountainFlag1")
	String getCloudMountainFlag1();
	
	@Key("CloudMountainFlag2")
	String getCloudMountainFlag2();
	
	@Key("CloudMountainFlag3")
	String getCloudMountainFlag3();
	
	@Key("TanorCommander1")
	String getTanorCommander1();
	
	@Key("TanorCommander2")
	String getTanorCommander2();
	
	@Key("TanorCommander3")
	String getTanorCommander3();
	
	@Key("TanorFlag1")
	String getTanorFlag1();
	
	@Key("TanorFlag2")
	String getTanorFlag2();
	
	@Key("TanorFlag3")
	String getTanorFlag3();
	
	@Key("DragonspineCommander1")
	String getDragonspineCommander1();
	
	@Key("DragonspineCommander2")
	String getDragonspineCommander2();
	
	@Key("DragonspineCommander3")
	String getDragonspineCommander3();
	
	@Key("DragonspineFlag1")
	String getDragonspineFlag1();
	
	@Key("DragonspineFlag2")
	String getDragonspineFlag2();
	
	@Key("DragonspineFlag3")
	String getDragonspineFlag3();
	
	@Key("AntharasCommander1")
	String getAntharasCommander1();
	
	@Key("AntharasCommander2")
	String getAntharasCommander2();
	
	@Key("AntharasCommander3")
	String getAntharasCommander3();
	
	@Key("AntharasCommander4")
	String getAntharasCommander4();
	
	@Key("AntharasFlag1")
	String getAntharasFlag1();
	
	@Key("AntharasFlag2")
	String getAntharasFlag2();
	
	@Key("AntharasFlag3")
	String getAntharasFlag3();
	
	@Key("WesternCommander1")
	String getWesternCommander1();
	
	@Key("WesternCommander2")
	String getWesternCommander2();
	
	@Key("WesternCommander3")
	String getWesternCommander3();
	
	@Key("WesternCommander4")
	String getWesternCommander4();
	
	@Key("WesternFlag1")
	String getWesternFlag1();
	
	@Key("WesternFlag2")
	String getWesternFlag2();
	
	@Key("WesternFlag3")
	String getWesternFlag3();
	
	@Key("HuntersCommander1")
	String getHuntersCommander1();
	
	@Key("HuntersCommander2")
	String getHuntersCommander2();
	
	@Key("HuntersCommander3")
	String getHuntersCommander3();
	
	@Key("HuntersCommander4")
	String getHuntersCommander4();
	
	@Key("HuntersFlag1")
	String getHuntersFlag1();
	
	@Key("HuntersFlag2")
	String getHuntersFlag2();
	
	@Key("HuntersFlag3")
	String getHuntersFlag3();
	
	@Key("AaruCommander1")
	String getAaruCommander1();
	
	@Key("AaruCommander2")
	String getAaruCommander2();
	
	@Key("AaruCommander3")
	String getAaruCommander3();
	
	@Key("AaruFlag1")
	String getAaruFlag1();
	
	@Key("AaruFlag2")
	String getAaruFlag2();
	
	@Key("AaruFlag3")
	String getAaruFlag3();
	
	@Key("DemonCommander1")
	String getDemonCommander1();
	
	@Key("DemonCommander2")
	String getDemonCommander2();
	
	@Key("DemonCommander3")
	String getDemonCommander3();
	
	@Key("DemonFlag1")
	String getDemonFlag1();
	
	@Key("DemonFlag2")
	String getDemonFlag2();
	
	@Key("DemonFlag3")
	String getDemonFlag3();
	
	@Key("MonasticCommander1")
	String getMonasticCommander1();
	
	@Key("MonasticCommander2")
	String getMonasticCommander2();
	
	@Key("MonasticCommander3")
	String getMonasticCommander3();
	
	@Key("MonasticFlag1")
	String getMonasticFlag1();
	
	@Key("MonasticFlag2")
	String getMonasticFlag2();
	
	@Key("MonasticFlag3")
	String getMonasticFlag3();
}