package toast.mobProperties;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandInfoWand extends CommandBase {
	// The tag applied to info wands to make them work.
	public static final String TAG_INFOWAND = "MP|InfoWand";
	
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return server.isSinglePlayer() || super.checkPermission(server, sender);
    }

    // The command name.
    @Override
    public String getCommandName() {
        return "mpinfo";
    }

    // Returns the help string.
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mpinfo <player> - gives a stick that writes nbt data to a file in mp\'s nbt function format.";
    }

    // Executes the command.
    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP player;
        if (args.length > 0) {
        	player = CommandBase.getPlayer(server, sender, args[0]);
        }
        else {
        	player = CommandBase.getCommandSenderAsPlayer(sender);
        }

        ItemStack infoWand = new ItemStack(Items.STICK);
        infoWand.setStackDisplayName("\u00a7bMP Info Wand");
        NBTTagCompound displayTag = infoWand.getTagCompound().getCompoundTag("display");
        NBTTagList tagList = new NBTTagList();
        tagList.appendTag(new NBTTagString("Right click a mob"));
        tagList.appendTag(new NBTTagString("or block write its"));
        tagList.appendTag(new NBTTagString("nbt data to a file"));
        tagList.appendTag(new NBTTagString("in json format"));
        displayTag.setTag("Lore", tagList);

        infoWand.getTagCompound().setBoolean(TAG_INFOWAND, true); // Actually makes the item work

        EntityItem drop = player.dropItem(infoWand, false);
        if (drop != null) {
        	drop.setNoPickupDelay();
        	drop.setOwner(player.getName());
        }

        sender.addChatMessage(new TextComponentString("Right click a mob or block with this to write its data to a file."));
    }
}