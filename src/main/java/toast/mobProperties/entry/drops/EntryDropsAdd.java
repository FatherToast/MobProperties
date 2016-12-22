package toast.mobProperties.entry.drops;

import java.util.List;

import net.minecraft.item.Item;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.entry.ItemStats;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.util.FileHelper;

import com.google.gson.JsonObject;

public class EntryDropsAdd extends EntryAbstract implements IPropertyDrops {
    /// The item id.
    public final Item item;
    /// The min and max item damage.
    public final double[] damages;
    /// The min and max item counts.
    public final double[] counts;
    /// The item's stats.
    public final ItemStats itemStats;

    public EntryDropsAdd(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.item = FileHelper.readItem(node, path, "id");
        this.damages = FileHelper.readCounts(node, path, "damage", 0.0, 0.0);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);
        this.itemStats = new ItemStats(path, root, index, node, loader);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "damage", "count", "item_stats" };
    }

    /// Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        int damage = FileHelper.getCount(this.damages, mobDrops.random);
        int count = FileHelper.getCount(this.counts, mobDrops.random);
        mobDrops.addDrop(this.item, damage, count, this.itemStats);
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        dropsList.add(new DropEntry(this.item, this.damages, this.counts, attempts, chance, conditions));
    }
}