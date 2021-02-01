package io.github.addoncommunity.galactifun.api.universe.populators;

import io.github.addoncommunity.galactifun.Galactifun;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.SlimefunItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


/**
 * Populator utility for surface boulders
 *
 * @author GallowsDove
 * @author Seggan
 *
 */
public final class GalacticBoulderPopulator extends BlockPopulator {
    private final int attempts;
    private final int chance;
    @Nonnull private final Material ore;
    @Nullable private final String id;
    @Nonnull private final List<Material> source;


    public GalacticBoulderPopulator(int attempts, int chance, @Nonnull SlimefunItemStack slimefunItem,
                                    @Nonnull Material... source) {
        this.attempts = attempts;
        this.chance = chance;
        this.ore = slimefunItem.getType();
        this.id = slimefunItem.getItemId();
        this.source = new ArrayList<>(Arrays.asList(source));
    }

    public GalacticBoulderPopulator(int attempts, int chance, @Nonnull Material ore, @Nonnull Material... source) {
        this.attempts = attempts;
        this.chance = chance;
        this.ore = ore;
        this.id = null;
        this.source = new ArrayList<>(Arrays.asList(source));
    }

    @Override
    public void populate(@Nonnull World world, @Nonnull Random random, @Nonnull Chunk chunk) {
        for (int i = 0; i < attempts; i++) {
            if (random.nextInt(100) < chance) {

                int x = random.nextInt(16);
                int z = random.nextInt(16);

                Block b = world.getHighestBlockAt((chunk.getX() << 4) + x, (chunk.getZ() << 4) + z);

                if (source.contains(b.getType())) {
                    b.getRelative(BlockFace.UP).setType(ore);

                    if (id != null) {
                        final int fx = x;
                        final int fy = b.getRelative(BlockFace.UP).getY();
                        final int fz = z;

                        // Cam produce concurrentModificationException error, currently non-avoidable
                        Bukkit.getScheduler().runTask(Galactifun.getInstance(),
                                () -> BlockStorage.store(chunk.getBlock(fx, fy, fz), id));
                    }
                }


            }
        }
    }
}