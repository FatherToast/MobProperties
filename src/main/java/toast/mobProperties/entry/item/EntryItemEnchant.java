package toast.mobProperties.entry.item;

import net.minecraft.enchantment.Enchantment;
import toast.mobProperties.EffectHelper;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.ItemStatsInfo;

import com.google.gson.JsonObject;

public class EntryItemEnchant extends EntryAbstract {
    /// The enchantment id.
    private final int effectId;
    /// The min and max enchantment levels.
    private final double[] levels;

    public EntryItemEnchant(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        Enchantment enchant = FileHelper.readEnchant(node, path, "id", false);
        this.effectId = enchant == null ? -1 : enchant.effectId;
        this.levels = FileHelper.readCounts(node, path, "level", 1.0, 1.0);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "id", "level" };
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
    	try {
	        int level = FileHelper.getCount(this.levels, itemStats.random);
	        if (this.effectId < 0) {
	            EffectHelper.enchantItem(itemStats.random, itemStats.theItem, level);
	        }
	        else {
	            EffectHelper.enchantItem(itemStats.theItem, this.effectId, level);
	        }
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }
}