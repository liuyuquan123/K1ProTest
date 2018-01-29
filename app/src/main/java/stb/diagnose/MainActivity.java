package stb.diagnose;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import stb.diagnose.util.FileUtils;


public class MainActivity extends Activity implements View.OnClickListener {
    private Button bt_sn;
    private Button bt_wmac_bulid;
    private Button bt_emac_bulid;
    private Button bt_test;
    private Button bt_exit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initView();
    }

    private void initView() {
        bt_sn = (Button) findViewById(R.id.bt_sn);
        bt_wmac_bulid = (Button) findViewById(R.id.bt_wmac_bulid);
        bt_emac_bulid = (Button) findViewById(R.id.bt_emac_bulid);
        bt_test = (Button) findViewById(R.id.bt_test);
        bt_exit = (Button) findViewById(R.id.bt_exit_home);
        bt_sn.setOnClickListener(this);
        bt_wmac_bulid.setOnClickListener(this);
        bt_emac_bulid.setOnClickListener(this);
        bt_test.setOnClickListener(this);
        bt_exit.setOnClickListener(this);
        File file = new File("/mnt/sdcard/data/data.txt");
        if (!file.exists()){
            FileUtils.getInstance(this).copyAssetsToSD("data","data").setFileOperateCallback(new FileUtils.FileOperateCallback() {
                @Override
                public void onSuccess() {
                    Log.d("liu", "成功: ");
                }

                @Override
                public void onFailed(String error) {
                    Log.d("liu", "失败: ");

                }
            });
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.bt_sn:
                Intent intent1 = new Intent(MainActivity.this, SNBuildActivity.class);
                startActivity(intent1);
                break;

            case R.id.bt_wmac_bulid:
                startActivity(new Intent(MainActivity.this, WifiMacBuildActivity.class));

                break;
            case R.id.bt_emac_bulid:
                startActivity(new Intent(MainActivity.this, MacBuildActivity.class));

                break;
            case R.id.bt_test:
                startActivity(new Intent(MainActivity.this, DiagnoseHomeActivity.class));
                break;

            case R.id.bt_exit_home:
                finish();
                break;


        }
    }

    // 写一个文件到SDCard
    private void writeFileToSDCard() throws IOException {
        // 比如可以将一个文件作为普通的文档存储，那么先获取系统默认的文档存放根目录
        File parent_path = Environment.getExternalStorageDirectory();

        // 可以建立一个子目录专门存放自己专属文件
        File dir = new File(parent_path.getAbsoluteFile(), "zhangphil");
        dir.mkdir();

        File file = new File(dir.getAbsoluteFile(), "myfile.txt");

        Log.d("文件路径", file.getAbsolutePath());

        // 创建这个文件，如果不存在
        file.createNewFile();

        FileOutputStream fos = new FileOutputStream(file);

        String data = "hello,world! Zhang Phil @ CSDN";
        byte[] buffer = data.getBytes();

        // 开始写入数据到这个文件。
        fos.write(buffer, 0, buffer.length);
        fos.flush();
        fos.close();

        Log.d("文件写入", "成功");
    }
}
