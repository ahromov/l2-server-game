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
package com.l2jserver.gameserver.ai;

import java.util.List;

/**
 * Class for AI action after some event.<br>
 * Has 2 lists for "work" and "break".
 * @author Yaroslav
 * @author Zoey76
 */
public class NextAction {
	public interface NextActionCallback {
		void doWork();
	}
	
	private final List<CtrlEvent> _events;
	private final List<CtrlIntention> _intentions;
	private final NextActionCallback _callback;
	
	public NextAction(CtrlEvent event, CtrlIntention intention, NextActionCallback callback) {
		_events = List.of(event);
		_intentions = List.of(intention);
		_callback = callback;
	}
	
	public void doAction() {
		_callback.doWork();
	}
	
	public List<CtrlEvent> getEvents() {
		return _events;
	}
	
	public List<CtrlIntention> getIntentions() {
		return _intentions;
	}
}