package toast.mobProperties.entry.stats;

import com.google.gson.JsonObject;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.entry.ItemStats;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

public class EntryStatsEquip extends EntryAbstract {
    /// If true, this overrides any existing armor.
    public final boolean override;
    /// The slot to equip the item in.
    public final EntityEquipmentSlot slot;
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
        this.item = FileHelper.readItem(node, path, "id", false);
        double mod = item == null ? Double.NaN : 1.0; // Turn numerical defaults to NaN if item is null
        
        this.override = FileHelper.readBoolean(node, path, "override", false);
        EntityEquipmentSlot slotTmp = FileHelper.readSlot(node, path, "slot", false);
        this.slot = slotTmp != null? slotTmp : EntityEquipmentSlot.MAINHAND;
        this.dropChances = FileHelper.readCounts(node, path, "drop_chance", 0.085 * mod, 0.085 * mod);

        this.damages = FileHelper.readCounts(node, path, "damage", 0.0 * mod, 0.0 * mod);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0 * mod, 1.0 * mod);
        this.itemStats = new ItemStats(path, root, index, node, loader);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "override", "slot", "drop_chance", "id", "damage", "count", "item_stats" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        if (this.item == null) { // Modify the equipped item, if any
        	ItemStack equipment = mobStats.theEntity.getItemStackFromSlot(this.slot);
        	if (equipment != null) {
                int count = Double.isNaN(this.counts[0]) ? -1 : FileHelper.getCount(this.counts, mobStats.random);
                if (count == 0) {
                    mobStats.theEntity.setItemStackToSlot(this.slot, (ItemStack) null);
                }
                else {
                    this.itemStats.generate(new ItemStatsInfo(equipment, mobStats.theEntity, mobStats));
                    if (count > 0) equipment.stackSize = count;
                    if (!Double.isNaN(this.damages[0]))
                    	equipment.setItemDamage(FileHelper.getCount(this.damages, mobStats.random));
                    
                    mobStats.theEntity.setItemStackToSlot(this.slot, equipment);
                    if (!Double.isNaN(this.dropChances[0]) && mobStats.theEntity instanceof EntityLiving) {
                        float dropChance = (float) FileHelper.getValue(this.dropChances, mobStats.random);
                        ((EntityLiving) mobStats.theEntity).setDropChance(this.slot, dropChance);
                    }
                }
        	}
            return;
        }

        int count = FileHelper.getCount(this.counts, mobStats.random);
        if (count > 0) {
            int damage = FileHelper.getCount(this.damages, mobStats.random);
            ItemStack equipment = this.itemStats.generate(mobStats.theEntity, this.item, damage, mobStats);
            equipment.stackSize = count;
            if (equipment.getItem() != null && (this.override || mobStats.theEntity.getItemStackFromSlot(this.slot) == null)) {
                mobStats.theEntity.setItemStackToSlot(this.slot, equipment);
                if (mobStats.theEntity instanceof EntityLiving) {
                    float dropChance = (float) FileHelper.getValue(this.dropChances, mobStats.random);
                    ((EntityLiving) mobStats.theEntity).setDropChance(this.slot, dropChance);
                }
            }
        }
        else if (this.override && count <= 0) {
            mobStats.theEntity.setItemStackToSlot(this.slot, (ItemStack) null);
        }
    }
}
