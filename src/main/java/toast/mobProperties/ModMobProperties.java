package toast.mobProperties;

import java.util.Random;

import net.minecraft.command.ServerCommandManager;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import toast.mobProperties.event.EventHandler;
import toast.mobProperties.event.TickHandler;
import toast.mobProperties.util.FileHelper;

@Mod(modid = ModMobProperties.MODID, name = "Mob Properties", version = ModMobProperties.VERSION)
public class ModMobProperties {
    /* TO DO *\
        > improvements
            > allow "lore" to accept string arrays
            > allow number ranges to accept number arrays
            > "Riding" tag needs to be implemented
            > stripped-down stats for nonliving entities for use in "spawn" function?
        > more conditions
            ?
        > expression evaluation engine
            > looting modifier *
            > nbt-defined variables
    \* ** ** */
    // This mod's id.
    public static final String MODID = "mob_properties";
    // This mod's version.
    public static final String VERSION = "1.0.3";

    // The mod's random number generator.
    public static final Random random = new Random();

    // Called before initialization. Loads the properties/configurations.
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Properties.load(new Configuration(event.getSuggestedConfigurationFile()));
        ModMobProperties.logDebug("Loading in debug mode!");
        FileHelper.init(event.getModConfigurationDirectory());
    }

    // Called during initialization. Registers entities, mob spawns, and renderers.
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        new TickHandler();
        new EventHandler();
    }

    // Called after initialization. Used to check for dependencies.
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ModMobProperties.log("Loading mob properties...");
        ModMobProperties.log("Loaded " + FileHelper.load() + " mob properties!");
        if (Properties.get().GENERAL.AUTO_GEN_FILES) {
            ModMobProperties.log("Generating default mob properties...");
            ModMobProperties.log("Generated " + FileHelper.generateDefaults() + " mob properties!");
        }
    }

    // Called as the server is starting.
    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        ServerCommandManager commandManager = (ServerCommandManager) event.getServer().getCommandManager();
        commandManager.registerCommand(new CommandReload());
        commandManager.registerCommand(new CommandInfoWand());
    }

    // Makes the first letter upper case.
    public static String cap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toUpperCase();
        return Character.toString(Character.toUpperCase(string.charAt(0))) + string.substring(1);
    }

    // Makes the first letter lower case.
    public static String decap(String string) {
        int length = string.length();
        if (length <= 0)
            return "";
        if (length == 1)
            return string.toLowerCase();
        return Character.toString(Character.toLowerCase(string.charAt(0))) + string.substring(1);
    }

	public static boolean debug() {
		return Properties.get().GENERAL.DEBUG;
	}

	// Prints the message to the console with this mod's name tag if debugging is enabled.
	public static void logDebug(String message) {
		if (ModMobProperties.debug()) ModMobProperties.log("(debug) " + message);
	}

	// Prints the message to the console with this mod's name tag.
	public static void log(String message) {
		System.out.println("[" + ModMobProperties.MODID + "] " + message);
	}

	// Prints the message to the console with this mod's name tag if debugging is enabled.
	public static void logWarning(String message) {
		ModMobProperties.log("[WARNING] " + message);
	}

	// Prints the message to the console with this mod's name tag if debugging is enabled.
	public static void logError(String message) {
		if (ModMobProperties.debug())
			throw new RuntimeException("[" + ModMobProperties.MODID + "] [ERROR] " + message);
		ModMobProperties.log("[ERROR] " + message);
	}

	// Throws a runtime exception with a message and this mod's name tag.
	public static void exception(String message) {
		throw new RuntimeException("[" + ModMobProperties.MODID + "] [FATAL ERROR] " + message);
	}
}