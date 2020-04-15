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
package com.l2jserver.gameserver.handler;

import java.util.HashMap;
import java.util.Map;

import com.l2jserver.gameserver.model.items.L2EtcItem;

/**
 * Item handlers.
 * @author UnAfraid
 */
public class ItemHandler implements IHandler<IItemHandler, L2EtcItem> {
	private final Map<String, IItemHandler> _datatable;
	
	protected ItemHandler() {
		_datatable = new HashMap<>();
	}
	
	@Override
	public void registerHandler(IItemHandler handler) {
		_datatable.put(handler.getClass().getSimpleName(), handler);
	}
	
	@Override
	public synchronized void removeHandler(IItemHandler handler) {
		_datatable.remove(handler.getClass().getSimpleName());
	}
	
	@Override
	public IItemHandler getHandler(L2EtcItem item) {
		if ((item == null) || (item.getHandlerName() == null)) {
			return null;
		}
		return _datatable.get(item.getHandlerName());
	}
	
	@Override
	public int size() {
		return _datatable.size();
	}
	
	public static ItemHandler getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder {
		protected static final ItemHandler INSTANCE = new ItemHandler();
	}
}
