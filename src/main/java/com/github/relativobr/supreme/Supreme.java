package com.github.relativobr.supreme;

import static com.github.relativobr.supreme.util.CompatibilySupremeLegacy.loadComponents;
import static com.github.relativobr.supreme.util.CompatibilySupremeLegacy.loadCoreResource;
import static com.github.relativobr.supreme.util.CompatibilySupremeLegacy.loadGear;
import static com.github.relativobr.supreme.util.CompatibilySupremeLegacy.loadGenerators;
import static com.github.relativobr.supreme.util.CompatibilySupremeLegacy.loadMachines;

import com.github.relativobr.supreme.setup.MainSetup;
import com.github.relativobr.supreme.util.CompatibilySupremeLegacyItem;
import com.github.relativobr.supreme.util.SupremeOptions;
import com.github.relativobr.supreme.util.SupremePowerSection;
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.libraries.dough.config.Config;
import io.github.thebusybiscuit.slimefun4.libraries.dough.updater.BlobBuildUpdater;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Supreme extends JavaPlugin implements SlimefunAddon {

  private static final int CONFIG_VERSION = 2;
  private static final String CONFIG_FILE_NAME = "config.yml";
  private static final String CONFIG_VERSION_PATH = "config-version";
  private static final DateTimeFormatter CONFIG_BACKUP_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

  private static Supreme instance;
  private static SupremeOptions supremeOptions = null;
  private static SupremePowerSection supremePowerSection = null;
  private static SupremeLocalization localization = null;
  private static List<CompatibilySupremeLegacyItem> legacyItem = null;

  public static Supreme inst() {
    return instance;
  }

  public static SupremeOptions getSupremeOptions() {
    if (supremeOptions == null) {
      ConfigurationSection typeSection = inst().getConfig().getConfigurationSection("options");
      if (typeSection == null) {
        supremeOptions = SupremeOptions.defaultValue();
      } else {
        supremeOptions = SupremeOptions.builder()
                .autoUpdate(typeSection.getBoolean("auto-update", true))
                .useLegacySupremeexpansionItemId(
                    typeSection.getBoolean("use-legacy-supremeexpansion-item-id", false))
                .lang(typeSection.getString("lang", "en-US"))
                .customTickerDelay(typeSection.getInt("custom-ticker-delay", 2))
                .enableGenerators(typeSection.getBoolean("enable-generators", true))
                .debugGenerators(typeSection.getBoolean("debug-generators", false))
                .limitProductionGenerators(typeSection.getBoolean("limit-production-generators", false))
                .delayTimeValidGenerators(typeSection.getInt("delay-time-valid-generators", 600))
                .enableQuarry(typeSection.getBoolean("enable-quarry", true))
                .limitProductionQuarry(typeSection.getBoolean("limit-production-quarry", false))
                .baseTimeVirtualGarden(typeSection.getInt("base-time-virtual-garden", 15))
                .baseTimeVirtualAquarium(typeSection.getInt("base-time-virtual-aquarium", 15))
                .baseTimeMobCollector(typeSection.getInt("base-time-mob-collector", 15))
                .baseTimeTechGenerator(typeSection.getInt("base-time-tech-generator", 1800))
                .maxAmountTechGenerator(typeSection.getInt("tech-generator-max-amount", 64))
                .mobTechEnableBee(typeSection.getBoolean("mob-tech-enable-bee", true))
                .mobTechEnableIronGolem(typeSection.getBoolean("mob-tech-enable-iron-golem", true))
                .mobTechEnableZombie(typeSection.getBoolean("mob-tech-enable-zombie", true))
                .enableWeapons(typeSection.getBoolean("enable-weapons", true))
                .enableTools(typeSection.getBoolean("enable-tools", true))
                .enableArmor(typeSection.getBoolean("enable-armor", true))
                .enableTech(typeSection.getBoolean("enable-tech", true))
                .enableResearches(typeSection.getBoolean("enable-researches", true))
                .enableItemConverter(typeSection.getBoolean("enable-item-converter-machine", true))
                .itemConverterBlacklist(typeSection.getStringList("item-converter-blacklist"))
                .customBc(typeSection.getBoolean("custom-bc", false))
                .machineMaxAttemptConsumed(typeSection.getInt("machine-max-attempt-consumed", 30))
                .build();
      }
    }
    return supremeOptions;
  }

  public static SupremePowerSection getSupremePowerSection() {
    if (supremePowerSection == null) {
      ConfigurationSection typeSection = inst().getConfig().getConfigurationSection("power-section");
      if (typeSection == null) {
        supremePowerSection = SupremePowerSection.defaultValue();
      } else {
        supremePowerSection = SupremePowerSection.builder()
                .capacitorAurumCapacity(getClampedEnergyValue(typeSection, "capacitor-aurum-capacity", 1000000))
                .capacitorTitaniumCapacity(getClampedEnergyValue(typeSection, "capacitor-titanium-capacity", 4000000))
                .capacitorAdamantiumCapacity(getClampedEnergyValue(typeSection, "capacitor-adamantium-capacity", 16000000))
                .capacitorThorniumCapacity(getClampedEnergyValue(typeSection, "capacitor-thornium-capacity", 100000000))
                .capacitorSupremeCapacity(getClampedEnergyValue(typeSection, "capacitor-supreme-capacity", 1600000000))
                .generatorBasicIgnisEnergy(getClampedEnergyValue(typeSection, "generator-basic-ignis-energy", 2500))
                .generatorBasicIgnisBuffer(getClampedEnergyValue(typeSection, "generator-basic-ignis-buffer", 5000))
                .generatorBasicVentusEnergy(getClampedEnergyValue(typeSection, "generator-basic-ventus-energy", 2500))
                .generatorBasicVentusBuffer(getClampedEnergyValue(typeSection, "generator-basic-ventus-buffer", 5000))
                .generatorBasicAquaEnergy(getClampedEnergyValue(typeSection, "generator-basic-aqua-energy", 2500))
                .generatorBasicAquaBuffer(getClampedEnergyValue(typeSection, "generator-basic-aqua-buffer", 5000))
                .generatorBasicLuxEnergy(getClampedEnergyValue(typeSection, "generator-basic-lux-energy", 2500))
                .generatorBasicLuxBuffer(getClampedEnergyValue(typeSection, "generator-basic-lux-buffer", 5000))
                .generatorBasicLumiumEnergy(getClampedEnergyValue(typeSection, "generator-basic-lumium-energy", 5000))
                .generatorBasicLumiumBuffer(getClampedEnergyValue(typeSection, "generator-basic-lumium-buffer", 10000))
                .generatorIgnisEnergy(getClampedEnergyValue(typeSection, "generator-ignis-energy", 25000))
                .generatorIgnisBuffer(getClampedEnergyValue(typeSection, "generator-ignis-buffer", 50000))
                .generatorVentusEnergy(getClampedEnergyValue(typeSection, "generator-ventus-energy", 25000))
                .generatorVentusBuffer(getClampedEnergyValue(typeSection, "generator-ventus-buffer", 50000))
                .generatorAquaEnergy(getClampedEnergyValue(typeSection, "generator-aqua-energy", 25000))
                .generatorAquaBuffer(getClampedEnergyValue(typeSection, "generator-aqua-buffer", 50000))
                .generatorLuxEnergy(getClampedEnergyValue(typeSection, "generator-lux-energy", 25000))
                .generatorLuxBuffer(getClampedEnergyValue(typeSection, "generator-lux-buffer", 50000))
                .generatorLumiumEnergy(getClampedEnergyValue(typeSection, "generator-lumium-energy", 75000))
                .generatorLumiumBuffer(getClampedEnergyValue(typeSection, "generator-lumium-buffer", 500000))
                .generatorThorniumEnergy(getClampedEnergyValue(typeSection, "generator-thornium-energy", 1000000))
                .generatorThorniumBuffer(getClampedEnergyValue(typeSection, "generator-thornium-buffer", 6000000))
                .generatorSupremeEnergy(getClampedEnergyValue(typeSection, "generator-supreme-energy", 2000000))
                .generatorSupremeBuffer(getClampedEnergyValue(typeSection, "generator-supreme-buffer", 12000000))
                .build();
      }
    }
    return supremePowerSection;
  }

  public static SupremeLocalization getLocalization() {
    if (localization == null) {
      localization = new SupremeLocalization(inst());
      localization.addLanguage(getSupremeOptions().getLang());
    }
    return localization;
  }

  public static List<CompatibilySupremeLegacyItem> getLegacyItem() {
    if (legacyItem == null || legacyItem.isEmpty()) {
      if (legacyItem == null) {
        legacyItem = new ArrayList<>();
      }
      loadComponents(legacyItem);
      loadGear(legacyItem);
      loadGenerators(legacyItem);
      loadMachines(legacyItem);
      loadCoreResource(legacyItem);
    }
    return legacyItem;
  }

  @Override
  public void onEnable() {

    instance = this;
    supremeOptions = null;
    supremePowerSection = null;
    localization = null;
    legacyItem = null;

    try {
      ensureConfigUpToDate();
    } catch (RuntimeException e) {
      log(Level.SEVERE, "Could not update config.yml safely, Supreme will not enable.");
      log(Level.SEVERE, e.getMessage());
      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    Supreme.inst().log(Level.INFO, "########################################");
    Supreme.inst().log(Level.INFO, "      Supreme - By RelativoBR      ");
    Supreme.inst().log(Level.INFO, "########################################");

    Config cfg = new Config(this);
    if (getSupremeOptions() == null) {
      log(Level.SEVERE, "Config section \"options\" missing, Check your config and report this!");
      inst().onDisable();
      return;
    }

      var autoUpdate = getSupremeOptions().isAutoUpdate() && getDescription().getVersion().startsWith("Dev");
      Supreme.inst().log(Level.INFO, "auto-update: " + autoUpdate);
      if (autoUpdate) {
          new BlobBuildUpdater(this, getFile(), "Supreme", "Dev").start();
      }

    // localization
    Supreme.inst().log(Level.INFO, "Loaded language Supreme: " + getSupremeOptions().getLang());
    getLocalization();
    logGeneratorDebugConfig();

    // check Compatibily Legacy (SupremeExpansion)
    if (getSupremeOptions().isUseLegacySupremeexpansionItemId()) {
      Supreme.inst().log(Level.INFO, "Legacy SupremeExpansion IDs: enable");
      getLegacyItem();
    } else {
      Supreme.inst().log(Level.INFO, "Legacy SupremeExpansion IDs: disable");
    }

    MainSetup.setup(this);

  }

  @Override
  public void onDisable() {
    supremeOptions = null;
    supremePowerSection = null;
    localization = null;
    legacyItem = null;
    instance = null;
  }

  @Override
  public String getBugTrackerURL() {
    return "";
  }

  @Override
  public JavaPlugin getJavaPlugin() {
    return this;
  }

  public final void log(Level level, String messages) {
    getLogger().log(level, messages);
  }

  private void logGeneratorDebugConfig() {
    if (!getSupremeOptions().isDebugGenerators()) {
      return;
    }

    File configFile = new File(getDataFolder(), "config.yml");
    SupremePowerSection powerSection = getSupremePowerSection();

    log(Level.INFO, "[GeneratorDebug] configFile=" + configFile.getAbsolutePath());
    log(Level.INFO,
        "[GeneratorDebug] loadedConfig"
            + " supremeEnergy=" + powerSection.getGeneratorSupremeEnergy()
            + " supremeBuffer=" + powerSection.getGeneratorSupremeBuffer()
            + " lumiumEnergy=" + powerSection.getGeneratorLumiumEnergy()
            + " lumiumBuffer=" + powerSection.getGeneratorLumiumBuffer());
  }

  private static int getClampedEnergyValue(ConfigurationSection section, String path, int defaultValue) {
    long value = section.getLong(path, defaultValue);

    if (value < 0) {
      inst().log(Level.WARNING, "Config value \"" + path + "\" was negative (" + value + "), using 0 instead.");
      return 0;
    }

    if (value > Integer.MAX_VALUE) {
      inst().log(Level.WARNING,
          "Config value \"" + path + "\" was too large (" + value + "), clamping to " + Integer.MAX_VALUE + ".");
      return Integer.MAX_VALUE;
    }

    return (int) value;
  }

  private void ensureConfigUpToDate() {
    if (!getDataFolder().exists() && !getDataFolder().mkdirs()) {
      log(Level.WARNING, "Could not create plugin data folder at " + getDataFolder().getAbsolutePath());
    }

    File configFile = new File(getDataFolder(), CONFIG_FILE_NAME);
    if (!configFile.exists()) {
      saveResource(CONFIG_FILE_NAME, false);
      reloadConfig();
      log(Level.INFO, "Created default config.yml (version " + CONFIG_VERSION + ").");
      return;
    }

    YamlConfiguration currentConfig = YamlConfiguration.loadConfiguration(configFile);
    int currentVersion = currentConfig.getInt(CONFIG_VERSION_PATH, 0);

    if (currentVersion == CONFIG_VERSION) {
      reloadConfig();
      log(Level.INFO, "Loaded config.yml version " + currentVersion + ".");
      return;
    }

    File backupFile = backupConfig(configFile, currentVersion);
    saveResource(CONFIG_FILE_NAME, true);
    reloadConfig();

    log(Level.INFO,
        "Updated config.yml from version " + currentVersion + " to " + CONFIG_VERSION
            + ". Backup saved to " + backupFile.getAbsolutePath());
  }

  private File backupConfig(File configFile, int currentVersion) {
    File backupDirectory = new File(getDataFolder(), "config-backups");
    if (!backupDirectory.exists() && !backupDirectory.mkdirs()) {
      throw new IllegalStateException("Could not create config backup directory at " + backupDirectory.getAbsolutePath());
    }

    String versionLabel = currentVersion > 0 ? String.valueOf(currentVersion) : "legacy";
    String timestamp = LocalDateTime.now().format(CONFIG_BACKUP_FORMAT);
    File backupFile = new File(backupDirectory, "config-v" + versionLabel + "-" + timestamp + ".yml");

    try {
      Files.copy(configFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      return backupFile;
    } catch (IOException e) {
      throw new IllegalStateException("Could not back up config.yml to " + backupFile.getAbsolutePath(), e);
    }
  }

}
