package stb.diagnose.util;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;

public class E2PFileRW {

	private static String file_name="/dev/block/platform/sdhci-tegra.3/by-name/E2P";
	private static String TAG="E2PFileRW";
	static File file=null;
	public static void initDevice(){
		file=new File(file_name);
		if(file==null){
			Log.i(TAG,"open file failed");
		}
	}
	
	public static void closeDevice(){
	}
	
	public static String getMacAddress(){
		return read(0, 12);
	}
    public static String getWifiMacAddress(){
        return read(16,28);
    }
	
	public static String getMcidNumber(){
		return read(1024,1040);
	}
	
	public  static void setMacAddress(String maddr){
		write(maddr, 0);
	}
    public static void setWifiMacAddress(String maddr){
        write(maddr,16);
    }
	public  static boolean setMcidNumber(String mmcid){
		return write(mmcid,1024);
	}
	
	public static String read(int from , int to){
        String result="";
        try{
            FileInputStream fis=new FileInputStream(file);
            BufferedInputStream bis=new BufferedInputStream(fis);
            bis.skip(from);
            int c=0;
            for(int i=0;(i<to-from)&&(c=bis.read())!=-1;i++){
            	if(c>122||c<=0){
            		continue;
            	}else{
            		result+=(char)c;
            	}
            }
            bis.close();
            fis.close();
        }catch(Exception e){
        	Log.e(TAG,e.getMessage());
        }
        return result;
    }
	
	public static boolean write(String str, int pos){
		boolean ok=false;
	    try {
	        RandomAccessFile raf = new RandomAccessFile(file,"rw");
	        byte[] b = str.getBytes();
	        raf.seek(pos);
	        raf.write(b, 0, b.length);
	        raf.close();
	        ok=true;
	    } catch (Exception e) {
            e.printStackTrace();	 
	    	ok=false;
	    }
	    return ok;
	}
}
