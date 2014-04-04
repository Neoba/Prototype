package openvcdiffjava;


public class Openvcdiffjava {

    static {
    	
        System.load("openvcdiffjavajni.dll");
    }

    public native byte[] vcdiffEncode(String dictionary, String target);
    public native String vcdiffDecode(String dictionary, byte[] delta);

}

