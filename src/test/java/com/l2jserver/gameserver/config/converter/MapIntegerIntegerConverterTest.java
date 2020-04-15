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
package com.l2jserver.gameserver.config.converter;

import java.util.Map;

import org.aeonbits.owner.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Map Integer-Integer Converter test.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class MapIntegerIntegerConverterTest {
	
	private static final String PROVIDE_KEY_VALUES = "PROVIDE_KEY_VALUES";
	
	private static final Converter<Map<Integer, Integer>> CONVERTER = new MapIntegerIntegerConverter();
	
	@Test(dataProvider = PROVIDE_KEY_VALUES)
	public void convertTest(String keyValues, Map<Integer, Integer> expected) {
		Assert.assertEquals(CONVERTER.convert(null, keyValues), expected);
	}
	
	@DataProvider(name = PROVIDE_KEY_VALUES)
	public Object[][] provideKeyValues() {
		return new Object[][] {
			{
				"264,3600;265,3600;266,3600;267,3600",
				Map.of(264, 3600, 265, 3600, 266, 3600, 267, 3600)
			},
			{
				"264, 3600; 265, 3600; 266, 3600; 267, 3600",
				Map.of(264, 3600, 265, 3600, 266, 3600, 267, 3600)
			},
			{
				"",
				Map.of()
			},
			{
				null,
				Map.of()
			}
		};
	}
}
