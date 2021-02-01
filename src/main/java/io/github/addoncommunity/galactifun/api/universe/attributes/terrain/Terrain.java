package io.github.addoncommunity.galactifun.api.universe.attributes.terrain;

import io.github.addoncommunity.galactifun.api.universe.attributes.terrain.features.TerrainFeature;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Defines the terrain of a celestial object
 * 
 * @author Mooy1
 * 
 */
public final class Terrain {

    public static final Terrain HILLY_CAVERNS = new Terrain( "Hilly Caverns",
            40, 35, 8, 0.01, .5, .5, TerrainFeature.CAVERNS
    );
    
    public static final Terrain SMOOTH = new Terrain( "Smooth",
            20, 45, 8,0.01, .5, .5
    );

    /**
     * Short description of the terrain
     */
    @Nonnull @Getter private final String desc;

    /**
     * Maximum y deviation
     */
    private final int maxDeviation;

    /**
     * Minimum y value
     */
    private final int minHeight;

    /**
     * Octave generator octaves
     */
    private final int octaves;

    /**
     * Octave generator scale
     */
    private final double scale;

    /**
     * noise amplitude
     */
    private final double amplitude;
    
    /**
     * noise frequency
     */
    private final double frequency;

    /**
     * Features
     */
    @Nonnull private final TerrainFeature[] features;

    public Terrain(@Nonnull String desc, int maxDeviation, int minHeight, int octaves, double scale, double amplitude, double frequency, @Nonnull TerrainFeature... features) {
        this.desc = desc;
        this.maxDeviation = maxDeviation;
        this.minHeight = minHeight;
        this.octaves = octaves;
        this.scale = scale;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.features = features;
    }

    /**
     * Creates a new ChunkGenerator based on this terrain
     */
    @Nonnull
    public ChunkGenerator createGenerator(@Nonnull BiomeSupplier biomeSupplier,
                                          @Nonnull MaterialSupplier materialSupplier,
                                          @Nonnull PopulatorSupplier populatorSupplier) {
        return new ChunkGenerator() {
            @Nonnull
            @Override
            public ChunkData generateChunkData(@Nonnull World world, @Nonnull Random random, int chunkX, int chunkZ, @Nonnull BiomeGrid grid) {
                ChunkData chunk = createChunkData(world);
                SimplexOctaveGenerator generator = new SimplexOctaveGenerator(world, Terrain.this.octaves);
                generator.setScale(Terrain.this.scale);

                int height;
                int startX = chunkX << 4;
                int startZ = chunkZ << 4;

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        chunk.setBlock(x, 0, z, Material.BEDROCK);

                        int realX = startX + x;
                        int realZ = startZ + z;

                        // find max height
                        height = (int) (Terrain.this.minHeight + Terrain.this.maxDeviation * (1 + generator.noise(
                                realX,
                                realZ,
                                Terrain.this.frequency,
                                Terrain.this.amplitude,
                                true)
                        ));

                        // features
                        for (TerrainFeature feature : Terrain.this.features) {
                            feature.generate(generator, chunk, realX, realZ, x, z, height);
                        }

                        // generate the rest
                        for (int y = 1 ; y < height ; y++) {
                            if (chunk.getType(x, y, z) == Material.AIR) {
                                chunk.setBlock(x, y, z, materialSupplier.get(random, height, x, y, z));
                            }
                        }

                        // set biome
                        Biome biome = biomeSupplier.get(random, chunkX, chunkZ);
                        for (int y = 0 ; y < 256 ; y++) {
                            grid.setBiome(x, y, z, biome);
                        }
                    }
                }
                
                return chunk;
            }

            @Nonnull
            @Override
            public List<BlockPopulator> getDefaultPopulators(@Nonnull World world) {
                List<BlockPopulator> list = new ArrayList<>(4);
                populatorSupplier.get(list);
                return list;
            }
        };
    }

    @FunctionalInterface
    public interface PopulatorSupplier {
        void get(@Nonnull List<BlockPopulator> populators);
    }
    
    @FunctionalInterface
    public interface MaterialSupplier {
        @Nonnull Material get(@Nonnull Random random, int top, int x, int y, int z);
    }

    @FunctionalInterface
    public interface BiomeSupplier {
        @Nonnull Biome get(@Nonnull Random random, int chunkX, int chunkZ);
    }
    
}