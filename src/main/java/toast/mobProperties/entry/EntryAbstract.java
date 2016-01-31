package toast.mobProperties.entry;

import toast.mobProperties.FileHelper;
import toast.mobProperties.IProperty;

import com.google.gson.JsonObject;

public abstract class EntryAbstract implements IProperty {
    // The Json string that makes up this property.
    private final String jsonString;

    public EntryAbstract(JsonObject node, String path) {
        FileHelper.verify(node, path, this);
        this.jsonString = FileHelper.getFunctionString(node, path);
    }

    // Returns this property's Json string.
	@Override
    public String getJsonString() {
        return this.jsonString;
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        throw new UnsupportedOperationException("Non-stats properties can not initialize mobs!");
    }

    // Modifies the item.
    @Override
    public void modifyItem(ItemStatsInfo itemStats) {
        throw new UnsupportedOperationException("Non-item properties can not modify items!");
    }

    // Adds any NBT tags to the list.
    @Override
    public void addTags(NBTStatsInfo nbtStats) {
        throw new UnsupportedOperationException("Non-nbt properties can not modify nbt!");
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        throw new UnsupportedOperationException("Non-drops properties can not modify drops!");
    }
}
