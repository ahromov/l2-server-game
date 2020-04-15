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
package com.l2jserver.gameserver.instancemanager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.SevenSigns;
import com.l2jserver.gameserver.model.L2MapRegion;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.TeleportWhereType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2SiegeFlagInstance;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.ClanHall;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jserver.gameserver.model.zone.type.L2RespawnZone;
import com.l2jserver.gameserver.util.IXmlReader;

/**
 * Map Region Manager.
 * @author Nyaran
 */
public final class MapRegionManager implements IXmlReader {
	
	private static final Logger LOG = LoggerFactory.getLogger(MapRegionManager.class);
	
	private static final Map<String, L2MapRegion> REGIONS = new HashMap<>();
	
	private static final String DEFAULT_RESPAWN = "talking_island_town";
	
	protected MapRegionManager() {
		load();
	}
	
	@Override
	public void load() {
		REGIONS.clear();
		parseDatapackDirectory("data/mapregion", false);
		LOG.info("Loaded {} map regions.", REGIONS.size());
	}
	
	@Override
	public void parseDocument(Document doc) {
		NamedNodeMap attrs;
		String name;
		String town;
		int locId;
		int castle;
		int bbs;
		
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
			if ("list".equalsIgnoreCase(n.getNodeName())) {
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
					if ("region".equalsIgnoreCase(d.getNodeName())) {
						attrs = d.getAttributes();
						name = attrs.getNamedItem("name").getNodeValue();
						town = attrs.getNamedItem("town").getNodeValue();
						locId = parseInteger(attrs, "locId");
						castle = parseInteger(attrs, "castle");
						bbs = parseInteger(attrs, "bbs");
						
						L2MapRegion region = new L2MapRegion(name, town, locId, castle, bbs);
						for (Node c = d.getFirstChild(); c != null; c = c.getNextSibling()) {
							attrs = c.getAttributes();
							if ("respawnPoint".equalsIgnoreCase(c.getNodeName())) {
								int spawnX = parseInteger(attrs, "X");
								int spawnY = parseInteger(attrs, "Y");
								int spawnZ = parseInteger(attrs, "Z");
								
								boolean other = parseBoolean(attrs, "isOther", false);
								boolean chaotic = parseBoolean(attrs, "isChaotic", false);
								boolean banish = parseBoolean(attrs, "isBanish", false);
								
								if (other) {
									region.addOtherSpawn(spawnX, spawnY, spawnZ);
								} else if (chaotic) {
									region.addChaoticSpawn(spawnX, spawnY, spawnZ);
								} else if (banish) {
									region.addBanishSpawn(spawnX, spawnY, spawnZ);
								} else {
									region.addSpawn(spawnX, spawnY, spawnZ);
								}
							} else if ("map".equalsIgnoreCase(c.getNodeName())) {
								region.addMap(parseInteger(attrs, "X"), parseInteger(attrs, "Y"));
							} else if ("banned".equalsIgnoreCase(c.getNodeName())) {
								region.addBannedRace(attrs.getNamedItem("race").getNodeValue(), attrs.getNamedItem("point").getNodeValue());
							}
						}
						REGIONS.put(name, region);
					}
				}
			}
		}
	}
	
	public final L2MapRegion getMapRegion(int locX, int locY) {
		for (L2MapRegion region : REGIONS.values()) {
			if (region.isZoneInRegion(getMapRegionX(locX), getMapRegionY(locY))) {
				return region;
			}
		}
		return null;
	}
	
	public final int getMapRegionLocId(int locX, int locY) {
		L2MapRegion region = getMapRegion(locX, locY);
		if (region != null) {
			return region.getLocId();
		}
		return 0;
	}
	
	public final L2MapRegion getMapRegion(L2Object obj) {
		return getMapRegion(obj.getX(), obj.getY());
	}
	
	public final int getMapRegionLocId(L2Object obj) {
		return getMapRegionLocId(obj.getX(), obj.getY());
	}
	
	public final int getMapRegionX(int posX) {
		return (posX >> 15) + 9 + 11;// + centerTileX;
	}
	
	public final int getMapRegionY(int posY) {
		return (posY >> 15) + 10 + 8;// + centerTileX;
	}
	
	/**
	 * Get town name by character position
	 * @param activeChar
	 * @return
	 */
	public String getClosestTownName(L2Character activeChar) {
		L2MapRegion region = getMapRegion(activeChar);
		
		if (region == null) {
			return "Aden Castle Town";
		}
		
		return region.getTown();
	}
	
	public int getAreaCastle(L2Character activeChar) {
		L2MapRegion region = getMapRegion(activeChar);
		if (region == null) {
			return 0;
		}
		return region.getCastle();
	}
	
	public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere) {
		Location loc;
		if (activeChar.isPlayer()) {
			final L2PcInstance player = activeChar.getActingPlayer();
			
			Castle castle = null;
			Fort fort = null;
			ClanHall clanhall = null;
			if ((player.getClan() != null) && !player.isFlyingMounted() && !player.isFlying()) // flying players in gracia cant use teleports to aden continent
			{
				// If teleport to clan hall
				if (teleportWhere == TeleportWhereType.CLANHALL) {
					clanhall = ClanHallManager.getInstance().getAbstractHallByOwner(player.getClan());
					if (clanhall != null) {
						L2ClanHallZone zone = clanhall.getZone();
						if ((zone != null) && !player.isFlyingMounted()) {
							if (player.getKarma() > 0) {
								return zone.getChaoticSpawnLoc();
							}
							return zone.getSpawnLoc();
						}
					}
				}
				
				// If teleport to castle
				if (teleportWhere == TeleportWhereType.CASTLE) {
					castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
					// Otherwise check if player is on castle or fortress ground
					// and player's clan is defender
					if (castle == null) {
						castle = CastleManager.getInstance().getCastle(player);
						if (!((castle != null) && castle.getSiege().isInProgress() && (castle.getSiege().getDefenderClan(player.getClan()) != null))) {
							castle = null;
						}
					}
					
					if ((castle != null) && (castle.getResidenceId() > 0)) {
						if (player.getKarma() > 0) {
							return castle.getResidenceZone().getChaoticSpawnLoc();
						}
						return castle.getResidenceZone().getSpawnLoc();
					}
				}
				
				// If teleport to fortress
				if (teleportWhere == TeleportWhereType.FORTRESS) {
					fort = FortManager.getInstance().getFortByOwner(player.getClan());
					// Otherwise check if player is on castle or fortress ground
					// and player's clan is defender
					if (fort == null) {
						fort = FortManager.getInstance().getFort(player);
						if (!((fort != null) && fort.getSiege().isInProgress() && (fort.getOwnerClan() == player.getClan()))) {
							fort = null;
						}
					}
					
					if ((fort != null) && (fort.getResidenceId() > 0)) {
						if (player.getKarma() > 0) {
							return fort.getResidenceZone().getChaoticSpawnLoc();
						}
						return fort.getResidenceZone().getSpawnLoc();
					}
				}
				
				// If teleport to SiegeHQ
				if (teleportWhere == TeleportWhereType.SIEGEFLAG) {
					castle = CastleManager.getInstance().getCastle(player);
					fort = FortManager.getInstance().getFort(player);
					clanhall = ClanHallManager.getInstance().getNearbyAbstractHall(activeChar.getX(), activeChar.getY(), 10000);
					L2SiegeFlagInstance tw_flag = TerritoryWarManager.getInstance().getHQForClan(player.getClan());
					if (tw_flag != null) {
						return tw_flag.getLocation();
					} else if (castle != null) {
						if (castle.getSiege().isInProgress()) {
							// Check if player's clan is attacker
							List<L2Npc> flags = castle.getSiege().getFlag(player.getClan());
							if ((flags != null) && !flags.isEmpty()) {
								// Spawn to flag - Need more work to get player to the nearest flag
								return flags.get(0).getLocation();
							}
						}
						
					} else if (fort != null) {
						if (fort.getSiege().isInProgress()) {
							// Check if player's clan is attacker
							List<L2Npc> flags = fort.getSiege().getFlag(player.getClan());
							if ((flags != null) && !flags.isEmpty()) {
								// Spawn to flag - Need more work to get player to the nearest flag
								return flags.get(0).getLocation();
							}
						}
					} else if ((clanhall != null) && clanhall.isSiegableHall()) {
						SiegableHall sHall = (SiegableHall) clanhall;
						List<L2Npc> flags = sHall.getSiege().getFlag(player.getClan());
						if ((flags != null) && !flags.isEmpty()) {
							return flags.get(0).getLocation();
						}
					}
				}
			}
			
			// Karma player land out of city
			if (player.getKarma() > 0) {
				try {
					L2RespawnZone zone = ZoneManager.getInstance().getZone(player, L2RespawnZone.class);
					if (zone != null) {
						return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getChaoticSpawnLoc();
					}
					return getMapRegion(activeChar).getChaoticSpawnLoc();
				} catch (Exception e) {
					if (player.isFlyingMounted()) {
						return REGIONS.get("union_base_of_kserth").getChaoticSpawnLoc();
					}
					return REGIONS.get(DEFAULT_RESPAWN).getChaoticSpawnLoc();
				}
			}
			
			// Checking if needed to be respawned in "far" town from the castle;
			castle = CastleManager.getInstance().getCastle(player);
			if (castle != null) {
				if (castle.getSiege().isInProgress()) {
					// Check if player's clan is participating
					if ((castle.getSiege().checkIsDefender(player.getClan()) || castle.getSiege().checkIsAttacker(player.getClan())) && (SevenSigns.getInstance().getSealOwner(SevenSigns.SEAL_STRIFE) == SevenSigns.CABAL_DAWN)) {
						return castle.getResidenceZone().getOtherSpawnLoc();
					}
				}
			}
			
			// Checking if in an instance
			if (player.getInstanceId() > 0) {
				Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
				if (inst != null) {
					loc = inst.getExitLoc();
					if (loc != null) {
						return loc;
					}
				}
			}
		}
		
		// Get the nearest town
		try {
			L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);
			if (zone != null) {
				return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getSpawnLoc();
			}
			return getMapRegion(activeChar).getSpawnLoc();
		} catch (Exception e) {
			// Port to the default respawn if no closest town found.
			return REGIONS.get(DEFAULT_RESPAWN).getSpawnLoc();
		}
	}
	
	public L2MapRegion getRestartRegion(L2Character activeChar, String point) {
		try {
			L2PcInstance player = ((L2PcInstance) activeChar);
			L2MapRegion region = REGIONS.get(point);
			
			if (region.getBannedRace().containsKey(player.getRace())) {
				getRestartRegion(player, region.getBannedRace().get(player.getRace()));
			}
			return region;
		} catch (Exception e) {
			return REGIONS.get(DEFAULT_RESPAWN);
		}
	}
	
	/**
	 * @param regionName the map region name.
	 * @return if exists the map region identified by that name, null otherwise.
	 */
	public L2MapRegion getMapRegionByName(String regionName) {
		return REGIONS.get(regionName);
	}
	
	public static MapRegionManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final MapRegionManager INSTANCE = new MapRegionManager();
	}
}
