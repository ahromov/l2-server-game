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
package com.l2jserver.gameserver.config.converter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Class Master Setting.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class ClassMasterSetting {
	
	private final Map<Integer, List<ItemHolder>> claimItems = new HashMap<>(3);
	
	private final Map<Integer, List<ItemHolder>> rewardItems = new HashMap<>(3);
	
	private final Map<Integer, Boolean> allowedClassChange = new HashMap<>(3);
	
	public void addClaimItems(Integer job, List<ItemHolder> items) {
		claimItems.put(job, items);
	}
	
	public void addRewardItems(Integer job, List<ItemHolder> items) {
		rewardItems.put(job, items);
	}
	
	public void addAllowedClassChange(Integer job) {
		allowedClassChange.put(job, true);
	}
	
	public boolean isAllowed(int job) {
		return allowedClassChange.getOrDefault(job, false);
	}
	
	public List<ItemHolder> getRewardItems(int job) {
		return rewardItems.getOrDefault(job, Collections.emptyList());
	}
	
	public List<ItemHolder> getRequireItems(int job) {
		return claimItems.getOrDefault(job, Collections.emptyList());
	}
}