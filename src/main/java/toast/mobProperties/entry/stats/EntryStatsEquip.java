package toast.mobProperties.entry.stats;

import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.ItemStats;
import toast.mobProperties.MobPropertyException;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.MobStatsInfo;

import com.google.gson.JsonObject;

public class EntryStatsEquip extends EntryAbstract {
    /// If true, this overrides any existing armor.
    public final boolean override;
    /// The slot to equip the item in.
    public final int slot;
    /// The min and max chance to drop the item.
    public final double[] dropChances;

    /// The item id.
    public final Item item;
    /// The min and max item damage.
    public final double[] damages;
    /// The min and max item counts.
    public final double[] counts;
    /// The item's stats.
    public final ItemStats itemStats;

    public EntryStatsEquip(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.override = FileHelper.readBoolean(node, path, "override", false);
        this.slot = FileHelper.readInteger(node, path, "slot", 0);
        if (this.slot < 0 || this.slot > 4)
            throw new MobPropertyException("Invalid slot! (" + this.slot + ": 0=hand, 1=legs, 2=body, 3=chest, 4=head)", path);
        this.dropChances = FileHelper.readCounts(node, path, "drop_chance", 0.085, 0.085);

        this.item = FileHelper.readItem(node, path, "id", false);
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
        return new String[] { "override", "slot", "drop_chance", "damage", "count", "item_stats" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (this.item == null)
            // TODO modify the currently equipped item.
            return;

        int count = FileHelper.getCount(this.counts, mobStats.random);
        if (count > 0) {
            int damage = FileHelper.getCount(this.damages, mobStats.random);
            ItemStack equipment = this.itemStats.generate(mobStats.theEntity, this.item, damage, mobStats);
            equipment.stackSize = count;
            if (equipment.getItem() != null && (this.override || mobStats.theEntity.getEquipmentInSlot(this.slot) == null)) {
                mobStats.theEntity.setCurrentItemOrArmor(this.slot, equipment);
                if (mobStats.theEntity instanceof EntityLiving) {
                    float dropChance = (float) FileHelper.getValue(this.dropChances, mobStats.random);
                    ((EntityLiving) mobStats.theEntity).setEquipmentDropChance(this.slot, dropChance);
                }
            }
        }
        else if (this.override && count < 0) {
            mobStats.theEntity.setCurrentItemOrArmor(this.slot, (ItemStack) null);
        }
    }
}
