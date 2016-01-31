package toast.mobProperties.entry.drops;

import java.util.List;

import net.minecraft.item.Item;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.ItemStats;
import toast.mobProperties.api.DropEntry;
import toast.mobProperties.api.IPropertyDrops;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.MobDropsInfo;

import com.google.gson.JsonObject;

public class EntryDropsRemove extends EntryAbstract implements IPropertyDrops {
    /// The item id.
    private final Item item;
    /// The min and max item damage.
    private final double[] damages;
    /// The min and max item counts.
    private final double[] counts;

    public EntryDropsRemove(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.item = FileHelper.readItem(node, path, "id");
        this.damages = FileHelper.readCounts(node, path, "damage", -1.0, -1.0);
        this.counts = FileHelper.readCounts(node, path, "count", Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "damage", "count" };
    }

    /// Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        int damage = FileHelper.getCount(this.damages, mobDrops.random);
        int count = FileHelper.getCount(this.counts, mobDrops.random);
        mobDrops.addDrop(this.item, damage, -count, (ItemStats) null);
    }

    /*
     * @see toast.mobProperties.api.IPropertyDrops#addDrops(java.util.List, double[], double, java.util.List)
     */
    @Override
    public void addDrops(List<DropEntry> dropsList, double[] attempts, double chance, List<String> conditions) {
        double[] countsBuf = { -this.counts[0], -this.counts[1] };
        if (countsBuf[0] == -Integer.MAX_VALUE) {
            countsBuf[0] = countsBuf[1] = Double.NEGATIVE_INFINITY;
        }
        dropsList.add(new DropEntry(this.item, this.damages, countsBuf, attempts, chance, conditions));
    }
}