package toast.mobProperties.entry.item;

import net.minecraft.enchantment.Enchantment;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.util.EffectHelper;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryItemEnchant extends EntryAbstract {
    /// The enchantment id.
    private final Enchantment effect;
    /// The min and max enchantment levels.
    private final double[] levels;
    /// If treasure enchantments can be rolled (ignored if effect is not null).
    private final boolean treasure;

    public EntryItemEnchant(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.effect = FileHelper.readEnchant(node, path, "id", false);
        this.levels = FileHelper.readCounts(node, path, "level", 1.0, 1.0);
        this.treasure = FileHelper.readBoolean(node, path, "treasure", false);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "id", "level", "treasure" };
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
    	try {
	        int level = FileHelper.getCount(this.levels, itemStats.random);
	        if (this.effect == null) {
	            EffectHelper.enchantItem(itemStats.random, itemStats.theItem, level, treasure);
	        }
	        else {
	            EffectHelper.enchantItem(itemStats.theItem, this.effect, level);
	        }
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}