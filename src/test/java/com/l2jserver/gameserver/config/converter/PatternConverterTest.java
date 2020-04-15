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

import java.util.regex.Pattern;

import org.aeonbits.owner.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Pattern Converter test.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class PatternConverterTest {
	
	private static final String PROVIDE_PATTERNS = "PROVIDE_PATTERNS";
	
	private static final Converter<Pattern> CONVERTER = new PatternConverter();
	
	@Test(dataProvider = PROVIDE_PATTERNS)
	public void convertTest(String pattern, String text, boolean expected) {
		Assert.assertEquals(CONVERTER.convert(null, pattern).matcher(text).matches(), expected);
	}
	
	@DataProvider(name = PROVIDE_PATTERNS)
	public Object[][] providePatterns() {
		return new Object[][] {
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]*",
				"OmfgWTF1",
				true
			},
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]*",
				"",
				false
			},
			{
				"[A-Z][a-z]{3,3}[A-Za-z0-9]+",
				"",
				false
			},
			{
				"[a-zA-Z0-9]*",
				"Zoey76",
				true
			}
		};
	}
}
