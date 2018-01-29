package stb.diagnose;


public class gsData {
	
	
	static{
		System.loadLibrary("stb_diagnose_eeprom");
	}

	public native static int initDevice();
	public native static void closeDevice();

	public native static String getMacAddress();
	public native static int setMacAddress(String maddr);

	public native static String getWifiMacAddress();
	public native static int setWifiMacAddress(String maddr);

//	public native static String getMcidNumber();
//	public native static void setMcidNumber(String mmcid);

	

}
