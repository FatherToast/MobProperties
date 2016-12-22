package toast.mobProperties.entry;

import com.google.gson.JsonObject;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandListener;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.server.MinecraftServer;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.event.MobStatsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobCommandSender;
import toast.mobProperties.util.MobPropertyException;

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
        MinecraftServer server = mobStats.theEntity.getServer();
        if (server != null) {
            ICommandManager commandManager = server.getCommandManager();
            ICommandListener commandListener = commandManager instanceof ICommandListener ? (ICommandListener) commandManager : null;

            if (this.noOutput && commandListener != null) {
				CommandBase.setCommandListener(null);
			}

        	MobCommandSender commandSender = new MobCommandSender(mobStats.theEntity);
        	for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                commandManager.executeCommand(commandSender, this.command);
        	}

        	if (this.noOutput && commandListener != null) { // Reapply command listener
				CommandBase.setCommandListener(commandListener);
			}
        }
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        MinecraftServer server = mobDrops.theEntity.getServer();
        if (server != null) {
            ICommandManager commandManager = server.getCommandManager();
            ICommandListener commandListener = commandManager instanceof ICommandListener ? (ICommandListener) commandManager : null;

            if (this.noOutput && commandListener != null) {
				CommandBase.setCommandListener(null);
			}

        	MobCommandSender commandSender = new MobCommandSender(mobDrops.theEntity);
        	for (int count = FileHelper.getCount(this.counts); count-- > 0;) {
                commandManager.executeCommand(commandSender, this.command);
        	}

        	if (this.noOutput && commandListener != null) { // Reapply command listener
				CommandBase.setCommandListener(commandListener);
			}
        }
    }
}
