package com.github.relativobr.supreme.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
public class SupremeOptions {

    boolean autoUpdate;
    boolean useLegacySupremeexpansionItemId;
    String lang;
    int customTickerDelay;
    boolean enableGenerators;
    boolean debugGenerators;
    boolean limitProductionGenerators;
    int delayTimeValidGenerators;
    int baseTimeVirtualGarden;
    int baseTimeVirtualAquarium;
    int baseTimeMobCollector;
    int baseTimeTechGenerator;
    int maxAmountTechGenerator;
    boolean mobTechEnableBee;
    boolean mobTechEnableIronGolem;
    boolean mobTechEnableZombie;
    boolean enableQuarry;
    boolean limitProductionQuarry;
    boolean enableWeapons;
    boolean enableTools;
    boolean enableArmor;
    boolean enableTech;
    boolean enableItemConverter;
    List<String> itemConverterBlacklist;
    boolean customBc;
    int machineMaxAttemptConsumed;

    public static SupremeOptions defaultValue() {
        return SupremeOptions.builder()
                .autoUpdate(true)
                .useLegacySupremeexpansionItemId(false)
                .lang("en-US")
                .customTickerDelay(2)
                .enableGenerators(true)
                .debugGenerators(false)
                .limitProductionGenerators(false)
                .delayTimeValidGenerators(600)
                .enableQuarry(true)
                .limitProductionQuarry(false)
                .baseTimeVirtualGarden(15)
                .baseTimeVirtualAquarium(15)
                .baseTimeMobCollector(15)
                .baseTimeTechGenerator(1800)
                .maxAmountTechGenerator(64)
                .mobTechEnableBee(true)
                .mobTechEnableIronGolem(true)
                .mobTechEnableZombie(true)
                .enableWeapons(true)
                .enableTools(true)
                .enableArmor(true)
                .enableTech(true)
                .enableItemConverter(true)
                .itemConverterBlacklist(Arrays.asList("SUPREME_ITEM_CONVERTER_MACHINE"))
                .customBc(false)
                .machineMaxAttemptConsumed(30)
                .itemConverterBlacklist(List.of("SUPREME_ITEM_CONVERTER_MACHINE"))
                .build();
    }
}
