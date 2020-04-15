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

import static com.l2jserver.gameserver.config.Configuration.tvt;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.enums.DuelState;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;

public class TvTEventTeleporter implements Runnable {
	/** The instance of the player to teleport */
	private L2PcInstance _playerInstance = null;
	/** Coordinates of the spot to teleport to */
	private Location _loc = null;
	/** Admin removed this player from event */
	private boolean _adminRemove = false;
	
	/**
	 * Initialize the teleporter and start the delayed task.
	 * @param playerInstance
	 * @param coordinates
	 * @param fastSchedule
	 * @param adminRemove
	 */
	public TvTEventTeleporter(L2PcInstance playerInstance, Location loc, boolean fastSchedule, boolean adminRemove) {
		_playerInstance = playerInstance;
		_loc = loc;
		_adminRemove = adminRemove;
		
		long delay = TvTEvent.isStarted() ? tvt().getRespawnTeleportDelay() : tvt().getStartLeaveTeleportDelay();
		ThreadPoolManager.getInstance().scheduleGeneral(this, fastSchedule ? 0 : delay);
	}
	
	/**
	 * The task method to teleport the player<br>
	 * 1. Unsummon pet if there is one<br>
	 * 2. Remove all effects<br>
	 * 3. Revive and full heal the player<br>
	 * 4. Teleport the player<br>
	 * 5. Broadcast status and user info
	 */
	@Override
	public void run() {
		if (_playerInstance == null) {
			return;
		}
		
		L2Summon summon = _playerInstance.getSummon();
		
		if (summon != null) {
			summon.unSummon(_playerInstance);
		}
		
		final var effectRemoval = tvt().getEffectsRemoval();
		if ((effectRemoval == 0) || ((effectRemoval == 1) && ((_playerInstance.getTeam() == Team.NONE) || (_playerInstance.isInDuel() && (_playerInstance.getDuelState() != DuelState.INTERRUPTED))))) {
			_playerInstance.stopAllEffectsExceptThoseThatLastThroughDeath();
		}
		
		if (_playerInstance.isInDuel()) {
			_playerInstance.setDuelState(DuelState.INTERRUPTED);
		}
		
		int TvTInstance = TvTEvent.getTvTEventInstance();
		if (TvTInstance != 0) {
			if (TvTEvent.isStarted() && !_adminRemove) {
				_playerInstance.setInstanceId(TvTInstance);
			} else {
				_playerInstance.setInstanceId(0);
			}
		} else {
			_playerInstance.setInstanceId(0);
		}
		
		_playerInstance.doRevive();
		_playerInstance.teleToLocation(_loc, true);
		
		if (TvTEvent.isStarted() && !_adminRemove) {
			int teamId = TvTEvent.getParticipantTeamId(_playerInstance.getObjectId()) + 1;
			switch (teamId) {
				case 0:
					_playerInstance.setTeam(Team.NONE);
					break;
				case 1:
					_playerInstance.setTeam(Team.BLUE);
					break;
				case 2:
					_playerInstance.setTeam(Team.RED);
					break;
			}
		} else {
			_playerInstance.setTeam(Team.NONE);
		}
		
		_playerInstance.setCurrentCp(_playerInstance.getMaxCp());
		_playerInstance.setCurrentHp(_playerInstance.getMaxHp());
		_playerInstance.setCurrentMp(_playerInstance.getMaxMp());
		
		_playerInstance.broadcastStatusUpdate();
		_playerInstance.broadcastUserInfo();
	}
}
