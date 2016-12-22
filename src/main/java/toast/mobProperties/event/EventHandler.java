package toast.mobProperties.event;

import java.io.File;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import toast.mobProperties.CommandInfoWand;
import toast.mobProperties.entry.IProperty;
import toast.mobProperties.entry.MobProperties;
import toast.mobProperties.util.EffectHelper;
import toast.mobProperties.util.FileHelper;

public class EventHandler
{
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
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityLivingBase && !(event.getEntity() instanceof EntityPlayer)) {
        	boolean alive = event.getEntity().isEntityAlive();

            EffectHelper.loadXP((EntityLivingBase) event.getEntity());
            byte initDat = EffectHelper.getInit(event.getEntity());
            if (initDat <= 0) {
                MobProperties mobProps = MobProperties.getProperties((EntityLivingBase) event.getEntity());
                if (mobProps != null) {
                    if (initDat == 0) {
                        EffectHelper.loadStats((EntityLivingBase) event.getEntity(), true);
                        if (alive && !event.getEntity().isEntityAlive()) {
                        	event.setCanceled(true);
                        	return;
                        }
                        initDat = EffectHelper.getInit(event.getEntity());
                        if (initDat == 0) {
                            EffectHelper.setInit(event.getEntity(), -1);
                            MobStatsInfo mobStats = new MobStatsInfo((EntityLivingBase) event.getEntity());
                            mobProps.preInit(mobStats);
                            mobStats.save();
                            if (alive && !event.getEntity().isEntityAlive()) {
                            	event.setCanceled(true);
                            	return;
                            }
                            initDat = EffectHelper.getInit(event.getEntity());
                            if (initDat > 0)
                                return;
                        }
                    }
                    TickHandler.markForInit((EntityLivingBase) event.getEntity(), mobProps);
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
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onLivingDrops(LivingDropsEvent event) {
        if (!event.getEntityLiving().worldObj.isRemote) {
            IProperty props = MobProperties.getProperties(event.getEntityLiving());
            MobDropsInfo mobDrops = new MobDropsInfo(event.getEntityLiving(), event.getSource(), event.getDrops(), event.getLootingLevel(), event.isRecentlyHit());
            if (props != null) {
                props.modifyDrops(mobDrops);
            }
            EffectHelper.loadDrops(mobDrops);
            mobDrops.applyDefaultAndAddDrops();

            EffectHelper.loadLegacyDrops(event.getEntityLiving(), event.getDrops());
        }
    }

    /**
     * Called by EntityPlayer.interactWith().
     * EntityPlayer entityPlayer = the player interacting.
     * Entity target = the entity being right clicked.
     *
     * @param event The event being triggered.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
    	if (event.getTarget() != null) {
    		ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
    		if (held != null && held.getItem() == Items.STICK && held.hasTagCompound() && held.getTagCompound().getBoolean(CommandInfoWand.TAG_INFOWAND)) {
    			// Force position tag to be [0,0,0]
    			double x = event.getTarget().posX;
    			double y = event.getTarget().posY;
    			double z = event.getTarget().posZ;
    			event.getTarget().posX = event.getTarget().posY = event.getTarget().posZ = 0.0;

    			NBTTagCompound tag = new NBTTagCompound();
    			event.getTarget().writeToNBT(tag);

    			// Remove dangerous tags and restore position
    			tag.removeTag("Dimension");
    			tag.removeTag("UUIDMost");
    			tag.removeTag("UUIDLeast");
    			event.getTarget().posX = x;
    			event.getTarget().posY = y;
    			event.getTarget().posZ = z;

    			File generated = FileHelper.generateNbtStats(event.getTarget().getName(), tag);
    			if (generated != null) {
    				event.getEntityPlayer().addChatMessage(new TextComponentString("[Info Wand] Generated external nbt stats file \"" + generated.getName().substring(0, generated.getName().length() - FileHelper.FILE_EXT.length()) + "\" at:"));
    				event.getEntityPlayer().addChatMessage(new TextComponentString("    " + generated.getAbsolutePath()));
    			}
    			else {
					event.getEntityPlayer().addChatMessage(new TextComponentString("[Info Wand] Failed to generate external nbt stats file!"));
				}
    			event.setCanceled(true);
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
    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
		ItemStack held = event.getEntityPlayer().getHeldItem(event.getHand());
		if (held != null && held.getItem() == Items.STICK && held.hasTagCompound() && held.getTagCompound().getBoolean(CommandInfoWand.TAG_INFOWAND)) {
			TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
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
				event.getEntityPlayer().addChatMessage(new TextComponentString("[Info Wand] Tile entity has no usable tags!"));
				return;
			}

			File generated = FileHelper.generateNbtStats(name, tag);
			if (generated != null) {
				event.getEntityPlayer().addChatMessage(new TextComponentString("[Info Wand] Generated external nbt stats file \"" + generated.getName().substring(0, generated.getName().length() - FileHelper.FILE_EXT.length()) + "\" at:"));
				event.getEntityPlayer().addChatMessage(new TextComponentString("    " + generated.getAbsolutePath()));
			}
			else {
				event.getEntityPlayer().addChatMessage(new TextComponentString("[Info Wand] Failed to generate external nbt stats file!"));
			}
			event.setCanceled(true);
		}
    }
}