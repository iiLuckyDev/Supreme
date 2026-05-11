package com.github.relativobr.supreme.generic.electric;

import javax.annotation.Nonnull;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

@AllArgsConstructor
public enum GenerationType {

    SKY("Sky") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            switch (world.getEnvironment()) {
                case NORMAL: {
                    if (block.getRelative(BlockFace.UP).getLightFromSky() == 15) {
                        return def;
                    }
                    return 0;
                }
                case NETHER:
                case THE_END:
                default:
                    return 0;
            }
        }
    },
    FIRE("Fire") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            Material material = block.getRelative(BlockFace.DOWN).getType();
            if (material == Material.FIRE
                    || material == Material.SOUL_FIRE
                    || material == Material.LAVA
                    || material == Material.CAMPFIRE
                    || material == Material.SOUL_CAMPFIRE) {
                return def;
            }
            return 0;
        }
    },
    WATER("Water") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            Material material = block.getRelative(BlockFace.DOWN).getType();
            if (material == Material.WATER
                    || material == Material.WATER_CAULDRON) {
                return def;
            }
            return 0;
        }
    },
    WIND("Wind") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            switch (world.getEnvironment()) {
                case NETHER:
                case NORMAL: {
                    if (block.getRelative(BlockFace.EAST).getType() == Material.AIR
                            || block.getRelative(BlockFace.WEST).getType() == Material.AIR
                            || block.getRelative(BlockFace.SOUTH).getType() == Material.AIR
                            || block.getRelative(BlockFace.NORTH).getType() == Material.AIR) {
                        return def;
                    }
                    return 0;
                }
                case THE_END:
                default:
                    return 0;
            }
        }
    },
    DARK("Dark") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            switch (world.getEnvironment()) {
                case NETHER:
                case THE_END:
                    return def;
                case NORMAL:
                    if (block.getRelative(BlockFace.UP).getLightFromSky() != 15) {
                        return def;
                    }
                    return 0;
                default:
                    return 0;
            }
        }
    },
    EVERY("Every") {
        @Override
        protected int generate(@Nonnull World world, @Nonnull Block block, int def) {
            return def;
        }
    };

    @Getter
    private final String toString;

    protected abstract int generate(@Nonnull World world, @Nonnull Block block, int def);

    protected boolean usesValidationCache() {
        return this == EVERY;
    }

    protected boolean usesNetworkBuffer() {
        return this == EVERY;
    }

}
