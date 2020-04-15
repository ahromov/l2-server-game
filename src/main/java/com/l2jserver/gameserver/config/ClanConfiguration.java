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

import java.util.regex.Pattern;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.HotReload;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Reloadable;

import com.l2jserver.gameserver.config.converter.PatternConverter;

/**
 * Clan Configuration.
 * @author Zoey76
 * @version 2.6.1.0
 */
@Sources({
	"file:./config/clan.properties",
	"classpath:config/clan.properties"
})
@HotReload(value = 20, unit = MINUTES, type = ASYNC)
public interface ClanConfiguration extends Config, Reloadable {
	
	@Key("ClanNameTemplate")
	@ConverterClass(PatternConverter.class)
	Pattern getClanNameTemplate();
	
	@Key("TakeFortPoints")
	Integer getTakeFortPoints();
	
	@Key("TakeCastlePoints")
	Integer getTakeCastlePoints();
	
	@Key("CastleDefendedPoints")
	Integer getCastleDefendedPoints();
	
	@Key("FestivalOfDarknessWin")
	Integer getFestivalOfDarknessWin();
	
	@Key("HeroPoints")
	Integer getHeroPoints();
	
	@Key("CompleteAcademyMinPoints")
	Integer getCompleteAcademyMinPoints();
	
	@Key("CompleteAcademyMaxPoints")
	Integer getCompleteAcademyMaxPoints();
	
	@Key("KillBallistaPoints")
	Integer getKillBallistaPoints();
	
	@Key("BloodAlliancePoints")
	Integer getBloodAlliancePoints();
	
	@Key("BloodOathPoints")
	Integer getBloodOathPoints();
	
	@Key("KnightsEpaulettePoints")
	Integer getKnightsEpaulettePoints();
	
	@Key("1stRaidRankingPoints")
	Integer get1stRaidRankingPoints();
	
	@Key("2ndRaidRankingPoints")
	Integer get2ndRaidRankingPoints();
	
	@Key("3rdRaidRankingPoints")
	Integer get3rdRaidRankingPoints();
	
	@Key("4thRaidRankingPoints")
	Integer get4thRaidRankingPoints();
	
	@Key("5thRaidRankingPoints")
	Integer get5thRaidRankingPoints();
	
	@Key("6thRaidRankingPoints")
	Integer get6thRaidRankingPoints();
	
	@Key("7thRaidRankingPoints")
	Integer get7thRaidRankingPoints();
	
	@Key("8thRaidRankingPoints")
	Integer get8thRaidRankingPoints();
	
	@Key("9thRaidRankingPoints")
	Integer get9thRaidRankingPoints();
	
	@Key("10thRaidRankingPoints")
	Integer get10thRaidRankingPoints();
	
	@Key("UpTo50thRaidRankingPoints")
	Integer getUpTo50thRaidRankingPoints();
	
	@Key("UpTo100thRaidRankingPoints")
	Integer getUpTo100thRaidRankingPoints();
	
	@Key("ReputationScorePerKill")
	Integer getReputationScorePerKill();
	
	@Key("LoseFortPoints")
	Integer getLoseFortPoints();
	
	@Key("LoseCastlePoints")
	Integer getLoseCastlePoints();
	
	@Key("CreateRoyalGuardCost")
	Integer getCreateRoyalGuardCost();
	
	@Key("CreateKnightUnitCost")
	Integer getCreateKnightUnitCost();
	
	@Key("ReinforceKnightUnitCost")
	Integer getReinforceKnightUnitCost();
	
	@Key("ClanLevel6Cost")
	Integer getClanLevel6Cost();
	
	@Key("ClanLevel7Cost")
	Integer getClanLevel7Cost();
	
	@Key("ClanLevel8Cost")
	Integer getClanLevel8Cost();
	
	@Key("ClanLevel9Cost")
	Integer getClanLevel9Cost();
	
	@Key("ClanLevel10Cost")
	Integer getClanLevel10Cost();
	
	@Key("ClanLevel11Cost")
	Integer getClanLevel11Cost();
	
	@Key("ClanLevel6Requirement")
	Integer getClanLevel6Requirement();
	
	@Key("ClanLevel7Requirement")
	Integer getClanLevel7Requirement();
	
	@Key("ClanLevel8Requirement")
	Integer getClanLevel8Requirement();
	
	@Key("ClanLevel9Requirement")
	Integer getClanLevel9Requirement();
	
	@Key("ClanLevel10Requirement")
	Integer getClanLevel10Requirement();
	
	@Key("ClanLevel11Requirement")
	Integer getClanLevel11Requirement();
}