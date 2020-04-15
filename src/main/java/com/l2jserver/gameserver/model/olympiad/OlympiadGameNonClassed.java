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
package com.l2jserver.gameserver.model.olympiad;

import static com.l2jserver.gameserver.config.Configuration.olympiad;

import java.util.List;

import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * @author DS
 */
public class OlympiadGameNonClassed extends OlympiadGameNormal {
	private OlympiadGameNonClassed(int id, Participant[] opponents) {
		super(id, opponents);
	}
	
	@Override
	public final CompetitionType getType() {
		return CompetitionType.NON_CLASSED;
	}
	
	@Override
	protected final int getDivider() {
		return olympiad().getDividerNonClassed();
	}
	
	@Override
	protected final List<ItemHolder> getReward() {
		return olympiad().getNonClassedReward();
	}
	
	@Override
	protected final String getWeeklyMatchType() {
		return COMP_DONE_WEEK_NON_CLASSED;
	}
	
	protected static final OlympiadGameNonClassed createGame(int id, List<Integer> list) {
		final Participant[] opponents = OlympiadGameNormal.createListOfParticipants(list);
		if (opponents == null) {
			return null;
		}
		
		return new OlympiadGameNonClassed(id, opponents);
	}
}
