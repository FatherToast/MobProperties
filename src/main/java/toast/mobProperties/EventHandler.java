package toast.mobProperties;

import java.io.File;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import toast.mobProperties.entry.MobDropsInfo;
import toast.mobProperties.entry.MobStatsInfo;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class EventHandler
{
    // Properties stored for easy access.
    public static final boolean DISABLED = Properties.getBoolean(Properties.GENERAL, "disable");

    public EventHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Called by World.spawnEntityInWorld().
     * Entity entity = the entity joining the world.
     * World world = the world the entity is joining.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityLivingBase && !(event.entity instanceof EntityPlayer)) {
            if (EventHandler.DISABLED) {
                // Remove this mod's effects
            }
            else {
                EffectHelper.loadXP((EntityLivingBase) event.entity);
                byte initDat = EffectHelper.getInit(event.entity);
                if (initDat <= 0) {
                    MobProperties mobProps = MobProperties.getProperties((EntityLivingBase) event.entity);
                    if (mobProps != null) {
                        if (initDat == 0) {
                            EffectHelper.loadStats((EntityLivingBase) event.entity, true);
                            if (!event.entity.isEntityAlive()) {
                            	event.setCanceled(true);
                            	return;
                            }
                            initDat = EffectHelper.getInit(event.entity);
                            if (initDat == 0) {
	                            EffectHelper.setInit(event.entity, -1);
	                            MobStatsInfo mobStats = new MobStatsInfo((EntityLivingBase) event.entity);
	                            mobProps.preInit(mobStats);
	                            mobStats.save();
	                            if (!event.entity.isEntityAlive()) {
	                            	event.setCanceled(true);
	                            	return;
	                            }
	                            initDat = EffectHelper.getInit(event.entity);
	                            if (initDat > 0)
	                                return;
                            }
                        }
                        TickHandler.markForInit((EntityLivingBase) event.entity, mobProps);
                    }
                }
            }
        }
    }

    /**
     * Called by EntityLiving.onDeath().
     * EntityLivingBase entityLiving = the entity dropping the items.
     * DamageSource source = the source of the lethal damage.
     * ArrayList<EntityItem> drops = the items being dropped.
     * int lootingLevel = the attacker's looting level.
     * boolean recentlyHit = if the entity was recently hit by another player.
     * int specialDropValue = recentlyHit ? entityLiving.getRNG().nextInt(200) - lootingLevel : 0.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDrops(LivingDropsEvent event) {
        if (!event.entityLiving.worldObj.isRemote) {
            IProperty props = MobProperties.getProperties(event.entityLiving);
            MobDropsInfo mobDrops = new MobDropsInfo(event.entityLiving, event.source, event.drops, event.lootingLevel, event.recentlyHit, event.specialDropValue);
            if (props != null) {
                props.modifyDrops(mobDrops);
            }
            EffectHelper.loadDrops(mobDrops);
            mobDrops.applyDefaultAndAddDrops();

            EffectHelper.loadLegacyDrops(event.entityLiving, event.drops);
        }
    }

    /**
     * Called by EntityPlayer.interactWith().
     * EntityPlayer entityPlayer = the player interacting.
     * Entity target = the entity being right clicked.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
    	if (event.target != null && event.entityPlayer != null && !event.entityPlayer.worldObj.isRemote) {
    		ItemStack held = event.entityPlayer.getHeldItem();
    		if (held != null && held.getItem() == Items.stick && held.stackTagCompound != null && held.stackTagCompound.getBoolean("MP|InfoWand")) {
    			// Force position tag to be [0,0,0]
    			float offset = event.target.ySize;
    			double x = event.target.posX;
    			double y = event.target.posY;
    			double z = event.target.posZ;
    			event.target.ySize = 0.0F;
    			event.target.posX = event.target.posY = event.target.posZ = 0.0;

    			NBTTagCompound tag = new NBTTagCompound();
    			event.target.writeToNBT(tag);

    			// Remove dangerous tags and restore position
    			tag.removeTag("Dimension");
    			tag.removeTag("UUIDMost");
    			tag.removeTag("UUIDLeast");
    			event.target.ySize = offset;
    			event.target.posX = x;
    			event.target.posY = y;
    			event.target.posZ = z;

    			File generated = FileHelper.generateNbtStats(event.target.getCommandSenderName(), tag);
    			if (generated != null) {
    				event.entityPlayer.addChatMessage(new ChatComponentText("[Info Wand] Generated external nbt stats file \"" + generated.getName().substring(0, generated.getName().length() - FileHelper.FILE_EXT.length()) + "\" at:"));
    				event.entityPlayer.addChatMessage(new ChatComponentText("    " + generated.getAbsolutePath()));
    			}
    			else {
					event.entityPlayer.addChatMessage(new ChatComponentText("[Info Wand] Failed to generate external nbt stats file!"));
				}
    		}
    	}
    }

    /**
     * Called by a number of methods.
     * EntityPlayer entityPlayer = the player interacting.
     * PlayerInteractEvent.Action action = the action being taken.
     * int x = the x-coord of the block being interacted with, if any.
     * int y = the y-coord of the block being interacted with, if any.
     * int z = the z-coord of the block being interacted with, if any.
     * int face = the side of the block being interacted with, if any.
     * World world = the world being interacted with.
     * Result useBlock = result for using the targeted block.
     * Result useItem = result for using the held item.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if (event.world != null && event.y >= 0 && event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer != null && !event.entityPlayer.worldObj.isRemote) {
    		ItemStack held = event.entityPlayer.getHeldItem();
    		if (held != null && held.getItem() == Items.stick && held.stackTagCompound != null && held.stackTagCompound.getBoolean("MP|InfoWand")) {
    			TileEntity tileEntity = event.world.getTileEntity(event.x, event.y, event.z);
    			if (tileEntity == null)
    				return;

    			NBTTagCompound tag = new NBTTagCompound();
    			tileEntity.writeToNBT(tag);
    			String name = tag.getString("id");

    			// Remove dangerous tags
    			tag.removeTag("id");
    			tag.removeTag("x");
    			tag.removeTag("y");
    			tag.removeTag("z");

    			if (tag.hasNoTags()) {
					event.entityPlayer.addChatMessage(new ChatComponentText("[Info Wand] Tile entity has no usable tags!"));
					return;
    			}

    			File generated = FileHelper.generateNbtStats(name, tag);
    			if (generated != null) {
    				event.entityPlayer.addChatMessage(new ChatComponentText("[Info Wand] Generated external nbt stats file \"" + generated.getName().substring(0, generated.getName().length() - FileHelper.FILE_EXT.length()) + "\" at:"));
    				event.entityPlayer.addChatMessage(new ChatComponentText("    " + generated.getAbsolutePath()));
    			}
    			else {
					event.entityPlayer.addChatMessage(new ChatComponentText("[Info Wand] Failed to generate external nbt stats file!"));
				}
    		}
    	}
    }
}