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
package com.l2jserver.gameserver.model.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

/**
 * TvT event team.
 * @author HorridoJoho
 */
public class TvTEventTeam {
	/** The name of the team. */
	private final String _name;
	/** The team spot coordinates. */
	private final Location _loc;
	/** The points of the team. */
	private short _points;
	/** Name and instance of all participated players in map. */
	private final Map<Integer, L2PcInstance> _participatedPlayers = new ConcurrentHashMap<>();
	
	public TvTEventTeam(String name, Location loc) {
		_name = name;
		_loc = loc;
		_points = 0;
	}
	
	/**
	 * Adds a player to the team.
	 * @param playerInstance as L2PcInstance
	 * @return boolean: true if success, otherwise false
	 */
	public boolean addPlayer(L2PcInstance playerInstance) {
		if (playerInstance == null) {
			return false;
		}
		
		_participatedPlayers.put(playerInstance.getObjectId(), playerInstance);
		
		return true;
	}
	
	/**
	 * Removes a player from the team
	 * @param playerObjectId
	 */
	public void removePlayer(int playerObjectId) {
		_participatedPlayers.remove(playerObjectId);
	}
	
	/**
	 * Increases the points of the team.
	 */
	public void increasePoints() {
		++_points;
	}
	
	/**
	 * Cleanup the team and make it ready for adding players again.
	 */
	public void cleanMe() {
		_participatedPlayers.clear();
		_points = 0;
	}
	
	/**
	 * Is given player in this team?
	 * @param playerObjectId
	 * @return boolean: true if player is in this team, otherwise false
	 */
	public boolean containsPlayer(int playerObjectId) {
		return _participatedPlayers.containsKey(playerObjectId);
	}
	
	/**
	 * Returns the name of the team.
	 * @return String: name of the team
	 */
	public String getName() {
		return _name;
	}
	
	/**
	 * Returns the coordinates of the team spot.
	 * @return team coordinates
	 */
	public Location getLocation() {
		return _loc;
	}
	
	/**
	 * Returns the points of the team.
	 * @return short: team points
	 */
	public short getPoints() {
		return _points;
	}
	
	/**
	 * Returns name and instance of all participated players in Map.
	 * @return map of players in this team
	 */
	public Map<Integer, L2PcInstance> getParticipatedPlayers() {
		return _participatedPlayers;
	}
	
	/**
	 * Returns player count of this team.
	 * @return number of players in team
	 */
	public int getParticipatedPlayerCount() {
		return _participatedPlayers.size();
	}
}
