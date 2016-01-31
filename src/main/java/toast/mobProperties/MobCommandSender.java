package toast.mobProperties;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;

public class MobCommandSender implements ICommandSender {

	/// The entity sending commands.
	public final EntityLivingBase commandSender;

	public MobCommandSender(EntityLivingBase entity) {
		this.commandSender = entity;
	}

	@Override
	public String getCommandSenderName() {
		return this.commandSender.getCommandSenderName();
	}
	/// Gets the name of the command sender as a chat component.
	@Override
	public IChatComponent func_145748_c_() {
		return new ChatComponentText(this.getCommandSenderName());
	}

	@Override
	public void addChatMessage(IChatComponent message) {
		// Do nothing
	}

	@Override
	public boolean canCommandSenderUseCommand(int permissionLevel, String commandName) {
		return true;
	}

	@Override
	public ChunkCoordinates getPlayerCoordinates() {
		return new ChunkCoordinates((int) Math.floor(this.commandSender.posX), (int) Math.floor(this.commandSender.posY), (int) Math.floor(this.commandSender.posZ));
	}
	@Override
	public World getEntityWorld() {
		return this.commandSender.worldObj;
	}

}
