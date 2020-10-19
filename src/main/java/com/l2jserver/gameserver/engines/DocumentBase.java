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
package com.l2jserver.gameserver.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.enums.CategoryType;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.enums.Race;
import com.l2jserver.gameserver.model.StatsSet;
import com.l2jserver.gameserver.model.base.PlayerState;
import com.l2jserver.gameserver.model.conditions.Condition;
import com.l2jserver.gameserver.model.conditions.ConditionCategoryType;
import com.l2jserver.gameserver.model.conditions.ConditionChangeWeapon;
import com.l2jserver.gameserver.model.conditions.ConditionGameChance;
import com.l2jserver.gameserver.model.conditions.ConditionGameTime;
import com.l2jserver.gameserver.model.conditions.ConditionGameTime.CheckGameTime;
import com.l2jserver.gameserver.model.conditions.ConditionLogicAnd;
import com.l2jserver.gameserver.model.conditions.ConditionLogicNot;
import com.l2jserver.gameserver.model.conditions.ConditionLogicOr;
import com.l2jserver.gameserver.model.conditions.ConditionMinDistance;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerActiveEffectId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerActiveSkillId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerAgathionEnergy;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerAgathionId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCallPc;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanCreateBase;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanCreateOutpost;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanEscape;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanRefuelAirship;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanResurrect;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanSummon;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanSummonSiegeGolem;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanSweep;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanTakeCastle;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanTakeFort;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanTransform;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCanUntransform;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCharges;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCheckAbnormal;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerClassIdRestriction;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCloakStatus;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerCp;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerFlyMounted;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerGrade;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasAgathion;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasCastle;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasClanHall;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasFort;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasPet;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHasServitor;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerHp;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerInsideZoneId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerInstanceId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerInvSize;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerIsClanLeader;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerIsHero;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerLandingZone;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerLevel;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerLevelRange;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerMp;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerPkCount;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerPledgeClass;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerRace;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerRangeFromNpc;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerSex;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerSiegeSide;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerSouls;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerState;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerSubclass;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerTransformationId;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerTvTEvent;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerVehicleMounted;
import com.l2jserver.gameserver.model.conditions.ConditionPlayerWeight;
import com.l2jserver.gameserver.model.conditions.ConditionSiegeZone;
import com.l2jserver.gameserver.model.conditions.ConditionSlotItemId;
import com.l2jserver.gameserver.model.conditions.ConditionTargetAbnormal;
import com.l2jserver.gameserver.model.conditions.ConditionTargetActiveEffectId;
import com.l2jserver.gameserver.model.conditions.ConditionTargetActiveSkillId;
import com.l2jserver.gameserver.model.conditions.ConditionTargetAggro;
import com.l2jserver.gameserver.model.conditions.ConditionTargetClassIdRestriction;
import com.l2jserver.gameserver.model.conditions.ConditionTargetInvSize;
import com.l2jserver.gameserver.model.conditions.ConditionTargetLevel;
import com.l2jserver.gameserver.model.conditions.ConditionTargetLevelRange;
import com.l2jserver.gameserver.model.conditions.ConditionTargetMyPartyExceptMe;
import com.l2jserver.gameserver.model.conditions.ConditionTargetNpcId;
import com.l2jserver.gameserver.model.conditions.ConditionTargetNpcType;
import com.l2jserver.gameserver.model.conditions.ConditionTargetPlayable;
import com.l2jserver.gameserver.model.conditions.ConditionTargetRace;
import com.l2jserver.gameserver.model.conditions.ConditionTargetUsesWeaponKind;
import com.l2jserver.gameserver.model.conditions.ConditionTargetWeight;
import com.l2jserver.gameserver.model.conditions.ConditionUsingItemType;
import com.l2jserver.gameserver.model.conditions.ConditionUsingSkill;
import com.l2jserver.gameserver.model.conditions.ConditionUsingSlotType;
import com.l2jserver.gameserver.model.conditions.ConditionWithSkill;
import com.l2jserver.gameserver.model.effects.AbstractEffect;
import com.l2jserver.gameserver.model.interfaces.IIdentifiable;
import com.l2jserver.gameserver.model.items.L2Item;
import com.l2jserver.gameserver.model.items.type.ArmorType;
import com.l2jserver.gameserver.model.items.type.WeaponType;
import com.l2jserver.gameserver.model.skills.AbnormalType;
import com.l2jserver.gameserver.model.skills.EffectScope;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.model.stats.Stats;
import com.l2jserver.gameserver.model.stats.functions.FuncTemplate;

/**
 * @author mkizub
 */
public abstract class DocumentBase {
	protected final Logger _log = Logger.getLogger(getClass().getName());
	
	private final File _file;
	protected final Map<String, String[]> _tables = new HashMap<>();
	
	protected DocumentBase(File pFile) {
		_file = pFile;
	}
	
	public Document parse() {
		Document doc = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(_file);
			parseDocument(doc);
		} catch (Exception e) {
			_log.log(Level.SEVERE, "Error loading file " + _file, e);
		}
		return doc;
	}
	
	protected abstract void parseDocument(Document doc);
	
	protected abstract StatsSet getStatsSet();
	
	protected abstract String getTableValue(String name);
	
	protected abstract String getTableValue(String name, int idx);
	
	protected void resetTable() {
		_tables.clear();
	}
	
	protected void setTable(String name, String[] table) {
		_tables.put(name, table);
	}
	
	protected void parseTemplate(Node n, Object template) {
		parseTemplate(n, template, null);
	}
	
	protected void parseTemplate(Node n, Object template, EffectScope effectScope) {
		Condition condition = null;
		n = n.getFirstChild();
		if (n == null) {
			return;
		}
		if ("cond".equalsIgnoreCase(n.getNodeName())) {
			condition = parseCondition(n.getFirstChild(), template);
			Node msg = n.getAttributes().getNamedItem("msg");
			Node msgId = n.getAttributes().getNamedItem("msgId");
			if ((condition != null) && (msg != null)) {
				condition.setMessage(msg.getNodeValue());
			} else if ((condition != null) && (msgId != null)) {
				condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
				Node addName = n.getAttributes().getNamedItem("addName");
				if ((addName != null) && (Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)) {
					condition.addName();
				}
			}
			n = n.getNextSibling();
		}
		for (; n != null; n = n.getNextSibling()) {
			final String name = n.getNodeName().toLowerCase();
			
			switch (name) {
				case "effect" -> {
					if (template instanceof AbstractEffect) {
						throw new RuntimeException("Nested effects");
					}
					attachEffect(n, template, condition, effectScope);
				}
				case "add", "sub", "mul", "div", "set", "share", "enchant", "enchanthp" -> attachFunc(n, template, name, condition);
			}
		}
	}
	
	protected void attachFunc(Node n, Object template, String functionName, Condition attachCond) {
		Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
		int order = -1;
		final Node orderNode = n.getAttributes().getNamedItem("order");
		if (orderNode != null) {
			order = Integer.parseInt(orderNode.getNodeValue());
		}
		
		String valueString = n.getAttributes().getNamedItem("val").getNodeValue();
		double value;
		if (valueString.charAt(0) == '#') {
			value = Double.parseDouble(getTableValue(valueString));
		} else {
			value = Double.parseDouble(valueString);
		}
		
		final Condition applyCond = parseCondition(n.getFirstChild(), template);
		final FuncTemplate ft = new FuncTemplate(attachCond, applyCond, functionName, order, stat, value);
		if (template instanceof L2Item) {
			((L2Item) template).attach(ft);
		} else if (template instanceof AbstractEffect) {
			((AbstractEffect) template).attach(ft);
		} else {
			throw new RuntimeException("Attaching stat to a non-effect template!!!");
		}
	}
	
	protected void attachEffect(Node n, Object template, Condition attachCond) {
		attachEffect(n, template, attachCond, null);
	}
	
	protected void attachEffect(Node n, Object template, Condition attachCond, EffectScope effectScope) {
		final NamedNodeMap attrs = n.getAttributes();
		final StatsSet set = new StatsSet();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node att = attrs.item(i);
			set.set(att.getNodeName(), getValue(att.getNodeValue(), template));
		}
		
		final StatsSet parameters = parseParameters(n.getFirstChild(), template);
		final Condition applyCond = parseCondition(n.getFirstChild(), template);
		
		if (template instanceof IIdentifiable) {
			set.set("id", ((IIdentifiable) template).getId());
		}
		
		final AbstractEffect effect = AbstractEffect.createEffect(attachCond, applyCond, set, parameters);
		parseTemplate(n, effect);
		if (template instanceof L2Item) {
			_log.severe("Item " + template + " with effects!!!");
		} else if (template instanceof Skill) {
			final Skill skill = (Skill) template;
			if (effectScope != null) {
				skill.addEffect(effectScope, effect);
			} else if (skill.isPassive()) {
				skill.addEffect(EffectScope.PASSIVE, effect);
			} else {
				skill.addEffect(EffectScope.GENERAL, effect);
			}
		}
	}
	
	/**
	 * Parse effect's parameters.
	 * @param n the node to start the parsing
	 * @param template the effect template
	 * @return the list of parameters if any, {@code null} otherwise
	 */
	private StatsSet parseParameters(Node n, Object template) {
		StatsSet parameters = null;
		while ((n != null)) {
			// Parse all parameters.
			if ((n.getNodeType() == Node.ELEMENT_NODE) && "param".equals(n.getNodeName())) {
				if (parameters == null) {
					parameters = new StatsSet();
				}
				NamedNodeMap params = n.getAttributes();
				for (int i = 0; i < params.getLength(); i++) {
					Node att = params.item(i);
					parameters.set(att.getNodeName(), getValue(att.getNodeValue(), template));
				}
			}
			n = n.getNextSibling();
		}
		return parameters == null ? StatsSet.EMPTY_STATSET : parameters;
	}
	
	protected Condition parseCondition(Node n, Object template) {
		while ((n != null) && (n.getNodeType() != Node.ELEMENT_NODE)) {
			n = n.getNextSibling();
		}
		
		Condition condition = null;
		if (n != null) {
			switch (n.getNodeName().toLowerCase()) {
				case "and" -> condition = parseLogicAnd(n, template);
				case "or" -> condition = parseLogicOr(n, template);
				case "not" -> condition = parseLogicNot(n, template);
				case "player" -> condition = parsePlayerCondition(n, template);
				case "target" -> condition = parseTargetCondition(n, template);
				case "using" -> condition = parseUsingCondition(n);
				case "game" -> condition = parseGameCondition(n);
			}
		}
		return condition;
	}
	
	protected Condition parseLogicAnd(Node n, Object template) {
		ConditionLogicAnd cond = new ConditionLogicAnd();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				cond.add(parseCondition(n, template));
			}
		}
		if ((cond.conditions == null) || (cond.conditions.length == 0)) {
			_log.severe("Empty <and> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseLogicOr(Node n, Object template) {
		ConditionLogicOr cond = new ConditionLogicOr();
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				cond.add(parseCondition(n, template));
			}
		}
		if ((cond.conditions == null) || (cond.conditions.length == 0)) {
			_log.severe("Empty <or> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseLogicNot(Node n, Object template) {
		for (n = n.getFirstChild(); n != null; n = n.getNextSibling()) {
			if (n.getNodeType() == Node.ELEMENT_NODE) {
				return new ConditionLogicNot(parseCondition(n, template));
			}
		}
		_log.severe("Empty <not> condition in " + _file);
		return null;
	}
	
	protected Condition parsePlayerCondition(Node n, Object template) {
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node a = attrs.item(i);
			switch (a.getNodeName().toLowerCase()) {
				case "races" -> {
					final String[] racesVal = a.getNodeValue().split(",");
					final Race[] races = new Race[racesVal.length];
					for (int r = 0; r < racesVal.length; r++) {
						if (racesVal[r] != null) {
							races[r] = Race.valueOf(racesVal[r]);
						}
					}
					cond = joinAnd(cond, new ConditionPlayerRace(races));
				}
				case "level" -> {
					int lvl = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
				}
				case "levelrange" -> {
					String[] range = getValue(a.getNodeValue(), template).split(";");
					if (range.length == 2) {
						final int minimumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
						final int maximumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
						cond = joinAnd(cond, new ConditionPlayerLevelRange(minimumLevel, maximumLevel));
					}
				}
				case "resting" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
				}
				case "flying" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
				}
				case "moving" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
				}
				case "running" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
				}
				case "standing" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
				}
				case "behind" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
				}
				case "front" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
				}
				case "chaotic" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
				}
				case "olympiad" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
				}
				case "ishero" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerIsHero(val));
				}
				case "transformationid" -> {
					int id = Integer.parseInt(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerTransformationId(id));
				}
				case "hp" -> {
					int hp = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerHp(hp));
				}
				case "mp" -> {
					int hp = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerMp(hp));
				}
				case "cp" -> {
					int cp = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerCp(cp));
				}
				case "grade" -> {
					int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerGrade(expIndex));
				}
				case "pkcount" -> {
					int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
				}
				case "siegezone" -> {
					int value = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionSiegeZone(value, true));
				}
				case "siegeside" -> {
					int value = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerSiegeSide(value));
				}
				case "charges" -> {
					int value = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerCharges(value));
				}
				case "souls" -> {
					int value = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerSouls(value));
				}
				case "weight" -> {
					int weight = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerWeight(weight));
				}
				case "invsize" -> {
					int size = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerInvSize(size));
				}
				case "isclanleader" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerIsClanLeader(val));
				}
				case "ontvtevent" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerTvTEvent(val));
				}
				case "pledgeclass" -> {
					int pledgeClass = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
				}
				case "clanhall" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
				}
				case "fort" -> {
					int fort = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerHasFort(fort));
				}
				case "castle" -> {
					int castle = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
				}
				case "sex" -> {
					int sex = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionPlayerSex(sex));
				}
				case "flymounted" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerFlyMounted(val));
				}
				case "vehiclemounted" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerVehicleMounted(val));
				}
				case "landingzone" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerLandingZone(val));
				}
				case "active_effect_id" -> {
					int effectId = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effectId));
				}
				case "active_effect_id_lvl" -> {
					String val = getValue(a.getNodeValue(), template);
					int effect_id = Integer.decode(getValue(val.split(",")[0], template));
					int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
					cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
				}
				case "active_skill_id" -> {
					int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
				}
				case "active_skill_id_lvl" -> {
					String val = getValue(a.getNodeValue(), template);
					int skill_id = Integer.decode(getValue(val.split(",")[0], template));
					int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
					cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
				}
				case "class_id_restriction" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
				}
				case "subclass" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerSubclass(val));
				}
				case "instanceid" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionPlayerInstanceId(array));
				}
				case "agathionid" -> {
					int agathionId = Integer.decode(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
				}
				case "cloakstatus" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionPlayerCloakStatus(val));
				}
				case "haspet" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					ArrayList<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionPlayerHasPet(array));
				}
				case "hasservitor" -> cond = joinAnd(cond, new ConditionPlayerHasServitor());
				case "npcidradius" -> {
					final StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					if (st.countTokens() == 3) {
						final String[] ids = st.nextToken().split(";");
						final int[] npcIds = new int[ids.length];
						for (int index = 0; index < ids.length; index++) {
							npcIds[index] = Integer.parseInt(getValue(ids[index], template));
						}
						final int radius = Integer.parseInt(st.nextToken());
						final boolean val = Boolean.parseBoolean(st.nextToken());
						cond = joinAnd(cond, new ConditionPlayerRangeFromNpc(npcIds, radius, val));
					}
				}
				case "callpc" -> cond = joinAnd(cond, new ConditionPlayerCallPc(Boolean.parseBoolean(a.getNodeValue())));
				case "cancreatebase" -> cond = joinAnd(cond, new ConditionPlayerCanCreateBase(Boolean.parseBoolean(a.getNodeValue())));
				case "cancreateoutpost" -> cond = joinAnd(cond, new ConditionPlayerCanCreateOutpost(Boolean.parseBoolean(a.getNodeValue())));
				case "canescape" -> cond = joinAnd(cond, new ConditionPlayerCanEscape(Boolean.parseBoolean(a.getNodeValue())));
				case "canrefuelairship" -> cond = joinAnd(cond, new ConditionPlayerCanRefuelAirship(Integer.parseInt(a.getNodeValue())));
				case "canresurrect" -> cond = joinAnd(cond, new ConditionPlayerCanResurrect(Boolean.parseBoolean(a.getNodeValue())));
				case "cansummon" -> cond = joinAnd(cond, new ConditionPlayerCanSummon(Boolean.parseBoolean(a.getNodeValue())));
				case "cansummonsiegegolem" -> cond = joinAnd(cond, new ConditionPlayerCanSummonSiegeGolem(Boolean.parseBoolean(a.getNodeValue())));
				case "cansweep" -> cond = joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
				case "cantakecastle" -> cond = joinAnd(cond, new ConditionPlayerCanTakeCastle());
				case "cantakefort" -> cond = joinAnd(cond, new ConditionPlayerCanTakeFort(Boolean.parseBoolean(a.getNodeValue())));
				case "cantransform" -> cond = joinAnd(cond, new ConditionPlayerCanTransform(Boolean.parseBoolean(a.getNodeValue())));
				case "canuntransform" -> cond = joinAnd(cond, new ConditionPlayerCanUntransform(Boolean.parseBoolean(a.getNodeValue())));
				case "insidezoneid" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					List<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionPlayerInsideZoneId(array));
				}
				case "checkabnormal" -> {
					final String value = a.getNodeValue();
					if (value.contains(",")) {
						final String[] values = value.split(",");
						cond = joinAnd(cond, new ConditionPlayerCheckAbnormal(AbnormalType.valueOf(values[0]), Integer.decode(getValue(values[1], template))));
					} else {
						cond = joinAnd(cond, new ConditionPlayerCheckAbnormal(AbnormalType.valueOf(value)));
					}
				}
				case "categorytype" -> {
					final String[] values = a.getNodeValue().split(",");
					final Set<CategoryType> array = new HashSet<>(values.length);
					for (String value : values) {
						array.add(CategoryType.valueOf(getValue(value, null)));
					}
					cond = joinAnd(cond, new ConditionCategoryType(array));
				}
				case "hasagathion" -> cond = joinAnd(cond, new ConditionPlayerHasAgathion(Boolean.parseBoolean(a.getNodeValue())));
				case "agathionenergy" -> cond = joinAnd(cond, new ConditionPlayerAgathionEnergy(Integer.decode(getValue(a.getNodeValue(), null))));
				default -> _log.severe("Unrecognized <player> condition " + a.getNodeName().toLowerCase() + " in " + _file);
			}
		}
		
		if (cond == null) {
			_log.severe("Unrecognized <player> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseTargetCondition(Node n, Object template) {
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node a = attrs.item(i);
			switch (a.getNodeName().toLowerCase()) {
				case "aggro" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionTargetAggro(val));
				}
				case "siegezone" -> {
					int value = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionSiegeZone(value, false));
				}
				case "level" -> {
					int lvl = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionTargetLevel(lvl));
				}
				case "levelrange" -> {
					String[] range = getValue(a.getNodeValue(), template).split(";");
					if (range.length == 2) {
						int minimumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
						int maximumLevel = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
						cond = joinAnd(cond, new ConditionTargetLevelRange(minimumLevel, maximumLevel));
					}
				}
				case "mypartyexceptme" -> cond = joinAnd(cond, new ConditionTargetMyPartyExceptMe(Boolean.parseBoolean(a.getNodeValue())));
				case "playable" -> cond = joinAnd(cond, new ConditionTargetPlayable());
				case "class_id_restriction" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					List<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
				}
				case "active_effect_id" -> {
					int effectId = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionTargetActiveEffectId(effectId));
				}
				case "active_effect_id_lvl" -> {
					String val = getValue(a.getNodeValue(), template);
					int effect_id = Integer.decode(getValue(val.split(",")[0], template));
					int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
					cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
				}
				case "active_skill_id" -> {
					int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
				}
				case "active_skill_id_lvl" -> {
					String val = getValue(a.getNodeValue(), template);
					int skill_id = Integer.decode(getValue(val.split(",")[0], template));
					int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
					cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
				}
				case "abnormal" -> {
					int abnormalId = Integer.decode(getValue(a.getNodeValue(), template));
					cond = joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
				}
				case "mindistance" -> {
					int distance = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionMinDistance(distance * distance));
				}
				case "race" -> cond = joinAnd(cond, new ConditionTargetRace(Race.valueOf(a.getNodeValue())));
				case "using" -> {
					int mask = 0;
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						for (WeaponType wt : WeaponType.values()) {
							if (wt.name().equals(item)) {
								mask |= wt.mask();
								break;
							}
						}
						for (ArmorType at : ArmorType.values()) {
							if (at.name().equals(item)) {
								mask |= at.mask();
								break;
							}
						}
					}
					cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
				}
				case "npcid" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					List<Integer> array = new ArrayList<>(st.countTokens());
					while (st.hasMoreTokens()) {
						String item = st.nextToken().trim();
						array.add(Integer.decode(getValue(item, null)));
					}
					cond = joinAnd(cond, new ConditionTargetNpcId(array));
				}
				case "npctype" -> {
					String values = getValue(a.getNodeValue(), template).trim();
					String[] valuesSplit = values.split(",");
					InstanceType[] types = new InstanceType[valuesSplit.length];
					for (int j = 0; j < valuesSplit.length; j++) {
						types[j] = Enum.valueOf(InstanceType.class, valuesSplit[j]);
					}
					cond = joinAnd(cond, new ConditionTargetNpcType(types));
				}
				case "weight" -> {
					int weight = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionTargetWeight(weight));
				}
				case "invsize" -> {
					int size = Integer.decode(getValue(a.getNodeValue(), null));
					cond = joinAnd(cond, new ConditionTargetInvSize(size));
				}
			}
		}
		
		if (cond == null) {
			_log.severe("Unrecognized <target> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseUsingCondition(Node n) {
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node a = attrs.item(i);
			switch (a.getNodeName().toLowerCase()) {
				case "kind" -> {
					int mask = 0;
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					while (st.hasMoreTokens()) {
						int old = mask;
						String item = st.nextToken().trim();
						for (WeaponType wt : WeaponType.values()) {
							if (wt.name().equals(item)) {
								mask |= wt.mask();
							}
						}
						
						for (ArmorType at : ArmorType.values()) {
							if (at.name().equals(item)) {
								mask |= at.mask();
							}
						}
						
						if (old == mask) {
							_log.info("[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
						}
					}
					cond = joinAnd(cond, new ConditionUsingItemType(mask));
				}
				case "slot" -> {
					int mask = 0;
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
					while (st.hasMoreTokens()) {
						int old = mask;
						String item = st.nextToken().trim();
						if (ItemTable.SLOTS.containsKey(item)) {
							mask |= ItemTable.SLOTS.get(item);
						}
						
						if (old == mask) {
							_log.info("[parseUsingCondition=\"slot\"] Unknown item slot name: " + item);
						}
					}
					cond = joinAnd(cond, new ConditionUsingSlotType(mask));
				}
				case "skill" -> {
					int id = Integer.parseInt(a.getNodeValue());
					cond = joinAnd(cond, new ConditionUsingSkill(id));
				}
				case "slotitem" -> {
					StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
					int id = Integer.parseInt(st.nextToken().trim());
					int slot = Integer.parseInt(st.nextToken().trim());
					int enchant = 0;
					if (st.hasMoreTokens()) {
						enchant = Integer.parseInt(st.nextToken().trim());
					}
					cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
				}
				case "weaponchange" -> {
					boolean val = Boolean.parseBoolean(a.getNodeValue());
					cond = joinAnd(cond, new ConditionChangeWeapon(val));
				}
			}
		}
		
		if (cond == null) {
			_log.severe("Unrecognized <using> condition in " + _file);
		}
		return cond;
	}
	
	protected Condition parseGameCondition(Node n) {
		Condition cond = null;
		NamedNodeMap attrs = n.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node a = attrs.item(i);
			if ("skill".equalsIgnoreCase(a.getNodeName())) {
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionWithSkill(val));
			}
			if ("night".equalsIgnoreCase(a.getNodeName())) {
				boolean val = Boolean.parseBoolean(a.getNodeValue());
				cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
			}
			if ("chance".equalsIgnoreCase(a.getNodeName())) {
				int val = Integer.decode(getValue(a.getNodeValue(), null));
				cond = joinAnd(cond, new ConditionGameChance(val));
			}
		}
		if (cond == null) {
			_log.severe("Unrecognized <game> condition in " + _file);
		}
		return cond;
	}
	
	protected void parseTable(Node n) {
		NamedNodeMap attrs = n.getAttributes();
		String name = attrs.getNamedItem("name").getNodeValue();
		if (name.charAt(0) != '#') {
			throw new IllegalArgumentException("Table name must start with #");
		}
		StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
		List<String> array = new ArrayList<>(data.countTokens());
		while (data.hasMoreTokens()) {
			array.add(data.nextToken());
		}
		setTable(name, array.toArray(new String[array.size()]));
	}
	
	protected void parseBeanSet(Node n, StatsSet set, Integer level) {
		String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
		String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
		char ch = value.isEmpty() ? ' ' : value.charAt(0);
		if ((ch == '#') || (ch == '-') || Character.isDigit(ch)) {
			set.set(name, String.valueOf(getValue(value, level)));
		} else {
			set.set(name, value);
		}
	}
	
	protected void setExtractableSkillData(StatsSet set, String value) {
		set.set("capsuled_items_skill", value);
	}
	
	protected String getValue(String value, Object template) {
		// is it a table?
		if (value.charAt(0) == '#') {
			if (template instanceof Skill) {
				return getTableValue(value);
			} else if (template instanceof Integer) {
				return getTableValue(value, (Integer) template);
			} else {
				throw new IllegalStateException();
			}
		}
		return value;
	}
	
	protected Condition joinAnd(Condition cond, Condition c) {
		if (cond == null) {
			return c;
		}
		if (cond instanceof ConditionLogicAnd) {
			((ConditionLogicAnd) cond).add(c);
			return cond;
		}
		ConditionLogicAnd and = new ConditionLogicAnd();
		and.add(cond);
		and.add(c);
		return and;
	}
}
