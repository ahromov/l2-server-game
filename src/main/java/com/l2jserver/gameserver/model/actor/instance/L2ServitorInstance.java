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
package com.l2jserver.gameserver.model.actor.instance;

import static com.l2jserver.gameserver.config.Configuration.character;

import java.util.concurrent.Future;

import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.data.sql.impl.CharSummonTable;
import com.l2jserver.gameserver.data.sql.impl.SummonEffectsTable;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.SetSummonRemainTime;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;

/**
 * @author UnAfraid
 */
public class L2ServitorInstance extends L2Summon implements Runnable {
	private float _expMultiplier = 0;
	private ItemHolder _itemConsume;
	private int _lifeTime;
	private int _lifeTimeRemaining;
	private int _consumeItemInterval;
	private int _consumeItemIntervalRemaining;
	protected Future<?> _summonLifeTask;
	
	private int _referenceSkill;
	
	public L2ServitorInstance(L2NpcTemplate template, L2PcInstance owner) {
		super(template, owner);
		setInstanceType(InstanceType.L2ServitorInstance);
		setShowSummonAnimation(true);
	}
	
	@Override
	public void onSpawn() {
		super.onSpawn();
		if (_summonLifeTask == null) {
			_summonLifeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(this, 0, 5000);
		}
	}
	
	@Override
	public final int getLevel() {
		return (getTemplate() != null ? getTemplate().getLevel() : 0);
	}
	
	@Override
	public int getSummonType() {
		return 1;
	}
	
	public void setExpMultiplier(float expMultiplier) {
		_expMultiplier = expMultiplier;
	}
	
	public float getExpMultiplier() {
		return _expMultiplier;
	}
	
	public void setItemConsume(ItemHolder item) {
		_itemConsume = item;
	}
	
	public ItemHolder getItemConsume() {
		return _itemConsume;
	}
	
	public void setItemConsumeInterval(int interval) {
		_consumeItemInterval = interval;
		_consumeItemIntervalRemaining = interval;
	}
	
	public int getItemConsumeInterval() {
		return _consumeItemInterval;
	}
	
	public void setLifeTime(int lifeTime) {
		_lifeTime = lifeTime;
		_lifeTimeRemaining = lifeTime;
	}
	
	public int getLifeTime() {
		return _lifeTime;
	}
	
	public void setLifeTimeRemaining(int time) {
		_lifeTimeRemaining = time;
	}
	
	public int getLifeTimeRemaining() {
		return _lifeTimeRemaining;
	}
	
	public void setReferenceSkill(int skillId) {
		_referenceSkill = skillId;
	}
	
	public int getReferenceSkill() {
		return _referenceSkill;
	}
	
	@Override
	public boolean doDie(L2Character killer) {
		if (!super.doDie(killer)) {
			return false;
		}
		
		if (_summonLifeTask != null) {
			_summonLifeTask.cancel(false);
		}
		
		CharSummonTable.getInstance().removeServitor(getOwner());
		return true;
		
	}
	
	@Override
	public void doPickupItem(L2Object object) {
		
	}
	
	@Override
	public void setRestoreSummon(boolean val) {
		_restoreSummon = val;
	}
	
	@Override
	public final void stopSkillEffects(boolean removed, int skillId) {
		super.stopSkillEffects(removed, skillId);
		SummonEffectsTable.getInstance().removeServitorEffects(getOwner(), getReferenceSkill(), skillId);
	}
	
	@Override
	public void storeMe() {
		if ((_referenceSkill == 0) || isDead()) {
			return;
		}
		
		if (character().restoreServitorOnReconnect()) {
			CharSummonTable.getInstance().saveSummon(this);
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects) {
		if (!character().summonStoreSkillCooltime()) {
			return;
		}
		
		if ((getOwner() == null) || getOwner().isInOlympiadMode()) {
			return;
		}
		
		// Clear list for overwrite
		SummonEffectsTable.getInstance().clearServitorEffects(getOwner(), getReferenceSkill());
		
		DAOFactory.getInstance().getServitorSkillSaveDAO().insert(this, storeEffects);
	}
	
	@Override
	public void restoreEffects() {
		if (getOwner().isInOlympiadMode()) {
			return;
		}
		
		DAOFactory.getInstance().getServitorSkillSaveDAO().load(this);
		
		SummonEffectsTable.getInstance().applyServitorEffects(this, getOwner(), getReferenceSkill());
	}
	
	@Override
	public void unSummon(L2PcInstance owner) {
		if (_summonLifeTask != null) {
			_summonLifeTask.cancel(false);
		}
		
		super.unSummon(owner);
		
		if (!_restoreSummon) {
			CharSummonTable.getInstance().removeServitor(owner);
		}
	}
	
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage) {
		return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
	}
	
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage) {
		return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
	}
	
	@Override
	public byte getAttackElement() {
		if (getOwner() != null) {
			return getOwner().getAttackElement();
		}
		return super.getAttackElement();
	}
	
	@Override
	public int getAttackElementValue(byte attackAttribute) {
		if (getOwner() != null) {
			return (getOwner().getAttackElementValue(attackAttribute));
		}
		return super.getAttackElementValue(attackAttribute);
	}
	
	@Override
	public int getDefenseElementValue(byte defenseAttribute) {
		if (getOwner() != null) {
			return (getOwner().getDefenseElementValue(defenseAttribute));
		}
		return super.getDefenseElementValue(defenseAttribute);
	}
	
	@Override
	public boolean isServitor() {
		return true;
	}
	
	@Override
	public void run() {
		int usedtime = 5000;
		_lifeTimeRemaining -= usedtime;
		
		if (isDead() || !isVisible()) {
			if (_summonLifeTask != null) {
				_summonLifeTask.cancel(false);
			}
			return;
		}
		
		// check if the summon's lifetime has ran out
		if (_lifeTimeRemaining < 0) {
			sendPacket(SystemMessageId.SERVITOR_PASSED_AWAY);
			unSummon(getOwner());
			return;
		}
		
		if (_consumeItemInterval > 0) {
			_consumeItemIntervalRemaining -= usedtime;
			
			// check if it is time to consume another item
			if ((_consumeItemIntervalRemaining <= 0) && (getItemConsume().getCount() > 0) && (getItemConsume().getId() > 0) && !isDead()) {
				if (destroyItemByItemId("Consume", getItemConsume().getId(), getItemConsume().getCount(), this, false)) {
					final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.SUMMONED_MOB_USES_S1);
					msg.addItemName(getItemConsume().getId());
					sendPacket(msg);
					
					// Reset
					_consumeItemIntervalRemaining = _consumeItemInterval;
				} else {
					sendPacket(SystemMessageId.SERVITOR_DISAPPEARED_NOT_ENOUGH_ITEMS);
					unSummon(getOwner());
				}
			}
		}
		
		sendPacket(new SetSummonRemainTime(getLifeTime(), _lifeTimeRemaining));
		updateEffectIcons();
	}
	
	@Override
	public double getMAtk(L2Character target, Skill skill) {
		return super.getMAtk(target, skill) + (getActingPlayer().getMAtk(target, skill) * (getActingPlayer().getServitorShareBonus(Stats.MAGIC_ATTACK) - 1.0));
	}
	
	@Override
	public double getMDef(L2Character target, Skill skill) {
		return super.getMDef(target, skill) + (getActingPlayer().getMDef(target, skill) * (getActingPlayer().getServitorShareBonus(Stats.MAGIC_DEFENCE) - 1.0));
	}
	
	@Override
	public double getPAtk(L2Character target) {
		return super.getPAtk(target) + (getActingPlayer().getPAtk(target) * (getActingPlayer().getServitorShareBonus(Stats.POWER_ATTACK) - 1.0));
	}
	
	@Override
	public double getPDef(L2Character target) {
		return super.getPDef(target) + (getActingPlayer().getPDef(target) * (getActingPlayer().getServitorShareBonus(Stats.POWER_DEFENCE) - 1.0));
	}
	
	@Override
	public int getMAtkSpd() {
		return (int) (super.getMAtkSpd() + (getActingPlayer().getMAtkSpd() * (getActingPlayer().getServitorShareBonus(Stats.MAGIC_ATTACK_SPEED) - 1.0)));
	}
	
	@Override
	public int getMaxHp() {
		return (int) (super.getMaxHp() + (getActingPlayer().getMaxHp() * (getActingPlayer().getServitorShareBonus(Stats.MAX_HP) - 1.0)));
	}
	
	@Override
	public int getMaxMp() {
		return (int) (super.getMaxMp() + (getActingPlayer().getMaxMp() * (getActingPlayer().getServitorShareBonus(Stats.MAX_MP) - 1.0)));
	}
	
	@Override
	public int getCriticalHit(L2Character target, Skill skill) {
		return (int) (super.getCriticalHit(target, skill) + ((getActingPlayer().getCriticalHit(target, skill)) * (getActingPlayer().getServitorShareBonus(Stats.CRITICAL_RATE) - 1.0)));
	}
	
	@Override
	public double getPAtkSpd() {
		return super.getPAtkSpd() + (getActingPlayer().getPAtkSpd() * (getActingPlayer().getServitorShareBonus(Stats.POWER_ATTACK_SPEED) - 1.0));
	}
	
	@Override
	public int getMaxRecoverableHp() {
		return (int) calcStat(Stats.MAX_RECOVERABLE_HP, getMaxHp());
	}
	
	@Override
	public int getMaxRecoverableMp() {
		return (int) calcStat(Stats.MAX_RECOVERABLE_MP, getMaxMp());
	}
}
