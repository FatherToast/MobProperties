package toast.mobProperties.entry;

import java.util.Random;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import toast.mobProperties.*;

public class ItemStatsInfo
{
    /// The mob info this is a part of.
    public final Object parent;
    /// The item currently being initialized.
    public final ItemStack theItem;
    /// The entity that is dropping or equipping the item.
    public final EntityLivingBase theEntity;
    /// The entity's random number generator.
    public final Random random;
    
    public ItemStatsInfo(ItemStack item, EntityLivingBase entity, Object mobInfo) {
        parent = mobInfo;
        theItem = item;
        theEntity = entity;
        random = entity.getRNG();
    }
}