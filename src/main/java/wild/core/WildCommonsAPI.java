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
package wild.core;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import lombok.Generated;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Maps;

import wild.api.WildCommons;
//import wild.api.bridges.BoostersBridge;
import wild.api.bridges.CosmeticsBridge;
import wild.api.bridges.EconomyBridge;
import wild.api.config.PluginConfig;
import wild.api.util.FileLogger;
import wild.core.commands.FireworkCommand;
import wild.core.commands.WildCommonsCommand;
import wild.core.nms.interfaces.NmsManager;

public class WildCommonsAPI {

	public static WildCommonsAPI instance;
	public static NmsManager nmsManager;
	public static FileLogger mysqlErrorLogger;
	public static boolean disableWorldPlayerSave;
	public static boolean serverInitialized;
	
	public static Map<Material, String> materialTranslationsMap = Maps.newHashMap();
	public static Map<Enchantment, String> enchantmentTranslationsMap = Maps.newHashMap();
	public static Map<PotionEffectType, String> potionEffectsTranslationsMap = Maps.newHashMap();
	public static Map<EntityType, String> entityTypesTranslationsMap = Maps.newHashMap();

	@Getter
	public final JavaPlugin plugin;
	public WildCommonsAPI(JavaPlugin plugin) {
		this.plugin = plugin;
	}

	@Getter
	public Logger logger;

	public void initializeWildCommons() {
		instance = this;
		mysqlErrorLogger = new FileLogger(plugin, "mysql.error.log");
		logger = plugin.getLogger();

		String version = WildCommons.getBukkitVersion();

		if("v1_8_R3".equals(version)) {
			nmsManager = new wild.core.nms.v1_8_R3.NmsManagerImpl();
		}else if ("v1_9_R2".equals(version)) {
			nmsManager = new wild.core.nms.v1_9_R2.NmsManagerImpl();
		} else if ("v1_12_R1".equals(version)) {
			nmsManager = new wild.core.nms.v1_12_R1.NmsManagerImpl();
		} else {
			System.out.println(
				" \n "
			   + "\n  ######################## ATTENZIONE ########################"
			   + "\n  #                                                          #"
			   + "\n  #  WildCommons non e' aggiornato. Il server verra' spento. #"
			   + "\n  #                                                          #"
			   + "\n  ############################################################"
			   + "\n "
			   + "\n Versione: " + version
			   + "\n "
			);
			
			WildCommons.pauseThread(30000);
			Bukkit.shutdown();
			return;
		}
		
		// Disabilita eventualmente il salvataggio dei giocatori
		try {
			PluginConfig config = new PluginConfig(plugin, "config_worlds.yml");
			String configKey = "disablePlayerSave";
			if (!config.isSet(configKey)) {
				config.set(configKey, false);
				config.save();
			}
			disableWorldPlayerSave = config.getBoolean(configKey);
			
		} catch (Exception ex) {
			plugin.getLogger().log(Level.WARNING, "Impossibile leggere o salvare il file di configurazione", ex);
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}

		try {
			plugin.getDataFolder().mkdirs();
			loadTranslations();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.WARNING, "Impossibile leggere le traduzioni", ex);
			WildCommons.pauseThread(10000);
			Bukkit.shutdown();
			return;
		}
		load(plugin);
	}

	private void load(JavaPlugin plugin) {
		try {
			wild.api.uuid.PackageAccess.UUIDRegistry_init();
		} catch (Exception ex) {
			plugin.getLogger().log(Level.WARNING, "Impossibile leggere il registro degli UUID", ex);
			WildCommons.pauseThread(10000);
			plugin.getPluginLoader().disablePlugin(plugin);
			Bukkit.shutdown();
			return;
		}
		
		// Registra il comando
		new WildCommonsCommand(plugin, "wildcommons");
		new FireworkCommand(plugin, "firework", "fw");
		
		// Registra il listener
		Bukkit.getPluginManager().registerEvents(new WildCommonsListener(), plugin);
		
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			serverInitialized = true;
			
			try {
				if (Bukkit.getPluginManager().isPluginEnabled("Essentials")) {
					EconomyBridge.setup();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
			
			try {
				if (Bukkit.getPluginManager().isPluginEnabled("ProCosmetics")) {
					CosmeticsBridge.setup();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}, 1L);
	}
	
	public void saveUUIDRegistry_save() {
		wild.api.uuid.PackageAccess.UUIDRegistry_save();
	}
	
	public void loadTranslations() throws Exception {
		materialTranslationsMap.clear();
		PluginConfig config;
		boolean configChanged;
		
		config = new PluginConfig(plugin, "translations_materials.yml");
		configChanged = false;

		for (Material material : Material.values()) {
			
			// Nome della chiave.
			String key = material.toString();
			String translation;
			
			if (config.isString(key)) {
				
				// Ottieni la traduzione dal config.
				translation = config.getString(key);
				
			} else {
			
				// Usa la traduzione di default.
				translation = material.toString();
				
				// Aggiunge il materiale alle traduzioni.
				config.set(key, translation);
				configChanged = true;
				
			}
			
			// Mette nella mappa delle traduzioni.
			materialTranslationsMap.put(material, translation);
		}
		
		if (configChanged) {
			config.save();
		}
		
		enchantmentTranslationsMap.clear();
		config = new PluginConfig(plugin, "translations_enchants.yml");
		configChanged = false;

		for (Enchantment enchant : Enchantment.values()) {
			
			if (enchant == null) continue;
			
			// Nome della chiave.
			String key = enchant.getName();
			String translation;
			
			if (config.isString(key)) {
				
				// Ottieni la traduzione dal config.
				translation = config.getString(key);
				
			} else {
			
				// Usa la traduzione di default.
				translation = enchant.getName();
				
				// Aggiunge il materiale alle traduzioni.
				config.set(key, translation);
				configChanged = true;
				
			}
			
			// Mette nella mappa delle traduzioni.
			enchantmentTranslationsMap.put(enchant, translation);
		}
		
		if (configChanged) {
			config.save();
		}
		
		potionEffectsTranslationsMap.clear();
		config = new PluginConfig(plugin, "translations_potions.yml");
		configChanged = false;

		for (PotionEffectType potionType : PotionEffectType.values()) {
			
			if (potionType == null) continue;
			
			// Nome della chiave.
			String key = potionType.getName();
			String translation;
			
			if (config.isString(key)) {
				
				// Ottieni la traduzione dal config.
				translation = config.getString(key);
				
			} else {
			
				// Usa la traduzione di default.
				translation = potionType.getName();
				
				// Aggiunge il materiale alle traduzioni.
				config.set(key, translation);
				configChanged = true;
				
			}
			
			// Mette nella mappa delle traduzioni.
			potionEffectsTranslationsMap.put(potionType, translation);
		}
		
		if (configChanged) {
			config.save();
		}
		
		entityTypesTranslationsMap.clear();
		config = new PluginConfig(plugin, "translations_entities.yml");
		configChanged = false;

		for (EntityType entityType : EntityType.values()) {
			
			// Nome della chiave.
			String key = entityType.toString();
			String translation;
			
			if (config.isString(key)) {
				
				// Ottieni la traduzione dal config.
				translation = config.getString(key);
				
			} else {
			
				// Usa la traduzione di default.
				translation = entityType.toString();
				
				// Aggiunge il materiale alle traduzioni.
				config.set(key, translation);
				configChanged = true;
				
			}
			
			// Mette nella mappa delle traduzioni.
			entityTypesTranslationsMap.put(entityType, translation);
		}
		
		if (configChanged) {
			config.save();
		}
	}
}
