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

import static com.l2jserver.gameserver.model.skills.targets.AffectObject.*;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.powermock.api.easymock.annotation.Mock;
import org.testng.annotations.Test;

import com.l2jserver.gameserver.model.L2Object;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.test.AbstractTest;

/**
 * Affect Object test.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class AffectObjectTest extends AbstractTest {
	
	@Mock
	private L2Character caster;
	@Mock
	private L2Object object;
	@Mock
	private L2Character creature;
	@Mock
	private L2Npc npc;
	
	@Test
	public void test_affect_object_all() {
		assertTrue(ALL.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_clan_player_not_in_clan() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getClanId()).andReturn(0);
		
		replayAll();
		
		assertFalse(CLAN.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_clan_player_in_clan_object_not_playable() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getClanId()).andReturn(1);
		expect(object.isPlayable()).andReturn(false);
		
		replayAll();
		
		assertFalse(CLAN.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_clan_player_in_clan_object_in_other_clan() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getClanId()).andReturn(1);
		expect(creature.isPlayable()).andReturn(true);
		expect(creature.getClanId()).andReturn(2);
		
		replayAll();
		
		assertFalse(CLAN.affectObject(caster, creature));
	}
	
	@Test
	public void test_affect_object_clan_player_in_clan_with_object() {
		expect(caster.isPlayable()).andReturn(true);
		expect(caster.getClanId()).andReturn(1);
		expect(creature.isPlayable()).andReturn(true);
		expect(creature.getClanId()).andReturn(1);
		
		replayAll();
		
		assertTrue(CLAN.affectObject(caster, creature));
	}
	
	@Test
	public void test_affect_object_friend_target_is_autoattackable() {
		expect(object.isAutoAttackable(caster)).andReturn(true);
		
		replayAll();
		
		assertFalse(FRIEND.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_friend_target_is_not_autoattackable() {
		expect(object.isAutoAttackable(caster)).andReturn(false);
		
		replayAll();
		
		assertTrue(FRIEND.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_hidden_place() {
		// TODO(Zoey76): Implement.
		assertFalse(HIDDEN_PLACE.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_invisible_visible_object() {
		expect(object.isInvisible()).andReturn(false);
		
		replayAll();
		
		assertFalse(INVISIBLE.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_invisible_invisible_object() {
		expect(object.isInvisible()).andReturn(true);
		
		replayAll();
		
		assertTrue(INVISIBLE.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_none() {
		assertFalse(NONE.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_not_friend_target_is_not_autoattackable() {
		expect(object.isAutoAttackable(caster)).andReturn(false);
		
		replayAll();
		
		assertFalse(NOT_FRIEND.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_not_friend_target_is_autoattackable() {
		expect(object.isAutoAttackable(caster)).andReturn(true);
		
		replayAll();
		
		assertTrue(NOT_FRIEND.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_object_dead_npc_body_not_npc() {
		expect(object.isNpc()).andReturn(false);
		
		replayAll();
		
		assertFalse(OBJECT_DEAD_NPC_BODY.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_object_dead_npc_body_not_dead() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.isDead()).andReturn(false);
		
		replayAll();
		
		assertFalse(OBJECT_DEAD_NPC_BODY.affectObject(caster, npc));
	}
	
	@Test
	public void test_affect_object_object_dead_npc_body_dead() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.isDead()).andReturn(true);
		
		replayAll();
		
		assertTrue(OBJECT_DEAD_NPC_BODY.affectObject(caster, npc));
	}
	
	@Test
	public void test_affect_object_undead_real_enemy_not_npc() {
		expect(object.isNpc()).andReturn(false);
		
		replayAll();
		
		assertFalse(UNDEAD_REAL_ENEMY.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_undead_real_enemy_not_undead() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.isUndead()).andReturn(false);
		
		replayAll();
		
		assertFalse(UNDEAD_REAL_ENEMY.affectObject(caster, npc));
	}
	
	@Test
	public void test_affect_object_undead_real_enemy_undead() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.isUndead()).andReturn(true);
		
		replayAll();
		
		assertTrue(UNDEAD_REAL_ENEMY.affectObject(caster, npc));
	}
	
	@Test
	public void test_affect_object_wyvern_object_not_npc() {
		expect(object.isNpc()).andReturn(false);
		
		replayAll();
		
		assertFalse(WYVERN_OBJECT.affectObject(caster, object));
	}
	
	@Test
	public void test_affect_object_wyvern_object_not_wyvern() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.getId()).andReturn(1);
		
		replayAll();
		
		assertFalse(WYVERN_OBJECT.affectObject(caster, npc));
	}
	
	@Test
	public void test_affect_wyvern_object_wyvern() {
		expect(npc.isNpc()).andReturn(true);
		expect(npc.getId()).andReturn(12621);
		
		replayAll();
		
		assertTrue(WYVERN_OBJECT.affectObject(caster, npc));
	}
}
