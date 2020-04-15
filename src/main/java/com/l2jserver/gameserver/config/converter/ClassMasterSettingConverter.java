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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.aeonbits.owner.Converter;

import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Class Master Setting converter.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class ClassMasterSettingConverter implements Converter<ClassMasterSetting> {
	
	@Override
	public ClassMasterSetting convert(Method method, String input) {
		final var classMasterSetting = new ClassMasterSetting();
		input = input.trim();
		if (input.isEmpty()) {
			return classMasterSetting;
		}
		
		final StringTokenizer st = new StringTokenizer(input, ";");
		while (st.hasMoreTokens()) {
			// get allowed class change
			final int job = Integer.parseInt(st.nextToken());
			classMasterSetting.addAllowedClassChange(job);
			
			final List<ItemHolder> requiredItems = new ArrayList<>();
			// parse items needed for class change
			if (st.hasMoreTokens()) {
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				while (st2.hasMoreTokens()) {
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					requiredItems.add(new ItemHolder(itemId, quantity));
				}
			}
			classMasterSetting.addClaimItems(job, requiredItems);
			
			final List<ItemHolder> rewardItems = new ArrayList<>();
			// parse gifts after class change
			if (st.hasMoreTokens()) {
				final StringTokenizer st2 = new StringTokenizer(st.nextToken(), "[],");
				while (st2.hasMoreTokens()) {
					final StringTokenizer st3 = new StringTokenizer(st2.nextToken(), "()");
					final int itemId = Integer.parseInt(st3.nextToken());
					final int quantity = Integer.parseInt(st3.nextToken());
					rewardItems.add(new ItemHolder(itemId, quantity));
				}
			}
			classMasterSetting.addRewardItems(job, rewardItems);
		}
		return classMasterSetting;
	}
}
