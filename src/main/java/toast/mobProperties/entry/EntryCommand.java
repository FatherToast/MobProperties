package toast.mobProperties.entry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import toast.mobProperties.FileHelper;
import toast.mobProperties.IPropertyReader;
import toast.mobProperties.MobCommandSender;
import toast.mobProperties.MobPropertyException;

import com.google.gson.JsonObject;

public class EntryCommand extends EntryAbstract {

    // The min and max number of times to perform the task.
    private final double[] counts;
    // The name of the external function to use.
    private final String command;
    // If true, the command's output is suppressed.
    private final boolean noOutput;

    public EntryCommand(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.counts = FileHelper.readCounts(node, path, "count", 1.0, 1.0);

        this.command = FileHelper.readText(node, path, "value", "");
        if (this.command == "")
            throw new MobPropertyException("Missing or invalid command!", path);

        this.noOutput = FileHelper.readBoolean(node, path, "suppress_output", true);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "value" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "count", "suppress_output" };
    }

    // Initializes the entity's stats.
    @Override
    public void init(MobStatsInfo mobStats) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            ICommandManager commandManager = server.getCommandManager();

            ServerCommandManager admin = null; // Used for silencing commands
            if (this.noOutput && commandManager instanceof ServerCommandManager) {
				admin = (ServerCommandManager) commandManager;
				CommandBase.setAdminCommander(null);
			}

        	MobCommandSender commandSender = new MobCommandSender(mobStats.theEntity);
        	for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                commandManager.executeCommand(commandSender, this.command);
        	}

        	if (admin != null) { // Reapply command admin
				CommandBase.setAdminCommander(admin);
			}
        }
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        MinecraftServer server = MinecraftServer.getServer();
        if (server != null) {
            ICommandManager commandManager = server.getCommandManager();

            ServerCommandManager admin = null; // Used for silencing commands
            if (this.noOutput && commandManager instanceof ServerCommandManager) {
				admin = (ServerCommandManager) commandManager;
				CommandBase.setAdminCommander(null);
			}

        	MobCommandSender commandSender = new MobCommandSender(mobDrops.theEntity);
        	for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                commandManager.executeCommand(commandSender, this.command);
        	}

        	if (admin != null) { // Reapply command admin
				CommandBase.setAdminCommander(admin);
			}
        }
    }
}
