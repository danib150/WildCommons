/*
 * Copyright (c) 2020, Wild Adventure
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 4. Redistribution of this software in source or binary forms shall be free
 *    of all charges or fees to the recipient of this software.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gmail.filoghost.boosters.sql;

import java.sql.SQLException;

import com.gmail.filoghost.boosters.api.Booster;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import wild.api.mysql.SQLResult;

@AllArgsConstructor
@Getter
public class BoosterImpl implements Booster {
	
	private final String playerName;
	private final int id;
	private final String pluginID;
	private final int multiplier;
	private final long durationMillis;
	@Setter private Long activatedAt;
	
	protected BoosterImpl(SQLResult result) throws SQLException {
		this(result.getString(SQLColumns.PLAYER), result.getInt(SQLColumns.ID), result.getString(SQLColumns.PLUGIN_ID), result.getInt(SQLColumns.MULTIPLIER), result.getInt(SQLColumns.DURATION), getActivatedAtNullable(result));
	}
	
	public boolean isExpired(long now) {
		if (!wasActivated()) {
			throw new IllegalStateException("Cannot check expiration on booster that was not activated");
		}
		
		return now >= activatedAt + durationMillis;
	}
	
	public boolean wasActivated() {
		return activatedAt != null;
	}

	private static Long getActivatedAtNullable(SQLResult result) throws SQLException {
		long activatedAt = result.getLong(SQLColumns.ACTIVATED_AT);
		return result.wasNull() ? null : activatedAt;
	}

	
}
