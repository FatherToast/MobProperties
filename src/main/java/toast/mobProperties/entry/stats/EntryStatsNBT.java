package toast.mobProperties.entry.stats;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.entry.NBTStats;
import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobStatsInfo;

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

        NBTTagList tagList = tag.getTagList("Pos", Constants.NBT.TAG_DOUBLE);
        double x = tagList.getDoubleAt(0);
        double y = tagList.getDoubleAt(1);
        double z = tagList.getDoubleAt(2);
        tagList.set(0, new NBTTagDouble(0.0));
        tagList.set(1, new NBTTagDouble(0.0));
        tagList.set(2, new NBTTagDouble(0.0));

        tagList = tag.getTagList("Rotation", Constants.NBT.TAG_FLOAT);
        float yaw = tagList.getFloatAt(0);
        tagList.set(0, new NBTTagFloat(0.0F));

        this.nbtStats.generate(mobStats.theEntity, tag, mobStats);

        tagList = tag.getTagList("Pos", Constants.NBT.TAG_DOUBLE);
        tagList.set(0, new NBTTagDouble(tagList.getDoubleAt(0) + x));
        tagList.set(1, new NBTTagDouble(tagList.getDoubleAt(1) + y));
        tagList.set(2, new NBTTagDouble(tagList.getDoubleAt(2) + z));

        tagList = tag.getTagList("Rotation", Constants.NBT.TAG_FLOAT);
        tagList.set(0, new NBTTagFloat(tagList.getFloatAt(0) + yaw));

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
