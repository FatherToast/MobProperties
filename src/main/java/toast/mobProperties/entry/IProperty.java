package toast.mobProperties.entry;

import toast.mobProperties.event.ItemStatsInfo;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.event.NBTStatsInfo;

public interface IProperty
{
    /// Returns an array of required field names.
    public String[] getRequiredFields();
    
    /// Returns an array of optional field names.
    public String[] getOptionalFields();
    
    /// Returns this property's Json string.
    public String getJsonString();

    /// Initializes the entity's stats.
    public void init(MobStatsInfo mobStats);
    
    /// Modifies the item.
    public void modifyItem(ItemStatsInfo itemStats);
    
    /// Adds any NBT tags to the list.
    public void addTags(NBTStatsInfo nbtStats);
    
    /// Modifies the list of drops.
    public void modifyDrops(MobDropsInfo mobDrops);
}
