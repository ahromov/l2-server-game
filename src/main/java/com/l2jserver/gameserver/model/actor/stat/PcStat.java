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
import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.vitality;

import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.model.L2PetLevelData;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.transform.TransformTemplate;
import com.l2jserver.gameserver.model.entity.RecoBonus;
import com.l2jserver.gameserver.model.stats.MoveType;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ExVitalityPointInfo;

public class PcStat extends PlayableStat {
	private int _oldMaxHp; // stats watch
	private int _oldMaxMp; // stats watch
	private int _oldMaxCp; // stats watch
	private float _vitalityPoints = 1;
	private byte _vitalityLevel = 0;
	private long _startingXp;
	/** Player's maximum cubic count. */
	private int _maxCubicCount = 1;
	/** Player's maximum talisman count. */
	private final AtomicInteger _talismanSlots = new AtomicInteger();
	private boolean _cloakSlot = false;
	
	public static final int VITALITY_LEVELS[] = {
		240,
		2000,
		13000,
		17000,
		20000
	};
	
	public static final int MAX_VITALITY_POINTS = VITALITY_LEVELS[4];
	public static final int MIN_VITALITY_POINTS = 1;
	
	public PcStat(L2PcInstance activeChar) {
		super(activeChar);
	}
	
	public void setStartingExp(long value) {
		if (general().enableBotReportButton()) {
			_startingXp = value;
		}
	}
	
	public long getStartingExp() {
		return _startingXp;
	}
	
	/**
	 * Gets the maximum cubic count.
	 * @return the maximum cubic count
	 */
	public int getMaxCubicCount() {
		return _maxCubicCount;
	}
	
	/**
	 * Sets the maximum cubic count.
	 * @param cubicCount the maximum cubic count
	 */
	public void setMaxCubicCount(int cubicCount) {
		_maxCubicCount = cubicCount;
	}
	
	/**
	 * Gets the maximum talisman count.
	 * @return the maximum talisman count
	 */
	public int getTalismanSlots() {
		return _talismanSlots.get();
	}
	
	public void addTalismanSlots(int count) {
		_talismanSlots.addAndGet(count);
	}
	
	public boolean canEquipCloak() {
		return _cloakSlot;
	}
	
	public void setCloakSlotStatus(boolean cloakSlot) {
		_cloakSlot = cloakSlot;
	}
	
	@Override
	public final int getMaxCp() {
		// Get the Max CP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_CP, getActiveChar().getTemplate().getBaseCpMax(getActiveChar().getLevel()));
		if (val != _oldMaxCp) {
			_oldMaxCp = val;
			
			// Launch a regen task if the new Max CP is higher than the old one
			if (getActiveChar().getStatus().getCurrentCp() != val) {
				getActiveChar().getStatus().setCurrentCp(getActiveChar().getStatus().getCurrentCp()); // trigger start of regeneration
			}
		}
		return val;
	}
	
	@Override
	public final int getMaxHp() {
		// Get the Max HP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_HP, getActiveChar().getTemplate().getBaseHpMax(getActiveChar().getLevel()));
		if (val != _oldMaxHp) {
			_oldMaxHp = val;
			
			// Launch a regen task if the new Max HP is higher than the old one
			if (getActiveChar().getStatus().getCurrentHp() != val) {
				getActiveChar().getStatus().setCurrentHp(getActiveChar().getStatus().getCurrentHp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	@Override
	public final int getMaxMp() {
		// Get the Max MP (base+modifier) of the L2PcInstance
		int val = (getActiveChar() == null) ? 1 : (int) calcStat(Stats.MAX_MP, getActiveChar().getTemplate().getBaseMpMax(getActiveChar().getLevel()));
		
		if (val != _oldMaxMp) {
			_oldMaxMp = val;
			
			// Launch a regen task if the new Max MP is higher than the old one
			if (getActiveChar().getStatus().getCurrentMp() != val) {
				getActiveChar().getStatus().setCurrentMp(getActiveChar().getStatus().getCurrentMp()); // trigger start of regeneration
			}
		}
		
		return val;
	}
	
	/**
	 * @param type movement type
	 * @return the base move speed of given movement type.
	 */
	@Override
	public double getBaseMoveSpeed(MoveType type) {
		final L2PcInstance player = getActiveChar();
		if (player.isTransformed()) {
			final TransformTemplate template = player.getTransformation().getTemplate(player);
			if (template != null) {
				return template.getBaseMoveSpeed(type);
			}
		} else if (player.isMounted()) {
			final L2PetLevelData data = PetDataTable.getInstance().getPetLevelData(player.getMountNpcId(), player.getMountLevel());
			if (data != null) {
				return data.getSpeedOnRide(type);
			}
		}
		return super.getBaseMoveSpeed(type);
	}
	
	@Override
	public double getRunSpeed() {
		double val = super.getRunSpeed() + character().getRunSpeedBoost();
		
		// Apply max run speed cap.
		if ((val > character().getMaxRunSpeed()) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
			return character().getMaxRunSpeed();
		}
		
		// Check for mount penalties
		if (getActiveChar().isMounted()) {
			// if level diff with mount >= 10, it decreases move speed by 50%
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10) {
				val /= 2;
			}
			// if mount is hungry, it decreases move speed by 50%
			if (getActiveChar().isHungry()) {
				val /= 2;
			}
		}
		
		return val;
	}
	
	@Override
	public double getWalkSpeed() {
		double val = super.getWalkSpeed() + character().getRunSpeedBoost();
		
		// Apply max run speed cap.
		if ((val > character().getMaxRunSpeed()) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
			return character().getMaxRunSpeed();
		}
		
		if (getActiveChar().isMounted()) {
			// if level diff with mount >= 10, it decreases move speed by 50%
			if ((getActiveChar().getMountLevel() - getActiveChar().getLevel()) >= 10) {
				val /= 2;
			}
			// if mount is hungry, it decreases move speed by 50%
			if (getActiveChar().isHungry()) {
				val /= 2;
			}
		}
		
		return val;
	}
	
	@Override
	public double getPAtkSpd() {
		double val = super.getPAtkSpd();
		
		if ((val > character().getMaxPAtkSpeed()) && !getActiveChar().canOverrideCond(PcCondOverride.MAX_STATS_VALUE)) {
			return character().getMaxPAtkSpeed();
		}
		
		return val;
	}
	
	private void updateVitalityLevel(boolean quiet) {
		final byte level;
		
		if (_vitalityPoints <= VITALITY_LEVELS[0]) {
			level = 0;
		} else if (_vitalityPoints <= VITALITY_LEVELS[1]) {
			level = 1;
		} else if (_vitalityPoints <= VITALITY_LEVELS[2]) {
			level = 2;
		} else if (_vitalityPoints <= VITALITY_LEVELS[3]) {
			level = 3;
		} else {
			level = 4;
		}
		
		if (!quiet && (level != _vitalityLevel)) {
			if (level < _vitalityLevel) {
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_DECREASED);
			} else {
				getActiveChar().sendPacket(SystemMessageId.VITALITY_HAS_INCREASED);
			}
			if (level == 0) {
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_EXHAUSTED);
			} else if (level == 4) {
				getActiveChar().sendPacket(SystemMessageId.VITALITY_IS_AT_MAXIMUM);
			}
		}
		
		_vitalityLevel = level;
	}
	
	/*
	 * Return current vitality points in integer format
	 */
	public int getVitalityPoints() {
		return (int) _vitalityPoints;
	}
	
	/*
	 * Set current vitality points to this value if quiet = true - does not send system messages
	 */
	public void setVitalityPoints(int points, boolean quiet) {
		points = Math.min(Math.max(points, MIN_VITALITY_POINTS), MAX_VITALITY_POINTS);
		if (points == _vitalityPoints) {
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
		getActiveChar().sendPacket(new ExVitalityPointInfo(getVitalityPoints()));
	}
	
	public synchronized void updateVitalityPoints(float points, boolean useRates, boolean quiet) {
		if ((points == 0) || !vitality().enabled()) {
			return;
		}
		
		if (useRates) {
			if (getActiveChar().isLucky()) {
				return;
			}
			
			if (points < 0) // vitality consumed
			{
				int stat = (int) calcStat(Stats.VITALITY_CONSUME_RATE, 1, getActiveChar(), null);
				
				if (stat == 0) {
					return;
				}
				if (stat < 0) {
					points = -points;
				}
			}
			
			if (points > 0) {
				// vitality increased
				points *= vitality().getRateVitalityGain();
			} else {
				// vitality decreased
				points *= vitality().getRateVitalityLost();
			}
		}
		
		if (points > 0) {
			points = Math.min(_vitalityPoints + points, MAX_VITALITY_POINTS);
		} else {
			points = Math.max(_vitalityPoints + points, MIN_VITALITY_POINTS);
		}
		
		if (Math.abs(points - _vitalityPoints) <= 1e-6) {
			return;
		}
		
		_vitalityPoints = points;
		updateVitalityLevel(quiet);
	}
	
	public double getVitalityMultiplier() {
		double vitality = 1.0;
		
		if (vitality().enabled()) {
			switch (getVitalityLevel()) {
				case 1:
					vitality = vitality().getRateVitalityLevel1();
					break;
				case 2:
					vitality = vitality().getRateVitalityLevel2();
					break;
				case 3:
					vitality = vitality().getRateVitalityLevel3();
					break;
				case 4:
					vitality = vitality().getRateVitalityLevel4();
					break;
			}
		}
		
		return vitality;
	}
	
	/**
	 * @return the _vitalityLevel
	 */
	public byte getVitalityLevel() {
		return _vitalityLevel;
	}
	
	public double getExpBonusMultiplier() {
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusExp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus exp from skills
		bonusExp = 1 + (calcStat(Stats.BONUS_EXP, 0, null, null) / 100);
		
		if (vitality > 1.0) {
			bonus += (vitality - 1);
		}
		if (nevits > 1.0) {
			bonus += (nevits - 1);
		}
		if (hunting > 1.0) {
			bonus += (hunting - 1);
		}
		if (bonusExp > 1) {
			bonus += (bonusExp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, character().getMaxExpBonus());
		
		return bonus;
	}
	
	public double getSpBonusMultiplier() {
		double bonus = 1.0;
		double vitality = 1.0;
		double nevits = 1.0;
		double hunting = 1.0;
		double bonusSp = 1.0;
		
		// Bonus from Vitality System
		vitality = getVitalityMultiplier();
		
		// Bonus from Nevit's Blessing
		nevits = RecoBonus.getRecoMultiplier(getActiveChar());
		
		// Bonus from Nevit's Hunting
		// TODO: Nevit's hunting bonus
		
		// Bonus sp from skills
		bonusSp = 1 + (calcStat(Stats.BONUS_SP, 0, null, null) / 100);
		
		if (vitality > 1.0) {
			bonus += (vitality - 1);
		}
		if (nevits > 1.0) {
			bonus += (nevits - 1);
		}
		if (hunting > 1.0) {
			bonus += (hunting - 1);
		}
		if (bonusSp > 1) {
			bonus += (bonusSp - 1);
		}
		
		// Check for abnormal bonuses
		bonus = Math.max(bonus, 1);
		bonus = Math.min(bonus, character().getMaxSpBonus());
		
		return bonus;
	}
	
	@Override
	public int getMaxLevel() {
		return getActiveChar().isSubClassActive() ? character().getMaxSubclassLevel() : character().getMaxPlayerLevel();
	}
	
	@Override
	public int getMaxExpLevel() {
		return getActiveChar().isSubClassActive() ? character().getMaxSubclassLevel() + 1 : character().getMaxPlayerLevel() + 1;
	}
	
	@Override
	public final L2PcInstance getActiveChar() {
		return (L2PcInstance) super.getActiveChar();
	}
}
