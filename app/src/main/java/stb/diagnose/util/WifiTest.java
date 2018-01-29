package stb.diagnose.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import static stb.diagnose.DiagnoseHomeActivity.WIFI;

/**
 *
 * Created by liu on 2017-07-24.
 */

public class WifiTest extends Thread {
    private Context mContext;
    private static final String TAG = "WifiTest";
    private boolean connected;
    private Handler handler;
    private String name;
    private String pwd;

    public WifiTest(Context context, Handler handler,String name,String pwd) {
        this.name=name;
        this.pwd=pwd;
        this.mContext = context;
        this.handler = handler;
    }

    @Override
    public void run() {

//        name = "妹子想蹭网？加微信吧";
//        pwd = "11111111";
        WifiManager wm = (WifiManager) mContext.getSystemService(mContext.WIFI_SERVICE);
        WifiConnect wc = new WifiConnect(wm);
        boolean ret = wc.Connect(name, pwd,
                WifiConnect.WifiCipherType.WIFICIPHER_WPA);
        Log.d(TAG, "run: ");
        int i = 1;
        int disconnTime = 0;
        connected = false;

        while (true) {;
            if (!ret) {
                if (disconnTime == 5) {
                    break;
                }
                Log.i(TAG, "enable wifi fail,try again");
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                wm.setWifiEnabled(false);
                wm.setWifiEnabled(true);
                ret = wc.Connect(
                        name,
                        pwd,
                        WifiConnect.WifiCipherType.WIFICIPHER_WPA);
                i++;
                disconnTime++;
                continue;
            }
            if (i == 35) {
                break;
            }
            WifiInfo mWifiInfo = wm.getConnectionInfo();
            Log.i(TAG, "wifi info:" + mWifiInfo.toString());
            if (mWifiInfo.getSSID().toString().equals("\"" + name + "\"")
                    && mWifiInfo.getIpAddress() != 0) {

                connected = true;
                Log.i(TAG, "connect suc pos:" + i);
                break;
            }
            if (i % 12 == 0) {
                try {
                    Log.i(TAG, "reconnect again");
                    ret = wc.Connect(
                            name,
                            pwd,
                            WifiConnect.WifiCipherType.WIFICIPHER_WPA);
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i++;
        }

        if (connected) {
            Log.i(TAG, "wifi connect successfull");
            LogInFile.write("/sdcard/wifi.txt","wifi connect successfull");
            Message msg = new Message();
            msg.what = WIFI;
            msg.arg1 = 1;
            handler.sendMessage(msg);
//            wm.setWifiEnabled(false);
        } else {
            Log.i(TAG, "wifi connect fail");
            LogInFile.write("/sdcard/wifi.txt","wifi connect fail");
            Message msg = new Message();
            msg.what = WIFI;
            msg.arg1 = 2;
            handler.sendMessage(msg);
        }

    }


    public boolean isConnect() {
        return connected;
    }
}

