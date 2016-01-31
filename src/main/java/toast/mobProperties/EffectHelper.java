package toast.mobProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.UUID;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import toast.mobProperties.entry.MobDropsInfo;
import toast.mobProperties.entry.MobStatsInfo;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public abstract class EffectHelper {
    // The NBT tags used by this mod.
    private static final String TAG_INIT = "MP|Init";

    private static final String TAG_BASE = "mp|drops";

    private static final String TAG_PRE_STATS = "pfs";
    private static final String TAG_STATS = "sfs";
    private static final String TAG_DROPS = "dfs";

    private static final String TAG_XP_BASE = "xpb";
    private static final String TAG_XP_ADD = "xpa";
    private static final String TAG_XP_MULT = "xpm";

    // Legacy tags. Kept for old version compatibility.
    @Deprecated
    private static final String TAG_ADD_DROPS = "add";
    @Deprecated
    private static final String TAG_SPAWNS = "ent";

    /// Applies the enchantment to the itemStack at the given level. Called by all other enchantItem methods to do the actual enchanting.
    public static void enchantItem(ItemStack itemStack, int id, int level) {
        if (Enchantment.enchantmentsList[id] != null) {
            itemStack.addEnchantment(Enchantment.enchantmentsList[id], level);
        }
    }

    /// Randomly enchants the itemStack based on the level (identical to using an enchantment table).
    public static void enchantItem(ItemStack itemStack, int level) {
        EffectHelper.enchantItem(_MobPropertiesMod.random, itemStack, level);
    }

    public static void enchantItem(Random random, ItemStack itemStack, int level) {
        EnchantmentHelper.addRandomEnchantment(random, itemStack, level);
    }

    /// Adds a line of text to the item stack's infobox.
    public static void addItemText(ItemStack itemStack, String text) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("display")) {
            itemStack.stackTagCompound.setTag("display", new NBTTagCompound());
        }
        NBTTagCompound displayTag = itemStack.stackTagCompound.getCompoundTag("display");
        if (!displayTag.hasKey("Lore")) {
            displayTag.setTag("Lore", new NBTTagList());
        }
        NBTTagString stringTag = new NBTTagString(text);
        displayTag.getTagList("Lore", stringTag.getId()).appendTag(stringTag);
    }

    /// Sets the item's color. No effect on most items.
    public static void dye(ItemStack itemStack, int color) {
        if (itemStack.stackTagCompound == null) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        if (!itemStack.stackTagCompound.hasKey("display")) {
            itemStack.stackTagCompound.setTag("display", new NBTTagCompound());
        }
        itemStack.stackTagCompound.getCompoundTag("display").setInteger("color", color);
    }

    /// Adds a custom potion effect to the entity (can put regen on undead, poison on spiders, etc.).
    public static void addPotionEffect(EntityLivingBase entity, int id, int duration, int amplifier, boolean ambient) {
        NBTTagCompound tag = new NBTTagCompound();
        entity.writeToNBT(tag);
        if (!tag.hasKey("ActiveEffects")) {
            tag.setTag("ActiveEffects", new NBTTagList());
        }
        NBTTagCompound potionTag = new NBTTagCompound();
        if (id != 0) {
            potionTag.setByte("Id", (byte) id);
        }
        if (duration != 0) {
            potionTag.setInteger("Duration", duration);
        }
        if (amplifier != 0) {
            potionTag.setByte("Amplifier", (byte) amplifier);
        }
        if (ambient) {
            potionTag.setBoolean("Ambient", ambient);
        }
        tag.getTagList("ActiveEffects", potionTag.getId()).appendTag(potionTag);
        entity.readFromNBT(tag);
    }

    /// Adds a custom potion effect to the item stack.
    public static void addPotionEffect(ItemStack itemStack, int id, int duration, int amplifier, boolean ambient) {
        if (itemStack.stackTagCompound == null) {
            itemStack.stackTagCompound = new NBTTagCompound();
        }
        if (!itemStack.stackTagCompound.hasKey("CustomPotionEffects")) {
            itemStack.stackTagCompound.setTag("CustomPotionEffects", new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setByte("Id", (byte) id);
        tag.setInteger("Duration", duration);
        tag.setByte("Amplifier", (byte) amplifier);
        tag.setBoolean("Ambient", ambient);
        itemStack.stackTagCompound.getTagList("CustomPotionEffects", tag.getId()).appendTag(tag);
    }

    /// Adds a custom attribute modifier to the item stack.
    public static void addModifier(ItemStack itemStack, String attribute, double value, int operation) {
        if (itemStack.stackTagCompound == null) {
            itemStack.stackTagCompound = new NBTTagCompound();
        }
        if (!itemStack.stackTagCompound.hasKey("AttributeModifiers")) {
            itemStack.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("AttributeName", attribute);
        tag.setString("Name", "MobProperties|" + Integer.toString(_MobPropertiesMod.random.nextInt(), Character.MAX_RADIX));
        tag.setDouble("Amount", value);
        tag.setInteger("Operation", operation);
        UUID id = UUID.randomUUID();
        tag.setLong("UUIDMost", id.getMostSignificantBits());
        tag.setLong("UUIDLeast", id.getLeastSignificantBits());
        itemStack.stackTagCompound.getTagList("AttributeModifiers", tag.getId()).appendTag(tag);
    }

    // Saves the entity's initialization state.
    public static void setInit(Entity entity, int state) {
        entity.getEntityData().setByte(EffectHelper.TAG_INIT, (byte) state);
    }

    // Loads the entity's initialization state.
    public static byte getInit(Entity entity) {
        return entity.getEntityData().getByte(EffectHelper.TAG_INIT);
    }

    // Saves xp data to the entity.
    public static void saveXP(MobDropsInfo mobDrops) {
        EffectHelper.saveXP(mobDrops.theEntity, mobDrops.xpBase, mobDrops.xpAdd, mobDrops.xpMult);
    }

    private static void saveXP(EntityLivingBase entity, int base, int add, double mult) {
        if (entity instanceof EntityLiving) {
            NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);
            if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE)) {
                entity.getEntityData().setTag(EffectHelper.TAG_BASE, tag);
            }

            if (base >= 0) {
                tag.setInteger(EffectHelper.TAG_XP_BASE, base);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_BASE);
            }
            if (add != 0) {
                tag.setInteger(EffectHelper.TAG_XP_ADD, add);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_ADD);
            }
            if (mult != 1.0) {
                tag.setDouble(EffectHelper.TAG_XP_MULT, mult);
            }
            else {
                tag.removeTag(EffectHelper.TAG_XP_MULT);
            }
        }
    }

    // Loads xp data to the mob info.
    public static void loadXP(MobDropsInfo mobDrops) {
        if (mobDrops.theEntity instanceof EntityLiving) {
            if (!mobDrops.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE))
                return;
            NBTTagCompound tag = mobDrops.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

            if (tag.hasKey(EffectHelper.TAG_XP_BASE)) {
                mobDrops.xpBase = tag.getInteger(EffectHelper.TAG_XP_BASE);
            }
            if (tag.hasKey(EffectHelper.TAG_XP_ADD)) {
                mobDrops.xpAdd = tag.getInteger(EffectHelper.TAG_XP_ADD);
            }
            if (tag.hasKey(EffectHelper.TAG_XP_MULT)) {
                mobDrops.xpMult = tag.getDouble(EffectHelper.TAG_XP_MULT);
            }
        }
    }

    // Loads xp data to the entity.
    public static void loadXP(EntityLivingBase entity) {
        if (entity instanceof EntityLiving) {
            if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE))
                return;
            NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

            if (tag.hasKey(EffectHelper.TAG_XP_BASE) || tag.hasKey(EffectHelper.TAG_XP_ADD) || tag.hasKey(EffectHelper.TAG_XP_MULT)) {
                String[] fieldNames = { "field_70728_aV", "experienceValue" };
                int exp;
                if (tag.hasKey(EffectHelper.TAG_XP_BASE)) {
                    exp = tag.getInteger(EffectHelper.TAG_XP_BASE);
                }
                else {
                    exp = ((Integer) ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, (EntityLiving) entity, fieldNames)).intValue();
                }
                if (tag.hasKey(EffectHelper.TAG_XP_ADD)) {
                    exp += tag.getInteger(EffectHelper.TAG_XP_ADD);
                }
                if (tag.hasKey(EffectHelper.TAG_XP_MULT)) {
                    exp = (int) Math.round(exp * tag.getDouble(EffectHelper.TAG_XP_MULT));
                }
                if (exp < 0) {
                    exp = 0;
                }
                ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, (EntityLiving) entity, Integer.valueOf(exp), fieldNames);
            }
        }
    }

    // Loads stats data and executes it.
    public static void loadStats(EntityLivingBase entity, boolean preStats) {
        if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE))
            return;
        NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        String tagName = preStats ? EffectHelper.TAG_PRE_STATS : EffectHelper.TAG_STATS;
        if (tag.hasKey(tagName)) {
            MobStatsInfo mobStats = new MobStatsInfo(entity);
            new MobStats(entity.getClass().getName(), tag.getTagList(tagName, new NBTTagString().getId())).init(mobStats);
            mobStats.save();
        }
    }

    // Saves drops data to the entity to be executed later.
    public static void saveDrops(MobStatsInfo mobStats) {
        if (mobStats.addDropsList.size() == 0)
            return;
        NBTTagCompound tag = mobStats.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);
        if (!mobStats.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE)) {
            mobStats.theEntity.getEntityData().setTag(EffectHelper.TAG_BASE, tag);
        }

        NBTTagList dropList = tag.getTagList(EffectHelper.TAG_DROPS, new NBTTagString().getId());
        if (!tag.hasKey(EffectHelper.TAG_DROPS)) {
            tag.setTag(EffectHelper.TAG_DROPS, dropList);
        }
        for (String dropsFunction : mobStats.addDropsList) {
            dropList.appendTag(new NBTTagString(dropsFunction));
        }
    }

    // Loads drops data and executes it.
    public static void loadDrops(MobDropsInfo mobDrops) {
        if (!mobDrops.theEntity.getEntityData().hasKey(EffectHelper.TAG_BASE))
            return;
        NBTTagCompound tag = mobDrops.theEntity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        if (tag.hasKey(EffectHelper.TAG_DROPS)) {
            new MobDrops(mobDrops.theEntity.getClass().getName(), tag.getTagList(EffectHelper.TAG_DROPS, new NBTTagString().getId())).modifyDrops(mobDrops);
        }
    }

    // Loads legacy drops data to the drops list.
    @Deprecated
    public static void loadLegacyDrops(EntityLivingBase entity, ArrayList<EntityItem> drops) {
        if (!entity.getEntityData().hasKey(EffectHelper.TAG_BASE))
            return;
        NBTTagCompound tag = entity.getEntityData().getCompoundTag(EffectHelper.TAG_BASE);

        if (tag.hasKey(EffectHelper.TAG_ADD_DROPS)) {
            NBTTagList dropList = tag.getTagList(EffectHelper.TAG_ADD_DROPS, new NBTTagCompound().getId());
            int length = dropList.tagCount();
            ItemStack itemStack;
            EntityItem drop;
            for (int i = 0; i < length; i++) {
                itemStack = ItemStack.loadItemStackFromNBT(dropList.getCompoundTagAt(i));
                if (itemStack == null || itemStack.stackSize == 0) {
                    continue;
                }
                if (itemStack.stackSize > 0) {
                    int count = itemStack.stackSize;
                    itemStack.stackSize = 1;
                    while (count-- > 0) {
                        drop = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, itemStack.copy());
                        drop.delayBeforeCanPickup = 10;
                        drops.add(drop);
                    }
                }
                else {
                    EffectHelper.removeDrop(itemStack, entity, drops);
                }
            }
        }
        if (tag.hasKey(EffectHelper.TAG_SPAWNS)) {
            NBTTagList spawnList = tag.getTagList(EffectHelper.TAG_SPAWNS, new NBTTagCompound().getId());
            int length = spawnList.tagCount();
            Entity mob;
            for (int i = 0; i < length; i++) {
                mob = EntityList.createEntityFromNBT(spawnList.getCompoundTagAt(i), entity.worldObj);
                if (mob != null) {
                    mob.rotationYaw += entity.rotationYaw;
                    mob.setPosition(entity.posX + mob.posX, entity.posY + mob.posY, entity.posZ + mob.posZ);
                    entity.worldObj.spawnEntityInWorld(mob);
                }
            }
        }
    }

    // Removes the item stack. Called when an item with a negative stack size is loaded.
    @Deprecated
    private static void removeDrop(ItemStack itemStack, EntityLivingBase entity, ArrayList<EntityItem> drops) {
        boolean infinite = itemStack.stackSize == -Integer.MAX_VALUE;
        EffectHelper.removeDrop(itemStack, infinite, entity, drops);
    }

    @Deprecated
    private static int removeDrop(ItemStack itemStack, boolean infinite, EntityLivingBase entity, ArrayList<EntityItem> drops) {
        EntityItem drop;
        for (Iterator<EntityItem> iterator = drops.iterator(); iterator.hasNext();) {
            drop = iterator.next();
            ItemStack dropStack = drop.getEntityItem();
            if (itemStack.getItem() == dropStack.getItem() && (itemStack.getItemDamage() < 0 || itemStack.getItemDamage() == dropStack.getItemDamage())) {
                if (infinite) {
                    iterator.remove();
                }
                else {
                    itemStack.stackSize += dropStack.stackSize;
                    if (itemStack.stackSize < 0) {
                        iterator.remove();
                    }
                    else if (itemStack.stackSize == 0) {
                        iterator.remove();
                        return 0;
                    }
                    else {
                        dropStack.stackSize = itemStack.stackSize;
                        drop.setEntityItemStack(dropStack);
                        return 0;
                    }
                }
            }
        }
        return itemStack.stackSize;
    }
}