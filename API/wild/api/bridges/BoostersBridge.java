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

import java.util.Set;

import org.bukkit.Bukkit;

import com.gmail.filoghost.boosters.api.BoostersAPI;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

public class BoostersBridge {
	
	private static boolean setupDone;
	private static boolean enabled;
	
	private static Set<String> registerQueue = Sets.newHashSet();

	
	public static void setup() {
		if (setupDone) return;
		
		enabled = Bukkit.getPluginManager().isPluginEnabled("Boosters");
		setupDone = true;
		
		if (!enabled) return;
		
		for (String pluginID : registerQueue) {
			BoostersAPI.registerPluginID(pluginID);
		}
		
	}
	
	
	public static void registerPluginID(String pluginID) {
		if (!setupDone) {
			registerQueue.add(pluginID);
			return;
		}
		
		if (!enabled) return;
		
		try {
			BoostersAPI.registerPluginID(pluginID);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	public static void unregisterPluginID(String pluginID) {
		if (!enabled) return;
		
		try {
			BoostersAPI.unregisterPluginID(pluginID);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	
	public static Booster getActiveBooster(String pluginID) {
		if (!enabled) return null;
		
		try {
			com.gmail.filoghost.boosters.api.Booster wrappedBooster = BoostersAPI.getActiveBooster(pluginID);
			if (wrappedBooster != null) {
				return new Booster(wrappedBooster);
			} else {
				return null;
			}
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
	
	// Nota: questo metodo non può essere sul booster, perché potrebbe essere null
	public static String messageSuffix(Booster booster) {
		if (!enabled) return "";
		
		try {
			System.out.println("enabled");
			return BoostersAPI.getBoosterMessageSuffix(booster != null ? booster.wrappedBooster : null);
		} catch (Throwable t) {
			t.printStackTrace();
			return "";
		}
	}
	
	
	public static int applyMultiplier(int amount, Booster booster) {
		if (!enabled) return amount;
		
		try {
			return booster != null ? amount * booster.getMultiplier() : amount;
		} catch (Throwable t) {
			t.printStackTrace();
			return amount;
		}
	}
	
	
	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Booster {
		
		private com.gmail.filoghost.boosters.api.Booster wrappedBooster;
		
		public String getPlayerName() {
			return wrappedBooster.getPlayerName();
		}
	  
		public int getMultiplier() {
			return wrappedBooster.getMultiplier();
		}
	
	}
	

}
