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
package com.l2jserver.gameserver.util;

import com.l2jserver.gameserver.network.L2GameClient;

/**
 * Collection of flood protectors for single player.
 * @author fordfrog
 */
public final class FloodProtectors {
	
	private final FloodProtectorAction _useItem;
	
	private final FloodProtectorAction _rollDice;
	
	private final FloodProtectorAction _firework;
	
	private final FloodProtectorAction _itemPetSummon;
	private final FloodProtectorAction _heroVoice;
	
	private final FloodProtectorAction _globalChat;
	
	private final FloodProtectorAction _subclass;
	
	private final FloodProtectorAction _dropItem;
	
	private final FloodProtectorAction _serverBypass;
	
	private final FloodProtectorAction _multiSell;
	
	private final FloodProtectorAction _transaction;
	
	private final FloodProtectorAction _manufacture;
	
	private final FloodProtectorAction _manor;
	
	private final FloodProtectorAction _sendMail;
	
	private final FloodProtectorAction _characterSelect;
	
	private final FloodProtectorAction _itemAuction;
	
	/**
	 * Creates new instance of FloodProtectors.
	 * @param client game client for which the collection of flood protectors is being created.
	 */
	public FloodProtectors(final L2GameClient client) {
		_useItem = new FloodProtectorAction(client, "UseItem");
		_rollDice = new FloodProtectorAction(client, "RollDice");
		_firework = new FloodProtectorAction(client, "Firework");
		_itemPetSummon = new FloodProtectorAction(client, "ItemPetSummon");
		_heroVoice = new FloodProtectorAction(client, "HeroVoice");
		_globalChat = new FloodProtectorAction(client, "GlobalChat");
		_subclass = new FloodProtectorAction(client, "Subclass");
		_dropItem = new FloodProtectorAction(client, "DropItem");
		_serverBypass = new FloodProtectorAction(client, "ServerBypass");
		_multiSell = new FloodProtectorAction(client, "MultiSell");
		_transaction = new FloodProtectorAction(client, "Transaction");
		_manufacture = new FloodProtectorAction(client, "Manufacture");
		_manor = new FloodProtectorAction(client, "Manor");
		_sendMail = new FloodProtectorAction(client, "SendMail");
		_characterSelect = new FloodProtectorAction(client, "CharacterSelect");
		_itemAuction = new FloodProtectorAction(client, "ItemAuction");
	}
	
	public FloodProtectorAction getUseItem() {
		return _useItem;
	}
	
	public FloodProtectorAction getRollDice() {
		return _rollDice;
	}
	
	public FloodProtectorAction getFirework() {
		return _firework;
	}
	
	public FloodProtectorAction getItemPetSummon() {
		return _itemPetSummon;
	}
	
	public FloodProtectorAction getHeroVoice() {
		return _heroVoice;
	}
	
	public FloodProtectorAction getGlobalChat() {
		return _globalChat;
	}
	
	public FloodProtectorAction getSubclass() {
		return _subclass;
	}
	
	public FloodProtectorAction getDropItem() {
		return _dropItem;
	}
	
	public FloodProtectorAction getServerBypass() {
		return _serverBypass;
	}
	
	public FloodProtectorAction getMultiSell() {
		return _multiSell;
	}
	
	public FloodProtectorAction getTransaction() {
		return _transaction;
	}
	
	public FloodProtectorAction getManufacture() {
		return _manufacture;
	}
	
	public FloodProtectorAction getManor() {
		return _manor;
	}
	
	public FloodProtectorAction getSendMail() {
		return _sendMail;
	}
	
	public FloodProtectorAction getCharacterSelect() {
		return _characterSelect;
	}
	
	public FloodProtectorAction getItemAuction() {
		return _itemAuction;
	}
}
