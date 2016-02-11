package toast.mobProperties.entry;

import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.biome.BiomeGenBase;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties._MobPropertiesMod;
import toast.mobProperties.api.DropEntry;

import com.google.gson.JsonObject;

public class PropertyGroupConditional extends PropertyGroup {
    // The method to call for difficulty checks, if found.
    private static Method worldDifficultyMethod;

    static {
        try {
            PropertyGroupConditional.worldDifficultyMethod = Class.forName("toast.apocalypse.WorldDifficultyManager").getMethod("getWorldDifficulty");
            _MobPropertiesMod.console("Successfully hooked into Apocalypse's world difficulty!");
        }
        catch (Exception ex) {
            // Do nothing
        }
    }

    // Returns true if the category can be executed.
    public static boolean isCategoryActive(boolean invert, String category, EntityLivingBase entity) {
        return PropertyGroupConditional.isCategoryActive(invert, category, entity, null, 0, false, false, false);
    }
    public static boolean isCategoryActive(boolean invert, String category, EntityLivingBase entity, DamageSource source, int looting, boolean recentlyHit, boolean rare, boolean superRare) {
		// Drops only
        if (category.equals("recently_hit"))
            return recentlyHit != invert;
        if (category.equals("rare"))
            return rare != invert;
        if (category.equals("rare_super"))
            return superRare != invert;
        if (category.startsWith("above_looting_")) {
            try {
                return looting > Integer.parseInt(category.substring(14)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("damage_type_")) {
            try {
            	// Vanilla damage types: "inFire", "onFire", "lava", "inWall", "drown", "starve", "cactus", "fall", "outOfWorld",
            	// "generic", "magic", "wither", "anvil", "fallingBlock", "mob", "player", "arrow", "fireball", "thrown",
            	// "indirectMagic", "thorns", "explosion.player", "explosion"
                return source.getDamageType() == category.substring(12) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_fire")) {
            try {
                return source.isFireDamage() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_magic")) {
            try {
                return source.isMagicDamage() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_projectile")) {
            try {
                return source.isProjectile() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_indirect")) {
            try {
                return source.getEntity() != source.getSourceOfDamage() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_explosion")) {
            try {
                return source.isExplosion() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killed_with_unblockable")) {
            try {
                return source.isUnblockable() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("killed_by_")) {
            try {
                return ((Class) EntityList.stringToClassMapping.get(category.substring(10))).isAssignableFrom(source.getEntity().getClass()) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killer_on_ground")) {
            try {
            	return source.getEntity().onGround != invert;
            }
	        catch (Exception ex) {
	            // Do nothing
	        }
	        return invert;
    	}
        if (category.equals("killer_burning")) {
            try {
            	return source.getEntity().isBurning() != invert;
            }
	        catch (Exception ex) {
	            // Do nothing
	        }
	        return invert;
    	}
        if (category.equals("killer_wet")) {
            try {
            	return source.getEntity().isWet() != invert;
            }
	        catch (Exception ex) {
	            // Do nothing
	        }
	        return invert;
    	}
        if (category.equals("killer_submerged")) {
            try {
            	return (source.getEntity().isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier((EntityLivingBase) source.getEntity())) != invert;
            }
	        catch (Exception ex) {
	            // Do nothing
	        }
	        return invert;
    	}
        if (category.startsWith("killer_has_potion_")) {
            try {
                return ((EntityLivingBase) source.getEntity()).isPotionActive(FileHelper.readPotion(category.substring(18), entity.getClass().getName() + "\\killer", true)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("check_killer_nbt_")) {
            try {
                String[] path = category.substring(17).split("/");
                String[] data = PropertyGroupConditional.getOperatorData(path[path.length - 1]);
                path[path.length - 1] = data[0];

                return PropertyGroupConditional.compareNBT(source.getEntity(), path, data) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.equals("killer_wither_skeleton")) {
            try {
            	return ((EntitySkeleton) source.getEntity()).getSkeletonType() == 1 != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.equals("on_ground"))
            return entity.onGround != invert;
        if (category.equals("burning"))
            return entity.isBurning() != invert;
        if (category.equals("wet"))
            return entity.isWet() != invert;
        if (category.equals("submerged"))
            return (entity.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(entity)) != invert;
        if (category.startsWith("has_potion_")) {
            try {
                return entity.isPotionActive(FileHelper.readPotion(category.substring(11), entity.getClass().getName(), true)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("check_nbt_")) {
            try {
                String[] path = category.substring(10).split("/");
                String[] data = PropertyGroupConditional.getOperatorData(path[path.length - 1]);
                path[path.length - 1] = data[0];

                return PropertyGroupConditional.compareNBT(entity, path, data) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.equals("wither_skeleton"))
            return (entity instanceof EntitySkeleton && ((EntitySkeleton) entity).getSkeletonType() == 1) != invert;

        if (category.equals("raining"))
            return entity.worldObj.isRaining() != invert;
        if (category.equals("thundering"))
            return entity.worldObj.isThundering() != invert;
        if (category.equals("can_see_sky"))
            return entity.worldObj.canBlockSeeTheSky((int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ)) != invert;
        if (category.startsWith("moon_phase_"))
            return entity.worldObj.provider.getMoonPhase(entity.worldObj.getWorldTime()) == PropertyGroupConditional.getMoonPhaseId(category.substring(11)) != invert;
        if (category.startsWith("beyond_")) {
            try {
                double distance = Double.parseDouble(category.substring(7));
                ChunkCoordinates spawnPoint = entity.worldObj.getSpawnPoint();
                return entity.getDistanceSq(spawnPoint.posX, spawnPoint.posY, spawnPoint.posZ) > distance * distance != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("difficulty_"))
            return entity.worldObj.difficultySetting == PropertyGroupConditional.getDifficulty(category.substring(11)) != invert;
        if (category.startsWith("past_world_difficulty_")) {
            try {
                long difficulty = (long) (Double.parseDouble(category.substring(22)) * 24000L);
                if (PropertyGroupConditional.worldDifficultyMethod == null) {
                    category = "past_world_time_" + difficulty;
                }
                else
                    return ((Long) PropertyGroupConditional.worldDifficultyMethod.invoke(null)).longValue() > difficulty != invert;
            }
            catch (Exception ex) {
                return invert;
            }
        }
        if (category.startsWith("past_day_time_")) {
            try {
                return (int) (entity.worldObj.getWorldInfo().getWorldTime() % 24000L) > Integer.parseInt(category.substring(14)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("past_world_time_")) {
            try {
                return entity.worldObj.getWorldInfo().getWorldTime() > Long.parseLong(category.substring(16)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("in_dimension_")) {
            try {
            	if (entity.worldObj != null)
            		return entity.worldObj.provider.dimensionId == Integer.parseInt(category.substring(13)) != invert;
                return entity.dimension == Integer.parseInt(category.substring(13)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("on_block_")) {
            try {
                Block block = FileHelper.readBlock(category.substring(9), entity.getClass().getName(), true);
                int x = (int) Math.floor(entity.posX);
                int yMin = (int) Math.floor(entity.posY) - 1;
                int yMax = (int) Math.floor(entity.posY) + (int) Math.floor(entity.height);
                int z = (int) Math.floor(entity.posZ);
                for (int y = yMin; y <= yMax; y++)
                    if (entity.worldObj.getBlock(x, y, z) == block != invert)
                        return true;
                return invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("below_")) {
            try {
                return (int) entity.posY < Integer.parseInt(category.substring(6)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.startsWith("in_biome_")) {
            try {
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)) == PropertyGroupConditional.getBiome(category.substring(9)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("in_biome_type_")) {
            try {
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).isEqualTo(PropertyGroupConditional.getBiomeType(category.substring(14))) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("biome_temp_")) {
            try {
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).getTempCategory() == PropertyGroupConditional.getTempCategory(category.substring(11)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("biome_height_below_")) {
            try {
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).rootHeight < PropertyGroupConditional.getBiomeHeight(category.substring(19)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("is_humid")) {
            try {
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).isHighHumidity() != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("rainfall_above_")) {
            try {
            	// Rainfall benchmarks: 0.0 is no rain, 0.5 is default, >0.85 is considered humid (affects fire), 1.0 is the max set by vanilla
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).rainfall > Float.parseFloat(category.substring(15)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("biome_temp_above_")) {
            try {
            	// Temperature benchmarks: -0.5 is the vanilla min, 0.2 is freezing point, 0.5 is default, 2.0 is the vanilla max
            	// Temmperature ranges: (-0.5) cold (0.1) invalid/cold (0.2) medium (1.0) hot (2.0)
                return entity.worldObj.getBiomeGenForCoords((int) Math.floor(entity.posX), (int) Math.floor(entity.posZ)).temperature > Float.parseFloat(category.substring(17)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        if (category.startsWith("temp_above_")) {
            try {
            	// At or below 64 blocks, the actual temp is the same as the biome temp
            	// For each block above 64, the biome temperature is reduced by 0.0016666...
            	// Everywhere above 64 blocks, the height used to calculate temp reduction is increased by a random number of blocks
                int x = (int) Math.floor(entity.posX);
                int z = (int) Math.floor(entity.posZ);
                return entity.worldObj.getBiomeGenForCoords(x, z).getFloatTemperature(x, (int) Math.floor(entity.posY), z) > Float.parseFloat(category.substring(11)) != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }

        if (category.startsWith("player_online_")) {
            try {
                return entity.worldObj.getPlayerEntityByName(category.substring(14)) != null != invert;
            }
            catch (Exception ex) {
                // Do nothing
            }
            return invert;
        }
        throw new RuntimeException("[ERROR] Conditional property has invalid condition! for " + entity.getClass().getName());
    }

    // Returns a string array with the path end for the left-hand operand, the operator string, and the right-hand operand.
    private static String[] getOperatorData(String last) {
        String[] operators = { "==", ">", "<", ">=", "<=" };
        String[] split;
        for (String operator : operators) {
            split = last.split(operator, 2);
            if (split.length == 2)
                return new String[] { split[0], operator, split[1] };
        }
        return new String[] { last, null, null };
    }

    // Compares the actual value given by the NBT path to a value, based on the operator string in the data array.
    private static boolean compareNBT(Entity entity, String[] path, String[] data) {
        // Step to tag at the end, tag becomes null if the target does not exist
        NBTBase tag = new NBTTagCompound();
        entity.writeToNBT((NBTTagCompound) tag);
        for (String pathStep : path) {
            if (tag instanceof NBTTagCompound) {
                if (!((NBTTagCompound) tag).hasKey(pathStep)) {
                    tag = null;
                    break;
                }
                tag = ((NBTTagCompound) tag).getTag(pathStep);
            }
            else if (tag instanceof NBTTagList) {
                int index = Integer.parseInt(pathStep);
                if (((NBTTagList) tag).tagCount() <= index) {
                    tag = null;
                    break;
                }
                // Only way to directly get an element from the list
                // The entity is not read from the tag again, anyway
                tag = ((NBTTagList) tag).removeTag(index);
            }
            else
                return false;
        }

        // Compare the actual to the value
        if (data[1] == null) // boolean check
            return tag != null && ((NBTBase.NBTPrimitive) tag).func_150290_f() == 1;
        double value;
        try {
        	value = Double.parseDouble(data[2]);
        }
        catch (NumberFormatException ex) { // String check
        	return data[1].equals("==") && data[2].equals(((NBTTagString) tag).func_150285_a_());
        }
        double actual;
        if (tag == null) {
            actual = 0.0;
        }
        else {
            actual = ((NBTBase.NBTPrimitive) tag).func_150286_g();
        }

        if (data[1].equals("=="))
            return actual == value;
        if (data[1].equals(">"))
            return actual > value;
        if (data[1].equals("<"))
            return actual < value;
        if (data[1].equals(">="))
            return actual >= value;
        if (data[1].equals("<="))
            return actual <= value;
        return false;
    }

    // Returns the moon phase id from the given string.
    private static int getMoonPhaseId(String phase) {
        if ("FULL".equalsIgnoreCase(phase))
            return 0;
        if ("WANING_GIBBOUS".equalsIgnoreCase(phase))
            return 1;
        if ("THIRD_QUARTER".equalsIgnoreCase(phase) || "WANING_HALF".equalsIgnoreCase(phase))
            return 2;
        if ("WANING_CRESCENT".equalsIgnoreCase(phase))
            return 3;
        if ("NEW".equalsIgnoreCase(phase))
            return 4;
        if ("WAXING_CRESCENT".equalsIgnoreCase(phase))
            return 5;
        if ("FIRST_QUARTER".equalsIgnoreCase(phase) || "WAXING_HALF".equalsIgnoreCase(phase))
            return 6;
        if ("WAXING_GIBBOUS".equalsIgnoreCase(phase))
            return 7;
        try {
            return Integer.parseInt(phase) % 8;
        }
        catch (Exception ex) {
            return -1;
        }
    }

    // Parses the world difficulty from a string.
    private static EnumDifficulty getDifficulty(String id) {
        if ("PEACEFUL".equalsIgnoreCase(id))
            return EnumDifficulty.PEACEFUL;
        if ("EASY".equalsIgnoreCase(id))
            return EnumDifficulty.EASY;
        if ("NORMAL".equalsIgnoreCase(id))
            return EnumDifficulty.NORMAL;
        if ("HARD".equalsIgnoreCase(id))
            return EnumDifficulty.HARD;
        try {
            return EnumDifficulty.getDifficultyEnum(Integer.parseInt(id));
        }
        catch (Exception ex) {
            return null;
        }
    }

    // Parses a biome from a string.
    private static BiomeGenBase getBiome(String id) {
    	for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
    		if (biome.biomeName.equals(id))
				return biome;
    	}
        try {
        	int biomeId = Integer.parseInt(id);
        	_MobPropertiesMod.console("[WARNING]  Usage of numerical biome id! (" + id + "=\"" + BiomeGenBase.getBiomeGenArray()[biomeId].biomeName + "\") in conditional check!");
            return BiomeGenBase.getBiomeGenArray()[biomeId];
        }
        catch (Exception ex) {
            return null;
        }
    }

    // Parses a biome from a string for use in the more relaxed biome check.
    private static BiomeGenBase getBiomeType(String id) {
    	if ("BEACH".equalsIgnoreCase(id))
			return BiomeGenBase.beach;
    	if ("DESERT".equalsIgnoreCase(id))
			return BiomeGenBase.desert;
    	if ("END".equalsIgnoreCase(id))
			return BiomeGenBase.sky;
    	if ("FOREST".equalsIgnoreCase(id))
			return BiomeGenBase.forest;
    	if ("HELL".equalsIgnoreCase(id))
			return BiomeGenBase.hell;
    	if ("HILLS".equalsIgnoreCase(id))
			return BiomeGenBase.extremeHills;
    	if ("JUNGLE".equalsIgnoreCase(id))
			return BiomeGenBase.jungle;
    	if ("MESA".equalsIgnoreCase(id))
			return BiomeGenBase.mesa;
    	if ("MUSHROOM".equalsIgnoreCase(id))
			return BiomeGenBase.mushroomIsland;
    	if ("OCEAN".equalsIgnoreCase(id))
			return BiomeGenBase.ocean;
    	if ("PLAINS".equalsIgnoreCase(id))
			return BiomeGenBase.plains;
    	if ("RIVER".equalsIgnoreCase(id))
			return BiomeGenBase.river;
    	if ("SAVANNA".equalsIgnoreCase(id))
			return BiomeGenBase.savanna;
    	if ("SNOW".equalsIgnoreCase(id))
			return BiomeGenBase.icePlains;
    	if ("STONE_BEACH".equalsIgnoreCase(id))
			return BiomeGenBase.stoneBeach;
    	if ("SWAMP".equalsIgnoreCase(id))
			return BiomeGenBase.swampland;
    	if ("TAIGA".equalsIgnoreCase(id))
			return BiomeGenBase.taiga;
    	return PropertyGroupConditional.getBiome(id);
    }

    // Parses the biome temperature category from a string.
    private static BiomeGenBase.TempCategory getTempCategory(String temp) {
    	if ("OCEAN".equalsIgnoreCase(temp))
			return BiomeGenBase.TempCategory.OCEAN;
		if ("COLD".equalsIgnoreCase(temp))
			return BiomeGenBase.TempCategory.COLD;
		if ("MEDIUM".equalsIgnoreCase(temp))
			return BiomeGenBase.TempCategory.MEDIUM;
		if ("WARM".equalsIgnoreCase(temp))
			return BiomeGenBase.TempCategory.WARM;
        return null;
    }

    // Parses a biome height level from a string.
    private static float getBiomeHeight(String height) {
        try {
            return Integer.parseInt(height) / 32.0F - 2.0F;
        }
        catch (Exception ex) {
        	// Do nothing
        }
    	if ("DEFAULT".equalsIgnoreCase(height))
    		return 0.1F;
    	if ("SHORES".equalsIgnoreCase(height))
    		return 0.0F;
    	if ("LOW_PLAINS".equalsIgnoreCase(height))
    		return 0.125F;
    	if ("MID_PLAINS".equalsIgnoreCase(height))
    		return 0.2F;
    	if ("LOW_HILLS".equalsIgnoreCase(height))
    		return 0.45F;
    	if ("MID_HILLS".equalsIgnoreCase(height))
    		return 1.0F;
    	if ("SHALLOW_WATERS".equalsIgnoreCase(height))
    		return -0.5F;
    	if ("OCEANS".equalsIgnoreCase(height))
    		return -1.0F;
    	if ("LOW_ISLANDS".equalsIgnoreCase(height))
    		return 0.2F;
    	if ("PARTLY_SUBMERGED".equalsIgnoreCase(height))
    		return -0.2F;
    	if ("ROCKY_WATERS".equalsIgnoreCase(height))
    		return 0.1F;
    	if ("HIGH_PLATEAUS".equalsIgnoreCase(height))
    		return 1.5F;
    	if ("DEEP_OCEANS".equalsIgnoreCase(height))
    		return -1.8F;
    	return Float.NaN;
    }

    // True if this should only execute when its category would not.
    private final boolean inverted;
    // The category name. Used to check if this property should execute.
    private final String category;

    public PropertyGroupConditional(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader, String function, boolean invert) {
        super(path, root, index, node, loader);
        this.inverted = invert;
        this.category = function;
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, mobStats.theEntity)) {
            super.init(mobStats);
        }
    }

    // Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        if (itemStats.parent instanceof MobDropsInfo) {
            MobDropsInfo mobDrops = (MobDropsInfo) itemStats.parent;
            if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, mobDrops.theEntity, mobDrops.theSource, mobDrops.looting, mobDrops.recentlyHit, mobDrops.rare, mobDrops.superRare)) {
                super.modifyItem(itemStats);
            }
        }
        else {
            if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, itemStats.theEntity)) {
                super.modifyItem(itemStats);
            }
        }
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        if (nbtStats.parent instanceof MobDropsInfo) {
            MobDropsInfo mobDrops = (MobDropsInfo) nbtStats.parent;
            if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, mobDrops.theEntity, mobDrops.theSource, mobDrops.looting, mobDrops.recentlyHit, mobDrops.rare, mobDrops.superRare)) {
                super.addTags(nbtStats);
            }
        }
        else {
            if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, nbtStats.theEntity)) {
                super.addTags(nbtStats);
            }
        }
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (PropertyGroupConditional.isCategoryActive(this.inverted, this.category, mobDrops.theEntity, mobDrops.theSource, mobDrops.looting, mobDrops.recentlyHit, mobDrops.rare, mobDrops.superRare)) {
            super.modifyDrops(mobDrops);
        }
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        StringBuffer condition = new StringBuffer("");
        if (this.inverted) {
            condition.append("!");
        }
        condition.append("if_");
        condition.append(this.category);
        conditions.add(condition.toString());

        super.addDrops(dropsList, attempts, chance, conditions);
    }
}
