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
package com.l2jserver.gameserver.model.conditions;

import static com.l2jserver.gameserver.network.SystemMessageId.CANNOT_USE_ON_YOURSELF;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.easymock.annotation.MockNice;
import org.testng.annotations.Test;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.test.AbstractTest;

/**
 * Condition Target My Party test.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class ConditionTargetMyPartyTest extends AbstractTest {
	
	@MockNice
	private Skill skill;
	@Mock
	private L2Character effector;
	@Mock
	private L2Character effected;
	@Mock
	private L2PcInstance player;
	@Mock
	private L2PcInstance otherPlayer;
	
	private final ConditionTargetMyParty conditionIncludeMe = new ConditionTargetMyParty("INCLUDE_ME");
	
	private final ConditionTargetMyParty conditionExceptMe = new ConditionTargetMyParty("EXCEPT_ME");
	
	@Test
	public void test_null_player() {
		assertFalse(conditionIncludeMe.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_self_target_exclude_me() {
		expect(effector.getActingPlayer()).andReturn(player);
		effector.sendPacket(CANNOT_USE_ON_YOURSELF);
		expectLastCall().once();
		replayAll();
		
		assertFalse(conditionExceptMe.testImpl(effector, player, skill, null));
	}
	
	@Test
	public void test_player_in_party_target_not_in_party() {
		expect(effector.getActingPlayer()).andReturn(player);
		expect(player.isInParty()).andReturn(true);
		expect(player.isInPartyWith(effected)).andReturn(false);
		effector.sendPacket(anyObject(SystemMessage.class));
		expectLastCall().once();
		replayAll();
		
		assertFalse(conditionIncludeMe.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_in_party_with_target() {
		expect(effector.getActingPlayer()).andReturn(player);
		expect(player.isInParty()).andReturn(true);
		expect(player.isInPartyWith(effected)).andReturn(true);
		replayAll();
		
		assertTrue(conditionIncludeMe.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_not_in_party_target_not_player_or_player_summon() {
		expect(effector.getActingPlayer()).andReturn(player);
		expect(player.isInParty()).andReturn(false);
		expect(effected.getActingPlayer()).andReturn(otherPlayer);
		effector.sendPacket(anyObject(SystemMessage.class));
		expectLastCall().once();
		replayAll();
		
		assertFalse(conditionIncludeMe.testImpl(effector, effected, skill, null));
	}
	
	@Test
	public void test_player_in_party_target_player_or_player_summon() {
		expect(effector.getActingPlayer()).andReturn(player);
		expect(player.isInParty()).andReturn(false);
		expect(effected.getActingPlayer()).andReturn(player);
		replayAll();
		
		assertTrue(conditionIncludeMe.testImpl(effector, effected, skill, null));
	}
}
