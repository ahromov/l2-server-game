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
package com.l2jserver.gameserver.util.file.filter;

import java.io.File;
import java.io.FileFilter;

/**
 * @author lasarus
 */
public class ExtFilter implements FileFilter {
	private final String _ext;
	
	public ExtFilter(String ext) {
		_ext = ext;
	}
	
	@Override
	public boolean accept(File f) {
		return f.getName().toLowerCase().endsWith(_ext);
	}
}
