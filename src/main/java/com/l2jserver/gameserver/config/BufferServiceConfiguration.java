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
import static org.aeonbits.owner.Config.LoadType.MERGE;

import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

/**
 * Buffer Service Configuration.
 * @author HorridoJoho
 */
@Sources({
	"file:${L2J_HOME}/custom/game/config/bufferservice.properties",
	"file:./config/bufferservice.properties",
	"classpath:config/bufferservice.properties"
})
@LoadPolicy(MERGE)
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface BufferServiceConfiguration extends Reloadable {
	
	@Key("Enable")
	Boolean enable();
	
	@Key("HealCooldown")
	Long getHealCooldown();
	
	@Key("MaxUniqueLists")
	Integer getMaxUniqueLists();
	
	@Key("Debug")
	Boolean getDebug();
	
	@Key("VoicedEnable")
	Boolean getVoicedEnable();
	
	@Key("VoicedCommand")
	String getVoicedCommand();
	
	@Key("VoicedName")
	String getVoicedName();
	
	@Key("VoicedRequiredItem")
	Integer getVoicedRequiredItem();
}