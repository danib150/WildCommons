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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.gmail.filoghost.boosters.BoostersPlugin;

import lombok.AllArgsConstructor;
import lombok.NonNull;

public class DBCache {
	
	private static final long CACHE_MAX_TIME = TimeUnit.SECONDS.toMillis(30);
	
	private static Set<Player> loadingPlayers = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<Player, Boolean>()));
	private static Map<Player, BoostersCache> boostersByPlayer = Collections.synchronizedMap(new WeakHashMap<>());
	
	
	public static void getNonExpiredBoosters(Player player, Runnable runIfLongLoading, Callback<List<BoosterImpl>> successCallback, Callback<Exception> errorCallback) {
		if (loadingPlayers.contains(player)) {
			// Sta già eseguendo il task: è per evitare spam di richieste al database
			return;
		}

		BoostersCache cache = boostersByPlayer.get(player);
		long now = System.currentTimeMillis();
		
		if (cache == null || now - cache.updatedAtTime > CACHE_MAX_TIME) {
			
			loadingPlayers.add(player);
			BukkitTask runIfLongLoadingTask = Bukkit.getScheduler().runTaskLaterAsynchronously(BoostersPlugin.instance, runIfLongLoading, 5); // 5 ticks = 250 ms
			
			Bukkit.getScheduler().runTaskAsynchronously(BoostersPlugin.instance, () -> {
				try {
					List<BoosterImpl> boostersList = SQLManager.getNonExpiredBoosters(player.getName());
					runIfLongLoadingTask.cancel(); // Cancella il task a questo punto (se non è ancora stato eseguito)
					boostersByPlayer.put(player, new BoostersCache(boostersList, now));
					
					Bukkit.getScheduler().runTask(BoostersPlugin.instance, () -> {
						successCallback.onCall(boostersList);
					});
				} catch (Exception e) {
					Bukkit.getScheduler().runTask(BoostersPlugin.instance, () -> {
						errorCallback.onCall(e);
					});
				} finally {
					loadingPlayers.remove(player);
				}
			});
		} else {
			for (Iterator<BoosterImpl> iter = cache.boosters.iterator(); iter.hasNext();) {
				BoosterImpl booster = iter.next();
				if (booster.wasActivated() && booster.isExpired(now)) {
					iter.remove();
				}
				
			}
			successCallback.onCall(cache.boosters);
		}
	}
	
	/*
	 * Cancella la cache del database per un giocatore.
	 * Si usa quando un booster viene creato o attivato, per fare in modo che il giocatore veda una lista aggiornata.
	 * Questo non succede troppo frequentemente, quindi non è un problema di performance eliminare la cache in quei casi.
	 */
	public static void invalidate(String playerName) {
		boostersByPlayer.entrySet().removeIf(entry -> entry.getKey().getName().equalsIgnoreCase(playerName));
	}
	
	public static void update(BoosterImpl updatedBooster) {
		for (BoostersCache cache : boostersByPlayer.values()) {
			for (BoosterImpl booster : cache.boosters) {
				if (booster.getId() == updatedBooster.getId()) {
					// Questo è l'unico campo non final, che può cambiare
					booster.setActivatedAt(updatedBooster.getActivatedAt());
				}
			}
		}
	}
	
	
	@AllArgsConstructor
	public static class BoostersCache {
		
		@NonNull private final List<BoosterImpl> boosters;
		private final long updatedAtTime;
		
	}
	
	
	public static interface Callback<T> {
		
		void onCall(T t);
		
	}
	
}
