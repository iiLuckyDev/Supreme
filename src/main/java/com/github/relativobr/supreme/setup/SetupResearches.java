package com.github.relativobr.supreme.setup;

import com.github.relativobr.supreme.Supreme;
import com.github.relativobr.supreme.util.ItemGroups;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.NamespacedKey;

public final class SetupResearches {

  private static final int BASE_RESEARCH_ID = 9500;

  private SetupResearches() {}

  public static void setup(Supreme supreme) {
    List<SlimefunItem> supremeItems = Slimefun.getRegistry().getAllSlimefunItems().stream()
        .filter(item -> item.getAddon().getJavaPlugin() == supreme)
        .filter(item -> item.getId().startsWith("SUPREME_"))
        .collect(Collectors.toList());

    Set<String> assigned = new HashSet<>();

    registerResearch(supreme, supremeItems, assigned, "industrial_alloys", BASE_RESEARCH_ID + 1,
        "Industrial Alloys", 8,
        item -> inGroup(item, ItemGroups.COMPONENTS_CATEGORY)
            && (hasAnyIdToken(item, "ALLOY_", "_PLATE", "SYNTHETIC_")
            || isId(item, "SUPREME_THORNERITE")));

    registerResearch(supreme, supremeItems, assigned, "machine_parts", BASE_RESEARCH_ID + 2,
        "Machine Parts", 12,
        item -> inGroup(item, ItemGroups.COMPONENTS_CATEGORY)
            && hasAnyIdToken(item, "INDUCTIVE_MACHINE", "INDUCTOR_MACHINE", "RUSTLESS_MACHINE",
            "STAINLESS_MACHINE", "CARRIAGE_MACHINE", "CONVEYANCE_MACHINE",
            "PETRIFIER_MACHINE", "CRYSTALLIZER_MACHINE", "BLEND_MACHINE"));

    registerResearch(supreme, supremeItems, assigned, "core_fabrication", BASE_RESEARCH_ID + 3,
        "Core Fabrication", 16,
        item -> inGroup(item, ItemGroups.RESOURCE_CATEGORY) && item.getId().startsWith("SUPREME_CORE_"));

    registerResearch(supreme, supremeItems, assigned, "elemental_essences", BASE_RESEARCH_ID + 4,
        "Elemental Essences", 18,
        item -> item.getId().startsWith("SUPREME_CETRUS_"));

    registerResearch(supreme, supremeItems, assigned, "thornium_processing", BASE_RESEARCH_ID + 5,
        "Thornium Processing", 24,
        item -> hasAnyIdToken(item, "SUPREME_THORNIUM_", "SUPREME_DUST_NETHERITE",
            "SUPREME_DUST_GLOW_INK", "SUPREME_DUST_AMETHYST"));

    registerResearch(supreme, supremeItems, assigned, "supreme_synthesis", BASE_RESEARCH_ID + 6,
        "Supreme Synthesis", 32,
        item -> isId(item, "SUPREME_SUPREME_NUGGET") || isId(item, "SUPREME_SUPREME"));

    registerResearch(supreme, supremeItems, assigned, "mob_tech_foundations", BASE_RESEARCH_ID + 7,
        "Mob-Tech Foundations", 20,
        item -> inGroup(item, ItemGroups.CARDS_CATEGORY)
            || hasAnyIdToken(item, "SUPREME_CENTER_CARD_"));

    registerResearch(supreme, supremeItems, assigned, "genetic_engineering", BASE_RESEARCH_ID + 8,
        "Genetic Engineering", 28,
        item -> inGroup(item, ItemGroups.TECHMOB_CATEGORY)
            || hasAnyIdToken(item, "SUPREME_GENE_", "SUPREME_EMPTY_MOBTECH"));

    registerResearch(supreme, supremeItems, assigned, "electric_foundations", BASE_RESEARCH_ID + 9,
        "Electric Foundations", 16,
        item -> inGroup(item, ItemGroups.ELECTRIC_CATEGORY) && (
            hasAnyIdToken(item, "BASIC_", "AURUM_CAPACITOR", "TITANIUM_CAPACITOR", "GENERATOR_MOB_BASIC")
                || isId(item, "SUPREME_GENERATOR_MOB_MEDIUM")));

    registerResearch(supreme, supremeItems, assigned, "electric_engineering", BASE_RESEARCH_ID + 10,
        "Electric Engineering", 28,
        item -> inGroup(item, ItemGroups.ELECTRIC_CATEGORY) && !hasAnyIdToken(item,
            "THORNIUM_GENERATOR", "THORNIUM_CAPACITOR", "GENERATOR_MOB_ADVANCED")
            && !isId(item, "SUPREME_SUPREME_GENERATOR")
            && !isId(item, "SUPREME_SUPREME_CAPACITOR"));

    registerResearch(supreme, supremeItems, assigned, "apex_power", BASE_RESEARCH_ID + 11,
        "Apex Power", 42,
        item -> inGroup(item, ItemGroups.ELECTRIC_CATEGORY) && (
            hasAnyIdToken(item, "THORNIUM_GENERATOR", "THORNIUM_CAPACITOR", "GENERATOR_MOB_ADVANCED")
                || isId(item, "SUPREME_SUPREME_GENERATOR")
                || isId(item, "SUPREME_SUPREME_CAPACITOR")));

    registerResearch(supreme, supremeItems, assigned, "automation_basics", BASE_RESEARCH_ID + 12,
        "Automation Basics", 18,
        item -> inGroup(item, ItemGroups.MACHINES_CATEGORY)
            && !hasAnyIdToken(item, "SUPREME_TECH_", "SUPREME_MOB_TECH_")
            && (endsWithAny(item, "_I", "_MACHINE")
            || hasAnyIdToken(item, "STONE_QUARRY", "COAL_QUARRY", "IRON_QUARRY",
            "ITEM_CONVERTER_MACHINE", "CHECK_INVENTORY")));

    registerResearch(supreme, supremeItems, assigned, "automation_advanced", BASE_RESEARCH_ID + 13,
        "Automation Expansion", 30,
        item -> inGroup(item, ItemGroups.MACHINES_CATEGORY)
            && !hasAnyIdToken(item, "SUPREME_TECH_", "SUPREME_MOB_TECH_")
            && (endsWithAny(item, "_II")
            || hasAnyIdToken(item, "GOLD_QUARRY", "DIAMOND_QUARRY")));

    registerResearch(supreme, supremeItems, assigned, "automation_mastery", BASE_RESEARCH_ID + 14,
        "Automation Mastery", 45,
        item -> inGroup(item, ItemGroups.MACHINES_CATEGORY)
            && !hasAnyIdToken(item, "SUPREME_TECH_", "SUPREME_MOB_TECH_")
            && (endsWithAny(item, "_III")
            || hasAnyIdToken(item, "THORNIUM_QUARRY", "SUPREME_NUGGETS_QUARRY", "MULTIBLOCK_")));

    registerResearch(supreme, supremeItems, assigned, "bio_automation", BASE_RESEARCH_ID + 15,
        "Bio Automation", 36,
        item -> hasAnyIdToken(item, "SUPREME_TECH_", "SUPREME_MOB_TECH_"));

    registerResearch(supreme, supremeItems, assigned, "field_tools", BASE_RESEARCH_ID + 16,
        "Field Tools", 18,
        item -> inAnyGroup(item, ItemGroups.TOOLS_CATEGORY, ItemGroups.ARMOR_CATEGORY, ItemGroups.WEAPONS_CATEGORY)
            && !hasAnyIdToken(item, "MAGIC", "RARE", "EPIC", "LEGENDARY", "_SUPREME"));

    registerResearch(supreme, supremeItems, assigned, "thornium_masterworks", BASE_RESEARCH_ID + 17,
        "Thornium Masterworks", 34,
        item -> inAnyGroup(item, ItemGroups.TOOLS_CATEGORY, ItemGroups.ARMOR_CATEGORY, ItemGroups.WEAPONS_CATEGORY)
            && hasAnyIdToken(item, "MAGIC", "RARE", "EPIC", "LEGENDARY", "_SUPREME"));

    List<SlimefunItem> unassigned = supremeItems.stream()
        .filter(item -> !assigned.contains(item.getId()))
        .collect(Collectors.toList());

    if (!unassigned.isEmpty()) {
      Research research = new Research(new NamespacedKey(supreme, "supreme_miscellany"),
          BASE_RESEARCH_ID + 18, "Supreme Miscellany", 24);
      research.addItems(unassigned.toArray(new SlimefunItem[0]));
      research.register();

      supreme.log(Level.WARNING,
          "Assigned " + unassigned.size() + " leftover Supreme items to the fallback research \"Supreme Miscellany\".");
    }
  }

  private static void registerResearch(Supreme supreme, List<SlimefunItem> items, Set<String> assigned,
      String key, int id, String name, int cost, Predicate<SlimefunItem> predicate) {
    List<SlimefunItem> researchItems = items.stream()
        .filter(item -> !assigned.contains(item.getId()))
        .filter(predicate)
        .collect(Collectors.toList());

    if (researchItems.isEmpty()) {
      return;
    }

    Research research = new Research(new NamespacedKey(supreme, key), id, name, cost);
    research.addItems(researchItems.toArray(new SlimefunItem[0]));
    research.register();

    researchItems.stream().map(SlimefunItem::getId).forEach(assigned::add);
  }

  private static boolean inGroup(SlimefunItem item, ItemGroup group) {
    return item.getItemGroup() == group;
  }

  private static boolean inAnyGroup(SlimefunItem item, ItemGroup... groups) {
    for (ItemGroup group : groups) {
      if (inGroup(item, group)) {
        return true;
      }
    }

    return false;
  }

  private static boolean isId(SlimefunItem item, String id) {
    return item.getId().equals(id);
  }

  private static boolean endsWithAny(SlimefunItem item, String... suffixes) {
    String id = item.getId();
    for (String suffix : suffixes) {
      if (id.endsWith(suffix)) {
        return true;
      }
    }

    return false;
  }

  private static boolean hasAnyIdToken(SlimefunItem item, String... tokens) {
    String id = item.getId();
    for (String token : tokens) {
      if (id.contains(token)) {
        return true;
      }
    }

    return false;
  }
}
