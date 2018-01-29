package stb.diagnose.util;

import android.content.Context;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

public class DefaultConfigFile{
//	public InputStream inputStream = null;
	private static int ereaIndex=0;
	private static int currentEreaIndex=0;
	private static String erea_name;
	private static String aaa_url;
	private static String aaa_backupurl;
	private static String managedomain_url;
	private static String aaa_user;
	private static String aaa_password;
	private static String access_mode;
	private static String access_user;
	private static String access_password;
	
	public DefaultConfigFile(){
		
	}
	
	public static void parser(String xmlPath, Context context){
		Log.i("parser df","begin to parser default config");
		XmlPullParser xmlParser = Xml.newPullParser();
		try{
			//InputStream inputStream = context.getResources().getAssets().open(xmlPath);
			InputStream inputStream = new FileInputStream(xmlPath);
			xmlParser.setInput(inputStream,"utf-8");
			int evtType = xmlParser.getEventType();
			while (evtType != XmlPullParser.END_DOCUMENT){
				switch(evtType){
				case XmlPullParser.START_TAG:
					String tag = xmlParser.getName();
					if (tag.equalsIgnoreCase("ereaIndex")){
						ereaIndex = Integer.parseInt(xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("erea")){
						currentEreaIndex = Integer.parseInt(xmlParser.getAttributeValue(null, "index"));
					}
					else if(tag.equalsIgnoreCase("erea_name") && (currentEreaIndex == ereaIndex)){
				//		erea_name = xmlParser.nextText();
						setProp("persist.sys.area_code",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("aaa_url") && (currentEreaIndex == ereaIndex)){
				//		aaa_url = xmlParser.nextText();
						setProp("persist.sys.aaa_url",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("aaa_backupurl") && (currentEreaIndex == ereaIndex)){
					//	aaa_backupurl = xmlParser.nextText();
						setProp("persist.sys.aaa_url_backup",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("managedomain_url") && (currentEreaIndex == ereaIndex)){
					//	managedomain_url = xmlParser.nextText();
						setProp("persist.sys.nm_url",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("aaa_user") && (currentEreaIndex == ereaIndex)){
					//	aaa_user = xmlParser.nextText();
						setProp("persist.sys.aaa_account",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("aaa_password") && (currentEreaIndex == ereaIndex)){
					//	aaa_password = xmlParser.nextText();
						setProp("persist.sys.aaa_password",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("access_mode") && (currentEreaIndex == ereaIndex)){
						setProp("persist.sys.nettype",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("access_user") && (currentEreaIndex == ereaIndex)){
					//	access_user = xmlParser.nextText();
						setProp("persist.sys.ctc_dhcp_account",xmlParser.nextText());
					}
					else if(tag.equalsIgnoreCase("access_password") && (currentEreaIndex == ereaIndex)){
					//	access_password = xmlParser.nextText();
						setProp("persist.sys.ctc_dhcp_passwd",xmlParser.nextText());
					}						
					break;
				default:
					break;
				}
				evtType = xmlParser.next();
			}
				
		}catch(IOException e){
			e.printStackTrace();
		}catch(XmlPullParserException e){
			e.printStackTrace();
		}
	//	Log.i("parser df","aaa url="+aaa_url);
	}

	private static String setProp(String key, String value)
    {
	     String prop=null;
	     try {
	      Class cls1= Class.forName("android.os.SystemProperties");
	      Class[] argsClass = { String.class, String.class };
	      Method method1 = cls1.getMethod("set", argsClass);
	      Object[] args = { key,value };
	      prop=(String)(method1.invoke(cls1, args));
	     } catch (Exception e) {
	      e.printStackTrace();
	     }
	     if(prop==null)
	      prop="";
	     return prop;
    }
}
