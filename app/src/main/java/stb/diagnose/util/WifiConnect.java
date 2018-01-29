package stb.diagnose.util;

/*
 *  WifiConnect.java
 *  Author: cscmaker
 */

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.List;

public class WifiConnect {
	private static  final String TAG="WifiConnect";
    WifiManager wifiManager;
    WifiConfiguration wifiConfig=null;
    boolean isFirst=true;
//定义几种加密方式，一种是WEP，一种是WPA，还有没有密码的情况
    public enum WifiCipherType
    {
  	  WIFICIPHER_WEP,WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID
    }
	
//构造函数
	public WifiConnect(WifiManager wifiManager)
	{
	  this.wifiManager = wifiManager;
	}
	
//打开wifi功能
     private boolean OpenWifi()
     {
    	 boolean bRet = true;
         if (!wifiManager.isWifiEnabled())
         {
       	  bRet = wifiManager.setWifiEnabled(true);  
         }
         return bRet;
     }
    
//提供一个外部接口，传入要连接的无线网
     public boolean Connect(String SSID, String Password, WifiCipherType Type)
     {
        if(!this.OpenWifi())
    	{
    		 return false;
    	}
        Log.i(TAG,"ssid:"+SSID+"----pwd:"+Password);
//开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
//状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
        while(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
        {
        	 try{
     //为了避免程序一直while循环，让它睡个100毫秒在检测……
           	  Thread.currentThread();
			  Thread.sleep(100);
           	}
           	catch(InterruptedException ie){
           }
        }
       /*if(!isFirst&&wifiConfig!=null){
    	   int netID = wifiManager.addNetwork(wifiConfig);
    	   boolean bRet = wifiManager.enableNetwork(netID, true);
    	   Log.i(DiagnoseHomeActivity.TAG,"reconnect netid:"+netID+"---enable Network:"+bRet);
    	   return bRet;
       }*/
        wifiConfig = this.CreateWifiInfo(SSID, Password, Type);
		//
    	if(wifiConfig == null)
		{
    	       return false;
		}
	   	
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        
        if(tempConfig != null)
        {
        	 Log.i(TAG,"exist ssid:"+SSID);
        	wifiManager.removeNetwork(tempConfig.networkId);
        }
        
      int netID = wifiManager.addNetwork(wifiConfig);
      Log.i(TAG,"net id:"+netID);
    	boolean bRet = wifiManager.enableNetwork(netID, true);  
    	isFirst=false;
    	Log.i(TAG,"enable network:"+bRet);
		return bRet;
     }
     
     public boolean reconnect(){
    	 return false;
     }
     
    //查看以前是否也配置过这个网络
     private WifiConfiguration IsExsits(String SSID)
     {
    	 List<WifiConfiguration> existingConfigs = wifiManager.getConfiguredNetworks();
    	 Log.i(TAG,"get existtingconfigs");
    	 if(existingConfigs==null){
    		 Log.i(TAG,"existtingconfigs is null");
    		 return null;
    	 }
    	    for (WifiConfiguration existingConfig : existingConfigs)
    	    {
    	      if (existingConfig.SSID.equals("\""+SSID+"\""))
    	      {
    	          return existingConfig;
    	      }
    	    }
    	 return null; 
     }
     
     private WifiConfiguration CreateWifiInfo(String SSID, String Password, WifiCipherType Type)
     {
     	WifiConfiguration config = new WifiConfiguration();
         config.allowedAuthAlgorithms.clear();
         config.allowedGroupCiphers.clear();
         config.allowedKeyManagement.clear();
         config.allowedPairwiseCiphers.clear();
         config.allowedProtocols.clear();
     	config.SSID = "\"" + SSID + "\"";  
     	if(Type == WifiCipherType.WIFICIPHER_NOPASS)
     	{
     		 config.wepKeys[0] = "";
     		 config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     		 config.wepTxKeyIndex = 0;
     	}
     	if(Type == WifiCipherType.WIFICIPHER_WEP)
     	{
     		config.preSharedKey = "\""+Password+"\""; 
     		config.hiddenSSID = true;  
     	    config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
     	    config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
     	    config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
     	    config.wepTxKeyIndex = 0;
     	}
     	if(Type == WifiCipherType.WIFICIPHER_WPA)
     	{
     		 Log.i(TAG,"type:"+ WifiCipherType.WIFICIPHER_WPA);
     	config.preSharedKey = "\""+Password+"\"";
     	config.hiddenSSID = true;  
     	config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
     	config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
     	config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
     	config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
     	//config.allowedProtocols.set(WifiConfiguration.Protocol.WPA); 
     	config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
     	config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
     	config.status = WifiConfiguration.Status.ENABLED;
     	}
     	else
     	{
     		return null;
     	}
     	return config;
     }
     
}
