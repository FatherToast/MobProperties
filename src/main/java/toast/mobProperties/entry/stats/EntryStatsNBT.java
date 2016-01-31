package toast.mobProperties.entry.stats;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.NBTStats;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.ItemStatsInfo;
import toast.mobProperties.entry.MobStatsInfo;

import com.google.gson.JsonObject;

public class EntryStatsNBT extends EntryAbstract {
    /// The nbt stats for this property.
    private final NBTStats nbtStats;

    public EntryStatsNBT(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.nbtStats = new NBTStats(path, root, index, node, loader);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "tags" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        NBTTagCompound tag = new NBTTagCompound();
        mobStats.theEntity.writeToNBT(tag);

        NBTTagList tagList = tag.getTagList("Pos", new NBTTagDouble(0.0).getId());
        double x = tagList.func_150309_d(0);
        double y = tagList.func_150309_d(1);
        double z = tagList.func_150309_d(2);
        tagList.func_150304_a(0, new NBTTagDouble(0.0));
        tagList.func_150304_a(1, new NBTTagDouble(0.0));
        tagList.func_150304_a(2, new NBTTagDouble(0.0));

        tagList = tag.getTagList("Rotation", new NBTTagFloat(0.0F).getId());
        float yaw = tagList.func_150308_e(0);
        tagList.func_150304_a(0, new NBTTagFloat(0.0F));

        this.nbtStats.generate(mobStats.theEntity, tag, mobStats);

        tagList = tag.getTagList("Pos", new NBTTagDouble(0.0).getId());
        tagList.func_150304_a(0, new NBTTagDouble(tagList.func_150309_d(0) + x));
        tagList.func_150304_a(1, new NBTTagDouble(tagList.func_150309_d(1) + y));
        tagList.func_150304_a(2, new NBTTagDouble(tagList.func_150309_d(2) + z));

        tagList = tag.getTagList("Rotation", new NBTTagFloat(0.0F).getId());
        tagList.func_150304_a(0, new NBTTagFloat(tagList.func_150308_e(0) + yaw));

        mobStats.theEntity.readFromNBT(tag);
    }

    /// Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        if (!itemStats.theItem.hasTagCompound()) {
            itemStats.theItem.setTagCompound(new NBTTagCompound());
        }
        this.nbtStats.generate(itemStats.theEntity, itemStats.theItem, itemStats.theItem.getTagCompound(), itemStats);
    }
}
