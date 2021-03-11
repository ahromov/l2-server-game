/*
 * Copyright © 2004-2021 L2J Server
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

import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Mutable;
import org.aeonbits.owner.Reloadable;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;
import static org.aeonbits.owner.Config.LoadType.MERGE;

/**
 * Discord Configuration.
 * @author Stalitsa
 * @version 2.6.2.0
 */
@Sources({
	"file:${L2J_HOME}/custom/game/config/discord.properties",
	"file:./config/discord.properties",
	"classpath:config/discord.properties"
})
@LoadPolicy(MERGE)
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface DiscordConfiguration extends Mutable, Reloadable {

	@Key("BotEnable")
	Boolean enableBot();

	@Key("BotPrefix")
	String getPrefix();

	@Key("BotToken")
	String getBotToken();

	@Key("ServerId")
	String getServerId();

	@Key("GameMasterId")
	String getGameMasterId();

	@Key("ConsoleLogChannelId")
	String getConsoleLogChannelId();

	@Key("GameChatChannelId")
	String getGameChatChannelId();
}
