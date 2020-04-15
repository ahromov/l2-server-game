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
package com.l2jserver.gameserver.dao.impl.mysql;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;
import com.l2jserver.gameserver.dao.PlayerDAO;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.data.sql.impl.ClanTable;
import com.l2jserver.gameserver.enums.Sex;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.model.actor.appearance.PcAppearance;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.SubClass;
import com.l2jserver.gameserver.model.entity.Hero;

/**
 * Player DAO MySQL implementation.
 * @author Zoey76
 */
public class PlayerDAOMySQLImpl implements PlayerDAO {
	
	private static final Logger LOG = LoggerFactory.getLogger(PlayerDAOMySQLImpl.class);
	
	private static final String INSERT = "INSERT INTO characters (account_name,charId,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,fame,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,title_color,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,newbie,nobless,power_grade,createDate) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	private static final String SELECT = "SELECT * FROM characters WHERE charId=?";
	
	private static final String UPDATE = "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,fame=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,title_color=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,newbie=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=?,bookmarkslot=?,vitality_points=?,language=? WHERE charId=?";
	
	private static final String UPDATE_ONLINE = "UPDATE characters SET online=?, lastAccess=? WHERE charId=?";
	
	private static final String SELECT_CHARACTERS = "SELECT charId, char_name FROM characters WHERE account_name=? AND charId<>?";
	
	@Override
	public L2PcInstance load(int objectId) {
		L2PcInstance player = null;
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(SELECT)) {
			// Retrieve the L2PcInstance from the characters table of the database
			ps.setInt(1, objectId);
			try (var rset = ps.executeQuery()) {
				if (rset.next()) {
					final int activeClassId = rset.getInt("classid");
					final boolean female = rset.getInt("sex") != Sex.MALE.ordinal();
					PcAppearance app = new PcAppearance(rset.getByte("face"), rset.getByte("hairColor"), rset.getByte("hairStyle"), female);
					
					player = new L2PcInstance(objectId, activeClassId, rset.getString("account_name"), app);
					player.setName(rset.getString("char_name"));
					player.setLastAccess(rset.getLong("lastAccess"));
					player.setExp(rset.getLong("exp"));
					player.setExpBeforeDeath(rset.getLong("expBeforeDeath"));
					player.setLevel(rset.getInt("level"));
					player.setSp(rset.getInt("sp"));
					player.setWantsPeace(rset.getInt("wantspeace"));
					player.setHeading(rset.getInt("heading"));
					player.setKarma(rset.getInt("karma"));
					player.setFame(rset.getInt("fame"));
					player.setPvpKills(rset.getInt("pvpkills"));
					player.setPkKills(rset.getInt("pkkills"));
					player.setOnlineTime(rset.getLong("onlinetime"));
					player.setNewbie(rset.getInt("newbie"));
					player.setNoble(rset.getInt("nobless") == 1);
					
					player.setClanJoinExpiryTime(rset.getLong("clan_join_expiry_time"));
					if (player.getClanJoinExpiryTime() < System.currentTimeMillis()) {
						player.setClanJoinExpiryTime(0);
					}
					player.setClanCreateExpiryTime(rset.getLong("clan_create_expiry_time"));
					if (player.getClanCreateExpiryTime() < System.currentTimeMillis()) {
						player.setClanCreateExpiryTime(0);
					}
					
					player.setPowerGrade(rset.getInt("power_grade"));
					player.setPledgeType(rset.getInt("subpledge"));
					// player.setApprentice(rset.getInt("apprentice"));
					
					player.setDeleteTimer(rset.getLong("deletetime"));
					player.setTitle(rset.getString("title"));
					player.setAccessLevel(rset.getInt("accesslevel"));
					player.getAppearance().setTitleColor(rset.getInt("title_color"));
					player.setFistsWeaponItem(player.findFistsWeaponItem(activeClassId));
					player.setUptime(System.currentTimeMillis());
					
					player.setCurrentCp(rset.getDouble("curCp"));
					player.setCurrentHp(rset.getDouble("curHp"));
					player.setCurrentMp(rset.getDouble("curMp"));
					player.setClassIndex(0);
					player.setBaseClass(rset.getInt("base_class"));
					
					// Restore Subclass Data (cannot be done earlier in function)
					DAOFactory.getInstance().getSubclassDAO().load(player);
					
					if (activeClassId != player.getBaseClass()) {
						for (SubClass subClass : player.getSubClasses().values()) {
							if (subClass.getClassId() == activeClassId) {
								player.setClassIndex(subClass.getClassIndex());
							}
						}
					}
					
					if ((player.getClassIndex() == 0) && (activeClassId != player.getBaseClass())) {
						// Subclass in use but doesn't exist in DB -
						// a possible restart-while-modify-subclass cheat has been attempted.
						// Switching to use base class
						player.setClassId(player.getBaseClass());
						LOG.warn("{} reverted to base class. Possibly has tried a relogin exploit while subclassing.", player);
					} else {
						player.setActiveClass(activeClassId);
					}
					
					player.setApprentice(rset.getInt("apprentice"));
					player.setSponsor(rset.getInt("sponsor"));
					player.setLvlJoinedAcademy(rset.getInt("lvl_joined_academy"));
					player.setIsIn7sDungeon(rset.getInt("isin7sdungeon") == 1);
					
					CursedWeaponsManager.getInstance().checkPlayer(player);
					
					player.setDeathPenaltyBuffLevel(rset.getInt("death_penalty_level"));
					
					player.setVitalityPoints(rset.getInt("vitality_points"), true);
					
					// Set the x,y,z position of the L2PcInstance and make it invisible
					player.setXYZInvisible(rset.getInt("x"), rset.getInt("y"), rset.getInt("z"));
					
					// Set Teleport Bookmark Slot
					player.setBookMarkSlot(rset.getInt("BookmarkSlot"));
					
					// character creation Time
					player.getCreateDate().setTimeInMillis(rset.getTimestamp("createDate").getTime());
					
					// Language
					player.setLang(rset.getString("language"));
					
					// Set Hero status if it applies
					player.setHero(Hero.getInstance().isHero(objectId));
					
					player.setClan(ClanTable.getInstance().getClan(rset.getInt("clanid")));
					
					if (player.getClan() != null) {
						if (player.getClan().getLeaderId() != player.getObjectId()) {
							if (player.getPowerGrade() == 0) {
								player.setPowerGrade(5);
							}
							player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
						} else {
							player.getClanPrivileges().setAll();
							player.setPowerGrade(1);
						}
						player.setPledgeClass(L2ClanMember.calculatePledgeClass(player));
					} else {
						if (player.isNoble()) {
							player.setPledgeClass(5);
						}
						
						if (player.isHero()) {
							player.setPledgeClass(8);
						}
						
						player.getClanPrivileges().clear();
					}
				}
			}
		} catch (Exception e) {
			LOG.error("Failed loading character. {}", e);
		}
		return player;
	}
	
	@Override
	public void loadCharacters(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var stmt = con.prepareStatement(SELECT_CHARACTERS)) {
			stmt.setString(1, player.getAccountName());
			stmt.setInt(2, player.getObjectId());
			try (var rs = stmt.executeQuery()) {
				while (rs.next()) {
					player.getAccountChars().put(rs.getInt("charId"), rs.getString("char_name"));
				}
			}
		} catch (Exception e) {
			LOG.error("Failed to load {} characters.", player, e);
		}
	}
	
	@Override
	public boolean insert(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(INSERT)) {
			ps.setString(1, player.getAccountName());
			ps.setInt(2, player.getObjectId());
			ps.setString(3, player.getName());
			ps.setInt(4, player.getBaseLevel());
			ps.setInt(5, player.getMaxHp());
			ps.setDouble(6, player.getCurrentHp());
			ps.setInt(7, player.getMaxCp());
			ps.setDouble(8, player.getCurrentCp());
			ps.setInt(9, player.getMaxMp());
			ps.setDouble(10, player.getCurrentMp());
			ps.setInt(11, player.getAppearance().getFace());
			ps.setInt(12, player.getAppearance().getHairStyle());
			ps.setInt(13, player.getAppearance().getHairColor());
			ps.setInt(14, player.getAppearance().getSex() ? 1 : 0);
			ps.setLong(15, player.getBaseExp());
			ps.setInt(16, player.getBaseSp());
			ps.setInt(17, player.getKarma());
			ps.setInt(18, player.getFame());
			ps.setInt(19, player.getPvpKills());
			ps.setInt(20, player.getPkKills());
			ps.setInt(21, player.getClanId());
			ps.setInt(22, player.getRace().ordinal());
			ps.setInt(23, player.getClassId().getId());
			ps.setLong(24, player.getDeleteTimer());
			ps.setInt(25, player.hasDwarvenCraft() ? 1 : 0);
			ps.setString(26, player.getTitle());
			ps.setInt(27, player.getAppearance().getTitleColor());
			ps.setInt(28, player.getAccessLevel().getLevel());
			ps.setInt(29, player.isOnlineInt());
			ps.setInt(30, player.isIn7sDungeon() ? 1 : 0);
			ps.setInt(31, player.getClanPrivileges().getBitmask());
			ps.setInt(32, player.getWantsPeace());
			ps.setInt(33, player.getBaseClass());
			ps.setInt(34, player.getNewbie());
			ps.setInt(35, player.isNoble() ? 1 : 0);
			ps.setLong(36, 0);
			ps.setTimestamp(37, new Timestamp(player.getCreateDate().getTimeInMillis()));
			ps.executeUpdate();
		} catch (Exception e) {
			LOG.error("Could not insert char data: {}", e);
			return false;
		}
		return true;
	}
	
	@Override
	public void storeCharBase(L2PcInstance player) {
		long totalOnlineTime = player.getOnlineTime();
		if (player.getOnlineBeginTime() > 0) {
			totalOnlineTime += MILLISECONDS.toSeconds(System.currentTimeMillis() - player.getOnlineBeginTime());
		}
		
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE)) {
			ps.setInt(1, player.getBaseLevel());
			ps.setInt(2, player.getMaxHp());
			ps.setDouble(3, player.getCurrentHp());
			ps.setInt(4, player.getMaxCp());
			ps.setDouble(5, player.getCurrentCp());
			ps.setInt(6, player.getMaxMp());
			ps.setDouble(7, player.getCurrentMp());
			ps.setInt(8, player.getAppearance().getFace());
			ps.setInt(9, player.getAppearance().getHairStyle());
			ps.setInt(10, player.getAppearance().getHairColor());
			ps.setInt(11, player.getAppearance().getSex() ? 1 : 0);
			ps.setInt(12, player.getHeading());
			ps.setInt(13, player.inObserverMode() ? player.getLastLocation().getX() : player.getX());
			ps.setInt(14, player.inObserverMode() ? player.getLastLocation().getY() : player.getY());
			ps.setInt(15, player.inObserverMode() ? player.getLastLocation().getZ() : player.getZ());
			ps.setLong(16, player.getBaseExp());
			ps.setLong(17, player.getExpBeforeDeath());
			ps.setInt(18, player.getBaseSp());
			ps.setInt(19, player.getKarma());
			ps.setInt(20, player.getFame());
			ps.setInt(21, player.getPvpKills());
			ps.setInt(22, player.getPkKills());
			ps.setInt(23, player.getClanId());
			ps.setInt(24, player.getRace().ordinal());
			ps.setInt(25, player.getClassId().getId());
			ps.setLong(26, player.getDeleteTimer());
			ps.setString(27, player.getTitle());
			ps.setInt(28, player.getAppearance().getTitleColor());
			ps.setInt(29, player.getAccessLevel().getLevel());
			ps.setInt(30, player.isOnlineInt());
			ps.setInt(31, player.isIn7sDungeon() ? 1 : 0);
			ps.setInt(32, player.getClanPrivileges().getBitmask());
			ps.setInt(33, player.getWantsPeace());
			ps.setInt(34, player.getBaseClass());
			ps.setLong(35, totalOnlineTime);
			ps.setInt(36, player.getNewbie());
			ps.setInt(37, player.isNoble() ? 1 : 0);
			ps.setInt(38, player.getPowerGrade());
			ps.setInt(39, player.getPledgeType());
			ps.setInt(40, player.getLvlJoinedAcademy());
			ps.setLong(41, player.getApprentice());
			ps.setLong(42, player.getSponsor());
			ps.setLong(43, player.getClanJoinExpiryTime());
			ps.setLong(44, player.getClanCreateExpiryTime());
			ps.setString(45, player.getName());
			ps.setLong(46, player.getDeathPenaltyBuffLevel());
			ps.setInt(47, player.getBookMarkSlot());
			ps.setInt(48, player.getVitalityPoints());
			ps.setString(49, player.getLang());
			ps.setInt(50, player.getObjectId());
			
			ps.execute();
		} catch (Exception e) {
			LOG.error("Could not store {} base data: {}", player, e);
		}
	}
	
	@Override
	public void updateOnlineStatus(L2PcInstance player) {
		try (var con = ConnectionFactory.getInstance().getConnection();
			var ps = con.prepareStatement(UPDATE_ONLINE)) {
			ps.setInt(1, player.isOnlineInt());
			ps.setLong(2, System.currentTimeMillis());
			ps.setInt(3, player.getObjectId());
			ps.execute();
		} catch (Exception e) {
			LOG.error("Failed updating character online status. {}", e);
		}
	}
}
