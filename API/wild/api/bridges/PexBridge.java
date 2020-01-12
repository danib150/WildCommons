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
package wild.api.bridges;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PexBridge {
	
	private static final long CACHE_MILLIS = TimeUnit.SECONDS.toMillis(60);
	private static final PrefixSuffix EMPTY_PREFIX_SUFFIX = new PrefixSuffix("", "");
	
	private static boolean enabled;
	private static PermissionManager manager;
	private static Map<Player, PrefixSuffixCache> cache;

	public static void setup() {
		if (enabled) return;
		enabled = Bukkit.getPluginManager().isPluginEnabled("PermissionsEx");
		
		if (enabled) {
			manager = PermissionsEx.getPermissionManager();
			cache = new WeakHashMap<>();
		}
	}
	
	public static PrefixSuffix getCachedPrefixSuffix(Player player) {
		if (!enabled) {
			return EMPTY_PREFIX_SUFFIX;
		}
		
		PrefixSuffixCache prefixSuffixCache = cache.get(player);
		long now = System.currentTimeMillis();
		
		if (prefixSuffixCache == null) {
			prefixSuffixCache = new PrefixSuffixCache(getPrefixSuffix(player), now);
			cache.put(player, prefixSuffixCache);
			
		} else if (now - prefixSuffixCache.lastUpdate > CACHE_MILLIS) {
			prefixSuffixCache.prefixSuffix = getPrefixSuffix(player);
			prefixSuffixCache.lastUpdate = now;
		}
		
		return prefixSuffixCache.prefixSuffix;
	}
	
	private static PrefixSuffix getPrefixSuffix(Player player) {
		if (!enabled) {
			return EMPTY_PREFIX_SUFFIX;
		}
		
		String prefix = null;
		String suffix = null;
		
		PermissionUser pexUser = manager.getUser(player);
		String ownPrefix = pexUser.getOwnPrefix();
		String ownSuffix = pexUser.getOwnSuffix();
		
		if (ownPrefix != null || ownSuffix != null) {
			// Tiene i propri prefissi personalizzati
			prefix = ownPrefix;
			suffix = ownSuffix;
			
		} else {
			// Cerca nel gruppo più alto
			PermissionGroup[] groups = pexUser.getGroups();
			if (groups != null && groups.length > 0) {
				
				PermissionGroup highestGroup = getHighestRankGroup(groups);
				if (highestGroup != null) {
					prefix = highestGroup.getPrefix();
					suffix = highestGroup.getSuffix();
				}
			}
		}
		
		if (prefix == null) {
			prefix = "";
		}
		if (suffix == null) {
			suffix = "";
		}
		
		return new PrefixSuffix(prefix, suffix);
	}
	
	private static PermissionGroup getHighestRankGroup(PermissionGroup[] groups) {
		PermissionGroup bestGroup = null;
		int bestGroupRank = 0;
		
		for (PermissionGroup group : groups) {
			if (bestGroup == null || group.getRank() < bestGroupRank) {
				bestGroup = group;
				bestGroupRank = group.getRank();
			}
		}
		
		return bestGroup;
	}
	
	
	@AllArgsConstructor(access = AccessLevel.PACKAGE)
	@Getter
	public static class PrefixSuffix {
		
		private final String prefix, suffix;
		
	}
	
	
	@AllArgsConstructor
	private static class PrefixSuffixCache {

		private PrefixSuffix prefixSuffix;
		private long lastUpdate;
		
	}

}
