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
package com.l2jserver.gameserver.model.actor.instance;

import static com.l2jserver.gameserver.config.Configuration.character;
import static com.l2jserver.gameserver.config.Configuration.clan;
import static com.l2jserver.gameserver.config.Configuration.customs;
import static com.l2jserver.gameserver.config.Configuration.general;
import static com.l2jserver.gameserver.config.Configuration.pvp;
import static com.l2jserver.gameserver.config.Configuration.rates;
import static com.l2jserver.gameserver.config.Configuration.vitality;
import static com.l2jserver.gameserver.model.items.L2Item.SLOT_DECO;
import static com.l2jserver.gameserver.model.items.L2Item.SLOT_MULTI_ALLWEAPON;
import static com.l2jserver.gameserver.network.SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION;
import static com.l2jserver.gameserver.network.SystemMessageId.EQUIPMENT_S1_S2_REMOVED;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_DISARMED;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_EQUIPPED;
import static com.l2jserver.gameserver.network.SystemMessageId.S1_S2_EQUIPPED;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.util.Rnd;
import com.l2jserver.gameserver.GameTimeController;
import com.l2jserver.gameserver.GeoData;
import com.l2jserver.gameserver.ItemsAutoDestroy;
import com.l2jserver.gameserver.LoginServerThread;
import com.l2jserver.gameserver.RecipeController;
import com.l2jserver.gameserver.SevenSigns;
import com.l2jserver.gameserver.SevenSignsFestival;
import com.l2jserver.gameserver.ThreadPoolManager;
import com.l2jserver.gameserver.ai.CtrlIntention;
import com.l2jserver.gameserver.ai.L2CharacterAI;
import com.l2jserver.gameserver.ai.L2PlayerAI;
import com.l2jserver.gameserver.ai.L2SummonAI;
import com.l2jserver.gameserver.cache.WarehouseCacheManager;
import com.l2jserver.gameserver.communitybbs.BB.Forum;
import com.l2jserver.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2jserver.gameserver.dao.factory.impl.DAOFactory;
import com.l2jserver.gameserver.data.sql.impl.CharNameTable;
import com.l2jserver.gameserver.data.sql.impl.CharSummonTable;
import com.l2jserver.gameserver.data.xml.impl.AdminData;
import com.l2jserver.gameserver.data.xml.impl.EnchantSkillGroupsData;
import com.l2jserver.gameserver.data.xml.impl.FishData;
import com.l2jserver.gameserver.data.xml.impl.NpcData;
import com.l2jserver.gameserver.data.xml.impl.PetDataTable;
import com.l2jserver.gameserver.data.xml.impl.PlayerTemplateData;
import com.l2jserver.gameserver.data.xml.impl.PlayerXpPercentLostData;
import com.l2jserver.gameserver.data.xml.impl.SkillTreesData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.DuelState;
import com.l2jserver.gameserver.enums.HtmlActionScope;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.enums.MountType;
import com.l2jserver.gameserver.enums.PartyDistributionType;
import com.l2jserver.gameserver.enums.PlayerAction;
import com.l2jserver.gameserver.enums.PrivateStoreType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.enums.ShortcutType;
import com.l2jserver.gameserver.enums.ShotType;
import com.l2jserver.gameserver.enums.Team;
import com.l2jserver.gameserver.enums.audio.Music;
import com.l2jserver.gameserver.handler.IItemHandler;
import com.l2jserver.gameserver.handler.ItemHandler;
import com.l2jserver.gameserver.idfactory.IdFactory;
import com.l2jserver.gameserver.instancemanager.AntiFeedManager;
import com.l2jserver.gameserver.instancemanager.CastleManager;
import com.l2jserver.gameserver.instancemanager.CoupleManager;
import com.l2jserver.gameserver.instancemanager.CursedWeaponsManager;
import com.l2jserver.gameserver.instancemanager.DimensionalRiftManager;
import com.l2jserver.gameserver.instancemanager.DuelManager;
import com.l2jserver.gameserver.instancemanager.FortManager;
import com.l2jserver.gameserver.instancemanager.FortSiegeManager;
import com.l2jserver.gameserver.instancemanager.GrandBossManager;
import com.l2jserver.gameserver.instancemanager.HandysBlockCheckerManager;
import com.l2jserver.gameserver.instancemanager.InstanceManager;
import com.l2jserver.gameserver.instancemanager.ItemsOnGroundManager;
import com.l2jserver.gameserver.instancemanager.PunishmentManager;
import com.l2jserver.gameserver.instancemanager.QuestManager;
import com.l2jserver.gameserver.instancemanager.SiegeManager;
import com.l2jserver.gameserver.instancemanager.TerritoryWarManager;
import com.l2jserver.gameserver.instancemanager.ZoneManager;
import com.l2jserver.gameserver.model.ArenaParticipantsHolder;
import com.l2jserver.gameserver.model.BlockList;
import com.l2jserver.gameserver.model.ClanPrivilege;
import com.l2jserver.gameserver.model.L2AccessLevel;
import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.model.L2ContactList;
import com.l2jserver.gameserver.model.L2EnchantSkillLearn;
import com.l2jserver.gameserver.model.L2ManufactureItem;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2Party.messageType;
import com.l2jserver.gameserver.model.L2PetLevelData;
import com.l2jserver.gameserver.model.L2PremiumItem;
import com.l2jserver.gameserver.model.L2Radar;
import com.l2jserver.gameserver.model.L2RecipeList;
import com.l2jserver.gameserver.model.L2Request;
import com.l2jserver.gameserver.model.L2SkillLearn;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.L2WorldRegion;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.Macro;
import com.l2jserver.gameserver.model.MacroList;
import com.l2jserver.gameserver.model.PartyMatchRoom;
import com.l2jserver.gameserver.model.PartyMatchRoomList;
import com.l2jserver.gameserver.model.PartyMatchWaitingList;
import com.l2jserver.gameserver.model.PcCondOverride;
import com.l2jserver.gameserver.model.ShortCuts;
import com.l2jserver.gameserver.model.Shortcut;
import com.l2jserver.gameserver.model.TeleportBookmark;
import com.l2jserver.gameserver.model.TeleportWhereType;
import com.l2jserver.gameserver.model.TerritoryWard;
import com.l2jserver.gameserver.model.TradeList;
import com.l2jserver.gameserver.model.UIKeysSettings;
import com.l2jserver.gameserver.model.actor.L2Attackable;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Decoy;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Playable;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.L2Vehicle;
import com.l2jserver.gameserver.model.actor.appearance.PcAppearance;
import com.l2jserver.gameserver.model.actor.knownlist.PcKnownList;
import com.l2jserver.gameserver.model.actor.stat.PcStat;
import com.l2jserver.gameserver.model.actor.status.PcStatus;
import com.l2jserver.gameserver.model.actor.tasks.player.DismountTask;
import com.l2jserver.gameserver.model.actor.tasks.player.FameTask;
import com.l2jserver.gameserver.model.actor.tasks.player.GameGuardCheckTask;
import com.l2jserver.gameserver.model.actor.tasks.player.InventoryEnableTask;
import com.l2jserver.gameserver.model.actor.tasks.player.LookingForFishTask;
import com.l2jserver.gameserver.model.actor.tasks.player.PetFeedTask;
import com.l2jserver.gameserver.model.actor.tasks.player.PvPFlagTask;
import com.l2jserver.gameserver.model.actor.tasks.player.RecoBonusTaskEnd;
import com.l2jserver.gameserver.model.actor.tasks.player.RecoGiveTask;
import com.l2jserver.gameserver.model.actor.tasks.player.RentPetTask;
import com.l2jserver.gameserver.model.actor.tasks.player.ResetChargesTask;
import com.l2jserver.gameserver.model.actor.tasks.player.ResetSoulsTask;
import com.l2jserver.gameserver.model.actor.tasks.player.SitDownTask;
import com.l2jserver.gameserver.model.actor.tasks.player.StandUpTask;
import com.l2jserver.gameserver.model.actor.tasks.player.TeleportWatchdogTask;
import com.l2jserver.gameserver.model.actor.tasks.player.VitalityTask;
import com.l2jserver.gameserver.model.actor.tasks.player.WarnUserTakeBreakTask;
import com.l2jserver.gameserver.model.actor.tasks.player.WaterTask;
import com.l2jserver.gameserver.model.actor.templates.L2PcTemplate;
import com.l2jserver.gameserver.model.actor.transform.Transform;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.base.ClassLevel;
import com.l2jserver.gameserver.model.base.PlayerClass;
import com.l2jserver.gameserver.model.base.SubClass;
import com.l2jserver.gameserver.model.effects.EffectFlag;
import com.l2jserver.gameserver.model.effects.L2EffectType;
import com.l2jserver.gameserver.model.entity.Castle;
import com.l2jserver.gameserver.model.entity.Duel;
import com.l2jserver.gameserver.model.entity.Fort;
import com.l2jserver.gameserver.model.entity.Instance;
import com.l2jserver.gameserver.model.entity.L2Event;
import com.l2jserver.gameserver.model.entity.Siege;
import com.l2jserver.gameserver.model.entity.TvTEvent;
import com.l2jserver.gameserver.model.events.Containers;
import com.l2jserver.gameserver.model.events.EventDispatcher;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerEquipItem;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerFameChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerHennaRemove;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerKarmaChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogin;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLogout;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerPKChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerProfessionCancel;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerProfessionChange;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerPvPChanged;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerPvPKill;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerSit;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerStand;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerTransform;
import com.l2jserver.gameserver.model.events.returns.TerminateReturn;
import com.l2jserver.gameserver.model.fishing.L2Fish;
import com.l2jserver.gameserver.model.fishing.L2Fishing;
import com.l2jserver.gameserver.model.holders.AdditionalSkillHolder;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.model.holders.PlayerEventHolder;
import com.l2jserver.gameserver.model.holders.SkillHolder;
import com.l2jserver.gameserver.model.holders.SkillUseHolder;
import com.l2jserver.gameserver.model.interfaces.IEventListener;
import com.l2jserver.gameserver.model.interfaces.ILocational;
import com.l2jserver.gameserver.model.itemcontainer.Inventory;
import com.l2jserver.gameserver.model.itemcontainer.ItemContainer;
import com.l2jserver.gameserver.model.itemcontainer.PcFreight;
import com.l2jserver.gameserver.model.itemcontainer.PcInventory;
import com.l2jserver.gameserver.model.itemcontainer.PcRefund;
import com.l2jserver.gameserver.model.itemcontainer.PcWarehouse;
import com.l2jserver.gameserver.model.itemcontainer.PetInventory;
import com.l2jserver.gameserver.model.items.L2Armor;
import com.l2jserver.gameserver.model.items.L2EtcItem;
import com.l2jserver.gameserver.model.items.L2Henna;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.L2Weapon;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.items.type.ActionType;
import com.l2jserver.gameserver.model.items.type.ArmorType;
import com.l2jserver.gameserver.model.items.type.EtcItemType;
import com.l2jserver.gameserver.model.items.type.ItemType2;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.multisell.PreparedListContainer;
import com.l2jserver.gameserver.model.olympiad.OlympiadGameManager;
import com.l2jserver.gameserver.model.olympiad.OlympiadGameTask;
import com.l2jserver.gameserver.model.olympiad.OlympiadManager;
import com.l2jserver.gameserver.model.punishment.PunishmentAffect;
import com.l2jserver.gameserver.model.punishment.PunishmentType;
import com.l2jserver.gameserver.model.quest.Quest;
import com.l2jserver.gameserver.model.quest.QuestState;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.BuffInfo;
import com.l2jserver.gameserver.model.skills.CommonSkill;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.skills.targets.L2TargetType;
import com.l2jserver.gameserver.model.stats.BaseStats;
import com.l2jserver.gameserver.model.stats.Formulas;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.variables.AccountVariables;
import com.l2jserver.gameserver.model.variables.PlayerVariables;
import com.l2jserver.gameserver.model.zone.L2ZoneType;
import com.l2jserver.gameserver.model.zone.ZoneId;
import com.l2jserver.gameserver.model.zone.type.L2BossZone;
import com.l2jserver.gameserver.network.L2GameClient;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.AbstractHtmlPacket;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.ChangeWaitType;
import com.l2jserver.gameserver.network.serverpackets.CharInfo;
import com.l2jserver.gameserver.network.serverpackets.ConfirmDlg;
import com.l2jserver.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jserver.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.l2jserver.gameserver.network.serverpackets.ExDominionWarStart;
import com.l2jserver.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jserver.gameserver.network.serverpackets.ExFishingEnd;
import com.l2jserver.gameserver.network.serverpackets.ExFishingStart;
import com.l2jserver.gameserver.network.serverpackets.ExGetBookMarkInfoPacket;
import com.l2jserver.gameserver.network.serverpackets.ExGetOnAirShip;
import com.l2jserver.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jserver.gameserver.network.serverpackets.ExPrivateStoreSetWholeMsg;
import com.l2jserver.gameserver.network.serverpackets.ExSetCompassZoneCode;
import com.l2jserver.gameserver.network.serverpackets.ExStartScenePlayer;
import com.l2jserver.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jserver.gameserver.network.serverpackets.ExVoteSystemInfo;
import com.l2jserver.gameserver.network.serverpackets.FriendStatusPacket;
import com.l2jserver.gameserver.network.serverpackets.GameGuardQuery;
import com.l2jserver.gameserver.network.serverpackets.GetOnVehicle;
import com.l2jserver.gameserver.network.serverpackets.HennaInfo;
import com.l2jserver.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.ItemList;
import com.l2jserver.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jserver.gameserver.network.serverpackets.LeaveWorld;
import com.l2jserver.gameserver.network.serverpackets.MagicSkillUse;
import com.l2jserver.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jserver.gameserver.network.serverpackets.NicknameChanged;
import com.l2jserver.gameserver.network.serverpackets.ObservationMode;
import com.l2jserver.gameserver.network.serverpackets.ObservationReturn;
import com.l2jserver.gameserver.network.serverpackets.PartySmallWindowUpdate;
import com.l2jserver.gameserver.network.serverpackets.PetInventoryUpdate;
import com.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import com.l2jserver.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreListBuy;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreListSell;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreManageListBuy;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreManageListSell;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreMsgBuy;
import com.l2jserver.gameserver.network.serverpackets.PrivateStoreMsgSell;
import com.l2jserver.gameserver.network.serverpackets.RecipeShopMsg;
import com.l2jserver.gameserver.network.serverpackets.RecipeShopSellList;
import com.l2jserver.gameserver.network.serverpackets.RelationChanged;
import com.l2jserver.gameserver.network.serverpackets.Ride;
import com.l2jserver.gameserver.network.serverpackets.ServerClose;
import com.l2jserver.gameserver.network.serverpackets.SetupGauge;
import com.l2jserver.gameserver.network.serverpackets.ShortCutInit;
import com.l2jserver.gameserver.network.serverpackets.SkillCoolTime;
import com.l2jserver.gameserver.network.serverpackets.SkillList;
import com.l2jserver.gameserver.network.serverpackets.Snoop;
import com.l2jserver.gameserver.network.serverpackets.SocialAction;
import com.l2jserver.gameserver.network.serverpackets.StatusUpdate;
import com.l2jserver.gameserver.network.serverpackets.StopMove;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.network.serverpackets.TargetSelected;
import com.l2jserver.gameserver.network.serverpackets.TargetUnselected;
import com.l2jserver.gameserver.network.serverpackets.TradeDone;
import com.l2jserver.gameserver.network.serverpackets.TradeOtherDone;
import com.l2jserver.gameserver.network.serverpackets.TradeStart;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;
import com.l2jserver.gameserver.network.serverpackets.ValidateLocation;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.gameserver.util.EnumIntBitmask;
import com.l2jserver.gameserver.util.FloodProtectors;
import com.l2jserver.gameserver.util.Util;

/**
 * This class represents all player characters in the world.<br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 */
public final class L2PcInstance extends L2Playable {
	
	public static final int ID_NONE = -1;
	public static final int REQUEST_TIMEOUT = 15;
	private static final Logger LOG = LoggerFactory.getLogger(L2PcInstance.class);
	private static final String COND_OVERRIDE_KEY = "cond_override";
	// during fall validations will be disabled for 10 ms.
	private static final int FALLING_VALIDATION_DELAY = 10000;
	public final ReentrantLock soulShotLock = new ReentrantLock();
	private final Queue<IEventListener> _eventListeners = new ConcurrentLinkedQueue<>();
	private final String _accountName;
	private final ReentrantLock _subclassLock = new ReentrantLock();
	private final PcAppearance _appearance;
	private final L2ContactList _contactList = new L2ContactList(this);
	private final Map<Integer, TeleportBookmark> _tpbookmarks = new ConcurrentHashMap<>();
	/** The table containing all L2RecipeList of the L2PcInstance */
	private final Map<Integer, L2RecipeList> _dwarvenRecipeBook = new ConcurrentHashMap<>();
	private final Map<Integer, L2RecipeList> _commonRecipeBook = new ConcurrentHashMap<>();
	/** Premium Items */
	private final Map<Integer, L2PremiumItem> _premiumItems = new ConcurrentHashMap<>();
	/** Location before entering Observer Mode */
	private final Location _lastLoc = new Location(0, 0, 0);
	/** Stored from last ValidatePosition **/
	private final Location _lastServerPosition = new Location(0, 0, 0);
	private final PcInventory _inventory = new PcInventory(this);
	private final PcFreight _freight = new PcFreight(this);
	/** The table containing all Quests began by the L2PcInstance */
	private final Map<String, QuestState> _quests = new ConcurrentHashMap<>();
	/** The list containing all shortCuts of this player. */
	private final ShortCuts _shortCuts = new ShortCuts(this);
	/** The list containing all macros of this player. */
	private final MacroList _macros = new MacroList(this);
	private final Set<L2PcInstance> _snoopListener = ConcurrentHashMap.newKeySet(1);
	private final Set<L2PcInstance> _snoopedPlayer = ConcurrentHashMap.newKeySet(1);
	// TODO: This needs to be better integrated and saved/loaded
	private final L2Radar _radar;
	private final AtomicInteger _charges = new AtomicInteger();
	private final L2Request _request = new L2Request(this);
	private final Map<Integer, String> _chars = new LinkedHashMap<>();
	/** Player's cubics. */
	private final Map<Integer, L2CubicInstance> _cubics = new ConcurrentSkipListMap<>(); // TODO(Zoey76): This should be sorted in insert order.
	/** Active shots. */
	private final Set<Integer> _activeSoulShots = ConcurrentHashMap.newKeySet(1);
	/** new loto ticket **/
	private final int _loto[] = new int[5];
	/** new race ticket **/
	private final int _race[] = new int[2];
	private final BlockList _blockList = new BlockList(this);
	/** Last Html Npcs, 0 = last html was not bound to an npc */
	private final int[] _htmlActionOriginObjectIds = new int[HtmlActionScope.values().length];
	/** Bypass validations */
	@SuppressWarnings("unchecked")
	private final LinkedList<String>[] _htmlActionCaches = new LinkedList[HtmlActionScope.values().length];
	private volatile Set<Integer> _friends;
	private PartyDistributionType _partyDistributionType;
	private L2GameClient _client;
	private long _deleteTimer;
	private Calendar _createDate = Calendar.getInstance();
	private String _lang = null;
	private String _htmlPrefix = null;
	private volatile boolean _isOnline = false;
	private long _onlineTime;
	private long _onlineBeginTime;
	private long _lastAccess;
	private long _uptime;
	private int _baseClass;
	private int _activeClass;
	private int _classIndex = 0;
	/** data for mounted pets */
	private int _controlItemId;
	private L2PetLevelData _leveldata;
	private int _curFeed;
	private Future<?> _mountFeedTask;
	private ScheduledFuture<?> _dismountTask;
	private boolean _petItems = false;
	/** The list of sub-classes this character has. */
	private Map<Integer, SubClass> _subClasses;
	/** The Experience of the L2PcInstance before the last Death Penalty */
	private long _expBeforeDeath;
	/** The Karma of the L2PcInstance (if higher than 0, the name of the L2PcInstance appears in red) */
	private int _karma;
	/** The number of player killed during a PvP (the player killed was PvP Flagged) */
	private int _pvpKills;
	/** The PK counter of the L2PcInstance (= Number of non PvP Flagged player killed) */
	private int _pkKills;
	/** The PvP Flag state of the L2PcInstance (0=White, 1=Purple) */
	private byte _pvpFlag;
	/** The Fame of this L2PcInstance */
	private int _fame;
	private ScheduledFuture<?> _fameTask;
	/** Vitality recovery task */
	private ScheduledFuture<?> _vitalityTask;
	private volatile ScheduledFuture<?> _teleportWatchdog;
	/** The Siege state of the L2PcInstance */
	private byte _siegeState = 0;
	/** The id of castle/fort which the L2PcInstance is registered for siege */
	private int _siegeSide = 0;
	private int _curWeightPenalty = 0;
	private int _lastCompassZone; // the last compass zone update send to the client
	private boolean _isIn7sDungeon = false;
	private int _bookmarkslot = 0; // The Teleport Bookmark Slot
	private boolean _canFeed;
	private boolean _isInSiege;
	private boolean _isInHideoutSiege = false;
	/** Olympiad */
	private boolean _inOlympiadMode = false;
	private boolean _OlympiadStart = false;
	private int _olympiadGameId = -1;
	private int _olympiadSide = -1;
	/** Olympiad buff count. */
	private int _olyBuffsCount = 0;
	/** Duel */
	private DuelState _duelState = DuelState.NO_DUEL;
	private int _duelId = 0;
	/** Boat and AirShip */
	private L2Vehicle _vehicle = null;
	private Location _inVehiclePosition;
	private ScheduledFuture<?> _taskforfish;
	private MountType _mountType = MountType.NONE;
	private int _mountNpcId;
	private int _mountLevel;
	/** Store object used to summon the strider you are mounting **/
	private int _mountObjectID = 0;
	private int _telemode = 0;
	private boolean _inCrystallize;
	private boolean _inCraftMode;
	private long _offlineShopStart = 0;
	private Transform _transformation;
	private volatile Map<Integer, Skill> _transformSkills;
	/** True if the L2PcInstance is sitting */
	private boolean _waitTypeSitting;
	private boolean _observerMode = false;
	/** The number of recommendation obtained by the player. */
	private int _recomHave;
	/** The number of recommendation that the player can give. */
	private int _recomLeft;
	/** Recommendation Bonus task **/
	private ScheduledFuture<?> _recoBonusTask;
	/** Recommendation task **/
	private ScheduledFuture<?> _recoGiveTask;
	/** Recommendation Two Hours bonus **/
	private boolean _recoTwoHoursGiven = false;
	private PcWarehouse _warehouse;
	private PcRefund _refund;
	private PrivateStoreType _privateStoreType = PrivateStoreType.NONE;
	private TradeList _activeTradeList;
	private ItemContainer _activeWarehouse;
	private volatile Map<Integer, L2ManufactureItem> _manufactureItems;
	private String _storeName = "";
	private TradeList _sellList;
	private TradeList _buyList;
	// Multisell
	private PreparedListContainer _currentMultiSell = null;
	/** Bitmask used to keep track of one-time/newbie quest rewards */
	private int _newbie;
	private boolean _noble = false;
	private boolean _hero = false;
	/** The L2FolkInstance corresponding to the last Folk which one the player talked. */
	private L2Npc _lastFolkNpc = null;
	/** Last NPC Id talked on a quest */
	private int _questNpcObject = 0;
	private L2Henna[] _henna = new L2Henna[3];
	private int _hennaSTR;
	private int _hennaINT;
	private int _hennaDEX;
	private int _hennaMEN;
	private int _hennaWIT;
	private int _hennaCON;
	/** The L2Summon of the L2PcInstance */
	private L2Summon _summon = null;
	/** The L2Decoy of the L2PcInstance */
	private L2Decoy _decoy = null;
	/** The L2Trap of the L2PcInstance */
	private L2TrapInstance _trap = null;
	/** The L2Agathion of the L2PcInstance */
	private int _agathionId = 0;
	// apparently, a L2PcInstance CAN have both a summon AND a tamed beast at the same time!!
	// after Freya players can control more than one tamed beast
	private volatile Set<L2TamedBeastInstance> _tamedBeasts = null;
	private boolean _minimapAllowed = false;
	private int _partyroom = 0;
	/** The Clan Identifier of the L2PcInstance */
	private int _clanId;
	/** The Clan object of the L2PcInstance */
	private L2Clan _clan;
	/** Apprentice and Sponsor IDs */
	private int _apprentice = 0;
	private int _sponsor = 0;
	private long _clanJoinExpiryTime;
	private long _clanCreateExpiryTime;
	private int _powerGrade = 0;
	private volatile EnumIntBitmask<ClanPrivilege> _clanPrivileges = new EnumIntBitmask<>(ClanPrivilege.class, false);
	/** L2PcInstance's pledge class (knight, Baron, etc.) */
	private int _pledgeClass = 0;
	private int _pledgeType = 0;
	/** Level at which the player joined the clan as an academy member */
	private int _lvlJoinedAcademy = 0;
	private int _wantsPeace = 0;
	// Death Penalty Buff Level
	private int _deathPenaltyBuffLevel = 0;
	private volatile ScheduledFuture<?> _chargeTask = null;
	// Absorbed Souls
	private int _souls = 0;
	private ScheduledFuture<?> _soulTask = null;
	// WorldPosition used by TARGET_SIGNET_GROUND
	private Location _currentSkillWorldPosition;
	private L2AccessLevel _accessLevel;
	private boolean _messageRefusal = false; // message refusal mode
	private boolean _silenceMode = false; // silence mode
	private List<Integer> _silenceModeExcluded; // silence mode
	private boolean _dietMode = false; // ignore weight penalty
	private boolean _tradeRefusal = false; // Trade refusal
	private boolean _exchangeRefusal = false; // Exchange refusal
	private L2Party _party;
	// this is needed to find the inviting player for Party response
	// there can only be one active party request at once
	private L2PcInstance _activeRequester;
	private long _requestExpireTime = 0;
	private L2ItemInstance _arrowItem;
	private L2ItemInstance _boltItem;
	// Used for protection after teleport
	private long _protectEndTime = 0;
	private L2ItemInstance _lure = null;
	private long _teleportProtectEndTime = 0;
	// protects a char from aggro mobs when getting up from fake death
	private long _recentFakeDeathEndTime = 0;
	private boolean _isFakeDeath;
	/** The fists L2Weapon of the L2PcInstance (used when no weapon is equipped) */
	private L2Weapon _fistsWeaponItem;
	private int _expertiseArmorPenalty = 0;
	private int _expertiseWeaponPenalty = 0;
	private int _expertisePenaltyBonus = 0;
	private boolean _isEnchanting = false;
	private int _activeEnchantItemId = ID_NONE;
	private int _activeEnchantSupportItemId = ID_NONE;
	private int _activeEnchantAttrItemId = ID_NONE;
	private long _activeEnchantTimestamp = 0;
	private boolean _inventoryDisable = false;
	/** Event parameters */
	private PlayerEventHolder eventStatus = null;
	private byte _handysBlockCheckerEventArena = -1;
	private L2Fishing _fishCombat;
	private boolean _fishing = false;
	private int _fishx = 0;
	private int _fishy = 0;
	private int _fishz = 0;
	private ScheduledFuture<?> _taskRentPet;
	private ScheduledFuture<?> _taskWater;
	/**
	 * Origin of the last incoming html action request.<br>
	 * This can be used for HTMLs continuing the conversation with an npc.
	 */
	private int _lastHtmlActionOriginObjId;
	private Forum _forumMail;
	private Forum _forumMemo;
	
	/** Current skill in use. Note that L2Character has _lastSkillCast, but this has the button presses */
	private SkillUseHolder _currentSkill;
	private SkillUseHolder _currentPetSkill;
	
	/** Skills queued because a skill is already in progress */
	private SkillUseHolder _queuedSkill;
	
	private int _cursedWeaponEquippedId = 0;
	private boolean _combatFlagEquippedId = false;
	
	private boolean _canRevive = true;
	private int _reviveRequested = 0;
	private double _revivePower = 0;
	private int _reviveRecovery = 0;
	private boolean _revivePet = false;
	
	private double _cpUpdateIncCheck = .0;
	private double _cpUpdateDecCheck = .0;
	private double _cpUpdateInterval = .0;
	private double _mpUpdateIncCheck = .0;
	private double _mpUpdateDecCheck = .0;
	private double _mpUpdateInterval = .0;
	
	/** Char Coords from Client */
	private int _clientX;
	private int _clientY;
	private int _clientZ;
	private int _clientHeading;
	private volatile long _fallingTimestamp = 0;
	private int _multiSocialTarget = 0;
	private int _multiSociaAction = 0;
	private int _movieId = 0;
	private String _adminConfirmCmd = null;
	private volatile long _lastItemAuctionInfoRequest = 0;
	private Future<?> _PvPRegTask;
	private long _pvpFlagLasts;
	private long _notMoveUntil = 0;
	/** Map containing all custom skills of this player. */
	private Map<Integer, Skill> _customSkills = null;
	private volatile int _actionMask;
	private Map<Stats, Double> _servitorShare;
	// Character UI
	private UIKeysSettings _uiKeySettings;
	// L2JMOD Wedding
	private boolean _married = false;
	private int _partnerId = 0;
	private int _coupleId = 0;
	private boolean _engagerequest = false;
	private int _engageid = 0;
	private boolean _marryrequest = false;
	private boolean _marryaccepted = false;
	// Save responder name for log it
	private String _lastPetitionGmName = null;
	private boolean _hasCharmOfCourage = false;
	/** List of all QuestState instance that needs to be notified of this L2PcInstance's or its pet's death */
	private volatile Set<QuestState> _notifyQuestOfDeathList;
	/**
	 * Used for AltGameSkillLearn to set a custom skill learning class Id.
	 */
	private ClassId _learningClass = getClassId();
	private ScheduledFuture<?> _taskWarnUserTakeBreak;
	private L2Fish _fish;
	
	/**
	 * Creates a player.
	 * @param objectId the object ID
	 * @param classId the player's class ID
	 * @param accountName the account name
	 * @param app the player appearance
	 */
	public L2PcInstance(int objectId, int classId, String accountName, PcAppearance app) {
		super(objectId, PlayerTemplateData.getInstance().getTemplate(classId));
		setInstanceType(InstanceType.L2PcInstance);
		super.initCharStatusUpdateValues();
		initPcStatusUpdateValues();
		
		for (int i = 0; i < _htmlActionCaches.length; ++i) {
			_htmlActionCaches[i] = new LinkedList<>();
		}
		
		_accountName = accountName;
		app.setOwner(this);
		_appearance = app;
		
		// Create an AI
		getAI();
		
		// Create a L2Radar object
		_radar = new L2Radar(this);
		
		startVitalityTask();
		
		Formulas.addFuncsToNewPlayer(this);
	}
	
	/**
	 * Creates a player.
	 * @param classId the player class ID
	 * @param accountName the account name
	 * @param app the player appearance
	 */
	private L2PcInstance(int classId, String accountName, PcAppearance app) {
		this(IdFactory.getInstance().getNextId(), classId, accountName, app);
	}
	
	/**
	 * Create a new L2PcInstance and add it in the characters table of the database.<br>
	 * <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Create a new L2PcInstance with an account name</li>
	 * <li>Set the name, the Hair Style, the Hair Color and the Face type of the L2PcInstance</li>
	 * <li>Add the player in the characters table of the database</li>
	 * </ul>
	 * @param classId the player class ID
	 * @param accountName The name of the L2PcInstance
	 * @param name The name of the L2PcInstance
	 * @param app the player's appearance
	 * @return The L2PcInstance added to the database or null
	 */
	public static L2PcInstance create(int classId, String accountName, String name, PcAppearance app) {
		// Create a new L2PcInstance with an account name
		L2PcInstance player = new L2PcInstance(classId, accountName, app);
		// Set the name of the L2PcInstance
		player.setName(name);
		// Set Character's create time
		player.setCreateDate(Calendar.getInstance());
		// Set the base class ID to that of the actual class ID.
		player.setBaseClass(player.getClassId());
		// Kept for backwards compatibility.
		player.setNewbie(1);
		// Give 20 recommendations
		player.setRecomLeft(20);
		// Add the player in the characters table of the database
		return DAOFactory.getInstance().getPlayerDAO().insert(player) ? player : null;
	}
	
	/**
	 * Restores a player from the database.
	 * @param objectId the player's object ID
	 * @return the player
	 */
	public static L2PcInstance load(int objectId) {
		try {
			final L2PcInstance player = DAOFactory.getInstance().getPlayerDAO().load(objectId);
			if (player == null) {
				return null;
			}
			
			DAOFactory.getInstance().getPlayerDAO().loadCharacters(player);
			
			// Retrieve from the database all items of this L2PcInstance and add them to _inventory
			player.getInventory().restore();
			player.getFreight().restore();
			if (!general().warehouseCache()) {
				player.getWarehouse();
			}
			
			// Retrieve from the database all secondary data of this L2PcInstance
			// Note that Clan, Noblesse and Hero skills are given separately and not here.
			// Retrieve from the database all skills of this L2PcInstance and add them to _skills
			DAOFactory.getInstance().getSkillDAO().load(player);
			
			player._macros.restoreMe();
			
			player._shortCuts.restoreMe();
			
			DAOFactory.getInstance().getHennaDAO().load(player);
			
			DAOFactory.getInstance().getTeleportBookmarkDAO().load(player);
			
			DAOFactory.getInstance().getRecipeBookDAO().load(player, true);
			
			if (character().storeRecipeShopList()) {
				DAOFactory.getInstance().getRecipeShopListDAO().load(player);
			}
			
			DAOFactory.getInstance().getPremiumItemDAO().load(player);
			
			DAOFactory.getInstance().getItemDAO().loadPetInventory(player);
			
			// Reward auto-get skills and all available skills if auto-learn skills is true.
			player.rewardSkills();
			
			DAOFactory.getInstance().getItemReuseDAO().load(player);
			
			// Buff and status icons
			if (character().storeSkillCooltime()) {
				player.restoreEffects();
			}
			
			// Restore current CP, HP and MP values
			if (player.getCurrentHp() < 0.5) {
				player.setIsDead(true);
				player.stopHpMpRegeneration();
			}
			
			// Restore pet if exists in the world
			player.setPet(L2World.getInstance().getPet(player.getObjectId()));
			if (player.hasSummon()) {
				player.getSummon().setOwner(player);
			}
			
			// Update the overloaded status of the L2PcInstance
			player.refreshOverloaded();
			// Update the expertise status of the L2PcInstance
			player.refreshExpertisePenalty();
			
			DAOFactory.getInstance().getFriendDAO().load(player);
			
			if (character().storeUISettings()) {
				player.restoreUISettings();
			}
			
			if (player.isGM()) {
				final long masks = player.getVariables().getLong(COND_OVERRIDE_KEY, PcCondOverride.getAllExceptionsMask());
				player.setOverrideCond(masks);
			}
			return player;
		} catch (Exception e) {
			LOG.error("Failed loading character.", e);
		}
		return null;
	}
	
	public boolean isSpawnProtected() {
		return _protectEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public boolean isTeleportProtected() {
		return _teleportProtectEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	public long getPvpFlagLasts() {
		return _pvpFlagLasts;
	}
	
	public void setPvpFlagLasts(long time) {
		_pvpFlagLasts = time;
	}
	
	public void startPvPFlag() {
		updatePvPFlag(1);
		
		if (_PvPRegTask == null) {
			_PvPRegTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PvPFlagTask(this), 1000, 1000);
		}
	}
	
	public void stopPvpRegTask() {
		if (_PvPRegTask != null) {
			_PvPRegTask.cancel(true);
			_PvPRegTask = null;
		}
	}
	
	public void stopPvPFlag() {
		stopPvpRegTask();
		
		updatePvPFlag(0);
		
		_PvPRegTask = null;
	}
	
	public String getAccountName() {
		if (getClient() == null) {
			return getAccountNamePlayer();
		}
		return getClient().getAccountName();
	}
	
	public String getAccountNamePlayer() {
		return _accountName;
	}
	
	public Map<Integer, String> getAccountChars() {
		return _chars;
	}
	
	public int getRelation(L2PcInstance target) {
		int result = 0;
		
		if (getClan() != null) {
			result |= RelationChanged.RELATION_CLAN_MEMBER;
			if (getClan() == target.getClan()) {
				result |= RelationChanged.RELATION_CLAN_MATE;
			}
			if (getAllyId() != 0) {
				result |= RelationChanged.RELATION_ALLY_MEMBER;
			}
		}
		if (isClanLeader()) {
			result |= RelationChanged.RELATION_LEADER;
		}
		if ((getParty() != null) && (getParty() == target.getParty())) {
			result |= RelationChanged.RELATION_HAS_PARTY;
			for (int i = 0; i < getParty().getMembers().size(); i++) {
				if (getParty().getMembers().get(i) != this) {
					continue;
				}
				switch (i) {
					case 0:
						result |= RelationChanged.RELATION_PARTYLEADER; // 0x10
						break;
					case 1:
						result |= RelationChanged.RELATION_PARTY4; // 0x8
						break;
					case 2:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x7
						break;
					case 3:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY2; // 0x6
						break;
					case 4:
						result |= RelationChanged.RELATION_PARTY3 + RelationChanged.RELATION_PARTY1; // 0x5
						break;
					case 5:
						result |= RelationChanged.RELATION_PARTY3; // 0x4
						break;
					case 6:
						result |= RelationChanged.RELATION_PARTY2 + RelationChanged.RELATION_PARTY1; // 0x3
						break;
					case 7:
						result |= RelationChanged.RELATION_PARTY2; // 0x2
						break;
					case 8:
						result |= RelationChanged.RELATION_PARTY1; // 0x1
						break;
				}
			}
		}
		if (getSiegeState() != 0) {
			if (TerritoryWarManager.getInstance().getRegisteredTerritoryId(this) != 0) {
				result |= RelationChanged.RELATION_TERRITORY_WAR;
			} else {
				result |= RelationChanged.RELATION_INSIEGE;
				if (getSiegeState() != target.getSiegeState()) {
					result |= RelationChanged.RELATION_ENEMY;
				} else {
					result |= RelationChanged.RELATION_ALLY;
				}
				if (getSiegeState() == 1) {
					result |= RelationChanged.RELATION_ATTACKER;
				}
			}
		}
		if ((getClan() != null) && (target.getClan() != null)) {
			if ((target.getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && (getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && target.getClan().isAtWarWith(getClan().getId())) {
				result |= RelationChanged.RELATION_1SIDED_WAR;
				if (getClan().isAtWarWith(target.getClan().getId())) {
					result |= RelationChanged.RELATION_MUTUAL_WAR;
				}
			}
		}
		if (getBlockCheckerArena() != -1) {
			result |= RelationChanged.RELATION_INSIEGE;
			ArenaParticipantsHolder holder = HandysBlockCheckerManager.getInstance().getHolder(getBlockCheckerArena());
			if (holder.getPlayerTeam(this) == 0) {
				result |= RelationChanged.RELATION_ENEMY;
			} else {
				result |= RelationChanged.RELATION_ALLY;
			}
			result |= RelationChanged.RELATION_ATTACKER;
		}
		return result;
	}
	
	private void initPcStatusUpdateValues() {
		_cpUpdateInterval = getMaxCp() / 352.0;
		_cpUpdateIncCheck = getMaxCp();
		_cpUpdateDecCheck = getMaxCp() - _cpUpdateInterval;
		_mpUpdateInterval = getMaxMp() / 352.0;
		_mpUpdateIncCheck = getMaxMp();
		_mpUpdateDecCheck = getMaxMp() - _mpUpdateInterval;
	}
	
	@Override
	public final PcKnownList getKnownList() {
		return (PcKnownList) super.getKnownList();
	}
	
	@Override
	public void initKnownList() {
		setKnownList(new PcKnownList(this));
	}
	
	@Override
	public final PcStat getStat() {
		return (PcStat) super.getStat();
	}
	
	@Override
	public void initCharStat() {
		setStat(new PcStat(this));
	}
	
	@Override
	public final PcStatus getStatus() {
		return (PcStatus) super.getStatus();
	}
	
	@Override
	public void initCharStatus() {
		setStatus(new PcStatus(this));
	}
	
	public final PcAppearance getAppearance() {
		return _appearance;
	}
	
	/**
	 * @return the base L2PcTemplate link to the L2PcInstance.
	 */
	public final L2PcTemplate getBaseTemplate() {
		return PlayerTemplateData.getInstance().getTemplate(_baseClass);
	}
	
	/**
	 * @return the L2PcTemplate link to the L2PcInstance.
	 */
	@Override
	public final L2PcTemplate getTemplate() {
		return (L2PcTemplate) super.getTemplate();
	}
	
	/**
	 * @param newclass
	 */
	public void setTemplate(ClassId newclass) {
		super.setTemplate(PlayerTemplateData.getInstance().getTemplate(newclass));
	}
	
	@Override
	protected L2CharacterAI initAI() {
		return new L2PlayerAI(this);
	}
	
	/** Return the Level of the L2PcInstance. */
	@Override
	public final int getLevel() {
		if (isSubClassActive()) {
			return getSubClasses().get(getClassIndex()).getStat().getLevel();
		}
		return getStat().getLevel();
	}
	
	public int getBaseLevel() {
		return getStat().getLevel();
	}
	
	public long getBaseExp() {
		return getStat().getExp();
	}
	
	public int getBaseSp() {
		return getStat().getSp();
	}
	
	@Override
	public double getLevelMod() {
		if (isTransformed()) {
			double levelMod = getTransformation().getLevelMod(this);
			if (levelMod > -1) {
				return levelMod;
			}
		}
		return super.getLevelMod();
	}
	
	/**
	 * @return the _newbie rewards state of the L2PcInstance.
	 */
	public int getNewbie() {
		return _newbie;
	}
	
	/**
	 * Set the _newbie rewards state of the L2PcInstance.
	 * @param newbieRewards The Identifier of the _newbie state
	 */
	public void setNewbie(int newbieRewards) {
		_newbie = newbieRewards;
	}
	
	public void setBaseClass(int baseClass) {
		_baseClass = baseClass;
	}
	
	public boolean isInStoreMode() {
		return getPrivateStoreType() != PrivateStoreType.NONE;
	}
	
	public boolean isInCraftMode() {
		return _inCraftMode;
	}
	
	public void isInCraftMode(boolean b) {
		_inCraftMode = b;
	}
	
	/**
	 * Manage Logout Task:
	 * <ul>
	 * <li>Remove player from world</li>
	 * <li>Save player data into DB</li>
	 * </ul>
	 */
	public void logout() {
		logout(true);
	}
	
	/**
	 * Manage Logout Task:
	 * <ul>
	 * <li>Remove player from world</li>
	 * <li>Save player data into DB</li>
	 * </ul>
	 * @param closeClient
	 */
	public void logout(boolean closeClient) {
		try {
			closeNetConnection(closeClient);
		} catch (Exception e) {
			LOG.warn("Exception on logout(): {}", e);
		}
	}
	
	/**
	 * @return a table containing all Common L2RecipeList of the L2PcInstance.
	 */
	public L2RecipeList[] getCommonRecipeBook() {
		return _commonRecipeBook.values().toArray(new L2RecipeList[_commonRecipeBook.values().size()]);
	}
	
	/**
	 * @return a table containing all Dwarf L2RecipeList of the L2PcInstance.
	 */
	public L2RecipeList[] getDwarvenRecipeBook() {
		return _dwarvenRecipeBook.values().toArray(new L2RecipeList[_dwarvenRecipeBook.values().size()]);
	}
	
	/**
	 * Add a new L2RecipList to the table _commonrecipebook containing all L2RecipeList of the L2PcInstance
	 * @param recipe The L2RecipeList to add to the _recipebook
	 * @param saveToDb
	 */
	public void registerCommonRecipeList(L2RecipeList recipe, boolean saveToDb) {
		_commonRecipeBook.put(recipe.getId(), recipe);
		
		if (saveToDb) {
			DAOFactory.getInstance().getRecipeBookDAO().insert(this, recipe.getId(), false);
		}
	}
	
	/**
	 * Add a new L2RecipList to the table _recipebook containing all L2RecipeList of the L2PcInstance
	 * @param recipe The L2RecipeList to add to the _recipebook
	 * @param saveToDb
	 */
	public void registerDwarvenRecipeList(L2RecipeList recipe, boolean saveToDb) {
		_dwarvenRecipeBook.put(recipe.getId(), recipe);
		
		if (saveToDb) {
			DAOFactory.getInstance().getRecipeBookDAO().insert(this, recipe.getId(), true);
		}
	}
	
	/**
	 * @param recipeId The Identifier of the L2RecipeList to check in the player's recipe books
	 * @return {@code true}if player has the recipe on Common or Dwarven Recipe book else returns {@code false}
	 */
	public boolean hasRecipeList(int recipeId) {
		return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId);
	}
	
	/**
	 * Tries to remove a L2RecipList from the table _DwarvenRecipeBook or from table _CommonRecipeBook, those table contain all L2RecipeList of the L2PcInstance
	 * @param recipeId The Identifier of the L2RecipeList to remove from the _recipebook
	 */
	public void unregisterRecipeList(int recipeId) {
		if (_dwarvenRecipeBook.remove(recipeId) != null) {
			DAOFactory.getInstance().getRecipeBookDAO().delete(this, recipeId, true);
		} else if (_commonRecipeBook.remove(recipeId) != null) {
			DAOFactory.getInstance().getRecipeBookDAO().delete(this, recipeId, false);
		} else {
			LOG.warn("Attempted to remove unknown RecipeList: {}", recipeId);
		}
		
		for (Shortcut sc : getAllShortCuts()) {
			if ((sc != null) && (sc.getId() == recipeId) && (sc.getType() == ShortcutType.RECIPE)) {
				deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
	}
	
	/**
	 * @return the Id for the last talked quest NPC.
	 */
	public int getLastQuestNpcObject() {
		return _questNpcObject;
	}
	
	public void setLastQuestNpcObject(int npcId) {
		_questNpcObject = npcId;
	}
	
	/**
	 * @param quest The name of the quest
	 * @return the QuestState object corresponding to the quest name.
	 */
	public QuestState getQuestState(String quest) {
		return _quests.get(quest);
	}
	
	/**
	 * Add a QuestState to the table _quest containing all quests began by the L2PcInstance.
	 * @param qs The QuestState to add to _quest
	 */
	public void setQuestState(QuestState qs) {
		_quests.put(qs.getQuestName(), qs);
	}
	
	/**
	 * Verify if the player has the quest state.
	 * @param quest the quest state to check
	 * @return {@code true} if the player has the quest state, {@code false} otherwise
	 */
	public boolean hasQuestState(String quest) {
		return _quests.containsKey(quest);
	}
	
	/**
	 * Verify if this player has completed the given quest.
	 * @param quest to check if its completed or not.
	 * @return {@code true} if the player has completed the given quest, {@code false} otherwise.
	 */
	public boolean hasQuestCompleted(String quest) {
		final QuestState qs = _quests.get(quest);
		return (qs != null) && qs.isCompleted();
	}
	
	/**
	 * Remove a QuestState from the table _quest containing all quests began by the L2PcInstance.
	 * @param quest The name of the quest
	 */
	public void delQuestState(String quest) {
		_quests.remove(quest);
	}
	
	/**
	 * Gets all the active quests.
	 * @return a list of active quests
	 */
	public List<Quest> getAllActiveQuests() {
		final List<Quest> quests = new LinkedList<>();
		for (QuestState qs : _quests.values()) {
			if ((qs == null) || (qs.getQuest() == null) || (!qs.isStarted() && !general().developer())) {
				continue;
			}
			
			// Ignore other scripts.
			final int questId = qs.getQuest().getId();
			if ((questId > 19999) || (questId < 1)) {
				continue;
			}
			quests.add(qs.getQuest());
		}
		
		return quests;
	}
	
	public void processQuestEvent(String questName, String event) {
		final Quest quest = QuestManager.getInstance().getQuest(questName);
		if ((quest == null) || (event == null) || event.isEmpty()) {
			return;
		}
		
		if (getLastQuestNpcObject() > 0) {
			final L2Object object = L2World.getInstance().findObject(getLastQuestNpcObject());
			if (object.isNpc() && isInsideRadius(object, L2Npc.INTERACTION_DISTANCE, false, false)) {
				final L2Npc npc = (L2Npc) object;
				quest.notifyEvent(event, npc, this);
			}
		}
	}
	
	/**
	 * Add QuestState instance that is to be notified of L2PcInstance's death.
	 * @param qs The QuestState that subscribe to this event
	 */
	public void addNotifyQuestOfDeath(QuestState qs) {
		if (qs == null) {
			return;
		}
		
		if (!getNotifyQuestOfDeath().contains(qs)) {
			getNotifyQuestOfDeath().add(qs);
		}
	}
	
	/**
	 * Remove QuestState instance that is to be notified of L2PcInstance's death.
	 * @param qs The QuestState that subscribe to this event
	 */
	public void removeNotifyQuestOfDeath(QuestState qs) {
		if ((qs == null) || (_notifyQuestOfDeathList == null)) {
			return;
		}
		
		_notifyQuestOfDeathList.remove(qs);
	}
	
	/**
	 * Gets the quest states registered for notify of death of this player.
	 * @return the quest states
	 */
	public final Set<QuestState> getNotifyQuestOfDeath() {
		if (_notifyQuestOfDeathList == null) {
			synchronized (this) {
				if (_notifyQuestOfDeathList == null) {
					_notifyQuestOfDeathList = ConcurrentHashMap.newKeySet(1);
				}
			}
		}
		
		return _notifyQuestOfDeathList;
	}
	
	public final boolean isNotifyQuestOfDeathEmpty() {
		return (_notifyQuestOfDeathList == null) || _notifyQuestOfDeathList.isEmpty();
	}
	
	/**
	 * @return a table containing all L2ShortCut of the L2PcInstance.
	 */
	public Shortcut[] getAllShortCuts() {
		return _shortCuts.getAllShortCuts();
	}
	
	/**
	 * @param slot The slot in which the shortCuts is equipped
	 * @param page The page of shortCuts containing the slot
	 * @return the L2ShortCut of the L2PcInstance corresponding to the position (page-slot).
	 */
	public Shortcut getShortCut(int slot, int page) {
		return _shortCuts.getShortCut(slot, page);
	}
	
	/**
	 * Add a L2shortCut to the L2PcInstance _shortCuts
	 * @param shortcut
	 */
	public void registerShortCut(Shortcut shortcut) {
		_shortCuts.registerShortCut(shortcut);
	}
	
	/**
	 * Updates the shortcut bars with the new skill.
	 * @param skillId the skill Id to search and update.
	 * @param skillLevel the skill level to update.
	 */
	public void updateShortCuts(int skillId, int skillLevel) {
		_shortCuts.updateShortCuts(skillId, skillLevel);
	}
	
	/**
	 * Delete the L2ShortCut corresponding to the position (page-slot) from the L2PcInstance _shortCuts.
	 * @param slot
	 * @param page
	 */
	public void deleteShortCut(int slot, int page) {
		_shortCuts.deleteShortCut(slot, page);
	}
	
	/**
	 * @param macro the macro to add to this L2PcInstance.
	 */
	public void registerMacro(Macro macro) {
		_macros.registerMacro(macro);
	}
	
	/**
	 * @param id the macro Id to delete.
	 */
	public void deleteMacro(int id) {
		_macros.deleteMacro(id);
	}
	
	/**
	 * @return all L2Macro of the L2PcInstance.
	 */
	public MacroList getMacros() {
		return _macros;
	}
	
	/**
	 * Get the siege state of the L2PcInstance.
	 * @return 1 = attacker, 2 = defender, 0 = not involved
	 */
	@Override
	public byte getSiegeState() {
		return _siegeState;
	}
	
	/**
	 * Set the siege state of the L2PcInstance.
	 * @param siegeState 1 = attacker, 2 = defender, 0 = not involved
	 */
	public void setSiegeState(byte siegeState) {
		_siegeState = siegeState;
	}
	
	public boolean isRegisteredOnThisSiegeField(int val) {
		if ((_siegeSide != val) && ((_siegeSide < 81) || (_siegeSide > 89))) {
			return false;
		}
		return true;
	}
	
	@Override
	public int getSiegeSide() {
		return _siegeSide;
	}
	
	/**
	 * Set the siege Side of the L2PcInstance.
	 * @param val
	 */
	public void setSiegeSide(int val) {
		_siegeSide = val;
	}
	
	@Override
	public byte getPvpFlag() {
		return _pvpFlag;
	}
	
	/**
	 * Set the PvP Flag of the L2PcInstance.
	 * @param pvpFlag
	 */
	public void setPvpFlag(int pvpFlag) {
		_pvpFlag = (byte) pvpFlag;
	}
	
	@Override
	public void updatePvPFlag(int value) {
		if (getPvpFlag() == value) {
			return;
		}
		setPvpFlag(value);
		
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		
		// If this player has a pet update the pets pvp flag as well
		if (hasSummon()) {
			sendPacket(new RelationChanged(getSummon(), getRelation(this), false));
		}
		
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		
		for (L2PcInstance target : plrs) {
			target.sendPacket(new RelationChanged(this, getRelation(target), isAutoAttackable(target)));
			if (hasSummon()) {
				target.sendPacket(new RelationChanged(getSummon(), getRelation(target), isAutoAttackable(target)));
			}
		}
	}
	
	@Override
	public void revalidateZone(boolean force) {
		// Cannot validate if not in a world region (happens during teleport)
		if (getWorldRegion() == null) {
			return;
		}
		
		// This function is called too often from movement code
		if (force) {
			_zoneValidateCounter = 4;
		} else {
			_zoneValidateCounter--;
			if (_zoneValidateCounter < 0) {
				_zoneValidateCounter = 4;
			} else {
				return;
			}
		}
		
		getWorldRegion().revalidateZones(this);
		
		if (general().allowWater()) {
			checkWaterState();
		}
		
		if (isInsideZone(ZoneId.ALTERED)) {
			if (_lastCompassZone == ExSetCompassZoneCode.ALTEREDZONE) {
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.ALTEREDZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.ALTEREDZONE);
			sendPacket(cz);
		} else if (isInsideZone(ZoneId.SIEGE)) {
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2) {
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2);
			sendPacket(cz);
		} else if (isInsideZone(ZoneId.PVP)) {
			if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE) {
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PVPZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE);
			sendPacket(cz);
		} else if (isIn7sDungeon()) {
			if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE) {
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE);
			sendPacket(cz);
		} else if (isInsideZone(ZoneId.PEACE)) {
			if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE) {
				return;
			}
			_lastCompassZone = ExSetCompassZoneCode.PEACEZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE);
			sendPacket(cz);
		} else {
			if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE) {
				return;
			}
			if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2) {
				updatePvPStatus();
			}
			_lastCompassZone = ExSetCompassZoneCode.GENERALZONE;
			ExSetCompassZoneCode cz = new ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE);
			sendPacket(cz);
		}
	}
	
	/**
	 * @return True if the L2PcInstance can Craft Dwarven Recipes.
	 */
	public boolean hasDwarvenCraft() {
		return getSkillLevel(CommonSkill.CREATE_DWARVEN.getId()) >= 1;
	}
	
	public int getDwarvenCraft() {
		return getSkillLevel(CommonSkill.CREATE_DWARVEN.getId());
	}
	
	/**
	 * @return True if the L2PcInstance can Craft Dwarven Recipes.
	 */
	public boolean hasCommonCraft() {
		return getSkillLevel(CommonSkill.CREATE_COMMON.getId()) >= 1;
	}
	
	public int getCommonCraft() {
		return getSkillLevel(CommonSkill.CREATE_COMMON.getId());
	}
	
	/**
	 * @return the PK counter of the L2PcInstance.
	 */
	public int getPkKills() {
		return _pkKills;
	}
	
	/**
	 * Set the PK counter of the L2PcInstance.
	 * @param pkKills
	 */
	public void setPkKills(int pkKills) {
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPKChanged(this, _pkKills, pkKills), this);
		_pkKills = pkKills;
	}
	
	/**
	 * @return the _deleteTimer of the L2PcInstance.
	 */
	public long getDeleteTimer() {
		return _deleteTimer;
	}
	
	/**
	 * Set the _deleteTimer of the L2PcInstance.
	 * @param deleteTimer
	 */
	public void setDeleteTimer(long deleteTimer) {
		_deleteTimer = deleteTimer;
	}
	
	/**
	 * @return the number of recommendation obtained by the L2PcInstance.
	 */
	public int getRecomHave() {
		return _recomHave;
	}
	
	/**
	 * Set the number of recommendation obtained by the L2PcInstance (Max : 255).
	 * @param value
	 */
	public void setRecomHave(int value) {
		_recomHave = Math.min(Math.max(value, 0), 255);
	}
	
	/**
	 * Increment the number of recommendation obtained by the L2PcInstance (Max : 255).
	 */
	protected void incRecomHave() {
		if (_recomHave < 255) {
			_recomHave++;
		}
	}
	
	/**
	 * @return the number of recommendation that the L2PcInstance can give.
	 */
	public int getRecomLeft() {
		return _recomLeft;
	}
	
	/**
	 * Set the number of recommendation obtained by the L2PcInstance (Max : 255).
	 * @param value
	 */
	public void setRecomLeft(int value) {
		_recomLeft = Math.min(Math.max(value, 0), 255);
	}
	
	/**
	 * Increment the number of recommendation that the L2PcInstance can give.
	 */
	protected void decRecomLeft() {
		if (_recomLeft > 0) {
			_recomLeft--;
		}
	}
	
	public void giveRecom(L2PcInstance target) {
		target.incRecomHave();
		decRecomLeft();
	}
	
	public long getExpBeforeDeath() {
		return _expBeforeDeath;
	}
	
	/**
	 * Set the exp of the L2PcInstance before a death
	 * @param exp
	 */
	public void setExpBeforeDeath(long exp) {
		_expBeforeDeath = exp;
	}
	
	/**
	 * Return the Karma of the L2PcInstance.
	 */
	@Override
	public int getKarma() {
		return _karma;
	}
	
	/**
	 * Set the Karma of the L2PcInstance and send a Server->Client packet StatusUpdate (broadcast).
	 * @param karma
	 */
	public void setKarma(int karma) {
		// Notify to scripts.
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerKarmaChanged(this, getKarma(), karma), this);
		
		if (karma < 0) {
			karma = 0;
		}
		if ((_karma == 0) && (karma > 0)) {
			Collection<L2Object> objs = getKnownList().getKnownObjects().values();
			
			for (L2Object object : objs) {
				if (!(object instanceof L2GuardInstance)) {
					continue;
				}
				
				if (((L2GuardInstance) object).getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE) {
					((L2GuardInstance) object).getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null);
				}
			}
		} else if ((_karma > 0) && (karma == 0)) {
			// Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast)
			setKarmaFlag(0);
		}
		
		_karma = karma;
		broadcastKarma();
	}
	
	public int getExpertiseArmorPenalty() {
		return _expertiseArmorPenalty;
	}
	
	public int getExpertiseWeaponPenalty() {
		return _expertiseWeaponPenalty;
	}
	
	public int getExpertisePenaltyBonus() {
		return _expertisePenaltyBonus;
	}
	
	public void setExpertisePenaltyBonus(int bonus) {
		_expertisePenaltyBonus = bonus;
	}
	
	public int getWeightPenalty() {
		if (_dietMode) {
			return 0;
		}
		return _curWeightPenalty;
	}
	
	/**
	 * Update the overloaded status of the L2PcInstance.
	 */
	public void refreshOverloaded() {
		int maxLoad = getMaxLoad();
		if (maxLoad > 0) {
			long weightproc = (((getCurrentLoad() - getBonusWeightPenalty()) * 1000L) / getMaxLoad());
			int newWeightPenalty;
			if ((weightproc < 500) || _dietMode) {
				newWeightPenalty = 0;
			} else if (weightproc < 666) {
				newWeightPenalty = 1;
			} else if (weightproc < 800) {
				newWeightPenalty = 2;
			} else if (weightproc < 1000) {
				newWeightPenalty = 3;
			} else {
				newWeightPenalty = 4;
			}
			
			if (_curWeightPenalty != newWeightPenalty) {
				_curWeightPenalty = newWeightPenalty;
				if ((newWeightPenalty > 0) && !_dietMode) {
					addSkill(SkillData.getInstance().getSkill(4270, newWeightPenalty));
					setIsOverloaded(getCurrentLoad() > maxLoad);
				} else {
					removeSkill(getKnownSkill(4270), false, true);
					setIsOverloaded(false);
				}
				sendPacket(new UserInfo(this));
				sendPacket(new EtcStatusUpdate(this));
				broadcastPacket(new CharInfo(this));
				broadcastPacket(new ExBrExtraUserInfo(this));
			}
		}
	}
	
	public void refreshExpertisePenalty() {
		if (!character().expertisePenalty()) {
			return;
		}
		
		final int expertiseLevel = getExpertiseLevel();
		
		int armorPenalty = 0;
		int weaponPenalty = 0;
		int crystaltype;
		
		for (L2ItemInstance item : getInventory().getItems()) {
			if ((item != null) && item.isEquipped() && ((item.getItemType() != EtcItemType.ARROW) && (item.getItemType() != EtcItemType.BOLT))) {
				crystaltype = item.getItem().getCrystalType().getId();
				if (crystaltype > expertiseLevel) {
					if (item.isWeapon() && (crystaltype > weaponPenalty)) {
						weaponPenalty = crystaltype;
					} else if (crystaltype > armorPenalty) {
						armorPenalty = crystaltype;
					}
				}
			}
		}
		
		boolean changed = false;
		final int bonus = getExpertisePenaltyBonus();
		
		// calc weapon penalty
		weaponPenalty = weaponPenalty - expertiseLevel - bonus;
		weaponPenalty = Math.min(Math.max(weaponPenalty, 0), 4);
		
		if ((getExpertiseWeaponPenalty() != weaponPenalty) || (getSkillLevel(CommonSkill.WEAPON_GRADE_PENALTY.getId()) != weaponPenalty)) {
			_expertiseWeaponPenalty = weaponPenalty;
			if (_expertiseWeaponPenalty > 0) {
				addSkill(SkillData.getInstance().getSkill(CommonSkill.WEAPON_GRADE_PENALTY.getId(), _expertiseWeaponPenalty));
			} else {
				removeSkill(getKnownSkill(CommonSkill.WEAPON_GRADE_PENALTY.getId()), false, true);
			}
			changed = true;
		}
		
		// calc armor penalty
		armorPenalty = armorPenalty - expertiseLevel - bonus;
		armorPenalty = Math.min(Math.max(armorPenalty, 0), 4);
		
		if ((getExpertiseArmorPenalty() != armorPenalty) || (getSkillLevel(CommonSkill.ARMOR_GRADE_PENALTY.getId()) != armorPenalty)) {
			_expertiseArmorPenalty = armorPenalty;
			if (_expertiseArmorPenalty > 0) {
				addSkill(SkillData.getInstance().getSkill(CommonSkill.ARMOR_GRADE_PENALTY.getId(), _expertiseArmorPenalty));
			} else {
				removeSkill(getKnownSkill(CommonSkill.ARMOR_GRADE_PENALTY.getId()), false, true);
			}
			changed = true;
		}
		
		if (changed) {
			sendPacket(new EtcStatusUpdate(this));
		}
	}
	
	public void useEquippableItem(int objectId, boolean abortAttack) {
		final var item = getInventory().getItemByObjectId(objectId);
		if (item == null) {
			return;
		}
		
		L2ItemInstance[] items = null;
		final boolean isEquiped = item.isEquipped();
		final int oldInvLimit = getInventoryLimit();
		SystemMessage sm = null;
		
		if (isEquiped) {
			if (item.getEnchantLevel() > 0) {
				sm = SystemMessage.getSystemMessage(EQUIPMENT_S1_S2_REMOVED);
				sm.addInt(item.getEnchantLevel());
				sm.addItemName(item);
			} else {
				sm = SystemMessage.getSystemMessage(S1_DISARMED);
				sm.addItemName(item);
			}
			sendPacket(sm);
			
			int slot = getInventory().getSlotFromItem(item);
			// we can't unequip talisman by body slot
			if (slot == SLOT_DECO) {
				items = getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot());
			} else {
				items = getInventory().unEquipItemInBodySlotAndRecord(slot);
			}
		} else {
			items = getInventory().equipItemAndRecord(item);
			
			if (item.isEquipped()) {
				if (item.getEnchantLevel() > 0) {
					sm = SystemMessage.getSystemMessage(S1_S2_EQUIPPED);
					sm.addInt(item.getEnchantLevel());
					sm.addItemName(item);
				} else {
					sm = SystemMessage.getSystemMessage(S1_EQUIPPED);
					sm.addItemName(item);
				}
				sendPacket(sm);
				
				// Consume mana - will start a task if required; returns if item is not a shadow item
				item.decreaseMana(false);
				
				if ((item.getItem().getBodyPart() & SLOT_MULTI_ALLWEAPON) != 0) {
					rechargeShots(true, true);
				}
			} else {
				sendPacket(CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION);
			}
		}
		
		refreshExpertisePenalty();
		
		broadcastUserInfo();
		
		final InventoryUpdate iu = new InventoryUpdate();
		iu.addItems(Arrays.asList(items));
		sendPacket(iu);
		
		if (abortAttack) {
			abortAttack();
		}
		
		if (getInventoryLimit() != oldInvLimit) {
			sendPacket(new ExStorageMaxCount(this));
		}
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerEquipItem(this, item), this);
	}
	
	/**
	 * @return the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).
	 */
	public int getPvpKills() {
		return _pvpKills;
	}
	
	/**
	 * Set the the PvP Kills of the L2PcInstance (Number of player killed during a PvP).
	 * @param pvpKills
	 */
	public void setPvpKills(int pvpKills) {
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPvPChanged(this, _pvpKills, pvpKills), this);
		_pvpKills = pvpKills;
	}
	
	/**
	 * @return the Fame of this L2PcInstance
	 */
	public int getFame() {
		return _fame;
	}
	
	/**
	 * Set the Fame of this L2PcInstane
	 * @param fame
	 */
	public void setFame(int fame) {
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerFameChanged(this, _fame, fame), this);
		_fame = (fame > character().getMaxPersonalFamePoints()) ? character().getMaxPersonalFamePoints() : fame;
	}
	
	/**
	 * @return the ClassId object of the L2PcInstance contained in L2PcTemplate.
	 */
	public ClassId getClassId() {
		return getTemplate().getClassId();
	}
	
	/**
	 * Set the template of the L2PcInstance.
	 * @param Id The Identifier of the L2PcTemplate to set to the L2PcInstance
	 */
	public void setClassId(int Id) {
		if (!_subclassLock.tryLock()) {
			return;
		}
		
		try {
			if ((getLvlJoinedAcademy() != 0) && (_clan != null) && (PlayerClass.values()[Id].getLevel() == ClassLevel.Third)) {
				if (getLvlJoinedAcademy() <= 16) {
					_clan.addReputationScore(clan().getCompleteAcademyMaxPoints(), true);
				} else if (getLvlJoinedAcademy() >= 39) {
					_clan.addReputationScore(clan().getCompleteAcademyMinPoints(), true);
				} else {
					_clan.addReputationScore((clan().getCompleteAcademyMaxPoints() - ((getLvlJoinedAcademy() - 16) * 20)), true);
				}
				setLvlJoinedAcademy(0);
				// oust pledge member from the academy, cuz he has finished his 2nd class transfer
				SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED);
				msg.addPcName(this);
				_clan.broadcastToOnlineMembers(msg);
				_clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(getName()));
				_clan.removeClanMember(getObjectId(), 0);
				sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED);
				
				// receive graduation gift
				getInventory().addItem("Gift", 8181, 1, this, null); // give academy circlet
			}
			if (isSubClassActive()) {
				getSubClasses().get(_classIndex).setClassId(Id);
			}
			setTarget(this);
			broadcastPacket(new MagicSkillUse(this, 5103, 1, 1000, 0));
			setClassTemplate(Id);
			if (getClassId().level() == 3) {
				sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER);
			} else {
				sendPacket(SystemMessageId.CLASS_TRANSFER);
			}
			
			// Update class icon in party and clan
			if (isInParty()) {
				getParty().broadcastPacket(new PartySmallWindowUpdate(this));
			}
			
			if (getClan() != null) {
				getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
			}
			
			// Add AutoGet skills and normal skills and/or learnByFS depending on configurations.
			rewardSkills();
			
			if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && character().decreaseSkillOnDelevel()) {
				checkPlayerSkills();
			}
		} finally {
			_subclassLock.unlock();
		}
	}
	
	/**
	 * @return the custom skill learning class Id.
	 */
	public ClassId getLearningClass() {
		return _learningClass;
	}
	
	/**
	 * @param learningClass the custom skill learning class Id to set.
	 */
	public void setLearningClass(ClassId learningClass) {
		_learningClass = learningClass;
	}
	
	/**
	 * @return the Experience of the L2PcInstance.
	 */
	@Override
	public long getExp() {
		if (isSubClassActive()) {
			return getSubClasses().get(getClassIndex()).getStat().getExp();
		}
		return getStat().getExp();
	}
	
	/**
	 * Set the Experience value of the L2PcInstance.
	 * @param exp
	 */
	public void setExp(long exp) {
		if (exp < 0) {
			LOG.warn("For player {} is set negative amount of exp [{}]", this, exp, new IllegalArgumentException());
			exp = 0;
		}
		getSubStat().setExp(exp);
	}
	
	public void setLevel(int level) {
		getSubStat().setLevel(Math.min(level, getMaxLevel()));
	}
	
	public final int getMaxLevel() {
		return getSubStat().getMaxLevel();
	}
	
	public final int getMaxExpLevel() {
		return getSubStat().getMaxExpLevel();
	}
	
	private final PcStat getSubStat() {
		return isSubClassActive() ? getSubClasses().get(getClassIndex()).getStat() : getStat();
	}
	
	@Override
	public final boolean addLevel(int value) {
		if ((getLevel() + value) > getMaxLevel()) {
			return false;
		}
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLevelChanged(this, getLevel(), getLevel() + value), this);
		
		boolean levelIncreased = getSubStat().addLevel(value);
		onLevelChange(levelIncreased);
		
		return levelIncreased;
	}
	
	@Override
	public void onLevelChange(boolean levelIncreased) {
		if (levelIncreased) {
			setCurrentCp(getMaxCp());
			setCurrentHp(getMaxHp());
			setCurrentMp(getMaxMp());
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
			sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL);
		} else {
			if (!isGM() && character().decreaseSkillOnDelevel()) {
				checkPlayerSkills();
			}
		}
		
		// Give AutoGet skills and all normal skills if Auto-Learn is activated.
		rewardSkills();
		
		if (getClan() != null) {
			getClan().updateClanMember(this);
			getClan().broadcastToOnlineMembers(new PledgeShowMemberListUpdate(this));
		}
		if (isInParty()) {
			getParty().recalculatePartyLevel(); // Recalculate the party level
		}
		
		if (isTransformed() || isInStance()) {
			getTransformation().onLevelUp(this);
		}
		
		// Synchronize level with pet if possible.
		if (hasPet()) {
			final L2PetInstance pet = (L2PetInstance) getSummon();
			if (pet.getPetData().isSynchLevel() && (pet.getLevel() != getLevel())) {
				pet.getStat().setLevel(getLevel());
				pet.getStat().getExpForLevel(getLevel());
				pet.setCurrentHp(pet.getMaxHp());
				pet.setCurrentMp(pet.getMaxMp());
				pet.broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
				pet.updateAndBroadcastStatus(1);
			}
		}
		
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.LEVEL, getLevel());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		sendPacket(su);
		
		// Update the overloaded status of the L2PcInstance
		refreshOverloaded();
		// Update the expertise status of the L2PcInstance
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to the L2PcInstance
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		sendPacket(new ExVoteSystemInfo(this));
	}
	
	public int getActiveEnchantAttrItemId() {
		return _activeEnchantAttrItemId;
	}
	
	public void setActiveEnchantAttrItemId(int objectId) {
		_activeEnchantAttrItemId = objectId;
	}
	
	public int getActiveEnchantItemId() {
		return _activeEnchantItemId;
	}
	
	public void setActiveEnchantItemId(int objectId) {
		// If we don't have a Enchant Item, we are not enchanting.
		if (objectId == ID_NONE) {
			setActiveEnchantSupportItemId(ID_NONE);
			setActiveEnchantTimestamp(0);
			setIsEnchanting(false);
		}
		_activeEnchantItemId = objectId;
	}
	
	public int getActiveEnchantSupportItemId() {
		return _activeEnchantSupportItemId;
	}
	
	public void setActiveEnchantSupportItemId(int objectId) {
		_activeEnchantSupportItemId = objectId;
	}
	
	public long getActiveEnchantTimestamp() {
		return _activeEnchantTimestamp;
	}
	
	public void setActiveEnchantTimestamp(long val) {
		_activeEnchantTimestamp = val;
	}
	
	public void setIsEnchanting(boolean val) {
		_isEnchanting = val;
	}
	
	public boolean isEnchanting() {
		return _isEnchanting;
	}
	
	/**
	 * @return the fists weapon of the L2PcInstance (used when no weapon is equipped).
	 */
	public L2Weapon getFistsWeaponItem() {
		return _fistsWeaponItem;
	}
	
	/**
	 * Set the fists weapon of the L2PcInstance (used when no weapon is equiped).
	 * @param weaponItem The fists L2Weapon to set to the L2PcInstance
	 */
	public void setFistsWeaponItem(L2Weapon weaponItem) {
		_fistsWeaponItem = weaponItem;
	}
	
	/**
	 * @param classId
	 * @return the fists weapon of the L2PcInstance Class (used when no weapon is equipped).
	 */
	public L2Weapon findFistsWeaponItem(int classId) {
		L2Weapon weaponItem = null;
		if ((classId >= 0x00) && (classId <= 0x09)) {
			// human fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(246);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x0a) && (classId <= 0x11)) {
			// human mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(251);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x12) && (classId <= 0x18)) {
			// elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(244);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x19) && (classId <= 0x1e)) {
			// elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(249);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x1f) && (classId <= 0x25)) {
			// dark elven fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(245);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x26) && (classId <= 0x2b)) {
			// dark elven mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(250);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x2c) && (classId <= 0x30)) {
			// orc fighter fists
			L2Item temp = ItemTable.getInstance().getTemplate(248);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x31) && (classId <= 0x34)) {
			// orc mage fists
			L2Item temp = ItemTable.getInstance().getTemplate(252);
			weaponItem = (L2Weapon) temp;
		} else if ((classId >= 0x35) && (classId <= 0x39)) {
			// dwarven fists
			L2Item temp = ItemTable.getInstance().getTemplate(247);
			weaponItem = (L2Weapon) temp;
		}
		
		return weaponItem;
	}
	
	/**
	 * This method reward all AutoGet skills and Normal skills if Auto-Learn configuration is true.
	 */
	public void rewardSkills() {
		// Give all normal skills if activated Auto-Learn is activated, included AutoGet skills.
		if (character().autoLearnSkills()) {
			giveAvailableSkills(character().autoLearnForgottenScrollSkills(), true);
		} else {
			giveAvailableAutoGetSkills();
		}
		
		checkPlayerSkills();
		checkItemRestriction();
		sendSkillList();
	}
	
	/**
	 * Re-give all skills which aren't saved to database, like Noble, Hero, Clan Skills.<br>
	 */
	public void regiveTemporarySkills() {
		// Do not call this on enterworld or char load
		
		// Add noble skills if noble
		if (isNoble()) {
			setNoble(true);
		}
		
		// Add Hero skills if hero
		if (isHero()) {
			setHero(true);
		}
		
		// Add clan skills
		if (getClan() != null) {
			L2Clan clan = getClan();
			clan.addSkillEffects(this);
			
			if ((clan.getLevel() >= SiegeManager.getInstance().getSiegeClanMinLevel()) && isClanLeader()) {
				SiegeManager.getInstance().addSiegeSkills(this);
			}
			if (getClan().getCastleId() > 0) {
				CastleManager.getInstance().getCastleByOwner(getClan()).giveResidentialSkills(this);
			}
			if (getClan().getFortId() > 0) {
				FortManager.getInstance().getFortByOwner(getClan()).giveResidentialSkills(this);
			}
		}
		
		// Reload passive skills from armors / jewels / weapons
		getInventory().reloadEquippedItems();
		
		// Add Death Penalty Buff Level
		restoreDeathPenaltyBuffLevel();
	}
	
	/**
	 * Give all available skills to the player.
	 * @param includedByFs if {@code true} forgotten scroll skills present in the skill tree will be added
	 * @param includeAutoGet if {@code true} auto-get skills present in the skill tree will be added
	 * @return the amount of new skills earned
	 */
	public int giveAvailableSkills(boolean includedByFs, boolean includeAutoGet) {
		int skillCounter = 0;
		// Get available skills
		Collection<Skill> skills = SkillTreesData.getInstance().getAllAvailableSkills(this, getClassId(), includedByFs, includeAutoGet);
		List<Skill> skillsForStore = new ArrayList<>();
		
		for (Skill sk : skills) {
			if (getKnownSkill(sk.getId()) == sk) {
				continue;
			}
			
			if (getSkillLevel(sk.getId()) == -1) {
				skillCounter++;
			}
			
			// fix when learning toggle skills
			if (sk.isToggle() && isAffectedBySkill(sk.getId())) {
				stopSkillEffects(true, sk.getId());
			}
			
			addSkill(sk, false);
			skillsForStore.add(sk);
		}
		
		DAOFactory.getInstance().getSkillDAO().insert(this, -1, skillsForStore);
		
		if (character().autoLearnSkills() && (skillCounter > 0)) {
			sendMessage("You have learned " + skillCounter + " new skills.");
		}
		return skillCounter;
	}
	
	/**
	 * Give all available auto-get skills to the player.
	 */
	public void giveAvailableAutoGetSkills() {
		// Get available skills
		final List<L2SkillLearn> autoGetSkills = SkillTreesData.getInstance().getAvailableAutoGetSkills(this);
		final SkillData st = SkillData.getInstance();
		Skill skill;
		for (L2SkillLearn s : autoGetSkills) {
			skill = st.getSkill(s.getSkillId(), s.getSkillLevel());
			if (skill != null) {
				addSkill(skill, true);
			} else {
				LOG.warn("Skipping null auto-get skill for player: {}", this);
			}
		}
	}
	
	/**
	 * @return the Race object of the L2PcInstance.
	 */
	@Override
	public Race getRace() {
		if (!isSubClassActive()) {
			return getTemplate().getRace();
		}
		return PlayerTemplateData.getInstance().getTemplate(_baseClass).getRace();
	}
	
	public L2Radar getRadar() {
		return _radar;
	}
	
	/* Return true if Hellbound minimap allowed */
	public boolean isMinimapAllowed() {
		return _minimapAllowed;
	}
	
	/* Enable or disable minimap on Hellbound */
	public void setMinimapAllowed(boolean b) {
		_minimapAllowed = b;
	}
	
	public void addSp(int sp) {
		getSubStat().addSp(sp);
	}
	
	@Override
	public int getSp() {
		return isSubClassActive() ? getSubClasses().get(getClassIndex()).getStat().getSp() : getStat().getSp();
	}
	
	/**
	 * Set the SP amount of the L2PcInstance.
	 * @param sp
	 */
	public void setSp(int sp) {
		if (sp < 0) {
			sp = 0;
		}
		if (isSubClassActive()) {
			getSubClasses().get(getClassIndex()).getStat().setSp(sp);
		} else {
			super.getStat().setSp(sp);
		}
	}
	
	/**
	 * @param castleId
	 * @return true if this L2PcInstance is a clan leader in ownership of the passed castle
	 */
	public boolean isCastleLord(int castleId) {
		L2Clan clan = getClan();
		
		// player has clan and is the clan leader, check the castle info
		if ((clan != null) && (clan.getLeader().getPlayerInstance() == this)) {
			// if the clan has a castle and it is actually the queried castle, return true
			Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
			if ((castle != null) && (castle == CastleManager.getInstance().getCastleById(castleId))) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * @return the Clan Identifier of the L2PcInstance.
	 */
	@Override
	public int getClanId() {
		return _clanId;
	}
	
	/**
	 * @return the Clan Crest Identifier of the L2PcInstance or 0.
	 */
	public int getClanCrestId() {
		if (_clan != null) {
			return _clan.getCrestId();
		}
		
		return 0;
	}
	
	/**
	 * @return The Clan CrestLarge Identifier or 0
	 */
	public int getClanCrestLargeId() {
		if ((_clan != null) && ((_clan.getCastleId() != 0) || (_clan.getHideoutId() != 0))) {
			return _clan.getCrestLargeId();
		}
		return 0;
	}
	
	public long getClanJoinExpiryTime() {
		return _clanJoinExpiryTime;
	}
	
	public void setClanJoinExpiryTime(long time) {
		_clanJoinExpiryTime = time;
	}
	
	public long getClanCreateExpiryTime() {
		return _clanCreateExpiryTime;
	}
	
	public void setClanCreateExpiryTime(long time) {
		_clanCreateExpiryTime = time;
	}
	
	/**
	 * Return the PcInventory Inventory of the L2PcInstance contained in _inventory.
	 */
	@Override
	public PcInventory getInventory() {
		return _inventory;
	}
	
	/**
	 * Delete a ShortCut of the L2PcInstance _shortCuts.
	 * @param objectId
	 */
	public void removeItemFromShortCut(int objectId) {
		_shortCuts.deleteShortCutByObjectId(objectId);
	}
	
	/**
	 * @return True if the L2PcInstance is sitting.
	 */
	public boolean isSitting() {
		return _waitTypeSitting;
	}
	
	/**
	 * Set _waitTypeSitting to given value
	 * @param state
	 */
	public void setIsSitting(boolean state) {
		_waitTypeSitting = state;
	}
	
	/**
	 * Sit down the L2PcInstance, set the AI Intention to AI_INTENTION_REST and send a Server->Client ChangeWaitType packet (broadcast)
	 */
	public void sitDown() {
		sitDown(true);
	}
	
	public void sitDown(boolean checkCast) {
		final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerSit(this), this, TerminateReturn.class);
		if ((terminate != null) && terminate.terminate()) {
			return;
		}
		
		if (checkCast && isCastingNow()) {
			return;
		}
		
		if (!_waitTypeSitting && !isAttackingDisabled() && !isOutOfControl() && !isImmobilized()) {
			breakAttack();
			setIsSitting(true);
			getAI().setIntention(CtrlIntention.AI_INTENTION_REST);
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_SITTING));
			// Schedule a sit down task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new SitDownTask(this), 2500);
			setIsParalyzed(true);
		}
	}
	
	/**
	 * Stand up the L2PcInstance, set the AI Intention to AI_INTENTION_IDLE and send a Server->Client ChangeWaitType packet (broadcast)
	 */
	public void standUp() {
		final TerminateReturn terminate = EventDispatcher.getInstance().notifyEvent(new OnPlayerStand(this), Containers.Players(), TerminateReturn.class);
		if ((terminate != null) && terminate.terminate()) {
			return;
		}
		
		if (_waitTypeSitting && !isInStoreMode() && !isAlikeDead()) {
			if (getEffectList().isAffected(EffectFlag.RELAXING)) {
				stopEffects(L2EffectType.RELAXING);
			}
			
			broadcastPacket(new ChangeWaitType(this, ChangeWaitType.WT_STANDING));
			// Schedule a stand up task to wait for the animation to finish
			ThreadPoolManager.getInstance().scheduleGeneral(new StandUpTask(this), 2500);
		}
	}
	
	/**
	 * @return the PcWarehouse object of the L2PcInstance.
	 */
	public PcWarehouse getWarehouse() {
		if (_warehouse == null) {
			_warehouse = new PcWarehouse(this);
			_warehouse.restore();
		}
		if (general().warehouseCache()) {
			WarehouseCacheManager.getInstance().addCacheTask(this);
		}
		return _warehouse;
	}
	
	/**
	 * Free memory used by Warehouse
	 */
	public void clearWarehouse() {
		if (_warehouse != null) {
			_warehouse.deleteMe();
		}
		_warehouse = null;
	}
	
	/**
	 * @return the PcFreight object of the L2PcInstance.
	 */
	public PcFreight getFreight() {
		return _freight;
	}
	
	/**
	 * @return true if refund list is not empty
	 */
	public boolean hasRefund() {
		return (_refund != null) && (_refund.getSize() > 0) && general().allowRefund();
	}
	
	/**
	 * @return refund object or create new if not exist
	 */
	public PcRefund getRefund() {
		if (_refund == null) {
			_refund = new PcRefund(this);
		}
		return _refund;
	}
	
	/**
	 * Clear refund
	 */
	public void clearRefund() {
		if (_refund != null) {
			_refund.deleteMe();
		}
		_refund = null;
	}
	
	/**
	 * @return the Adena amount of the L2PcInstance.
	 */
	public long getAdena() {
		return _inventory.getAdena();
	}
	
	/**
	 * @return the Ancient Adena amount of the L2PcInstance.
	 */
	public long getAncientAdena() {
		return _inventory.getAncientAdena();
	}
	
	/**
	 * Add adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAdena(String process, long count, L2Object reference, boolean sendMessage) {
		if (sendMessage) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
			sm.addLong(count);
			sendPacket(sm);
		}
		
		if (count > 0) {
			_inventory.addAdena(process, count, this, reference);
			
			// Send update packet
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAdenaInstance());
				sendPacket(iu);
			} else {
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean reduceAdena(String process, long count, L2Object reference, boolean sendMessage) {
		if (count > getAdena()) {
			if (sendMessage) {
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			return false;
		}
		
		if (count > 0) {
			L2ItemInstance adenaItem = _inventory.getAdenaInstance();
			if (!_inventory.reduceAdena(process, count, this, reference)) {
				return false;
			}
			
			// Send update packet
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(adenaItem);
				sendPacket(iu);
			} else {
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA);
				sm.addLong(count);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Add ancient adena to Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : int Quantity of ancient adena to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addAncientAdena(String process, long count, L2Object reference, boolean sendMessage) {
		if (sendMessage) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
			sm.addItemName(Inventory.ANCIENT_ADENA_ID);
			sm.addLong(count);
			sendPacket(sm);
		}
		
		if (count > 0) {
			_inventory.addAncientAdena(process, count, this, reference);
			
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(_inventory.getAncientAdenaInstance());
				sendPacket(iu);
			} else {
				sendPacket(new ItemList(this, false));
			}
		}
	}
	
	/**
	 * Reduce ancient adena in Inventory of the L2PcInstance and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param count : long Quantity of ancient adena to be reduced
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean reduceAncientAdena(String process, long count, L2Object reference, boolean sendMessage) {
		if (count > getAncientAdena()) {
			if (sendMessage) {
				sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
			}
			
			return false;
		}
		
		if (count > 0) {
			L2ItemInstance ancientAdenaItem = _inventory.getAncientAdenaInstance();
			if (!_inventory.reduceAncientAdena(process, count, this, reference)) {
				return false;
			}
			
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate iu = new InventoryUpdate();
				iu.addItem(ancientAdenaItem);
				sendPacket(iu);
			} else {
				sendPacket(new ItemList(this, false));
			}
			
			if (sendMessage) {
				if (count > 1) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
					sm.addItemName(Inventory.ANCIENT_ADENA_ID);
					sm.addLong(count);
					sendPacket(sm);
				} else {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
					sm.addItemName(Inventory.ANCIENT_ADENA_ID);
					sendPacket(sm);
				}
			}
		}
		
		return true;
	}
	
	/**
	 * Adds item to inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be added
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 */
	public void addItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage) {
		if (item.getCount() > 0) {
			// Sends message to client if requested
			if (sendMessage) {
				if (item.getCount() > 1) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
					sm.addItemName(item);
					sm.addLong(item.getCount());
					sendPacket(sm);
				} else if (item.getEnchantLevel() > 0) {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2);
					sm.addInt(item.getEnchantLevel());
					sm.addItemName(item);
					sendPacket(sm);
				} else {
					SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
					sm.addItemName(item);
					sendPacket(sm);
				}
			}
			
			// Add the item to inventory
			L2ItemInstance newitem = _inventory.addItem(process, item, this, reference);
			
			// Send inventory update packet
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate playerIU = new InventoryUpdate();
				playerIU.addItem(newitem);
				sendPacket(playerIU);
			} else {
				sendPacket(new ItemList(this, false));
			}
			
			// Update current load as well
			StatusUpdate su = new StatusUpdate(this);
			su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
			sendPacket(su);
			
			// If over capacity, drop the item
			if (!canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !_inventory.validateCapacity(0, item.isQuestItem()) && newitem.isDropable() && (!newitem.isStackable() || (newitem.getLastChange() != L2ItemInstance.MODIFIED))) {
				dropItem("InvDrop", newitem, null, true, true);
			} else if (CursedWeaponsManager.getInstance().isCursed(newitem.getId())) {
				CursedWeaponsManager.getInstance().activate(this, newitem);
			}
			
			// Combat Flag
			else if (FortSiegeManager.getInstance().isCombat(item.getId())) {
				if (FortSiegeManager.getInstance().activateCombatFlag(this, item)) {
					Fort fort = FortManager.getInstance().getFort(this);
					fort.getSiege().announceToPlayer(SystemMessage.getSystemMessage(SystemMessageId.C1_ACQUIRED_THE_FLAG), getName());
				}
			}
			// Territory Ward
			else if ((item.getId() >= 13560) && (item.getId() <= 13568)) {
				TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(item.getId() - 13479);
				if (ward != null) {
					ward.activate(this, item);
				}
			}
		}
	}
	
	/**
	 * Equivalent to {@link #addItem(String, int, long, L2Object, boolean)} with parameters (process, itemId, 1, -1, reference, sendMessage)
	 * @param process
	 * @param itemId
	 * @param reference
	 * @param sendMessage
	 * @return the new or updated item
	 */
	public L2ItemInstance addItem(String process, int itemId, L2Object reference, boolean sendMessage) {
		return addItem(process, itemId, 1, -1, reference, sendMessage);
	}
	
	/**
	 * Equivalent to {@link #addItem(String, int, long, int, L2Object, boolean)} with parameters (process, itemId, count, -1, reference, sendMessage)
	 * @param process
	 * @param itemId
	 * @param count
	 * @param reference
	 * @param sendMessage
	 * @return the new or updated item
	 */
	public L2ItemInstance addItem(String process, int itemId, long count, L2Object reference, boolean sendMessage) {
		return addItem(process, itemId, count, -1, reference, sendMessage);
	}
	
	/**
	 * Adds item to Inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item Identifier of the item to be added
	 * @param count : long Quantity of items to be added
	 * @param enchantLevel : int Enchant of the item; -1 to not modify on existing items, for new items use the default enchantLevel when -1
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return the new or updated item
	 */
	public L2ItemInstance addItem(String process, int itemId, long count, int enchantLevel, L2Object reference, boolean sendMessage) {
		if (count > 0) {
			final L2Item item = ItemTable.getInstance().getTemplate(itemId);
			if (item == null) {
				LOG.error("Item doesn't exist so cannot be added. Item ID: {}", itemId);
				return null;
			}
			// Sends message to client if requested
			if (sendMessage && ((!isCastingNow() && item.hasExImmediateEffect()) || !item.hasExImmediateEffect())) {
				if (count > 1) {
					if (process.equalsIgnoreCase("Sweeper") || process.equalsIgnoreCase("Quest")) {
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
						sm.addItemName(itemId);
						sm.addLong(count);
						sendPacket(sm);
					} else {
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1_S2);
						sm.addItemName(itemId);
						sm.addLong(count);
						sendPacket(sm);
					}
				} else {
					if (process.equalsIgnoreCase("Sweeper") || process.equalsIgnoreCase("Quest")) {
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					} else {
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1);
						sm.addItemName(itemId);
						sendPacket(sm);
					}
				}
			}
			
			// Auto-use herbs.
			if (item.hasExImmediateEffect()) {
				final IItemHandler handler = ItemHandler.getInstance().getHandler(item instanceof L2EtcItem ? (L2EtcItem) item : null);
				if (handler == null) {
					LOG.warn("No item handler registered for Herb {}!", item);
				} else {
					handler.useItem(this, new L2ItemInstance(itemId), false);
				}
			} else {
				// Add the item to inventory
				L2ItemInstance createdItem = _inventory.addItem(process, itemId, count, enchantLevel, this, reference);
				
				// If over capacity, drop the item
				if (!canOverrideCond(PcCondOverride.ITEM_CONDITIONS) && !_inventory.validateCapacity(0, item.isQuestItem()) && createdItem.isDropable() && (!createdItem.isStackable() || (createdItem.getLastChange() != L2ItemInstance.MODIFIED))) {
					dropItem("InvDrop", createdItem, null, true);
				} else if (CursedWeaponsManager.getInstance().isCursed(createdItem.getId())) {
					CursedWeaponsManager.getInstance().activate(this, createdItem);
				}
				// Territory Ward
				else if ((createdItem.getId() >= 13560) && (createdItem.getId() <= 13568)) {
					TerritoryWard ward = TerritoryWarManager.getInstance().getTerritoryWard(createdItem.getId() - 13479);
					if (ward != null) {
						ward.activate(this, createdItem);
					}
				}
				return createdItem;
			}
		}
		return null;
	}
	
	/**
	 * @param process the process name
	 * @param item the item holder
	 * @param reference the reference object
	 * @param sendMessage if {@code true} a system message will be sent
	 */
	public void addItem(String process, ItemHolder item, L2Object reference, boolean sendMessage) {
		addItem(process, item.getId(), item.getCount(), reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean destroyItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage) {
		return destroyItem(process, item, item.getCount(), reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param item : L2ItemInstance to be destroyed
	 * @param count
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean destroyItem(String process, L2ItemInstance item, long count, L2Object reference, boolean sendMessage) {
		item = _inventory.destroyItem(process, item, count, this, reference);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			return false;
		}
		
		// Send inventory update packet
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		} else {
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage) {
			if (count > 1) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(item);
				sm.addLong(count);
				sendPacket(sm);
			} else {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(item);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Destroys item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItem(String process, int objectId, long count, L2Object reference, boolean sendMessage) {
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		return destroyItem(process, item, count, reference, sendMessage);
	}
	
	/**
	 * Destroys shots from inventory without logging and only occasional saving to database. Sends a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	public boolean destroyItemWithoutTrace(String process, int objectId, long count, L2Object reference, boolean sendMessage) {
		L2ItemInstance item = _inventory.getItemByObjectId(objectId);
		
		if ((item == null) || (item.getCount() < count)) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		return destroyItem(null, item, count, reference, sendMessage);
	}
	
	/**
	 * Destroy item from inventory by using its <B>itemId</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param itemId : int Item identifier of the item to be destroyed
	 * @param count : int Quantity of items to be destroyed
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @return boolean informing if the action was successful
	 */
	@Override
	public boolean destroyItemByItemId(String process, int itemId, long count, L2Object reference, boolean sendMessage) {
		if (itemId == Inventory.ADENA_ID) {
			return reduceAdena(process, count, reference, sendMessage);
		}
		
		L2ItemInstance item = _inventory.getItemByItemId(itemId);
		
		if ((item == null) || (item.getCount() < count) || (_inventory.destroyItemByItemId(process, itemId, count, this, reference) == null)) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		// Send inventory update packet
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		} else {
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage) {
			if (count > 1) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED);
				sm.addItemName(itemId);
				sm.addLong(count);
				sendPacket(sm);
			} else {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
				sm.addItemName(itemId);
				sendPacket(sm);
			}
		}
		
		return true;
	}
	
	/**
	 * Transfers item to another ItemContainer and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Identifier of the item to be transfered
	 * @param count : long Quantity of items to be transfered
	 * @param target
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance transferItem(String process, int objectId, long count, Inventory target, L2Object reference) {
		L2ItemInstance oldItem = checkItemManipulation(objectId, count, "transfer");
		if (oldItem == null) {
			return null;
		}
		L2ItemInstance newItem = getInventory().transferItem(process, objectId, count, target, this, reference);
		if (newItem == null) {
			return null;
		}
		
		// Send inventory update packet
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate playerIU = new InventoryUpdate();
			
			if ((oldItem.getCount() > 0) && (oldItem != newItem)) {
				playerIU.addModifiedItem(oldItem);
			} else {
				playerIU.addRemovedItem(oldItem);
			}
			
			sendPacket(playerIU);
		} else {
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate playerSU = new StatusUpdate(this);
		playerSU.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(playerSU);
		
		// Send target update packet
		if (target instanceof PcInventory) {
			L2PcInstance targetPlayer = ((PcInventory) target).getOwner();
			
			if (!general().forceInventoryUpdate()) {
				InventoryUpdate playerIU = new InventoryUpdate();
				
				if (newItem.getCount() > count) {
					playerIU.addModifiedItem(newItem);
				} else {
					playerIU.addNewItem(newItem);
				}
				
				targetPlayer.sendPacket(playerIU);
			} else {
				targetPlayer.sendPacket(new ItemList(targetPlayer, false));
			}
			
			// Update current load as well
			playerSU = new StatusUpdate(targetPlayer);
			playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.getCurrentLoad());
			targetPlayer.sendPacket(playerSU);
		} else if (target instanceof PetInventory) {
			PetInventoryUpdate petIU = new PetInventoryUpdate();
			
			if (newItem.getCount() > count) {
				petIU.addModifiedItem(newItem);
			} else {
				petIU.addNewItem(newItem);
			}
			
			((PetInventory) target).getOwner().sendPacket(petIU);
		}
		return newItem;
	}
	
	/**
	 * Use instead of calling {@link #addItem(String, L2ItemInstance, L2Object, boolean)} and {@link #destroyItemByItemId(String, int, long, L2Object, boolean)}<br>
	 * This method validates slots and weight limit, for stackable and non-stackable items.
	 * @param process a generic string representing the process that is exchanging this items
	 * @param reference the (probably NPC) reference, could be null
	 * @param coinId the item Id of the item given on the exchange
	 * @param cost the amount of items given on the exchange
	 * @param rewardId the item received on the exchange
	 * @param count the amount of items received on the exchange
	 * @param sendMessage if {@code true} it will send messages to the acting player
	 * @return {@code true} if the player successfully exchanged the items, {@code false} otherwise
	 */
	public boolean exchangeItemsById(String process, L2Object reference, int coinId, long cost, int rewardId, long count, boolean sendMessage) {
		final PcInventory inv = getInventory();
		if (!inv.validateCapacityByItemId(rewardId, count)) {
			if (sendMessage) {
				sendPacket(SystemMessageId.SLOTS_FULL);
			}
			return false;
		}
		
		if (!inv.validateWeightByItemId(rewardId, count)) {
			if (sendMessage) {
				sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
			}
			return false;
		}
		
		if (destroyItemByItemId(process, coinId, cost, reference, sendMessage)) {
			addItem(process, rewardId, count, reference, sendMessage);
			return true;
		}
		return false;
	}
	
	/**
	 * Drop item from inventory and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process String Identifier of process triggering this action
	 * @param item L2ItemInstance to be dropped
	 * @param reference L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage boolean Specifies whether to send message to Client about this action
	 * @param protectItem whether or not dropped item must be protected temporary against other players
	 * @return boolean informing if the action was successful
	 */
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage, boolean protectItem) {
		item = _inventory.dropItem(process, item, this, reference);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return false;
		}
		
		item.dropMe(this, (getX() + Rnd.get(50)) - 25, (getY() + Rnd.get(50)) - 25, getZ() + 20);
		
		if ((general().getAutoDestroyDroppedItemAfter() > 0) && general().destroyPlayerDroppedItem() && !general().getProtectedItems().contains(item.getId())) {
			if ((item.isEquipable() && general().destroyEquipableItem()) || !item.isEquipable()) {
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		
		// protection against auto destroy dropped item
		if (general().destroyPlayerDroppedItem()) {
			if (!item.isEquipable() || (item.isEquipable() && general().destroyEquipableItem())) {
				item.setProtected(false);
			} else {
				item.setProtected(true);
			}
		} else {
			item.setProtected(true);
		}
		
		// retail drop protection
		if (protectItem) {
			item.getDropProtection().protect(this);
		}
		
		// Send inventory update packet
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(item);
			sendPacket(playerIU);
		} else {
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		
		return true;
	}
	
	public boolean dropItem(String process, L2ItemInstance item, L2Object reference, boolean sendMessage) {
		return dropItem(process, item, reference, sendMessage, false);
	}
	
	/**
	 * Drop item from inventory by using its <B>objectID</B> and send a Server->Client InventoryUpdate packet to the L2PcInstance.
	 * @param process : String Identifier of process triggering this action
	 * @param objectId : int Item Instance identifier of the item to be dropped
	 * @param count : long Quantity of items to be dropped
	 * @param x : int coordinate for drop X
	 * @param y : int coordinate for drop Y
	 * @param z : int coordinate for drop Z
	 * @param reference : L2Object Object referencing current action like NPC selling item or previous item in transformation
	 * @param sendMessage : boolean Specifies whether to send message to Client about this action
	 * @param protectItem
	 * @return L2ItemInstance corresponding to the new item or the updated item in inventory
	 */
	public L2ItemInstance dropItem(String process, int objectId, long count, int x, int y, int z, L2Object reference, boolean sendMessage, boolean protectItem) {
		L2ItemInstance invitem = _inventory.getItemByObjectId(objectId);
		L2ItemInstance item = _inventory.dropItem(process, objectId, count, this, reference);
		
		if (item == null) {
			if (sendMessage) {
				sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			}
			
			return null;
		}
		
		item.dropMe(this, x, y, z);
		
		if ((general().getAutoDestroyDroppedItemAfter() > 0) && general().destroyPlayerDroppedItem() && !general().getProtectedItems().contains(item.getId())) {
			if ((item.isEquipable() && general().destroyEquipableItem()) || !item.isEquipable()) {
				ItemsAutoDestroy.getInstance().addItem(item);
			}
		}
		if (general().destroyPlayerDroppedItem()) {
			if (!item.isEquipable() || (item.isEquipable() && general().destroyEquipableItem())) {
				item.setProtected(false);
			} else {
				item.setProtected(true);
			}
		} else {
			item.setProtected(true);
		}
		
		// retail drop protection
		if (protectItem) {
			item.getDropProtection().protect(this);
		}
		
		// Send inventory update packet
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate playerIU = new InventoryUpdate();
			playerIU.addItem(invitem);
			sendPacket(playerIU);
		} else {
			sendPacket(new ItemList(this, false));
		}
		
		// Update current load as well
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.CUR_LOAD, getCurrentLoad());
		sendPacket(su);
		
		// Sends message to client if requested
		if (sendMessage) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1);
			sm.addItemName(item);
			sendPacket(sm);
		}
		
		return item;
	}
	
	public L2ItemInstance checkItemManipulation(int objectId, long count, String action) {
		// TODO: if we remove objects that are not visible from the L2World, we'll have to remove this check
		if (L2World.getInstance().findObject(objectId) == null) {
			LOG.warn("{} tried to {} item not available in L2World", this, action);
			return null;
		}
		
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if ((item == null) || (item.getOwnerId() != getObjectId())) {
			LOG.warn("{} tried to {} item he is not owner of", this, action);
			return null;
		}
		
		if ((count < 0) || ((count > 1) && !item.isStackable())) {
			LOG.warn("{} tried to {} item with invalid count: {}", this, action, count);
			return null;
		}
		
		if (count > item.getCount()) {
			LOG.warn("{} tried to {} more items than he owns", this, action);
			return null;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if ((hasSummon() && (getSummon().getControlObjectId() == objectId)) || (getMountObjectID() == objectId)) {
			if (general().debug()) {
				LOG.debug("{} tried to {} item controling pet", this, action);
			}
			
			return null;
		}
		
		if (getActiveEnchantItemId() == objectId) {
			if (general().debug()) {
				LOG.debug("{} tried to {} an enchant scroll he was using", this, action);
			}
			return null;
		}
		
		// We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
		if (item.isAugmented() && (isCastingNow() || isCastingSimultaneouslyNow())) {
			return null;
		}
		
		return item;
	}
	
	/**
	 * Set _protectEndTime according settings.
	 * @param protect
	 */
	public void setProtection(boolean protect) {
		if (general().developer() && (protect || (_protectEndTime > 0))) {
			LOG.debug("{}: Protection {} (currently {})", this, (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + //
				(character().getPlayerSpawnProtection() * GameTimeController.TICKS_PER_SECOND)) : "OFF"), GameTimeController.getInstance().getGameTicks());
		}
		
		_protectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (character().getPlayerSpawnProtection() * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public void setTeleportProtection(boolean protect) {
		if (general().developer() && (protect || (_teleportProtectEndTime > 0))) {
			LOG.debug("{}: Tele Protection {} (currently {})", this, (protect ? "ON " + (GameTimeController.getInstance().getGameTicks() + //
				(character().getPlayerTeleportProtection() * GameTimeController.TICKS_PER_SECOND)) : "OFF"), GameTimeController.getInstance().getGameTicks());
		}
		
		_teleportProtectEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (character().getPlayerTeleportProtection() * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public boolean isRecentFakeDeath() {
		return _recentFakeDeathEndTime > GameTimeController.getInstance().getGameTicks();
	}
	
	/**
	 * Set protection from agro mobs when getting up from fake death, according settings.
	 * @param protect
	 */
	public void setRecentFakeDeath(boolean protect) {
		_recentFakeDeathEndTime = protect ? GameTimeController.getInstance().getGameTicks() + (character().getPlayerFakeDeathUpProtection() * GameTimeController.TICKS_PER_SECOND) : 0;
	}
	
	public final boolean isFakeDeath() {
		return _isFakeDeath;
	}
	
	public final void setIsFakeDeath(boolean value) {
		_isFakeDeath = value;
	}
	
	@Override
	public final boolean isAlikeDead() {
		return super.isAlikeDead() || isFakeDeath();
	}
	
	/**
	 * @return the client owner of this char.
	 */
	public L2GameClient getClient() {
		return _client;
	}
	
	public void setClient(L2GameClient client) {
		_client = client;
	}
	
	public String getIPAddress() {
		String ip = "N/A";
		if ((_client != null) && (_client.getConnectionAddress() != null)) {
			ip = _client.getConnectionAddress().getHostAddress();
		}
		return ip;
	}
	
	/**
	 * Close the active connection with the client.
	 * @param closeClient
	 */
	private void closeNetConnection(boolean closeClient) {
		L2GameClient client = _client;
		if (client != null) {
			if (client.isDetached()) {
				client.cleanMe(true);
			} else {
				if (!client.getConnection().isClosed()) {
					if (closeClient) {
						client.close(LeaveWorld.STATIC_PACKET);
					} else {
						client.close(ServerClose.STATIC_PACKET);
					}
				}
			}
		}
	}
	
	public Location getCurrentSkillWorldPosition() {
		return _currentSkillWorldPosition;
	}
	
	public void setCurrentSkillWorldPosition(Location worldPosition) {
		_currentSkillWorldPosition = worldPosition;
	}
	
	@Override
	public void enableSkill(Skill skill) {
		super.enableSkill(skill);
		removeTimeStamp(skill);
	}
	
	@Override
	public boolean checkDoCastConditions(Skill skill) {
		if (!super.checkDoCastConditions(skill)) {
			return false;
		}
		
		if (inObserverMode()) {
			return false;
		}
		
		if (isInOlympiadMode() && skill.isBlockedInOlympiad()) {
			sendPacket(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
			return false;
		}
		
		// Check if the spell using charges or not in AirShip
		if (((getCharges() < skill.getChargeConsume())) || (isInAirShip() && !skill.hasEffectType(L2EffectType.REFUEL_AIRSHIP))) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addSkillName(skill);
			sendPacket(sm);
			return false;
		}
		return true;
	}
	
	/**
	 * Returns true if cp update should be done, false if not
	 * @return boolean
	 */
	private boolean needCpUpdate() {
		double currentCp = getCurrentCp();
		
		if ((currentCp <= 1.0) || (getMaxCp() < MAX_HP_BAR_PX)) {
			return true;
		}
		
		if ((currentCp <= _cpUpdateDecCheck) || (currentCp >= _cpUpdateIncCheck)) {
			if (currentCp == getMaxCp()) {
				_cpUpdateIncCheck = currentCp + 1;
				_cpUpdateDecCheck = currentCp - _cpUpdateInterval;
			} else {
				double doubleMulti = currentCp / _cpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_cpUpdateDecCheck = _cpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Returns true if mp update should be done, false if not
	 * @return boolean
	 */
	private boolean needMpUpdate() {
		double currentMp = getCurrentMp();
		
		if ((currentMp <= 1.0) || (getMaxMp() < MAX_HP_BAR_PX)) {
			return true;
		}
		
		if ((currentMp <= _mpUpdateDecCheck) || (currentMp >= _mpUpdateIncCheck)) {
			if (currentMp == getMaxMp()) {
				_mpUpdateIncCheck = currentMp + 1;
				_mpUpdateDecCheck = currentMp - _mpUpdateInterval;
			} else {
				double doubleMulti = currentMp / _mpUpdateInterval;
				int intMulti = (int) doubleMulti;
				
				_mpUpdateDecCheck = _mpUpdateInterval * (doubleMulti < intMulti ? intMulti-- : intMulti);
				_mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval;
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send packet StatusUpdate with current HP,MP and CP to the L2PcInstance and only current HP, MP and Level to all other L2PcInstance of the Party. <B><U> Actions</U> :</B>
	 * <li>Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance</li><BR>
	 * <li>Send the Server->Client packet PartySmallWindowUpdate with current HP, MP and Level to all other L2PcInstance of the Party</li> <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all L2PcInstance of the _statusListener</B></FONT>
	 */
	@Override
	public void broadcastStatusUpdate() {
		// TODO We mustn't send these informations to other players
		// Send the Server->Client packet StatusUpdate with current HP and MP to all L2PcInstance that must be informed of HP/MP updates of this L2PcInstance
		// super.broadcastStatusUpdate();
		
		// Send the Server->Client packet StatusUpdate with current HP, MP and CP to this L2PcInstance
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.MAX_HP, getMaxHp());
		su.addAttribute(StatusUpdate.CUR_HP, (int) getCurrentHp());
		su.addAttribute(StatusUpdate.MAX_MP, getMaxMp());
		su.addAttribute(StatusUpdate.CUR_MP, (int) getCurrentMp());
		su.addAttribute(StatusUpdate.MAX_CP, getMaxCp());
		su.addAttribute(StatusUpdate.CUR_CP, (int) getCurrentCp());
		sendPacket(su);
		
		final boolean needCpUpdate = needCpUpdate();
		final boolean needHpUpdate = needHpUpdate();
		
		// Check if a party is in progress and party window update is usefull
		if (isInParty() && (needCpUpdate || needHpUpdate || needMpUpdate())) {
			getParty().broadcastToPartyMembers(this, new PartySmallWindowUpdate(this));
		}
		
		if (isInOlympiadMode() && isOlympiadStart() && (needCpUpdate || needHpUpdate)) {
			final OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(getOlympiadGameId());
			if ((game != null) && game.isBattleStarted()) {
				game.getZone().broadcastStatusUpdate(this);
			}
		}
		
		// In duel MP updated only with CP or HP
		if (isInDuel() && (needCpUpdate || needHpUpdate)) {
			DuelManager.getInstance().broadcastToOppositTeam(this, new ExDuelUpdateUserInfo(this));
		}
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers. <B><U> Concept</U> :</B> Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>. In order to inform other players of this
	 * L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet <B><U> Actions</U> :</B>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li> <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP,
	 * STR, DEX...</B></FONT>
	 */
	public final void broadcastUserInfo() {
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		
		// Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance
		broadcastPacket(new CharInfo(this));
		broadcastPacket(new ExBrExtraUserInfo(this));
		if (TerritoryWarManager.getInstance().isTWInProgress() && (TerritoryWarManager.getInstance().checkIsRegistered(-1, getObjectId()) || TerritoryWarManager.getInstance().checkIsRegistered(-1, getClan()))) {
			broadcastPacket(new ExDominionWarStart(this));
		}
	}
	
	public final void broadcastTitleInfo() {
		// Send a Server->Client packet UserInfo to this L2PcInstance
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		
		// Send a Server->Client packet TitleUpdate to all L2PcInstance in _KnownPlayers of the L2PcInstance
		
		broadcastPacket(new NicknameChanged(this));
	}
	
	@Override
	public final void broadcastPacket(L2GameServerPacket mov) {
		if (!(mov instanceof CharInfo)) {
			sendPacket(mov);
		}
		
		mov.setInvisible(isInvisible());
		
		final Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs) {
			if ((player == null) || !isVisibleFor(player)) {
				continue;
			}
			player.sendPacket(mov);
			if (mov instanceof CharInfo) {
				int relation = getRelation(player);
				Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
				if ((oldrelation != null) && (oldrelation != relation)) {
					player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
					if (hasSummon()) {
						player.sendPacket(new RelationChanged(getSummon(), relation, isAutoAttackable(player)));
					}
				}
			}
		}
	}
	
	@Override
	public void broadcastPacket(L2GameServerPacket mov, int radiusInKnownlist) {
		if (!(mov instanceof CharInfo)) {
			sendPacket(mov);
		}
		
		mov.setInvisible(isInvisible());
		
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs) {
			if (player == null) {
				continue;
			}
			if (isInsideRadius(player, radiusInKnownlist, false, false)) {
				player.sendPacket(mov);
				if (mov instanceof CharInfo) {
					int relation = getRelation(player);
					Integer oldrelation = getKnownList().getKnownRelations().get(player.getObjectId());
					if ((oldrelation != null) && (oldrelation != relation)) {
						player.sendPacket(new RelationChanged(this, relation, isAutoAttackable(player)));
						if (hasSummon()) {
							player.sendPacket(new RelationChanged(getSummon(), relation, isAutoAttackable(player)));
						}
					}
				}
			}
		}
	}
	
	/**
	 * @return the Alliance Identifier of the L2PcInstance.
	 */
	@Override
	public int getAllyId() {
		if (_clan == null) {
			return 0;
		}
		return _clan.getAllyId();
	}
	
	public int getAllyCrestId() {
		if (getClanId() == 0) {
			return 0;
		}
		if (getClan().getAllyId() == 0) {
			return 0;
		}
		return getClan().getAllyCrestId();
	}
	
	public void queryGameGuard() {
		if (getClient() != null) {
			getClient().setGameGuardOk(false);
			sendPacket(GameGuardQuery.STATIC_PACKET);
		}
		if (general().gameGuardEnforce()) {
			ThreadPoolManager.getInstance().scheduleGeneral(new GameGuardCheckTask(this), 30 * 1000);
		}
	}
	
	/**
	 * Send a Server->Client packet StatusUpdate to the L2PcInstance.
	 */
	@Override
	public void sendPacket(L2GameServerPacket packet) {
		if (_client != null) {
			_client.sendPacket(packet);
		}
	}
	
	/**
	 * Send SystemMessage packet.
	 * @param id SystemMessageId
	 */
	@Override
	public void sendPacket(SystemMessageId id) {
		sendPacket(SystemMessage.getSystemMessage(id));
	}
	
	/**
	 * Manage Interact Task with another L2PcInstance. <B><U> Actions</U> :</B>
	 * <li>If the private store is a STORE_PRIVATE_SELL, send a Server->Client PrivateBuyListSell packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_BUY, send a Server->Client PrivateBuyListBuy packet to the L2PcInstance</li>
	 * <li>If the private store is a STORE_PRIVATE_MANUFACTURE, send a Server->Client RecipeShopSellList packet to the L2PcInstance</li>
	 * @param target The L2Character targeted
	 */
	public void doInteract(L2Character target) {
		if (target instanceof L2PcInstance) {
			L2PcInstance temp = (L2PcInstance) target;
			sendPacket(ActionFailed.STATIC_PACKET);
			
			if ((temp.getPrivateStoreType() == PrivateStoreType.SELL) || (temp.getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)) {
				sendPacket(new PrivateStoreListSell(this, temp));
			} else if (temp.getPrivateStoreType() == PrivateStoreType.BUY) {
				sendPacket(new PrivateStoreListBuy(this, temp));
			} else if (temp.getPrivateStoreType() == PrivateStoreType.MANUFACTURE) {
				sendPacket(new RecipeShopSellList(this, temp));
			}
			
		} else {
			// _interactTarget=null should never happen but one never knows ^^;
			if (target != null) {
				target.onAction(this);
			}
		}
	}
	
	/**
	 * Manages AutoLoot Task.<br>
	 * <ul>
	 * <li>Send a system message to the player.</li>
	 * <li>Add the item to the player's inventory.</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this player with NewItem (use a new slot) or ModifiedItem (increase amount).</li>
	 * <li>Send a Server->Client packet StatusUpdate to this player with current weight.</li>
	 * </ul>
	 * <font color=#FF0000><B><U>Caution</U>: If a party is in progress, distribute the items between the party members!</b></font>
	 * @param target the NPC dropping the item
	 * @param itemId the item ID
	 * @param itemCount the item count
	 */
	public void doAutoLoot(L2Attackable target, int itemId, long itemCount) {
		if (isInParty() && !ItemTable.getInstance().getTemplate(itemId).hasExImmediateEffect()) {
			getParty().distributeItem(this, itemId, itemCount, false, target);
		} else if (itemId == Inventory.ADENA_ID) {
			addAdena("Loot", itemCount, target, true);
		} else {
			addItem("Loot", itemId, itemCount, target, true);
		}
	}
	
	/**
	 * Method overload for {@link L2PcInstance#doAutoLoot(L2Attackable, int, long)}
	 * @param target the NPC dropping the item
	 * @param item the item holder
	 */
	public void doAutoLoot(L2Attackable target, ItemHolder item) {
		doAutoLoot(target, item.getId(), item.getCount());
	}
	
	/**
	 * Manage Pickup Task. <B><U> Actions</U> :</B>
	 * <li>Send a Server->Client packet StopMove to this L2PcInstance</li>
	 * <li>Remove the L2ItemInstance from the world and send server->client GetItem packets</li>
	 * <li>Send a System Message to the L2PcInstance : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2</li>
	 * <li>Add the Item to the L2PcInstance inventory</li>
	 * <li>Send a Server->Client packet InventoryUpdate to this L2PcInstance with NewItem (use a new slot) or ModifiedItem (increase amount)</li>
	 * <li>Send a Server->Client packet StatusUpdate to this L2PcInstance with current weight</li> <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
	 * @param object The L2ItemInstance to pick up
	 */
	@Override
	public void doPickupItem(L2Object object) {
		if (isAlikeDead() || isFakeDeath() || isInvisible()) {
			return;
		}
		
		// Set the AI Intention to AI_INTENTION_IDLE
		getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		
		// Check if the L2Object to pick up is a L2ItemInstance
		if (!(object.isItem())) {
			// dont try to pickup anything that is not an item :)
			LOG.warn("{} trying to pickup wrong target.", this, getTarget());
			return;
		}
		
		L2ItemInstance target = (L2ItemInstance) object;
		
		sendPacket(new StopMove(this));
		
		SystemMessage smsg = null;
		synchronized (target) {
			// Check if the target to pick up is visible
			if (!target.isVisible()) {
				// Send a Server->Client packet ActionFailed to this L2PcInstance
				sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (!target.getDropProtection().tryPickUp(this)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
				smsg.addItemName(target);
				sendPacket(smsg);
				return;
			}
			
			if (((isInParty() && (getParty().getDistributionType() == PartyDistributionType.FINDERS_KEEPERS)) || !isInParty()) && !_inventory.validateCapacity(target)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.SLOTS_FULL);
				return;
			}
			
			if (isInvisible() && !canOverrideCond(PcCondOverride.ITEM_CONDITIONS)) {
				return;
			}
			
			if ((target.getOwnerId() != 0) && (target.getOwnerId() != getObjectId()) && !isInLooterParty(target.getOwnerId())) {
				if (target.getId() == Inventory.ADENA_ID) {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA);
					smsg.addLong(target.getCount());
				} else if (target.getCount() > 1) {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S);
					smsg.addItemName(target);
					smsg.addLong(target.getCount());
				} else {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1);
					smsg.addItemName(target);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(smsg);
				return;
			}
			
			// You can pickup only 1 combat flag
			if (FortSiegeManager.getInstance().isCombat(target.getId())) {
				if (!FortSiegeManager.getInstance().checkIfCanPickup(this)) {
					return;
				}
			}
			
			if ((target.getItemLootShedule() != null) && ((target.getOwnerId() == getObjectId()) || isInLooterParty(target.getOwnerId()))) {
				target.resetOwnerTimer();
			}
			
			// Remove the L2ItemInstance from the world and send server->client GetItem packets
			target.pickupMe(this);
			if (general().saveDroppedItem()) {
				ItemsOnGroundManager.getInstance().removeObject(target);
			}
		}
		
		// Auto use herbs - pick up
		if (target.getItem().hasExImmediateEffect()) {
			IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
			if (handler == null) {
				LOG.warn("No item handler registered for item ID: {}.", target.getId());
			} else {
				handler.useItem(this, target, false);
			}
			ItemTable.getInstance().destroyItem("Consume", target, this, null);
		}
		// Cursed Weapons are not distributed
		else if (CursedWeaponsManager.getInstance().isCursed(target.getId())) {
			addItem("Pickup", target, null, true);
		} else if (FortSiegeManager.getInstance().isCombat(target.getId())) {
			addItem("Pickup", target, null, true);
		} else {
			// if item is instance of ArmorType or WeaponType broadcast an "Attention" system message
			if ((target.getItemType() instanceof ArmorType) || (target.getItemType() instanceof WeaponType)) {
				if (target.getEnchantLevel() > 0) {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2_S3);
					smsg.addPcName(this);
					smsg.addInt(target.getEnchantLevel());
					smsg.addItemName(target.getId());
					broadcastPacket(smsg, 1400);
				} else {
					smsg = SystemMessage.getSystemMessage(SystemMessageId.ANNOUNCEMENT_C1_PICKED_UP_S2);
					smsg.addPcName(this);
					smsg.addItemName(target.getId());
					broadcastPacket(smsg, 1400);
				}
			}
			
			// Check if a Party is in progress
			if (isInParty()) {
				getParty().distributeItem(this, target);
			} else if ((target.getId() == Inventory.ADENA_ID) && (getInventory().getAdenaInstance() != null)) {
				addAdena("Pickup", target.getCount(), null, true);
				ItemTable.getInstance().destroyItem("Pickup", target, this, null);
			} else {
				addItem("Pickup", target, null, true);
				// Auto-Equip arrows/bolts if player has a bow/crossbow and player picks up arrows/bolts.
				final L2ItemInstance weapon = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (weapon != null) {
					final L2EtcItem etcItem = target.getEtcItem();
					if (etcItem != null) {
						final EtcItemType itemType = etcItem.getItemType();
						if ((weapon.getItemType() == WeaponType.BOW) && (itemType == EtcItemType.ARROW)) {
							checkAndEquipArrows();
						} else if ((weapon.getItemType() == WeaponType.CROSSBOW) && (itemType == EtcItemType.BOLT)) {
							checkAndEquipBolts();
						}
					}
				}
			}
		}
	}
	
	@Override
	public void doAttack(L2Character target) {
		super.doAttack(target);
		setRecentFakeDeath(false);
	}
	
	@Override
	public void doCast(Skill skill) {
		if (getCurrentSkill() != null) {
			if (!checkUseMagicConditions(skill, getCurrentSkill().isCtrlPressed(), getCurrentSkill().isShiftPressed())) {
				setIsCastingNow(false);
				return;
			}
		}
		super.doCast(skill);
		setRecentFakeDeath(false);
	}
	
	public boolean canOpenPrivateStore() {
		return !isAlikeDead() && !isInOlympiadMode() && !isMounted() && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow();
	}
	
	public void tryOpenPrivateBuyStore() {
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore()) {
			if ((getPrivateStoreType() == PrivateStoreType.BUY) || (getPrivateStoreType() == PrivateStoreType.BUY_MANAGE)) {
				setPrivateStoreType(PrivateStoreType.NONE);
			}
			if (getPrivateStoreType() == PrivateStoreType.NONE) {
				if (isSitting()) {
					standUp();
				}
				setPrivateStoreType(PrivateStoreType.BUY_MANAGE);
				sendPacket(new PrivateStoreManageListBuy(this));
			}
		} else {
			if (isInsideZone(ZoneId.NO_STORE)) {
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public void tryOpenPrivateSellStore(boolean isPackageSale) {
		// Player shouldn't be able to set stores if he/she is alike dead (dead or fake death)
		if (canOpenPrivateStore()) {
			if ((getPrivateStoreType() == PrivateStoreType.SELL) || (getPrivateStoreType() == PrivateStoreType.SELL_MANAGE) || (getPrivateStoreType() == PrivateStoreType.PACKAGE_SELL)) {
				setPrivateStoreType(PrivateStoreType.NONE);
			}
			
			if (getPrivateStoreType() == PrivateStoreType.NONE) {
				if (isSitting()) {
					standUp();
				}
				setPrivateStoreType(PrivateStoreType.SELL_MANAGE);
				sendPacket(new PrivateStoreManageListSell(this, isPackageSale));
			}
		} else {
			if (isInsideZone(ZoneId.NO_STORE)) {
				sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE);
			}
			sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
	
	public final PreparedListContainer getMultiSell() {
		return _currentMultiSell;
	}
	
	public final void setMultiSell(PreparedListContainer list) {
		_currentMultiSell = list;
	}
	
	@Override
	public boolean isTransformed() {
		return (_transformation != null) && !_transformation.isStance();
	}
	
	public boolean isInStance() {
		return (_transformation != null) && _transformation.isStance();
	}
	
	public void transform(Transform transformation) {
		if (_transformation != null) {
			// You already polymorphed and cannot polymorph again.
			SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			sendPacket(msg);
			return;
		}
		
		setQueuedSkill(null, false, false);
		if (isMounted()) {
			// Get off the strider or something else if character is mounted
			dismount();
		}
		
		_transformation = transformation;
		getEffectList().stopAllToggles();
		transformation.onTransform(this);
		sendSkillList();
		sendPacket(new SkillCoolTime(this));
		broadcastUserInfo();
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerTransform(this, transformation.getId()), this);
	}
	
	@Override
	public void untransform() {
		if (_transformation != null) {
			setQueuedSkill(null, false, false);
			_transformation.onUntransform(this);
			_transformation = null;
			getEffectList().stopAllToggles(false);
			getEffectList().stopSkillEffects(false, AbnormalType.TRANSFORM);
			sendSkillList();
			sendPacket(new SkillCoolTime(this));
			broadcastUserInfo();
			
			// Notify to scripts
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerTransform(this, 0), this);
		}
	}
	
	@Override
	public Transform getTransformation() {
		return _transformation;
	}
	
	/**
	 * This returns the transformation Id of the current transformation. For example, if a player is transformed as a Buffalo, and then picks up the Zariche, the transform Id returned will be that of the Zariche, and NOT the Buffalo.
	 * @return Transformation Id
	 */
	public int getTransformationId() {
		return (isTransformed() ? getTransformation().getId() : 0);
	}
	
	public int getTransformationDisplayId() {
		return (isTransformed() ? getTransformation().getDisplayId() : 0);
	}
	
	/**
	 * Set a target. <B><U> Actions</U> :</B>
	 * <ul>
	 * <li>Remove the L2PcInstance from the _statusListener of the old target if it was a L2Character</li>
	 * <li>Add the L2PcInstance to the _statusListener of the new target if it's a L2Character</li>
	 * <li>Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)</li>
	 * </ul>
	 * @param newTarget The L2Object to target
	 */
	@Override
	public void setTarget(L2Object newTarget) {
		if (newTarget != null) {
			boolean isInParty = (newTarget.isPlayer() && isInParty() && getParty().containsPlayer(newTarget.getActingPlayer()));
			
			// Prevents /target exploiting
			if (!isInParty && (Math.abs(newTarget.getZ() - getZ()) > 1000)) {
				newTarget = null;
			}
			
			// Check if the new target is visible
			if ((newTarget != null) && !isInParty && !newTarget.isVisible()) {
				newTarget = null;
			}
			
			// vehicles cant be targeted
			if (!isGM() && (newTarget instanceof L2Vehicle)) {
				newTarget = null;
			}
		}
		
		// Get the current target
		L2Object oldTarget = getTarget();
		
		if (oldTarget != null) {
			if (oldTarget.equals(newTarget)) // no target change?
			{
				// Validate location of the target.
				if ((newTarget != null) && (newTarget.getObjectId() != getObjectId())) {
					sendPacket(new ValidateLocation(newTarget));
				}
				return;
			}
			
			// Remove the target from the status listener.
			oldTarget.removeStatusListener(this);
		}
		
		if (newTarget instanceof L2Character) {
			final L2Character target = (L2Character) newTarget;
			
			// Validate location of the new target.
			if (newTarget.getObjectId() != getObjectId()) {
				sendPacket(new ValidateLocation(target));
			}
			
			// Show the client his new target.
			sendPacket(new MyTargetSelected(this, target));
			
			// Register target to listen for hp changes.
			target.addStatusListener(this);
			
			// Send max/current hp.
			final StatusUpdate su = new StatusUpdate(target);
			su.addAttribute(StatusUpdate.MAX_HP, target.getMaxHp());
			su.addAttribute(StatusUpdate.CUR_HP, (int) target.getCurrentHp());
			sendPacket(su);
			
			// To others the new target, and not yourself!
			Broadcast.toKnownPlayers(this, new TargetSelected(getObjectId(), newTarget.getObjectId(), getX(), getY(), getZ()));
		}
		
		// Target was removed?
		if ((newTarget == null) && (getTarget() != null)) {
			broadcastPacket(new TargetUnselected(this));
		}
		
		// Target the new L2Object (add the target to the L2PcInstance _target, _knownObject and L2PcInstance to _KnownObject of the L2Object)
		super.setTarget(newTarget);
	}
	
	/**
	 * Return the active weapon instance (always equiped in the right hand).
	 */
	@Override
	public L2ItemInstance getActiveWeaponInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
	}
	
	/**
	 * Return the active weapon item (always equiped in the right hand).
	 */
	@Override
	public L2Weapon getActiveWeaponItem() {
		L2ItemInstance weapon = getActiveWeaponInstance();
		
		if (weapon == null) {
			return getFistsWeaponItem();
		}
		
		return (L2Weapon) weapon.getItem();
	}
	
	public L2ItemInstance getChestArmorInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST);
	}
	
	public L2ItemInstance getLegsArmorInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LEGS);
	}
	
	public L2Armor getActiveChestArmorItem() {
		L2ItemInstance armor = getChestArmorInstance();
		
		if (armor == null) {
			return null;
		}
		
		return (L2Armor) armor.getItem();
	}
	
	public L2Armor getActiveLegsArmorItem() {
		L2ItemInstance legs = getLegsArmorInstance();
		
		if (legs == null) {
			return null;
		}
		
		return (L2Armor) legs.getItem();
	}
	
	public boolean isWearingHeavyArmor() {
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null)) {
			if ((legs.getItemType() == ArmorType.HEAVY) && (armor.getItemType() == ArmorType.HEAVY)) {
				return true;
			}
		}
		if (armor != null) {
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && (armor.getItemType() == ArmorType.HEAVY))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isWearingLightArmor() {
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null)) {
			if ((legs.getItemType() == ArmorType.LIGHT) && (armor.getItemType() == ArmorType.LIGHT)) {
				return true;
			}
		}
		if (armor != null) {
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && (armor.getItemType() == ArmorType.LIGHT))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isWearingMagicArmor() {
		L2ItemInstance legs = getLegsArmorInstance();
		L2ItemInstance armor = getChestArmorInstance();
		
		if ((armor != null) && (legs != null)) {
			if ((legs.getItemType() == ArmorType.MAGIC) && (armor.getItemType() == ArmorType.MAGIC)) {
				return true;
			}
		}
		if (armor != null) {
			if (((getInventory().getPaperdollItem(Inventory.PAPERDOLL_CHEST).getItem().getBodyPart() == L2Item.SLOT_FULL_ARMOR) && (armor.getItemType() == ArmorType.MAGIC))) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isMarried() {
		return _married;
	}
	
	public void setMarried(boolean state) {
		_married = state;
	}
	
	public boolean isEngageRequest() {
		return _engagerequest;
	}
	
	public void setEngageRequest(boolean state, int playerid) {
		_engagerequest = state;
		_engageid = playerid;
	}
	
	public boolean isMarryRequest() {
		return _marryrequest;
	}
	
	public void setMarryRequest(boolean state) {
		_marryrequest = state;
	}
	
	public boolean isMarryAccepted() {
		return _marryaccepted;
	}
	
	public void setMarryAccepted(boolean state) {
		_marryaccepted = state;
	}
	
	public int getEngageId() {
		return _engageid;
	}
	
	public int getPartnerId() {
		return _partnerId;
	}
	
	public void setPartnerId(int partnerid) {
		_partnerId = partnerid;
	}
	
	public int getCoupleId() {
		return _coupleId;
	}
	
	public void setCoupleId(int coupleId) {
		_coupleId = coupleId;
	}
	
	public void engageAnswer(int answer) {
		if (!_engagerequest) {
			return;
		} else if (_engageid == 0) {
			return;
		} else {
			L2PcInstance ptarget = L2World.getInstance().getPlayer(_engageid);
			setEngageRequest(false, 0);
			if (ptarget != null) {
				if (answer == 1) {
					CoupleManager.getInstance().createCouple(ptarget, L2PcInstance.this);
					ptarget.sendMessage("Request to Engage has been >ACCEPTED<");
				} else {
					ptarget.sendMessage("Request to Engage has been >DENIED<!");
				}
			}
		}
	}
	
	/**
	 * Return the secondary weapon instance (always equiped in the left hand).
	 */
	@Override
	public L2ItemInstance getSecondaryWeaponInstance() {
		return getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
	}
	
	/**
	 * Return the secondary L2Item item (always equiped in the left hand).<BR>
	 * Arrows, Shield..<BR>
	 */
	@Override
	public L2Item getSecondaryWeaponItem() {
		L2ItemInstance item = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (item != null) {
			return item.getItem();
		}
		return null;
	}
	
	/**
	 * Kill the L2Character, Apply Death Penalty, Manage gain/loss Karma and Item Drop. <B><U> Actions</U> :</B>
	 * <li>Reduce the Experience of the L2PcInstance in function of the calculated Death Penalty</li>
	 * <li>If necessary, unsummon the Pet of the killed L2PcInstance</li>
	 * <li>Manage Karma gain for attacker and Karam loss for the killed L2PcInstance</li>
	 * <li>If the killed L2PcInstance has Karma, manage Drop Item</li>
	 * <li>Kill the L2PcInstance</li>
	 * @param killer
	 */
	@Override
	public boolean doDie(L2Character killer) {
		// Kill the L2PcInstance
		if (!super.doDie(killer)) {
			return false;
		}
		
		if (killer != null) {
			final L2PcInstance pk = killer.getActingPlayer();
			if (pk != null) {
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerPvPKill(pk, this), this);
				
				TvTEvent.onKill(killer, this);
				
				if (L2Event.isParticipant(pk)) {
					pk.getEventStatus().getKills().add(this);
				}
			}
			
			broadcastStatusUpdate();
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			// Issues drop of Cursed Weapon.
			if (isCursedWeaponEquipped()) {
				CursedWeaponsManager.getInstance().drop(_cursedWeaponEquippedId, killer);
			} else if (isCombatFlagEquipped()) {
				// TODO: Fort siege during TW??
				if (TerritoryWarManager.getInstance().isTWInProgress()) {
					TerritoryWarManager.getInstance().dropCombatFlag(this, true, false);
				} else {
					Fort fort = FortManager.getInstance().getFort(this);
					if (fort != null) {
						FortSiegeManager.getInstance().dropCombatFlag(this, fort.getResidenceId());
					} else {
						int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
						getInventory().unEquipItemInBodySlot(slot);
						destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
					}
				}
			} else {
				final boolean insidePvpZone = isInsideZone(ZoneId.PVP);
				final boolean insideSiegeZone = isInsideZone(ZoneId.SIEGE);
				if ((pk == null) || !pk.isCursedWeaponEquipped()) {
					onDieDropItem(killer); // Check if any item should be dropped
					
					if (!insidePvpZone && !insideSiegeZone) {
						if ((pk != null) && (pk.getClan() != null) && (getClan() != null) && !isAcademyMember() && !(pk.isAcademyMember())) {
							if ((_clan.isAtWarWith(pk.getClanId()) && pk.getClan().isAtWarWith(_clan.getId())) || (isInSiege() && pk.isInSiege())) {
								if (AntiFeedManager.getInstance().check(killer, this)) {
									// when your reputation score is 0 or below, the other clan cannot acquire any reputation points
									if (getClan().getReputationScore() > 0) {
										pk.getClan().addReputationScore(clan().getReputationScorePerKill(), false);
									}
									// when the opposing sides reputation score is 0 or below, your clans reputation score does not decrease
									if (pk.getClan().getReputationScore() > 0) {
										_clan.takeReputationScore(clan().getReputationScorePerKill(), false);
									}
								}
							}
						}
					}
					// If player is Lucky shouldn't get penalized.
					if (character().delevel() && !isLucky() && (insideSiegeZone || !insidePvpZone)) {
						calculateDeathExpPenalty(killer, isAtWarWith(pk));
					}
				}
			}
		}
		
		if (isMounted()) {
			stopFeed();
		}
		synchronized (this) {
			if (isFakeDeath()) {
				stopFakeDeath(true);
			}
		}
		
		// Unsummon Cubics
		if (!_cubics.isEmpty()) {
			for (L2CubicInstance cubic : _cubics.values()) {
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}
		
		if (isChannelized()) {
			getSkillChannelized().abortChannelization();
		}
		
		if (isInParty() && getParty().isInDimensionalRift()) {
			getParty().getDimensionalRift().getDeadMemberList().add(this);
		}
		
		if (getAgathionId() != 0) {
			setAgathionId(0);
		}
		
		// calculate death penalty buff
		calculateDeathPenaltyBuffLevel(killer);
		
		stopRentPet();
		stopWaterTask();
		
		AntiFeedManager.getInstance().setLastDeathTime(getObjectId());
		
		return true;
	}
	
	private void onDieDropItem(L2Character killer) {
		if (L2Event.isParticipant(this) || (killer == null)) {
			return;
		}
		
		L2PcInstance pk = killer.getActingPlayer();
		if ((getKarma() <= 0) && (pk != null) && (pk.getClan() != null) && (getClan() != null) && (pk.getClan().isAtWarWith(getClanId())
		// || getClan().isAtWarWith(((L2PcInstance)killer).getClanId())
		)) {
			return;
		}
		
		if ((!isInsideZone(ZoneId.PVP) || (pk == null)) && (!isGM() || pvp().canGMDropEquipment())) {
			boolean isKarmaDrop = false;
			boolean isKillerNpc = (killer instanceof L2Npc);
			int pkLimit = pvp().getMinimumPKRequiredToDrop();
			
			int dropEquip = 0;
			int dropEquipWeapon = 0;
			int dropItem = 0;
			int dropLimit = 0;
			int dropPercent = 0;
			
			if ((getKarma() > 0) && (getPkKills() >= pkLimit)) {
				isKarmaDrop = true;
				dropPercent = rates().getKarmaRateDrop();
				dropEquip = rates().getKarmaRateDropEquip();
				dropEquipWeapon = rates().getKarmaRateDropEquipWeapon();
				dropItem = rates().getKarmaRateDropItem();
				dropLimit = rates().getKarmaDropLimit();
			} else if (isKillerNpc && (getLevel() > 4) && !isFestivalParticipant()) {
				dropPercent = rates().getPlayerRateDrop();
				dropEquip = rates().getPlayerRateDropEquip();
				dropEquipWeapon = rates().getPlayerRateDropEquipWeapon();
				dropItem = rates().getPlayerRateDropItem();
				dropLimit = rates().getPlayerDropLimit();
			}
			
			if ((dropPercent > 0) && (Rnd.get(100) < dropPercent)) {
				int dropCount = 0;
				
				int itemDropPercent = 0;
				
				for (L2ItemInstance itemDrop : getInventory().getItems()) {
					// Don't drop:
					// Adena
					// Shadow Items
					// Time Limited Items
					// Quest Items
					// Control Item of active pet
					// Item listed in the non droppable item list
					// Item listed in the non droppable pet item list
					if (itemDrop.isShadowItem() || itemDrop.isTimeLimitedItem() || !itemDrop.isDropable() || (itemDrop.getId() == Inventory.ADENA_ID) || //
						(itemDrop.getItem().getType2() == ItemType2.QUEST) || (hasSummon() && (getSummon().getControlObjectId() == itemDrop.getId())) || //
						pvp().getNonDroppableItems().contains(itemDrop.getId()) || pvp().getPetItems().contains(itemDrop.getId())) {
						continue;
					}
					
					if (itemDrop.isEquipped()) {
						// Set proper chance according to Item type of equipped Item
						itemDropPercent = itemDrop.getItem().getType2() == ItemType2.WEAPON ? dropEquipWeapon : dropEquip;
						getInventory().unEquipItemInSlot(itemDrop.getLocationSlot());
					} else {
						itemDropPercent = dropItem; // Item in inventory
					}
					
					// NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
					if (Rnd.get(100) < itemDropPercent) {
						dropItem("DieDrop", itemDrop, killer, true);
						
						if (isKarmaDrop) {
							LOG.info("{} has karma and dropped id = {}, count = {}", this, itemDrop.getId(), itemDrop.getCount());
						} else {
							LOG.info("{} dropped id = {}, count = {}", this, itemDrop.getId(), itemDrop.getCount());
						}
						
						if (++dropCount >= dropLimit) {
							break;
						}
					}
				}
			}
		}
	}
	
	public void onKillUpdatePvPKarma(L2Character target) {
		if ((target == null) || !target.isPlayable()) {
			return;
		}
		
		L2PcInstance targetPlayer = target.getActingPlayer();
		if ((targetPlayer == null) || (targetPlayer == this)) {
			return;
		}
		
		if (isCursedWeaponEquipped() && target.isPlayer()) {
			CursedWeaponsManager.getInstance().increaseKills(_cursedWeaponEquippedId);
			return;
		}
		
		// If in duel and you kill (only can kill l2summon), do nothing
		if (isInDuel() && targetPlayer.isInDuel()) {
			return;
		}
		
		// If in Arena, do nothing
		if (isInsideZone(ZoneId.PVP) || targetPlayer.isInsideZone(ZoneId.PVP)) {
			if ((getSiegeState() > 0) && (targetPlayer.getSiegeState() > 0) && (getSiegeState() != targetPlayer.getSiegeState())) {
				final L2Clan killerClan = getClan();
				final L2Clan targetClan = targetPlayer.getClan();
				if ((killerClan != null) && (targetClan != null)) {
					killerClan.addSiegeKill();
					targetClan.addSiegeDeath();
				}
			}
			return;
		}
		
		// Check if it's pvp
		if ((checkIfPvP(target) && (targetPlayer.getPvpFlag() != 0)) || (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))) {
			increasePvpKills(target);
		} else {
			// Target player doesn't have pvp flag set check about wars
			if ((targetPlayer.getClan() != null) && (getClan() != null) && getClan().isAtWarWith(targetPlayer.getClanId()) && targetPlayer.getClan().isAtWarWith(getClanId()) && (targetPlayer.getPledgeType() != L2Clan.SUBUNIT_ACADEMY) && (getPledgeType() != L2Clan.SUBUNIT_ACADEMY)) {
				// 'Both way war' -> 'PvP Kill'
				increasePvpKills(target);
				return;
			}
			
			// 'No war' or 'One way war' -> 'Normal PK'
			if (targetPlayer.getKarma() > 0) {
				// Target player has karma
				if (pvp().awardPKKillPVPPoint()) {
					increasePvpKills(target);
				}
			} else if (targetPlayer.getPvpFlag() == 0) {
				// Target player doesn't have karma
				increasePkKillsAndKarma(target);
				stopPvPFlag();
				// Unequip adventurer items
				checkItemRestriction();
			}
		}
	}
	
	/**
	 * Increase the pvp kills count and send the info to the player
	 * @param target
	 */
	public void increasePvpKills(L2Character target) {
		if ((target instanceof L2PcInstance) && AntiFeedManager.getInstance().check(this, target)) {
			setPvpKills(getPvpKills() + 1);
			
			// Send a Server->Client UserInfo packet to attacker with its Karma and PK Counter
			sendPacket(new UserInfo(this));
			sendPacket(new ExBrExtraUserInfo(this));
		}
	}
	
	/**
	 * Increase pk count, karma and send the info to the player
	 * @param target
	 */
	public void increasePkKillsAndKarma(L2Character target) {
		// Only playables can increase karma/pk
		if ((target == null) || !target.isPlayable()) {
			return;
		}
		
		// Calculate new karma. (calculate karma before incrase pk count!)
		setKarma(getKarma() + Formulas.calculateKarmaGain(getPkKills(), target.isSummon()));
		
		// PK Points are increased only if you kill a player.
		if (target.isPlayer()) {
			setPkKills(getPkKills() + 1);
		}
		
		// Update player's UI.
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
	}
	
	public void updatePvPStatus() {
		if (isInsideZone(ZoneId.PVP)) {
			return;
		}
		
		setPvpFlagLasts(System.currentTimeMillis() + pvp().getPvPVsNormalTime());
		
		if (getPvpFlag() == 0) {
			startPvPFlag();
		}
	}
	
	public void updatePvPStatus(L2Character target) {
		L2PcInstance player_target = target.getActingPlayer();
		
		if (player_target == null) {
			return;
		}
		
		if ((isInDuel() && (player_target.getDuelId() == getDuelId()))) {
			return;
		}
		if ((!isInsideZone(ZoneId.PVP) || !player_target.isInsideZone(ZoneId.PVP)) && (player_target.getKarma() == 0)) {
			if (checkIfPvP(player_target)) {
				setPvpFlagLasts(System.currentTimeMillis() + pvp().getPvPVsPvPTime());
			} else {
				setPvpFlagLasts(System.currentTimeMillis() + pvp().getPvPVsNormalTime());
			}
			if (getPvpFlag() == 0) {
				startPvPFlag();
			}
		}
	}
	
	/**
	 * @return {@code true} if player has Lucky effect and is level 9 or less
	 */
	public boolean isLucky() {
		return (getLevel() <= 9) && isAffectedBySkill(CommonSkill.LUCKY.getId());
	}
	
	/**
	 * Restore the specified % of experience this L2PcInstance has lost and sends a Server->Client StatusUpdate packet.
	 * @param restorePercent
	 */
	public void restoreExp(double restorePercent) {
		if (getExpBeforeDeath() > 0) {
			getSubStat().addExp(Math.round(((getExpBeforeDeath() - getExp()) * restorePercent) / 100));
			setExpBeforeDeath(0);
		}
	}
	
	@Override
	public void addExpAndSp(long addToExp, int addToSp) {
		addExpAndSp(addToExp, addToSp, false);
	}
	
	/**
	 * Used for quest no bonus and no pet consume
	 * @param addToExp
	 * @param addToSp
	 * @param useBonuses
	 */
	public final void addExpAndSpQuest(long addToExp, int addToSp) {
		if (addToExp != 0) {
			getSubStat().addExp(addToExp);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE);
			sm.addLong(addToExp);
			sendPacket(sm);
		}
		
		if (addToSp != 0) {
			getSubStat().addSp(addToSp);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP);
			sm.addInt(addToSp);
			sendPacket(sm);
		}
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
	}
	
	public final void removeExpAndSp(long removeFromExp, int removeFromSp) {
		getSubStat().removeExp(removeFromExp);
		getSubStat().removeSp(removeFromSp);
		
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
	}
	
	public final void addExpAndSp(long addToExp, int addToSp, boolean useBonuses) {
		// Allowed to gain exp?
		if (!getAccessLevel().canGainExp()) {
			return;
		}
		changeKarma(addToExp);
		
		long baseExp = addToExp;
		int baseSp = addToSp;
		
		if (useBonuses) {
			addToExp *= getStat().getExpBonusMultiplier();
			addToSp *= getStat().getSpBonusMultiplier();
		}
		
		float ratioTakenByPlayer = 0;
		
		// if this player has a pet and it is in his range he takes from the owner's Exp, give the pet Exp now
		if (hasPet() && Util.checkIfInShortRadius(character().getPartyRange(), this, getSummon(), false)) {
			L2PetInstance pet = (L2PetInstance) getSummon();
			ratioTakenByPlayer = pet.getPetLevelData().getOwnerExpTaken() / 100f;
			
			// only give exp/sp to the pet by taking from the owner if the pet has a non-zero, positive ratio
			// allow possible customizations that would have the pet earning more than 100% of the owner's exp/sp
			if (ratioTakenByPlayer > 1) {
				ratioTakenByPlayer = 1;
			}
			
			if (!pet.isDead()) {
				pet.addExpAndSp((long) (addToExp * (1 - ratioTakenByPlayer)), (int) (addToSp * (1 - ratioTakenByPlayer)));
			}
			
			// now adjust the max ratio to avoid the owner earning negative exp/sp
			baseExp = (long) (addToExp * ratioTakenByPlayer);
			baseSp = (int) (addToSp * ratioTakenByPlayer);
			addToExp = (long) (addToExp * ratioTakenByPlayer);
			addToSp = (int) (addToSp * ratioTakenByPlayer);
		}
		
		getSubStat().addExp(addToExp);
		getSubStat().addSp(addToSp);
		
		SystemMessage sm = null;
		
		sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_BONUS_S2_AND_S3_SP_BONUS_S4);
		sm.addLong(addToExp);
		sm.addLong(addToExp - baseExp);
		sm.addInt(addToSp);
		sm.addInt(addToSp - baseSp);
		
		sendPacket(sm);
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
	}
	
	public final void removeExp(long exp) {
		changeKarma(exp);
		getSubStat().removeExp(exp);
		
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
	}
	
	private void changeKarma(long exp) {
		if (!isCursedWeaponEquipped() && (getKarma() > 0) && (isGM() || !isInsideZone(ZoneId.PVP))) {
			int karmaLost = Formulas.calculateKarmaLost(this, exp);
			if (karmaLost > 0) {
				setKarma(getKarma() - karmaLost);
				final SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1);
				msg.addInt(getKarma());
				sendPacket(msg);
			}
		}
	}
	
	public final boolean removeSp(int sp) {
		return getSubStat().removeSp(sp);
	}
	
	//
	/**
	 * Reduce the Experience (and level if necessary) of the L2PcInstance in function of the calculated Death Penalty.<BR>
	 * <B><U> Actions</U> :</B>
	 * <li>Calculate the Experience loss</li>
	 * <li>Set the value of _expBeforeDeath</li>
	 * <li>Set the new Experience value of the L2PcInstance and Decrease its level if necessary</li>
	 * <li>Send a Server->Client StatusUpdate packet with its new Experience</li>
	 * @param killer
	 * @param atWar
	 */
	public void calculateDeathExpPenalty(L2Character killer, boolean atWar) {
		final int lvl = getLevel();
		double percentLost = PlayerXpPercentLostData.getInstance().getXpPercent(getLevel());
		
		if (killer != null) {
			if (killer.isRaid()) {
				percentLost *= calcStat(Stats.REDUCE_EXP_LOST_BY_RAID, 1);
			} else if (killer.isMonster()) {
				percentLost *= calcStat(Stats.REDUCE_EXP_LOST_BY_MOB, 1);
			} else if (killer.isPlayable()) {
				percentLost *= calcStat(Stats.REDUCE_EXP_LOST_BY_PVP, 1);
			}
		}
		
		if (getKarma() > 0) {
			percentLost *= rates().getRateKarmaExpLost();
		}
		
		// Calculate the Experience loss
		long lostExp = 0;
		if (!L2Event.isParticipant(this)) {
			if (lvl < character().getMaxPlayerLevel()) {
				lostExp = Math.round(((getStat().getExpForLevel(lvl + 1) - getStat().getExpForLevel(lvl)) * percentLost) / 100);
			} else {
				lostExp = Math.round(((getStat().getExpForLevel(character().getMaxPlayerLevel() + 1) - getStat().getExpForLevel(character().getMaxPlayerLevel())) * percentLost) / 100);
			}
		}
		
		if (isFestivalParticipant() || atWar) {
			lostExp /= 4.0;
		}
		
		setExpBeforeDeath(getExp());
		
		removeExp(lostExp);
	}
	
	public boolean isPartyWaiting() {
		return PartyMatchWaitingList.getInstance().getPlayers().contains(this);
	}
	
	public int getPartyRoom() {
		return _partyroom;
	}
	
	public void setPartyRoom(int id) {
		_partyroom = id;
	}
	
	public boolean isInPartyMatchRoom() {
		return _partyroom > 0;
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task. <B><U> Actions</U> :</B>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 */
	public void stopAllTimers() {
		stopHpMpRegeneration();
		stopWarnUserTakeBreak();
		stopWaterTask();
		stopFeed();
		DAOFactory.getInstance().getPetDAO().updateFood(this, _mountNpcId);
		stopRentPet();
		stopPvpRegTask();
		stopSoulTask();
		stopChargeTask();
		stopFameTask();
		stopVitalityTask();
		stopRecoBonusTask();
		stopRecoGiveTask();
	}
	
	@Override
	public L2Summon getSummon() {
		return _summon;
	}
	
	/**
	 * @return the L2Decoy of the L2PcInstance or null.
	 */
	public L2Decoy getDecoy() {
		return _decoy;
	}
	
	/**
	 * Set the L2Decoy of the L2PcInstance.
	 * @param decoy
	 */
	public void setDecoy(L2Decoy decoy) {
		_decoy = decoy;
	}
	
	/**
	 * @return the L2Trap of the L2PcInstance or null.
	 */
	public L2TrapInstance getTrap() {
		return _trap;
	}
	
	/**
	 * Set the L2Trap of this L2PcInstance
	 * @param trap
	 */
	public void setTrap(L2TrapInstance trap) {
		_trap = trap;
	}
	
	/**
	 * Set the L2Summon of the L2PcInstance.
	 * @param summon
	 */
	public void setPet(L2Summon summon) {
		_summon = summon;
	}
	
	/**
	 * Gets the players tamed beasts.
	 * @return the tamed beasts
	 */
	public Set<L2TamedBeastInstance> getTamedBeasts() {
		if (_tamedBeasts == null) {
			synchronized (this) {
				if (_tamedBeasts == null) {
					_tamedBeasts = ConcurrentHashMap.newKeySet(1);
				}
			}
		}
		return _tamedBeasts;
	}
	
	/**
	 * Verifies if the player has tamed beasts.
	 * @return {@code true} if the player has tamed beasts
	 */
	public boolean hasTamedBeasts() {
		return (_tamedBeasts != null) && !_tamedBeasts.isEmpty();
	}
	
	/**
	 * Adds a tamed beast to the player.
	 * @param tamedBeast the tamed beast
	 */
	public void addTamedBeast(L2TamedBeastInstance tamedBeast) {
		getTamedBeasts().add(tamedBeast);
	}
	
	public void removeTamedBeast(L2TamedBeastInstance tamedBeast) {
		if (hasTamedBeasts()) {
			_tamedBeasts.remove(tamedBeast);
		}
	}
	
	/**
	 * @return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2Request getRequest() {
		return _request;
	}
	
	/**
	 * @return the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 */
	public L2PcInstance getActiveRequester() {
		L2PcInstance requester = _activeRequester;
		if (requester != null) {
			if (requester.isRequestExpired() && (_activeTradeList == null)) {
				_activeRequester = null;
			}
		}
		return _activeRequester;
	}
	
	/**
	 * Set the L2PcInstance requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
	 * @param requester
	 */
	public void setActiveRequester(L2PcInstance requester) {
		_activeRequester = requester;
	}
	
	/**
	 * @return True if a transaction is in progress.
	 */
	public boolean isProcessingRequest() {
		return (getActiveRequester() != null) || (_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	/**
	 * @return True if a transaction is in progress.
	 */
	public boolean isProcessingTransaction() {
		return (getActiveRequester() != null) || (_activeTradeList != null) || (_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 * @param partner
	 */
	public void onTransactionRequest(L2PcInstance partner) {
		_requestExpireTime = GameTimeController.getInstance().getGameTicks() + (REQUEST_TIMEOUT * GameTimeController.TICKS_PER_SECOND);
		partner.setActiveRequester(this);
	}
	
	/**
	 * Return true if last request is expired.
	 * @return
	 */
	public boolean isRequestExpired() {
		return !(_requestExpireTime > GameTimeController.getInstance().getGameTicks());
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 */
	public void onTransactionResponse() {
		_requestExpireTime = 0;
	}
	
	/**
	 * @return active Warehouse.
	 */
	public ItemContainer getActiveWarehouse() {
		return _activeWarehouse;
	}
	
	/**
	 * Select the Warehouse to be used in next activity.
	 * @param warehouse
	 */
	public void setActiveWarehouse(ItemContainer warehouse) {
		_activeWarehouse = warehouse;
	}
	
	/**
	 * @return active TradeList.
	 */
	public TradeList getActiveTradeList() {
		return _activeTradeList;
	}
	
	/**
	 * Select the TradeList to be used in next activity.
	 * @param tradeList
	 */
	public void setActiveTradeList(TradeList tradeList) {
		_activeTradeList = tradeList;
	}
	
	public void onTradeStart(L2PcInstance partner) {
		_activeTradeList = new TradeList(this);
		_activeTradeList.setPartner(partner);
		
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_C1);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(new TradeStart(this));
	}
	
	public void onTradeConfirm(L2PcInstance partner) {
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CONFIRMED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
		sendPacket(TradeOtherDone.STATIC_PACKET);
	}
	
	public void onTradeCancel(L2PcInstance partner) {
		if (_activeTradeList == null) {
			return;
		}
		
		_activeTradeList.lock();
		_activeTradeList = null;
		
		sendPacket(new TradeDone(0));
		SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.C1_CANCELED_TRADE);
		msg.addPcName(partner);
		sendPacket(msg);
	}
	
	public void onTradeFinish(boolean successfull) {
		_activeTradeList = null;
		sendPacket(new TradeDone(1));
		if (successfull) {
			sendPacket(SystemMessageId.TRADE_SUCCESSFUL);
		}
	}
	
	public void startTrade(L2PcInstance partner) {
		onTradeStart(partner);
		partner.onTradeStart(this);
	}
	
	public void cancelActiveTrade() {
		if (_activeTradeList == null) {
			return;
		}
		
		L2PcInstance partner = _activeTradeList.getPartner();
		if (partner != null) {
			partner.onTradeCancel(this);
		}
		onTradeCancel(this);
	}
	
	public boolean hasManufactureShop() {
		return (_manufactureItems != null) && !_manufactureItems.isEmpty();
	}
	
	/**
	 * Get the manufacture items map of this player.
	 * @return the the manufacture items map
	 */
	public Map<Integer, L2ManufactureItem> getManufactureItems() {
		if (_manufactureItems == null) {
			synchronized (this) {
				if (_manufactureItems == null) {
					_manufactureItems = Collections.synchronizedMap(new LinkedHashMap<Integer, L2ManufactureItem>());
				}
			}
		}
		return _manufactureItems;
	}
	
	/**
	 * Get the store name, if any.
	 * @return the store name
	 */
	public String getStoreName() {
		return _storeName;
	}
	
	/**
	 * Set the store name.
	 * @param name the store name to set
	 */
	public void setStoreName(String name) {
		_storeName = name == null ? "" : name;
	}
	
	/**
	 * @return the _buyList object of the L2PcInstance.
	 */
	public TradeList getSellList() {
		if (_sellList == null) {
			_sellList = new TradeList(this);
		}
		return _sellList;
	}
	
	/**
	 * @return the _buyList object of the L2PcInstance.
	 */
	public TradeList getBuyList() {
		if (_buyList == null) {
			_buyList = new TradeList(this);
		}
		return _buyList;
	}
	
	/**
	 * <B><U> Values </U> :</B>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 * @return the Private Store type of the L2PcInstance.
	 */
	public PrivateStoreType getPrivateStoreType() {
		return _privateStoreType;
	}
	
	/**
	 * Set the Private Store type of the L2PcInstance. <B><U> Values </U> :</B>
	 * <li>0 : STORE_PRIVATE_NONE</li>
	 * <li>1 : STORE_PRIVATE_SELL</li>
	 * <li>2 : sellmanage</li><BR>
	 * <li>3 : STORE_PRIVATE_BUY</li><BR>
	 * <li>4 : buymanage</li><BR>
	 * <li>5 : STORE_PRIVATE_MANUFACTURE</li><BR>
	 * @param privateStoreType
	 */
	public void setPrivateStoreType(PrivateStoreType privateStoreType) {
		_privateStoreType = privateStoreType;
		
		if (customs().offlineDisconnectFinished() && (privateStoreType == PrivateStoreType.NONE) && ((getClient() == null) || getClient().isDetached())) {
			deleteMe();
		}
	}
	
	/**
	 * @return the _clan object of the L2PcInstance.
	 */
	@Override
	public L2Clan getClan() {
		return _clan;
	}
	
	/**
	 * Set the _clan object, _clanId, _clanLeader Flag and title of the L2PcInstance.
	 * @param clan
	 */
	public void setClan(L2Clan clan) {
		_clan = clan;
		if (clan == null) {
			_clanId = 0;
			_clanPrivileges = new EnumIntBitmask<>(ClanPrivilege.class, false);
			_pledgeType = 0;
			_powerGrade = 0;
			_lvlJoinedAcademy = 0;
			_apprentice = 0;
			_sponsor = 0;
			_activeWarehouse = null;
			return;
		}
		
		if (!clan.isMember(getObjectId())) {
			// char has been kicked from clan
			setClan(null);
			return;
		}
		
		_clanId = clan.getId();
	}
	
	/**
	 * @return True if the L2PcInstance is the leader of its clan.
	 */
	public boolean isClanLeader() {
		if (getClan() == null) {
			return false;
		}
		return getObjectId() == getClan().getLeaderId();
	}
	
	/**
	 * Reduce the number of arrows/bolts owned by the L2PcInstance and send it Server->Client Packet InventoryUpdate or ItemList (to unequip if the last arrow was consummed).
	 */
	@Override
	protected void reduceArrowCount(boolean bolts) {
		L2ItemInstance arrows = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		
		if (arrows == null) {
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts) {
				_boltItem = null;
			} else {
				_arrowItem = null;
			}
			sendPacket(new ItemList(this, false));
			return;
		}
		
		// Adjust item quantity
		if (arrows.getCount() > 1) {
			synchronized (arrows) {
				arrows.changeCountWithoutTrace(-1, this, null);
				arrows.setLastChange(L2ItemInstance.MODIFIED);
				
				// could do also without saving, but let's save approx 1 of 10
				if ((GameTimeController.getInstance().getGameTicks() % 10) == 0) {
					arrows.updateDatabase();
				}
				_inventory.refreshWeight();
			}
		} else {
			// Destroy entire item and save to database
			_inventory.destroyItem("Consume", arrows, this, null);
			
			getInventory().unEquipItemInSlot(Inventory.PAPERDOLL_LHAND);
			if (bolts) {
				_boltItem = null;
			} else {
				_arrowItem = null;
			}
			
			sendPacket(new ItemList(this, false));
			return;
		}
		
		if (!general().forceInventoryUpdate()) {
			InventoryUpdate iu = new InventoryUpdate();
			iu.addModifiedItem(arrows);
			sendPacket(iu);
		} else {
			sendPacket(new ItemList(this, false));
		}
	}
	
	/**
	 * Equip arrows needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.
	 */
	@Override
	protected boolean checkAndEquipArrows() {
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null) {
			// Get the L2ItemInstance of the arrows needed for this bow
			_arrowItem = getInventory().findArrowForBow(getActiveWeaponItem());
			if (_arrowItem != null) {
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _arrowItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		} else {
			// Get the L2ItemInstance of arrows equiped in left hand
			_arrowItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		return _arrowItem != null;
	}
	
	/**
	 * Equip bolts needed in left hand and send a Server->Client packet ItemList to the L2PcINstance then return True.
	 */
	@Override
	protected boolean checkAndEquipBolts() {
		// Check if nothing is equiped in left hand
		if (getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND) == null) {
			// Get the L2ItemInstance of the arrows needed for this bow
			_boltItem = getInventory().findBoltForCrossBow(getActiveWeaponItem());
			if (_boltItem != null) {
				// Equip arrows needed in left hand
				getInventory().setPaperdollItem(Inventory.PAPERDOLL_LHAND, _boltItem);
				
				// Send a Server->Client packet ItemList to this L2PcINstance to update left hand equipement
				ItemList il = new ItemList(this, false);
				sendPacket(il);
			}
		} else {
			// Get the L2ItemInstance of arrows equiped in left hand
			_boltItem = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		}
		return _boltItem != null;
	}
	
	/**
	 * Disarm the player's weapon.
	 * @return {@code true} if the player was disarmed or doesn't have a weapon to disarm, {@code false} otherwise.
	 */
	public boolean disarmWeapons() {
		// If there is no weapon to disarm then return true.
		final L2ItemInstance wpn = getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
		if (wpn == null) {
			return true;
		}
		
		// Don't allow disarming a cursed weapon
		if (isCursedWeaponEquipped()) {
			return false;
		}
		
		// Don't allow disarming a Combat Flag or Territory Ward.
		if (isCombatFlagEquipped()) {
			return false;
		}
		
		// Don't allow disarming if the weapon is force equip.
		if (wpn.getWeaponItem().isForceEquip()) {
			return false;
		}
		
		final L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(wpn.getItem().getBodyPart());
		final InventoryUpdate iu = new InventoryUpdate();
		for (L2ItemInstance itm : unequiped) {
			iu.addModifiedItem(itm);
		}
		
		sendPacket(iu);
		abortAttack();
		broadcastUserInfo();
		
		// This can be 0 if the user pressed the right mousebutton twice very fast.
		if (unequiped.length > 0) {
			final SystemMessage sm;
			if (unequiped[0].getEnchantLevel() > 0) {
				sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
				sm.addInt(unequiped[0].getEnchantLevel());
				sm.addItemName(unequiped[0]);
			} else {
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
				sm.addItemName(unequiped[0]);
			}
			sendPacket(sm);
		}
		return true;
	}
	
	/**
	 * Disarm the player's shield.
	 * @return {@code true}.
	 */
	public boolean disarmShield() {
		L2ItemInstance sld = getInventory().getPaperdollItem(Inventory.PAPERDOLL_LHAND);
		if (sld != null) {
			L2ItemInstance[] unequiped = getInventory().unEquipItemInBodySlotAndRecord(sld.getItem().getBodyPart());
			InventoryUpdate iu = new InventoryUpdate();
			for (L2ItemInstance itm : unequiped) {
				iu.addModifiedItem(itm);
			}
			sendPacket(iu);
			
			abortAttack();
			broadcastUserInfo();
			
			// this can be 0 if the user pressed the right mousebutton twice very fast
			if (unequiped.length > 0) {
				SystemMessage sm = null;
				if (unequiped[0].getEnchantLevel() > 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addInt(unequiped[0].getEnchantLevel());
					sm.addItemName(unequiped[0]);
				} else {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(unequiped[0]);
				}
				sendPacket(sm);
			}
		}
		return true;
	}
	
	public boolean mount(L2Summon pet) {
		if (!disarmWeapons() || !disarmShield() || isTransformed()) {
			return false;
		}
		
		getEffectList().stopAllToggles();
		setMount(pet.getId(), pet.getLevel());
		setMountObjectID(pet.getControlObjectId());
		startFeed(pet.getId());
		broadcastPacket(new Ride(this));
		
		// Notify self and others about speed change
		broadcastUserInfo();
		
		pet.unSummon(this);
		return true;
	}
	
	public boolean mount(int npcId, int controlItemObjId, boolean useFood) {
		if (!disarmWeapons() || !disarmShield() || isTransformed()) {
			return false;
		}
		
		getEffectList().stopAllToggles();
		setMount(npcId, getLevel());
		setMountObjectID(controlItemObjId);
		broadcastPacket(new Ride(this));
		
		// Notify self and others about speed change
		broadcastUserInfo();
		if (useFood) {
			startFeed(npcId);
		}
		return true;
	}
	
	public boolean mountPlayer(L2Summon pet) {
		if ((pet != null) && pet.isMountable() && !isMounted() && !isBetrayed()) {
			if (isDead()) {
				// A strider cannot be ridden when dead
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD);
				return false;
			} else if (pet.isDead()) {
				// A dead strider cannot be ridden.
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN);
				return false;
			} else if (pet.isInCombat() || pet.isRooted()) {
				// A strider in battle cannot be ridden
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN);
				return false;
				
			} else if (isInCombat()) {
				// A strider cannot be ridden while in battle
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE);
				return false;
			} else if (isSitting()) {
				// A strider can be ridden only when standing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING);
				return false;
			} else if (isFishing()) {
				// You can't mount, dismount, break and drop items while fishing
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
				return false;
			} else if (isTransformed() || isCursedWeaponEquipped()) {
				// no message needed, player while transformed doesn't have mount action
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			} else if (getInventory().getItemByItemId(9819) != null) {
				sendPacket(ActionFailed.STATIC_PACKET);
				// FIXME: Wrong Message
				sendMessage("You cannot mount a steed while holding a flag.");
				return false;
			} else if (pet.isHungry()) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			} else if (!Util.checkIfInRange(200, this, pet, true)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_FENRIR_TO_MOUNT);
				return false;
			} else if (!pet.isDead() && !isMounted()) {
				mount(pet);
			}
		} else if (isRentedPet()) {
			stopRentPet();
		} else if (isMounted()) {
			if ((getMountType() == MountType.WYVERN) && isInsideZone(ZoneId.NO_LANDING)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.NO_DISMOUNT_HERE);
				return false;
			} else if (isHungry()) {
				sendPacket(ActionFailed.STATIC_PACKET);
				sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT);
				return false;
			} else {
				dismount();
			}
		}
		return true;
	}
	
	public boolean dismount() {
		boolean wasFlying = isFlying();
		
		sendPacket(new SetupGauge(3, 0, 0));
		int petId = _mountNpcId;
		setMount(0, 0);
		stopFeed();
		if (wasFlying) {
			removeSkill(CommonSkill.WYVERN_BREATH.getSkill());
		}
		broadcastPacket(new Ride(this));
		setMountObjectID(0);
		DAOFactory.getInstance().getPetDAO().updateFood(this, petId);
		// Notify self and others about speed change
		broadcastUserInfo();
		return true;
	}
	
	public long getUptime() {
		return System.currentTimeMillis() - _uptime;
	}
	
	public void setUptime(long time) {
		_uptime = time;
	}
	
	/**
	 * Return True if the L2PcInstance is invulnerable.
	 */
	@Override
	public boolean isInvul() {
		return super.isInvul() || _isTeleporting;
	}
	
	/**
	 * Return True if the L2PcInstance has a Party in progress.
	 */
	@Override
	public boolean isInParty() {
		return _party != null;
	}
	
	/**
	 * Set the _party object of the L2PcInstance AND join it.
	 * @param party
	 */
	public void joinParty(L2Party party) {
		if (party != null) {
			// First set the party otherwise this wouldn't be considered
			// as in a party into the L2Character.updateEffectIcons() call.
			_party = party;
			party.addPartyMember(this);
		}
	}
	
	/**
	 * Manage the Leave Party task of the L2PcInstance.
	 */
	public void leaveParty() {
		if (isInParty()) {
			_party.removePartyMember(this, messageType.Disconnected);
			_party = null;
		}
	}
	
	/**
	 * Return the _party object of the L2PcInstance.
	 */
	@Override
	public L2Party getParty() {
		return _party;
	}
	
	/**
	 * Set the _party object of the L2PcInstance (without joining it).
	 * @param party
	 */
	public void setParty(L2Party party) {
		_party = party;
	}
	
	public PartyDistributionType getPartyDistributionType() {
		return _partyDistributionType;
	}
	
	public void setPartyDistributionType(PartyDistributionType pdt) {
		_partyDistributionType = pdt;
	}
	
	/**
	 * Return True if the L2PcInstance is a GM.
	 */
	@Override
	public boolean isGM() {
		return getAccessLevel().isGm();
	}
	
	public void setAccountAccesslevel(int level) {
		LoginServerThread.getInstance().sendAccessLevel(getAccountName(), level);
	}
	
	/**
	 * @return the _accessLevel of the L2PcInstance.
	 */
	@Override
	public L2AccessLevel getAccessLevel() {
		if (general().everybodyHasAdminRights()) {
			return AdminData.getInstance().getMasterAccessLevel();
		} else if (_accessLevel == null) {
			setAccessLevel(0);
		}
		return _accessLevel;
	}
	
	/**
	 * Set the access level for this player.
	 * @param level the access level
	 */
	public void setAccessLevel(int level) {
		_accessLevel = AdminData.getInstance().getAccessLevel(level);
		
		getAppearance().setNameColor(_accessLevel.getNameColor());
		getAppearance().setTitleColor(_accessLevel.getTitleColor());
		broadcastUserInfo();
		
		CharNameTable.getInstance().addName(this);
		
		if (!AdminData.getInstance().hasAccessLevel(level)) {
			LOG.warn("Tried to set unregistered access level {} for {}. Setting access level without privileges!", level, this);
		} else if (level > 0) {
			LOG.info("{} access level set for character {}.", _accessLevel.getName(), this);
		}
	}
	
	/**
	 * Update Stats of the L2PcInstance client side by sending Server->Client packet UserInfo/StatusUpdate to this L2PcInstance and CharInfo/StatusUpdate to all L2PcInstance in its _KnownPlayers (broadcast).
	 * @param broadcastType
	 */
	public void updateAndBroadcastStatus(int broadcastType) {
		refreshOverloaded();
		refreshExpertisePenalty();
		// Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers (broadcast)
		if (broadcastType == 1) {
			sendPacket(new UserInfo(this));
			sendPacket(new ExBrExtraUserInfo(this));
		}
		if (broadcastType == 2) {
			broadcastUserInfo();
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma and PvP Flag to the L2PcInstance and all L2PcInstance to inform (broadcast).
	 * @param flag
	 */
	public void setKarmaFlag(int flag) {
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		Collection<L2PcInstance> plrs = getKnownList().getKnownPlayers().values();
		for (L2PcInstance player : plrs) {
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (hasSummon()) {
				player.sendPacket(new RelationChanged(getSummon(), getRelation(player), isAutoAttackable(player)));
			}
		}
	}
	
	/**
	 * Send a Server->Client StatusUpdate packet with Karma to the L2PcInstance and all L2PcInstance to inform (broadcast).
	 */
	public void broadcastKarma() {
		StatusUpdate su = new StatusUpdate(this);
		su.addAttribute(StatusUpdate.KARMA, getKarma());
		sendPacket(su);
		
		for (L2PcInstance player : getKnownList().getKnownPlayers().values()) {
			player.sendPacket(new RelationChanged(this, getRelation(player), isAutoAttackable(player)));
			if (hasSummon()) {
				player.sendPacket(new RelationChanged(getSummon(), getRelation(player), isAutoAttackable(player)));
			}
		}
	}
	
	/**
	 * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
	 * @param isOnline
	 * @param updateInDb
	 */
	public void setOnlineStatus(boolean isOnline, boolean updateInDb) {
		if (_isOnline != isOnline) {
			_isOnline = isOnline;
		}
		
		// Update the characters table of the database with online status and lastAccess (called when login and logout)
		if (updateInDb) {
			DAOFactory.getInstance().getPlayerDAO().updateOnlineStatus(this);
		}
	}
	
	public void setIsIn7sDungeon(boolean isIn7sDungeon) {
		_isIn7sDungeon = isIn7sDungeon;
	}
	
	public Forum getMail() {
		if (_forumMail == null) {
			setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			
			if (_forumMail == null) {
				ForumsBBSManager.getInstance().createNewForum(getName(), ForumsBBSManager.getInstance().getForumByName("MailRoot"), Forum.MAIL, Forum.OWNERONLY, getObjectId());
				setMail(ForumsBBSManager.getInstance().getForumByName("MailRoot").getChildByName(getName()));
			}
		}
		
		return _forumMail;
	}
	
	public void setMail(Forum forum) {
		_forumMail = forum;
	}
	
	public Forum getMemo() {
		if (_forumMemo == null) {
			setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			
			if (_forumMemo == null) {
				ForumsBBSManager.getInstance().createNewForum(_accountName, ForumsBBSManager.getInstance().getForumByName("MemoRoot"), Forum.MEMO, Forum.OWNERONLY, getObjectId());
				setMemo(ForumsBBSManager.getInstance().getForumByName("MemoRoot").getChildByName(_accountName));
			}
		}
		
		return _forumMemo;
	}
	
	public void setMemo(Forum forum) {
		_forumMemo = forum;
	}
	
	public Map<Integer, L2PremiumItem> getPremiumItemList() {
		return _premiumItems;
	}
	
	/**
	 * Update L2PcInstance stats in the characters table of the database.
	 * @param storeActiveEffects
	 */
	public synchronized void store(boolean storeActiveEffects) {
		DAOFactory.getInstance().getPlayerDAO().storeCharBase(this);
		
		DAOFactory.getInstance().getSubclassDAO().update(this);
		
		storeEffect(storeActiveEffects);
		
		DAOFactory.getInstance().getItemReuseDAO().insert(this);
		
		if (character().storeRecipeShopList()) {
			DAOFactory.getInstance().getRecipeShopListDAO().delete(this);
			DAOFactory.getInstance().getRecipeShopListDAO().insert(this);
		}
		
		if (character().storeUISettings()) {
			storeUISettings();
		}
		
		SevenSigns.getInstance().saveSevenSignsData(getObjectId());
		
		final PlayerVariables vars = getScript(PlayerVariables.class);
		if (vars != null) {
			vars.storeMe();
		}
		
		final AccountVariables aVars = getScript(AccountVariables.class);
		if (aVars != null) {
			aVars.storeMe();
		}
	}
	
	@Override
	public void storeMe() {
		store(true);
	}
	
	@Override
	public void storeEffect(boolean storeEffects) {
		if (!character().storeSkillCooltime()) {
			return;
		}
		
		DAOFactory.getInstance().getPlayerSkillSaveDAO().delete(this);
		
		DAOFactory.getInstance().getPlayerSkillSaveDAO().insert(this, storeEffects);
	}
	
	/**
	 * @return True if the L2PcInstance is on line.
	 */
	public boolean isOnline() {
		return _isOnline;
	}
	
	public int isOnlineInt() {
		if (_isOnline && (_client != null)) {
			return _client.isDetached() ? 2 : 1;
		}
		return 0;
	}
	
	/**
	 * Verifies if the player is in offline mode.<br>
	 * The offline mode may happen for different reasons:<br>
	 * Abnormally: Player gets abruptly disconnected from server.<br>
	 * Normally: The player gets into offline shop mode, only available by enabling the offline shop mod.
	 * @return {@code true} if the player is in offline mode, {@code false} otherwise
	 */
	public boolean isInOfflineMode() {
		return (_client == null) || _client.isDetached();
	}
	
	public boolean isIn7sDungeon() {
		return _isIn7sDungeon;
	}
	
	@Override
	public Skill addSkill(Skill newSkill) {
		addCustomSkill(newSkill);
		return super.addSkill(newSkill);
	}
	
	/**
	 * Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance and save update in the character_skills table of the database. <B><U> Concept</U> :</B> All skills own by a L2PcInstance are identified in <B>_skills</B> <B><U> Actions</U> :</B>
	 * <li>Replace oldSkill by newSkill or Add the newSkill</li>
	 * <li>If an old skill has been replaced, remove all its Func objects of L2Character calculator set</li>
	 * <li>Add Func objects of newSkill to the calculator set of the L2Character</li>
	 * @param newSkill The L2Skill to add to the L2Character
	 * @param store
	 * @return The L2Skill replaced or null if just added a new L2Skill
	 */
	public Skill addSkill(Skill newSkill, boolean store) {
		// Add a skill to the L2PcInstance _skills and its Func objects to the calculator set of the L2PcInstance
		final Skill oldSkill = addSkill(newSkill);
		// Add or update a L2PcInstance skill in the character_skills table of the database
		if (store) {
			storeSkill(newSkill, oldSkill, -1);
		}
		return oldSkill;
	}
	
	@Override
	public Skill removeSkill(Skill skill, boolean store) {
		removeCustomSkill(skill);
		return store ? removeSkill(skill) : super.removeSkill(skill, true);
	}
	
	public Skill removeSkill(Skill skill, boolean store, boolean cancelEffect) {
		removeCustomSkill(skill);
		return store ? removeSkill(skill) : super.removeSkill(skill, cancelEffect);
	}
	
	/**
	 * Remove a skill from the L2Character and its Func objects from calculator set of the L2Character and save update in the character_skills table of the database. <B><U> Concept</U> :</B> All skills own by a L2Character are identified in <B>_skills</B> <B><U> Actions</U> :</B>
	 * <li>Remove the skill from the L2Character _skills</li>
	 * <li>Remove all its Func objects from the L2Character calculator set</li> <B><U> Overridden in </U> :</B>
	 * <li>L2PcInstance : Save update in the character_skills table of the database</li>
	 * @param skill The L2Skill to remove from the L2Character
	 * @return The L2Skill removed
	 */
	public Skill removeSkill(Skill skill) {
		removeCustomSkill(skill);
		// Remove a skill from the L2Character and its Func objects from calculator set of the L2Character
		final Skill oldSkill = super.removeSkill(skill, true);
		if (oldSkill != null) {
			DAOFactory.getInstance().getSkillDAO().delete(this, oldSkill);
		}
		
		if ((getTransformationId() > 0) || isCursedWeaponEquipped()) {
			return oldSkill;
		}
		
		if (skill != null) {
			for (Shortcut sc : getAllShortCuts()) {
				if ((sc != null) && (sc.getId() == skill.getId()) && (sc.getType() == ShortcutType.SKILL) && !((skill.getId() >= 3080) && (skill.getId() <= 3259))) {
					deleteShortCut(sc.getSlot(), sc.getPage());
				}
			}
		}
		return oldSkill;
	}
	
	/**
	 * Add or update a L2PcInstance skill in the character_skills table of the database.<br>
	 * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
	 * @param newSkill
	 * @param oldSkill
	 * @param newClassIndex
	 */
	private void storeSkill(Skill newSkill, Skill oldSkill, int newClassIndex) {
		final int classIndex = (newClassIndex > -1) ? newClassIndex : _classIndex;
		if ((oldSkill != null) && (newSkill != null)) {
			DAOFactory.getInstance().getSkillDAO().update(this, classIndex, newSkill, oldSkill);
		} else if (newSkill != null) {
			DAOFactory.getInstance().getSkillDAO().insert(this, classIndex, newSkill);
		} else {
			LOG.warn("Could not store new skill, it's null!");
		}
	}
	
	/**
	 * Retrieve from the database all skill effects of this L2PcInstance and add them to the player.
	 */
	@Override
	public void restoreEffects() {
		DAOFactory.getInstance().getPlayerSkillSaveDAO().load(this);
		
		DAOFactory.getInstance().getPlayerSkillSaveDAO().delete(this);
	}
	
	/**
	 * @return the number of Henna empty slot of the L2PcInstance.
	 */
	public int getHennaEmptySlots() {
		int totalSlots = 0;
		if (getClassId().level() == 1) {
			totalSlots = 2;
		} else {
			totalSlots = 3;
		}
		
		for (int i = 0; i < 3; i++) {
			if (_henna[i] != null) {
				totalSlots--;
			}
		}
		
		if (totalSlots <= 0) {
			return 0;
		}
		
		return totalSlots;
	}
	
	/**
	 * Remove a henna from the player and updates the database.
	 * @param slot
	 * @return
	 */
	public boolean removeHenna(int slot) {
		if ((slot < 1) || (slot > 3)) {
			return false;
		}
		
		slot--;
		
		L2Henna henna = _henna[slot];
		if (henna == null) {
			return false;
		}
		
		_henna[slot] = null;
		
		DAOFactory.getInstance().getHennaDAO().delete(this, slot + 1);
		
		// Calculate Henna modifiers of this L2PcInstance
		recalcHennaStats();
		
		// Send Server->Client HennaInfo packet to this L2PcInstance
		sendPacket(new HennaInfo(this));
		
		// Send Server->Client UserInfo packet to this L2PcInstance
		sendPacket(new UserInfo(this));
		sendPacket(new ExBrExtraUserInfo(this));
		// Add the recovered dyes to the player's inventory and notify them.
		getInventory().addItem("Henna", henna.getDyeItemId(), henna.getCancelCount(), this, null);
		reduceAdena("Henna", henna.getCancelFee(), this, false);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
		sm.addItemName(henna.getDyeItemId());
		sm.addLong(henna.getCancelCount());
		sendPacket(sm);
		sendPacket(SystemMessageId.SYMBOL_DELETED);
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaRemove(this, henna), this);
		return true;
	}
	
	/**
	 * Adds a henna to the player and stores it into the database.
	 * @param henna the henna to add to the player
	 * @return {@code true} if the henna is added to the player, {@code false} otherwise
	 */
	public boolean addHenna(L2Henna henna) {
		for (int i = 0; i < 3; i++) {
			if (_henna[i] == null) {
				_henna[i] = henna;
				
				// Calculate Henna modifiers of this L2PcInstance
				recalcHennaStats();
				
				DAOFactory.getInstance().getHennaDAO().insert(this, henna, i + 1);
				
				// Send Server->Client HennaInfo packet to this L2PcInstance
				sendPacket(new HennaInfo(this));
				
				// Send Server->Client UserInfo packet to this L2PcInstance
				sendPacket(new UserInfo(this));
				sendPacket(new ExBrExtraUserInfo(this));
				
				// Notify to scripts
				EventDispatcher.getInstance().notifyEventAsync(new OnPlayerHennaRemove(this, henna), this);
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Calculate Henna modifiers of this L2PcInstance.
	 */
	public void recalcHennaStats() {
		_hennaINT = 0;
		_hennaSTR = 0;
		_hennaCON = 0;
		_hennaMEN = 0;
		_hennaWIT = 0;
		_hennaDEX = 0;
		
		for (L2Henna h : _henna) {
			if (h == null) {
				continue;
			}
			
			_hennaINT += ((_hennaINT + h.getStatINT()) > 5) ? 5 - _hennaINT : h.getStatINT();
			_hennaSTR += ((_hennaSTR + h.getStatSTR()) > 5) ? 5 - _hennaSTR : h.getStatSTR();
			_hennaMEN += ((_hennaMEN + h.getStatMEN()) > 5) ? 5 - _hennaMEN : h.getStatMEN();
			_hennaCON += ((_hennaCON + h.getStatCON()) > 5) ? 5 - _hennaCON : h.getStatCON();
			_hennaWIT += ((_hennaWIT + h.getStatWIT()) > 5) ? 5 - _hennaWIT : h.getStatWIT();
			_hennaDEX += ((_hennaDEX + h.getStatDEX()) > 5) ? 5 - _hennaDEX : h.getStatDEX();
		}
	}
	
	/**
	 * @param slot the character inventory henna slot.
	 * @return the Henna of this L2PcInstance corresponding to the selected slot.
	 */
	public L2Henna getHenna(int slot) {
		if ((slot < 1) || (slot > 3)) {
			return null;
		}
		return _henna[slot - 1];
	}
	
	/**
	 * @return {@code true} if player has at least 1 henna symbol, {@code false} otherwise.
	 */
	public boolean hasHennas() {
		for (L2Henna henna : _henna) {
			if (henna != null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return the henna holder for this player.
	 */
	public L2Henna[] getHennaList() {
		return _henna;
	}
	
	/**
	 * @return the INT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatINT() {
		return _hennaINT;
	}
	
	/**
	 * @return the STR Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatSTR() {
		return _hennaSTR;
	}
	
	/**
	 * @return the CON Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatCON() {
		return _hennaCON;
	}
	
	/**
	 * @return the MEN Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatMEN() {
		return _hennaMEN;
	}
	
	/**
	 * @return the WIT Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatWIT() {
		return _hennaWIT;
	}
	
	/**
	 * @return the DEX Henna modifier of this L2PcInstance.
	 */
	public int getHennaStatDEX() {
		return _hennaDEX;
	}
	
	/**
	 * Return True if the L2PcInstance is autoAttackable.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Check if the attacker isn't the L2PcInstance Pet</li>
	 * <li>Check if the attacker is L2MonsterInstance</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same party</li>
	 * <li>Check if the L2PcInstance has Karma</li>
	 * <li>If the attacker is a L2PcInstance, check if it is not in the same siege clan (Attacker, Defender)</li>
	 * </ul>
	 */
	@Override
	public boolean isAutoAttackable(L2Character attacker) {
		if (attacker == null) {
			return false;
		}
		
		// Check if the attacker isn't the L2PcInstance Pet
		if ((attacker == this) || (attacker == getSummon())) {
			return false;
		}
		
		// Friendly mobs doesnt attack players
		if (attacker instanceof L2FriendlyMobInstance) {
			return false;
		}
		
		// Check if the attacker is a L2MonsterInstance
		if (attacker.isMonster()) {
			return true;
		}
		
		// is AutoAttackable if both players are in the same duel and the duel is still going on
		if (attacker.isPlayable() && (getDuelState() == DuelState.DUELLING) && (getDuelId() == attacker.getActingPlayer().getDuelId())) {
			Duel duel = DuelManager.getInstance().getDuel(getDuelId());
			if (duel.getTeamA().contains(this) && duel.getTeamA().contains(attacker)) {
				return false;
			} else if (duel.getTeamB().contains(this) && duel.getTeamB().contains(attacker)) {
				return false;
			}
			return true;
		}
		
		// Check if the attacker is not in the same party. NOTE: Party checks goes before oly checks in order to prevent patry member autoattack at oly.
		if (isInParty() && getParty().getMembers().contains(attacker)) {
			return false;
		}
		
		// Check if the attacker is in olympia and olympia start
		if (attacker.isPlayer() && attacker.getActingPlayer().isInOlympiadMode()) {
			if (isInOlympiadMode() && isOlympiadStart() && (((L2PcInstance) attacker).getOlympiadGameId() == getOlympiadGameId())) {
				return true;
			}
			return false;
		}
		
		// Check if the attacker is in TvT and TvT is started
		if (isOnEvent()) {
			return true;
		}
		
		// Check if the attacker is a L2Playable
		if (attacker.isPlayable()) {
			if (isInsideZone(ZoneId.PEACE)) {
				return false;
			}
			
			// Get L2PcInstance
			L2PcInstance attackerPlayer = attacker.getActingPlayer();
			
			if (getClan() != null) {
				Siege siege = SiegeManager.getInstance().getSiege(getX(), getY(), getZ());
				if (siege != null) {
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Defender clan
					if (siege.checkIsDefender(attackerPlayer.getClan()) && siege.checkIsDefender(getClan())) {
						return false;
					}
					
					// Check if a siege is in progress and if attacker and the L2PcInstance aren't in the Attacker clan
					if (siege.checkIsAttacker(attackerPlayer.getClan()) && siege.checkIsAttacker(getClan())) {
						return false;
					}
				}
				
				// Check if clan is at war
				if ((getClan() != null) && (attackerPlayer.getClan() != null) && getClan().isAtWarWith(attackerPlayer.getClanId()) && attackerPlayer.getClan().isAtWarWith(getClanId()) && (getWantsPeace() == 0) && (attackerPlayer.getWantsPeace() == 0) && !isAcademyMember()) {
					return true;
				}
			}
			
			// Check if the L2PcInstance is in an arena, but NOT siege zone. NOTE: This check comes before clan/ally checks, but after party checks.
			// This is done because in arenas, clan/ally members can autoattack if they arent in party.
			if ((isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP)) && !(isInsideZone(ZoneId.SIEGE) && attackerPlayer.isInsideZone(ZoneId.SIEGE))) {
				return true;
			}
			
			// Check if the attacker is not in the same clan
			if ((getClan() != null) && getClan().isMember(attacker.getObjectId())) {
				return false;
			}
			
			// Check if the attacker is not in the same ally
			if (attacker.isPlayer() && (getAllyId() != 0) && (getAllyId() == attackerPlayer.getAllyId())) {
				return false;
			}
			
			// Now check again if the L2PcInstance is in pvp zone, but this time at siege PvP zone, applying clan/ally checks
			if ((isInsideZone(ZoneId.PVP) && attackerPlayer.isInsideZone(ZoneId.PVP)) && (isInsideZone(ZoneId.SIEGE) && attackerPlayer.isInsideZone(ZoneId.SIEGE))) {
				return true;
			}
		} else if (attacker instanceof L2DefenderInstance) {
			if (getClan() != null) {
				Siege siege = SiegeManager.getInstance().getSiege(this);
				return ((siege != null) && siege.checkIsAttacker(getClan()));
			}
		}
		
		// Check if the L2PcInstance has Karma
		if ((getKarma() > 0) || (getPvpFlag() > 0)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check if the active L2Skill can be casted.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Check if the skill isn't toggle and is offensive</li>
	 * <li>Check if the target is in the skill cast range</li>
	 * <li>Check if the skill is Spoil type and if the target isn't already spoiled</li>
	 * <li>Check if the caster owns enough consumed Item, enough HP and MP to cast the skill</li>
	 * <li>Check if the caster isn't sitting</li>
	 * <li>Check if all skills are enabled and this skill is enabled</li>
	 * <li>Check if the caster own the weapon needed</li>
	 * <li>Check if the skill is active</li>
	 * <li>Check if all casting conditions are completed</li>
	 * <li>Notify the AI with AI_INTENTION_CAST and target</li>
	 * </ul>
	 * @param skill The L2Skill to use
	 * @param forceUse used to force ATTACK on players
	 * @param dontMove used to prevent movement, if not in range
	 */
	@Override
	public boolean useMagic(Skill skill, boolean forceUse, boolean dontMove) {
		// Check if the skill is active
		if (skill.isPassive()) {
			// just ignore the passive skill request. why does the client send it anyway ??
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Casting in Progress *******************************************
		
		// If a skill is currently being used, queue this one if this is not the same
		if (isCastingNow()) {
			SkillUseHolder currentSkill = getCurrentSkill();
			// Check if new skill different from current skill in progress
			if ((currentSkill != null) && (skill.getId() == currentSkill.getSkillId())) {
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			} else if (isSkillDisabled(skill)) {
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Create a new SkillDat object and queue it in the player _queuedSkill
			setQueuedSkill(skill, forceUse, dontMove);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		setIsCastingNow(true);
		// Create a new SkillDat object and set the player _currentSkill
		// This is used mainly to save & queue the button presses, since L2Character has
		// _lastSkillCast which could otherwise replace it
		setCurrentSkill(skill, forceUse, dontMove);
		
		if (getQueuedSkill() != null) {
			setQueuedSkill(null, false, false);
		}
		
		// Check if the target is correct and Notify the AI with AI_INTENTION_CAST and target
		L2Object target = null;
		switch (skill.getTargetType()) {
			case AURA: // AURA, SELF should be cast even if no target has been found
			case FRONT_AURA:
			case BEHIND_AURA:
			case GROUND:
			case SELF:
			case AURA_CORPSE_MOB:
			case COMMAND_CHANNEL:
			case AURA_FRIENDLY:
			case AURA_UNDEAD_ENEMY:
				target = this;
				break;
			default:
				
				// Get the first target of the list
				target = skill.getFirstOfTargetList(this);
				break;
		}
		
		// Notify the AI with AI_INTENTION_CAST and target
		getAI().setIntention(CtrlIntention.AI_INTENTION_CAST, skill, target);
		return true;
	}
	
	private boolean checkUseMagicConditions(Skill skill, boolean forceUse, boolean dontMove) {
		// ************************************* Check Player State *******************************************
		
		// Abnormal effects(ex : Stun, Sleep...) are checked in L2Character useMagic()
		if (isOutOfControl() || isParalyzed() || isStunned() || isSleeping()) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the player is dead
		if (isDead()) {
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		if (isFishing() && !skill.hasEffectType(L2EffectType.FISHING, L2EffectType.FISHING_START)) {
			// Only fishing skills are available
			sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW);
			return false;
		}
		
		if (inObserverMode()) {
			sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			abortCast();
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the caster is sitting
		if (isSitting()) {
			// Send a System Message to the caster
			sendPacket(SystemMessageId.CANT_MOVE_SITTING);
			
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the skill type is toggle.
		if (skill.isToggle() && isAffectedBySkill(skill.getId())) {
			stopSkillEffects(true, skill.getId());
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if the player uses "Fake Death" skill
		// Note: do not check this before TOGGLE reset
		if (isFakeDeath()) {
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Target *******************************************
		// Create and set a L2Object containing the target of the skill
		L2Object target = null;
		L2TargetType sklTargetType = skill.getTargetType();
		Location worldPosition = getCurrentSkillWorldPosition();
		
		if ((sklTargetType == L2TargetType.GROUND) && (worldPosition == null)) {
			LOG.warn("WorldPosition is null for {}, {}", skill.getName(), this);
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		switch (sklTargetType) {
			// Target the player if skill type is AURA, PARTY, CLAN or SELF
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case PARTY:
			case CLAN:
			case PARTY_CLAN:
			case GROUND:
			case SELF:
			case AREA_SUMMON:
			case AURA_CORPSE_MOB:
			case COMMAND_CHANNEL:
			case AURA_FRIENDLY:
			case AURA_UNDEAD_ENEMY:
				target = this;
				break;
			case PET:
			case SERVITOR:
			case SUMMON:
				target = getSummon();
				break;
			default:
				target = getTarget();
				break;
		}
		
		// Check the validity of the target
		if (target == null) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// skills can be used on Walls and Doors only during siege
		if (target.isDoor()) {
			final L2DoorInstance door = (L2DoorInstance) target;
			
			if ((door.getCastle() != null) && (door.getCastle().getResidenceId() > 0)) {
				if (!door.getCastle().getSiege().isInProgress()) {
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			} else if ((door.getFort() != null) && (door.getFort().getResidenceId() > 0)) {
				if (!door.getFort().getSiege().isInProgress() || !door.getIsShowHp()) {
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					return false;
				}
			}
		}
		
		// Are the target and the player in the same duel?
		if (isInDuel()) {
			final L2PcInstance cha = target.getActingPlayer();
			if ((cha != null) && (cha.getDuelId() != getDuelId())) {
				sendMessage("You cannot do this while duelling.");
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		// ************************************* Check skill availability *******************************************
		
		// Check if this skill is enabled (ex : reuse time)
		if (isSkillDisabled(skill)) {
			final SystemMessage sm;
			if (hasSkillReuse(skill.getReuseHashCode())) {
				int remainingTime = (int) (getSkillRemainingReuseTime(skill.getReuseHashCode()) / 1000);
				int hours = remainingTime / 3600;
				int minutes = (remainingTime % 3600) / 60;
				int seconds = (remainingTime % 60);
				if (hours > 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HOURS_S3_MINUTES_S4_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addInt(hours);
					sm.addInt(minutes);
				} else if (minutes > 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTES_S3_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
					sm.addInt(minutes);
				} else {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S2_SECONDS_REMAINING_FOR_REUSE_S1);
					sm.addSkillName(skill);
				}
				
				sm.addInt(seconds);
			} else {
				sm = SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE);
				sm.addSkillName(skill);
			}
			
			sendPacket(sm);
			return false;
		}
		
		// ************************************* Check casting conditions *******************************************
		
		// Check if all casting conditions are completed
		if (!skill.checkCondition(this, target, false)) {
			// Send a Server->Client packet ActionFailed to the L2PcInstance
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// ************************************* Check Skill Type *******************************************
		
		// Check if this is bad magic skill
		if (skill.isBad()) {
			if ((isInsidePeaceZone(this, target)) && !getAccessLevel().allowPeaceAttack()) {
				// If L2Character or target is in a peace zone, send a system message TARGET_IN_PEACEZONE a Server->Client packet ActionFailed
				sendPacket(SystemMessageId.TARGET_IN_PEACEZONE);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (isInOlympiadMode() && !isOlympiadStart()) {
				// if L2PcInstance is in Olympia and the match isn't already start, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if ((target.getActingPlayer() != null) && (getSiegeState() > 0) && isInsideZone(ZoneId.SIEGE) && (target.getActingPlayer().getSiegeState() == getSiegeState()) && (target.getActingPlayer() != this) && (target.getActingPlayer().getSiegeSide() == getSiegeSide())) {
				if (TerritoryWarManager.getInstance().isTWInProgress()) {
					sendPacket(SystemMessageId.YOU_CANNOT_ATTACK_A_MEMBER_OF_THE_SAME_TERRITORY);
				} else {
					sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
				}
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			if (!target.canBeAttacked() && !getAccessLevel().allowPeaceAttack() && !target.isDoor()) {
				// If target is not attackable, send a Server->Client packet ActionFailed
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Check for Event Mob's
			if ((target instanceof L2EventMonsterInstance) && ((L2EventMonsterInstance) target).eventSkillAttackBlocked()) {
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
			
			// Check if a Forced ATTACK is in progress on non-attackable target
			if (!target.isAutoAttackable(this) && !forceUse) {
				switch (sklTargetType) {
					case AURA:
					case FRONT_AURA:
					case BEHIND_AURA:
					case AURA_CORPSE_MOB:
					case CLAN:
					case PARTY:
					case SELF:
					case GROUND:
					case AREA_SUMMON:
					case UNLOCKABLE:
					case AURA_FRIENDLY:
					case AURA_UNDEAD_ENEMY:
						break;
					default: // Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
				}
			}
			
			// Check if the target is in the skill cast range
			if (dontMove) {
				// Calculate the distance between the L2PcInstance and the target
				if (sklTargetType == L2TargetType.GROUND) {
					if (!isInsideRadius(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), skill.getCastRange() + getTemplate().getCollisionRadius(), false, false)) {
						// Send a System Message to the caster
						sendPacket(SystemMessageId.TARGET_TOO_FAR);
						
						// Send a Server->Client packet ActionFailed to the L2PcInstance
						sendPacket(ActionFailed.STATIC_PACKET);
						return false;
					}
				} else if ((skill.getCastRange() > 0) && !isInsideRadius(target, skill.getCastRange() + getTemplate().getCollisionRadius(), false, false)) {
					// Send a System Message to the caster
					sendPacket(SystemMessageId.TARGET_TOO_FAR);
					
					// Send a Server->Client packet ActionFailed to the L2PcInstance
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			}
		}
		
		// Check if the skill is a good magic, target is a monster and if force attack is set, if not then we don't want to cast.
		if ((skill.getEffectPoint() > 0) && target.isMonster() && !forceUse) {
			sendPacket(ActionFailed.STATIC_PACKET);
			return false;
		}
		
		// Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
		switch (sklTargetType) {
			case PARTY:
			case CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case PARTY_CLAN: // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
			case AURA:
			case FRONT_AURA:
			case BEHIND_AURA:
			case AREA_SUMMON:
			case GROUND:
			case SELF:
			case ENEMY:
				break;
			default:
				// Verify that player can attack a player or summon
				if (target.isPlayable() && !getAccessLevel().allowPeaceAttack() && !checkPvpSkill(target, skill)) {
					// Send a System Message to the player
					sendPacket(SystemMessageId.INCORRECT_TARGET);
					
					// Send a Server->Client packet ActionFailed to the player
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
		}
		
		// GeoData Los Check here
		if (skill.getCastRange() > 0) {
			if (sklTargetType == L2TargetType.GROUND) {
				if (!GeoData.getInstance().canSeeTarget(this, worldPosition)) {
					sendPacket(SystemMessageId.CANT_SEE_TARGET);
					sendPacket(ActionFailed.STATIC_PACKET);
					return false;
				}
			} else if (!GeoData.getInstance().canSeeTarget(this, target)) {
				sendPacket(SystemMessageId.CANT_SEE_TARGET);
				sendPacket(ActionFailed.STATIC_PACKET);
				return false;
			}
		}
		
		if ((skill.isFlyType()) && !GeoData.getInstance().canMove(this, target)) {
			sendPacket(SystemMessageId.THE_TARGET_IS_LOCATED_WHERE_YOU_CANNOT_CHARGE);
			return false;
		}
		
		// finally, after passing all conditions
		return true;
	}
	
	public boolean isInLooterParty(int LooterId) {
		L2PcInstance looter = L2World.getInstance().getPlayer(LooterId);
		
		// if L2PcInstance is in a CommandChannel
		if (isInParty() && getParty().isInCommandChannel() && (looter != null)) {
			return getParty().getCommandChannel().getMembers().contains(looter);
		}
		
		if (isInParty() && (looter != null)) {
			return getParty().getMembers().contains(looter);
		}
		
		return false;
	}
	
	/**
	 * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
	 * @param target L2Object instance containing the target
	 * @param skill L2Skill instance with the skill being casted
	 * @return {@code false} if the skill is a pvpSkill and target is not a valid pvp target, {@code true} otherwise.
	 */
	public boolean checkPvpSkill(L2Object target, Skill skill) {
		if ((skill == null) || (target == null)) {
			return false;
		}
		
		if (!target.isPlayable()) {
			return true;
		}
		
		if (skill.isDebuff() || skill.hasEffectType(L2EffectType.STEAL_ABNORMAL) || skill.isBad()) {
			final L2PcInstance targetPlayer = target.getActingPlayer();
			
			if ((targetPlayer == null) || (this == target)) {
				return false;
			}
			
			final boolean isCtrlPressed = (getCurrentSkill() != null) && getCurrentSkill().isCtrlPressed();
			
			// Peace Zone
			if (target.isInsideZone(ZoneId.PEACE)) {
				return false;
			}
			
			// Siege
			if ((getSiegeState() != 0) && (targetPlayer.getSiegeState() != 0)) {
				// Register for same siege
				if (getSiegeSide() == targetPlayer.getSiegeSide()) {
					// Same side
					if (getSiegeState() == targetPlayer.getSiegeState()) {
						sendPacket(SystemMessageId.FORCED_ATTACK_IS_IMPOSSIBLE_AGAINST_SIEGE_SIDE_TEMPORARY_ALLIED_MEMBERS);
						return false;
					}
				}
			}
			
			// Duel
			if (isInDuel() && targetPlayer.isInDuel()) {
				if (getDuelId() == targetPlayer.getDuelId()) {
					return true;
				}
			}
			
			// Party
			if (isInParty() && targetPlayer.isInParty()) {
				// Same Party
				if (getParty().getLeader() == targetPlayer.getParty().getLeader()) {
					if ((skill.getEffectRange() > 0) && isCtrlPressed && (getTarget() == target)) {
						if (skill.isDamage()) {
							return true;
						}
					}
					return false;
				} else if ((getParty().getCommandChannel() != null) && getParty().getCommandChannel().containsPlayer(targetPlayer)) {
					if ((skill.getEffectRange() > 0) && isCtrlPressed && (getTarget() == target)) {
						if (skill.isDamage()) {
							return true;
						}
					}
					return false;
				}
			}
			
			// You can debuff anyone except party members while in an arena...
			if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
				return true;
			}
			
			// Olympiad
			if (isInOlympiadMode() && targetPlayer.isInOlympiadMode()) {
				if (getOlympiadGameId() == targetPlayer.getOlympiadGameId()) {
					return true;
				}
			}
			
			final L2Clan aClan = getClan();
			final L2Clan tClan = targetPlayer.getClan();
			
			if ((aClan != null) && (tClan != null)) {
				if (aClan.isAtWarWith(tClan.getId()) && tClan.isAtWarWith(aClan.getId())) {
					// Check if skill can do damage
					if ((skill.isAOE() && (skill.getEffectRange() > 0)) && isCtrlPressed && (getTarget() == target)) {
						return true;
					}
					return isCtrlPressed;
				} else if ((getClanId() == targetPlayer.getClanId()) || ((getAllyId() > 0) && (getAllyId() == targetPlayer.getAllyId()))) {
					// Check if skill can do damage
					if ((skill.getEffectRange() > 0) && isCtrlPressed && (getTarget() == target) && skill.isDamage()) {
						return true;
					}
					return false;
				}
			}
			
			// On retail, it is impossible to debuff a "peaceful" player.
			if ((targetPlayer.getPvpFlag() == 0) && (targetPlayer.getKarma() == 0)) {
				// Check if skill can do damage
				if ((skill.getEffectRange() > 0) && isCtrlPressed && (getTarget() == target) && skill.isDamage()) {
					return true;
				}
				return false;
			}
			
			if ((targetPlayer.getPvpFlag() > 0) || (targetPlayer.getKarma() > 0)) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	/**
	 * @return True if the L2PcInstance is a Mage.
	 */
	public boolean isMageClass() {
		return getClassId().isMage();
	}
	
	public boolean isMounted() {
		return _mountType != MountType.NONE;
	}
	
	public boolean checkLandingState() {
		// Check if char is in a no landing zone
		if (isInsideZone(ZoneId.NO_LANDING)) {
			return true;
		} else
		// if this is a castle that is currently being sieged, and the rider is NOT a castle owner
		// he cannot land.
		// castle owner is the leader of the clan that owns the castle where the pc is
		if (isInsideZone(ZoneId.SIEGE) && !((getClan() != null) && (CastleManager.getInstance().getCastle(this) == CastleManager.getInstance().getCastleByOwner(getClan())) && (this == getClan().getLeader().getPlayerInstance()))) {
			return true;
		}
		
		return false;
	}
	
	// returns false if the change of mount type fails.
	public void setMount(int npcId, int npcLevel) {
		final MountType type = MountType.findByNpcId(npcId);
		switch (type) {
			case NONE: // None
			{
				setIsFlying(false);
				break;
			}
			case STRIDER: // Strider
			{
				if (isNoble()) {
					addSkill(CommonSkill.STRIDER_SIEGE_ASSAULT.getSkill(), false);
				}
				break;
			}
			case WYVERN: // Wyvern
			{
				setIsFlying(true);
				break;
			}
		}
		
		_mountType = type;
		_mountNpcId = npcId;
		_mountLevel = npcLevel;
	}
	
	/**
	 * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern, 3: Wolf).
	 */
	public MountType getMountType() {
		return _mountType;
	}
	
	@Override
	public final void stopAllEffects() {
		super.stopAllEffects();
		updateAndBroadcastStatus(2);
	}
	
	@Override
	public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
		super.stopAllEffectsExceptThoseThatLastThroughDeath();
		updateAndBroadcastStatus(2);
	}
	
	public final void stopAllEffectsNotStayOnSubclassChange() {
		getEffectList().stopAllEffectsNotStayOnSubclassChange();
		updateAndBroadcastStatus(2);
	}
	
	public final void stopCubics() {
		if (!_cubics.isEmpty()) {
			for (L2CubicInstance cubic : _cubics.values()) {
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
			broadcastUserInfo();
		}
	}
	
	public final void stopCubicsByOthers() {
		if (!_cubics.isEmpty()) {
			boolean broadcast = false;
			for (L2CubicInstance cubic : _cubics.values()) {
				if (cubic.givenByOther()) {
					cubic.stopAction();
					cubic.cancelDisappear();
					_cubics.remove(cubic.getId());
					broadcast = true;
				}
			}
			if (broadcast) {
				broadcastUserInfo();
			}
		}
	}
	
	/**
	 * Send a Server->Client packet UserInfo to this L2PcInstance and CharInfo to all L2PcInstance in its _KnownPlayers.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Others L2PcInstance in the detection area of the L2PcInstance are identified in <B>_knownPlayers</B>.<br>
	 * In order to inform other players of this L2PcInstance state modifications, server just need to go through _knownPlayers to send Server->Client Packet<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Send a Server->Client packet UserInfo to this L2PcInstance (Public and Private Data)</li>
	 * <li>Send a Server->Client packet CharInfo to all L2PcInstance in _KnownPlayers of the L2PcInstance (Public data only)</li>
	 * </ul>
	 * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT>
	 */
	@Override
	public void updateAbnormalEffect() {
		broadcastUserInfo();
	}
	
	/**
	 * Disable the Inventory and create a new task to enable it after 1.5s.
	 * @param val
	 */
	public void setInventoryBlockingStatus(boolean val) {
		_inventoryDisable = val;
		if (val) {
			ThreadPoolManager.getInstance().scheduleGeneral(new InventoryEnableTask(this), 1500);
		}
	}
	
	/**
	 * @return True if the Inventory is disabled.
	 */
	public boolean isInventoryDisabled() {
		return _inventoryDisable;
	}
	
	/**
	 * Add a cubic to this player.
	 * @param cubicId the cubic ID
	 * @param level
	 * @param cubicPower
	 * @param cubicDelay
	 * @param cubicSkillChance
	 * @param cubicMaxCount
	 * @param cubicDuration
	 * @param givenByOther
	 * @return the old cubic for this cubic ID if any, otherwise {@code null}
	 */
	public L2CubicInstance addCubic(int cubicId, int level, double cubicPower, int cubicDelay, int cubicSkillChance, int cubicMaxCount, int cubicDuration, boolean givenByOther) {
		return _cubics.put(cubicId, new L2CubicInstance(this, cubicId, level, (int) cubicPower, cubicDelay, cubicSkillChance, cubicMaxCount, cubicDuration, givenByOther));
	}
	
	/**
	 * Get the player's cubics.
	 * @return the cubics
	 */
	public Map<Integer, L2CubicInstance> getCubics() {
		return _cubics;
	}
	
	/**
	 * Get the player cubic by cubic ID, if any.
	 * @param cubicId the cubic ID
	 * @return the cubic with the given cubic ID, {@code null} otherwise
	 */
	public L2CubicInstance getCubicById(int cubicId) {
		return _cubics.get(cubicId);
	}
	
	/**
	 * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).
	 */
	public int getEnchantEffect() {
		L2ItemInstance wpn = getActiveWeaponInstance();
		
		if (wpn == null) {
			return 0;
		}
		
		return Math.min(127, wpn.getEnchantLevel());
	}
	
	/**
	 * @return the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.
	 */
	public L2Npc getLastFolkNPC() {
		return _lastFolkNpc;
	}
	
	/**
	 * Set the _lastFolkNpc of the L2PcInstance corresponding to the last Folk wich one the player talked.
	 * @param folkNpc
	 */
	public void setLastFolkNPC(L2Npc folkNpc) {
		_lastFolkNpc = folkNpc;
	}
	
	/**
	 * @return True if L2PcInstance is a participant in the Festival of Darkness.
	 */
	public boolean isFestivalParticipant() {
		return SevenSignsFestival.getInstance().isParticipant(this);
	}
	
	public void addAutoSoulShot(int itemId) {
		_activeSoulShots.add(itemId);
	}
	
	public boolean removeAutoSoulShot(int itemId) {
		return _activeSoulShots.remove(itemId);
	}
	
	public Set<Integer> getAutoSoulShot() {
		return _activeSoulShots;
	}
	
	@Override
	public void rechargeShots(boolean physical, boolean magic) {
		L2ItemInstance item;
		IItemHandler handler;
		
		if ((_activeSoulShots == null) || _activeSoulShots.isEmpty()) {
			return;
		}
		
		for (int itemId : _activeSoulShots) {
			item = getInventory().getItemByItemId(itemId);
			
			if (item != null) {
				if (magic) {
					if (item.getItem().getDefaultAction() == ActionType.SPIRITSHOT) {
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null) {
							handler.useItem(this, item, false);
						}
					}
				}
				
				if (physical) {
					if (item.getItem().getDefaultAction() == ActionType.SOULSHOT) {
						handler = ItemHandler.getInstance().getHandler(item.getEtcItem());
						if (handler != null) {
							handler.useItem(this, item, false);
						}
					}
				}
			} else {
				removeAutoSoulShot(itemId);
			}
		}
	}
	
	/**
	 * Cancel autoshot for all shots matching crystaltype<BR>
	 * {@link L2Item#getCrystalType()}
	 * @param crystalType int type to disable
	 */
	public void disableAutoShotByCrystalType(int crystalType) {
		for (int itemId : _activeSoulShots) {
			if (ItemTable.getInstance().getTemplate(itemId).getCrystalType().getId() == crystalType) {
				disableAutoShot(itemId);
			}
		}
	}
	
	/**
	 * Cancel autoshot use for shot itemId
	 * @param itemId int id to disable
	 * @return true if canceled.
	 */
	public boolean disableAutoShot(int itemId) {
		if (_activeSoulShots.contains(itemId)) {
			removeAutoSoulShot(itemId);
			sendPacket(new ExAutoSoulShot(itemId, 0));
			
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
			sm.addItemName(itemId);
			sendPacket(sm);
			return true;
		}
		return false;
	}
	
	/**
	 * Cancel all autoshots for player
	 */
	public void disableAutoShotsAll() {
		for (int itemId : _activeSoulShots) {
			sendPacket(new ExAutoSoulShot(itemId, 0));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED);
			sm.addItemName(itemId);
			sendPacket(sm);
		}
		_activeSoulShots.clear();
	}
	
	public EnumIntBitmask<ClanPrivilege> getClanPrivileges() {
		return _clanPrivileges;
	}
	
	public void setClanPrivileges(EnumIntBitmask<ClanPrivilege> clanPrivileges) {
		_clanPrivileges = clanPrivileges.clone();
	}
	
	public boolean hasClanPrivilege(ClanPrivilege privilege) {
		return _clanPrivileges.has(privilege);
	}
	
	public int getPledgeClass() {
		return _pledgeClass;
	}
	
	// baron etc
	public void setPledgeClass(int classId) {
		_pledgeClass = classId;
		checkItemRestriction();
	}
	
	@Override
	public int getPledgeType() {
		return _pledgeType;
	}
	
	public void setPledgeType(int typeId) {
		_pledgeType = typeId;
	}
	
	public int getApprentice() {
		return _apprentice;
	}
	
	public void setApprentice(int apprentice_id) {
		_apprentice = apprentice_id;
	}
	
	public int getSponsor() {
		return _sponsor;
	}
	
	public void setSponsor(int sponsor_id) {
		_sponsor = sponsor_id;
	}
	
	public int getBookMarkSlot() {
		return _bookmarkslot;
	}
	
	public void setBookMarkSlot(int slot) {
		_bookmarkslot = slot;
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	@Override
	public void sendMessage(String message) {
		sendPacket(SystemMessage.sendString(message));
	}
	
	public void enterObserverMode(Location loc) {
		setLastLocation();
		
		// Remove Hide.
		getEffectList().stopSkillEffects(true, AbnormalType.HIDE);
		
		_observerMode = true;
		setTarget(null);
		setIsParalyzed(true);
		startParalyze();
		setIsInvul(true);
		setInvisible(true);
		sendPacket(new ObservationMode(loc));
		
		teleToLocation(loc, false);
		
		broadcastUserInfo();
	}
	
	public void setLastLocation() {
		_lastLoc.setXYZ(getX(), getY(), getZ());
	}
	
	public void unsetLastLocation() {
		_lastLoc.setXYZ(0, 0, 0);
	}
	
	public void enterOlympiadObserverMode(Location loc, int id) {
		if (hasSummon()) {
			getSummon().unSummon(this);
		}
		
		// Remove Hide.
		getEffectList().stopSkillEffects(true, AbnormalType.HIDE);
		
		if (!_cubics.isEmpty()) {
			for (L2CubicInstance cubic : _cubics.values()) {
				cubic.stopAction();
				cubic.cancelDisappear();
			}
			_cubics.clear();
		}
		
		if (getParty() != null) {
			getParty().removePartyMember(this, messageType.Expelled);
		}
		
		_olympiadGameId = id;
		if (isSitting()) {
			standUp();
		}
		if (!_observerMode) {
			setLastLocation();
		}
		
		_observerMode = true;
		setTarget(null);
		setIsInvul(true);
		setInvisible(true);
		teleToLocation(loc, false);
		sendPacket(new ExOlympiadMode(3));
		
		broadcastUserInfo();
	}
	
	public void leaveObserverMode() {
		setTarget(null);
		
		teleToLocation(_lastLoc, false);
		unsetLastLocation();
		sendPacket(new ObservationReturn(getLocation()));
		
		setIsParalyzed(false);
		if (!isGM()) {
			setInvisible(false);
			setIsInvul(false);
		}
		if (hasAI()) {
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		
		setFalling(); // prevent receive falling damage
		_observerMode = false;
		
		broadcastUserInfo();
	}
	
	public void leaveOlympiadObserverMode() {
		if (_olympiadGameId == -1) {
			return;
		}
		_olympiadGameId = -1;
		_observerMode = false;
		setTarget(null);
		sendPacket(new ExOlympiadMode(0));
		setInstanceId(0);
		teleToLocation(_lastLoc, true);
		if (!isGM()) {
			setInvisible(false);
			setIsInvul(false);
		}
		if (hasAI()) {
			getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
		}
		unsetLastLocation();
		broadcastUserInfo();
	}
	
	public int getOlympiadSide() {
		return _olympiadSide;
	}
	
	public void setOlympiadSide(int i) {
		_olympiadSide = i;
	}
	
	public int getOlympiadGameId() {
		return _olympiadGameId;
	}
	
	public void setOlympiadGameId(int id) {
		_olympiadGameId = id;
	}
	
	/**
	 * Gets the player's olympiad buff count.
	 * @return the olympiad's buff count
	 */
	public int getOlympiadBuffCount() {
		return _olyBuffsCount;
	}
	
	/**
	 * Sets the player's olympiad buff count.
	 * @param buffs the olympiad's buff count
	 */
	public void setOlympiadBuffCount(int buffs) {
		_olyBuffsCount = buffs;
	}
	
	public Location getLastLocation() {
		return _lastLoc;
	}
	
	public boolean inObserverMode() {
		return _observerMode;
	}
	
	public int getTeleMode() {
		return _telemode;
	}
	
	public void setTeleMode(int mode) {
		_telemode = mode;
	}
	
	public void setLoto(int i, int val) {
		_loto[i] = val;
	}
	
	public int getLoto(int i) {
		return _loto[i];
	}
	
	public void setRace(int i, int val) {
		_race[i] = val;
	}
	
	public int getRace(int i) {
		return _race[i];
	}
	
	public boolean getMessageRefusal() {
		return _messageRefusal;
	}
	
	public void setMessageRefusal(boolean mode) {
		_messageRefusal = mode;
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean getDietMode() {
		return _dietMode;
	}
	
	public void setDietMode(boolean mode) {
		_dietMode = mode;
	}
	
	public boolean getTradeRefusal() {
		return _tradeRefusal;
	}
	
	public void setTradeRefusal(boolean mode) {
		_tradeRefusal = mode;
	}
	
	public boolean getExchangeRefusal() {
		return _exchangeRefusal;
	}
	
	public void setExchangeRefusal(boolean mode) {
		_exchangeRefusal = mode;
	}
	
	public BlockList getBlockList() {
		return _blockList;
	}
	
	public void setIsInOlympiadMode(boolean b) {
		_inOlympiadMode = b;
	}
	
	public void setIsOlympiadStart(boolean b) {
		_OlympiadStart = b;
	}
	
	public boolean isOlympiadStart() {
		return _OlympiadStart;
	}
	
	public boolean isHero() {
		return _hero;
	}
	
	public void setHero(boolean hero) {
		if (hero && (_baseClass == _activeClass)) {
			for (Skill skill : SkillTreesData.getInstance().getHeroSkillTree().values()) {
				addSkill(skill, false); // Don't persist hero skills into database
			}
		} else {
			for (Skill skill : SkillTreesData.getInstance().getHeroSkillTree().values()) {
				removeSkill(skill, false, true); // Just remove skills from non-hero players
			}
		}
		_hero = hero;
		
		sendSkillList();
	}
	
	public boolean isInOlympiadMode() {
		return _inOlympiadMode;
	}
	
	@Override
	public boolean isInDuel() {
		return _duelState != DuelState.NO_DUEL;
	}
	
	@Override
	public int getDuelId() {
		return _duelId;
	}
	
	public DuelState getDuelState() {
		return _duelState;
	}
	
	public void setDuelState(DuelState mode) {
		_duelState = mode;
	}
	
	/**
	 * Sets up the duel state using a non 0 duelId.
	 * @param duelId 0=not in a duel
	 */
	public void setIsInDuel(int duelId) {
		if (duelId > 0) {
			_duelState = DuelState.DUELLING;
			_duelId = duelId;
		} else {
			if (_duelState == DuelState.DEAD) {
				enableAllSkills();
				getStatus().startHpMpRegeneration();
			}
			_duelState = DuelState.NO_DUEL;
			_duelId = 0;
		}
	}
	
	public boolean isNoble() {
		return _noble;
	}
	
	public void setNoble(boolean val) {
		final Collection<Skill> nobleSkillTree = SkillTreesData.getInstance().getNobleSkillTree().values();
		if (val) {
			for (Skill skill : nobleSkillTree) {
				addSkill(skill, false);
			}
		} else {
			for (Skill skill : nobleSkillTree) {
				removeSkill(skill, false, true);
			}
		}
		
		_noble = val;
		
		sendSkillList();
	}
	
	public int getLvlJoinedAcademy() {
		return _lvlJoinedAcademy;
	}
	
	public void setLvlJoinedAcademy(int lvl) {
		_lvlJoinedAcademy = lvl;
	}
	
	@Override
	public boolean isAcademyMember() {
		return _lvlJoinedAcademy > 0;
	}
	
	@Override
	public void setTeam(Team team) {
		super.setTeam(team);
		broadcastUserInfo();
		if (hasSummon()) {
			getSummon().broadcastStatusUpdate();
		}
	}
	
	public int getWantsPeace() {
		return _wantsPeace;
	}
	
	public void setWantsPeace(int wantsPeace) {
		_wantsPeace = wantsPeace;
	}
	
	public boolean isFishing() {
		return _fishing;
	}
	
	public void setFishing(boolean fishing) {
		_fishing = fishing;
	}
	
	public void sendSkillList() {
		boolean isDisabled = false;
		SkillList sl = new SkillList();
		
		for (Skill s : getAllSkills()) {
			if (s == null) {
				continue;
			}
			
			if (((_transformation != null) && !s.isPassive()) || (hasTransformSkill(s.getId()) && s.isPassive())) {
				continue;
			}
			if (getClan() != null) {
				isDisabled = s.isClanSkill() && (getClan().getReputationScore() < 0);
			}
			
			boolean isEnchantable = SkillData.getInstance().isEnchantable(s.getId());
			if (isEnchantable) {
				L2EnchantSkillLearn esl = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(s.getId());
				if ((esl == null) || (s.getLevel() < esl.getBaseLevel())) {
					isEnchantable = false;
				}
			}
			
			sl.addSkill(s.getDisplayId(), s.getDisplayLevel(), s.isPassive(), isDisabled, isEnchantable);
		}
		if (_transformation != null) {
			Map<Integer, Integer> ts = new TreeMap<>();
			
			for (SkillHolder holder : _transformation.getTemplate(this).getSkills()) {
				ts.putIfAbsent(holder.getSkillId(), holder.getSkillLvl());
				
				if (ts.get(holder.getSkillId()) < holder.getSkillLvl()) {
					ts.put(holder.getSkillId(), holder.getSkillLvl());
				}
			}
			
			for (AdditionalSkillHolder holder : _transformation.getTemplate(this).getAdditionalSkills()) {
				if (getLevel() >= holder.getMinLevel()) {
					ts.putIfAbsent(holder.getSkillId(), holder.getSkillLvl());
					if (ts.get(holder.getSkillId()) < holder.getSkillLvl()) {
						ts.put(holder.getSkillId(), holder.getSkillLvl());
					}
				}
			}
			
			// Add collection skills.
			for (L2SkillLearn skill : SkillTreesData.getInstance().getCollectSkillTree().values()) {
				if (getKnownSkill(skill.getSkillId()) != null) {
					addTransformSkill(SkillData.getInstance().getSkill(skill.getSkillId(), skill.getSkillLevel()));
				}
			}
			
			for (Entry<Integer, Integer> transformSkill : ts.entrySet()) {
				Skill sk = SkillData.getInstance().getSkill(transformSkill.getKey(), transformSkill.getValue());
				addTransformSkill(sk);
				sl.addSkill(transformSkill.getKey(), transformSkill.getValue(), false, false, false);
			}
		}
		sendPacket(sl);
	}
	
	/**
	 * 1. Add the specified class ID as a subclass (up to the maximum number of <b>three</b>) for this character.<BR>
	 * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
	 * @param classId
	 * @param classIndex
	 * @return boolean subclassAdded
	 */
	public boolean addSubClass(int classId, int classIndex) {
		if (!_subclassLock.tryLock()) {
			return false;
		}
		
		try {
			if ((getTotalSubClasses() == character().getMaxSubclass()) || (classIndex == 0)) {
				return false;
			}
			
			if (getSubClasses().containsKey(classIndex)) {
				return false;
			}
			
			// Note: Never change _classIndex in any method other than setActiveClass().
			
			SubClass newClass = new SubClass(this);
			newClass.setClassId(classId);
			newClass.setClassIndex(classIndex);
			
			if (!DAOFactory.getInstance().getSubclassDAO().insert(this, newClass)) {
				return false;
			}
			
			// Commit after database INSERT in case exception is thrown.
			getSubClasses().put(newClass.getClassIndex(), newClass);
			
			final ClassId subTemplate = ClassId.getClassId(classId);
			final Map<Integer, L2SkillLearn> skillTree = SkillTreesData.getInstance().getCompleteClassSkillTree(subTemplate);
			final Map<Integer, Skill> prevSkillList = new HashMap<>();
			for (L2SkillLearn skillInfo : skillTree.values()) {
				if (skillInfo.getGetLevel() <= 40) {
					Skill prevSkill = prevSkillList.get(skillInfo.getSkillId());
					Skill newSkill = SkillData.getInstance().getSkill(skillInfo.getSkillId(), skillInfo.getSkillLevel());
					
					if ((prevSkill != null) && (prevSkill.getLevel() > newSkill.getLevel())) {
						continue;
					}
					
					prevSkillList.put(newSkill.getId(), newSkill);
					storeSkill(newSkill, prevSkill, classIndex);
				}
			}
			return true;
		} finally {
			_subclassLock.unlock();
		}
	}
	
	/**
	 * 1. Completely erase all existence of the subClass linked to the classIndex.<br>
	 * 2. Send over the newClassId to addSubClass() to create a new instance on this classIndex.<br>
	 * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.
	 * @param classIndex the class index to delete
	 * @param newClassId the new class Id
	 * @return {@code true} if the sub-class was modified, {@code false} otherwise
	 */
	public boolean modifySubClass(int classIndex, int newClassId) {
		if (!_subclassLock.tryLock()) {
			return false;
		}
		
		try {
			DAOFactory.getInstance().getHennaDAO().deleteAll(this, classIndex);
			
			DAOFactory.getInstance().getSkillDAO().deleteAll(this, classIndex);
			
			DAOFactory.getInstance().getShortcutDAO().delete(this, classIndex);
			
			DAOFactory.getInstance().getPlayerSkillSaveDAO().delete(this, classIndex);
			
			DAOFactory.getInstance().getSubclassDAO().delete(this, classIndex);
			
			// Notify to scripts
			int classId = getSubClasses().get(classIndex).getClassId();
			EventDispatcher.getInstance().notifyEventAsync(new OnPlayerProfessionCancel(this, classId), this);
			
			getSubClasses().remove(classIndex);
		} finally {
			_subclassLock.unlock();
		}
		
		return addSubClass(newClassId, classIndex);
	}
	
	public boolean isSubClassActive() {
		return _classIndex > 0;
	}
	
	public Map<Integer, SubClass> getSubClasses() {
		if (_subClasses == null) {
			_subClasses = new ConcurrentSkipListMap<>();
		}
		
		return _subClasses;
	}
	
	public int getTotalSubClasses() {
		return getSubClasses().size();
	}
	
	public int getBaseClass() {
		return _baseClass;
	}
	
	public void setBaseClass(ClassId classId) {
		_baseClass = classId.ordinal();
	}
	
	public int getActiveClass() {
		return _activeClass;
	}
	
	public void setActiveClass(int classId) {
		_activeClass = classId;
	}
	
	public int getClassIndex() {
		return _classIndex;
	}
	
	public void setClassIndex(int classIndex) {
		_classIndex = classIndex;
	}
	
	private void setClassTemplate(int classId) {
		_activeClass = classId;
		
		final L2PcTemplate pcTemplate = PlayerTemplateData.getInstance().getTemplate(classId);
		if (pcTemplate == null) {
			LOG.error("Missing template for classId: {}", classId);
			throw new Error();
		}
		// Set the template of the L2PcInstance
		setTemplate(pcTemplate);
		
		// Notify to scripts
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerProfessionChange(this, pcTemplate, isSubClassActive()), this);
	}
	
	/**
	 * Changes the character's class based on the given class index.<br>
	 * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.<br>
	 * <font color="00FF00"/>WARNING: Use only on subclass change</font>
	 * @param classIndex
	 * @return
	 */
	public boolean changeActiveClass(int classIndex) {
		if (!_subclassLock.tryLock()) {
			return false;
		}
		
		try {
			// Cannot switch or change subclasses while transformed
			if (_transformation != null) {
				return false;
			}
			
			// Remove active item skills before saving char to database
			// because next time when choosing this class, weared items can
			// be different
			for (L2ItemInstance item : getInventory().getAugmentedItems()) {
				if ((item != null) && item.isEquipped()) {
					item.getAugmentation().removeBonus(this);
				}
			}
			
			// abort any kind of cast.
			abortCast();
			
			if (isChannelized()) {
				getSkillChannelized().abortChannelization();
			}
			
			// 1. Call store() before modifying _classIndex to avoid skill effects rollover.
			// 2. Register the correct _classId against applied 'classIndex'.
			store(character().subclassStoreSkillCooltime());
			
			resetTimeStamps();
			
			// clear charges
			_charges.set(0);
			stopChargeTask();
			
			if (hasServitor()) {
				getSummon().unSummon(this);
			}
			
			if (classIndex == 0) {
				setClassTemplate(getBaseClass());
			} else {
				try {
					setClassTemplate(getSubClasses().get(classIndex).getClassId());
				} catch (Exception e) {
					LOG.warn("Could not switch {} sub class to class index {}, {}", this, classIndex, e);
					return false;
				}
			}
			
			_classIndex = classIndex;
			
			setLearningClass(getClassId());
			
			if (isInParty()) {
				getParty().recalculatePartyLevel();
			}
			
			// Update the character's change in class status.
			// 1. Remove any active cubics from the player.
			// 2. Renovate the characters table in the database with the new class info, storing also buff/effect data.
			// 3. Remove all existing skills.
			// 4. Restore all the learned skills for the current class from the database.
			// 5. Restore effect/buff data for the new class.
			// 6. Restore henna data for the class, applying the new stat modifiers while removing existing ones.
			// 7. Reset HP/MP/CP stats and send Server->Client character status packet to reflect changes.
			// 8. Restore shortcut data related to this class.
			// 9. Resend a class change animation effect to broadcast to all nearby players.
			for (Skill oldSkill : getAllSkills()) {
				removeSkill(oldSkill, false, true);
			}
			
			stopAllEffectsExceptThoseThatLastThroughDeath();
			stopAllEffectsNotStayOnSubclassChange();
			stopCubics();
			
			DAOFactory.getInstance().getRecipeBookDAO().load(this, false);
			
			// Restore any Death Penalty Buff
			restoreDeathPenaltyBuffLevel();
			
			DAOFactory.getInstance().getSkillDAO().load(this);
			
			rewardSkills();
			regiveTemporarySkills();
			
			// Prevents some issues when changing between subclasses that shares skills
			resetDisabledSkills();
			
			restoreEffects();
			
			sendPacket(new EtcStatusUpdate(this));
			
			// if player has quest 422: Repent Your Sins, remove it
			QuestState st = getQuestState("Q00422_RepentYourSins");
			if (st != null) {
				st.exitQuest(true);
			}
			
			for (int i = 0; i < 3; i++) {
				_henna[i] = null;
			}
			
			DAOFactory.getInstance().getHennaDAO().load(this);
			
			// Calculate henna modifiers of this player.
			recalcHennaStats();
			
			sendPacket(new HennaInfo(this));
			
			if (getCurrentHp() > getMaxHp()) {
				setCurrentHp(getMaxHp());
			}
			if (getCurrentMp() > getMaxMp()) {
				setCurrentMp(getMaxMp());
			}
			if (getCurrentCp() > getMaxCp()) {
				setCurrentCp(getMaxCp());
			}
			
			refreshOverloaded();
			refreshExpertisePenalty();
			broadcastUserInfo();
			
			// Clear resurrect xp calculation
			setExpBeforeDeath(0);
			
			_shortCuts.restoreMe();
			sendPacket(new ShortCutInit(this));
			
			broadcastPacket(new SocialAction(getObjectId(), SocialAction.LEVEL_UP));
			sendPacket(new SkillCoolTime(this));
			sendPacket(new ExStorageMaxCount(this));
			return true;
		} finally {
			_subclassLock.unlock();
		}
	}
	
	public boolean isLocked() {
		return _subclassLock.isLocked();
	}
	
	public void stopWarnUserTakeBreak() {
		if (_taskWarnUserTakeBreak != null) {
			_taskWarnUserTakeBreak.cancel(true);
			_taskWarnUserTakeBreak = null;
		}
	}
	
	public void startWarnUserTakeBreak() {
		if (_taskWarnUserTakeBreak == null) {
			_taskWarnUserTakeBreak = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new WarnUserTakeBreakTask(this), 7200000, 7200000);
		}
	}
	
	public void stopRentPet() {
		if (_taskRentPet != null) {
			// if the rent of a wyvern expires while over a flying zone, tp to down before unmounting
			if (checkLandingState() && (getMountType() == MountType.WYVERN)) {
				teleToLocation(TeleportWhereType.TOWN);
			}
			
			if (dismount()) // this should always be true now, since we teleported already
			{
				_taskRentPet.cancel(true);
				_taskRentPet = null;
			}
		}
	}
	
	public void startRentPet(int seconds) {
		if (_taskRentPet == null) {
			_taskRentPet = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RentPetTask(this), seconds * 1000L, seconds * 1000L);
		}
	}
	
	public boolean isRentedPet() {
		if (_taskRentPet != null) {
			return true;
		}
		
		return false;
	}
	
	public void stopWaterTask() {
		if (_taskWater != null) {
			_taskWater.cancel(false);
			_taskWater = null;
			sendPacket(new SetupGauge(2, 0));
		}
	}
	
	public void startWaterTask() {
		if (!isDead() && (_taskWater == null)) {
			int timeinwater = (int) calcStat(Stats.BREATH, 60000, this, null);
			
			sendPacket(new SetupGauge(2, timeinwater));
			_taskWater = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new WaterTask(this), timeinwater, 1000);
		}
	}
	
	public boolean isInWater() {
		if (_taskWater != null) {
			return true;
		}
		
		return false;
	}
	
	public void checkWaterState() {
		if (isInsideZone(ZoneId.WATER)) {
			startWaterTask();
		} else {
			stopWaterTask();
		}
	}
	
	public void onPlayerEnter() {
		startWarnUserTakeBreak();
		
		if (SevenSigns.getInstance().isSealValidationPeriod() || SevenSigns.getInstance().isCompResultsPeriod()) {
			if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(getObjectId()) != SevenSigns.getInstance().getCabalHighestScore())) {
				teleToLocation(TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town due to the beginning of the Seal Validation period.");
			}
		} else {
			if (!isGM() && isIn7sDungeon() && (SevenSigns.getInstance().getPlayerCabal(getObjectId()) == SevenSigns.CABAL_NULL)) {
				teleToLocation(TeleportWhereType.TOWN);
				setIsIn7sDungeon(false);
				sendMessage("You have been teleported to the nearest town because you have not signed for any cabal.");
			}
		}
		
		if (isGM()) {
			if (isInvul()) {
				sendMessage("Entering world in Invulnerable mode.");
			}
			if (isInvisible()) {
				sendMessage("Entering world in Invisible mode.");
			}
			if (isSilenceMode()) {
				sendMessage("Entering world in Silence mode.");
			}
		}
		
		revalidateZone(true);
		
		notifyFriends();
		if (!canOverrideCond(PcCondOverride.SKILL_CONDITIONS) && character().decreaseSkillOnDelevel()) {
			checkPlayerSkills();
		}
		
		try {
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(this)) {
				zone.onPlayerLoginInside(this);
			}
		} catch (Exception e) {
			LOG.error("{}", e);
		}
		
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLogin(this), this);
	}
	
	public long getLastAccess() {
		return _lastAccess;
	}
	
	public void setLastAccess(long lastAccess) {
		_lastAccess = lastAccess;
	}
	
	@Override
	public void doRevive() {
		super.doRevive();
		updateEffectIcons();
		sendPacket(new EtcStatusUpdate(this));
		_revivePet = false;
		_reviveRequested = 0;
		_revivePower = 0;
		
		if (isMounted()) {
			startFeed(_mountNpcId);
		}
		if (isInParty() && getParty().isInDimensionalRift()) {
			if (!DimensionalRiftManager.getInstance().checkIfInPeaceZone(getX(), getY(), getZ())) {
				getParty().getDimensionalRift().memberRessurected(this);
			}
		}
		if (getInstanceId() > 0) {
			final Instance instance = InstanceManager.getInstance().getInstance(getInstanceId());
			if (instance != null) {
				instance.cancelEjectDeadPlayer(this);
			}
		}
	}
	
	@Override
	public void setName(String value) {
		super.setName(value);
		if (general().cacheCharNames()) {
			CharNameTable.getInstance().addName(this);
		}
	}
	
	@Override
	public void doRevive(double revivePower) {
		doRevive();
		restoreExp(revivePower);
	}
	
	public void reviveRequest(L2PcInstance reviver, Skill skill, boolean Pet, int resPower, int resRecovery) {
		if (isResurrectionBlocked()) {
			return;
		}
		
		if (_reviveRequested == 1) {
			if (_revivePet == Pet) {
				reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED); // Resurrection is already been proposed.
			} else {
				if (Pet) {
					reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
				} else {
					reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
				}
			}
			return;
		}
		
		if ((Pet && hasPet() && getSummon().isDead()) || (!Pet && isDead())) {
			_reviveRequested = 1;
			_reviveRecovery = resRecovery;
			int restoreExp = 0;
			
			_revivePower = Formulas.calculateSkillResurrectRestorePercent(resPower, reviver);
			restoreExp = (int) Math.round(((getExpBeforeDeath() - getExp()) * _revivePower) / 100);
			_revivePet = Pet;
			
			if (hasCharmOfCourage()) {
				ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECT_USING_CHARM_OF_COURAGE.getId());
				dlg.addTime(60000);
				sendPacket(dlg);
				return;
			}
			ConfirmDlg dlg = new ConfirmDlg(SystemMessageId.RESURRECTION_REQUEST_BY_C1_FOR_S2_XP.getId());
			dlg.addPcName(reviver);
			dlg.addString(Integer.toString(Math.abs(restoreExp)));
			sendPacket(dlg);
		}
	}
	
	public void reviveAnswer(int answer) {
		if ((_reviveRequested != 1) || (!isDead() && !_revivePet) || (_revivePet && hasPet() && !getSummon().isDead())) {
			return;
		}
		
		if (answer == 1) {
			if (!_revivePet) {
				if (_revivePower != 0) {
					doRevive(_revivePower);
				} else {
					doRevive();
				}
				
				if (_reviveRecovery != 0) {
					setCurrentHpMp(getMaxHp() * (_reviveRecovery / 100.0), getMaxMp() * (_reviveRecovery / 100.0));
					setCurrentCp(0);
				}
			} else if (hasPet()) {
				if (_revivePower != 0) {
					getSummon().doRevive(_revivePower);
				} else {
					getSummon().doRevive();
				}
				
				if (_reviveRecovery != 0) {
					getSummon().setCurrentHpMp(getSummon().getMaxHp() * (_reviveRecovery / 100.0), getSummon().getMaxMp() * (_reviveRecovery / 100.0));
				}
			}
		}
		_revivePet = false;
		_reviveRequested = 0;
		_revivePower = 0;
		_reviveRecovery = 0;
	}
	
	public boolean isReviveRequested() {
		return (_reviveRequested == 1);
	}
	
	public boolean isRevivingPet() {
		return _revivePet;
	}
	
	public void removeReviving() {
		_reviveRequested = 0;
		_revivePower = 0;
	}
	
	public void onActionRequest() {
		if (isSpawnProtected()) {
			sendPacket(SystemMessageId.YOU_ARE_NO_LONGER_PROTECTED_FROM_AGGRESSIVE_MONSTERS);
			
			if (character().restoreServitorOnReconnect() && !hasSummon() && CharSummonTable.getInstance().getServitors().containsKey(getObjectId())) {
				CharSummonTable.getInstance().restoreServitor(this);
			}
			if (character().restorePetOnReconnect() && !hasSummon() && CharSummonTable.getInstance().getPets().containsKey(getObjectId())) {
				CharSummonTable.getInstance().restorePet(this);
			}
		}
		if (isTeleportProtected()) {
			sendMessage("Teleport spawn protection ended.");
		}
		setProtection(false);
		setTeleportProtection(false);
	}
	
	/**
	 * Expertise of the L2PcInstance (None=0, D=1, C=2, B=3, A=4, S=5, S80=6, S84=7)
	 * @return int Expertise skill level.
	 */
	public int getExpertiseLevel() {
		int level = getSkillLevel(239);
		if (level < 0) {
			level = 0;
		}
		return level;
	}
	
	@Override
	public void teleToLocation(ILocational loc, boolean allowRandomOffset) {
		if ((getVehicle() != null) && !getVehicle().isTeleporting()) {
			setVehicle(null);
		}
		
		if (isFlyingMounted() && (loc.getZ() < -1005)) {
			super.teleToLocation(loc.getX(), loc.getY(), -1005, loc.getHeading(), loc.getInstanceId());
		}
		
		super.teleToLocation(loc, allowRandomOffset);
	}
	
	@Override
	public final void onTeleported() {
		super.onTeleported();
		
		if (isInAirShip()) {
			getAirShip().sendInfo(this);
		}
		
		// Force a revalidation
		revalidateZone(true);
		
		checkItemRestriction();
		
		if ((character().getPlayerTeleportProtection() > 0) && !isInOlympiadMode()) {
			setTeleportProtection(true);
		}
		
		if (hasTamedBeasts()) {
			for (L2TamedBeastInstance tamedBeast : _tamedBeasts) {
				tamedBeast.deleteMe();
			}
			_tamedBeasts.clear();
		}
		
		// Modify the position of the pet if necessary
		final L2Summon summon = getSummon();
		if (summon != null) {
			summon.setFollowStatus(false);
			summon.teleToLocation(getLocation(), false);
			((L2SummonAI) summon.getAI()).setStartFollowController(true);
			summon.setFollowStatus(true);
			summon.updateAndBroadcastStatus(0);
		}
		
		TvTEvent.onTeleported(this);
	}
	
	@Override
	public void setIsTeleporting(boolean teleport) {
		setIsTeleporting(teleport, true);
	}
	
	public void setIsTeleporting(boolean teleport, boolean useWatchDog) {
		super.setIsTeleporting(teleport);
		if (!useWatchDog) {
			return;
		}
		if (teleport) {
			if ((_teleportWatchdog == null) && (character().getTeleportWatchdogTimeout() > 0)) {
				synchronized (this) {
					if (_teleportWatchdog == null) {
						_teleportWatchdog = ThreadPoolManager.getInstance().scheduleGeneral(new TeleportWatchdogTask(this), character().getTeleportWatchdogTimeout());
					}
				}
			}
		} else {
			if (_teleportWatchdog != null) {
				_teleportWatchdog.cancel(false);
				_teleportWatchdog = null;
			}
		}
	}
	
	public void setLastServerPosition(int x, int y, int z) {
		_lastServerPosition.setXYZ(x, y, z);
	}
	
	public Location getLastServerPosition() {
		return _lastServerPosition;
	}
	
	public int getLastServerDistance(int x, int y, int z) {
		return (int) Util.calculateDistance(x, y, z, _lastServerPosition.getX(), _lastServerPosition.getY(), _lastServerPosition.getZ(), true, false);
	}
	
	@Override
	public void reduceCurrentHp(double value, L2Character attacker, boolean awake, boolean isDOT, Skill skill) {
		if (skill != null) {
			getStatus().reduceHp(value, attacker, awake, isDOT, skill.isToggle(), skill.getDmgDirectlyToHP());
		} else {
			getStatus().reduceHp(value, attacker, awake, isDOT, false, false);
		}
		
		// notify the tamed beast of attacks
		if (hasTamedBeasts()) {
			for (L2TamedBeastInstance tamedBeast : _tamedBeasts) {
				tamedBeast.onOwnerGotAttacked(attacker);
			}
		}
	}
	
	public void broadcastSnoop(int type, String name, String _text) {
		if (!_snoopListener.isEmpty()) {
			Snoop sn = new Snoop(getObjectId(), getName(), type, name, _text);
			
			for (L2PcInstance pci : _snoopListener) {
				if (pci != null) {
					pci.sendPacket(sn);
				}
			}
		}
	}
	
	public void addSnooper(L2PcInstance pci) {
		_snoopListener.add(pci);
	}
	
	public void removeSnooper(L2PcInstance pci) {
		_snoopListener.remove(pci);
	}
	
	public void addSnooped(L2PcInstance pci) {
		_snoopedPlayer.add(pci);
	}
	
	public void removeSnooped(L2PcInstance pci) {
		_snoopedPlayer.remove(pci);
	}
	
	public void addHtmlAction(HtmlActionScope scope, String action) {
		_htmlActionCaches[scope.ordinal()].add(action);
	}
	
	public void clearHtmlActions(HtmlActionScope scope) {
		_htmlActionCaches[scope.ordinal()].clear();
	}
	
	public void setHtmlActionOriginObjectId(HtmlActionScope scope, int npcObjId) {
		if (npcObjId < 0) {
			throw new IllegalArgumentException();
		}
		
		_htmlActionOriginObjectIds[scope.ordinal()] = npcObjId;
	}
	
	public int getLastHtmlActionOriginId() {
		return _lastHtmlActionOriginObjId;
	}
	
	private boolean validateHtmlAction(Iterable<String> actionIter, String action) {
		for (String cachedAction : actionIter) {
			if (cachedAction.charAt(cachedAction.length() - 1) == AbstractHtmlPacket.VAR_PARAM_START_CHAR) {
				if (action.startsWith(cachedAction.substring(0, cachedAction.length() - 1).trim())) {
					return true;
				}
			} else if (cachedAction.equals(action)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Check if the HTML action was sent in a HTML packet.<br>
	 * If the HTML action was not sent for whatever reason, -1 is returned.<br>
	 * Otherwise, the NPC object ID or 0 is returned.<br>
	 * 0 means the HTML action was not bound to an NPC<br>
	 * and no range checks need to be made.
	 * @param action the HTML action to check
	 * @return NPC object ID, 0 or -1
	 */
	public int validateHtmlAction(String action) {
		for (int i = 0; i < _htmlActionCaches.length; ++i) {
			if ((_htmlActionCaches[i] != null) && validateHtmlAction(_htmlActionCaches[i], action)) {
				_lastHtmlActionOriginObjId = _htmlActionOriginObjectIds[i];
				return _lastHtmlActionOriginObjId;
			}
		}
		
		return -1;
	}
	
	/**
	 * Performs following tests:
	 * <ul>
	 * <li>Inventory contains item</li>
	 * <li>Item owner id == owner id</li>
	 * <li>It isn't pet control item while mounting pet or pet summoned</li>
	 * <li>It isn't active enchant item</li>
	 * <li>It isn't cursed weapon/item</li>
	 * <li>It isn't wear item</li>
	 * </ul>
	 * @param objectId item object id
	 * @param action just for login purpose
	 * @return
	 */
	public boolean validateItemManipulation(int objectId, String action) {
		L2ItemInstance item = getInventory().getItemByObjectId(objectId);
		
		if ((item == null) || (item.getOwnerId() != getObjectId())) {
			LOG.info("{} tried to {} item he is not owner of", this, action);
			return false;
		}
		
		// Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
		if ((hasSummon() && (getSummon().getControlObjectId() == objectId)) || (getMountObjectID() == objectId)) {
			if (general().debug()) {
				LOG.debug("{} tried to {} item controling pet", this, action);
			}
			
			return false;
		}
		
		if (getActiveEnchantItemId() == objectId) {
			if (general().debug()) {
				LOG.debug("{} tried to {} an enchant scroll he was using", this, action);
			}
			return false;
		}
		
		if (CursedWeaponsManager.getInstance().isCursed(item.getId())) {
			// can not trade a cursed weapon
			return false;
		}
		
		return true;
	}
	
	/**
	 * @return Returns the inBoat.
	 */
	public boolean isInBoat() {
		return (_vehicle != null) && _vehicle.isBoat();
	}
	
	/**
	 * @return
	 */
	public L2BoatInstance getBoat() {
		return (L2BoatInstance) _vehicle;
	}
	
	/**
	 * @return Returns the inAirShip.
	 */
	public boolean isInAirShip() {
		return (_vehicle != null) && _vehicle.isAirShip();
	}
	
	/**
	 * @return
	 */
	public L2AirShipInstance getAirShip() {
		return (L2AirShipInstance) _vehicle;
	}
	
	public L2Vehicle getVehicle() {
		return _vehicle;
	}
	
	public void setVehicle(L2Vehicle v) {
		if ((v == null) && (_vehicle != null)) {
			_vehicle.removePassenger(this);
		}
		
		_vehicle = v;
	}
	
	public boolean isInVehicle() {
		return _vehicle != null;
	}
	
	public boolean isInCrystallize() {
		return _inCrystallize;
	}
	
	public void setInCrystallize(boolean inCrystallize) {
		_inCrystallize = inCrystallize;
	}
	
	/**
	 * @return
	 */
	public Location getInVehiclePosition() {
		return _inVehiclePosition;
	}
	
	public void setInVehiclePosition(Location pt) {
		_inVehiclePosition = pt;
	}
	
	/**
	 * Manage the delete task of a L2PcInstance (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>If the L2PcInstance is in observer mode, set its position to its position before entering in observer mode</li>
	 * <li>Set the online Flag to True or False and update the characters table of the database with online status and lastAccess</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 * <li>Cancel Crafting, Attak or Cast</li>
	 * <li>Remove the L2PcInstance from the world</li>
	 * <li>Stop Party and Unsummon Pet</li>
	 * <li>Update database with items in its inventory and remove them from the world</li>
	 * <li>Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI</li>
	 * <li>Close the connection with the client</li>
	 * </ul>
	 */
	@Override
	public boolean deleteMe() {
		cleanup();
		storeMe();
		return super.deleteMe();
	}
	
	private synchronized void cleanup() {
		EventDispatcher.getInstance().notifyEventAsync(new OnPlayerLogout(this), this);
		
		try {
			for (L2ZoneType zone : ZoneManager.getInstance().getZones(this)) {
				zone.onPlayerLogoutInside(this);
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout)
		try {
			if (!isOnline()) {
				LOG.error("deleteMe() called on offline {}", this);
			}
			setOnlineStatus(false, true);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			if (general().enableBlockCheckerEvent() && (getBlockCheckerArena() != -1)) {
				HandysBlockCheckerManager.getInstance().onDisconnect(this);
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			_isOnline = false;
			abortAttack();
			abortCast();
			stopMove(null);
			setDebug(null);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// remove combat flag
		try {
			if (getInventory().getItemByItemId(9819) != null) {
				Fort fort = FortManager.getInstance().getFort(this);
				if (fort != null) {
					FortSiegeManager.getInstance().dropCombatFlag(this, fort.getResidenceId());
				} else {
					int slot = getInventory().getSlotFromItem(getInventory().getItemByItemId(9819));
					getInventory().unEquipItemInBodySlot(slot);
					destroyItem("CombatFlag", getInventory().getItemByItemId(9819), null, true);
				}
			} else if (isCombatFlagEquipped()) {
				TerritoryWarManager.getInstance().dropCombatFlag(this, false, false);
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			PartyMatchWaitingList.getInstance().removePlayer(this);
			if (_partyroom != 0) {
				PartyMatchRoom room = PartyMatchRoomList.getInstance().getRoom(_partyroom);
				if (room != null) {
					room.deleteMember(this);
				}
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			if (isFlying()) {
				removeSkill(SkillData.getInstance().getSkill(4289, 1));
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Recommendations must be saved before task (timer) is canceled
		try {
			storeRecommendations();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		// Stop the HP/MP/CP Regeneration task (scheduled tasks)
		try {
			stopAllTimers();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			setIsTeleporting(false);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Stop crafting, if in progress
		try {
			RecipeController.getInstance().requestMakeItemAbort(this);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Cancel Attak or Cast
		try {
			setTarget(null);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		if (isChannelized()) {
			getSkillChannelized().abortChannelization();
		}
		
		// Stop all toggles.
		getEffectList().stopAllToggles();
		
		// Remove from world regions zones
		final L2WorldRegion oldRegion = getWorldRegion();
		if (oldRegion != null) {
			oldRegion.removeFromZones(this);
		}
		
		// Remove the L2PcInstance from the world
		try {
			decayMe();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// If a Party is in progress, leave it (and festival party)
		if (isInParty()) {
			try {
				leaveParty();
			} catch (Exception e) {
				LOG.error("deleteMe() {}", e);
			}
		}
		
		if (OlympiadManager.getInstance().isRegistered(this) || (getOlympiadGameId() != -1)) {
			OlympiadManager.getInstance().removeDisconnectedCompetitor(this);
		}
		
		// If the L2PcInstance has Pet, unsummon it
		if (hasSummon()) {
			try {
				getSummon().setRestoreSummon(true);
				
				getSummon().unSummon(this);
				// Dead pet wasn't unsummoned, broadcast npcinfo changes (pet will be without owner name - means owner offline)
				if (hasSummon()) {
					getSummon().broadcastNpcInfo(0);
				}
			} catch (Exception e) {
				LOG.error("deleteMe() {}", e);
			} // returns pet to control item
		}
		
		if (getClan() != null) {
			// set the status for pledge member list to OFFLINE
			try {
				L2ClanMember clanMember = getClan().getClanMember(getObjectId());
				if (clanMember != null) {
					clanMember.setPlayerInstance(null);
				}
				
			} catch (Exception e) {
				LOG.error("deleteMe() {}", e);
			}
		}
		
		if (getActiveRequester() != null) {
			// deals with sudden exit in the middle of transaction
			setActiveRequester(null);
			cancelActiveTrade();
		}
		
		// If the L2PcInstance is a GM, remove it from the GM List
		if (isGM()) {
			try {
				AdminData.getInstance().deleteGm(this);
			} catch (Exception e) {
				LOG.error("deleteMe() {}", e);
			}
		}
		
		try {
			// Check if the L2PcInstance is in observer mode to set its position to its position
			// before entering in observer mode
			if (inObserverMode()) {
				setLocationInvisible(_lastLoc);
			}
			
			if (getVehicle() != null) {
				getVehicle().oustPlayer(this);
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// remove player from instance and set spawn location if any
		try {
			final int instanceId = getInstanceId();
			if ((instanceId != 0) && !general().restorePlayerInstance()) {
				final Instance inst = InstanceManager.getInstance().getInstance(instanceId);
				if (inst != null) {
					inst.removePlayer(getObjectId());
					final Location loc = inst.getExitLoc();
					if (loc != null) {
						final int x = loc.getX() + Rnd.get(-30, 30);
						final int y = loc.getY() + Rnd.get(-30, 30);
						setXYZInvisible(x, y, loc.getZ());
						if (hasSummon()) // dead pet
						{
							getSummon().teleToLocation(loc, true);
							getSummon().setInstanceId(0);
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// TvT Event removal
		try {
			TvTEvent.onLogout(this);
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Update database with items in its inventory and remove them from the world
		try {
			getInventory().deleteMe();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		// Update database with items in its warehouse and remove them from the world
		try {
			clearWarehouse();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		if (general().warehouseCache()) {
			WarehouseCacheManager.getInstance().remCacheTask(this);
		}
		
		try {
			getFreight().deleteMe();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		try {
			clearRefund();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		if (isCursedWeaponEquipped()) {
			try {
				CursedWeaponsManager.getInstance().getCursedWeapon(_cursedWeaponEquippedId).setPlayer(null);
			} catch (Exception e) {
				LOG.error("deleteMe() {}", e);
			}
		}
		
		// Remove all L2Object from _knownObjects and _knownPlayer of the L2Character then cancel Attak or Cast and notify AI
		try {
			getKnownList().removeAllKnownObjects();
		} catch (Exception e) {
			LOG.error("deleteMe() {}", e);
		}
		
		if (getClanId() > 0) {
			getClan().broadcastToOtherOnlineMembers(new PledgeShowMemberListUpdate(this), this);
			// ClanTable.getInstance().getClan(getClanId()).broadcastToOnlineMembers(new PledgeShowMemberListAdd(this));
		}
		
		for (L2PcInstance player : _snoopedPlayer) {
			player.removeSnooper(this);
		}
		
		for (L2PcInstance player : _snoopListener) {
			player.removeSnooped(this);
		}
		
		// Remove L2Object object from _allObjects of L2World
		L2World.getInstance().removeObject(this);
		L2World.getInstance().removeFromAllPlayers(this);
		
		try {
			notifyFriends();
			getBlockList().playerLogout();
		} catch (Exception e) {
			LOG.error("Exception on deleteMe() notifyFriends: {}", e);
		}
	}
	
	// startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check.
	// Also worthy of note is the fact the code to find the hook landing position was also striped.
	// The stripped code was moved into fishing.java.
	// In my opinion it makes more sense for it to be there since all other skill related checks were also there.
	// Last but not least, moving the zone check there, fixed a bug where baits would always be consumed no matter if fishing actualy took place.
	// startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing coordinates.
	public void startFishing(int _x, int _y, int _z) {
		stopMove(null);
		setIsImmobilized(true);
		_fishing = true;
		_fishx = _x;
		_fishy = _y;
		_fishz = _z;
		// broadcastUserInfo();
		// Starts fishing
		int lvl = getRandomFishLvl();
		int grade = getRandomFishGrade();
		int group = getRandomFishGroup(grade);
		List<L2Fish> fish = FishData.getInstance().getFish(lvl, group, grade);
		if ((fish == null) || fish.isEmpty()) {
			sendMessage("Error - Fish are not defined");
			endFishing(false);
			return;
		}
		// Use a copy constructor else the fish data may be over-written below
		_fish = fish.get(Rnd.get(fish.size())).clone();
		fish.clear();
		sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		if (!GameTimeController.getInstance().isNight() && _lure.isNightLure()) {
			_fish.setFishGroup(-1);
		}
		// sendMessage("Hook x,y: " + _x + "," + _y + " - Water Z, Player Z:" + _z + ", " + getZ()); //debug line, uncoment to show coordinates used in fishing.
		broadcastPacket(new ExFishingStart(this, _fish.getFishGroup(), _x, _y, _z, _lure.isNightLure()));
		sendPacket(Music.SF_P_01.getPacket());
		startLookingForFishTask();
	}
	
	public void stopLookingForFishTask() {
		if (_taskforfish != null) {
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void startLookingForFishTask() {
		if (!isDead() && (_taskforfish == null)) {
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null) {
				int lureid = _lure.getId();
				isNoob = _fish.getFishGrade() == 0;
				isUpperGrade = _fish.getFishGrade() == 2;
				if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511)) {
					checkDelay = _fish.getGutsCheckTime() * 133;
				} else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486))) {
					checkDelay = _fish.getGutsCheckTime() * 100;
				} else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513)) {
					checkDelay = _fish.getGutsCheckTime() * 66;
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(this, _fish.getStartCombatTime(), _fish.getFishGuts(), _fish.getFishGroup(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int getRandomFishGrade() {
		switch (_lure.getId()) {
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}
	
	private int getRandomFishGroup(int group) {
		int check = Rnd.get(100);
		int type = 1;
		switch (group) {
			case 0: // fish for novices
				switch (_lure.getId()) {
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54) {
							type = 5;
						} else if (check <= 77) {
							type = 4;
						} else {
							type = 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54) {
							type = 4;
						} else if (check <= 77) {
							type = 6;
						} else {
							type = 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54) {
							type = 6;
						} else if (check <= 77) {
							type = 5;
						} else {
							type = 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33) {
							type = 4;
						} else if (check <= 66) {
							type = 5;
						} else {
							type = 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch (_lure.getId()) {
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54) {
							type = 1;
						} else if (check <= 74) {
							type = 0;
						} else if (check <= 94) {
							type = 2;
						} else {
							type = 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54) {
							type = 0;
						} else if (check <= 74) {
							type = 1;
						} else if (check <= 94) {
							type = 2;
						} else {
							type = 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55) {
							type = 2;
						} else if (check <= 74) {
							type = 1;
						} else if (check <= 94) {
							type = 0;
						} else {
							type = 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33) {
							type = 0;
						} else if (check <= 66) {
							type = 1;
						} else {
							type = 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch (_lure.getId()) {
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54) {
							type = 8;
						} else if (check <= 77) {
							type = 7;
						} else {
							type = 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54) {
							type = 7;
						} else if (check <= 77) {
							type = 9;
						} else {
							type = 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54) {
							type = 9;
						} else if (check <= 77) {
							type = 8;
						} else {
							type = 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if (check <= 33) {
							type = 7;
						} else if (check <= 66) {
							type = 8;
						} else {
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	private int getRandomFishLvl() {
		int skilllvl = getSkillLevel(1315);
		final BuffInfo info = getEffectList().getBuffInfoBySkillId(2274);
		if (info != null) {
			// TODO (Adry_85): Unhardcode
			skilllvl = 0;
			switch (info.getSkill().getLevel()) {
				case 1: {
					skilllvl = 2;
				}
				case 2: {
					skilllvl = 5;
				}
				case 3: {
					skilllvl = 8;
				}
				case 4: {
					skilllvl = 11;
				}
				case 5: {
					skilllvl = 14;
				}
				case 6: {
					skilllvl = 17;
				}
				case 7: {
					skilllvl = 20;
				}
				case 8: {
					skilllvl = 23;
				}
			}
		}
		if (skilllvl <= 0) {
			return 1;
		}
		int randomlvl;
		int check = Rnd.get(100);
		
		if (check <= 50) {
			randomlvl = skilllvl;
		} else if (check <= 85) {
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0) {
				randomlvl = 1;
			}
		} else {
			randomlvl = skilllvl + 1;
			if (randomlvl > 27) {
				randomlvl = 27;
			}
		}
		
		return randomlvl;
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade) {
		_fishCombat = new L2Fishing(this, _fish, isNoob, isUpperGrade);
	}
	
	public void endFishing(boolean win) {
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		// broadcastUserInfo();
		if (_fishCombat == null) {
			sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		broadcastPacket(new ExFishingEnd(win, this));
		sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		setIsImmobilized(false);
		stopLookingForFishTask();
	}
	
	public L2Fishing getFishCombat() {
		return _fishCombat;
	}
	
	public int getFishx() {
		return _fishx;
	}
	
	public int getFishy() {
		return _fishy;
	}
	
	public int getFishz() {
		return _fishz;
	}
	
	public L2ItemInstance getLure() {
		return _lure;
	}
	
	public void setLure(L2ItemInstance lure) {
		_lure = lure;
	}
	
	public int getInventoryLimit() {
		int ivlim;
		if (isGM()) {
			ivlim = character().getMaximumSlotsForGMPlayer();
		} else if (getRace() == Race.DWARF) {
			ivlim = character().getMaximumSlotsForDwarf();
		} else {
			ivlim = character().getMaximumSlotsForNoDwarf();
		}
		ivlim += (int) getStat().calcStat(Stats.INV_LIM, 0, null, null);
		
		return ivlim;
	}
	
	public int getWareHouseLimit() {
		int whlim;
		if (getRace() == Race.DWARF) {
			whlim = character().getMaximumWarehouseSlotsForDwarf();
		} else {
			whlim = character().getMaximumWarehouseSlotsForNoDwarf();
		}
		
		whlim += (int) getStat().calcStat(Stats.WH_LIM, 0, null, null);
		
		return whlim;
	}
	
	public int getPrivateSellStoreLimit() {
		int pslim;
		
		if (getRace() == Race.DWARF) {
			pslim = character().getMaxPvtStoreSellSlotsDwarf();
		} else {
			pslim = character().getMaxPvtStoreSellSlotsOther();
		}
		
		pslim += (int) getStat().calcStat(Stats.P_SELL_LIM, 0, null, null);
		
		return pslim;
	}
	
	public int getPrivateBuyStoreLimit() {
		int pblim;
		
		if (getRace() == Race.DWARF) {
			pblim = character().getMaxPvtStoreBuySlotsDwarf();
		} else {
			pblim = character().getMaxPvtStoreBuySlotsOther();
		}
		pblim += (int) getStat().calcStat(Stats.P_BUY_LIM, 0, null, null);
		
		return pblim;
	}
	
	public int getDwarfRecipeLimit() {
		int recdlim = character().getDwarfRecipeLimit();
		recdlim += (int) getStat().calcStat(Stats.REC_D_LIM, 0, null, null);
		return recdlim;
	}
	
	public int getCommonRecipeLimit() {
		int recclim = character().getCommonRecipeLimit();
		recclim += (int) getStat().calcStat(Stats.REC_C_LIM, 0, null, null);
		return recclim;
	}
	
	public int getMountNpcId() {
		return _mountNpcId;
	}
	
	public int getMountLevel() {
		return _mountLevel;
	}
	
	public int getMountObjectID() {
		return _mountObjectID;
	}
	
	public void setMountObjectID(int newID) {
		_mountObjectID = newID;
	}
	
	/**
	 * @return the current skill in use or return null.
	 */
	public SkillUseHolder getCurrentSkill() {
		return _currentSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player _currentSkill.
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed) {
		if (currentSkill == null) {
			_currentSkill = null;
			return;
		}
		_currentSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * @return the current pet skill in use or return null.
	 */
	public SkillUseHolder getCurrentPetSkill() {
		return _currentPetSkill;
	}
	
	/**
	 * Create a new SkillDat object and set the player _currentPetSkill.
	 * @param currentSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setCurrentPetSkill(Skill currentSkill, boolean ctrlPressed, boolean shiftPressed) {
		if (currentSkill == null) {
			_currentPetSkill = null;
			return;
		}
		_currentPetSkill = new SkillUseHolder(currentSkill, ctrlPressed, shiftPressed);
	}
	
	public SkillUseHolder getQueuedSkill() {
		return _queuedSkill;
	}
	
	/**
	 * Create a new SkillDat object and queue it in the player _queuedSkill.
	 * @param queuedSkill
	 * @param ctrlPressed
	 * @param shiftPressed
	 */
	public void setQueuedSkill(Skill queuedSkill, boolean ctrlPressed, boolean shiftPressed) {
		if (queuedSkill == null) {
			_queuedSkill = null;
			return;
		}
		_queuedSkill = new SkillUseHolder(queuedSkill, ctrlPressed, shiftPressed);
	}
	
	/**
	 * @return {@code true} if player is jailed, {@code false} otherwise.
	 */
	public boolean isJailed() {
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.JAIL) || PunishmentManager.getInstance().hasPunishment(getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.JAIL)
			|| PunishmentManager.getInstance().hasPunishment(getIPAddress(), PunishmentAffect.IP, PunishmentType.JAIL);
	}
	
	/**
	 * @return {@code true} if player is chat banned, {@code false} otherwise.
	 */
	public boolean isChatBanned() {
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.CHAT_BAN) || PunishmentManager.getInstance().hasPunishment(getAccountName(), PunishmentAffect.ACCOUNT, PunishmentType.CHAT_BAN)
			|| PunishmentManager.getInstance().hasPunishment(getIPAddress(), PunishmentAffect.IP, PunishmentType.CHAT_BAN);
	}
	
	public void startFameTask(long delay, int fameFixRate) {
		if ((getLevel() < 40) || (getClassId().level() < 2)) {
			return;
		}
		if (_fameTask == null) {
			_fameTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new FameTask(this, fameFixRate), delay, delay);
		}
	}
	
	public void stopFameTask() {
		if (_fameTask != null) {
			_fameTask.cancel(false);
			_fameTask = null;
		}
	}
	
	public void startVitalityTask() {
		if (vitality().enabled() && (_vitalityTask == null)) {
			_vitalityTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new VitalityTask(this), 1000, 60000);
		}
	}
	
	public void stopVitalityTask() {
		if (_vitalityTask != null) {
			_vitalityTask.cancel(false);
			_vitalityTask = null;
		}
	}
	
	public int getPowerGrade() {
		return _powerGrade;
	}
	
	public void setPowerGrade(int power) {
		_powerGrade = power;
	}
	
	public boolean isCursedWeaponEquipped() {
		return _cursedWeaponEquippedId != 0;
	}
	
	public int getCursedWeaponEquippedId() {
		return _cursedWeaponEquippedId;
	}
	
	public void setCursedWeaponEquippedId(int value) {
		_cursedWeaponEquippedId = value;
	}
	
	public boolean isCombatFlagEquipped() {
		return _combatFlagEquippedId;
	}
	
	public void setCombatFlagEquipped(boolean value) {
		_combatFlagEquippedId = value;
	}
	
	/**
	 * Returns the Number of Souls this L2PcInstance got.
	 * @return
	 */
	public int getChargedSouls() {
		return _souls;
	}
	
	/**
	 * Increase Souls
	 * @param count
	 */
	public void increaseSouls(int count) {
		_souls += count;
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOUR_SOUL_HAS_INCREASED_BY_S1_SO_IT_IS_NOW_AT_S2);
		sm.addInt(count);
		sm.addInt(_souls);
		sendPacket(sm);
		restartSoulTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Decreases existing Souls.
	 * @param count
	 * @param skill
	 * @return consumed souls count
	 */
	public int decreaseSouls(int count) {
		if (_souls == 0) {
			return 0;
		}
		
		int consumedSouls;
		if (_souls <= count) {
			consumedSouls = _souls;
			_souls = 0;
			stopSoulTask();
		} else {
			_souls -= count;
			consumedSouls = count;
			restartSoulTask();
		}
		
		sendPacket(new EtcStatusUpdate(this));
		return consumedSouls;
	}
	
	/**
	 * Clear out all Souls from this L2PcInstance
	 */
	public void clearSouls() {
		_souls = 0;
		stopSoulTask();
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Starts/Restarts the SoulTask to Clear Souls after 10 Mins.
	 */
	private void restartSoulTask() {
		if (_soulTask != null) {
			_soulTask.cancel(false);
			_soulTask = null;
		}
		_soulTask = ThreadPoolManager.getInstance().scheduleGeneral(new ResetSoulsTask(this), 600000);
		
	}
	
	/**
	 * Stops the Clearing Task.
	 */
	public void stopSoulTask() {
		if (_soulTask != null) {
			_soulTask.cancel(false);
			_soulTask = null;
		}
	}
	
	public int getDeathPenaltyBuffLevel() {
		return _deathPenaltyBuffLevel;
	}
	
	public void setDeathPenaltyBuffLevel(int level) {
		_deathPenaltyBuffLevel = level;
	}
	
	public void calculateDeathPenaltyBuffLevel(L2Character killer) {
		if (killer == null) {
			LOG.warn("{} called calculateDeathPenaltyBuffLevel with killer null!", this);
			return;
		}
		
		if (isResurrectSpecialAffected() || isLucky() || isBlockedFromDeathPenalty() || isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.SIEGE) || canOverrideCond(PcCondOverride.DEATH_PENALTY)) {
			return;
		}
		double percent = 1.0;
		
		if (killer.isRaid()) {
			percent *= calcStat(Stats.REDUCE_DEATH_PENALTY_BY_RAID, 1);
		} else if (killer.isMonster()) {
			percent *= calcStat(Stats.REDUCE_DEATH_PENALTY_BY_MOB, 1);
		} else if (killer.isPlayable()) {
			percent *= calcStat(Stats.REDUCE_DEATH_PENALTY_BY_PVP, 1);
		}
		
		if (Rnd.get(1, 100) <= (character().getDeathPenaltyChance() * percent)) {
			if (!killer.isPlayable() || (getKarma() > 0)) {
				increaseDeathPenaltyBuffLevel();
			}
		}
	}
	
	public void increaseDeathPenaltyBuffLevel() {
		if (getDeathPenaltyBuffLevel() >= 15) {
			return;
		}
		
		if (getDeathPenaltyBuffLevel() != 0) {
			Skill skill = SkillData.getInstance().getSkill(5076, getDeathPenaltyBuffLevel());
			
			if (skill != null) {
				removeSkill(skill, true);
			}
		}
		_deathPenaltyBuffLevel++;
		addSkill(SkillData.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
		sendPacket(new EtcStatusUpdate(this));
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
		sm.addInt(getDeathPenaltyBuffLevel());
		sendPacket(sm);
	}
	
	public void reduceDeathPenaltyBuffLevel() {
		if (getDeathPenaltyBuffLevel() <= 0) {
			return;
		}
		
		Skill skill = SkillData.getInstance().getSkill(5076, getDeathPenaltyBuffLevel());
		
		if (skill != null) {
			removeSkill(skill, true);
		}
		
		_deathPenaltyBuffLevel--;
		
		if (getDeathPenaltyBuffLevel() > 0) {
			addSkill(SkillData.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
			sendPacket(new EtcStatusUpdate(this));
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED);
			sm.addInt(getDeathPenaltyBuffLevel());
			sendPacket(sm);
		} else {
			sendPacket(new EtcStatusUpdate(this));
			sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED);
		}
	}
	
	public void restoreDeathPenaltyBuffLevel() {
		if (getDeathPenaltyBuffLevel() > 0) {
			addSkill(SkillData.getInstance().getSkill(5076, getDeathPenaltyBuffLevel()), false);
		}
	}
	
	@Override
	public L2PcInstance getActingPlayer() {
		return this;
	}
	
	@Override
	public int getMaxLoad() {
		return (int) calcStat(Stats.WEIGHT_LIMIT, Math.floor(BaseStats.CON.calcBonus(this) * 69000 * character().getWeightLimit()), this, null);
	}
	
	@Override
	public int getBonusWeightPenalty() {
		return (int) calcStat(Stats.WEIGHT_PENALTY, 1, this, null);
	}
	
	@Override
	public int getCurrentLoad() {
		return getInventory().getTotalWeight();
	}
	
	@Override
	public final void sendDamageMessage(L2Character target, int damage, boolean mcrit, boolean pcrit, boolean miss) {
		// Check if hit is missed
		if (miss) {
			if (target.isPlayer()) {
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_EVADED_C2_ATTACK);
				sm.addPcName(target.getActingPlayer());
				sm.addCharName(this);
				target.sendPacket(sm);
			}
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ATTACK_WENT_ASTRAY);
			sm.addPcName(this);
			sendPacket(sm);
			return;
		}
		
		// Check if hit is critical
		if (pcrit) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_HAD_CRITICAL_HIT);
			sm.addPcName(this);
			sendPacket(sm);
		}
		if (mcrit) {
			sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC);
		}
		
		if (isInOlympiadMode() && target.isPlayer() && target.getActingPlayer().isInOlympiadMode() && (target.getActingPlayer().getOlympiadGameId() == getOlympiadGameId())) {
			OlympiadGameManager.getInstance().notifyCompetitorDamage(this, damage);
		}
		
		final SystemMessage sm;
		
		if ((target.isInvul() || target.isHpBlocked()) && !target.isNpc()) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACK_WAS_BLOCKED);
		} else if (target.isDoor() || (target instanceof L2ControlTowerInstance)) {
			sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG);
			sm.addInt(damage);
		} else {
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_DONE_S3_DAMAGE_TO_C2);
			sm.addPcName(this);
			sm.addCharName(target);
			sm.addInt(damage);
		}
		sendPacket(sm);
	}
	
	/**
	 * @return
	 */
	public int getAgathionId() {
		return _agathionId;
	}
	
	/**
	 * @param npcId
	 */
	public void setAgathionId(int npcId) {
		_agathionId = npcId;
	}
	
	public int getVitalityPoints() {
		return getStat().getVitalityPoints();
	}
	
	/**
	 * @return Vitality Level
	 */
	public int getVitalityLevel() {
		return getStat().getVitalityLevel();
	}
	
	public void setVitalityPoints(int points, boolean quiet) {
		getStat().setVitalityPoints(points, quiet);
	}
	
	public void updateVitalityPoints(float points, boolean useRates, boolean quiet) {
		getStat().updateVitalityPoints(points, useRates, quiet);
	}
	
	public void checkItemRestriction() {
		for (int i = 0; i < Inventory.PAPERDOLL_TOTALSLOTS; i++) {
			L2ItemInstance equippedItem = getInventory().getPaperdollItem(i);
			if ((equippedItem != null) && !equippedItem.getItem().checkCondition(this, this, false)) {
				getInventory().unEquipItemInSlot(i);
				
				InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(equippedItem);
				sendPacket(iu);
				
				SystemMessage sm = null;
				if (equippedItem.getItem().getBodyPart() == L2Item.SLOT_BACK) {
					sendPacket(SystemMessageId.CLOAK_REMOVED_BECAUSE_ARMOR_SET_REMOVED);
					return;
				}
				
				if (equippedItem.getEnchantLevel() > 0) {
					sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
					sm.addInt(equippedItem.getEnchantLevel());
					sm.addItemName(equippedItem);
				} else {
					sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
					sm.addItemName(equippedItem);
				}
				sendPacket(sm);
			}
		}
	}
	
	public void addTransformSkill(Skill sk) {
		if (_transformSkills == null) {
			synchronized (this) {
				if (_transformSkills == null) {
					_transformSkills = new ConcurrentHashMap<>();
				}
			}
		}
		_transformSkills.put(sk.getId(), sk);
		if (sk.isPassive()) {
			addSkill(sk, false);
		}
	}
	
	public Skill getTransformSkill(int id) {
		if (_transformSkills == null) {
			return null;
		}
		return _transformSkills.get(id);
	}
	
	public boolean hasTransformSkill(int id) {
		if (_transformSkills == null) {
			return false;
		}
		return _transformSkills.containsKey(id);
	}
	
	public void removeAllTransformSkills() {
		_transformSkills = null;
	}
	
	protected void startFeed(int npcId) {
		_canFeed = npcId > 0;
		if (!isMounted()) {
			return;
		}
		if (hasSummon()) {
			setCurrentFeed(((L2PetInstance) getSummon()).getCurrentFed());
			_controlItemId = getSummon().getControlObjectId();
			sendPacket(new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume()));
			if (!isDead()) {
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PetFeedTask(this), 10000, 10000);
			}
		} else if (_canFeed) {
			setCurrentFeed(getMaxFeed());
			SetupGauge sg = new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume());
			sendPacket(sg);
			if (!isDead()) {
				_mountFeedTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new PetFeedTask(this), 10000, 10000);
			}
		}
	}
	
	public void stopFeed() {
		if (_mountFeedTask != null) {
			_mountFeedTask.cancel(false);
			_mountFeedTask = null;
		}
	}
	
	private final L2PetLevelData getPetLevelData(int npcId) {
		if (_leveldata == null) {
			_leveldata = PetDataTable.getInstance().getPetData(npcId).getPetLevelData(getMountLevel());
		}
		return _leveldata;
	}
	
	public int getCurrentFeed() {
		return _curFeed;
	}
	
	public void setCurrentFeed(int num) {
		boolean lastHungryState = isHungry();
		_curFeed = num > getMaxFeed() ? getMaxFeed() : num;
		SetupGauge sg = new SetupGauge(3, (getCurrentFeed() * 10000) / getFeedConsume(), (getMaxFeed() * 10000) / getFeedConsume());
		sendPacket(sg);
		// broadcast move speed change when strider becomes hungry / full
		if (lastHungryState != isHungry()) {
			broadcastUserInfo();
		}
	}
	
	public int getFeedConsume() {
		// if pet is attacking
		if (isAttackingNow()) {
			return getPetLevelData(_mountNpcId).getPetFeedBattle();
		}
		return getPetLevelData(_mountNpcId).getPetFeedNormal();
	}
	
	private int getMaxFeed() {
		return getPetLevelData(_mountNpcId).getPetMaxFeed();
	}
	
	public boolean isHungry() {
		return _canFeed ? (getCurrentFeed() < ((PetDataTable.getInstance().getPetData(getMountNpcId()).getHungryLimit() / 100f) * getPetLevelData(getMountNpcId()).getPetMaxFeed())) : false;
	}
	
	public void enteredNoLanding(int delay) {
		_dismountTask = ThreadPoolManager.getInstance().scheduleGeneral(new DismountTask(this), delay * 1000);
	}
	
	public void exitedNoLanding() {
		if (_dismountTask != null) {
			_dismountTask.cancel(true);
			_dismountTask = null;
		}
	}
	
	public void setIsInSiege(boolean b) {
		_isInSiege = b;
	}
	
	public boolean isInSiege() {
		return _isInSiege;
	}
	
	/**
	 * @param isInHideoutSiege sets the value of {@link #_isInHideoutSiege}.
	 */
	public void setIsInHideoutSiege(boolean isInHideoutSiege) {
		_isInHideoutSiege = isInHideoutSiege;
	}
	
	/**
	 * @return the value of {@link #_isInHideoutSiege}, {@code true} if the player is participing on a Hideout Siege, otherwise {@code false}.
	 */
	public boolean isInHideoutSiege() {
		return _isInHideoutSiege;
	}
	
	public FloodProtectors getFloodProtectors() {
		return getClient().getFloodProtectors();
	}
	
	public boolean isFlyingMounted() {
		return (isTransformed() && (getTransformation().isFlying()));
	}
	
	/**
	 * Returns the Number of Charges this L2PcInstance got.
	 * @return
	 */
	public int getCharges() {
		return _charges.get();
	}
	
	public void increaseCharges(int count, int max) {
		if (_charges.get() >= max) {
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
			return;
		}
		
		// Charge clear task should be reset every time a charge is increased.
		restartChargeTask();
		
		if (_charges.addAndGet(count) >= max) {
			_charges.set(max);
			sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED);
		} else {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addInt(_charges.get());
			sendPacket(sm);
		}
		
		sendPacket(new EtcStatusUpdate(this));
	}
	
	public boolean decreaseCharges(int count) {
		if (_charges.get() < count) {
			return false;
		}
		
		// Charge clear task should be reset every time a charge is decreased and stopped when charges become 0.
		if (_charges.addAndGet(-count) == 0) {
			stopChargeTask();
		} else {
			restartChargeTask();
		}
		
		sendPacket(new EtcStatusUpdate(this));
		return true;
	}
	
	public void clearCharges() {
		_charges.set(0);
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
	 */
	private void restartChargeTask() {
		if (_chargeTask != null) {
			synchronized (this) {
				if (_chargeTask != null) {
					_chargeTask.cancel(false);
				}
			}
		}
		_chargeTask = ThreadPoolManager.getInstance().scheduleGeneral(new ResetChargesTask(this), 600000);
	}
	
	/**
	 * Stops the Charges Clearing Task.
	 */
	public void stopChargeTask() {
		if (_chargeTask != null) {
			_chargeTask.cancel(false);
			_chargeTask = null;
		}
	}
	
	public void teleportBookmarkModify(int id, int icon, String tag, String name) {
		final TeleportBookmark bookmark = _tpbookmarks.get(id);
		if (bookmark != null) {
			bookmark.setIcon(icon);
			bookmark.setTag(tag);
			bookmark.setName(name);
			
			DAOFactory.getInstance().getTeleportBookmarkDAO().update(this, id, icon, tag, name);
		}
		
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	public void teleportBookmarkDelete(int id) {
		if (_tpbookmarks.remove(id) != null) {
			sendPacket(new ExGetBookMarkInfoPacket(this));
			
			DAOFactory.getInstance().getTeleportBookmarkDAO().delete(this, id);
		}
	}
	
	public void teleportBookmarkGo(int id) {
		if (!teleportBookmarkCondition(0)) {
			return;
		}
		if (getInventory().getInventoryItemCount(13016, 0) == 0) {
			sendPacket(SystemMessageId.YOU_CANNOT_TELEPORT_BECAUSE_YOU_DO_NOT_HAVE_A_TELEPORT_ITEM);
			return;
		}
		SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(13016);
		sendPacket(sm);
		
		final TeleportBookmark bookmark = _tpbookmarks.get(id);
		if (bookmark != null) {
			destroyItem("Consume", getInventory().getItemByItemId(13016).getObjectId(), 1, null, false);
			teleToLocation(bookmark, false);
		}
		sendPacket(new ExGetBookMarkInfoPacket(this));
	}
	
	public boolean teleportBookmarkCondition(int type) {
		if (isInCombat()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
			return false;
		} else if (isInSiege() || (getSiegeState() != 0)) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING);
			return false;
		} else if (isInDuel()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
			return false;
		} else if (isFlying()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
			return false;
		} else if (isInOlympiadMode()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
			return false;
		} else if (isParalyzed()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_PARALYZED);
			return false;
		} else if (isDead()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
			return false;
		} else if ((type == 1) && (isIn7sDungeon() || (isInParty() && getParty().isInDimensionalRift()))) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		} else if (isInWater()) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
			return false;
		} else if ((type == 1) && (isInsideZone(ZoneId.SIEGE) || isInsideZone(ZoneId.CLAN_HALL) || isInsideZone(ZoneId.JAIL) || isInsideZone(ZoneId.CASTLE) || isInsideZone(ZoneId.NO_SUMMON_FRIEND) || isInsideZone(ZoneId.FORT))) {
			sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			return false;
		} else if (isInsideZone(ZoneId.NO_BOOKMARK) || isInBoat() || isInAirShip()) {
			if (type == 0) {
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_IN_THIS_AREA);
			} else if (type == 1) {
				sendPacket(SystemMessageId.YOU_CANNOT_USE_MY_TELEPORTS_TO_REACH_THIS_AREA);
			}
			return false;
		}
		/*
		 * TODO: Instant Zone still not implemented else if (isInsideZone(ZoneId.INSTANT)) { sendPacket(SystemMessage.getSystemMessage(2357)); return; }
		 */
		else {
			return true;
		}
	}
	
	public void teleportBookmarkAdd(int x, int y, int z, int icon, String tag, String name) {
		if (!teleportBookmarkCondition(1)) {
			return;
		}
		
		if (_tpbookmarks.size() >= _bookmarkslot) {
			sendPacket(SystemMessageId.YOU_HAVE_NO_SPACE_TO_SAVE_THE_TELEPORT_LOCATION);
			return;
		}
		
		if (getInventory().getInventoryItemCount(20033, 0) == 0) {
			sendPacket(SystemMessageId.YOU_CANNOT_BOOKMARK_THIS_LOCATION_BECAUSE_YOU_DO_NOT_HAVE_A_MY_TELEPORT_FLAG);
			return;
		}
		
		int id;
		for (id = 1; id <= _bookmarkslot; ++id) {
			if (!_tpbookmarks.containsKey(id)) {
				break;
			}
		}
		_tpbookmarks.put(id, new TeleportBookmark(id, x, y, z, icon, tag, name));
		
		destroyItem("Consume", getInventory().getItemByItemId(20033).getObjectId(), 1, null, false);
		
		final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED);
		sm.addItemName(20033);
		sendPacket(sm);
		
		sendPacket(new ExGetBookMarkInfoPacket(this));
		
		DAOFactory.getInstance().getTeleportBookmarkDAO().insert(this, id, x, y, z, icon, tag, name);
	}
	
	@Override
	public void sendInfo(L2PcInstance activeChar) {
		if (isInBoat()) {
			setXYZ(getBoat().getLocation());
			
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1)) {
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon()) {
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2)) {
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon()) {
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
			activeChar.sendPacket(new GetOnVehicle(getObjectId(), getBoat().getObjectId(), getInVehiclePosition()));
		} else if (isInAirShip()) {
			setXYZ(getAirShip().getLocation());
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1)) {
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon()) {
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2)) {
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon()) {
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
			activeChar.sendPacket(new ExGetOnAirShip(this, getAirShip()));
		} else {
			activeChar.sendPacket(new CharInfo(this));
			activeChar.sendPacket(new ExBrExtraUserInfo(this));
			int relation1 = getRelation(activeChar);
			int relation2 = activeChar.getRelation(this);
			Integer oldrelation = getKnownList().getKnownRelations().get(activeChar.getObjectId());
			if ((oldrelation != null) && (oldrelation != relation1)) {
				activeChar.sendPacket(new RelationChanged(this, relation1, isAutoAttackable(activeChar)));
				if (hasSummon()) {
					activeChar.sendPacket(new RelationChanged(getSummon(), relation1, isAutoAttackable(activeChar)));
				}
			}
			oldrelation = activeChar.getKnownList().getKnownRelations().get(getObjectId());
			if ((oldrelation != null) && (oldrelation != relation2)) {
				sendPacket(new RelationChanged(activeChar, relation2, activeChar.isAutoAttackable(this)));
				if (activeChar.hasSummon()) {
					sendPacket(new RelationChanged(activeChar.getSummon(), relation2, activeChar.isAutoAttackable(this)));
				}
			}
		}
		
		switch (getPrivateStoreType()) {
			case SELL:
				activeChar.sendPacket(new PrivateStoreMsgSell(this));
				break;
			case PACKAGE_SELL:
				activeChar.sendPacket(new ExPrivateStoreSetWholeMsg(this));
				break;
			case BUY:
				activeChar.sendPacket(new PrivateStoreMsgBuy(this));
				break;
			case MANUFACTURE:
				activeChar.sendPacket(new RecipeShopMsg(this));
				break;
		}
		if (isTransformed()) {
			// Required double send for fix Mounted H5+
			sendPacket(new CharInfo(activeChar));
		}
	}
	
	public void showQuestMovie(int id) {
		if (_movieId > 0) {
			return;
		}
		abortAttack();
		abortCast();
		stopMove(null);
		_movieId = id;
		sendPacket(new ExStartScenePlayer(id));
	}
	
	public boolean isAllowedToEnchantSkills() {
		if (isLocked()) {
			return false;
		}
		if (isTransformed() || isInStance()) {
			return false;
		}
		if (AttackStanceTaskManager.getInstance().hasAttackStanceTask(this)) {
			return false;
		}
		if (isCastingNow() || isCastingSimultaneouslyNow()) {
			return false;
		}
		if (isInBoat() || isInAirShip()) {
			return false;
		}
		return true;
	}
	
	/**
	 * @return the _createDate of the L2PcInstance.
	 */
	public Calendar getCreateDate() {
		return _createDate;
	}
	
	/**
	 * Set the _createDate of the L2PcInstance.
	 * @param createDate
	 */
	public void setCreateDate(Calendar createDate) {
		_createDate = createDate;
	}
	
	/**
	 * @return number of days to char birthday.
	 */
	public int checkBirthDay() {
		Calendar now = Calendar.getInstance();
		
		// "Characters with a February 29 creation date will receive a gift on February 28."
		if ((_createDate.get(Calendar.DAY_OF_MONTH) == 29) && (_createDate.get(Calendar.MONTH) == 1)) {
			_createDate.add(Calendar.HOUR_OF_DAY, -24);
		}
		
		if ((now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH)) && (now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH)) && (now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR))) {
			return 0;
		}
		
		int i;
		for (i = 1; i < 6; i++) {
			now.add(Calendar.HOUR_OF_DAY, 24);
			if ((now.get(Calendar.MONTH) == _createDate.get(Calendar.MONTH)) && (now.get(Calendar.DAY_OF_MONTH) == _createDate.get(Calendar.DAY_OF_MONTH)) && (now.get(Calendar.YEAR) != _createDate.get(Calendar.YEAR))) {
				return i;
			}
		}
		return -1;
	}
	
	public Set<Integer> getFriends() {
		if (_friends == null) {
			synchronized (this) {
				if (_friends == null) {
					_friends = ConcurrentHashMap.newKeySet(1);
				}
			}
		}
		return _friends;
	}
	
	public boolean hasFriends() {
		return (_friends != null) && !_friends.isEmpty();
	}
	
	public boolean isFriend(int objectId) {
		return hasFriends() && _friends.contains(objectId);
	}
	
	public void addFriend(int objectId) {
		getFriends().add(objectId);
	}
	
	public void removeFriend(int objectId) {
		if (hasFriends()) {
			_friends.remove(objectId);
		}
	}
	
	private void notifyFriends() {
		if (hasFriends()) {
			final FriendStatusPacket pkt = new FriendStatusPacket(getObjectId());
			for (int id : _friends) {
				final L2PcInstance friend = L2World.getInstance().getPlayer(id);
				if (friend != null) {
					friend.sendPacket(pkt);
				}
			}
		}
	}
	
	/**
	 * Verify if this player is in silence mode.
	 * @return the {@code true} if this player is in silence mode, {@code false} otherwise
	 */
	public boolean isSilenceMode() {
		return _silenceMode;
	}
	
	/**
	 * Set the silence mode.
	 * @param mode the value
	 */
	public void setSilenceMode(boolean mode) {
		_silenceMode = mode;
		if (_silenceModeExcluded != null) {
			_silenceModeExcluded.clear(); // Clear the excluded list on each setSilenceMode
		}
		sendPacket(new EtcStatusUpdate(this));
	}
	
	/**
	 * While at silenceMode, checks if this player blocks PMs for this user
	 * @param playerObjId the player object Id
	 * @return {@code true} if the given Id is not excluded and this player is in silence mode, {@code false} otherwise
	 */
	public boolean isSilenceMode(int playerObjId) {
		if (character().silenceModeExclude() && _silenceMode && (_silenceModeExcluded != null)) {
			return !_silenceModeExcluded.contains(playerObjId);
		}
		return _silenceMode;
	}
	
	/**
	 * Add a player to the "excluded silence mode" list.
	 * @param playerObjId the player's object Id
	 */
	public void addSilenceModeExcluded(int playerObjId) {
		if (_silenceModeExcluded == null) {
			_silenceModeExcluded = new ArrayList<>(1);
		}
		_silenceModeExcluded.add(playerObjId);
	}
	
	public double getCollisionRadius() {
		if (isMounted() && (getMountNpcId() > 0)) {
			return NpcData.getInstance().getTemplate(getMountNpcId()).getfCollisionRadius();
		} else if (isTransformed()) {
			return getTransformation().getCollisionRadius(this);
		}
		return getAppearance().getSex() ? getBaseTemplate().getFCollisionRadiusFemale() : getBaseTemplate().getfCollisionRadius();
	}
	
	public double getCollisionHeight() {
		if (isMounted() && (getMountNpcId() > 0)) {
			return NpcData.getInstance().getTemplate(getMountNpcId()).getfCollisionHeight();
		} else if (isTransformed()) {
			return getTransformation().getCollisionHeight(this);
		}
		return getAppearance().getSex() ? getBaseTemplate().getFCollisionHeightFemale() : getBaseTemplate().getfCollisionHeight();
	}
	
	public final int getClientX() {
		return _clientX;
	}
	
	public final void setClientX(int val) {
		_clientX = val;
	}
	
	public final int getClientY() {
		return _clientY;
	}
	
	public final void setClientY(int val) {
		_clientY = val;
	}
	
	public final int getClientZ() {
		return _clientZ;
	}
	
	public final void setClientZ(int val) {
		_clientZ = val;
	}
	
	public final int getClientHeading() {
		return _clientHeading;
	}
	
	public final void setClientHeading(int val) {
		_clientHeading = val;
	}
	
	/**
	 * @param z
	 * @return true if character falling now on the start of fall return false for correct coord sync!
	 */
	public final boolean isFalling(int z) {
		if (isDead() || isFlying() || isFlyingMounted() || isInsideZone(ZoneId.WATER)) {
			return false;
		}
		
		if (System.currentTimeMillis() < _fallingTimestamp) {
			return true;
		}
		
		final int deltaZ = getZ() - z;
		if (deltaZ <= getBaseTemplate().getSafeFallHeight()) {
			return false;
		}
		
		// If there is no geodata loaded for the place we are client Z correction might cause falling damage.
		if (!GeoData.getInstance().hasGeo(getX(), getY())) {
			return false;
		}
		
		final int damage = (int) Formulas.calcFallDam(this, deltaZ);
		if (damage > 0) {
			reduceCurrentHp(Math.min(damage, getCurrentHp() - 1), null, false, true, null);
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1);
			sm.addInt(damage);
			sendPacket(sm);
		}
		
		setFalling();
		
		return false;
	}
	
	/**
	 * Set falling timestamp
	 */
	public final void setFalling() {
		_fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY;
	}
	
	/**
	 * @return the _movieId
	 */
	public int getMovieId() {
		return _movieId;
	}
	
	public void setMovieId(int id) {
		_movieId = id;
	}
	
	/**
	 * Update last item auction request timestamp to current
	 */
	public void updateLastItemAuctionRequest() {
		_lastItemAuctionInfoRequest = System.currentTimeMillis();
	}
	
	/**
	 * @return true if receiving item auction requests<br>
	 *         (last request was in 2 seconds before)
	 */
	public boolean isItemAuctionPolling() {
		return (System.currentTimeMillis() - _lastItemAuctionInfoRequest) < 2000;
	}
	
	@Override
	public boolean isMovementDisabled() {
		return super.isMovementDisabled() || (_movieId > 0);
	}
	
	private void restoreUISettings() {
		_uiKeySettings = new UIKeysSettings(getObjectId());
	}
	
	private void storeUISettings() {
		if (_uiKeySettings == null) {
			return;
		}
		
		if (!_uiKeySettings.isSaved()) {
			_uiKeySettings.saveInDB();
		}
	}
	
	public UIKeysSettings getUISettings() {
		return _uiKeySettings;
	}
	
	public String getHtmlPrefix() {
		if (!customs().multiLangEnable()) {
			return null;
		}
		return _htmlPrefix;
	}
	
	public String getLang() {
		return _lang;
	}
	
	public boolean setLang(String lang) {
		boolean result = false;
		if (customs().multiLangEnable()) {
			if (customs().getMultiLangAllowed().contains(lang)) {
				_lang = lang;
				result = true;
			} else {
				_lang = customs().getMultiLangDefault();
			}
			
			_htmlPrefix = "data/lang/" + _lang + "/";
		} else {
			_lang = null;
			_htmlPrefix = null;
		}
		
		return result;
	}
	
	public long getOfflineStartTime() {
		return _offlineShopStart;
	}
	
	public void setOfflineStartTime(long time) {
		_offlineShopStart = time;
	}
	
	/**
	 * Remove player from BossZones (used on char logout/exit)
	 */
	public void removeFromBossZone() {
		try {
			for (L2BossZone zone : GrandBossManager.getInstance().getZones().values()) {
				zone.removePlayer(this);
			}
		} catch (Exception e) {
			LOG.warn("Exception on removeFromBossZone(): {}", e);
		}
	}
	
	/**
	 * Check all player skills for skill level. If player level is lower than skill learn level - 9, skill level is decreased to next possible level.
	 */
	public void checkPlayerSkills() {
		for (Entry<Integer, Skill> e : getSkills().entrySet()) {
			final L2SkillLearn learn = SkillTreesData.getInstance().getClassSkill(e.getKey(), e.getValue().getLevel() % 100, getClassId());
			if (learn != null) {
				int lvlDiff = e.getKey() == CommonSkill.EXPERTISE.getId() ? 0 : 9;
				if (getLevel() < (learn.getGetLevel() - lvlDiff)) {
					deacreaseSkillLevel(e.getValue(), lvlDiff);
				}
			}
		}
	}
	
	private void deacreaseSkillLevel(Skill skill, int lvlDiff) {
		int nextLevel = -1;
		final Map<Integer, L2SkillLearn> skillTree = SkillTreesData.getInstance().getCompleteClassSkillTree(getClassId());
		for (L2SkillLearn sl : skillTree.values()) {
			if ((sl.getSkillId() == skill.getId()) && (nextLevel < sl.getSkillLevel()) && (getLevel() >= (sl.getGetLevel() - lvlDiff))) {
				nextLevel = sl.getSkillLevel(); // next possible skill level
			}
		}
		
		if (nextLevel == -1) {
			LOG.info("Removing {}, from {}", skill, this);
			removeSkill(skill, true); // there is no lower skill
		} else {
			LOG.info("Decreasing {} to {} for {}", skill, nextLevel, this);
			addSkill(SkillData.getInstance().getSkill(skill.getId(), nextLevel), true); // replace with lower one
		}
	}
	
	public boolean canMakeSocialAction() {
		return ((getPrivateStoreType() == PrivateStoreType.NONE) && (getActiveRequester() == null) && !isAlikeDead() && !isAllSkillsDisabled() && !isCastingNow() && !isCastingSimultaneouslyNow() && (getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE));
	}
	
	public void setMultiSocialAction(int id, int targetId) {
		_multiSociaAction = id;
		_multiSocialTarget = targetId;
	}
	
	public int getMultiSociaAction() {
		return _multiSociaAction;
	}
	
	public int getMultiSocialTarget() {
		return _multiSocialTarget;
	}
	
	public Collection<TeleportBookmark> getTeleportBookmarks() {
		return _tpbookmarks.values();
	}
	
	public int getBookmarkslot() {
		return _bookmarkslot;
	}
	
	public int getQuestInventoryLimit() {
		return character().getMaximumSlotsForQuestItems();
	}
	
	public boolean canAttackCharacter(L2Character cha) {
		if (cha instanceof L2Attackable) {
			return true;
		} else if (cha instanceof L2Playable) {
			if (cha.isInsideZone(ZoneId.PVP) && !cha.isInsideZone(ZoneId.SIEGE)) {
				return true;
			}
			
			L2PcInstance target;
			if (cha instanceof L2Summon) {
				target = ((L2Summon) cha).getOwner();
			} else {
				target = (L2PcInstance) cha;
			}
			
			if (isInDuel() && target.isInDuel() && (target.getDuelId() == getDuelId())) {
				return true;
			} else if (isInParty() && target.isInParty()) {
				if (getParty() == target.getParty()) {
					return false;
				}
				if (((getParty().getCommandChannel() != null) || (target.getParty().getCommandChannel() != null)) && (getParty().getCommandChannel() == target.getParty().getCommandChannel())) {
					return false;
				}
			} else if ((getClan() != null) && (target.getClan() != null)) {
				if (getClanId() == target.getClanId()) {
					return false;
				}
				if (((getAllyId() > 0) || (target.getAllyId() > 0)) && (getAllyId() == target.getAllyId())) {
					return false;
				}
				if (getClan().isAtWarWith(target.getClan().getId()) && target.getClan().isAtWarWith(getClan().getId())) {
					return true;
				}
			} else if ((getClan() == null) || (target.getClan() == null)) {
				if ((target.getPvpFlag() == 0) && (target.getKarma() == 0)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Test if player inventory is under 90% capaity
	 * @param includeQuestInv check also quest inventory
	 * @return
	 */
	public boolean isInventoryUnder90(boolean includeQuestInv) {
		return (getInventory().getSize(includeQuestInv) <= (getInventoryLimit() * 0.9));
	}
	
	public boolean havePetInvItems() {
		return _petItems;
	}
	
	public void setPetInvItems(boolean haveit) {
		_petItems = haveit;
	}
	
	public String getAdminConfirmCmd() {
		return _adminConfirmCmd;
	}
	
	public void setAdminConfirmCmd(String adminConfirmCmd) {
		_adminConfirmCmd = adminConfirmCmd;
	}
	
	public int getBlockCheckerArena() {
		return _handysBlockCheckerEventArena;
	}
	
	public void setBlockCheckerArena(byte arena) {
		_handysBlockCheckerEventArena = arena;
	}
	
	/**
	 * Update L2PcInstance Recommendations data.
	 */
	public void storeRecommendations() {
		long recoTaskEnd = 0;
		if (_recoBonusTask != null) {
			recoTaskEnd = Math.max(0, _recoBonusTask.getDelay(TimeUnit.MILLISECONDS));
		}
		
		DAOFactory.getInstance().getRecommendationBonusDAO().insert(this, recoTaskEnd);
	}
	
	public void checkRecoBonusTask() {
		final long taskTime = DAOFactory.getInstance().getRecommendationBonusDAO().load(this);
		if (taskTime > 0) {
			// Add 20 recos on first login
			if (taskTime == 3600000) {
				setRecomLeft(getRecomLeft() + 20);
			}
			
			// If player have some timeleft, start bonus task
			_recoBonusTask = ThreadPoolManager.getInstance().scheduleGeneral(new RecoBonusTaskEnd(this), taskTime);
		}
		
		// Create task to give new recommendations
		_recoGiveTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RecoGiveTask(this), 7200000, 3600000);
		
		// Store new data
		storeRecommendations();
	}
	
	public void stopRecoBonusTask() {
		if (_recoBonusTask != null) {
			_recoBonusTask.cancel(false);
			_recoBonusTask = null;
		}
	}
	
	public void stopRecoGiveTask() {
		if (_recoGiveTask != null) {
			_recoGiveTask.cancel(false);
			_recoGiveTask = null;
		}
	}
	
	public boolean isRecoTwoHoursGiven() {
		return _recoTwoHoursGiven;
	}
	
	public void setRecoTwoHoursGiven(boolean val) {
		_recoTwoHoursGiven = val;
	}
	
	public int getRecomBonusTime() {
		if (_recoBonusTask != null) {
			return (int) Math.max(0, _recoBonusTask.getDelay(TimeUnit.SECONDS));
		}
		
		return 0;
	}
	
	public int getRecomBonusType() {
		// Maintain = 1
		return 0;
	}
	
	public String getLastPetitionGmName() {
		return _lastPetitionGmName;
	}
	
	public void setLastPetitionGmName(String gmName) {
		_lastPetitionGmName = gmName;
	}
	
	public L2ContactList getContactList() {
		return _contactList;
	}
	
	public void setEventStatus() {
		eventStatus = new PlayerEventHolder(this);
	}
	
	public PlayerEventHolder getEventStatus() {
		return eventStatus;
	}
	
	public void setEventStatus(PlayerEventHolder pes) {
		eventStatus = pes;
	}
	
	public long getNotMoveUntil() {
		return _notMoveUntil;
	}
	
	public void updateNotMoveUntil() {
		_notMoveUntil = System.currentTimeMillis() + SECONDS.toMillis(character().getNpcTalkBlockingTime());
	}
	
	@Override
	public boolean isPlayer() {
		return true;
	}
	
	@Override
	public boolean isChargedShot(ShotType type) {
		final L2ItemInstance weapon = getActiveWeaponInstance();
		return (weapon != null) && weapon.isChargedShot(type);
	}
	
	@Override
	public void setChargedShot(ShotType type, boolean charged) {
		final L2ItemInstance weapon = getActiveWeaponInstance();
		if (weapon != null) {
			weapon.setChargedShot(type, charged);
		}
	}
	
	/**
	 * @param skillId the display skill Id
	 * @return the custom skill
	 */
	public final Skill getCustomSkill(int skillId) {
		return (_customSkills != null) ? _customSkills.get(skillId) : null;
	}
	
	/**
	 * Add a skill level to the custom skills map.
	 * @param skill the skill to add
	 */
	private final void addCustomSkill(Skill skill) {
		if ((skill != null) && (skill.getDisplayId() != skill.getId())) {
			if (_customSkills == null) {
				_customSkills = new ConcurrentHashMap<>();
			}
			_customSkills.put(skill.getDisplayId(), skill);
		}
	}
	
	/**
	 * Remove a skill level from the custom skill map.
	 * @param skill the skill to remove
	 */
	private final void removeCustomSkill(Skill skill) {
		if ((skill != null) && (_customSkills != null) && (skill.getDisplayId() != skill.getId())) {
			_customSkills.remove(skill.getDisplayId());
		}
	}
	
	/**
	 * @return {@code true} if current player can revive and shows 'To Village' button upon death, {@code false} otherwise.
	 */
	@Override
	public boolean canRevive() {
		for (IEventListener listener : _eventListeners) {
			if (listener.isOnEvent() && !listener.canRevive()) {
				return false;
			}
		}
		return _canRevive;
	}
	
	/**
	 * This method can prevent from displaying 'To Village' button upon death.
	 * @param val
	 */
	@Override
	public void setCanRevive(boolean val) {
		_canRevive = val;
	}
	
	/**
	 * @return {@code true} if player is on event, {@code false} otherwise.
	 */
	@Override
	public boolean isOnEvent() {
		for (IEventListener listener : _eventListeners) {
			if (listener.isOnEvent()) {
				return true;
			}
		}
		return super.isOnEvent();
	}
	
	public boolean isBlockedFromExit() {
		for (IEventListener listener : _eventListeners) {
			if (listener.isOnEvent() && listener.isBlockingExit()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isBlockedFromDeathPenalty() {
		for (IEventListener listener : _eventListeners) {
			if (listener.isOnEvent() && listener.isBlockingDeathPenalty()) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addOverrideCond(PcCondOverride... excs) {
		super.addOverrideCond(excs);
		getVariables().set(COND_OVERRIDE_KEY, Long.toString(_exceptions));
	}
	
	@Override
	public void removeOverridedCond(PcCondOverride... excs) {
		super.removeOverridedCond(excs);
		getVariables().set(COND_OVERRIDE_KEY, Long.toString(_exceptions));
	}
	
	/**
	 * @return {@code true} if {@link PlayerVariables} instance is attached to current player's scripts, {@code false} otherwise.
	 */
	public boolean hasVariables() {
		return getScript(PlayerVariables.class) != null;
	}
	
	/**
	 * @return {@link PlayerVariables} instance containing parameters regarding player.
	 */
	public PlayerVariables getVariables() {
		final PlayerVariables vars = getScript(PlayerVariables.class);
		return vars != null ? vars : addScript(new PlayerVariables(getObjectId()));
	}
	
	/**
	 * @return {@code true} if {@link AccountVariables} instance is attached to current player's scripts, {@code false} otherwise.
	 */
	public boolean hasAccountVariables() {
		return getScript(AccountVariables.class) != null;
	}
	
	/**
	 * @return {@link AccountVariables} instance containing parameters regarding player.
	 */
	public AccountVariables getAccountVariables() {
		final AccountVariables vars = getScript(AccountVariables.class);
		return vars != null ? vars : addScript(new AccountVariables(getAccountName()));
	}
	
	/**
	 * Adds a event listener.
	 * @param listener
	 */
	public void addEventListener(IEventListener listener) {
		_eventListeners.add(listener);
	}
	
	/**
	 * Removes event listener
	 * @param listener
	 */
	public void removeEventListener(IEventListener listener) {
		_eventListeners.remove(listener);
	}
	
	public void removeEventListener(Class<? extends IEventListener> clazz) {
		_eventListeners.removeIf(e -> e.getClass() == clazz);
	}
	
	public Collection<IEventListener> getEventListeners() {
		return _eventListeners;
	}
	
	@Override
	public int getId() {
		return getClassId().getId();
	}
	
	public boolean isPartyBanned() {
		return PunishmentManager.getInstance().hasPunishment(getObjectId(), PunishmentAffect.CHARACTER, PunishmentType.PARTY_BAN);
	}
	
	/**
	 * @param act
	 * @return {@code true} if action was added successfully, {@code false} otherwise.
	 */
	public boolean addAction(PlayerAction act) {
		if (!hasAction(act)) {
			_actionMask |= act.getMask();
			return true;
		}
		return false;
	}
	
	/**
	 * @param act
	 * @return {@code true} if action was removed successfully, {@code false} otherwise.
	 */
	public boolean removeAction(PlayerAction act) {
		if (hasAction(act)) {
			_actionMask &= ~act.getMask();
			return true;
		}
		return false;
	}
	
	/**
	 * @param act
	 * @return {@code true} if action is present, {@code false} otherwise.
	 */
	public boolean hasAction(PlayerAction act) {
		return (_actionMask & act.getMask()) == act.getMask();
	}
	
	/**
	 * Set true/false if character got Charm of Courage
	 * @param val true/false
	 */
	public void setCharmOfCourage(boolean val) {
		_hasCharmOfCourage = val;
		
	}
	
	/**
	 * @return {@code true} if effect is present, {@code false} otherwise.
	 */
	public boolean hasCharmOfCourage() {
		return _hasCharmOfCourage;
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player got war with the target, {@code false} otherwise.
	 */
	public boolean isAtWarWith(L2Character target) {
		if (target == null) {
			return false;
		}
		if ((_clan != null) && !isAcademyMember()) {
			if ((target.getClan() != null) && !target.isAcademyMember()) {
				return _clan.isAtWarWith(target.getClan());
			}
		}
		return false;
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player in same party with the target, {@code false} otherwise.
	 */
	public boolean isInPartyWith(L2Character target) {
		if (!isInParty() || !target.isInParty()) {
			return false;
		}
		return getParty().getLeaderObjectId() == target.getParty().getLeaderObjectId();
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player in same command channel with the target, {@code false} otherwise.
	 */
	public boolean isInCommandChannelWith(L2Character target) {
		if (!isInParty() || !target.isInParty()) {
			return false;
		}
		
		if (!getParty().isInCommandChannel() || !target.getParty().isInCommandChannel()) {
			return false;
		}
		return getParty().getCommandChannel().getLeaderObjectId() == target.getParty().getCommandChannel().getLeaderObjectId();
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player in same clan with the target, {@code false} otherwise.
	 */
	public boolean isInClanWith(L2Character target) {
		if ((getClanId() == 0) || (target.getClanId() == 0)) {
			return false;
		}
		return getClanId() == target.getClanId();
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player in same ally with the target, {@code false} otherwise.
	 */
	public boolean isInAllyWith(L2Character target) {
		if ((getAllyId() == 0) || (target.getAllyId() == 0)) {
			return false;
		}
		return getAllyId() == target.getAllyId();
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player at duel with the target, {@code false} otherwise.
	 */
	public boolean isInDuelWith(L2Character target) {
		if (!isInDuel() || !target.isInDuel()) {
			return false;
		}
		return getDuelId() == target.getDuelId();
	}
	
	/**
	 * @param target the target
	 * @return {@code true} if this player is on same siege side with the target, {@code false} otherwise.
	 */
	public boolean isOnSameSiegeSideWith(L2Character target) {
		return (getSiegeState() > 0) && isInsideZone(ZoneId.SIEGE) && (getSiegeState() == target.getSiegeState()) && (getSiegeSide() == target.getSiegeSide());
	}
	
	public void setServitorShare(Map<Stats, Double> map) {
		_servitorShare = map;
	}
	
	public final double getServitorShareBonus(Stats stat) {
		if (_servitorShare == null) {
			return 1.0d;
		}
		return _servitorShare.get(stat);
	}
	
	public boolean canSummonTarget(L2PcInstance target) {
		if (this == target) {
			return false;
		}
		
		if (target.isAlikeDead()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			sendPacket(sm);
			return false;
		}
		
		if (target.isInStoreMode()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			sendPacket(sm);
			return false;
		}
		
		if (target.isRooted() || target.isInCombat()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED);
			sm.addPcName(target);
			sendPacket(sm);
			return false;
		}
		
		if (target.isInOlympiadMode() || OlympiadManager.getInstance().isRegisteredInComp(target)) {
			sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD);
			return false;
		}
		
		if (target.isFestivalParticipant() || target.isFlyingMounted() || target.isCombatFlagEquipped() || !TvTEvent.onEscapeUse(target.getObjectId())) {
			sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (target.inObserverMode()) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_STATE_FORBIDS_SUMMONING);
			sm.addCharName(target);
			sendPacket(sm);
			return false;
		}
		
		if (target.isInsideZone(ZoneId.NO_SUMMON_FRIEND) || target.isInsideZone(ZoneId.JAIL)) {
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IN_SUMMON_BLOCKING_AREA);
			sm.addString(target.getName());
			sendPacket(sm);
			return false;
		}
		
		if (isInsideZone(ZoneId.NO_SUMMON_FRIEND) || isInsideZone(ZoneId.JAIL) || isFlyingMounted()) {
			sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
			return false;
		}
		
		if (getInstanceId() > 0) {
			if (!general().allowSummonInInstance() || !InstanceManager.getInstance().getInstance(getInstanceId()).isSummonAllowed()) {
				sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION);
				return false;
			}
		}
		
		// TODO: on retail character can enter 7s dungeon with summon friend, but should be teleported away by mobs, because currently this is not working in L2J we do not allowing summoning.
		if (isIn7sDungeon()) {
			int targetCabal = SevenSigns.getInstance().getPlayerCabal(target.getObjectId());
			if (SevenSigns.getInstance().isSealValidationPeriod()) {
				if (targetCabal != SevenSigns.getInstance().getCabalHighestScore()) {
					sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
					return false;
				}
			} else if (targetCabal == SevenSigns.CABAL_NULL) {
				sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING);
				return false;
			}
		}
		return true;
	}
	
	public Map<Integer, TeleportBookmark> getTpbookmarks() {
		return _tpbookmarks;
	}
	
	public long getOnlineTime() {
		return _onlineTime;
	}
	
	public void setOnlineTime(long time) {
		_onlineTime = time;
		_onlineBeginTime = System.currentTimeMillis();
	}
	
	public void setHenna(L2Henna[] henna) {
		_henna = henna;
	}
	
	public long getOnlineBeginTime() {
		return _onlineBeginTime;
	}
	
	public int getControlItemId() {
		return _controlItemId;
	}
	
	public void setControlItemId(int controlItemId) {
		_controlItemId = controlItemId;
	}
	
	// TODO(Zoey76): Improve this.
	public void getDwarvenRecipeBookClear() {
		_dwarvenRecipeBook.clear();
	}
}
