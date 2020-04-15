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
package com.l2jserver.gameserver.model.actor.stat;

import static com.l2jserver.gameserver.config.Configuration.character;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import com.l2jserver.gameserver.data.json.ExperienceData;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.playable.OnPlayableExpChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.events.returns.TerminateReturn;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.model.zone.type.L2SwampZone;

public class PlayableStat extends CharStat {
	protected static final Logger _log = Logger.getLogger(PlayableStat.class.getName());
	private final AtomicLong _exp = new AtomicLong();
	private final AtomicInteger _sp = new AtomicInteger();
	private boolean expBlock = false;
	private boolean spBlock = false;

	public PlayableStat(L2Playable activeChar) {
		super(activeChar);
	}

	public long getExp() {
		return _exp.get();
	}

	public int getSp() {
		return _sp.get();
	}

	/**
	 * This method not contains checks!
	 * 
	 * @param exp
	 */
	public void setExp(long exp) {
		_exp.set(exp);
	}

	/**
	 * This method not contains checks!
	 * 
	 * @param sp
	 */
	public void setSp(int sp) {
		_sp.set(sp);
	}

	public boolean isExpBlock() {
		return expBlock;
	}

	/**
	 * @param _expBlock the _expBlock to set
	 */
	public void setExpBlock(boolean _expBlock) {
		this.expBlock = _expBlock;
	}

	/**
	 * @return the _spBlock
	 */
	public boolean isSpBlock() {
		return spBlock;
	}

	/**
	 * @param _spBlock the _spBlock to set
	 */
	public void setSpBlock(boolean _spBlock) {
		this.spBlock = _spBlock;
	}

	/**
	 * Contains only under zero check
	 * 
	 * @param exp
	 * @return
	 */
	public boolean removeExp(long exp) {
		final long currentExp = getExp();
		if (currentExp < exp) {
			_exp.addAndGet(-currentExp);
		} else {
			_exp.addAndGet(-exp);
		}
		syncExpLevel(false);
		return true;
	}

	/**
	 * Contains only under zero check
	 * 
	 * @param sp
	 * @return
	 */
	public boolean removeSp(int sp) {
		final int currentSp = getSp();
		if (currentSp < sp) {
			_sp.addAndGet(-currentSp);
		} else {
			_sp.addAndGet(-sp);
		}
		return true;
	}

	public boolean addExp(long value) {
		if (isExpBlock())
			return false;

		final long currentExp = getExp();
		final long totalExp = currentExp + value;
		final TerminateReturn term = EventDispatcher.getInstance().notifyEvent(
				new OnPlayableExpChanged(getActiveChar(), currentExp, totalExp), getActiveChar(),
				TerminateReturn.class);
		if ((term != null) && term.terminate()) {
			return false;
		}

		if ((totalExp < 0) || ((value > 0) && (currentExp == (getExpForLevel(getMaxExpLevel()) - 1)))) {
			return true;
		}

		if (totalExp >= getExpForLevel(getMaxExpLevel())) {
			value = (getExpForLevel(getMaxExpLevel()) - 1 - currentExp);
		}

		if (_exp.addAndGet(value) >= getExpForLevel(getLevel() + 1)) {
			syncExpLevel(true);
		}

		return true;
	}

	/**
	 * Check if level need to be increased / decreased
	 * 
	 * @param isExpIncreased
	 */
	public void syncExpLevel(boolean isExpIncreased) {
		int minimumLevel = getActiveChar().getMinLevel();
		long currentExp = getExp();
		int maxLevel = getMaxLevel();
		int currentLevel = getLevel();

		if (isExpIncreased) {
			for (int tmp = currentLevel; tmp <= maxLevel; tmp++) {
				if (currentExp >= getExpForLevel(tmp)) {
					if (currentExp >= getExpForLevel(tmp + 1)) {
						continue;
					}
					if (tmp < minimumLevel) {
						tmp = minimumLevel;
					}

					if (tmp != currentLevel) {
						int newLevel = tmp - currentLevel;
						EventDispatcher.getInstance().notifyEventAsync(
								new OnPlayerLevelChanged(getActiveChar().getActingPlayer(), currentLevel, newLevel),
								getActiveChar());
						getActiveChar().addLevel(newLevel);
					}
					break;
				}
			}
		} else {
			for (int tmp = currentLevel; tmp >= minimumLevel; tmp--) {
				if (currentExp < getExpForLevel(tmp)) {
					if (currentExp < getExpForLevel(tmp - 1)) {
						continue;
					}
					--tmp;
					if (tmp < minimumLevel) {
						tmp = minimumLevel;
					}

					if (tmp != currentLevel) {
						int newLevel = tmp - currentLevel;
						EventDispatcher.getInstance().notifyEventAsync(
								new OnPlayerLevelChanged(getActiveChar().getActingPlayer(), currentLevel, newLevel),
								getActiveChar());
						getActiveChar().addLevel(newLevel);
					}
					break;
				}
			}
		}
	}

	public boolean addSp(int sp) {
		if (isSpBlock())
			return false;

		if (sp < 0) {
			_log.warning("addSp acept only possitive numbers!");
			return false;
		}
		int currentSp = getSp();
		if (currentSp == Integer.MAX_VALUE) {
			return false;
		}

		if (sp > (Integer.MAX_VALUE - currentSp)) {
			_sp.set(Integer.MAX_VALUE);
		} else {
			_sp.addAndGet(sp);
		}
		return true;
	}

	public boolean addLevel(int value) {
		final int currentLevel = getLevel();
		if ((currentLevel + value) > getMaxLevel()) {
			if (currentLevel < getMaxLevel()) {
				value = getMaxLevel() - currentLevel;
			} else {
				return false;
			}
		}

		boolean levelIncreased = ((currentLevel + value) > currentLevel);
		value += currentLevel;
		setLevel(value);

		// Sync up exp with current level
		if ((getExp() >= getExpForLevel(getLevel() + 1)) || (getExpForLevel(getLevel()) > getExp())) {
			setExp(getExpForLevel(getLevel()));
		}

		if (!levelIncreased) {
			return false;
		}

		getActiveChar().getStatus().setCurrentHp(getActiveChar().getStat().getMaxHp());
		getActiveChar().getStatus().setCurrentMp(getActiveChar().getStat().getMaxMp());

		return true;
	}

	/**
	 * Get required exp for specific level
	 * 
	 * @param level
	 * @return
	 */
	public long getExpForLevel(int level) {
		return ExperienceData.getInstance().getExpForLevel(level);
	}

	/**
	 * Get maximum level that playable can reach.<br>
	 * <B><U> Overridden in </U> :</B>
	 * <li>PcStat</li>
	 * <li>PetStat</li>
	 */
	public int getMaxLevel() {
		// Dummy method
		return character().getMaxPlayerLevel();
	}

	/**
	 * Get maximum level of expirince is max level +1 for get (100%)<br>
	 * <B><U> Overridden in </U> :</B>
	 * <li>PcStat</li>
	 * <li>PetStat</li>
	 */
	public int getMaxExpLevel() {
		// Dummy method
		return character().getMaxPlayerLevel() + 1;
	}

	@Override
	public L2Playable getActiveChar() {
		return (L2Playable) super.getActiveChar();
	}

	@Override
	public double getRunSpeed() {
		if (getActiveChar().isInsideZone(ZoneId.SWAMP)) {
			final L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			if (zone != null) {
				return super.getRunSpeed() * zone.getMoveBonus();
			}
		}
		return super.getRunSpeed();
	}

	@Override
	public double getWalkSpeed() {
		if (getActiveChar().isInsideZone(ZoneId.SWAMP)) {
			final L2SwampZone zone = ZoneManager.getInstance().getZone(getActiveChar(), L2SwampZone.class);
			if (zone != null) {
				return super.getWalkSpeed() * zone.getMoveBonus();
			}
		}
		return super.getWalkSpeed();
	}
}
