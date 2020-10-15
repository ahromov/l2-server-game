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
package com.l2jserver.gameserver.model.effects;

import java.util.concurrent.atomic.AtomicInteger;

import com.l2jserver.gameserver.model.skills.BuffInfo;

/**
 * Effect tick task.
 * @author Zoey76
 */
public class EffectTickTask implements Runnable {
	private final BuffInfo _info;
	private final AbstractEffect _effect;
	private final AtomicInteger _tickCount = new AtomicInteger();
	
	public EffectTickTask(BuffInfo info, AbstractEffect effect) {
		_info = info;
		_effect = effect;
	}
	
	public BuffInfo getBuffInfo() {
		return _info;
	}
	
	public AbstractEffect getEffect() {
		return _effect;
	}
	
	public int getTickCount() {
		return _tickCount.get();
	}
	
	@Override
	public void run() {
		_info.onTick(_effect, _tickCount.incrementAndGet());
	}
}
