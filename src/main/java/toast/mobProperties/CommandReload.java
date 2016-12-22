package toast.mobProperties;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import toast.mobProperties.entry.MobProperties;
import toast.mobProperties.entry.PropertyExternal;
import toast.mobProperties.entry.drops.EntryDropsSchematic;
import toast.mobProperties.util.FileHelper;

public class CommandReload extends CommandBase {
    // Returns true if the given command sender is allowed to use this command.
    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return server.isSinglePlayer() || super.checkPermission(server, sender);
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
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        sender.addChatMessage(new TextComponentString("Reloading mob properties!"));

        ModMobProperties.log("Reloading mob properties...");
        MobProperties.unload();
        PropertyExternal.unload();
        EntryDropsSchematic.unload();
        ModMobProperties.log("Loaded " + FileHelper.load() + " mob properties!");
        if (Properties.get().GENERAL.AUTO_GEN_FILES) {
            ModMobProperties.log("Generating default mob properties...");
            ModMobProperties.log("Generated " + FileHelper.generateDefaults() + " mob properties!");
        }
    }
}