package toast.mobProperties.entry.stats;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.MobStats;
import toast.mobProperties.NBTStats;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.MobStatsInfo;

import com.google.gson.JsonObject;

public class EntryStatsRiding extends EntryAbstract {
    /// True if the item spawned is the mount.
    private final boolean isMount;
    /// The item id.
    private final String entityId;
    /// The nbt stats for this property.
    private final NBTStats nbtStats;
    /// The item's stats.
    private final MobStats entityStats;

    public EntryStatsRiding(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader, boolean riding) {
        super(node, path);
        this.isMount = riding;
        this.entityId = FileHelper.readText(node, path, "id", "");
        this.nbtStats = new NBTStats(path, root, index, node, loader);
        this.entityStats = new MobStats(path, root, index, node, loader);
    }

    /// Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    /// Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "tags", "stats" };
    }

    /// Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        Entity entity;
        if (!mobStats.theEntity.worldObj.isRemote) {
            entity = EntityList.createEntityByName(this.entityId, mobStats.theEntity.worldObj);
            if (entity == null)
                return;

            entity.rotationYaw = mobStats.theEntity.rotationYaw;
            entity.setPosition(mobStats.theEntity.posX, mobStats.theEntity.posY, mobStats.theEntity.posZ);
            this.initEntity(mobStats.theEntity, entity, mobStats);

            if (this.isMount) {
                mobStats.theEntity.mountEntity(entity);
            }
            else {
                entity.mountEntity(mobStats.theEntity);
            }
            mobStats.theEntity.worldObj.spawnEntityInWorld(entity);
        }
    }

    /// Initializes an entity to be dropped.
    private void initEntity(EntityLivingBase parentEntity, Entity entity, Object mobInfo) {
        if (this.nbtStats.hasEntries()) {
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            this.nbtStats.generate(parentEntity, tag, mobInfo);
            entity.readFromNBT(tag);
        }
        if (entity instanceof EntityLivingBase) {
            MobStatsInfo mobStats = new MobStatsInfo((EntityLivingBase) entity);
            this.entityStats.init(mobStats);
            mobStats.save();
        }
    }
}
