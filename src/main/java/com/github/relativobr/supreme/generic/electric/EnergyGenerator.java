package com.github.relativobr.supreme.generic.electric;

import com.github.relativobr.supreme.Supreme;
import com.github.relativobr.supreme.util.UtilEnergy;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNet;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.core.attributes.EnergyNetProvider;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;
import me.mrCookieSlime.Slimefun.api.inventory.DirtyChestMenu;
import net.guizhanss.guizhanlib.slimefun.machines.MenuBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public final class EnergyGenerator extends MenuBlock implements EnergyNetProvider {

  private static final String GENERATION_KEY = "supreme-generator-output";
  private static final String GENERATION_DELAY_KEY = "supreme-generator-delay";
  private static final String ENERGY_CHARGE_KEY = "energy-charge";
  private static final String LEGACY_BUFFER_CHARGE_KEY = "supreme-buffer-charge";
  private static final Map<String, Long> DEBUG_LOG_TIMESTAMPS = new ConcurrentHashMap<>();
  private static final Map<String, PendingDistribution> PENDING_DISTRIBUTIONS = new ConcurrentHashMap<>();
  private static final long DEBUG_LOG_INTERVAL_MS = 5000L;

  private int energy;
  private int buffer;
  private GenerationType type;


  public EnergyGenerator(ItemGroup categories, SlimefunItemStack item, ItemStack[] recipe) {
    super(categories, item, RecipeType.ENHANCED_CRAFTING_TABLE, recipe);
  }

  public GenerationType getType() {
    return type;
  }

  public EnergyGenerator setType(GenerationType value) {
    this.type = value;
    return this;
  }

  public EnergyGenerator setEnergy(int value) {
    this.energy = value;
    return this;
  }

  public EnergyGenerator setBuffer(int value) {
    this.buffer = value;
    return this;
  }

  @Override
  protected void setup(BlockMenuPreset blockMenuPreset) {
    blockMenuPreset.drawBackground(
        new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17,
            18, 19, 20, 21, 22, 23, 24, 25, 26
        }
    );
  }

  @Override
  protected int[] getInputSlots(DirtyChestMenu dirtyChestMenu, ItemStack itemStack) {
    return new int[0];
  }

  @Override
  public int getGeneratedOutput(Location l, Config data) {
    Config generatorData = data != null ? data : BlockStorage.getLocationInfo(l);
    int generate = getStoredValue(generatorData, GENERATION_KEY);
    int currentDelay = getStoredValue(generatorData, GENERATION_DELAY_KEY);
    boolean useValidationCache = type != null && type.usesValidationCache();

    if (useValidationCache && generate > 0
        && currentDelay < Supreme.getSupremeOptions().getDelayTimeValidGenerators()) {
      currentDelay++;
    } else {
      // Environment-based generators must be revalidated every tick so they stop immediately
      // when their source condition disappears.
      generate = this.type.generate(l.getWorld(), l.getBlock(), this.energy);
      currentDelay = 0;
    }

    if (generatorData != null) {
      generatorData.setValue(GENERATION_KEY, String.valueOf(generate));
      generatorData.setValue(GENERATION_DELAY_KEY, String.valueOf(currentDelay));
    }

    updateMenu(l, generatorData, generate);
    logDebugState(l, generatorData, generate, currentDelay);

    return generate;
  }

  @Override
  public int getCapacity() {
    return this.buffer;
  }

  @Override
  public int[] getInputSlots() {
    return new int[0];
  }

  @Override
  public int[] getOutputSlots() {
    return new int[0];
  }

  @Override
  public int getCharge(@Nonnull Location l, @Nonnull Config data) {
    if (data == null) {
      return 0;
    }

    int rawStoredCharge = getRawStoredCharge(data);
    boolean environmentActive = isEnvironmentActive(l);
    int visibleCharge = rawStoredCharge;

    logChargeVisibility(l, rawStoredCharge, visibleCharge, environmentActive);
    return visibleCharge;
  }

  @Override
  public int getCharge(@Nonnull Location l) {
    return getCharge(l, BlockStorage.getLocationInfo(l));
  }

  @Override
  public void setCharge(@Nonnull Location l, int charge) {
    boolean environmentActive = isEnvironmentActive(l);
    if (type != null && !type.usesNetworkBuffer()) {
      if (distributeEnvironmentalCharge(l, charge)) {
        return;
      }

      if (!environmentActive) {
        int currentCharge = getRawStoredCharge(BlockStorage.getLocationInfo(l));
        logIgnoredChargeUpdate(l, charge, currentCharge);
        updateMenu(l, BlockStorage.getLocationInfo(l), getStoredValue(BlockStorage.getLocationInfo(l), GENERATION_KEY));
        return;
      }
    }

    Config latestData = BlockStorage.getLocationInfo(l);
    int previousCharge = getRawStoredCharge(latestData);
    int clamped = Math.max(0, Math.min(getCapacity(), charge));
    applyStoredCharge(l, clamped);
    Config updatedData = BlockStorage.getLocationInfo(l);
    updateMenu(l, updatedData, getStoredValue(updatedData, GENERATION_KEY));
    logChargeUpdate(l, charge, clamped, previousCharge, getRawStoredCharge(updatedData), environmentActive);
  }

  @Override
  protected void onNewInstance(BlockMenu menu, Block b) {
    updateMenu(b.getLocation(), BlockStorage.getLocationInfo(b.getLocation()),
        getStoredValue(BlockStorage.getLocationInfo(b.getLocation()), GENERATION_KEY));
  }

  private void updateMenu(Location location, Config data, int generated) {
    BlockMenu menu = BlockStorage.getInventory(location);
    if (menu == null || !menu.hasViewer()) {
      return;
    }

    int capacity = getCapacity();
    int storedCharge = getRawStoredCharge(BlockStorage.getLocationInfo(location));

    if (generated <= 0) {
      menu.replaceExistingItem(
          13,
          CustomItemStack.create(
              Material.RED_STAINED_GLASS_PANE,
              "&cNot generating",
              "&7Type: &6" + this.type,
              "&7Stored: &6" + UtilEnergy.format(storedCharge) + " J",
              "&7Capacity: &6" + UtilEnergy.format(capacity) + " J"
          )
      );
    } else {
      menu.replaceExistingItem(
          13,
          CustomItemStack.create(
              Material.GREEN_STAINED_GLASS_PANE,
              "&aGeneration",
              "&7Type: &6" + this.type,
              "&7Generating: &6" + UtilEnergy.format(generated) + " J/tick",
              "&7Stored: &6" + UtilEnergy.format(storedCharge) + " J",
              "&7Capacity: &6" + UtilEnergy.format(capacity) + " J"
          )
      );
    }
  }

  private int getStoredValue(Config data, String key) {
    if (data == null || !data.contains(key)) {
      return 0;
    }

    String rawValue = data.getString(key);
    if (rawValue == null) {
      return 0;
    }

    try {
      return Integer.parseInt(rawValue);
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  private int getRawStoredCharge(Config data) {
    return Math.max(
        getStoredValue(data, ENERGY_CHARGE_KEY),
        getStoredValue(data, LEGACY_BUFFER_CHARGE_KEY)
    );
  }

  private boolean isEnvironmentActive(Location location) {
    return type != null && type.generate(location.getWorld(), location.getBlock(), this.energy) > 0;
  }

  private void logDebugState(Location location, Config data, int generated, int currentDelay) {
    if (!Supreme.getSupremeOptions().isDebugGenerators()) {
      return;
    }

    String locationKey = getLocationKey(location);
    if (!shouldLog("state:" + locationKey)) {
      return;
    }

    int capacity = getCapacity();
    int storedCharge = getRawStoredCharge(data);
    boolean environmentActive = isEnvironmentActive(location);
    int visibleCharge = storedCharge;

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] id=" + getId()
            + " location=" + location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
            + " configuredEnergy=" + energy
            + " generatedOutput=" + generated
            + " storedCharge=" + storedCharge
            + " visibleCharge=" + visibleCharge
            + " environmentActive=" + environmentActive
            + " buffer=" + capacity
            + " tickerDelay=" + Slimefun.getTickerTask().getTickRate()
            + " validationDelay=" + currentDelay);
  }

  private void logChargeUpdate(Location location, int requestedCharge, int clampedCharge, int previousCharge, int updatedCharge, boolean environmentActive) {
    if (!Supreme.getSupremeOptions().isDebugGenerators()) {
      return;
    }

    String locationKey = getLocationKey(location);
    if (!shouldLog("setCharge:" + locationKey)) {
      return;
    }

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] setCharge id=" + getId()
            + " location=" + location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
            + " requested=" + requestedCharge
            + " clamped=" + clampedCharge
            + " previousCharge=" + previousCharge
            + " updatedCharge=" + updatedCharge
            + " environmentActive=" + environmentActive);
  }

  private void logIgnoredChargeUpdate(Location location, int requestedCharge, int currentCharge) {
    if (!Supreme.getSupremeOptions().isDebugGenerators()) {
      return;
    }

    String locationKey = getLocationKey(location);
    if (!shouldLog("ignoredSetCharge:" + locationKey)) {
      return;
    }

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] ignoredSetCharge id=" + getId()
            + " location=" + location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
            + " requested=" + requestedCharge
            + " currentCharge=" + currentCharge
            + " environmentActive=false");
  }

  private boolean distributeEnvironmentalCharge(Location source, int requestedCharge) {
    EnergyNet network = EnergyNet.getNetworkFromLocation(source);
    if (network == null) {
      return false;
    }

    List<Location> activeGenerators = getActiveEnvironmentalGenerators(network);
    if (activeGenerators.isEmpty()) {
      return false;
    }

    String distributionKey = getDistributionKey(network);
    PendingDistribution pending = PENDING_DISTRIBUTIONS.computeIfAbsent(distributionKey, key -> {
      scheduleDistributionReset(key);
      return new PendingDistribution();
    });

    if (requestedCharge > 0) {
      pending.totalCharge = Math.min(activeGenerators.size() * getCapacity(), pending.totalCharge + requestedCharge);
    }

    int remainingCharge = pending.totalCharge;
    int remainingGenerators = activeGenerators.size();
    for (Location target : activeGenerators) {
      int assignedCharge = remainingGenerators > 0
          ? Math.min(getCapacity(), remainingCharge / remainingGenerators)
          : 0;

      if (remainingCharge > 0 && remainingGenerators > 0 && remainingCharge % remainingGenerators != 0) {
        assignedCharge = Math.min(getCapacity(), assignedCharge + 1);
      }

      applyStoredCharge(target, assignedCharge);
      updateMenu(target, BlockStorage.getLocationInfo(target),
          getStoredValue(BlockStorage.getLocationInfo(target), GENERATION_KEY));

      remainingCharge = Math.max(0, remainingCharge - assignedCharge);
      remainingGenerators--;
    }

    logDistributedCharge(source, requestedCharge, activeGenerators);
    return true;
  }

  private void applyStoredCharge(Location location, int charge) {
    BlockStorage.addBlockInfo(location, ENERGY_CHARGE_KEY, String.valueOf(charge), false);
    BlockStorage.addBlockInfo(location, LEGACY_BUFFER_CHARGE_KEY, String.valueOf(charge), false);
  }

  private void logDistributedCharge(Location source, int requestedCharge, List<Location> activeGenerators) {
    if (!Supreme.getSupremeOptions().isDebugGenerators()) {
      return;
    }

    String sourceKey = getLocationKey(source);
    if (!shouldLog("distributedCharge:" + sourceKey)) {
      return;
    }

    StringBuilder targets = new StringBuilder();
    for (Location target : activeGenerators) {
      if (targets.length() > 0) {
        targets.append(';');
      }
      targets.append(target.getBlockX()).append(',').append(target.getBlockY()).append(',').append(target.getBlockZ())
          .append('=').append(getRawStoredCharge(BlockStorage.getLocationInfo(target)));
    }

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] distributedCharge id=" + getId()
            + " source=" + source.getWorld().getName() + "@" + source.getBlockX() + "," + source.getBlockY() + "," + source.getBlockZ()
            + " requested=" + requestedCharge
            + " targets=" + targets);
  }

  private void logChargeVisibility(Location location, int rawStoredCharge, int visibleCharge, boolean environmentActive) {
    if (!Supreme.getSupremeOptions().isDebugGenerators() || rawStoredCharge == visibleCharge) {
      return;
    }

    String locationKey = getLocationKey(location);
    if (!shouldLog("getCharge:" + locationKey)) {
      return;
    }

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] getCharge id=" + getId()
            + " location=" + location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
            + " rawStoredCharge=" + rawStoredCharge
            + " visibleCharge=" + visibleCharge
            + " environmentActive=" + environmentActive);
  }

  private boolean shouldLog(String debugKey) {
    long now = System.currentTimeMillis();
    long lastLog = DEBUG_LOG_TIMESTAMPS.getOrDefault(debugKey, 0L);

    if (now - lastLog < DEBUG_LOG_INTERVAL_MS) {
      return false;
    }

    DEBUG_LOG_TIMESTAMPS.put(debugKey, now);
    return true;
  }

  private String getLocationKey(Location location) {
    return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
  }

  private String getDistributionKey(EnergyNet network) {
    return System.identityHashCode(network) + ":" + getId();
  }

  private List<Location> getActiveEnvironmentalGenerators(EnergyNet network) {
    List<Location> activeGenerators = new ArrayList<>();

    for (Map.Entry<Location, EnergyNetProvider> entry : network.getGenerators().entrySet()) {
      Location location = entry.getKey();
      if (!getId().equals(BlockStorage.checkID(location))) {
        continue;
      }

      if (!isEnvironmentActive(location)) {
        continue;
      }

      activeGenerators.add(location);
    }

    activeGenerators.sort(Comparator
        .comparingInt(Location::getBlockX)
        .thenComparingInt(Location::getBlockY)
        .thenComparingInt(Location::getBlockZ));
    return activeGenerators;
  }

  private void scheduleDistributionReset(String distributionKey) {
    Bukkit.getScheduler().runTaskLater(Supreme.inst(), () -> PENDING_DISTRIBUTIONS.remove(distributionKey), 1L);
  }

  private static final class PendingDistribution {
    private int totalCharge;
  }


}
