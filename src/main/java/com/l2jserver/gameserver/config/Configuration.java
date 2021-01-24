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

import org.aeonbits.owner.ConfigFactory;

/**
 * Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class Configuration {
	
	public static final String EOL = System.lineSeparator();
	
	private static final IPConfigData ipConfigData = new IPConfigData();
	
	private static final ServerConfiguration server = ConfigFactory.create(ServerConfiguration.class);
	
	private static final HexIdConfiguration hexId = ConfigFactory.create(HexIdConfiguration.class);
	
	private static final DatabaseConfiguration database = ConfigFactory.create(DatabaseConfiguration.class);
	
	private static final MMOConfiguration mmo = ConfigFactory.create(MMOConfiguration.class);
	
	private static final TelnetConfiguration telnet = ConfigFactory.create(TelnetConfiguration.class);
	
	private static final GeneralConfiguration general = ConfigFactory.create(GeneralConfiguration.class);
	
	private static final GeodataConfiguration geodata = ConfigFactory.create(GeodataConfiguration.class);
	
	private static final CharacterConfiguration character = ConfigFactory.create(CharacterConfiguration.class);
	
	private static final CastleConfiguration castle = ConfigFactory.create(CastleConfiguration.class);
	
	private static final ClanConfiguration clan = ConfigFactory.create(ClanConfiguration.class);
	
	private static final ClanHallConfiguration clanhall = ConfigFactory.create(ClanHallConfiguration.class);
	
	private static final FortressConfiguration fortress = ConfigFactory.create(FortressConfiguration.class);
	
	private static final SevenSignsConfiguration sevenSigns = ConfigFactory.create(SevenSignsConfiguration.class);
	
	private static final TvTConfiguration tvt = ConfigFactory.create(TvTConfiguration.class);
	
	private static final NPCConfiguration npc = ConfigFactory.create(NPCConfiguration.class);
	
	private static final OlympiadConfiguration olympiad = ConfigFactory.create(OlympiadConfiguration.class);
	
	private static final SiegeConfiguration siege = ConfigFactory.create(SiegeConfiguration.class);
	
	private static final TerritoryWarConfiguration territoryWar = ConfigFactory.create(TerritoryWarConfiguration.class);
	
	private static final FortSiegeConfiguration fortSiege = ConfigFactory.create(FortSiegeConfiguration.class);
	
	private static final GrandBossConfiguration grandBoss = ConfigFactory.create(GrandBossConfiguration.class);
	
	private static final GraciaSeedsConfiguration graciaSeeds = ConfigFactory.create(GraciaSeedsConfiguration.class);
	
	private static final RatesConfiguration rates = ConfigFactory.create(RatesConfiguration.class);
	
	private static final VitalityConfiguration vitality = ConfigFactory.create(VitalityConfiguration.class);
	
	private static final PvPConfiguration pvp = ConfigFactory.create(PvPConfiguration.class);
	
	private static final CustomsConfiguration customs = ConfigFactory.create(CustomsConfiguration.class);
	
	private static final FloodProtectorConfiguration floodProtector = ConfigFactory.create(FloodProtectorConfiguration.class);
	
	private static final CustomTeleportConfiguration customTeleport = ConfigFactory.create(CustomTeleportConfiguration.class);
	
	private static final CustomClassMasterConfiguration customClassMaster = ConfigFactory.create(CustomClassMasterConfiguration.class);
	
	private static final CustomNobleMasterConfiguration customNobleMaster = ConfigFactory.create(CustomNobleMasterConfiguration.class);
	
	private static final CustomBufferConfiguration customBuffer = ConfigFactory.create(CustomBufferConfiguration.class);
	
	private static final CustomServicesConfiguration customServices = ConfigFactory.create(CustomServicesConfiguration.class);
	
	private static final CustomShopConfiguration customShop = ConfigFactory.create(CustomShopConfiguration.class);
	
	private static final BufferServiceConfiguration bufferService = ConfigFactory.create(BufferServiceConfiguration.class);
	
	private static final DiscordConfiguration discord = ConfigFactory.create(DiscordConfiguration.class);
	
	private Configuration() {
		// Do nothing.
	}
	
	public static ServerConfiguration server() {
		return server;
	}
	
	public static HexIdConfiguration hexId() {
		return hexId;
	}
	
	public static DatabaseConfiguration database() {
		return database;
	}
	
	public static MMOConfiguration mmo() {
		return mmo;
	}
	
	public static TelnetConfiguration telnet() {
		return telnet;
	}
	
	public static GeneralConfiguration general() {
		return general;
	}
	
	public static GeodataConfiguration geodata() {
		return geodata;
	}
	
	public static CharacterConfiguration character() {
		return character;
	}
	
	public static CastleConfiguration castle() {
		return castle;
	}
	
	public static ClanConfiguration clan() {
		return clan;
	}
	
	public static ClanHallConfiguration clanhall() {
		return clanhall;
	}
	
	public static FortressConfiguration fortress() {
		return fortress;
	}
	
	public static SevenSignsConfiguration sevenSigns() {
		return sevenSigns;
	}
	
	public static TvTConfiguration tvt() {
		return tvt;
	}
	
	public static NPCConfiguration npc() {
		return npc;
	}
	
	public static OlympiadConfiguration olympiad() {
		return olympiad;
	}
	
	public static SiegeConfiguration siege() {
		return siege;
	}
	
	public static TerritoryWarConfiguration territoryWar() {
		return territoryWar;
	}
	
	public static FortSiegeConfiguration fortSiege() {
		return fortSiege;
	}
	
	public static GrandBossConfiguration grandBoss() {
		return grandBoss;
	}
	
	public static GraciaSeedsConfiguration graciaSeeds() {
		return graciaSeeds;
	}
	
	public static RatesConfiguration rates() {
		return rates;
	}
	
	public static VitalityConfiguration vitality() {
		return vitality;
	}
	
	public static PvPConfiguration pvp() {
		return pvp;
	}
	
	public static CustomsConfiguration customs() {
		return customs;
	}
	
	public static FloodProtectorConfiguration floodProtector() {
		return floodProtector;
	}
	
	public static IPConfigData ip() {
		return ipConfigData;
	}
	
	public static CustomTeleportConfiguration customTeleportConfiguration() {
		return customTeleport;
	}
	
	public static CustomClassMasterConfiguration customClassMaster() {
		return customClassMaster;
	}
	
	public static CustomNobleMasterConfiguration customNobleMaster() {
		return customNobleMaster;
	}
	
	public static CustomBufferConfiguration customBufferConfiguration() {
		return customBuffer;
	}
	
	public static CustomServicesConfiguration customServicesConfiguration() {
		return customServices;
	}
	
	public static CustomShopConfiguration customShopConfiguration() {
		return customShop;
	}
	
	public static BufferServiceConfiguration bufferService() {
		return bufferService;
	}
	
	public static DiscordConfiguration discord() {
		return discord;
	}
}
