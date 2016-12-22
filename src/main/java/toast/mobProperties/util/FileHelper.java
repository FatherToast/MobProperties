package toast.mobProperties.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import toast.mobProperties.ModMobProperties;
import toast.mobProperties.entry.IProperty;
import toast.mobProperties.entry.MobProperties;
import toast.mobProperties.entry.PropertyExternal;
import toast.mobProperties.entry.drops.EntryDropsSchematic;

public abstract class FileHelper {
    // The directory for config files.
    public static File CONFIG_DIRECTORY;
    // The main directory for mob properties.
    public static File PROPS_DIRECTORY;
    // The directory for external functions.
    public static File EXTERNAL_DIRECTORY;
    // The directory for schematic files.
    public static File SCHEMATIC_DIRECTORY;

    // The file extention for mob properties files.
    public static final String FILE_EXT = ".json";
    // The file extention for schematic files.
    public static final String SCHEMATIC_FILE_EXT = ".schematic";
    // The json parser object for reading json.
    private static final JsonParser PARSER = new JsonParser();
    // The gson objects for writing json.
    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Gson GSON_COMPACT = new GsonBuilder().disableHtmlEscaping().create();

    // Special characters recognized by the property reader.
    public static final char CHAR_RAND = '~';
    public static final char CHAR_INVERT = '!';

    // Initializes this file helper.
    public static void init(File directory) {
        FileHelper.CONFIG_DIRECTORY = directory;
        FileHelper.PROPS_DIRECTORY = new File(directory, "MobProperties");
        FileHelper.PROPS_DIRECTORY.mkdirs();
        FileHelper.EXTERNAL_DIRECTORY = new File(directory, "MobPropertiesExternal");
        FileHelper.EXTERNAL_DIRECTORY.mkdirs();
        FileHelper.SCHEMATIC_DIRECTORY = new File(directory, "MobPropertiesSchematics");
        FileHelper.SCHEMATIC_DIRECTORY.mkdirs();
    }

    // Generates a json file representing all the nbt data given and returns the generated file.
    public static File generateNbtStats(String entityName, NBTTagCompound tag) {
    	File directory = new File(FileHelper.EXTERNAL_DIRECTORY, "nbt");
    	directory.mkdirs();

        String fileName = entityName;
        char[] fileNameArray = fileName.toCharArray();
        fileName = "";
        for (char letter : fileNameArray) {
            fileName += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
        }
        try {
        	// Ensure the file does not already exist
        	File propFile = new File(directory, fileName + FileHelper.FILE_EXT);
            if (propFile.exists()) {
            	fileName += "_";
                int attempt = 0;
                for (; attempt <= 999; attempt++)
                    if (!(propFile = new File(directory, fileName + attempt + FileHelper.FILE_EXT)).exists()) {
                        break;
                    }
                if (attempt > 999) {
                    ModMobProperties.logWarning("Too many similar files for \"" + entityName + "\"! Cancelled external nbt function generator.");
                    return null;
                }
                fileName += attempt;
            }

            // Build and write the Json file
            JsonObject props = new JsonObject();
            props.addProperty("_comment", "Auto-generated nbt function for " + entityName);
            props.addProperty("function", "all");
            props.addProperty("count", "1.0~1.0");
            props.add("functions", FileHelper.tagsToJsonArray(tag));
            propFile.createNewFile();
            FileWriter out = new FileWriter(propFile);
            FileHelper.GSON_PRETTY.toJson(props, out);
            out.close();
            return propFile;
        }
        catch (Exception ex) {
            ModMobProperties.logWarning("Failed to generate external nbt function for \"" + entityName + "\": " + tag.toString());
            ex.printStackTrace();
        }
        return null;
    }
    // Recursively turns an nbt tag compound into an array of Json function objects.
    private static JsonArray tagsToJsonArray(NBTTagCompound compound) {
    	JsonArray array = new JsonArray();
        for (String name : compound.getKeySet()) {
        	array.add(FileHelper.tagToJson(name, compound.getTag(name), compound));
        }
        return array;
    }
    // Recursively turns an nbt tag list into an array of Json function objects.
    private static JsonArray tagsToJsonArray(NBTTagList list) {
    	// Makes a temp copy to avoid clearing the original, as now the only
    	// way to get a list's NBTBase objects is by removing them
    	NBTTagList listCopy = list.copy();
    	JsonArray array = new JsonArray();
        while (listCopy.tagCount() > 0) {
        	array.add(FileHelper.tagToJson(null, listCopy.removeTag(0), null));
        }
        return array;
    }
    // Returns a single nbt tag as a Json object. (Parent is often null.)
    private static JsonObject tagToJson(String name, NBTBase tag, NBTTagCompound parent) {
        JsonObject json = new JsonObject();
        if (name != null) {
        	json.addProperty("name", name);
        }

        Class tagClass = tag.getClass();
        // Nbt objects
        if (NBTTagCompound.class.equals(tagClass)) {
        	json.addProperty("function", "compound");
        	json.add("tags", FileHelper.tagsToJsonArray((NBTTagCompound) tag));
        }
        else if (NBTTagList.class.equals(tagClass)) {
        	json.addProperty("function", "list");
        	json.add("tags", FileHelper.tagsToJsonArray((NBTTagList) tag));
        }
        // Nbt primitives
        else if (NBTTagString.class.equals(tagClass)) {
        	json.addProperty("function", "string");
        	json.addProperty("value", ((NBTTagString) tag).getString());
        }
        else if (NBTTagByte.class.equals(tagClass)) {
	        // Check if it is potentially a potion id
			try {
	            if (parent != null && "Id".equals(name)) {
	            	Set<String> parentNames = parent.getKeySet();
	            	if (parentNames.size() == 4 && parentNames.contains("Amplifier") && parentNames.contains("Duration") && parentNames.contains("Ambient")) {
	    	        	json.addProperty("function", "potion_id");
	    	        	String value = Potion.REGISTRY.getNameForObject(Potion.getPotionById(((NBTTagByte) tag).getByte())).toString();
	    	        	json.addProperty("value", value);
	            		return json;
	            	}
	            }
			}
			catch (Exception ex) {
				// Standard takes precedence in case of error
			}

			// Standard byte
        	json.addProperty("function", "byte");
        	String value = Byte.toString(((NBTTagByte) tag).getByte());
        	json.addProperty("value", value + "~" + value);
        }
        else if (NBTTagByteArray.class.equals(tagClass)) {
        	json.addProperty("function", "byte_array");
        	JsonArray value = new JsonArray();
        	String subVal;
        	for (byte entry : ((NBTTagByteArray) tag).getByteArray()) {
        		subVal = Byte.toString(entry);
        		value.add(new JsonPrimitive(subVal + "~" + subVal));
        	}
        	json.add("value", value);
        }
        else if (NBTTagShort.class.equals(tagClass)) {
            // Check if it is potentially an item or enchant id
    		try {
	            if (parent != null && "id".equals(name)) {
	            	Set<String> parentNames = parent.getKeySet();
	            	if (parentNames.size() == 2 && parentNames.contains("lvl")) {
	    	        	json.addProperty("function", "enchant_id");
	    	        	String value = Enchantment.REGISTRY.getNameForObject(Enchantment.getEnchantmentByID(((NBTTagShort) tag).getShort())).toString();
	    	        	json.addProperty("value", value);
	            		return json;
	            	}
	            	else if (parentNames.contains("Count") && parentNames.contains("Damage")) {
	    	        	json.addProperty("function", "item_id");
	    	        	String value = Item.REGISTRY.getNameForObject(Item.getItemById(((NBTTagShort) tag).getShort())).toString();
	    	        	json.addProperty("value", value);
	            		return json;
	            	}
	            }
    		}
    		catch (Exception ex) {
    			// Standard takes precedence in case of error
    		}

    		// Standard short
        	json.addProperty("function", "short");
        	String value = Short.toString(((NBTTagShort) tag).getShort());
        	json.addProperty("value", value + "~" + value);
        }
        else if (NBTTagInt.class.equals(tagClass)) {
        	json.addProperty("function", "int");
        	String value = Integer.toString(((NBTTagInt) tag).getInt());
        	json.addProperty("value", value + "~" + value);
        }
        else if (NBTTagIntArray.class.equals(tagClass)) {
        	json.addProperty("function", "int_array");
        	JsonArray value = new JsonArray();
        	String subVal;
        	for (int entry : ((NBTTagIntArray) tag).getIntArray()) {
        		subVal = Integer.toString(entry);
        		value.add(new JsonPrimitive(subVal + "~" + subVal));
        	}
        	json.add("value", value);
        }
        else if (NBTTagLong.class.equals(tagClass)) {
        	json.addProperty("function", "long");
        	String value = Long.toString(((NBTTagLong) tag).getLong());
        	json.addProperty("value", value);
        }
        else if (NBTTagFloat.class.equals(tagClass)) {
        	json.addProperty("function", "float");
        	String value = Float.toString(((NBTTagFloat) tag).getFloat());
        	json.addProperty("value", value + "~" + value);
        }
        else if (NBTTagDouble.class.equals(tagClass)) {
        	json.addProperty("function", "double");
        	String value = Double.toString(((NBTTagDouble) tag).getDouble());
        	json.addProperty("value", value + "~" + value);
        }
        // Special cases will have already returned
        return json;
    }

    // Loads all mob properties into memory.
    public static int load() {
        int filesLoaded = 0;
        String[] types = { "drops", "items", "nbt", "stats" };
        File externalDir;
        for (String type : types) {
            externalDir = new File(FileHelper.EXTERNAL_DIRECTORY, type);
            externalDir.mkdirs();
            filesLoaded += FileHelper.loadExternalDirectory(type, externalDir);
        }
        filesLoaded += FileHelper.loadDirectory(FileHelper.PROPS_DIRECTORY);
        filesLoaded += FileHelper.loadSchematicDirectory(FileHelper.SCHEMATIC_DIRECTORY);
        return filesLoaded;
    }

    // Recursively loads the mob properties in the given directory.
    private static int loadDirectory(File directory) {
        int filesLoaded = 0;
        JsonObject node;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            node = FileHelper.loadFile(propFile);
            MobProperties.load(propFile.getPath(), node);
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadDirectory(subDirectory);
        }
        return filesLoaded;
    }

    // Recursively loads the external functions in the given directory.
    private static int loadExternalDirectory(String type, File directory) {
        int filesLoaded = 0;
        for (File propFile : directory.listFiles(new ExtensionFilter(FileHelper.FILE_EXT))) {
            PropertyExternal.load(type, propFile.getPath(), propFile.getName(), FileHelper.loadFile(propFile));
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadExternalDirectory(type, subDirectory);
        }
        return filesLoaded;
    }

    // Recursively loads the schematics in the given directory.
    private static int loadSchematicDirectory(File directory) {
        int filesLoaded = 0;
        for (File schematicFile : directory.listFiles(new ExtensionFilter(FileHelper.SCHEMATIC_FILE_EXT))) {
        	try {
        		EntryDropsSchematic.load(schematicFile.getPath(), schematicFile.getName(), FileHelper.loadUncompressedNBTFile(schematicFile));
        	}
        	catch (MobPropertyException ex) {
        		EntryDropsSchematic.load(schematicFile.getPath(), schematicFile.getName(), FileHelper.loadNBTFile(schematicFile));
        	}
            filesLoaded++;
        }
        for (File subDirectory : directory.listFiles(new FolderFilter())) {
            filesLoaded += FileHelper.loadSchematicDirectory(subDirectory);
        }
        return filesLoaded;
    }

    // Loads a file as a Json node object. Throws an exception if it fails.
    private static JsonObject loadFile(File propFile) {
        JsonElement node = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(propFile)));
            node = FileHelper.PARSER.parse(in);
            in.close();
        }
        catch (Exception ex) {
            throw new MobPropertyException("Error reading file! (See \"Caused by:\" below for more info.)", propFile.getPath(), ex);
        }
        if (node == null)
            throw new MobPropertyException("Failed to read file!", propFile.getPath());
        if (!node.isJsonObject())
            throw new MobPropertyException("Invalid file! (non-object)", propFile.getPath());
        return node.getAsJsonObject();
    }

    // Loads a file as an NBT object. Throws an exception if it fails.
	private static NBTTagCompound loadNBTFile(File nbtFile) {
        try {
            if (nbtFile.exists())
				return CompressedStreamTools.readCompressed(new FileInputStream(nbtFile));
        }
        catch (Exception ex) {
            throw new MobPropertyException("Error reading nbt file!", nbtFile.getPath(), ex);
        }
        throw new MobPropertyException("Failed to read nbt file!", nbtFile.getPath());
    }

    // Loads an uncompressed file as an NBT object. Throws an exception if it fails.
    private static NBTTagCompound loadUncompressedNBTFile(File nbtFile) {
        try {
            if (nbtFile.exists())
				return CompressedStreamTools.read(new DataInputStream(new FileInputStream(nbtFile)));
        }
        catch (Exception ex) {
            throw new MobPropertyException("Error reading uncompressed nbt file!", nbtFile.getPath(), ex);
        }
        throw new MobPropertyException("Failed to read uncompressed nbt file!", nbtFile.getPath());
    }

    // Generates a trivial property file for each entity id, if a file does not already exist.
    public static int generateDefaults() {
        int filesGenerated = 0;

        // Generate or load default file.
        JsonObject defaultProps;
        File defaultPropFile = new File(FileHelper.CONFIG_DIRECTORY, "MobProperties.json");
        if (!defaultPropFile.exists()) {
            defaultProps = new JsonObject();
            defaultProps.addProperty("_comment", "This is the default Mop Properties file. When this mod generates a default file for any mob, it will be an auto-formatted copy of this file. Remember, comments outside of functions (such as this one) will not be copied.");
            defaultProps.add("drops", new JsonArray());
            defaultProps.add("pre_stats", new JsonArray());
            defaultProps.add("stats", new JsonArray());
            try {
                defaultPropFile.createNewFile();
                FileWriter out = new FileWriter(defaultPropFile);
                FileHelper.GSON_PRETTY.toJson(defaultProps, out);
                out.close();
                filesGenerated++;
            }
            catch (Exception ex) {
                ModMobProperties.logWarning("Failed to generate the default properties file!");
            }
        }
        else {
            defaultProps = FileHelper.loadFile(defaultPropFile);
        }

        // Generate or load each mob file.
        String fileName;
        File directory, propFile;
        JsonObject props;
        for (Entry<String, Class<? extends Entity>> mobEntry : EntityList.NAME_TO_CLASS.entrySet()) {
            if (MobProperties.getProperties(mobEntry.getKey()) == null && EntityLivingBase.class.isAssignableFrom(mobEntry.getValue()) && !Modifier.isAbstract(mobEntry.getValue().getModifiers())) {
                directory = FileHelper.PROPS_DIRECTORY;
                fileName = mobEntry.getKey();
                String[] split = fileName.split("\\.", 2);
                if (mobEntry.getValue().getName().startsWith("net.minecraft.")) {
                	if (split.length > 1) split[0] = split[0] + "." + split[1]; // Just in case a vanilla id has a "."
                	split = new String[] { "minecraft", split[0] };
                }
                if (split.length > 1) {
                    fileName = split[1];
                    char[] dirNameArray = split[0].toCharArray();
                    split[0] = "";
                    for (char letter : dirNameArray) {
                        split[0] += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
                    }
                    directory = new File(directory, split[0]);
                    directory.mkdirs();
                }
                char[] fileNameArray = fileName.toCharArray();
                fileName = "";
                for (char letter : fileNameArray) {
                    fileName += Character.isLetterOrDigit(letter) ? Character.toString(letter) : "_";
                }
                try {
                    propFile = new File(directory, fileName + FileHelper.FILE_EXT);
                    if (propFile.exists()) {
                        int attempt = 0;
                        for (; attempt < 100; attempt++)
                            if (! (propFile = new File(directory, fileName + attempt + FileHelper.FILE_EXT)).exists()) {
                                break;
                            }
                        if (attempt > 99) {
                            ModMobProperties.logWarning("Failed to generate default properties file for \"" + mobEntry.getKey() + "\"!");
                            continue;
                        }
                        fileName += attempt;
                    }
                    props = new JsonObject();
                    props.addProperty("_name", mobEntry.getKey());
                    for (Map.Entry<String, JsonElement> entry : defaultProps.entrySet()) {
                        if (entry.getKey() != null && !entry.getKey().equals("_name") && !entry.getKey().equals("_comment")) {
                            props.add(entry.getKey(), entry.getValue());
                        }
                    }
                    propFile.createNewFile();
                    FileWriter out = new FileWriter(propFile);
                    FileHelper.GSON_PRETTY.toJson(props, out);
                    out.close();
                    filesGenerated++;
                    MobProperties.load(propFile.getPath(), props);
                }
                catch (MobPropertyException ex) {
                    throw ex;
                }
                catch (Exception ex) {
                    ModMobProperties.logWarning("Failed to generate default properties file for \"" + mobEntry.getKey() + "\"!");
                    ex.printStackTrace();
                }
            }
        }
        return filesGenerated;
    }

    // Makes sure that only defined fields are being used.
    public static void verify(JsonObject node, String path, IProperty property) {
        List<String> required = Arrays.asList(property.getRequiredFields());
        List<String> optional = Arrays.asList(property.getOptionalFields());
        HashSet<String> allowed = new HashSet<String>();
        allowed.addAll(required);
        allowed.addAll(optional);
        allowed.add("_comment");
        if (! (property instanceof MobProperties)) {
            allowed.add("function");
        }
        if (path.matches("^.*\\\\entry_[0-9]+\\(choose\\)\\\\functions\\\\entry_[0-9]+\\(\\w+\\)$")) {
            allowed.add("weight");
        }

        try {
            Set<Map.Entry<String, JsonElement>> fields = node.entrySet();
            HashSet<String> fieldNames = new HashSet<String>();
            for (Map.Entry<String, JsonElement> entry : fields) {
                fieldNames.add(entry.getKey());
            }

            for (String name : required)
                if (!fieldNames.contains(name))
                    throw new MobPropertyException("Verify error! Missing required field \"" + name + "\". (Required fields: " + Arrays.toString(property.getRequiredFields()) + ")", path);
            for (String name : fieldNames)
                if (!allowed.contains(name))
                    throw new MobPropertyException("Verify error! Invalid field \"" + name + "\". (Allowed fields: " + Arrays.toString(allowed.toArray(new String[0])) + ")", path);
        }
        catch (IllegalStateException ex) {
            throw new MobPropertyException("Verify error! (functions must be objects)", path);
        }
    }

    // Returns a function as a compact string.
    public static String getFunctionString(JsonObject node, String path) {
        try {
            return FileHelper.GSON_COMPACT.toJson(node);
        }
        catch (Exception ex) {
            throw new MobPropertyException("Error generating function string!", path, ex);
        }
    }

    // Loads a function from the given string.
    public static JsonObject loadFunctionFromString(String path, String prop, int index) {
        try {
            path += "\\entry_" + (index + 1);
            JsonObject node = null;
            try {
                node = FileHelper.PARSER.parse(prop).getAsJsonObject();
            }
            catch (Exception ex) {
                throw new MobPropertyException("Error loading function string! " + prop, path, ex);
            }
            if (node == null)
                throw new MobPropertyException("Failed to load function string! " + prop, path);
            if (!node.isJsonObject())
                throw new MobPropertyException("Invalid function string! (non-object) " + prop, path);

            return node;
        }
        catch (MobPropertyException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Returns a randomized double within the values' range.
    public static double getValue(double[] values) {
        return FileHelper.getCount(values, ModMobProperties.random);
    }
    public static double getValue(double[] values, Random random) {
        if (values[0] == values[1])
            return values[0];
        return random.nextDouble() * (values[1] - values[0]) + values[0];
    }

    // Returns a randomized integer within the counts' range.
    public static int getCount(double[] counts) {
        return FileHelper.getCount(counts, ModMobProperties.random);
    }
    public static int getCount(double[] counts, Random random) {
        double count = FileHelper.getValue(counts, random);
        int intCount = (int) Math.floor(count);
        count -= intCount;
        if (0.0 < count && random.nextDouble() < count) {
            intCount++;
        }
        return intCount;
    }

    // Returns a randomized long within the counts' range.
    public static long getLongCount(double[] counts) {
        return FileHelper.getLongCount(counts, ModMobProperties.random);
    }
    public static long getLongCount(double[] counts, Random random) {
        double count = FileHelper.getValue(counts, random);
        long longCount = (long) Math.floor(count);
        count -= longCount;
        if (0.0 < count && random.nextDouble() < count) {
            longCount++;
        }
        return longCount;
    }

    // Returns the text of the node, or the default.
    public static String readText(JsonObject node, String path, String tag, String defaultValue) {
        try {
            return node.get(tag).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid value for \"" + tag + "\"! (wrong node type)", path);
        }
    }

    // Returns the boolean value of the node, or the default.
    public static boolean readBoolean(JsonObject node, String path, String tag, boolean defaultValue) {
        String text = FileHelper.readText(node, path, tag, Boolean.toString(defaultValue));
        if (text.equals("false"))
            return false;
        else if (text.equals("true"))
            return true;
        throw new MobPropertyException("Invalid boolean value! (" + text + ": must be true or false)", path);
    }

    // Reads the line part as a number range.
    public static double[] readCounts(JsonObject node, String path, String tag, int index, double defaultMin, double defaultMax) {
        path += "\\" + tag + "\\entry_" + (index + 1);
        String value = "";
        try {
            value = node.getAsJsonArray(tag).get(index).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return new double[] { defaultMin, defaultMax };
        }
        catch (IndexOutOfBoundsException ex) {
            throw new MobPropertyException("Unexpected error! (array index out of bounds)", path);
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid number range! (wrong node type)", path);
        }
        return FileHelper.readCounts(value, path);
    }
    public static double[] readCounts(JsonObject node, String path, String tag, double defaultMin, double defaultMax) {
        path += "\\" + tag;
        String value = "";
        try {
            value = node.get(tag).getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return new double[] { defaultMin, defaultMax };
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid number range! (wrong node type)", path);
        }
        return FileHelper.readCounts(value, path);
    }
    private static double[] readCounts(String value, String path) {
        double[] counts = new double[2];
        String[] subParts = value.split(Character.toString(FileHelper.CHAR_RAND));
        try {
            if (subParts[0].startsWith("0x")) {
                counts[0] = Integer.parseInt(subParts[0].substring(2), 16);
            }
            else {
                counts[0] = Double.valueOf(subParts[0]).doubleValue();
            }
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid number range! (" + subParts[0].trim() + ")", path);
        }
        if (subParts.length == 1) {
            counts[1] = counts[0];
        }
        else if (subParts.length == 2) {
            try {
                if (subParts[1].startsWith("0x")) {
                    counts[1] = Integer.parseInt(subParts[1].substring(2), 16);
                }
                else {
                    counts[1] = Double.valueOf(subParts[1]).doubleValue();
                }
            }
            catch (Exception ex) {
                throw new MobPropertyException("Invalid number range! (" + subParts[1].trim() + ")", path);
            }
        }
        else
            throw new MobPropertyException("Invalid number range! (too many \'~\'s)", path);
        if (Double.isNaN(counts[0]) || Double.isNaN(counts[1]) || Double.isInfinite(counts[0]) || Double.isInfinite(counts[1]))
            throw new MobPropertyException("Invalid number range! (NaN and Infinity are not allowed)", path);
        if (counts[0] > counts[1]) {
            double temp = counts[0];
            counts[0] = counts[1];
            counts[1] = temp;
        }
        return counts;
    }

    // Reads the object's weight.
    public static int readWeight(JsonObject node, String path, int defaultValue) {
        String value = "";
        try {
            value = node.get("weight").getAsString();
        }
        catch (NullPointerException ex) { // The object does not exist.
            return defaultValue;
        }
        catch (IndexOutOfBoundsException ex) {
            throw new MobPropertyException("Unexpected error! (array index out of bounds)", path);
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid number range! (wrong node type)", path);
        }

        try {
            int weight = Integer.parseInt(value);
            if (weight < 0)
                throw new MobPropertyException("Invalid weight! (" + value + ": must be non-negative)", path);
            return weight;
        }
        catch (NumberFormatException ex) {
            throw new MobPropertyException("Invalid weight! (" + value + ")", path, ex);
        }
    }

    // Reads the line part as an integer.
    public static int readInteger(JsonObject node, String path, String tag, int defaultValue) {
        path += "\\" + tag;
        String value = "";
        try {
            value = node.get(tag).getAsString();
        }
        catch (NullPointerException ex) {
            return defaultValue;
        }
        catch (Exception ex) {
            throw new MobPropertyException("Invalid integer! (wrong node type)", path);
        }

        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw new MobPropertyException("Invalid integer! (" + value + ")", path, ex);
        }
    }

    // Reads the line and optionally throws an exception if it does not represent a valid equipment slot.
    public static EntityEquipmentSlot readSlot(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readSlot(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid equipment slot.
    public static EntityEquipmentSlot readSlot(String id, String path, boolean required) {
    	EntityEquipmentSlot slot = null;
    	try {
    		slot = EntityEquipmentSlot.fromString(id.toLowerCase());

    		// Legacy ids
    		if (slot == null) {
    			int index = Integer.parseInt(id);
    	    	for (EntityEquipmentSlot slotType : EntityEquipmentSlot.values()) {
    	    		if (index == slotType.getSlotIndex()) {
    	    			slot = slotType;
    	    			break;
    	    		}
    	    	}
    	    	if (slot != null) {
                    ModMobProperties.logWarning("Usage of numerical slot id! (" + id + "=\"" + slot.getName() + "\") at " + path);
                }
    		}
    	}
    	catch (Exception ex) {
    		// Do nothing
    	}

        if (required && slot == null) {
        	if (id.length() > 0) {
        		String helpText = "";
    	    	for (EntityEquipmentSlot slotType : EntityEquipmentSlot.values()) {
    	    		helpText = helpText + ", \"" + slotType.getName() + "\"";
    	    	}
    	    	helpText = helpText.substring(2);
        		ModMobProperties.log("[INFO] Valid slot ids: " + helpText);
        	}
            throw new MobPropertyException("Missing or invalid slot! (" + id + ")", path);
        }
        return slot;
    }

    // Reads the line and throws an exception if it does not represent a valid item.
    public static Item readItem(JsonObject node, String path, String tag) {
        return FileHelper.readItem(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid item.
    public static Item readItem(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readItem(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid item.
    public static Item readItem(String id, String path, boolean required) {
        Item item = Item.REGISTRY.getObject(new ResourceLocation(id));

        // Legacy ids
        if (item == null) {
            try {
                item = Item.getItemById(Integer.parseInt(id));
                if (item != null) {
                    ModMobProperties.logWarning("Usage of numerical item id! (" + id + "=\"" + Item.REGISTRY.getNameForObject(item) + "\") at " + path);
                }
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
        }

        if (required && item == null)
            throw new MobPropertyException("Missing or invalid item! (" + id + ")", path);
        return item;
    }

    // Reads the line and throws an exception if it does not represent a valid block.
    public static Block readBlock(JsonObject node, String path, String tag) {
        return FileHelper.readBlock(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid block.
    public static Block readBlock(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readBlock(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid block.
    public static Block readBlock(String id, String path, boolean required) {
    	if (id.equals("air") || id.equals("minecraft:air"))
    		return Blocks.AIR;
        Block block = Block.REGISTRY.getObject(new ResourceLocation(id));

        // Legacy ids
        if (block == null || block == Blocks.AIR) {
            try {
                block = Block.getBlockById(Integer.parseInt(id));
                if (block != null && block != Blocks.AIR) {
                    ModMobProperties.logWarning("Usage of numerical block id! (" + id + "=\"" + Block.REGISTRY.getNameForObject(block) + "\") at " + path);
                }
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
        }

        if (required && (block == null || block == Blocks.AIR))
            throw new MobPropertyException("Missing or invalid block! (" + id + ")", path);
        return block;
    }

    // Reads the line and throws an exception if it does not represent a valid potion.
    public static Potion readPotion(JsonObject node, String path, String tag) {
        return FileHelper.readPotion(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid potion.
    public static Potion readPotion(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readPotion(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid potion.
    public static Potion readPotion(String id, String path, boolean required) {
        Potion potion = Potion.getPotionFromResourceLocation(id);

    	// Legacy ids
    	if (potion == null) {
    		try {
    			potion = Potion.getPotionById(Integer.parseInt(id));
                if (potion != null) {
                    ModMobProperties.logWarning("Usage of numerical potion id! (" + id + "=\"" + Potion.REGISTRY.getNameForObject(potion) + "\") at " + path);
                }
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                // Do nothing
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
    	}
    	if (potion == null) {
    		String idCmp = id.startsWith("potion.") ? "effect" + id.substring("potion".length()) : id;

	    	for (Iterator<Potion> itr = Potion.REGISTRY.iterator(); itr.hasNext();) {
	    		Potion potionType = itr.next();
	    		if (potionType != null && idCmp.equals(potionType.getName())) {
	    			potion = potionType;
	    			break;
	    		}
	    	}
            if (potion != null) {
                ModMobProperties.logWarning("Usage of lang key potion id! (\"" + id + "\"=\"" + Potion.REGISTRY.getNameForObject(potion) + "\") at " + path);
            }
    	}

        if (required && potion == null)
			throw new MobPropertyException("Missing or invalid potion! (" + id + ")", path);
        return potion;
    }

    // Reads the line and throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(JsonObject node, String path, String tag) {
        return FileHelper.readEnchant(node, path, tag, true);
    }
    // Reads the line and optionally throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(JsonObject node, String path, String tag, boolean required) {
        return FileHelper.readEnchant(FileHelper.readText(node, path, tag, ""), path, required);
    }
    // Reads the text and optionally throws an exception if it does not represent a valid enchantment.
    public static Enchantment readEnchant(String id, String path, boolean required) {
    	Enchantment enchant = Enchantment.getEnchantmentByLocation(id);

    	// Legacy ids
    	if (enchant == null) {
    		try {
    			enchant = Enchantment.getEnchantmentByID(Integer.parseInt(id));
                if (enchant != null) {
                    ModMobProperties.logWarning("Usage of numerical enchantment id! (" + id + "=\"" + Enchantment.REGISTRY.getNameForObject(enchant) + "\") at " + path);
                }
            }
            catch (ArrayIndexOutOfBoundsException ex) {
                // Do nothing
            }
            catch (NumberFormatException ex) {
                // Do nothing
            }
    	}
    	if (enchant == null) {
	    	for (Iterator<Enchantment> itr = Enchantment.REGISTRY.iterator(); itr.hasNext();) {
	    		Enchantment enchantType = itr.next();
	    		if (enchantType != null && id.equals(enchantType.getName())) {
	    			enchant = enchantType;
	    			break;
	    		}
	    	}
            if (enchant != null) {
                ModMobProperties.logWarning("Usage of lang key enchantment id! (\"" + id + "\"=\"" + Enchantment.REGISTRY.getNameForObject(enchant) + "\") at " + path);
            }
    	}

        if (required && enchant == null)
			throw new MobPropertyException("Missing or invalid enchantment! (" + id + ")", path);
        return enchant;
    }

    /// All the file filters used.
    public static class ExtensionFilter implements FilenameFilter {
        // The file extension to accept.
        private final String extension;

        public ExtensionFilter(String ext) {
            this.extension = ext;
        }

        // Returns true if the file should be accepted.
        @Override
        public boolean accept(File file, String name) {
            return name.toLowerCase().endsWith(this.extension);
        }
    }

    public static class FolderFilter implements FileFilter {
        public FolderFilter() {
        }

        /// Returns true if the file should be accepted.
        @Override
        public boolean accept(File file) {
            return file.isDirectory();
        }
    }
}
