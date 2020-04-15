/*
 * Copyright © 2020 L2J Server
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.skills.Skill;

/**
 * Formulas test.
 * @author Zoey76
 */
public class FormulasTest {
	private static final String PROVIDE_SPEED_SKILL_TIME = "PROVIDE_SPEED_SKILL_TIME";
	
	private static final String PROVIDE_CHARACTERS = "PROVIDE_CHARACTERS";
	
	private static final Integer HP_REGENERATE_PERIOD_CHARACTER = 3000;
	
	private static final Integer HP_REGENERATE_PERIOD_DOOR = 300000;
	
	@BeforeClass
	private void init() {
		server().setProperty("DatapackRoot", "src/test/resources");
	}
	
	@Test(dataProvider = PROVIDE_CHARACTERS)
	public void testGetRegeneratePeriod(L2Character character, Integer expected) {
		assertEquals(Formulas.getRegeneratePeriod(character), expected.intValue());
	}
	
	@Test(dataProvider = PROVIDE_SPEED_SKILL_TIME)
	public void testCalcAtkSpd(int hitTime, boolean isChanneling, int channelingSkillId, boolean isStatic, boolean isMagic, //
		int mAtkSpeed, double pAtkSpeed, boolean isChargedSpiritshots, boolean isChargedBlessedSpiritShots, double expected) {
		final L2Character character = mock(L2Character.class);
		when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		when(character.getPAtkSpd()).thenReturn(pAtkSpeed);
		when(character.isChargedShot(SPIRITSHOTS)).thenReturn(isChargedSpiritshots);
		when(character.isChargedShot(BLESSED_SPIRITSHOTS)).thenReturn(isChargedBlessedSpiritShots);
		when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		when(character.getMAtkSpd()).thenReturn(mAtkSpeed);
		
		final Skill skill = mock(Skill.class);
		when(skill.getHitTime()).thenReturn(hitTime);
		when(skill.isChanneling()).thenReturn(isChanneling);
		when(skill.getChannelingSkillId()).thenReturn(channelingSkillId);
		when(skill.isStatic()).thenReturn(isStatic);
		when(skill.isMagic()).thenReturn(isMagic);
		assertEquals(Formulas.calcCastTime(character, skill), expected);
	}
	
	@DataProvider(name = PROVIDE_CHARACTERS)
	private Iterator<Object[]> provideCharacters() {
		final List<Object[]> result = new LinkedList<>();
		final L2Character c1 = mock(L2Character.class);
		when(c1.isDoor()).thenReturn(true);
		
		final L2Character c2 = mock(L2Character.class);
		when(c2.isDoor()).thenReturn(false);
		
		// @formatter:off
		result.add(new Object[]{ c1, HP_REGENERATE_PERIOD_DOOR });
		result.add(new Object[]{ c2, HP_REGENERATE_PERIOD_CHARACTER });
		// @formatter:on
		
		return result.iterator();
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
		result.add(new Object[]{ 600, false, 0, false, true, 500, 0.0, false, false, 500.0 });
		result.add(new Object[]{ 3000, false, 0, false, true, 600, 0.0, false, false, 1665.0 });
		result.add(new Object[]{ 0, false, 0, false, false, 0, 500.0, false, false, 0.0 });
		result.add(new Object[]{ 600, false, 0, false, false, 0, 500.0, false, false, 500.0 });
		result.add(new Object[]{ 3000, false, 0, false, false, 0, 600.0, false, false, 1665.0 });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, true, false, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, false, true, 0, 0.0, false, true, POSITIVE_INFINITY });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, true, false, 840.0 });
		result.add(new Object[]{ 1400, false, 0, true, true, 0, 0.0, false, true, 840.0 });
		// @formatter:on
		return result.iterator();
	}
}
