package toast.mobProperties.entry.drops;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import toast.mobProperties.entry.EntryAbstract;
import toast.mobProperties.entry.IPropertyReader;
import toast.mobProperties.event.MobDropsInfo;
import toast.mobProperties.util.FileHelper;
import toast.mobProperties.util.MobPropertyException;

import com.google.gson.JsonObject;

public class EntryDropsSchematic extends EntryAbstract {

    // Mapping of all loaded external functions to their file name.
    private static final HashMap<String, Schematic> SCHEMATIC_MAP = new HashMap<String, Schematic>();

    // Unloads all properties.
    public static void unload() {
        EntryDropsSchematic.SCHEMATIC_MAP.clear();
    }

    // Loads a single schematic from NBT.
    public static void load(String path, String fileName, NBTTagCompound tag) {
        String name = fileName.substring(0, fileName.length() - FileHelper.SCHEMATIC_FILE_EXT.length());
        if (EntryDropsSchematic.SCHEMATIC_MAP.containsKey(name))
            throw new MobPropertyException("Duplicate schematic name! (name: " + name + ")", path);

        EntryDropsSchematic.SCHEMATIC_MAP.put(name, new Schematic(tag));
    }

    // The name of the schematic to use.
    private final String schematicName;
    // The code for the block update.
    private final byte update;
    // The min and max offsets.
    private final double[] offsetsX, offsetsY, offsetsZ;
    // The code for the override when placing anything other than air. 0=air, 1=all, 2=replaceable.
    private final byte blockOverride;
    // The code for the override when placing air. 0=air, 1=all, 2=replaceable.
    private final byte airOverride;

    public EntryDropsSchematic(String path, JsonObject root, int index, JsonObject node, IPropertyReader loader) {
        super(node, path);
        this.schematicName = FileHelper.readText(node, path, "file", "");
        if (this.schematicName == "")
            throw new MobPropertyException("Missing or invalid schematic file name!", path);

        this.update = (byte) FileHelper.readInteger(node, path, "update", 3);
        this.offsetsX = FileHelper.readCounts(node, path, "x", Double.NaN, Double.NaN);
        this.offsetsY = FileHelper.readCounts(node, path, "y", 0.0, 0.0);
        this.offsetsZ = FileHelper.readCounts(node, path, "z", Double.NaN, Double.NaN);

        String text = FileHelper.readText(node, path, "override", "true");
        if (text.equals("true")) {
            this.blockOverride = 1;
        }
        else if (text.equals("false")) {
            this.blockOverride = 0;
        }
        else if (text.equals("replaceable")) {
            this.blockOverride = 2;
        }
        else
			throw new MobPropertyException("Invalid override value! (must be true, false, or replaceable)", path);

        text = FileHelper.readText(node, path, "air_override", "replaceable");
        if (text.equals("true")) {
            this.airOverride = 1;
        }
        else if (text.equals("false")) {
            this.airOverride = 0;
        }
        else if (text.equals("replaceable")) {
            this.airOverride = 2;
        }
        else
			throw new MobPropertyException("Invalid air override value! (must be true, false, or replaceable)", path);
    }

    // Returns an array of required field names.
    @Override
    public String[] getRequiredFields() {
        return new String[] { "file" };
    }

    // Returns an array of optional field names.
    @Override
    public String[] getOptionalFields() {
        return new String[] { "update", "x", "y", "z", "override", "air_override" };
    }

    // Modifies the list of drops.
    @Override
    public void modifyDrops(MobDropsInfo mobDrops) {
        if (!mobDrops.theEntity.worldObj.isRemote) {
            Schematic schematic = EntryDropsSchematic.SCHEMATIC_MAP.get(this.schematicName);
            if (schematic != null) {
	            int x = (int) Math.floor(mobDrops.theEntity.posX);
	            if (Double.isNaN(this.offsetsX[0])) {
	            	x -= schematic.getXSize() >> 1;
	            }
	            else {
					x += FileHelper.getCount(this.offsetsX);
				}
	            int y = (int) Math.floor(mobDrops.theEntity.posY) + FileHelper.getCount(this.offsetsY);
	            int z = (int) Math.floor(mobDrops.theEntity.posZ);
	            if (Double.isNaN(this.offsetsZ[0])) {
	            	z -= schematic.getZSize() >> 1;
	            }
	            else {
					z += FileHelper.getCount(this.offsetsZ);
				}

            	schematic.place(mobDrops.theEntity.worldObj, x, y, z, this.update, this.blockOverride, this.airOverride);
            }
            else {
                super.modifyDrops(mobDrops);
            }
        }
    }
}
