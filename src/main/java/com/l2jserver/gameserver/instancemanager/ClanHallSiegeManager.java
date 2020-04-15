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

import static com.l2jserver.gameserver.config.Configuration.clanhall;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.entity.clanhall.ClanHallSiegeEngine;
import com.l2jserver.gameserver.model.entity.clanhall.SiegableHall;
import com.l2jserver.gameserver.model.zone.type.L2ClanHallZone;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * Clan Hall Siege Manager.
 * @author BiggBoss
 */
public final class ClanHallSiegeManager {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClanHallSiegeManager.class);
	
	private static final String SQL_LOAD_HALLS = "SELECT * FROM siegable_clanhall";
	
	private final Map<Integer, SiegableHall> _siegableHalls = new HashMap<>();
	
	protected ClanHallSiegeManager() {
		loadClanHalls();
	}
	
	private final void loadClanHalls() {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var s = con.createStatement();
			var rs = s.executeQuery(SQL_LOAD_HALLS)) {
			_siegableHalls.clear();
			
			while (rs.next()) {
				final int id = rs.getInt("clanHallId");
				
				StatsSet set = new StatsSet();
				
				set.set("id", id);
				set.set("name", rs.getString("name"));
				set.set("ownerId", rs.getInt("ownerId"));
				set.set("desc", rs.getString("desc"));
				set.set("location", rs.getString("location"));
				set.set("nextSiege", rs.getLong("nextSiege"));
				set.set("siegeLenght", rs.getLong("siegeLenght"));
				set.set("scheduleConfig", rs.getString("schedule_config"));
				SiegableHall hall = new SiegableHall(set);
				_siegableHalls.put(id, hall);
				ClanHallManager.addClanHall(hall);
			}
			LOG.info("Loaded {} conquerable clan halls.", _siegableHalls.size());
		} catch (Exception ex) {
			LOG.warn("Could not load siegable clan halls!", ex);
		}
	}
	
	public Map<Integer, SiegableHall> getConquerableHalls() {
		return _siegableHalls;
	}
	
	public SiegableHall getSiegableHall(int clanHall) {
		return getConquerableHalls().get(clanHall);
	}
	
	public final SiegableHall getNearbyClanHall(L2Character activeChar) {
		return getNearbyClanHall(activeChar.getX(), activeChar.getY(), 10000);
	}
	
	public final SiegableHall getNearbyClanHall(int x, int y, int maxDist) {
		L2ClanHallZone zone = null;
		
		for (Map.Entry<Integer, SiegableHall> ch : _siegableHalls.entrySet()) {
			zone = ch.getValue().getZone();
			if ((zone != null) && (zone.getDistanceToZone(x, y) < maxDist)) {
				return ch.getValue();
			}
		}
		return null;
	}
	
	public final ClanHallSiegeEngine getSiege(L2Character character) {
		SiegableHall hall = getNearbyClanHall(character);
		if (hall == null) {
			return null;
		}
		return hall.getSiege();
	}
	
	public final void registerClan(L2Clan clan, SiegableHall hall, L2PcInstance player) {
		if (clan.getLevel() < clanhall().getMinClanLevel()) {
			player.sendMessage("Only clans of level " + clanhall().getMinClanLevel() + " or higher may register for a castle siege");
		} else if (hall.isWaitingBattle()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED);
			sm.addString(hall.getName());
			player.sendPacket(sm);
		} else if (hall.isInSiege()) {
			player.sendPacket(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
		} else if (hall.getOwnerId() == clan.getId()) {
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING);
		} else if ((clan.getCastleId() != 0) || (clan.getHideoutId() != 0)) {
			player.sendPacket(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
		} else if (hall.getSiege().checkIsAttacker(clan)) {
			player.sendPacket(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
		} else if (isClanParticipating(clan)) {
			player.sendPacket(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
		} else if (hall.getSiege().getAttackers().size() >= clanhall().getMaxAttackers()) {
			player.sendPacket(SystemMessageId.ATTACKER_SIDE_FULL);
		} else {
			hall.addAttacker(clan);
		}
	}
	
	public final void unRegisterClan(L2Clan clan, SiegableHall hall) {
		if (!hall.isRegistering()) {
			return;
		}
		hall.removeAttacker(clan);
	}
	
	public final boolean isClanParticipating(L2Clan clan) {
		for (SiegableHall hall : getConquerableHalls().values()) {
			if ((hall.getSiege() != null) && hall.getSiege().checkIsAttacker(clan)) {
				return true;
			}
		}
		return false;
	}
	
	public final void onServerShutDown() {
		for (SiegableHall hall : getConquerableHalls().values()) {
			// Rainbow springs has his own attackers table
			if ((hall.getId() == 62) || (hall.getSiege() == null)) {
				continue;
			}
			
			hall.getSiege().saveAttackers();
		}
	}
	
	public static ClanHallSiegeManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static final class SingletonHolder {
		protected static final ClanHallSiegeManager INSTANCE = new ClanHallSiegeManager();
	}
}