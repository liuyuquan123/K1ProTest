package stb.diagnose;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;
import stb.diagnose.util.EthernetTest;
import stb.diagnose.util.RecordAudioUtils;
import stb.diagnose.util.USBTest;
import stb.diagnose.util.UiThreadUtils;
import stb.diagnose.util.WifiTest;


public class DiagnoseHomeActivity extends Activity {
    public static final int USB = 1;
    public static final int NEXT_ITEM = 2;
    public static final int CAN = 3;
    public static final int WIFI = 4;
    public static final int ETHERNET = 5;
    public static final int FINSH = 6;


    public static final String TAG = "DiagnoseHomeActivity";
    public static final int ZWJMSG = 7;
    private static final int TF = 8;
    private static final int BT = 9;
    private static final int SERIAL = 10;
    boolean isRecording = false;
    private boolean isPlaying = false;
    private Uri uri;
    private Context mContext;
    private LayoutItem layoutItem;
    private LinearLayout contain;
    private int pos = -3;
    private MediaPlayer mp;
    private String[] results = new String[9];
    private String[] serialResult = new String[1];
    private String[] preDevices = new String[]{"风扇测试", "喇叭测试", "mic测试", "USB测试",
            "网口测试", "BT测试", "2.4g Wifi测试","5g Wifi测试", "串口测试"};
    private String[] usbResults = new String[6];
    private List<String> ethResults = new ArrayList<String>();
    //
    private WifiManager wm;
    private Timer myTimer = new Timer();
    private boolean isTimeout;
    private String submit_content = "";
    private int final_pos = -1;
    private boolean all_pass = true;
    private String fileName;
    private boolean zwjStandBy;
    private String zwjMsg = "";
    private String[] ethResult;

    private ExecutorService mExecutorService;
    private MediaRecorder mMediaRecorder;
    private File mAudioFile;
    private long mBeginRecordInMillis, mEndRecordInMillis;
    // 播放状态
    private volatile boolean mIsplaying;
    private MediaPlayer mMediaPlayer;
    private String[] devices = new String[5];
    private String[] items;
    private String ssid = "quan";
    private String Ssid = "5Gquan";
    private String passward = "12345678";
    private String ip = "192.168.1.1";
    private String btName = "iPhone";

    boolean BTDiscoveryFinish = false;
    boolean findBT = false;

    protected SerialPort mSerialPort;
    protected InputStream mInputStream;
    protected OutputStream mOutputStream;
    private StringBuilder sb;
    private Thread sendThread;
    private String mPort;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {


        @Override
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {

                case 10:
                    layoutItem.text.setTextColor(Color.WHITE);
                    layoutItem.text.setText("");
                    layoutItem.text.setText(sb);
                    break;

                case NEXT_ITEM:
                    pos++;
                    if (pos > 10) {
                        return;
                    }
                    getLayout();
                    break;
                case BT:
                    try {
                        unregisterReceiver(receiver);
                    } catch (Exception e) {

                    }
                    if (msg.arg1 == 1) {
                        layoutItem.bt_submit.setText("发现指定设备！");
                        layoutItem.bt_submit.setTextColor(Color.GREEN);
                        setItemResult(1);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    } else {
                        layoutItem.bt_submit.setText("未能找到指定设备");
                        layoutItem.bt_submit.setTextColor(Color.RED);
                        setItemResult(0);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    }

                    break;


                case TF:
                    if (msg.arg1 == 1) {
                        layoutItem.tf_submit.setText("TF卡读写测试成功");
                        setItemResult(1);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    } else {
                        layoutItem.tf_submit.setText("TF卡读写失败");
                        setItemResult(0);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                        // handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    }
                    break;

                case FINSH:
                    if (msg.arg1 == 1) {
                        Log.d(TAG, "handleMessage: 上传成功");
                        layoutItem.tv_submit_result.setText("上传成功");
                        layoutItem.tv_submit_result.setTextColor(Color.GREEN);
                    } else if (msg.arg1 == 2) {
                        Log.d(TAG, "handleMessage:  上传失败   pos=" + pos);
                        layoutItem.tv_submit_result.setText("上传失败");
                        layoutItem.tv_submit_result.setTextColor(Color.RED);
                    }

                    break;


                case WIFI:
                    if (msg.arg1 == 1) {
                        Log.d(TAG, "handleMessage: wifi自动连接成功");
                        if (pos == 7) {
                            layoutItem.tv_wifi_submit1.setText("wifi自动连接成功");
                            layoutItem.tv_wifi_submit1.setBackgroundColor(Color.GREEN);
                        } else {
                            layoutItem.tv_wifi_submit.setText("wifi自动连接成功");
                            layoutItem.tv_wifi_submit.setBackgroundColor(Color.GREEN);
                        }
                        setItemResult(1);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    }
                    if (msg.arg1 == 2) {
                        Log.d(TAG, "handleMessage:wifi自动连接失败 ");
                        if (pos == 7) {
                            layoutItem.tv_wifi_submit.setText("wifi自动连接失败");
                            layoutItem.tv_wifi_submit.setBackgroundColor(Color.RED);
                        } else {
                            layoutItem.tv_wifi_submit.setText("wifi自动连接失败");
                            layoutItem.tv_wifi_submit.setBackgroundColor(Color.RED);
                        }

                        setItemResult(0);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);

                    }

                    break;

                case ETHERNET:
                    if (msg.arg1 == 1) {
                        if (msg.arg2 == 1) {
                            ethResults.add("eth1  ----pass");
                            layoutItem.tv_eth1.setText("左网口测试通过");
                            Log.d(TAG, "handleMessage: eth1  ----pass");
                            layoutItem.tv_eth1.setTextColor(Color.GREEN);
                        } else {
                            ethResults.add("eth1  ----fail");
                            layoutItem.tv_eth1.setText("左网口测试不通过");
                            layoutItem.tv_eth1.setTextColor(Color.RED);
                        }
                    }
                    if (msg.arg1 == 2) {
                        if (msg.arg2 == 1) {
                            ethResults.add("eth2  ----pass");
                            layoutItem.tv_eth2.setText("右网口测试通过");
                            layoutItem.tv_eth2.setTextColor(Color.GREEN);
                        } else {
                            ethResults.add("eth2  ----fail");
                            layoutItem.tv_eth2.setText("右网口测试不通过");
                            layoutItem.tv_eth2.setTextColor(Color.RED);
                        }
                    }
                    if (msg.arg1 == 3) {
                        if (msg.arg2 == 1) {
                            ethResults.add("eth3  ----pass");
                            layoutItem.tv_eth3.setText("右上网口测试通过");
                            layoutItem.tv_eth3.setTextColor(Color.GREEN);
                        } else {
                            ethResults.add("eth3  ----fail");
                            layoutItem.tv_eth3.setText("右上网口测试不通过");
                            layoutItem.tv_eth3.setTextColor(Color.RED);
                        }

                    }
                    if (msg.arg1 == 4) {
                        if (msg.arg2 == 1) {
                            ethResults.add("eth4  ----pass");
                            layoutItem.tv_eth4.setText("右下网口测试通过");
                            layoutItem.tv_eth4.setTextColor(Color.GREEN);
                        } else {
                            ethResults.add("eth4  ----fail");
                            layoutItem.tv_eth4.setText("右下网口测试不通过");
                            layoutItem.tv_eth4.setTextColor(Color.RED);
                        }

                    }

                    boolean EthIsPassed = true;
                    if (ethResults.size() == 2) {
                        String s = ethResults.toString();
                        ethResult = new String[ethResults.size()];
                        ethResult = ethResults.toArray(ethResult);
                        if (s.contains("fail")) {
                            EthIsPassed = false;
                        }

                        if (EthIsPassed) {
                            setItemResult(1);
                            handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                        } else {
                            setItemResult(0);
                            handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                        }

                    }

                    break;


                case USB:
                    if (msg.arg1 == 1) {
                        usbResults[0] = "usb1----pass";
                        layoutItem.tv_usb1_submit.setTextColor(Color.GREEN);
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb1_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb1_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb1_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb1_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb1_submit.setText("检测到设备");
                        }

                    } else if (msg.arg1 == 2) {
                        layoutItem.tv_usb2_submit.setTextColor(Color.GREEN);
                        usbResults[1] = "usb2----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb2_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb2_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb2_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb2_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb2_submit.setText("检测到设备");
                        }

                    } else if (msg.arg1 == 3) {
                        usbResults[2] = "usb3----pass";
                        layoutItem.tv_usb3_submit.setTextColor(Color.GREEN);
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb3_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb3_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb3_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb3_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb3_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 4) {
                        usbResults[3] = "usb4  ----pass";
                        layoutItem.tv_usb4_submit.setTextColor(Color.GREEN);
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb4_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb4_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb4_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb4_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb4_submit.setText("检测到设备");
                        }

                    } else if (msg.arg1 == 5) {
                        layoutItem.tv_usb5_submit.setTextColor(Color.GREEN);
                        usbResults[4] = "usb5  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb5_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb5_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb5_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb5_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb5_submit.setText("检测到设备");
                        }

                    } else if (msg.arg1 == 6) {
                        layoutItem.tv_usb6_submit.setTextColor(Color.GREEN);
                        usbResults[5] = "usb6  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb6_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb6_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb6_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb6_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb6_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 7) {
                        layoutItem.tv_usb7_submit.setTextColor(Color.GREEN);
                        usbResults[6] = "usb7  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb7_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb7_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb7_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb7_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb7_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 8) {
                        layoutItem.tv_usb8_submit.setTextColor(Color.GREEN);
                        usbResults[7] = "usb8  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb8_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb8_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb8_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb8_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb8_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 9) {
                        layoutItem.tv_usb9_submit.setTextColor(Color.GREEN);
                        usbResults[8] = "usb9  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb9_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb9_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb9_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb9_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb9_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 10) {
                        layoutItem.tv_usb10_submit.setTextColor(Color.GREEN);
                        usbResults[9] = "usb9  ----pass";
                        if (msg.arg2 == 1) {
                            layoutItem.tv_usb10_submit.setText("检测到U盘");
                        } else if (msg.arg2 == 2) {
                            layoutItem.tv_usb10_submit.setText("检测到鼠标");
                        } else if (msg.arg2 == 3) {
                            layoutItem.tv_usb10_submit.setText("检测到键盘");
                        } else if (msg.arg2 == 4) {
                            layoutItem.tv_usb10_submit.setText("检测到扫描枪");
                        } else if (msg.arg2 == -1) {
                            layoutItem.tv_usb1_submit.setText("检测到设备");
                        }
                    } else if (msg.arg1 == 11) {
//                        usbTestOK();
                    }
                    break;


                default:
                    break;

            }
        }
    };
    private String serial;
    private String sn_file;


    private void usbTestOK() {
        boolean isPassed = true;
        for (int i = 0; i < usbResults.length; i++) {
            if (usbResults[i] == null || usbResults[i].equals("")) {
                if (i == 0) {
                    usbResults[0] = "usb1  ----fail";
                }
                if (i == 1) {
                    usbResults[1] = "usb2   ----fail";
                }
                if (i == 2) {
                    usbResults[2] = "usb3   ----fail";
                }
                if (i == 3) {
                    usbResults[3] = "usb4   ----fail";
                }
                if (i == 4) {
                    usbResults[4] = "usb5   ----fail";
                }
                if (i == 5) {
                    usbResults[5] = "usb6   ----fail";
                }
                if (i == 6) {
                    usbResults[6] = "usb7   ----fail";
                }
                if (i == 7) {
                    usbResults[7] = "usb8   ----fail";
                }
                if (i == 8) {
                    usbResults[8] = "usb9   ----fail";
                }
                if (i == 9) {
                    usbResults[9] = "usb10  ----fail";
                }
                isPassed = false;
            }
        }
        if (isPassed) {
            setItemResult(1);
            handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
        } else {
            setItemResult(0);
            handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        initView();
        // 录音JNI函数不具备线程安全性，所以要用单线程
        mExecutorService = Executors.newSingleThreadExecutor();
        layoutItem = new LayoutItem();
        layoutItem.initView();
        getLayout();
//        String usbPath = getExtSDCard();
        String usbPath = "/storage/usbdrive1";
        Log.d(TAG, "onCreate: " + usbPath);
        File file = new File(usbPath, "data.txt");
        if (file.exists()) {
            readFileByLines(file, 1);
            ssid = devices[0].split("@")[1];
            passward = devices[1].split("@")[1];
            ip = devices[2].split("@")[1];
            btName = devices[3].split("@")[1];
            Ssid = devices[4].split("@")[1];
            Log.d(TAG, "onCreate: " + ssid + "   " + passward + "  " + ip + "  " + btName);
        }

    }

    public static void list(File file) {
        if (file.isDirectory())//判断file是否是目录
        {
            File[] lists = file.listFiles();
            if (lists != null) {
                for (int i = 0; i < lists.length; i++) {
                    list(lists[i]);//是目录就递归进入目录内再进行判断
                }
            }
        }
        Log.d(TAG, "list: " + file);
        System.out.println(file);//file不是目录，就输出它的路径名，这是递归的出口
    }

    private void readFileByLines(File file, int type) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 0;
            if (type == 0) {
                while ((tempString = reader.readLine()) != null) {
                    if (line != 0 && line < 13) {
                        items[line - 1] = tempString;
                        Log.d("itens", tempString);
                    }
                    line++;
                }
            } else {
                while ((tempString = reader.readLine()) != null) {
                    if (line < 5) {
                        devices[line] = tempString;
                        Log.d("devices", devices[line]);
                    } else {
                        break;
                    }
                    line++;
                }
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }


    /**
     * 初始化控件
     */
    @SuppressLint("WifiManagerLeak")
    private void initView() {
        layoutItem = new LayoutItem();
        contain = (LinearLayout) findViewById(R.id.contain);
        mContext = this;
        wm = (WifiManager) getSystemService(WIFI_SERVICE);
    }

    private void getLayout() {

        contain.removeAllViews();
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(1920, 1080);
        Log.d(TAG, "getLayout: " + pos + layoutItem.getView());
        contain.addView(layoutItem.getView(), layoutParams);
        layoutItem.autoDealWith();
    }


    public class LayoutItem {
        LinearLayout sn_layout_01;
        EditText inputsn01;
        TextView getsn01;
        TextView sn_submit;
        Button sn_submit_button;

        LinearLayout mac_layout_02;
        TextView mac_address_02;
        TextView tv_mac_hint;
        EditText et_mac;
        Button bt_mac_compare;
        private String mac_address;


        LinearLayout wifi_mac_layout;
        TextView tv_wifi_mac_hint;
        TextView tv_wifi_mac_add;
        EditText et_wifimac;
        Button bt_wifi_mac_compare;
        private String wifi_mac_address;

        LinearLayout fengshan_layout;
        Button bt_fengshan_pass;
        Button bt_fengshan_fail;

        LinearLayout wifi_layout;
        TextView tv_wifi_submit;
        TextView tv_wifi_adv;


        LinearLayout wifi_layout1;
        TextView tv_wifi_submit1;
        TextView tv_wifi_adv1;


        LinearLayout mic_layout;
        Button bt_pressToSay;
        Button bt_playAudio;
        Button bt_mic_pass;
        Button bt_mic_fail;
        TextView tv_log;

        LinearLayout laba_layout;
        Button bt_pass_submit_04;
        Button bt_fail_submit_04;
        Button bt_laba_start;


        LinearLayout usb_layout;
        TextView tv_usbtest;
        TextView tv_usb1_submit;
        TextView tv_usb2_submit;
        TextView tv_usb3_submit;
        TextView tv_usb4_submit;
        TextView tv_usb5_submit;
        TextView tv_usb6_submit;
        TextView tv_usb7_submit;
        TextView tv_usb8_submit;
        TextView tv_usb9_submit;
        TextView tv_usb10_submit;
        Button bt_usb_isok, bt_usb1;
        LinearLayout tf_layout;
        TextView tf_submit;

        LinearLayout ethernet_layout;
        TextView tv_eth1;
        TextView tv_eth2;
        TextView tv_eth3;
        TextView tv_eth4;
        Button bt_eth1, bt_eth2, bt_eth3, bt_eth4;
        TextView tv_eth_test;

        LinearLayout serial_layout;
        Spinner spinner;
        TextView text;
        String prot = "ttysWK0";
        EditText editText;
        EditText et_text;
        int baudrate = 9600;

        Button btn_serial_pass01, btn_serial_pass02, btn_serial_pass03, btn_serial_pass04;
        Button btn_serial_fail01, btn_serial_fail02, btn_serial_fail03, btn_serial_fail04;
        Button btn_serial_end;

        LinearLayout submit_layout;
        LinearLayout ll_content;
        Button bt_exit;
        TextView tv_submit_result;
        TextView tv_test_result;


        LinearLayout bt_layout;
        TextView bt_submit;


        private View getView() {
            if (pos > 9 || pos < -3) {
                return null;
            }

            switch (pos) {
                case -3:
                    return sn_layout_01;
                case -2:
                    return wifi_mac_layout;
                case -1:
                    return mac_layout_02;
                case 0:
                    return fengshan_layout;
                case 1:
                    return laba_layout;
                case 2:
                    return mic_layout;
                case 3:
                    return usb_layout;
                case 4:
                    return ethernet_layout;
                case 5:
                    return bt_layout;
//                case 6:
//                    return tf_layout;
                case 6:
                    return wifi_layout;

                case 7:
                    return wifi_layout1;
                case 8:
                    return serial_layout;
                case 9:
                    return submit_layout;
                default:
                    break;

            }

            return null;
        }


        private void autoDealWith() {
            switch (pos) {

                case 3:
                    tv_usb1_submit.setText("正在检测设备...");
                    break;
                case 4:
                    tv_eth_test.setText("正在获取进行网口测试,请依次点击测试按钮！！");
                    break;

                case 5:
                    bt_submit.setText("正在进行蓝牙测试......");
                    bTTest();
                    break;

                case 6:
                    tv_wifi_adv.setText("正在进行 2.4G wifi测试......");
                    wifiTest(ssid);
                    break;
                case 7:
                    tv_wifi_adv1.setText("正在进行 5G wifi测试......");
                    wifiTest(Ssid);
                    break;

                case 9:
                    submit_content = "";
                    for (int i = 0; i < results.length; i++) {
                        submit_content += results[i] + "\n";
                        TextView tmp = new TextView(DiagnoseHomeActivity.this);
                        tmp.setTextSize(15);
                        tmp.setText(results[i]);
//                        LogInFile.write("/sdcard/result.txt", results[i] + " pos  " + final_pos);
                        if (results[i].contains("fail")) {
                            tmp.setTextColor(Color.RED);
                            tv_test_result.setText("测试结果:FAIL");
                            tv_test_result.setTextColor(Color.RED);
                            all_pass = false;
                        }
                        ll_content.addView(tmp);
                    }
                    if (tv_test_result.getText().toString().contains("FAIL")) {
                        tv_test_result.setText("测试结果:FAIL");
                        tv_test_result.setTextColor(Color.RED);
                    } else {
                        tv_test_result.setText("测试结果:PASS");
                        tv_test_result.setTextColor(Color.GREEN);
                    }
                    saveResult();
                    break;
                default:
                    break;


            }
        }


        private void initView() {
            initSnView();
            initWifiMacC();
            initMacConp();
            initFengShanItem();
            initMicItem();
            initSpeakerItem();
            initUsbItem();
            initWifiItem();
            initWifiItem1();
            initEthernetItem();
            initTfItem();
            initBTItem();
            initSubmitItem();
            initSerialItem();


        }

        private void initWifiItem1() {
            wifi_layout1 = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_wifi1, null);
            tv_wifi_submit1 = (TextView) wifi_layout1.findViewById(R.id.wifi_submit1);
            tv_wifi_adv1 = (TextView) wifi_layout1.findViewById(R.id.wifi_adv1);


        }

        private void initSnView() {

            // item 1
            sn_layout_01 = (LinearLayout) LayoutInflater.from(
                    DiagnoseHomeActivity.this).inflate(R.layout.layout_sn_01,
                    null);
            inputsn01 = (EditText) sn_layout_01.findViewById(R.id.inputsn01);
            getsn01 = (TextView) sn_layout_01.findViewById(R.id.getsn01);
            getsn01.setText(getDeviceSerial());
            sn_submit = (TextView) sn_layout_01.findViewById(R.id.sn_submit);
            sn_submit.setText("请用扫描枪扫描SN!");
            sn_submit_button = (Button) sn_layout_01.findViewById(R.id.sn_compare);

            inputsn01.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // TODO Auto-generated method stub
                    if (v.getId() == R.id.inputsn01
                            && keyCode == KeyEvent.KEYCODE_ENTER) {
                        Log.i(TAG, "sn input keycode enter");
                        inputsn01.clearFocus();
                        inputsn01.setEnabled(false);
                        return true;
                    }
                    return false;
                }
            });

            sn_submit_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    inputsn01.clearFocus();
                    inputsn01.setEnabled(false);
                    compareSN();
                }
            });
        }

        private void compareSN() {
            sn_file = inputsn01.getText().toString();
            if (getsn01.getText().toString()
                    .equals(inputsn01.getText().toString())) {
                sn_submit.setText("sn一致");
                sn_submit_button.setEnabled(false);
                handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
            } else {
                Toast.makeText(getBaseContext(), "sn不一致,请重新输入", Toast.LENGTH_LONG).show();
                inputsn01.setEnabled(true);
                inputsn01.setText("");

            }
        }

        private void initWifiMacC() {
            wifi_mac_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_wifi_mac, null);
            tv_wifi_mac_hint = (TextView) wifi_mac_layout.findViewById(R.id.tv_wifimac_hint);
            tv_wifi_mac_add = (TextView) wifi_mac_layout.findViewById(R.id.tv_wifimac_address);
            et_wifimac = (EditText) wifi_mac_layout.findViewById(R.id.et_wifimac);
            bt_wifi_mac_compare = (Button) wifi_mac_layout.findViewById(R.id.bt_wifi_mac_compare);
            wifi_mac_address = gsData.getWifiMacAddress().replace(":", "");
            tv_wifi_mac_add.setText(wifi_mac_address);
            et_wifimac.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (v.getId() == R.id.et_wifimac
                            && keyCode == KeyEvent.KEYCODE_ENTER) {
                        Log.i(TAG, "sn input keycode enter");
                        et_wifimac.clearFocus();
                        et_wifimac.setEnabled(false);
                        return true;
                    }
                    return false;
                }
            });

            bt_wifi_mac_compare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String current_mac = et_wifimac.getText().toString();
                    et_wifimac.clearFocus();
                    et_wifimac.setEnabled(false);
                    if (wifi_mac_address.equalsIgnoreCase(current_mac)) {
                        tv_wifi_mac_hint.setText("Wifi Mac地址一致，测试通过");
                        tv_wifi_mac_hint.setTextColor(Color.GREEN);
                        bt_wifi_mac_compare.setEnabled(false);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 500);
                    } else {
                        tv_wifi_mac_hint.setText("Wifi Mac地址不一致，请重新输入");
                        et_wifimac.setEnabled(true);
                        et_wifimac.setText("");
                    }


                }
            });

        }

        private void initMacConp() {
            // item 2
            mac_layout_02 = (LinearLayout) LayoutInflater.from(
                    DiagnoseHomeActivity.this).inflate(
                    R.layout.layout_mac, null);

            bt_mac_compare = (Button) mac_layout_02.findViewById(R.id.bt_mac_compare);
            mac_address_02 = (TextView) mac_layout_02
                    .findViewById(R.id.mac_address);
            tv_mac_hint = (TextView) mac_layout_02.findViewById(R.id.tv_mac);
            mac_address = gsData.getMacAddress().replace(":", "");
            mac_address_02.setText(mac_address);
            et_mac = (EditText) mac_layout_02.findViewById(R.id.et_mac);

            et_mac.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (v.getId() == R.id.et_mac
                            && keyCode == KeyEvent.KEYCODE_ENTER) {
                        Log.i(TAG, "sn input keycode enter");
                        et_mac.clearFocus();
                        et_mac.setEnabled(false);
                        return true;
                    }
                    return false;
                }
            });


            bt_mac_compare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String current_mac = et_mac.getText().toString();
                    et_mac.clearFocus();
                    et_mac.setEnabled(false);
                    if (mac_address.equalsIgnoreCase(current_mac)) {
                        tv_mac_hint.setText("MAC地址一致，MAC测试通过");
                        tv_mac_hint.setTextColor(Color.GREEN);
                        bt_mac_compare.setEnabled(false);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 500);
                    } else {
                        tv_mac_hint.setText("MAC地址不一致，请重新输入");
                        et_mac.setEnabled(true);
                        et_mac.setText("");
                    }


                }
            });

        }

        private void initSerialItem() {
            serial_layout = (LinearLayout) getLayoutInflater().inflate(R.layout.item_serial, null);
            sb = new StringBuilder();
            text = (TextView) serial_layout.findViewById(R.id.text_receive);
            editText = (EditText) serial_layout.findViewById(R.id.et_num);
            et_text = (EditText) serial_layout.findViewById(R.id.et_text);
            spinner = (Spinner) serial_layout.findViewById(R.id.sp_dev);
            btn_serial_pass01 = (Button) serial_layout.findViewById(R.id.btn_serial_pass01);
            btn_serial_pass02 = (Button) serial_layout.findViewById(R.id.btn_serial_pass02);
            btn_serial_pass03 = (Button) serial_layout.findViewById(R.id.btn_serial_pass03);
            btn_serial_pass04 = (Button) serial_layout.findViewById(R.id.btn_serial_pass04);
            btn_serial_pass01.setOnClickListener(new NextItemListener());
            btn_serial_pass02.setOnClickListener(new NextItemListener());
            btn_serial_pass03.setOnClickListener(new NextItemListener());
            btn_serial_pass04.setOnClickListener(new NextItemListener());

            btn_serial_end = (Button) serial_layout.findViewById(R.id.btn_serial_end);
            btn_serial_fail01 = (Button) serial_layout.findViewById(R.id.btn_serial_fail01);
            btn_serial_fail02 = (Button) serial_layout.findViewById(R.id.btn_serial_fail02);
            btn_serial_fail03 = (Button) serial_layout.findViewById(R.id.btn_serial_fail03);
            btn_serial_fail04 = (Button) serial_layout.findViewById(R.id.btn_serial_fail04);

            btn_serial_end.setOnClickListener(new NextItemListener());
            btn_serial_fail01.setOnClickListener(new NextItemListener());
            btn_serial_fail02.setOnClickListener(new NextItemListener());
            btn_serial_fail03.setOnClickListener(new NextItemListener());
            btn_serial_fail04.setOnClickListener(new NextItemListener());


            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mPort = parent.getSelectedItem().toString().split(":")[1];
                    Log.d(TAG, "onItemSelected: " + mPort);

                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            mPort = spinner.getSelectedItem().toString().split(":")[1];
            Log.d(TAG, "initSerialItem: " + mPort);

//
//        Button btn_set = (Button) findViewById(R.id.btn_set);
//        btn_set.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                EditText et_num = (EditText) findViewById(R.id.et_num);
//                baudrate = Integer.parseInt(TextUtils.isEmpty(et_num.getText()
//                        .toString().trim()) ? "9600" : et_num.getText()
//                        .toString().trim());
//            }
//        });

            Button btn_open = (Button) serial_layout.findViewById(R.id.btn_open);
            btn_open.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 打开
                    try {
                        if (editText.getText().toString().isEmpty()) {
                            baudrate = 9600;
                        } else {
                            baudrate = Integer.parseInt(editText.getText().toString());
                        }

                        mSerialPort = new SerialPort(new File("/dev/" + mPort), baudrate,
                                0);
                        mInputStream = mSerialPort.getInputStream();
                        mOutputStream = mSerialPort.getOutputStream();
                        receiveThread();
                    } catch (SecurityException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        Log.i("test", "打开失败");
                        e.printStackTrace();
                    }
                }
            });

            Button btn_send = (Button) serial_layout.findViewById(R.id.btn_send);
            btn_send.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 发送
                    sendThread = new Thread() {
                        int i = 0;

                        @Override
                        public void run() {
                            while (i < 5) {
                                try {
                                    i++;
                                    if (et_text.getText().toString().isEmpty()) {
                                        mOutputStream.write(("hello".getBytes()));
                                    } else {
                                        mOutputStream.write((et_text.getText().toString()).getBytes());
                                    }

                                    Log.i("test", "发送成功:1" + i);
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    Log.i("test", "发送失败");
                                    e.printStackTrace();
                                }
                            }
                        }
                    };
                    sendThread.start();
                }
            });

            Button btn_receive = (Button) serial_layout.findViewById(R.id.btn_receive);
            btn_receive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeSerialPort();
                }
            });


        }

        private void receiveThread() {
            // 接收
            Thread receiveThread = new Thread() {
                @Override
                public void run() {
                    while (true) {
                        int size;
                        try {
                            byte[] buffer = new byte[1024];
                            if (mInputStream == null)
                                return;
                            size = mInputStream.read(buffer);
                            if (size > 0) {
                                String recinfo = new String(buffer, 0,
                                        size);
                                Log.i("test", "接收到串口信息:" + recinfo);
                                sb.append(recinfo).append(",");
                                handler.sendEmptyMessage(10);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            receiveThread.start();
        }

        /**
         * 关闭串口
         */
        public void closeSerialPort() {

            if (mSerialPort != null) {
                mSerialPort.close();
            }
            if (mInputStream != null) {
                try {
                    mInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (mOutputStream != null) {
                try {
                    mOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }


        private void initSubmitItem() {
            submit_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_submit, null);
            ll_content = (LinearLayout) submit_layout.findViewById(R.id.ll_content);
            tv_test_result = (TextView) submit_layout.findViewById(R.id.tv_testresult);
            bt_exit = (Button) submit_layout.findViewById(R.id.bt_exit);
            bt_exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DiagnoseHomeActivity.this.finish();
                }
            });

        }


        private void initBTItem() {
            bt_layout = (LinearLayout) LayoutInflater.from(
                    DiagnoseHomeActivity.this).inflate(R.layout.layout_bt,
                    null);
            bt_submit = (TextView) bt_layout.findViewById(R.id.bt_submit);
        }

        private void initTfItem() {
            tf_layout = (LinearLayout) LayoutInflater.from(
                    DiagnoseHomeActivity.this).inflate(R.layout.layout_tf,
                    null);
            tf_submit = (TextView) tf_layout.findViewById(R.id.tf_submit);

        }

        private void initFengShanItem() {
            fengshan_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_fengshan, null);
            bt_fengshan_pass = (Button) fengshan_layout.findViewById(R.id.bt_fengshan_pass);
            bt_fengshan_pass.setOnClickListener(new NextItemListener());
            bt_fengshan_fail = (Button) fengshan_layout.findViewById(R.id.bt_fengshan_fail);
            bt_fengshan_fail.setOnClickListener(new NextItemListener());

        }


        private void initMicItem() {
            mic_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.layout_mic, null);
            bt_pressToSay = (Button) mic_layout.findViewById(R.id.bt_PressToSay);
            bt_playAudio = (Button) mic_layout.findViewById(R.id.btnPlayAudio);
            tv_log = (TextView) mic_layout.findViewById(R.id.tvLog);

            initUIControlerEventHandlers();
            bt_playAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setIsplaying(!mIsplaying);


                }
            });
            bt_mic_pass = (Button) mic_layout.findViewById(R.id.bt_mic_pass);
            bt_mic_fail = (Button) mic_layout.findViewById(R.id.bt_mic_fail);
            bt_mic_pass.setOnClickListener(new NextItemListener());
            bt_mic_fail.setOnClickListener(new NextItemListener());

        }

        private void initEthernetItem() {
            ethernet_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_ethernet, null);
            tv_eth1 = (TextView) ethernet_layout.findViewById(R.id.tv_eth1);
            tv_eth2 = (TextView) ethernet_layout.findViewById(R.id.tv_eth2);
            tv_eth3 = (TextView) ethernet_layout.findViewById(R.id.tv_eth3);
            tv_eth4 = (TextView) ethernet_layout.findViewById(R.id.tv_eth4);
            bt_eth1 = (Button) ethernet_layout.findViewById(R.id.bt_eth1);
            bt_eth2 = (Button) ethernet_layout.findViewById(R.id.bt_eth2);
            bt_eth3 = (Button) ethernet_layout.findViewById(R.id.bt_eth3);
            bt_eth4 = (Button) ethernet_layout.findViewById(R.id.bt_eth4);
            bt_eth1.setOnClickListener(new NextItemListener());
            bt_eth2.setOnClickListener(new NextItemListener());
            bt_eth3.setOnClickListener(new NextItemListener());
            bt_eth4.setOnClickListener(new NextItemListener());
            tv_eth_test = (TextView) ethernet_layout.findViewById(R.id.tv_eth_test);

        }

        private void initUsbItem() {
            usb_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_usb, null);
            tv_usbtest = (TextView) usb_layout.findViewById(R.id.tv_usbtest);
            tv_usb1_submit = (TextView) usb_layout.findViewById(R.id.tv_usb1_submit);
            tv_usb2_submit = (TextView) usb_layout.findViewById(R.id.tv_usb2_submit);
            tv_usb3_submit = (TextView) usb_layout.findViewById(R.id.tv_usb3_submit);
            tv_usb4_submit = (TextView) usb_layout.findViewById(R.id.tv_usb4_submit);
            tv_usb5_submit = (TextView) usb_layout.findViewById(R.id.tv_usb5_submit);
            tv_usb6_submit = (TextView) usb_layout.findViewById(R.id.tv_usb6_submit);
            tv_usb7_submit = (TextView) usb_layout.findViewById(R.id.tv_usb7_submit);
            tv_usb8_submit = (TextView) usb_layout.findViewById(R.id.tv_usb8_submit);
            tv_usb9_submit = (TextView) usb_layout.findViewById(R.id.tv_usb9_submit);
            tv_usb10_submit = (TextView) usb_layout.findViewById(R.id.tv_usb10_submit);

            bt_usb1 = (Button) usb_layout.findViewById(R.id.bt_usb1);
            bt_usb1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    usbTest();
                }
            });
            bt_usb_isok = (Button) usb_layout.findViewById(R.id.bt_usb_isok);
            bt_usb_isok.setOnClickListener(new NextItemListener());


        }

        private void initSpeakerItem() {
            laba_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_laba, null);
            bt_laba_start = (Button) laba_layout.findViewById(R.id.bt_laba_start);
            bt_pass_submit_04 = (Button) laba_layout.findViewById(R.id.bt_pass_submit_04);
            bt_fail_submit_04 = (Button) laba_layout.findViewById(R.id.bt_fail_submit_04);
            bt_pass_submit_04.setOnClickListener(new NextItemListener());
            bt_fail_submit_04.setOnClickListener(new NextItemListener());
            bt_laba_start.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPlaying) {
                        startMediaPlayer();
                        bt_laba_start.setText("停止测试");
                    } else {
                        startMediaPlayer();
                        bt_laba_start.setText("开始测试");
                    }


                }
            });


        }

        private void initWifiItem() {
            wifi_layout = (LinearLayout) LayoutInflater.from(mContext)
                    .inflate(R.layout.item_layout_wifi, null);
            tv_wifi_submit = (TextView) wifi_layout.findViewById(R.id.wifi_submit);
            tv_wifi_adv = (TextView) wifi_layout.findViewById(R.id.wifi_adv);

        }

    }

    private void saveResult() {
        String pass = "";
        if (all_pass) {
            pass = "Pass";
        } else {
            pass = "Fail";
        }
        String sn = getDeviceSerial();
        try {
            fileName = sn + "_ " + pass + "_ " + "TestResult" + ".txt";
            File file = new File("/mnt/sdcard/" + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream is = new FileOutputStream(file, false);
            is.write(submit_content.getBytes());
            is.flush();
            is.close();
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

    }

    private String getDeviceSerial() {
        String serial = "unknown";
        try {
            Class clazz = Class.forName("android.os.Build");
            Class paraTypes = Class.forName("java.lang.String");
            Method method = clazz.getDeclaredMethod("getString", paraTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            serial = (String) method.invoke(new Build(), "ro.serialno");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return serial;
    }

    private void bTTest() {
        new BTTest().start();
    }

    private void wifiTest(String ssid) {
        new WifiTest(mContext, handler, ssid, passward).start();
    }

    private void TfCardTest() {
        new TFTest().start();
    }

    private void usbTest() {
        new USBTest(handler).start();
    }

    private void ethernetTest(int eth) {
        new EthernetTest(ip, eth, handler).start();
    }

    public void startMediaPlayer() {

        if (mp != null) {
            mp.reset();
        }

        if (!isPlaying) {


            try {

                mp = MediaPlayer.create(mContext, R.raw.advert);
                mp.start();
                isPlaying = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mp.stop();
            isPlaying = false;
        }

    }


    private void setItemResult(int pass) {
        final_pos = pos;
        if (pass == 1) {

            results[pos] = preDevices[pos] + "  ------pass";
            if (pos == 3) {
                for (int i = 0; i < usbResults.length; i++) {
                    results[3] += "\n		" + usbResults[i];
                }
            }


            if (pos == 4) {
                for (int i = 0; i < ethResult.length; i++) {
                    results[4] += "\n		" + ethResult[i];
                }
            }


            if (pos == 8) {
                for (int i = 0; i < serialResult.length; i++) {
                    results[8] += "\n		" + serialResult[i];
                }

            }


        } else {
//            LogInFile.write("/sdcard/pos.txt", "setItemResult: " + pos + " preDevices " + preDevices.length
//                    + "   results" + results.length);
            results[pos] = preDevices[pos] + "  ------fail";
            if (pos == 3) {
                for (int i = 0; i < usbResults.length; i++) {
                    results[3] += "\n		" + usbResults[i];
                }
            }

            if (pos == 4) {
                for (int i = 0; i < ethResult.length; i++) {
                    results[4] += "\n		" + ethResult[i];
                }
            }

            if (pos == 8) {
                for (int i = 0; i < serialResult.length; i++) {
                    results[8] += "\n		" + serialResult[i];
                }

            }

        }
        Log.i(TAG, "pos:" + pos + " setresult:" + results[pos]);
    }


    class NextItemListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            int id = v.getId();
            v.setEnabled(false);
            switch (id) {

                case R.id.bt_usb_isok:
                    usbTestOK();
                    break;


                case R.id.bt_fengshan_pass:
                    setItemResult(1);
                    handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    break;
                case R.id.bt_fengshan_fail:
                    Toast.makeText(mContext, "风扇异常，自动退出测试", Toast.LENGTH_SHORT).show();
                    finish();
                    break;


                case R.id.bt_mic_pass:
                    handler.sendEmptyMessage(NEXT_ITEM);
                    setItemResult(1);
                    mExecutorService.shutdownNow();
                    releaseRecorder();
                    stopPlay();
                    break;

                case R.id.bt_mic_fail:
                    handler.sendEmptyMessage(NEXT_ITEM);
                    setItemResult(0);
                    mExecutorService.shutdownNow();
                    releaseRecorder();
                    stopPlay();
                    break;


                case R.id.bt_pass_submit_04:
                    setItemResult(1);
                    if (isPlaying) {
                        startMediaPlayer();
                    }
                    handler.sendEmptyMessage(NEXT_ITEM);
                    break;


                case R.id.bt_fail_submit_04:

                    setItemResult(0);
                    if (isPlaying) {
                        startMediaPlayer();
                    }
                    handler.sendEmptyMessage(NEXT_ITEM);
                    break;

                case R.id.bt_eth1:
                    ethernetTest(1);
                    break;
                case R.id.bt_eth2:
                    ethernetTest(2);
                    break;
                case R.id.bt_eth3:
                    ethernetTest(3);
                    break;
                case R.id.bt_eth4:
                    ethernetTest(4);
                    break;

                case R.id.btn_serial_pass01:
                    serialResult[0] = "P1串口测试通过";
                    break;

                case R.id.btn_serial_pass02:
                    serialResult[1] = "P2串口测试通过";
                    break;

                case R.id.btn_serial_pass03:
                    serialResult[2] = "P3串口测试通过";
                    break;

                case R.id.btn_serial_pass04:
                    serialResult[0] = "串口测试通过";
                    break;

                case R.id.btn_serial_fail01:
                    serialResult[0] = "P1串口测试失败";
                    break;
                case R.id.btn_serial_fail02:
                    serialResult[1] = "P2串口测试失败";
                    break;
                case R.id.btn_serial_fail03:
                    serialResult[2] = "P3串口测试失败";
                    break;
                case R.id.btn_serial_fail04:
                    serialResult[0] = "串口测试失败";
                    break;

                case R.id.btn_serial_end:
                    for (int i = 0; i < serialResult.length; i++) {
                        serial = serialResult[i] + " ";
                    }
                    if (serial.contains("失败")) {
                        setItemResult(0);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);
                    } else {
                        setItemResult(1);
                        handler.sendEmptyMessageDelayed(NEXT_ITEM, 1000);

                    }

                    break;

            }
        }
    }

    class BTTest extends Thread {
        @Override
        public void run() {

            // 检查设备是否支持蓝牙
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                Log.i(TAG, "bt adapter find fail");
                return;
            }
            // 打开蓝牙
            if (!adapter.isEnabled()) {
                boolean isBTEnable = adapter.enable();

                if (isBTEnable) {
                    Log.i(TAG, "bt open ok");
                } else {
                    Log.i(TAG, "bt open fail");
                    Message msg = new Message();
                    msg.what = BT;
                    msg.arg1 = 0;
                    handler.sendMessage(msg);
                    return;
                }
                /*
                 * Intent intent = new Intent(
				 * BluetoothAdapter.ACTION_REQUEST_ENABLE); // 设置蓝牙可见性，最多300秒
				 * intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION
				 * , 300); startActivity(intent);
				 */
            }
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
            intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(receiver, intentFilter);
            try {
                boolean starDiscovery = false;
                while (!starDiscovery) {
                    starDiscovery = adapter.startDiscovery();
                    /*if(starDiscovery){
                        Thread.sleep(2000);
						if(adapter.isDiscovering()){
							break;
						}

					}*/

                    Thread.sleep(500);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            int i = 0;
            int btFailTime = 0;
            while (true) {
                if (btFailTime == 3 || i == 40) {
                    break;
                }
                if (!findBT) {
                    if (BTDiscoveryFinish) {
                        try {
                            Thread.sleep(200);
                            boolean starDiscovery = false;
                            while (!starDiscovery) {
                                starDiscovery = adapter.startDiscovery();
                                Thread.sleep(500);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        BTDiscoveryFinish = false;
                        btFailTime++;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.i(TAG, "bt find successfull");
                    Message msg = new Message();
                    msg.what = BT;
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
                    return;
                }
                i++;
            }
            Log.i(TAG, "bt open ok");

            Message msg = new Message();
            msg.what = BT;
            msg.arg1 = 0;
            handler.sendMessage(msg);

        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "action" + action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device == null || device.getName() == null) {
                    Log.d(TAG, "onReceive: 设备为空");
                    return;
                }
                Log.i(TAG, "find bt:" + device.getName() + "");
                if (device.getName().equalsIgnoreCase(btName)) {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    findBT = true;
                    Log.i(TAG, "find " + btName);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                BTDiscoveryFinish = true;
            }
        }
    };

    // TFTest----------------------------------------------------------------------------------------
    class TFTest extends Thread {
        @Override
        public void run() {
            int bj = -1;
            bj = nativeTFTest();
            Log.d(TAG, "run: " + bj);
            Message msg = new Message();
            msg.what = TF;
            msg.arg1 = bj + 1;
            handler.sendMessageDelayed(msg, 1000);
        }
    }

    static {
        System.loadLibrary("stb_diagnose_test");
    }

    private static native int nativeTFTest();
// TFTest----------------------------------------------------------------------------------------


    // mic测试----------------------------------------------------------------------------------------
    public void setIsplaying(boolean isplaying) {
        mIsplaying = isplaying;

        layoutItem.bt_playAudio.setText(
                mIsplaying ? R.string.record_audio_stop_playing : R.string.record_audio_playing);

        if (mIsplaying) {

            if (mAudioFile != null) {
                mExecutorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        doPlayAudio(mAudioFile);
                    }
                });
            } else {
                Toast.makeText(mContext, "请先录音...",
                        Toast.LENGTH_SHORT).show();
            }


        }
    }


    /**
     * running in background thread
     */
    private void doPlayAudio(File audioFile) {

        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(audioFile.getAbsolutePath());
            mMediaPlayer.setVolume(1.0f, 1.0f);
            mMediaPlayer.setLooping(false);

            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopPlay();
                }
            });

            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    echoPayFail();
                    stopPlay();
                    return true;
                }
            });

            mMediaPlayer.prepare();
            mMediaPlayer.start();

        } catch (Exception e) {
            Log.e(TAG, "播放失败.", e);
            echoPayFail();
            stopPlay();
        }

    }


    private void initUIControlerEventHandlers() {
        layoutItem.bt_pressToSay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecordAudio();
                        break;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        stopRecordAudio();
                        break;

                    default:
                        break;

                }

                return true;
            }
        });
    }

    /**
     * 开始录音
     */
    private void startRecordAudio() {
        layoutItem.bt_pressToSay.setText(R.string.record_audio_speaking);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                releaseRecorder();

                if (!doStartRecordAudio()) {
                    echoFail();
                }
            }
        });

    }

    /**
     * 停止录音
     */
    private void stopRecordAudio() {
        layoutItem.bt_pressToSay.setText(R.string.record_audio_press_to_say);

        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                if (!doStopRecordAudio()) {
                    echoFail();
                }

                releaseRecorder();
            }
        });

    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    private boolean doStartRecordAudio() {
        mMediaRecorder = new MediaRecorder();

        // 配置
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioSamplingRate(44100); //44.1kHz
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setAudioEncodingBitRate(96000); //96kbps

        // 创建录音文件
        if (!createAudioFile()) return false;

        mMediaRecorder.setOutputFile(mAudioFile.getAbsolutePath());

        try {
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            mBeginRecordInMillis = System.currentTimeMillis();
        } catch (Exception e) {
            Log.e(TAG, "准备录音或启动录音时失败。", e);
            return false;
        }

        return true;
    }

    private boolean createAudioFile() {
        try {
            mAudioFile = RecordAudioUtils.createAudioFile(RecordAudioUtils.AUDIO_PCM);
        } catch (IOException e) {
            Log.e(TAG, "开始录音时，创建文件失败。", e);
            return false;
        }
        return true;
    }

    private boolean doStopRecordAudio() {

        try {
            mMediaRecorder.stop();
            mEndRecordInMillis = System.currentTimeMillis();

            final int recordPeriodInSecond = (int) ((mEndRecordInMillis - mBeginRecordInMillis) / 1000);

            // 只接受超过3秒的录音
            if (recordPeriodInSecond >= 3) {
                UiThreadUtils.runInUIThread(new Runnable() {
                    @Override
                    public void run() {
                        layoutItem.tv_log.setText(layoutItem.tv_log.getText() + "\n录音时长：" + recordPeriodInSecond + "秒!");
                    }
                });
            } else {
                mAudioFile.delete();
            }

        } catch (IllegalStateException e) {
            Log.e(TAG, "停止录音时失败。", e);
            return false;
        }

        return true;
    }

    /**
     * 反馈错误给用户
     */
    private void echoFail() {
        mAudioFile = null;
        UiThreadUtils.showToast(mContext, "录音失败");
    }

    private void releaseRecorder() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }


    /**
     * running in background thread
     */
    private void echoPayFail() {
        UiThreadUtils.showToast(mContext, "插放失败");
    }

    private void stopPlay() {
        setIsplaying(false);

        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }


    public static String getExtSDCard() {
        File[] files = new File("/mnt").listFiles();
        String sdcard = Environment.getExternalStorageDirectory().getAbsolutePath().toLowerCase();
        String file;
        for (int i = 0; i < files.length; i++) {
            file = files[i].getAbsolutePath().toLowerCase();
            if (!file.equals(sdcard) && (file.contains("ext") || file.contains("sdcard"))) {
                return file;
            }
        }
        return null;
    }
    //--------------------------------------------------------------------------------------------

}
