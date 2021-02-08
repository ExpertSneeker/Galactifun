package io.github.addoncommunity.galactifun.api.universe.world;

import io.github.addoncommunity.galactifun.api.universe.world.features.TerrainFeature;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import javax.annotation.Nonnull;
import java.util.Random;

/**
 * Defines the terrain of a celestial world, default implementation
 * 
 * @author Mooy1
 * @author Seggan
 * 
 */
public class Terrain extends AbstractTerrain {

    public static final Terrain HILLY_CAVERNS = new Terrain( "Hilly Caverns",
            40, 8, 0.01, .5, .5, TerrainFeature.CAVERNS
    );
    public static final Terrain SMOOTH = new Terrain( "Smooth",
            15, 8,0.01, .5, .5
    );

    /**
     * Maximum y deviation
     */
    protected final double maxDeviation;

    /**
     * Octave generator octaves
     */
    protected final int octaves;

    /**
     * Octave generator scale
     */
    protected final double scale;

    /**
     * noise amplitude
     */
    protected final double amplitude;
    
    /**
     * noise frequency
     */
    protected final double frequency;

    /**
     * Features
     */
    @Nonnull
    protected final TerrainFeature[] features;

    public Terrain(@Nonnull String name, int maxDeviation, int octaves, double scale, double amplitude,
                   double frequency, @Nonnull TerrainFeature... features) {
        super(name);
        this.maxDeviation = maxDeviation;
        this.octaves = octaves;
        this.scale = scale;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.features = features;
    }
    @Override
    protected void generateChunk(@Nonnull CelestialWorld celestialWorld, int chunkX, int chunkZ, @Nonnull Random random,
                                 @Nonnull ChunkGenerator.ChunkData chunk, @Nonnull ChunkGenerator.BiomeGrid grid, @Nonnull World world) {
        
        SimplexOctaveGenerator generator = new SimplexOctaveGenerator(world, this.octaves);
        generator.setScale(this.scale);
        
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int height;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {

                int realX = startX + x;
                int realZ = startZ + z;
                
                // find max height
                height = (int) Math.floor(celestialWorld.getAvgHeight() + this.maxDeviation * 
                        generator.noise(realX, realZ, this.frequency, this.amplitude, true)
                );
                
                // features
                for (TerrainFeature feature : this.features) {
                    feature.generate(generator, chunk, realX, realZ, x, z, height);
                }
                
                // bedrock
                chunk.setBlock(x, 0, z, Material.BEDROCK);
                
                // generate the rest
                for (int y = 1 ; y <= height ; y++) {
                    if (chunk.getType(x, y, z) == Material.AIR) {
                        chunk.setBlock(x, y, z, celestialWorld.generateBlock(random, height, x, y, z));
                    }
                }

                // set biome
                for (int y = 0 ; y < 256 ; y++) {
                    celestialWorld.generateBiome(grid, chunkX, y, chunkZ);
                }
            }
        }
    }

}
