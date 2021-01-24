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
package com.l2jserver.gameserver.model.stats;

import static com.l2jserver.gameserver.config.Configuration.server;
import static com.l2jserver.gameserver.enums.ShotType.BLESSED_SPIRITSHOTS;
import static com.l2jserver.gameserver.enums.ShotType.SPIRITSHOTS;
import static java.lang.Double.NaN;
import static java.lang.Double.POSITIVE_INFINITY;
import static org.easymock.EasyMock.expect;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.powermock.api.easymock.annotation.Mock;
import org.powermock.api.easymock.annotation.MockNice;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.test.AbstractTest;

/**
 * Formulas test.
 * @author Zoey76
 * @version 2.6.2.0
 */
public class FormulasTest extends AbstractTest {
	
	private static final String PROVIDE_SPEED_SKILL_TIME = "PROVIDE_SPEED_SKILL_TIME";
	
	private static final int HP_REGENERATE_PERIOD_CHARACTER = 3000;
	
	private static final int HP_REGENERATE_PERIOD_DOOR = 300000;
	
	@Mock
	private L2Character character;
	@MockNice
	private Skill skill;
	
	@BeforeClass
	private void init() {
		server().setProperty("DatapackRoot", "src/test/resources");
	}
	
	@Test
	public void test_get_regenerate_period() {
		expect(character.isDoor()).andReturn(false);
		replayAll();
		
		assertEquals(Formulas.getRegeneratePeriod(character), HP_REGENERATE_PERIOD_CHARACTER);
	}
	
	@Test
	public void test_get_regenerate_period_door() {
		expect(character.isDoor()).andReturn(true);
		replayAll();
		
		assertEquals(Formulas.getRegeneratePeriod(character), HP_REGENERATE_PERIOD_DOOR);
	}
	
	@Test(dataProvider = PROVIDE_SPEED_SKILL_TIME)
	public void test_calculate_cast_time(int hitTime, boolean isChanneling, int channelingSkillId, boolean isStatic, boolean isMagic, //
		int mAtkSpeed, double pAtkSpeed, boolean isChargedSpiritshots, boolean isChargedBlessedSpiritShots, double expected) {
		expect(character.getMAtkSpd()).andReturn(mAtkSpeed);
		expect(character.getPAtkSpd()).andReturn(pAtkSpeed);
		expect(character.isChargedShot(SPIRITSHOTS)).andReturn(isChargedSpiritshots);
		expect(character.isChargedShot(BLESSED_SPIRITSHOTS)).andReturn(isChargedBlessedSpiritShots);
		expect(skill.getHitTime()).andReturn(hitTime);
		expect(skill.isChanneling()).andReturn(isChanneling);
		expect(skill.getChannelingSkillId()).andReturn(channelingSkillId);
		expect(skill.isStatic()).andReturn(isStatic);
		expect(skill.isMagic()).andReturn(isMagic);
		replayAll();
		
		assertEquals(Formulas.calcCastTime(character, skill), expected);
	}
	
	@DataProvider(name = PROVIDE_SPEED_SKILL_TIME)
	private Iterator<Object[]> provide() {
		final Set<Object[]> result = new HashSet<>();
		// @formatter:off
		// TODO(Zoey76): Take care of the "bad" values.
		result.add(new Object[]{ 0, true, 1, false, false, 0, 0.0, false, false, 0.0 });
		result.add(new Object[]{ 0, true, 0, false, false, 0, 0.0, false, false, NaN });
		result.add(new Object[]{ 0, false, 1, false, false, 0, 0.0, false, false, NaN });
		result.add(new Object[]{ 0, false, 0, false, true, 500, 0.0, false, false, 0.0 });
		result.add(new Object[]{ 600, false, 0, false, true, 500, 0.0, false, false, 399.59999999999997 });
		result.add(new Object[]{ 3000, false, 0, false, true, 600, 0.0, false, false, 1665.0 });
		result.add(new Object[]{ 0, false, 0, false, false, 0, 500.0, false, false, 0.0 });
		result.add(new Object[]{ 600, false, 0, false, false, 0, 500.0, false, false, 399.59999999999997 });
		result.add(new Object[]{ 3000, false, 0, false, false, 0, 600.0, false, false, 1665.0 });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, true, false, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, false, true, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, true, false, 840.0 });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, false, true, 840.0 });
		// @formatter:on
		return result.iterator();
	}
}
