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
import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.npc;
import static com.l2jserver.gameserver.config.Configuration.rates;

import java.util.List;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.data.sql.impl.CharSummonTable;
import com.l2jserver.gameserver.data.sql.impl.SummonEffectsTable;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.enums.ItemLocation;
import com.l2jserver.gameserver.enums.PartyDistributionType;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2PetData;
import com.l2jserver.gameserver.model.L2PetLevelData;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.stat.PetStat;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.itemcontainer.PetInventory;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.BaseStats;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ExChangeNpcState;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.SocialAction;
import com.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.StopMove;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.taskmanager.DecayTaskManager;

public class L2PetInstance extends L2Summon {
	private static final Logger LOG = LoggerFactory.getLogger(L2PetInstance.class);
	
	private int _curFed;
	private final PetInventory _inventory;
	private final int _controlObjectId;
	private boolean _respawned;
	private final boolean _mountable;
	private Future<?> _feedTask;
	private L2PetData _data;
	private L2PetLevelData _leveldata;
	
	/** The Experience before the last Death Penalty */
	private long _expBeforeDeath = 0;
	private int _curWeightPenalty = 0;
	
	protected boolean _bufferMode = true;
	
	/**
	 * Creates a pet.
	 * @param template the pet NPC template
	 * @param owner the owner
	 * @param control the summoning item
	 */
	public L2PetInstance(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control) {
		this(template, owner, control, (byte) (template.getDisplayId() == 12564 ? owner.getLevel() : template.getLevel()));
	}
	
	/**
	 * Creates a pet.
	 * @param template the pet NPC template
	 * @param owner the pet NPC template
	 * @param control the summoning item
	 * @param level the level
	 */
	public L2PetInstance(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control, byte level) {
		super(template, owner);
		setInstanceType(InstanceType.L2PetInstance);
		
		_controlObjectId = control.getObjectId();
		
		getStat().setLevel(Math.max(level, getMinLevel()));
		
		_inventory = new PetInventory(this);
		_inventory.restore();
		
		int npcId = template.getId();
		_mountable = PetDataTable.isMountable(npcId);
		getPetData();
		getPetLevelData();
	}
	
	public final L2PetLevelData getPetLevelData() {
		if (_leveldata == null) {
			_leveldata = PetDataTable.getInstance().getPetLevelData(getTemplate().getId(), getStat().getLevel());
		}
		
		return _leveldata;
	}
	
	public final L2PetData getPetData() {
		if (_data == null) {
			_data = PetDataTable.getInstance().getPetData(getTemplate().getId());
		}
		
		return _data;
	}
	
	public final void setPetData(L2PetLevelData value) {
		_leveldata = value;
	}
	
	/**
	 * Manage Feeding Task.<BR>
	 * Feed or kill the pet depending on hunger level.<br>
	 * If pet has food in inventory and feed level drops below 55% then consume food from inventory.<br>
	 * Send a broadcastStatusUpdate packet for this L2PetInstance
	 */
	class FeedTask implements Runnable {
		private final Logger LOG = LoggerFactory.getLogger(FeedTask.class);
		
		@Override
		public void run() {
			try {
				if ((getOwner() == null) || !getOwner().hasSummon() || (getOwner().getSummon().getObjectId() != getObjectId())) {
					stopFeed();
					return;
				} else if (getCurrentFed() > getFeedConsume()) {
					setCurrentFed(getCurrentFed() - getFeedConsume());
				} else {
					setCurrentFed(0);
				}
				
				broadcastStatusUpdate();
				
				List<Integer> foodIds = getPetData().getFood();
				if (foodIds.isEmpty()) {
					if (isUncontrollable()) {
						// Owl Monk remove PK
						if ((getTemplate().getId() == 16050) && (getOwner() != null)) {
							getOwner().setPkKills(Math.max(0, getOwner().getPkKills() - Rnd.get(1, 6)));
						}
						sendPacket(SystemMessageId.THE_HELPER_PET_LEAVING);
						deleteMe(getOwner());
					} else if (isHungry()) {
						sendPacket(SystemMessageId.THERE_NOT_MUCH_TIME_REMAINING_UNTIL_HELPER_LEAVES);
					}
					return;
				}
				
				L2ItemInstance food = null;
				for (int id : foodIds) {
					food = getInventory().getItemByItemId(id);
					if (food != null) {
						break;
					}
				}
				
				if ((food != null) && isHungry()) {
					final IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
					if (handler != null) {
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY);
						sm.addItemName(food.getId());
						sendPacket(sm);
						handler.useItem(L2PetInstance.this, food, false);
					}
				}
				
				if (isUncontrollable()) {
					sendPacket(SystemMessageId.YOUR_PET_IS_STARVING_AND_WILL_NOT_OBEY_UNTIL_IT_GETS_ITS_FOOD_FEED_YOUR_PET);
				}
			} catch (Exception e) {
				LOG.error("Pet [ObjectId: {}] a feed task error has occurred", getObjectId(), e);
			}
		}
		
		private int getFeedConsume() {
			// if pet is attacking
			if (isAttackingNow()) {
				return getPetLevelData().getPetFeedBattle();
			}
			return getPetLevelData().getPetFeedNormal();
		}
	}
	
	public synchronized static L2PetInstance spawnPet(L2NpcTemplate template, L2PcInstance owner, L2ItemInstance control) {
		if (L2World.getInstance().getPet(owner.getObjectId()) != null) {
			return null; // owner has a pet listed in world
		}
		final L2PetData data = PetDataTable.getInstance().getPetData(template.getId());
		
		final L2PetInstance pet = DAOFactory.getInstance().getPetDAO().load(control, template, owner);
		if (pet != null) {
			pet.setTitle(owner.getName());
			if (data.isSynchLevel() && (pet.getLevel() != owner.getLevel())) {
				pet.getStat().setLevel(owner.getLevel());
				pet.getStat().setExp(pet.getStat().getExpForLevel(owner.getLevel()));
			}
			L2World.getInstance().addPet(owner.getObjectId(), pet);
		}
		return pet;
	}
	
	@Override
	public PetStat getStat() {
		return (PetStat) super.getStat();
	}
	
	@Override
	public void initCharStat() {
		setStat(new PetStat(this));
	}
	
	public boolean isRespawned() {
		return _respawned;
	}
	
	public void setRespawned(boolean respawned) {
		_respawned = respawned;
	}
	
	@Override
	public int getSummonType() {
		return 2;
	}
	
	@Override
	public int getControlObjectId() {
		return _controlObjectId;
	}
	
	public L2ItemInstance getControlItem() {
		return getOwner().getInventory().getItemByObjectId(_controlObjectId);
	}
	
	public int getCurrentFed() {
		return _curFed;
	}
	
	public void setCurrentFed(int num) {
		if (num <= 0) {
			sendPacket(new ExChangeNpcState(getObjectId(), 0x64));
		} else if ((_curFed <= 0) && (num > 0)) {
			sendPacket(new ExChangeNpcState(getObjectId(), 0x65));
		}
		_curFed = num > getMaxFed() ? getMaxFed() : num;
	}
	
	/**
	 * Returns the pet's currently equipped weapon instance (if any).
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance() {
		for (L2ItemInstance item : getInventory().getItems()) {
			if ((item.getItemLocation() == ItemLocation.PET_EQUIP) && (item.getItem().getBodyPart() == L2Item.SLOT_R_HAND)) {
				return item;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the pet's currently equipped weapon (if any).
	 */
	@Override
	public L2Weapon getActiveWeaponItem() {
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null) {
			return null;
		}
		
		return (L2Weapon) weapon.getItem();
	}
	
	@Override
	public L2ItemInstance getSecondaryWeaponInstance() {
		// temporary? unavailable
		return null;
	}
	
	@Override
	public L2Weapon getSecondaryWeaponItem() {
		// temporary? unavailable
		return null;
	}
	
	@Override
	public PetInventory getInventory() {
		return _inventory;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage) {
		L2ItemInstance item = _inventory.destroyItem(process, objectId, count, getOwner(), reference);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		sendPacket(petIU);
		
		if (sendMessage) {
			if (count > 1) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item.getId());
				sm.addLong(count);
				sendPacket(sm);
			} else {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getId());
				sendPacket(sm);
			}
		}
		return true;
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successfull
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage) {
		L2ItemInstance item = _inventory.destroyItemByItemId(process, itemId, count, getOwner(), reference);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		// Send Pet inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		petIU.addItem(item);
		sendPacket(petIU);
		
		if (sendMessage) {
			if (count > 1) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item.getId());
				sm.addLong(count);
				sendPacket(sm);
			} else {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item.getId());
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	@Override
	public void doPickupItem(L2Object object) {
		if (isDead()) {
			return;
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		broadcastPacket(new StopMove(this));
		
		if (!(object instanceof L2ItemInstance)) {
			// dont try to pickup anything that is not an item :)
			LOG.warn("{} trying to pickup wrong target. {}", this, object);
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		boolean follow = getFollowStatus();
		final L2ItemInstance target = (L2ItemInstance) object;
		
		// Cursed weapons
		if (CursedWeaponsManager.getInstance().isCursed(target.getId())) {
			SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
			smsg.addItemName(target.getId());
			sendPacket(smsg);
			return;
		} else if (FortSiegeManager.getInstance().isCombat(target.getId())) {
			return;
		}
		
		SystemMessage smsg = null;
		synchronized (target) {
			// Check if the target to pick up is visible
			if (!target.isVisible()) {
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}
			
			if (((isInParty() && (getParty().getDistributionType() == PartyDistributionType.FINDERS_KEEPERS)) || !isInParty()) && !_inventory.validateCapacity(target)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
				return;
			}
			
			if ((target.getOwnerId() != 0) && (target.getOwnerId() != getOwner().getObjectId()) && !getOwner().isInLooterParty(target.getOwnerId())) {
				if (target.getId() == Inventory.ADENA_ID) {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addLong(target.getCount());
				} else if (target.getCount() > 1) {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addLong(target.getCount());
				} else {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(smsg);
				return;
			}
			
			if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getOwner().getObjectId()) || getOwner().isInLooterParty(target.getOwnerId()))) {
				target.resetOwnerTimer();
			}
			
			// Remove from the ground!
			target.pickupMe(this);
			
			if (general().saveDroppedItem()) {
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		// Herbs
		if (target.getItem().hasExImmediateEffect()) {
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if (handler == null) {
				LOG.warn("No item handler registered for item ID: {}.", target.getId());
			} else {
				handler.useItem(this, target, false);
			}
			
			ItemTable.getInstance().destroyItem("Consume", target, getOwner(), null);
			broadcastStatusUpdate();
		} else {
			if (target.getId() == Inventory.ADENA_ID) {
				smsg = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA);
				smsg.addLong(target.getCount());
				sendPacket(smsg);
			} else if (target.getEnchantLevel() > 0) {
				smsg = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2);
				smsg.addInt(target.getEnchantLevel());
				smsg.addItemName(target);
				sendPacket(smsg);
			} else if (target.getCount() > 1) {
				smsg = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S);
				smsg.addLong(target.getCount());
				smsg.addItemName(target);
				sendPacket(smsg);
			} else {
				smsg = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
			}
			
			// If owner is in party and it isnt finders keepers, distribute the item instead of stealing it -.-
			if (getOwner().isInParty() && (getOwner().getParty().getDistributionType() != PartyDistributionType.FINDERS_KEEPERS)) {
				getOwner().getParty().distributeItem(getOwner(), target);
			} else {
				final L2ItemInstance item = getInventory().addItem("Pickup", target, getOwner(), this);
				// sendPacket(new PetItemList(getInventory().getItems()));
				sendPacket(new PetInventoryUpdate(item));
			}
		}
		
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		if (follow) {
			followOwner();
		}
	}
	
	@Override
	public void deleteMe(L2PcInstance owner) {
		getInventory().transferItemsToOwner();
		super.deleteMe(owner);
		destroyControlItem(owner, false); // this should also delete the pet from the db
		CharSummonTable.getInstance().getPets().remove(getOwner().getObjectId());
	}
	
	@Override
	public boolean doDie(L2Character killer) {
		L2PcInstance owner = getOwner();
		if ((owner != null) && !owner.isInDuel() && (!isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE))) {
			deathPenalty();
		}
		if (!super.doDie(killer, true)) {
			return false;
		}
		stopFeed();
		sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_24_HOURS);
		DecayTaskManager.getInstance().add(this);
		// do not decrease exp if is in duel, arena
		return true;
	}
	
	@Override
	public void doRevive() {
		getOwner().removeReviving();
		
		super.doRevive();
		
		// stopDecay
		DecayTaskManager.getInstance().cancel(this);
		startFeed();
		if (!isHungry()) {
			setRunning();
		}
		getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
	}
	
	@Override
	public void doRevive(double revivePower) {
		// Restore the pet's lost experience,
		// depending on the % return of the skill used (based on its power).
		restoreExp(revivePower);
		doRevive();
	}
	
	/**
	 * Transfers item to another inventory
	 * @param process string identifier of process triggering this action
	 * @param objectId Item Identifier of the item to be transfered
	 * @param count Quantity of items to be transfered
	 * @param target
	 * @param actor the player requesting the item transfer
	 * @param reference Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2PcInstance actor, L2Object reference) {
		L2ItemInstance oldItem = getInventory().getItemByObjectId(objectId);
		L2ItemInstance playerOldItem = target.getItemByItemId(oldItem.getId());
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, actor, reference);
		
		if (newItem == null) {
			return null;
		}
		
		// Send inventory update packet
		PetInventoryUpdate petIU = new PetInventoryUpdate();
		if ((oldItem.getCount() > 0) && (oldItem != newItem)) {
			petIU.addModifiedItem(oldItem);
		} else {
			petIU.addRemovedItem(oldItem);
		}
		sendPacket(petIU);
		
		// Send target update packet
		if (!newItem.isStackable()) {
			InventoryUpdate iu = new InventoryUpdate();
			iu.addNewItem(newItem);
			sendPacket(iu);
		} else if ((playerOldItem != null) && newItem.isStackable()) {
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(newItem);
			sendPacket(iu);
		}
		
		return newItem;
	}
	
	/**
	 * Remove the Pet from DB and its associated item from the player inventory
	 * @param owner The owner from whose inventory we should delete the item
	 * @param evolve
	 */
	public void destroyControlItem(L2PcInstance owner, boolean evolve) {
		// remove the pet instance from world
		L2World.getInstance().removePet(owner.getObjectId());
		
		// delete from inventory
		try {
			L2ItemInstance removedItem;
			if (evolve) {
				removedItem = owner.getInventory().destroyItem("Evolve", getControlObjectId(), 1, getOwner(), this);
			} else {
				removedItem = owner.getInventory().destroyItem("PetDestroy", getControlObjectId(), 1, getOwner(), this);
				if (removedItem != null) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(removedItem);
					owner.sendPacket(sm);
				}
			}
			
			if (removedItem == null) {
				LOG.warn("Couldn't destroy pet control item for {} pet: {} evolve: {}", owner, this, evolve);
			} else {
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(removedItem);
				
				owner.sendPacket(iu);
				
				StatusUpdate su = new StatusUpdate(owner);
				su.addAttribute(StatusUpdate.CUR_LOAD, owner.getCurrentLoad());
				owner.sendPacket(su);
				
				owner.broadcastUserInfo();
				
				L2World.getInstance().removeObject(removedItem);
			}
		} catch (Exception e) {
			LOG.error("Destroying control item: {}", e);
		}
		
		// Pet control item no longer exists, delete the pet from the database.
		DAOFactory.getInstance().getPetDAO().delete(this);
	}
	
	public void dropAllItems() {
		try {
			for (L2ItemInstance item : getInventory().getItems()) {
				dropItemHere(item);
			}
		} catch (Exception e) {
			LOG.warn("Pet Drop Error: {}", e);
		}
	}
	
	public void dropItemHere(L2ItemInstance dropit, boolean protect) {
		dropit = getInventory().dropItem("Drop", dropit.getObjectId(), dropit.getCount(), getOwner(), this);
		
		if (dropit != null) {
			if (protect) {
				dropit.getDropProtection().protect(getOwner());
			}
			LOG.info("Item id to drop: {} amount: {}", dropit.getId(), dropit.getCount());
			dropit.dropMe(this, getX(), getY(), getZ() + 100);
		}
	}
	
	public void dropItemHere(L2ItemInstance dropit) {
		dropItemHere(dropit, false);
	}
	
	/**
	 * @return Returns the mount able.
	 */
	@Override
	public boolean isMountable() {
		return _mountable;
	}
	
	@Override
	public final void stopSkillEffects(boolean removed, int skillId) {
		super.stopSkillEffects(removed, skillId);
		SummonEffectsTable.getInstance().removePetEffects(getControlObjectId(), skillId);
	}
	
	@Override
	public void storeMe() {
		if (getControlObjectId() == 0) {
			// this is a summon, not a pet, don't store anything
			return;
		}
		
		if (!character().restorePetOnReconnect()) {
			setRestoreSummon(false);
		}
		
		if (!isRespawned()) {
			DAOFactory.getInstance().getPetDAO().insert(this);
		} else {
			DAOFactory.getInstance().getPetDAO().update(this);
		}
		
		setRespawned(true);
		
		if (isRestoreSummon()) {
			CharSummonTable.getInstance().getPets().put(getOwner().getObjectId(), getControlObjectId());
		} else {
			CharSummonTable.getInstance().getPets().remove(getOwner().getObjectId());
		}
		
		final L2ItemInstance itemInst = getControlItem();
		if ((itemInst != null) && (itemInst.getEnchantLevel() != getStat().getLevel())) {
			itemInst.setEnchantLevel(getStat().getLevel());
			itemInst.updateDatabase();
		}
	}
	
	@Override
	public void storeEffect(boolean storeEffects) {
		if (!character().summonStoreSkillCooltime()) {
			return;
		}
		
		// Clear list for overwrite
		SummonEffectsTable.getInstance().clearPetEffects(getControlObjectId());
		
		DAOFactory.getInstance().getPetSkillSaveDAO().insert(this, storeEffects);
	}
	
	@Override
	public void restoreEffects() {
		DAOFactory.getInstance().getPetSkillSaveDAO().load(this);
		SummonEffectsTable.getInstance().applyPetEffects(this, getControlObjectId());
	}
	
	public synchronized void stopFeed() {
		if (_feedTask != null) {
			_feedTask.cancel(false);
			_feedTask = null;
		}
	}
	
	public synchronized void startFeed() {
		stopFeed();
		
		if (!isDead() && (getOwner().getSummon() == this)) {
			_feedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FeedTask(), 10000, 10000);
		}
	}
	
	@Override
	public int getMaxLoad() {
		return (int) calcStat(Stats.WEIGHT_LIMIT, Math.floor(BaseStats.CON.calcBonus(this) * 34500 * character().getWeightLimit()), this, null);
	}
	
	@Override
	public int getBonusWeightPenalty() {
		return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
	}
	
	@Override
	public int getCurrentLoad() {
		return getInventory().getTotalWeight();
	}
	
	@Override
	public synchronized void unSummon(L2PcInstance owner) {
		stopFeed();
		stopHpMpRegeneration();
		super.unSummon(owner);
		
		if (!isDead()) {
			if (getInventory() != null) {
				getInventory().deleteMe();
			}
			L2World.getInstance().removePet(owner.getObjectId());
		}
	}
	
	/**
	 * Restore the specified % of experience this L2PetInstance has lost.<BR>
	 * <BR>
	 * @param restorePercent
	 */
	public void restoreExp(double restorePercent) {
		if (_expBeforeDeath > 0) {
			// Restore the specified % of lost experience.
			getStat().addExp(Math.round(((_expBeforeDeath - getStat().getExp()) * restorePercent) / 100));
			_expBeforeDeath = 0;
		}
	}
	
	private void deathPenalty() {
		// TODO: Need Correct Penalty
		
		int lvl = getStat().getLevel();
		double percentLost = (-0.07 * lvl) + 6.5;
		
		// Calculate the Experience loss
		long lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
		
		// Get the Experience before applying penalty
		_expBeforeDeath = getStat().getExp();
		
		// Set the new Experience value of the L2PetInstance
		getStat().removeExp(lostExp);
	}
	
	public void addExp(long exp) {
		getStat().addExp(exp);
	}
	
	public long getExpForLevel(int level) {
		return getStat().getExpForLevel(level);
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp) {
		getStat().addExpAndSp(Math.round(addToExp * (isSinEater() ? rates().getSinEaterXpRate() : rates().getPetXpRate())), addToSp);
	}
	
	public boolean isSinEater() {
		return getId() == 12564;
	}
	
	@Override
	public long getExpForThisLevel() {
		return getStat().getExpForLevel(getLevel());
	}
	
	@Override
	public long getExpForNextLevel() {
		return getStat().getExpForLevel(getLevel() + 1);
	}
	
	@Override
	public final boolean addLevel(int value) {
		if ((getLevel() + value) > getStat().getMaxLevel()) {
			return false;
		}
		
		boolean levelIncreased = getStat().addLevel(value);
		onLevelChange(levelIncreased);
		return levelIncreased;
	}
	
	@Override
	public long getExp() {
		return getStat().getExp();
	}
	
	public void setExp(long exp) {
		getStat().setExp(exp);
	}
	
	public void setSp(int sp) {
		getStat().setSp(sp);
	}
	
	@Override
	public void onLevelChange(boolean levelIncreased) {
		StatusUpdate su = new StatusUpdate(getStat().getActiveChar());
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		getStat().getActiveChar().broadcastPacket(su);
		if (levelIncreased) {
			getStat().getActiveChar().broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
		}
		// Send a Server->Client packet PetInfo to the L2PcInstance
		getStat().getActiveChar().updateAndBroadcastStatus(1);
		
		if (getStat().getActiveChar().getControlItem() != null) {
			getStat().getActiveChar().getControlItem().setEnchantLevel(getLevel());
		}
	}
	
	@Override
	public final int getLevel() {
		return getStat().getLevel();
	}
	
	@Override
	public int getMinLevel() {
		return PetDataTable.getInstance().getPetMinLevel((this).getTemplate().getId());
	}
	
	public int getMaxFed() {
		return getStat().getMaxFeed();
	}
	
	@Override
	public int getCriticalHit(L2Character target, Skill skill) {
		return getStat().getCriticalHit(target, skill);
	}
	
	public void updateRefOwner(L2PcInstance owner) {
		int oldOwnerId = getOwner().getObjectId();
		
		setOwner(owner);
		L2World.getInstance().removePet(oldOwnerId);
		L2World.getInstance().addPet(oldOwnerId, this);
	}
	
	public int getInventoryLimit() {
		return npc().getMaximumSlotsForPet();
	}
	
	public void refreshOverloaded() {
		int maxLoad = getMaxLoad();
		if (maxLoad > 0) {
			long weightproc = (((getCurrentLoad() - getBonusWeightPenalty()) * 1000L) / maxLoad);
			int newWeightPenalty;
			if ((weightproc < 500) || getOwner().getDietMode()) {
				newWeightPenalty = 0;
			} else if (weightproc < 666) {
				newWeightPenalty = 1;
			} else if (weightproc < 800) {
				newWeightPenalty = 2;
			} else if (weightproc < 1000) {
				newWeightPenalty = 3;
			} else {
				newWeightPenalty = 4;
			}
			
			if (_curWeightPenalty != newWeightPenalty) {
				_curWeightPenalty = newWeightPenalty;
				if (newWeightPenalty > 0) {
					addSkill(SkillData.getInstance().getSkill(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() >= maxLoad);
				} else {
					removeSkill(getKnownSkill(4270), true);
					setIsOverloaded(false);
				}
			}
		}
	}
	
	@Override
	public void updateAndBroadcastStatus(int val) {
		refreshOverloaded();
		super.updateAndBroadcastStatus(val);
	}
	
	@Override
	public final boolean isHungry() {
		return getCurrentFed() < ((getPetData().getHungryLimit() / 100f) * getPetLevelData().getPetMaxFeed());
	}
	
	/**
	 * Verifies if a pet can be controlled by it's owner.<br>
	 * Starving pets cannot be controlled.
	 * @return {@code true} if the per cannot be controlled
	 */
	public boolean isUncontrollable() {
		return getCurrentFed() <= 0;
	}
	
	@Override
	public final int getWeapon() {
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (weapon != null) {
			return weapon.getId();
		}
		return 0;
	}
	
	@Override
	public final int getArmor() {
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
		if (weapon != null) {
			return weapon.getId();
		}
		return 0;
	}
	
	public final int getJewel() {
		L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_NECK);
		if (weapon != null) {
			return weapon.getId();
		}
		return 0;
	}
	
	@Override
	public short getSoulShotsPerHit() {
		return getPetLevelData().getPetSoulShot();
	}
	
	@Override
	public short getSpiritShotsPerHit() {
		return getPetLevelData().getPetSpiritShot();
	}
	
	@Override
	public void setName(String name) {
		final L2ItemInstance controlItem = getControlItem();
		if (controlItem != null) {
			if (controlItem.getCustomType2() == (name == null ? 1 : 0)) {
				// name not set yet
				controlItem.setCustomType2(name != null ? 1 : 0);
				controlItem.updateDatabase();
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(controlItem);
				sendPacket(iu);
			}
		} else {
			LOG.warn("Pet control item null, for pet: {}", toString());
		}
		super.setName(name);
	}
	
	public boolean canEatFoodId(int itemId) {
		return _data.getFood().contains(itemId);
	}
	
	@Override
	public boolean isPet() {
		return true;
	}
	
	@Override
	public final double getRunSpeed() {
		return super.getRunSpeed() * (isUncontrollable() ? 0.5d : 1.0d);
	}
	
	@Override
	public final double getWalkSpeed() {
		return super.getWalkSpeed() * (isUncontrollable() ? 0.5d : 1.0d);
	}
	
	@Override
	public final double getMovementSpeedMultiplier() {
		return super.getMovementSpeedMultiplier() * (isUncontrollable() ? 0.5d : 1.0d);
	}
	
	@Override
	public final double getMoveSpeed() {
		if (isInsideZone(ZoneId.WATER)) {
			return isRunning() ? getSwimRunSpeed() : getSwimWalkSpeed();
		}
		return isRunning() ? getRunSpeed() : getWalkSpeed();
	}
	
	/**
	 * Verify if this pet is in support mode.
	 * @return {@code true} if this baby pet is in support mode, {@code false} otherwise
	 */
	public boolean isInSupportMode() {
		return _bufferMode;
	}
}