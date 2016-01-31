package toast.mobProperties;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;

public class CommandInfoWand extends CommandBase {
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
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
    public void processCommand(ICommandSender sender, String[] args) {
        EntityPlayerMP player;
        if (args.length > 0) {
        	player = CommandBase.getPlayer(sender, args[0]);
        }
        else {
        	player = CommandBase.getCommandSenderAsPlayer(sender);
        }

        ItemStack infoWand = new ItemStack(Items.stick);
        infoWand.setStackDisplayName("\u00a7bMP Info Wand");
        NBTTagCompound displayTag = infoWand.stackTagCompound.getCompoundTag("display");
        NBTTagList tagList = new NBTTagList();
        tagList.appendTag(new NBTTagString("Right click a mob"));
        tagList.appendTag(new NBTTagString("or block write its"));
        tagList.appendTag(new NBTTagString("nbt data to a file"));
        tagList.appendTag(new NBTTagString("in json format"));
        displayTag.setTag("Lore", tagList);

        infoWand.stackTagCompound.setBoolean("MP|InfoWand", true); // Actually makes the item work

        EntityItem drop = player.dropPlayerItemWithRandomChoice(infoWand, false);
        drop.delayBeforeCanPickup = 0;

        sender.addChatMessage(new ChatComponentText("Right click a mob or block with this to write its data to a file."));
    }
}