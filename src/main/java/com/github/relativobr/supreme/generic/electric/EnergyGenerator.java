package com.github.relativobr.supreme.generic.electric;

import com.github.relativobr.supreme.Supreme;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.implementation.items.electric.AbstractEnergyProvider;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.annotation.Nonnull;

import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class EnergyGenerator extends AbstractEnergyProvider {

  private static final String GENERATION_KEY = "supreme-generator-output";
  private static final String GENERATION_DELAY_KEY = "supreme-generator-delay";
  private static final Map<String, Long> DEBUG_LOG_TIMESTAMPS = new ConcurrentHashMap<>();
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

  @Nonnull
  @Override
  public ItemStack getProgressBar() {
    return new ItemStack(Material.OBSERVER);
  }

  @Override
  public int getEnergyProduction() {
    return this.energy;
  }

  @Override
  protected void registerDefaultFuelTypes() {}

  @Override
  public int getGeneratedOutput(Location l, Config data) {
    Config generatorData = data != null ? data : BlockStorage.getLocationInfo(l);
    int generate = getStoredValue(generatorData, GENERATION_KEY);
    int currentDelay = getStoredValue(generatorData, GENERATION_DELAY_KEY);

    if (generate > 0 && currentDelay < Supreme.getSupremeOptions().getDelayTimeValidGenerators()) {
      currentDelay++;
    } else {
      // check block
      generate = this.type.generate(l.getWorld(), l.getBlock(), this.energy);
      currentDelay = 0;
    }

    if (generatorData != null) {
      generatorData.setValue(GENERATION_KEY, String.valueOf(generate));
      generatorData.setValue(GENERATION_DELAY_KEY, String.valueOf(currentDelay));
    }

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

  private void logDebugState(Location location, Config data, int generated, int currentDelay) {
    if (!Supreme.getSupremeOptions().isDebugGenerators()) {
      return;
    }

    String locationKey = location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
    long now = System.currentTimeMillis();
    long lastLog = DEBUG_LOG_TIMESTAMPS.getOrDefault(locationKey, 0L);

    if (now - lastLog < DEBUG_LOG_INTERVAL_MS) {
      return;
    }

    DEBUG_LOG_TIMESTAMPS.put(locationKey, now);

    int storedCharge = 0;
    if (data != null && isChargeable()) {
      storedCharge = getCharge(location, data);
    }

    Supreme.inst().log(Level.INFO,
        "[GeneratorDebug] id=" + getId()
            + " location=" + location.getWorld().getName() + "@" + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ()
            + " configuredEnergy=" + energy
            + " generatedOutput=" + generated
            + " storedCharge=" + storedCharge
            + " buffer=" + buffer
            + " tickerDelay=" + Slimefun.getTickerTask().getTickRate()
            + " validationDelay=" + currentDelay);
  }


}
