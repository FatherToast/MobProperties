package toast.mobProperties.entry.item;

import toast.mobProperties.EffectHelper;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.ItemStatsInfo;

import com.google.gson.JsonObject;

public class EntryItemColor extends EntryAbstract {
    /// The range of colors the item can be.
    private final double[] colors;

    public EntryItemColor(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.colors = FileHelper.readCounts(node, path, "value", 0x000000, 0xffffff);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "value" };
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        int color = FileHelper.getCount(this.colors, itemStats.random);
        EffectHelper.dye(itemStats.theItem, color);
    }
}