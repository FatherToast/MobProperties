package toast.mobProperties.api;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains several helper methods to allow properties to easily build DropEntry lists.
 */
public class DropsHelper {
    /**
     * Automatically adjusts the attempts or chance for a drop and returns the new chance.
     * The changes to attempts will be reflected in the original array, but the chance
     * must be assigned the return value of this to recieve its update, if any.
     *
     * @param counts The min and max number of times the new part will be called.
     * @param attempts The min and max number of times the current position can be called.
     * @param chance The chance for the current position to be reached at all.
     * @return The new value for the chance. May or may not be different.
     */
    public static double adjustAttemptsAndChance(double[] counts, double[] attempts, double chance) {
        if (counts[0] == counts[1] && counts[0] >= 0.0 && counts[0] <= 1.0) {
            chance *= counts[0];
        }
        else {
            attempts[0] *= counts[0];
            attempts[1] *= counts[1];
        }
        return chance;
    }

    /**
     * @param attempts The attempts array to copy.
     * @return A new array containing the same values as the attempts array.
     */
    public static double[] copy(double[] attempts) {
        return new double[] { attempts[0], attempts[1] };
    }

    /**
     * @param conditions The conditions list to copy.
     * @return A new list containing the same values as the conditions list.
     */
    public static List<String> copy(List<String> conditions) {
        return new ArrayList<String>(conditions);
    }
}
