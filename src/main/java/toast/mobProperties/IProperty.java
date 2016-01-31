package toast.mobProperties;

import toast.mobProperties.entry.ItemStatsInfo;
import toast.mobProperties.entry.MobDropsInfo;
import toast.mobProperties.entry.MobStatsInfo;
import toast.mobProperties.entry.NBTStatsInfo;

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
