package utils;

public final class Constants {
    private Constants() {
    	
    }

    public static final String HOST = "localhost"; 		//localhost													        //milo.digitalpetri.com
    public static final int PORT = Integer.getInteger("port", 12686); //12686 (localhost)								      //62541 (petri)
    public static final String PATH = "/milo";
    
    public static final String HOST_STERFIVE = "opcuademo.sterfive.com";
    public static final int PORT_STERFIVE = Integer.getInteger("port", 26543);
    public static final String PATH_STERFIVE = "";
    
}