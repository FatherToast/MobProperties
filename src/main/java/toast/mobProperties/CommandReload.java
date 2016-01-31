package toast.mobProperties;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import toast.mobProperties.entry.PropertyExternal;
import toast.mobProperties.entry.drops.EntryDropsSchematic;

public class CommandReload extends CommandBase {
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().isSinglePlayer() || super.canCommandSenderUseCommand(sender);
    }

    // The command name.
    @Override
    public String getCommandName() {
        return "mpreload";
    }

    // Returns the help string.
    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/mpreload - reloads all mob properties.";
    }

    // Executes the command.
    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        sender.addChatMessage(new ChatComponentText("Reloading mob properties!"));

        _MobPropertiesMod.console("Reloading mob properties...");
        MobProperties.unload();
        PropertyExternal.unload();
        EntryDropsSchematic.unload();
        _MobPropertiesMod.console("Loaded " + FileHelper.load() + " mob properties!");
        if (!EventHandler.DISABLED && Properties.getBoolean(Properties.GENERAL, "auto_generate_files")) {
            _MobPropertiesMod.console("Generating default mob properties...");
            _MobPropertiesMod.console("Generated " + FileHelper.generateDefaults() + " mob properties!");
        }
    }
}