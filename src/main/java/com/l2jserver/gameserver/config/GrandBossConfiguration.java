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
 * Grand Boss Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/grandboss.properties",
	"classpath:config/grandboss.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface GrandBossConfiguration extends Config, Reloadable {
	
	@Key("AntharasWaitTime")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getAntharasWaitTime();
	
	@Key("IntervalOfAntharasSpawn")
	Integer getIntervalOfAntharasSpawn();
	
	@Key("RandomOfAntharasSpawn")
	Integer getRandomOfAntharasSpawn();
	
	@Key("ValakasWaitTime")
	@ConverterClass(Minutes2MillisecondsConverter.class)
	Integer getValakasWaitTime();
	
	@Key("IntervalOfValakasSpawn")
	Integer getIntervalOfValakasSpawn();
	
	@Key("RandomOfValakasSpawn")
	Integer getRandomOfValakasSpawn();
	
	@Key("IntervalOfBaiumSpawn")
	Integer getIntervalOfBaiumSpawn();
	
	@Key("RandomOfBaiumSpawn")
	Integer getRandomOfBaiumSpawn();
	
	@Key("IntervalOfCoreSpawn")
	Integer getIntervalOfCoreSpawn();
	
	@Key("RandomOfCoreSpawn")
	Integer getRandomOfCoreSpawn();
	
	@Key("IntervalOfOrfenSpawn")
	Integer getIntervalOfOrfenSpawn();
	
	@Key("RandomOfOrfenSpawn")
	Integer getRandomOfOrfenSpawn();
	
	@Key("IntervalOfQueenAntSpawn")
	Integer getIntervalOfQueenAntSpawn();
	
	@Key("RandomOfQueenAntSpawn")
	Integer getRandomOfQueenAntSpawn();
	
	@Key("IntervalOfBelethSpawn")
	Integer getIntervalOfBelethSpawn();
	
	@Key("RandomOfBelethSpawn")
	Integer getRandomOfBelethSpawn();
	
	@Key("BelethMinPlayers")
	Integer getBelethMinPlayers();
}