package toast.mobProperties.entry.drops;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.MobStats;
import toast.mobProperties.NBTStats;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.MobDropsInfo;
import toast.mobProperties.entry.MobStatsInfo;

import com.google.gson.JsonObject;

public class EntryDropsSpawn extends EntryAbstract {
    // The item id.
    private final String entityId;
    // The min and max item counts.
    private final double[] counts;
    // The nbt stats for this property.
    private final NBTStats nbtStats;
    // The item's stats.
    private final MobStats entityStats;

    public EntryDropsSpawn(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.entityId = FileHelper.readText(node, path, "id", "");
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);
        this.nbtStats = new NBTStats(path, root, index, node, loader);
        this.entityStats = new MobStats(path, root, index, node, loader);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "id" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count", "tags", "stats" };
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        Entity entity;
        if (!mobDrops.theEntity.worldObj.isRemote) {
            for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                entity = EntityList.createEntityByName(this.entityId, mobDrops.theEntity.worldObj);
                if (entity == null)
                    return;

                this.initEntity(mobDrops.theEntity, entity, mobDrops);

                mobDrops.theEntity.worldObj.spawnEntityInWorld(entity);
            }
        }
    }

    // Initializes an entity to be "dropped".
    private void initEntity(EntityLivingBase parentEntity, Entity entity, Object mobInfo) {
        if (this.nbtStats.hasEntries()) {
            NBTTagCompound tag = new NBTTagCompound();
            entity.writeToNBT(tag);
            this.nbtStats.generate(parentEntity, tag, mobInfo);
            entity.readFromNBT(tag);
            entity.setPosition(parentEntity.posX + entity.posX, parentEntity.posY + entity.posY, parentEntity.posZ + entity.posZ);
            entity.rotationYaw = parentEntity.rotationYaw + entity.rotationYaw;
        }
        else {
            entity.setPosition(parentEntity.posX, parentEntity.posY, parentEntity.posZ);
            entity.rotationYaw = parentEntity.rotationYaw;
        }
        if (entity instanceof EntityLivingBase) {
            MobStatsInfo mobStats = new MobStatsInfo((EntityLivingBase) entity);
            this.entityStats.init(mobStats);
            mobStats.save();
        }
    }
}
