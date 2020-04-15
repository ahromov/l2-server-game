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
package com.l2jserver.gameserver.config.converter;

import java.math.BigInteger;

import org.aeonbits.owner.Converter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Hex Id Converter test.
 * @author Zoey76
 * @version 2.6.1.0
 */
public class HexIdConverterTest {
	
	private static final String PROVIDE_HEX_ID = "PROVIDE_HEX_ID";
	
	private static final Converter<BigInteger> CONVERTER = new HexIdConverter();
	
	@Test(dataProvider = PROVIDE_HEX_ID)
	public void convertTest(String hexId, BigInteger expected) {
		Assert.assertEquals(CONVERTER.convert(null, hexId), expected);
	}
	
	@DataProvider(name = PROVIDE_HEX_ID)
	public Object[][] provideKeyValues() {
		return new Object[][] {
			{
				"-1eeb34fce0c64b610338d1269d8cfea4",
				new BigInteger("-1eeb34fce0c64b610338d1269d8cfea4", 16)
			}
		};
	}
}
