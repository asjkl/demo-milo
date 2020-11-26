package utils;

public final class Constants {
    private Constants() {
    	
    }

    public static final String HOST = "localhost";
    public static final int PORT = Integer.getInteger("port", 12686);
    public static final String PATH = "/milo";
}
