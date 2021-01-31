package io.github.addoncommunity.galactifun.base.milkyway.solarsystem;

import io.github.addoncommunity.galactifun.api.universe.Planet;
import io.github.addoncommunity.galactifun.api.universe.attributes.Atmosphere;
import io.github.addoncommunity.galactifun.api.universe.attributes.DayCycle;
import io.github.addoncommunity.galactifun.api.universe.attributes.Gravity;
import io.github.addoncommunity.galactifun.api.universe.attributes.Terrain;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

/**
 * Mars
 * 
 * @author Seggan
 * @author Mooy1
 */
public final class Mars extends Planet {
    
    public Mars() {
        super("Mars", 144_610_000L, 55_910_000L, new Gravity(.378), new DayCycle(1.03),
                new Atmosphere(0, false, false, false, false, World.Environment.NETHER),
                Terrain.HILLY_CAVERNS);
    }
    
    @Nonnull
    @Override
    protected Material generateBlock(@Nonnull Random random, int top, int x, int y, int z) {
        // top 4 blocks
        if (y > top - 4) {
            return Material.RED_SAND;
        }
        if (random.nextDouble() > 0.1 || y > 40) {
            // 90% of blocks are terracotta
            return Material.TERRACOTTA;
        } else {
            if (y > 15) {
                // Blue ice is the other 10% or if y > 15
                return Material.BLUE_ICE;
            } else {
                // Otherwise iron ore
                return Material.IRON_ORE;
            }
        }
    }

    @Nonnull
    @Override
    protected Biome getBiome(@Nonnull Random random, int chunkX, int chunkZ) {
        return Biome.NETHER_WASTES;
    }

    @Override
    protected void getPopulators(@Nonnull List<BlockPopulator> populators) {
        populators.add(new BlockPopulator() {

            // boulder populator
            @Override
            public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {
                if (random.nextBoolean()) {
                    int x = random.nextInt(16);
                    int z = random.nextInt(16);

                    Block b = world.getHighestBlockAt((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);

                    if (b.getType() == Material.GRANITE) return;

                    b.getRelative(BlockFace.UP).setType(Material.GRANITE);
                }
            }
        });
    }

}