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
package com.l2jserver.gameserver.model.clientstrings;

/**
 * @author Forsaiken
 */
final class BuilderContainer extends Builder {
	private final Builder[] _builders;
	
	BuilderContainer(final Builder[] builders) {
		_builders = builders;
	}
	
	@Override
	public String toString(final Object param) {
		return toString(new Object[] {
			param
		});
	}
	
	@Override
	public final String toString(final Object... params) {
		final int buildersLength = _builders.length;
		final int paramsLength = params.length;
		final String[] builds = new String[buildersLength];
		
		int buildTextLen = 0;
		if (paramsLength != 0) {
			for (int i = buildersLength; i-- > 0;) {
				var builder = _builders[i];
				var paramIndex = builder.getIndex();
				var build = (paramIndex != -1) && (paramIndex < paramsLength) ? builder.toString(params[paramIndex]) : builder.toString();
				buildTextLen += build.length();
				builds[i] = build;
			}
		} else {
			for (int i = buildersLength; i-- > 0;) {
				var build = _builders[i].toString();
				buildTextLen += build.length();
				builds[i] = build;
			}
		}
		
		final var sb = new StringBuilder(buildTextLen);
		for (int i = 0; i < buildersLength; i++) {
			sb.append(builds[i]);
		}
		return sb.toString();
	}
	
	@Override
	public int getIndex() {
		return -1;
	}
}