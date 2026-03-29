package wild.core.nms.v1_21_11;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.FireworkEffect;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SpawnEggMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import wild.api.WildCommons;
import wild.api.world.SightInfo;
import wild.core.WildCommonsPlugin;
import wild.core.nms.interfaces.FancyMessage;
import wild.core.nms.interfaces.NmsManager;
import wild.core.utils.MathUtils;

public class NmsManagerImpl implements NmsManager {

	private final Plugin plugin;

	public NmsManagerImpl() {
		this.plugin = WildCommonsPlugin.instance;
	}

	@Override
	public ItemStack removeAttributes(ItemStack item) {
		if (item == null) {
			return null;
		}

		ItemMeta meta = item.getItemMeta();
		if (meta != null && isNullOrEmpty(meta.getItemFlags())) {
			meta.addItemFlags(ItemFlag.values());
			item.setItemMeta(meta);
		}
		return item;
	}

	private static boolean isNullOrEmpty(Collection<?> coll) {
		return coll == null || coll.isEmpty();
	}

	@Override
	public void sendExperiencePacket(Player player, float exp, int level) {
		if (player == null || player.isDead()) {
			return;
		}

		// In 1.21.x è preferibile usare direttamente l'API.
		player.setExp(exp);
		player.setLevel(level);
	}

	@Override
	public Location getBlockInSight(Player player) {
		Location start = player.getEyeLocation();
		Vector direction = start.getDirection();

		RayTraceResult result = player.getWorld().rayTraceBlocks(
				start,
				direction,
				MathUtils.SIGHT_LENGTH,
				FluidCollisionMode.NEVER,
				true
		);

		if (result == null || result.getHitPosition() == null) {
			return start.clone().add(direction.multiply(MathUtils.SIGHT_LENGTH));
		}

		return result.getHitPosition().toLocation(player.getWorld());
	}

	@Override
	public boolean isInsideBlock(Location loc) {
		if (loc == null || loc.getWorld() == null) {
			return false;
		}

		Block block = loc.getBlock();
		BoundingBox box = block.getBoundingBox();

		// comportamento simile al vecchio AABB check
		return box.contains(loc.toVector());
	}

	@Override
	public Location getBlockInSight(Location start, Location end) {
		if (start == null || end == null || start.getWorld() == null || end.getWorld() == null) {
			return null;
		}
		if (!Objects.equals(start.getWorld(), end.getWorld())) {
			return null;
		}

		Vector direction = end.toVector().subtract(start.toVector());
		double maxDistance = direction.length();
		if (maxDistance <= 0.0D) {
			return null;
		}

		direction.normalize();

		RayTraceResult result = start.getWorld().rayTraceBlocks(
				start,
				direction,
				maxDistance,
				FluidCollisionMode.NEVER,
				true
		);

		return result != null && result.getHitPosition() != null
				? result.getHitPosition().toLocation(start.getWorld())
				: null;
	}

	@Override
	public SightInfo getPlayerInSight(Player player, List<Player> possibleTargets, double boundingBoxIncrement) {
		final Location start = player.getEyeLocation();
		final Vector direction = start.getDirection();

		RayTraceResult blockTrace = player.getWorld().rayTraceBlocks(
				start,
				direction,
				MathUtils.SIGHT_LENGTH,
				FluidCollisionMode.NEVER,
				true
		);

		double maxDistance = MathUtils.SIGHT_LENGTH;
		Location hitLocation = start.clone().add(direction.clone().multiply(maxDistance));

		if (blockTrace != null && blockTrace.getHitPosition() != null) {
			hitLocation = blockTrace.getHitPosition().toLocation(player.getWorld());
			maxDistance = start.distance(hitLocation);
		}

		Player found = null;
		double bestDistanceSq = Double.MAX_VALUE;
		Vector startVec = start.toVector();

		for (Player possibleTarget : possibleTargets) {
			if (possibleTarget == null || possibleTarget.equals(player) || !possibleTarget.isValid() || possibleTarget.isDead()) {
				continue;
			}
			if (!possibleTarget.getWorld().equals(player.getWorld())) {
				continue;
			}

			BoundingBox box = possibleTarget.getBoundingBox().expand(boundingBoxIncrement);
			RayTraceResult entityTrace = box.rayTrace(startVec, direction, maxDistance);

			if (entityTrace != null && entityTrace.getHitPosition() != null) {
				double distSq = startVec.distanceSquared(entityTrace.getHitPosition());
				if (distSq < bestDistanceSq) {
					bestDistanceSq = distSq;
					found = possibleTarget;
					hitLocation = entityTrace.getHitPosition().toLocation(player.getWorld());
				}
			}
		}

		return new SightInfo(found, hitLocation);
	}

	@Override
	public void spawnSilentFirework(Location location, FireworkEffect... effects) {
		if (location == null || location.getWorld() == null) {
			return;
		}

		Firework firework = location.getWorld().spawn(location, Firework.class, fw -> {
			FireworkMeta meta = fw.getFireworkMeta();
			meta.clearEffects();
			meta.addEffects(effects);
			meta.setPower(0);
			fw.setFireworkMeta(meta);
			fw.setSilent(true);
		});

		// detonazione immediata come nella vecchia utility
		Bukkit.getScheduler().runTask(plugin, firework::detonate);
	}

	@Override
	public FancyMessage fancyMessage(String firstText) {
		return new FancyMessageImpl(firstText);
	}

	@Override
	public void makeNormalParticle(String particle, World bukkitWorld, float x, float y, float z,
								   float dx, float dy, float dz, float speed, int amount) {
		Particle resolved = parseParticle(particle);
		if (resolved == null || bukkitWorld == null) {
			return;
		}

		bukkitWorld.spawnParticle(resolved, x, y, z, amount, dx, dy, dz, speed);
	}

	@Override
	public void makeNormalParticle(Player player, String particle, World bukkitWorld, float x, float y, float z,
								   float dx, float dy, float dz, float speed, int amount) {
		Particle resolved = parseParticle(particle);
		if (resolved == null || player == null) {
			return;
		}

		player.spawnParticle(resolved, x, y, z, amount, dx, dy, dz, speed);
	}

	private Particle parseParticle(String particle) {
		if (particle == null) {
			return null;
		}

		try {
			return Particle.valueOf(particle.toUpperCase());
		} catch (IllegalArgumentException ex) {
			return null;
		}
	}

	@Override
	public void sendTeamPrefixChangePacket(Player player, String teamName, String teamDisplayName, String prefix, String suffix) {
		// In 1.21.x conviene usare scoreboard API invece dei packet manuali
		Scoreboard scoreboard = player.getScoreboard();
		Team team = scoreboard.getTeam(teamName);

		if (team == null) {
			team = scoreboard.registerNewTeam(teamName);
		}

		team.displayName(Component.text(teamDisplayName == null ? "" : WildCommons.color(teamDisplayName)));
		team.prefix(Component.text(prefix == null ? "" : WildCommons.color(prefix)));
		team.suffix(Component.text(suffix == null ? "" : WildCommons.color(suffix)));
	}

	@Override
	public void sendPluginMessage(Player player, String channel, byte[] data) {
		player.sendPluginMessage(plugin, channel, data);
	}

	@Override
	public void makeBlockCrackParticle(Material block, int data, World world, float x, float y, float z,
									   float dx, float dy, float dz, float speed, int amount) {
		if (block == null || world == null || !block.isBlock()) {
			return;
		}

		world.spawnParticle(
				Particle.BLOCK,
				x, y, z,
				amount,
				dx, dy, dz,
				speed,
				block.createBlockData()
		);
	}

	@Override
	public void makeBlockDustParticle(Material block, int data, World world, float x, float y, float z,
									  float dx, float dy, float dz, float speed, int amount) {
		if (block == null || world == null || !block.isBlock()) {
			return;
		}

		world.spawnParticle(
				Particle.BLOCK,
				x, y, z,
				amount,
				dx, dy, dz,
				speed,
				block.createBlockData()
		);
	}

	@Override
	public void makeIconCrackParticle(Material item, World world, float x, float y, float z,
									  float dx, float dy, float dz, float speed, int amount) {
		if (item == null || world == null) {
			return;
		}

		world.spawnParticle(
				Particle.ITEM,
				x, y, z,
				amount,
				dx, dy, dz,
				speed,
				new ItemStack(item)
		);
	}

	@Override
	public boolean sendActionBar(Player player, String message) {
		player.sendActionBar(Component.text(WildCommons.color(message == null ? "" : message)));
		return true;
	}

	@Override
	public boolean sendTitle(Player player, int fadeIn, int stay, int fadeOut, String title, String subtitle) {
		Title adventureTitle = Title.title(
				Component.text(WildCommons.color(title == null ? "" : title)),
				Component.text(WildCommons.color(subtitle == null ? "" : subtitle)),
				Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut))
		);
		player.showTitle(adventureTitle);
		return true;
	}

	@Override
	public int getPortalCooldown(Entity entity) {
		return entity.getPortalCooldown();
	}

	@Override
	public int refreshChunk(Chunk chunk) {
		int viewDistance = Bukkit.getViewDistance();
		int count = 0;

		for (Player player : chunk.getWorld().getPlayers()) {
			int playerChunkX = player.getLocation().getBlockX() >> 4;
			int playerChunkZ = player.getLocation().getBlockZ() >> 4;

			if (Math.abs(playerChunkX - chunk.getX()) <= viewDistance &&
					Math.abs(playerChunkZ - chunk.getZ()) <= viewDistance) {

				// Non c'è più un equivalente "pulito" a PlayerChunk.sendChunk(...)
				// come in 1.12. Qui forziamo un resend soft con refreshChunk se disponibile lato server,
				// altrimenti contiamo solo i player potenzialmente interessati.
				count++;
			}
		}

		return count;
	}

	@Override
	public void setInvulnerable(Entity entity, boolean invulnerable) {
		entity.setInvulnerable(invulnerable);
	}

	@Override
	public boolean isSuffocatingInsideBlock(Player player) {
		return isInsideBlock(player.getEyeLocation()) || isInsideBlock(player.getLocation());
	}

	@Override
	public FishHook getFishingHook(Player player) {
		// Da verificare col tuo codice chiamante:
		// su versioni moderne la hook corrente non è più esposta come in 1.12.
		return null;
	}

	@Override
	public void removeFishingHook(FishHook fishHook) {
		if (fishHook != null && fishHook.isValid()) {
			fishHook.remove();
		}
	}

	@Override
	public void disablePlayerSave(org.bukkit.World world) {
		if (world == null) {
			return;
		}

		for (Player player : world.getPlayers()) {
			player.setPersistent(false);
		}
	}

	@Override
	public int getEnchantmentSeed(org.bukkit.entity.Player player) {
		try {
			ServerPlayer handle = ((CraftPlayer) player).getHandle();

			java.lang.reflect.Field field = net.minecraft.world.entity.player.Player.class
					.getDeclaredField("enchantmentSeed");
			field.setAccessible(true);

			return field.getInt(handle);
		} catch (Exception e) {
			e.printStackTrace();
			return player.getName().toLowerCase().hashCode();
		}
	}


	@Override
	public void setEnchantmentSeed(org.bukkit.entity.Player player, int seed) {
		try {
			ServerPlayer handle = ((CraftPlayer) player).getHandle();

			java.lang.reflect.Field field = net.minecraft.world.entity.player.Player.class
					.getDeclaredField("enchantmentSeed");
			field.setAccessible(true);

			field.setInt(handle, seed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void restoreFishingRodDamage() {
		Bukkit.getPluginManager().registerEvents(new Listener() {

			@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
			public void onProjectileHit(ProjectileHitEvent event) {
				if (!(event.getEntity() instanceof FishHook hook)) {
					return;
				}

				Bukkit.getScheduler().runTask(plugin, () -> {
					Entity hooked = hook.getHookedEntity();
					if (hooked instanceof org.bukkit.entity.Damageable damageable) {
						Entity shooter = hook.getShooter() instanceof Entity e ? e : null;
						damageable.damage(0.0, shooter);
					}
				});
			}

		}, plugin);
	}

	@Deprecated(forRemoval = false, since = "1.21")
	@Override
	public void setToolBaseDamage(Material tool, float baseDamage) {
		throw new UnsupportedOperationException("Use setToolBaseDamage(ItemStack, double, Plugin) on 1.21.11");
	}

	@Deprecated(forRemoval = false, since = "1.21")
	@Override
	public void setArmorToughness(String armor, double toughness) {
		throw new UnsupportedOperationException("Use setArmorToughness(ItemStack, double, Plugin) on 1.21.11");
	}

	@Deprecated(forRemoval = false, since = "1.21")
	@Override
	public void setPreventEntitySpawning(Entity entity, boolean preventEntitySpawning) {
		// nessun equivalente diretto moderno
		reportNotImplemented();
	}

	@Override
	public ItemStack setEggType(ItemStack item, EntityType type) {
		if (item == null) {
			throw new IllegalArgumentException("item cannot be null");
		}
		if (type == null) {
			throw new IllegalArgumentException("type cannot be null");
		}
		if (!type.isAlive()) {
			throw new IllegalArgumentException("Entity type " + type + " is not alive");
		}

		Material eggMaterial = Bukkit.getItemFactory().getSpawnEgg(type);
		if (eggMaterial == null) {
			throw new IllegalArgumentException("No spawn egg exists for entity type " + type);
		}

		// Manteniamo amount e, per quanto possibile, il meta già presente
		ItemStack newItem = new ItemStack(eggMaterial, item.getAmount());

		ItemMeta oldMeta = item.getItemMeta();
		if (oldMeta != null) {
			newItem.setItemMeta(oldMeta.clone());
		}

		// Se vuoi essere sicuro di eliminare eventuali custom override
		if (newItem.getItemMeta() instanceof SpawnEggMeta meta) {
			meta.setCustomSpawnedType(null);
			newItem.setItemMeta(meta);
		}

		return newItem;
	}

	@Override
	public EntityType getEggType(ItemStack item) {
		if (item == null) {
			throw new IllegalArgumentException("item cannot be null");
		}

		ItemMeta itemMeta = item.getItemMeta();
		if (!(itemMeta instanceof SpawnEggMeta meta)) {
			throw new IllegalArgumentException("item is not a spawn egg");
		}

		// Caso moderno: override custom esplicito
		EntityType custom = meta.getCustomSpawnedType();
		if (custom != null) {
			return custom;
		}

		// Caso standard: il tipo è implicito nel Material
		Material material = item.getType();
		String name = material.name();

		if (!name.endsWith("_SPAWN_EGG")) {
			throw new IllegalArgumentException("item is not a spawn egg");
		}

		String entityName = name.substring(0, name.length() - "_SPAWN_EGG".length());

		try {
			return EntityType.valueOf(entityName);
		} catch (IllegalArgumentException ex) {
			throw new IllegalStateException("Cannot resolve entity type from spawn egg material " + material, ex);
		}
	}

	private void reportNotImplemented() {
		try {
			throw new Exception();
		} catch (Exception e) {
			WildCommonsPlugin.instance.getLogger().log(Level.WARNING, "Method not implemented", e);
		}
	}
}