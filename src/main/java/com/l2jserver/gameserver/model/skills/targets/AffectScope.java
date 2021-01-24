/*
 * Copyright © 2004-2021 L2J Server
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
package com.l2jserver.gameserver.model.skills.targets;

import static java.util.Comparator.comparingDouble;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Affect Scope.
 * @author Zoey76
 * @version 2.6.2.0
 */
public enum AffectScope {
	/** Affects Valakas. */
	BALAKAS_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects dead clan mates. */
	DEAD_PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			if (!caster.isPlayable()) {
				return List.of();
			}
			
			final var player = caster.getActingPlayer();
			final var clanId = player.getClanId();
			if (clanId == 0) {
				return List.of();
			}
			
			final var affectLimit = skill.getAffectLimit();
			final var affectObject = skill.getAffectObject();
			final var targets = new ArrayList<L2Object>(affectLimit);
			for (var object : L2World.getInstance().getVisibleObjects(target, skill.getAffectRange())) {
				if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
					break;
				}
				
				if (!object.isPlayable()) {
					continue;
				}
				
				final var targetPlayer = object.getActingPlayer();
				if (targetPlayer == null) {
					continue;
				}
				
				if (clanId != targetPlayer.getClanId()) {
					continue;
				}
				
				if (!affectObject.affectObject(caster, targetPlayer)) {
					continue;
				}
				
				targets.add(targetPlayer);
			}
			return targets;
		}
	},
	/** Affects fan area. */
	FAN {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects nothing. */
	NONE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			return List.of();
		}
	},
	/** Affects party members. */
	PARTY {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var affectRange = skill.getAffectRange();
			final var targets = new ArrayList<L2Object>(affectRange);
			if (caster.isInParty()) {
				for (var partyMember : caster.getParty().getMembers()) {
					if (Skill.addCharacter(caster, partyMember, affectRange, false)) {
						targets.add(partyMember);
					}
					
					if (Skill.addSummon(caster, partyMember, affectRange, false)) {
						targets.add(partyMember.getSummon());
					}
				}
			} else {
				final var player = caster.getActingPlayer();
				if (Skill.addCharacter(caster, player, affectRange, false)) {
					targets.add(player);
				}
				
				if (Skill.addSummon(caster, player, affectRange, false)) {
					targets.add(player.getSummon());
				}
			}
			return targets;
		}
	},
	/** Affects party and clan mates. */
	PARTY_PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var targets = new HashSet<L2Object>();
			targets.addAll(PARTY.affectTargets(caster, target, skill));
			targets.addAll(PLEDGE.affectTargets(caster, target, skill));
			return new LinkedList<>(targets);
		}
	},
	/** Affects clan mates. */
	PLEDGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var affectRange = skill.getAffectRange();
			final var affectLimit = skill.getAffectLimit();
			final var targets = new ArrayList<L2Object>(affectLimit);
			if (caster.isPlayer()) {
				final var clan = caster.getClan();
				if (clan != null) {
					for (var clanMember : clan.getMembers()) {
						if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
							break;
						}
						
						final var clanMemberPlayer = clanMember.getPlayerInstance();
						if (clanMemberPlayer == null) {
							continue;
						}
						
						// TODO(Zoey76): Handle Duel.
						// TODO(Zoey76): Handle PVP.
						// TODO(Zoey76): Handle TVT.
						
						if (Skill.addCharacter(caster, clanMemberPlayer, affectRange, false)) {
							targets.add(clanMemberPlayer);
						}
						
						if (Skill.addSummon(caster, clanMemberPlayer, affectRange, false)) {
							targets.add(clanMemberPlayer.getSummon());
						}
					}
				} else {
					final var player = caster.getActingPlayer();
					if (Skill.addCharacter(caster, player, affectRange, false)) {
						targets.add(player);
					}
					
					if (Skill.addSummon(caster, player, affectRange, false)) {
						targets.add(player.getSummon());
					}
				}
			} else if (caster.isNpc()) {
				final L2Npc npc = (L2Npc) caster;
				targets.add(caster);
				
				final var clans = npc.getTemplate().getClans();
				if ((clans == null) || clans.isEmpty()) {
					return targets;
				}
				
				for (var creature : npc.getKnownList().getKnownCharactersInRadius(affectRange)) {
					if ((affectLimit > 0) && (targets.size() >= affectLimit)) {
						break;
					}
					
					if (!creature.isNpc()) {
						continue;
					}
					
					if (!npc.isInMyClan((L2Npc) creature)) {
						continue;
					}
					
					targets.add(creature);
				}
			}
			return targets;
		}
	},
	/** Affects point blank targets, using caster as point of origin. */
	POINT_BLANK {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var affectLimit = skill.getAffectLimit();
			final var affectObject = skill.getAffectObject();
			return caster.getKnownList().getKnownCharactersInRadius(skill.getAffectRange()) //
				.stream() //
				.filter(c -> affectObject.affectObject(caster, c)) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets, using selected target as point of origin. */
	RANGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var affectLimit = skill.getAffectLimit();
			return L2World.getInstance().getVisibleObjects(target, skill.getAffectRange()) //
				.stream() //
				.filter(L2Object::isCharacter) //
				.map(o -> (L2Character) o) //
				.filter(c -> !c.isDead()) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets sorted by HP, using selected target as point of origin. */
	RANGE_SORT_BY_HP {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			final var affectLimit = skill.getAffectLimit();
			return L2World.getInstance().getVisibleObjects(caster, target, skill.getAffectRange()) //
				.stream() //
				.filter(L2Object::isCharacter) //
				.map(o -> (L2Character) o) //
				.filter(c -> !c.isDead()) //
				.sorted(comparingDouble(c -> c.getCurrentHp() / c.getMaxHp())) //
				.limit(affectLimit > 0 ? affectLimit : Integer.MAX_VALUE) //
				.collect(Collectors.toList());
		}
	},
	/** Affects ranged targets, using selected target as point of origin. */
	RING_RANGE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects a single target. */
	SINGLE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			if (!skill.getAffectObject().affectObject(caster, target)) {
				return List.of();
			}
			return List.of(target);
		}
	},
	/** Affects targets inside an square area, using selected target as point of origin. */
	SQUARE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects targets inside an square area, using caster as point of origin. */
	SQUARE_PB {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects static object targets. */
	STATIC_OBJECT_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	},
	/** Affects wyvern. */
	WYVERN_SCOPE {
		@Override
		public List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill) {
			// TODO(Zoey76): Implement.
			return List.of();
		}
	};
	
	public abstract List<L2Object> affectTargets(L2Character caster, L2Character target, Skill skill);
}
