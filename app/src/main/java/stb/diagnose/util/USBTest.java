package stb.diagnose.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static stb.diagnose.DiagnoseHomeActivity.USB;

/**
 * Created by liu on 2017-08-01.
 */

public class USBTest extends Thread {

    private static final String TAG = "USBTest";
    private Handler handler;

    public USBTest(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void run() {
        Process process1 = null;
        Process process2 = null;
        String s = "";
        String name = "";
        try {
            process1 = Runtime.getRuntime().exec(
                    "cat /proc/bus/input/devices");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    process1.getInputStream()));
                /*
                 * BufferedReader in = new BufferedReader( new
				 * InputStreamReader(new
				 * FileInputStream("/mnt/sdcard/usb_devices.txt")));
				 */
            String line = null;
            while ((line = in.readLine()) != null) {
                s += line + "\n";
                if (line.contains("Name=")) {
                    name = line;
                } else if (line.contains("Phys=usb-tegra-")) {
                    Log.i(TAG, "name:" + name + "---phys:" + line);
                    if (line.contains("Phys=usb-tegra-ehci.1-1.4/input0")) {
                        Log.i(TAG, line);
                        sendMsg(name, 1);
                    } else if (line.contains("Phys=usb-tegra-xhci-3.4.1/input0")) {
                        Log.i(TAG, line);
                        sendMsg(name, 2);
                    } else if (line.contains("Phys=usb-tegra-xhci-3.4.2/input0")) {
                        Log.i(TAG, line);
                        sendMsg(name, 3);
                    } else if (line.contains("Phys=usb-tegra-xhci-3.3/input0")) {
                        Log.i(TAG, line);
                        sendMsg(name, 4);
                    } else if (line.contains("Phys=usb-tegra-xhci-3.2/input0")) {
                        sendMsg(name, 5);
                    } else if (line.contains("Phys=usb-tegra-xhci-3.1/input0")) {
                        sendMsg(name, 6);
                    }
//                     else if (line.contains("Phys=usb-tegra-xhci-3.4.3/input0")) {
//                        sendMsg(name, 7);
//                    } else if (line.contains("Phys=usb-tegra-xhci-3.4.2/input0")) {
//                        sendMsg(name, 8);
//                    } else if (line.contains("Phys=usb-tegra-xhci-3.4.1/input0")) {
//                        sendMsg(name, 9);
//                    } else if (line.contains("Phys=usb-tegra-ehci.1-1.4/input0")) {
//                        sendMsg(name, 10);
//                    }


                }
            }
//            Log.i(TAG, "cat /proc/bus/input/devices:" + s);
//            s = "";
//            process2 = Runtime.getRuntime().exec("" +
//                    "" +
//                    "");
//            BufferedReader in1 = new BufferedReader(new InputStreamReader(
//                    process2.getInputStream()));
//            while ((line = in1.readLine()) != null) {
//                s += line + "\n";
//                if (line.contains("/storage/usbdrive1") || line.contains("/storage/usbdrive5")) {
//                    sendMsg("usb", 1);
//                } else if (line.contains("/storage/usbdrive2")) {
//                    sendMsg("usb", 2);
//                } else if (line.contains("/storage/usbdrive3")) {
//                    sendMsg("usb", 3);
//                } else if (line.contains("/storage/usbdrive4")) {
//                    sendMsg("usb", 4);
//                }
//            }
            Log.i(TAG, "mount:\n" + s);
            Message msg = new Message();
            msg.what = USB;
            msg.arg1 = 11;
            handler.sendMessageDelayed(msg, 2000);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMsg(String name, int pos) {
        Message msg = new Message();
        msg.what = USB;
        if (name.contains("Mouse")) {
            msg.arg1 = pos;
            msg.arg2 = 2;

        } else if (name.contains("Keyboard")) {
            msg.arg1 = pos;
            msg.arg2 = 3;
        } else if (name.contains("Scanner")) {
            msg.arg1 = pos;
            msg.arg2 = 4;
        } else if (name.equals("usb")) {
            msg.arg1 = pos;
            msg.arg2 = 1;
        } else {
            msg.arg1 = pos;
            msg.arg2 = -1;
        }
        handler.sendMessageDelayed(msg, 500);
    }
}