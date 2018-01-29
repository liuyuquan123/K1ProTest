package stb.diagnose.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import static stb.diagnose.DiagnoseHomeActivity.ETHERNET;

/**
 * Created by liu on 2017-08-01.
 */
public class EthernetTest extends Thread {
    private String ip;
    private int eth;
    private  static  final String TAG="EthernetTest";
    private Handler handler;

    public EthernetTest(String ip, int eth, Handler handler) {
        this.ip = ip;
        this.eth = eth;
        this.handler=handler;
    }

    @Override
    public void run() {

        String s = "";
        Process process2 = null;
        try {
            String line = null;
            process2 = Runtime.getRuntime().exec("ping -c 1 " + ip);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    process2.getInputStream()));

            while ((line = in1.readLine()) != null) {
                s += line + "\n";
            }
            Log.d(TAG, "run: "+s);
            if (s.contains("1 received")) {
                Message message = new Message();
                message.what = ETHERNET;
                if (eth == 1) {
                    message.arg1 = 1;
                    message.arg2 = 1;
                }
                if (eth == 2) {
                    message.arg1 = 2;
                    message.arg2 = 1;
                }
                if (eth == 3) {
                    message.arg1 = 3;
                    message.arg2 = 1;
                }
                if (eth == 4) {
                    message.arg1 = 4;
                    message.arg2 = 1;
                }
                handler.sendMessage(message);
            } else if (s.contains("0 received")||s.equals("")) {
                Message message = new Message();
                message.what=ETHERNET;
                if (eth == 1) {
                    message.arg1 = 1;
                    message.arg2 = 0;
                }
                if (eth == 2) {
                    message.arg1 = 2;
                    message.arg2 = 0;
                }
                if (eth == 3) {
                    message.arg1 = 3;
                    message.arg2 = 0;
                }
                if (eth == 4) {
                    message.arg1 = 4;
                    message.arg2 = 0;
                }
                handler.sendMessage(message);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}