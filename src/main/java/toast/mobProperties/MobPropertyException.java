package toast.mobProperties;

public class MobPropertyException extends RuntimeException
{
    public MobPropertyException(String comment, String path) {
        super(comment + " at " + path);
    }
    
    public MobPropertyException(String comment, String path, Exception ex) {
        super(comment + " at " + path, ex);
    }
}