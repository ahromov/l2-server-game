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

import static com.l2jserver.gameserver.model.skills.targets.AffectScope.BALAKAS_SCOPE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.DEAD_PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.FAN;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.NONE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PARTY;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PARTY_PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.PLEDGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.POINT_BLANK;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RANGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RANGE_SORT_BY_HP;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.RING_RANGE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SINGLE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SQUARE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.SQUARE_PB;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.STATIC_OBJECT_SCOPE;
import static com.l2jserver.gameserver.model.skills.targets.AffectScope.WYVERN_SCOPE;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.mockStatic;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.testng.Assert.assertEquals;

import java.util.List;
import java.util.Set;

import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.easymock.annotation.MockNice;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.Test;

import com.l2jserver.gameserver.model.L2Clan;
import com.l2jserver.gameserver.model.L2ClanMember;
import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.L2Party;
import com.l2jserver.gameserver.model.L2World;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.L2Summon;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.actor.instance.L2ServitorInstance;
import com.l2jserver.gameserver.model.actor.knownlist.CharKnownList;
import com.l2jserver.gameserver.model.actor.knownlist.NpcKnownList;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.test.AbstractTest;

/**
 * Affect Scope test.
 * @author Zoey76
 * @version 2.6.2.0
 */
@PrepareForTest({
	L2NpcTemplate.class,
	L2World.class
})
public class AffectScopeTest extends AbstractTest {
	
	private static final int AFFECT_LIMIT = 5;
	
	private static final int AFFECT_RANGE = 1000;
	
	@Mock
	private L2Character caster;
	@Mock
	private CharKnownList casterKnownList;
	@Mock
	private L2Character target;
	@MockNice
	private Skill skill;
	@Mock
	private L2PcInstance player;
	@Mock
	private L2Summon summon;
	@Mock
	private L2World world;
	@Mock
	private L2Object object1;
	@Mock
	private L2ServitorInstance object2;
	@Mock
	private L2PcInstance object3;
	@Mock
	private L2PcInstance object4;
	@Mock
	private L2PcInstance object5;
	@Mock
	private L2PcInstance object6;
	@Mock
	private L2PcInstance object7;
	@Mock
	private L2PcInstance object8;
	@Mock
	private L2PcInstance object9;
	@Mock
	private AffectObject affectObject;
	@Mock
	private L2Party party;
	@Mock
	private L2Clan clan;
	@Mock
	private L2ClanMember clanMember1;
	@Mock
	private L2ClanMember clanMember2;
	@Mock
	private L2ClanMember clanMember3;
	@Mock
	private L2Npc npc1;
	@Mock
	private L2Npc npc2;
	@Mock
	private L2Npc npc3;
	@Mock
	private L2NpcTemplate npcTemplate;
	@Mock
	private NpcKnownList npcKnownList;
	
	@Test
	public void test_balakas_scope() {
		assertEquals(BALAKAS_SCOPE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_dead_pledge_scope_caster_not_playable() {
		expect(caster.isPlayable()).andReturn(false);
		replayAll();
		
		assertEquals(DEAD_PLEDGE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_dead_pledge_scope_player_not_in_clan() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getActingPlayer()).andReturn(player);
		expect(player.getClanId()).andReturn(0);
		replayAll();
		
		assertEquals(DEAD_PLEDGE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_dead_pledge_scope_player() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getActingPlayer()).andReturn(player);
		expect(player.getClanId()).andReturn(1);
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(skill.getAffectObject()).andReturn(affectObject);
		mockStatic(L2World.class);
		expect(L2World.getInstance()).andReturn(world);
		expect(world.getVisibleObjects(target, AFFECT_RANGE)).andReturn(List.of(object1, object2, object3, object4, object5, object6, object7, object8, object9));
		expect(object1.isPlayable()).andReturn(false);
		expect(object2.isPlayable()).andReturn(true);
		expect(object2.getActingPlayer()).andReturn(null);
		expect(object3.isPlayable()).andReturn(true);
		expect(object3.getActingPlayer()).andReturn(object3);
		expect(object3.getClanId()).andReturn(0);
		expect(object4.isPlayable()).andReturn(true);
		expect(object4.getActingPlayer()).andReturn(object4);
		expect(object4.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object4)).andReturn(false);
		
		expect(object5.isPlayable()).andReturn(true);
		expect(object5.getActingPlayer()).andReturn(object5);
		expect(object5.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object5)).andReturn(true);
		
		expect(object6.isPlayable()).andReturn(true);
		expect(object6.getActingPlayer()).andReturn(object6);
		expect(object6.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object6)).andReturn(true);
		
		expect(object7.isPlayable()).andReturn(true);
		expect(object7.getActingPlayer()).andReturn(object7);
		expect(object7.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object7)).andReturn(true);
		
		expect(object8.isPlayable()).andReturn(true);
		expect(object8.getActingPlayer()).andReturn(object8);
		expect(object8.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object8)).andReturn(true);
		
		expect(object9.isPlayable()).andReturn(true);
		expect(object9.getActingPlayer()).andReturn(object9);
		expect(object9.getClanId()).andReturn(1);
		expect(affectObject.affectObject(caster, object9)).andReturn(true);
		replayAll();
		
		assertEquals(DEAD_PLEDGE.affectTargets(caster, target, skill), List.of(object5, object6, object7, object8, object9));
	}
	
	@Test
	public void test_fan_scope() {
		assertEquals(FAN.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_none_scope() {
		assertEquals(NONE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_party_scope_caster_is_summon_in_party() {
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(summon.isInParty()).andReturn(true);
		expect(summon.getParty()).andReturn(party);
		expect(party.getMembers()).andReturn(List.of(player, object3));
		
		mockStatic(Skill.class);
		expect(Skill.addCharacter(summon, player, AFFECT_RANGE, false)).andReturn(true);
		expect(Skill.addSummon(summon, player, AFFECT_RANGE, false)).andReturn(true);
		expect(player.getSummon()).andReturn(summon);
		
		expect(Skill.addCharacter(summon, object3, AFFECT_RANGE, false)).andReturn(true);
		expect(Skill.addSummon(summon, object3, AFFECT_RANGE, false)).andReturn(true);
		expect(object3.getSummon()).andReturn(object2);
		replayAll();
		
		assertEquals(PARTY.affectTargets(summon, target, skill), List.of(player, summon, object3, object2));
	}
	
	@Test
	public void test_party_scope_caster_is_summon_not_in_party() {
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(summon.isInParty()).andReturn(false);
		expect(summon.getActingPlayer()).andReturn(player);
		
		mockStatic(Skill.class);
		expect(Skill.addCharacter(summon, player, AFFECT_RANGE, false)).andReturn(true);
		expect(Skill.addSummon(summon, player, AFFECT_RANGE, false)).andReturn(true);
		expect(player.getSummon()).andReturn(summon);
		
		replayAll();
		
		assertEquals(PARTY.affectTargets(summon, target, skill), List.of(player, summon));
	}
	
	@Test(enabled = false)
	public void test_party_pledge_scope() {
		assertEquals(PARTY_PLEDGE.affectTargets(caster, target, skill), List.of(target, summon, object3));
	}
	
	@Test
	public void test_pledge_scope_caster_is_player_in_clan() {
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(caster.isPlayer()).andReturn(true);
		expect(caster.getClan()).andReturn(clan);
		expect(clan.getMembers()).andReturn(new L2ClanMember[] {
			clanMember1,
			clanMember2,
			clanMember3
		});
		
		mockStatic(Skill.class);
		expect(clanMember1.getPlayerInstance()).andReturn(player);
		expect(Skill.addCharacter(caster, player, AFFECT_RANGE, false)).andReturn(true);
		expect(Skill.addSummon(caster, player, AFFECT_RANGE, false)).andReturn(true);
		expect(player.getSummon()).andReturn(summon);
		
		expect(clanMember2.getPlayerInstance()).andReturn(object3);
		expect(Skill.addCharacter(caster, object3, AFFECT_RANGE, false)).andReturn(false);
		expect(Skill.addSummon(caster, object3, AFFECT_RANGE, false)).andReturn(false);
		
		expect(clanMember3.getPlayerInstance()).andReturn(null);
		
		replayAll();
		assertEquals(PLEDGE.affectTargets(caster, target, skill), List.of(player, summon));
	}
	
	@Test
	public void test_pledge_scope_caster_is_player_not_in_clan() {
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(caster.isPlayer()).andReturn(true);
		expect(caster.getClan()).andReturn(null);
		expect(caster.getActingPlayer()).andReturn(player);
		
		mockStatic(Skill.class);
		expect(Skill.addCharacter(caster, player, AFFECT_RANGE, false)).andReturn(true);
		expect(Skill.addSummon(caster, player, AFFECT_RANGE, false)).andReturn(true);
		expect(player.getSummon()).andReturn(summon);
		
		replayAll();
		assertEquals(PLEDGE.affectTargets(caster, target, skill), List.of(player, summon));
	}
	
	@Test
	public void test_pledge_scope_caster_is_npc_in_clan() {
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(npc1.isPlayer()).andReturn(false);
		expect(npc1.isNpc()).andReturn(true);
		expect(npc1.getTemplate()).andReturn(npcTemplate);
		expect(npcTemplate.getClans()).andReturn(Set.of(1, 2));
		expect(npc1.getKnownList()).andReturn(npcKnownList);
		expect(npcKnownList.getKnownCharactersInRadius(AFFECT_RANGE)).andReturn(List.of(target, npc2, npc3, summon));
		
		expect(target.isNpc()).andReturn(false);
		
		expect(npc2.isNpc()).andReturn(true);
		expect(npc1.isInMyClan(npc2)).andReturn(true);
		
		expect(npc3.isNpc()).andReturn(true);
		expect(npc1.isInMyClan(npc3)).andReturn(false);
		
		expect(summon.isNpc()).andReturn(false);
		
		replayAll();
		assertEquals(PLEDGE.affectTargets(npc1, target, skill), List.of(npc1, npc2));
	}
	
	@Test
	public void test_point_blank_scope() {
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		expect(caster.getKnownList()).andReturn(casterKnownList);
		expect(casterKnownList.getKnownCharactersInRadius(AFFECT_RANGE)).andReturn(List.of(target, npc2, npc3, summon));
		expect(skill.getAffectObject()).andReturn(affectObject);
		expect(affectObject.affectObject(caster, target)).andReturn(true);
		expect(affectObject.affectObject(caster, npc2)).andReturn(false);
		expect(affectObject.affectObject(caster, npc3)).andReturn(false);
		expect(affectObject.affectObject(caster, summon)).andReturn(true);
		
		replayAll();
		
		assertEquals(POINT_BLANK.affectTargets(caster, target, skill), List.of(target, summon));
	}
	
	@Test
	public void test_range_scope() {
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		
		mockStatic(L2World.class);
		expect(L2World.getInstance()).andReturn(world);
		expect(world.getVisibleObjects(target, AFFECT_RANGE)).andReturn(List.of(object1, object2, object3, object4, object5, object6, object7, object8, object9));
		
		expect(object1.isCharacter()).andReturn(false);
		expect(object2.isCharacter()).andReturn(true);
		expect(object2.isDead()).andReturn(true);
		
		expect(object3.isCharacter()).andReturn(true);
		expect(object3.isDead()).andReturn(false);
		
		expect(object4.isCharacter()).andReturn(true);
		expect(object4.isDead()).andReturn(false);
		
		expect(object5.isCharacter()).andReturn(true);
		expect(object5.isDead()).andReturn(false);
		
		expect(object6.isCharacter()).andReturn(true);
		expect(object6.isDead()).andReturn(false);
		
		expect(object7.isCharacter()).andReturn(true);
		expect(object7.isDead()).andReturn(false);
		
		expect(object8.isCharacter()).andReturn(true);
		expect(object8.isDead()).andReturn(false);
		
		replayAll();
		
		assertEquals(RANGE.affectTargets(caster, target, skill), List.of(object3, object4, object5, object6, object7));
	}
	
	@Test
	public void test_range_sort_by_hp_scope() {
		expect(skill.getAffectLimit()).andReturn(AFFECT_LIMIT);
		expect(skill.getAffectRange()).andReturn(AFFECT_RANGE);
		mockStatic(L2World.class);
		expect(L2World.getInstance()).andReturn(world);
		expect(world.getVisibleObjects(caster, target, AFFECT_RANGE)).andReturn(List.of(target, object1, object2, object3, object4));
		
		expect(object1.isCharacter()).andReturn(false);
		
		expect(object2.isCharacter()).andReturn(true);
		expect(object2.isDead()).andReturn(true);
		
		expect(object3.isCharacter()).andReturn(true);
		expect(object3.isDead()).andReturn(false);
		expect(object3.getCurrentHp()).andReturn(1000.0).times(3);
		expect(object3.getMaxHp()).andReturn(1000).times(3);
		
		expect(object4.isCharacter()).andReturn(true);
		expect(object4.isDead()).andReturn(false);
		expect(object4.getCurrentHp()).andReturn(1900.0).times(3);
		expect(object4.getMaxHp()).andReturn(2000).times(3);
		
		expect(target.isCharacter()).andReturn(true);
		expect(target.isDead()).andReturn(false);
		expect(target.getCurrentHp()).andReturn(500.0).times(3);
		expect(target.getMaxHp()).andReturn(1000).times(3);
		
		replayAll();
		
		assertEquals(RANGE_SORT_BY_HP.affectTargets(caster, target, skill), List.of(target, object4, object3));
	}
	
	@Test
	public void test_ring_range_scope() {
		assertEquals(RING_RANGE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_single_scope_object_no_affected() {
		expect(skill.getAffectObject()).andReturn(affectObject);
		expect(affectObject.affectObject(caster, target)).andReturn(false);
		replayAll();
		
		assertEquals(SINGLE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_single_scope() {
		expect(skill.getAffectObject()).andReturn(affectObject);
		expect(affectObject.affectObject(caster, target)).andReturn(true);
		replayAll();
		
		assertEquals(SINGLE.affectTargets(caster, target, skill), List.of(target));
	}
	
	@Test
	public void test_square_scope() {
		assertEquals(SQUARE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_square_pb_scope() {
		assertEquals(SQUARE_PB.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_static_object_scope() {
		assertEquals(STATIC_OBJECT_SCOPE.affectTargets(caster, target, skill), List.of());
	}
	
	@Test
	public void test_wyvern_scope() {
		assertEquals(WYVERN_SCOPE.affectTargets(caster, target, skill), List.of());
	}
}
