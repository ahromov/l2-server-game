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
package com.l2jserver.gameserver.config;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.aeonbits.owner.Config.HotReloadType.ASYNC;

import java.util.List;
import java.util.Set;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.ItemHolderConverter;
import com.l2jserver.gameserver.model.holders.ItemHolder;

/**
 * Olympiad Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/olympiad.properties",
	"classpath:config/olympiad.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface OlympiadConfiguration extends Config, Reloadable {
	
	@Key("StartHour")
	Integer getStartHour();
	
	@Key("StartMinute")
	Integer getStartMinute();
	
	@Key("MaxBuffs")
	Integer getMaxBuffs();
	
	@Key("CompetitionPeriod")
	Integer getCompetitionPeriod();
	
	@Key("BattlePeriod")
	Integer getBattlePeriod();
	
	@Key("WeeklyPeriod")
	Integer getWeeklyPeriod();
	
	@Key("ValidationPeriod")
	Integer getValidationPeriod();
	
	@Key("StartPoints")
	Integer getStartPoints();
	
	@Key("WeeklyPoints")
	Integer getWeeklyPoints();
	
	@Key("ClassedParticipants")
	Integer getClassedParticipants();
	
	@Key("NonClassedParticipants")
	Integer getNonClassedParticipants();
	
	@Key("TeamsParticipants")
	Integer getTeamsParticipants();
	
	@Key("RegistrationDisplayNumber")
	Integer getRegistrationDisplayNumber();
	
	@Separator(";")
	@Key("ClassedReward")
	@ConverterClass(ItemHolderConverter.class)
	List<ItemHolder> getClassedReward();
	
	@Separator(";")
	@Key("NonClassedReward")
	@ConverterClass(ItemHolderConverter.class)
	List<ItemHolder> getNonClassedReward();
	
	@Separator(";")
	@Key("TeamReward")
	@ConverterClass(ItemHolderConverter.class)
	List<ItemHolder> getTeamReward();
	
	@Key("CompetitionRewardItem")
	Integer getCompetitionRewardItem();
	
	@Key("MinMatchesForPoints")
	Integer getMinMatchesForPoints();
	
	@Key("GPPerPoint")
	Integer getGPPerPoint();
	
	@Key("HeroPoints")
	Integer getHeroPoints();
	
	@Key("Rank1Points")
	Integer getRank1Points();
	
	@Key("Rank2Points")
	Integer getRank2Points();
	
	@Key("Rank3Points")
	Integer getRank3Points();
	
	@Key("Rank4Points")
	Integer getRank4Points();
	
	@Key("Rank5Points")
	Integer getRank5Points();
	
	@Key("MaxPoints")
	Integer getMaxPoints();
	
	@Key("ShowMonthlyWinners")
	Boolean showMonthlyWinners();
	
	@Key("AnnounceGames")
	Boolean announceGames();
	
	@Key("RestrictedItems")
	Set<Integer> getRestrictedItems();
	
	@Key("EnchantLimit")
	Integer getEnchantLimit();
	
	@Key("LogFights")
	Boolean logFights();
	
	@Key("WaitTime")
	Integer getWaitTime();
	
	@Key("DividerClassed")
	Integer getDividerClassed();
	
	@Key("DividerNonClassed")
	Integer getDividerNonClassed();
	
	@Key("MaxWeeklyMatches")
	Integer getMaxWeeklyMatches();
	
	@Key("MaxWeeklyMatchesNonClassed")
	Integer getMaxWeeklyMatchesNonClassed();
	
	@Key("MaxWeeklyMatchesClassed")
	Integer getMaxWeeklyMatchesClassed();
	
	@Key("MaxWeeklyMatchesTeam")
	Integer getMaxWeeklyMatchesTeam();
	
	@Key("CurrentCycle")
	Integer getCurrentCycle();
	
	@Key("Period")
	Integer getPeriod();
	
	@Key("OlympiadEnd")
	Long getOlympiadEnd();
	
	@Key("ValidationEnd")
	Long getValidationEnd();
	
	@Key("NextWeeklyChange")
	Long getNextWeeklyChange();
}