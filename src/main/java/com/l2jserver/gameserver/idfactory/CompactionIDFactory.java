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
package com.l2jserver.gameserver.idfactory;

import static com.l2jserver.gameserver.config.Configuration.server;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.l2jserver.commons.database.ConnectionFactory;

/**
 * 18.8.2010 - JIV: Disabling until someone update it
 */
@Deprecated
public class CompactionIDFactory extends IdFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(CompactionIDFactory.class);
	
	private int _curOID;
	
	private final int _freeSize;
	
	protected CompactionIDFactory() {
		super();
		_curOID = FIRST_OID;
		_freeSize = 0;
		
		try (var con = ConnectionFactory.getInstance().getConnection()) {
			Integer[] tmp_obj_ids = extractUsedObjectIDTable();
			
			int N = tmp_obj_ids.length;
			for (int idx = 0; idx < N; idx++) {
				N = insertUntil(tmp_obj_ids, idx, N, con);
			}
			_curOID++;
			LOG.info("Next usable Object Id is {}.", _curOID);
			_initialized = true;
		} catch (Exception ex) {
			LOG.error("Could not initialize properly!", ex);
		}
	}
	
	private int insertUntil(Integer[] tmp_obj_ids, int idx, int N, Connection con) throws SQLException {
		int id = tmp_obj_ids[idx];
		if (id == _curOID) {
			_curOID++;
			return N;
		}
		// check these IDs not present in DB
		if (server().badIdChecking()) {
			for (String check : ID_CHECKS) {
				try (var ps = con.prepareStatement(check)) {
					ps.setInt(1, _curOID);
					ps.setInt(2, id);
					try (var rs = ps.executeQuery()) {
						while (rs.next()) {
							int badId = rs.getInt(1);
							LOG.error("Bad Id {} in DB found by {}!", badId, check);
							throw new RuntimeException();
						}
					}
				}
			}
		}
		
		int hole = id - _curOID;
		if (hole > (N - idx)) {
			hole = N - idx;
		}
		for (int i = 1; i <= hole; i++) {
			id = tmp_obj_ids[N - i];
			LOG.info("Compacting DB object Id={} into {}.", id, _curOID);
			for (String update : ID_UPDATES) {
				try (var ps = con.prepareStatement(update)) {
					ps.setInt(1, _curOID);
					ps.setInt(2, id);
					ps.execute();
				}
			}
			_curOID++;
		}
		if (hole < (N - idx)) {
			_curOID++;
		}
		return N - hole;
	}
	
	@Override
	public synchronized int getNextId() {
		// @formatter:off
		/*
		 * if (_freeSize == 0)
		 */
			return _curOID++;
		/*
		 * else
		 * 	return _freeOIDs[--_freeSize];
		 */
		// @formatter:on
	}
	
	@Override
	public synchronized void releaseId(int id) {
		// Don't release Ids until we are sure it isn't messing up
		// @formatter:off
		/*
		 if (_freeSize >= _freeOIDs.length)
		 {
		 	int[] tmp = new int[_freeSize + STACK_SIZE_INCREMENT];
		 	System.arraycopy(_freeOIDs, 0, tmp, 0, _freeSize);
		 	_freeOIDs = tmp;
		 }
		 _freeOIDs[_freeSize++] = id;
		 */
		// @formatter:on
	}
	
	@Override
	public int size() {
		return (_freeSize + LAST_OID) - FIRST_OID;
	}
}
