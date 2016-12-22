package toast.mobProperties.util;

import net.minecraft.command.CommandResultStats.Type;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class MobCommandSender implements ICommandSender {

	/// The entity sending commands.
	public final EntityLivingBase commandSender;

	public MobCommandSender(EntityLivingBase entity) {
		this.commandSender = entity;
	}

	@Override
	public boolean canCommandSenderUseCommand(int permissionLevel, String commandName) {
		return true;
	}

	@Override
	public String getName() {
		return this.commandSender.getName();
	}
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(this.getName());
	}

	@Override
	public Entity getCommandSenderEntity() {
		return this.commandSender;
	}
	@Override
	public BlockPos getPosition() {
		return new BlockPos(this.commandSender);
	}
	@Override
	public Vec3d getPositionVector() {
		return new Vec3d(this.commandSender.posX, this.commandSender.posY, this.commandSender.posZ);
	}
	
	@Override
	public World getEntityWorld() {
		return this.commandSender.worldObj;
	}
	@Override
	public MinecraftServer getServer() {
		return this.commandSender.getServer();
	}

	@Override
	public boolean sendCommandFeedback() {
		return false; // Do nothing
	}
	@Override
	public void addChatMessage(ITextComponent message) {
		// Do nothing
	}
	@Override
	public void setCommandStat(Type type, int amount) {
		// Do nothing
	}
}
