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
package com.l2jserver.gameserver.model.quest;

/**
 * This class merely enumerates the three necessary states for all quests:<br>
 * <ul>
 * <li>CREATED: a quest state is created but the quest is not yet accepted.</li>
 * <li>STARTED: the player has accepted the quest. Quest is currently in progress</li>
 * <li>COMPLETED: the quest has been completed.</li>
 * </ul>
 * In addition, this class defines two functions for lookup and inverse lookup of the state given a name.<br>
 * This is useful only for saving the state values into the database with a more readable form and then being able to read the string back and remap them to their correct states.<br>
 * All quests have these and only these states.
 * @author Luis Arias
 * @author Fulminus
 */
public class State {
	public static final int CREATED = 0;
	public static final int STARTED = 1;
	public static final int COMPLETED = 2;
	
	/**
	 * Get the quest state's string representation from its byte value.
	 * @param state the byte value of the state
	 * @return the String representation of the quest state (default: Start)
	 */
	public static String getStateName(int state) {
		return switch (state) {
			case STARTED -> "Started";
			case COMPLETED -> "Completed";
			default -> "Start";
		};
	}
	
	/**
	 * Get the quest state's byte value from its string representation.
	 * @param statename the String representation of the state
	 * @return the byte value of the quest state (default: 0)
	 */
	public static int getStateId(String statename) {
		return switch (statename) {
			case "Started" -> 1;
			case "Completed" -> 2;
			default -> 0;
		};
	}
}
