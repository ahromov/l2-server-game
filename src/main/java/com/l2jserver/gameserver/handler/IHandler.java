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

/**
 * Handler Interface.
 * @author UnAfraid
 * @author Zoey76
 * @param <K>
 * @param <V>
 */
public interface IHandler<K, V> {
	@SuppressWarnings("unchecked")
	default void registerByClass(Class<?> clazz) throws Exception {
		final Object object = clazz.getDeclaredConstructor().newInstance();
		registerHandler((K) object);
	}
	
	/**
	 * Registers the handler.
	 * @param handler the handler
	 */
	void registerHandler(K handler);
	
	/**
	 * Removes the handler.
	 * @param handler the handler to remove
	 */
	void removeHandler(K handler);
	
	/**
	 * Gets the handler for the given object.
	 * @param val the object
	 * @return the handler
	 */
	K getHandler(V val);
	
	/**
	 * Gets the amount of handlers.
	 * @return the amount of handlers
	 */
	int size();
}
