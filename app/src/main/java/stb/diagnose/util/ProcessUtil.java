package stb.diagnose.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/28.
 */

public class ProcessUtil {


    private static final String TAG = "pubsubUtil";

    private static List<String> pmb_state = new ArrayList<>();
    private static List<String> trk_state = new ArrayList<>();
    private static List<String> mcb_state = new ArrayList<>();
    private static String pmb_exception = "";
    private static String mcb_exception = "";
    private static String trk_exception = "";

    static Handler mainHandler = new Handler(Looper.getMainLooper());


    public static void lightProcess(String mode) {
        Log.i(TAG, "xwj connect");
        String s = "";
        Process process2 = null;
        try {
            String line = null;
            String command = "/system/bin/led_control.sh " + mode;
            process2 = Runtime.getRuntime().exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    process2.getInputStream()));
            while ((line = in1.readLine()) != null) {
                s += line + "\n";
                System.out.println("vallen light test result  :" + s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static String getXWJversion() {
        Log.i(TAG, "xwj version");
        String s = "";
        Process process2 = null;
        try {
            String line = null;
            String command = "/system/bin/pltver";
            process2 = Runtime.getRuntime().exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    process2.getInputStream()));
            while ((line = in1.readLine()) != null) {
                s += line + "\n";
                System.out.println("vallen version test result  :" + s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return s;
    }


    public static String copyVoiceText(Context mContext) {
        Log.i(TAG, "copy txt");
        Process process = null;
        String path = getExternalStorageDirectory();
        String srcfile = path + "/VoiceText.txt";
        String destfile = "/mnt/sdcard/pubsub/";


        try {
            process = new ProcessBuilder()
                    .command("/system/bin/cp", srcfile, destfile)
                    .redirectErrorStream(true)
                    .start();

            InputStream in = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            StringBuilder buffer = new StringBuilder();
            String line = "";
            //System.out.println("vallen exec "+reader.readLine());
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            Log.i(TAG, buffer.toString());
            if (buffer.toString().contains("No")) {

                copy(mContext, "VoiceText.txt", destfile, "VoiceText.txt");

            }
            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
            //return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }




       /* try {
            String line = null;
            String command = "cp "+path+"/VoiceText.txt"+" /mnt/sdcard/pubsub/";
            process2 = Runtime.getRuntime().exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(
                    process2.getInputStream()));

            Log.i(TAG, " vallen exec process" +in1.readLine() +command +s.isEmpty());
            while ((line = in1.readLine()) != null) {
                s += line + "\n";
                System.out.println("vallen copy file test result  :" + s);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }



              return s;*/
    }

    public static void copy(Context myContext, String ASSETS_NAME,
                            String savePath, String saveName) {
        System.out.println("vallen copy file from assets");
        String filename = savePath + "/" + saveName;
        File dir = new File(savePath);// 如果目录不存在，创建这个目录
        if (!dir.exists())
            dir.mkdir();
        try {
            if (!(new File(filename)).exists()) {

                System.out.println("vallen copy file from assets if file is not exist");
                InputStream is = myContext.getResources().getAssets()
                        .open(ASSETS_NAME);
                FileOutputStream fos = new FileOutputStream(filename);
                byte[] buffer = new byte[7168];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getExternalStorageDirectory() {
        String dir = new String();
        try {
            Runtime runtime = Runtime.getRuntime();
            Process proc = runtime.exec("mount");
            InputStream is = proc.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            String line;
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                if (line.contains("secure")) continue;
                if (line.contains("asec")) continue;

                if (line.contains("fat")) {
                    String columns[] = line.split(" ");
                    if (columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1]);
                        break;
                    }
                } else if (line.contains("fuse")) {
                    String columns[] = line.split(" ");
                    if (line.contains("usb") && columns != null && columns.length > 1) {
                        dir = dir.concat(columns[1]);
                        break;
                    }
                    continue;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dir;
    }

}



