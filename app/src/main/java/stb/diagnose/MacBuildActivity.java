package stb.diagnose;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MacBuildActivity extends Activity implements OnKeyListener, OnClickListener {

    private TextView tv_getmac;
    private EditText et_inputmac;
    private Button savemac;
    private Button bt_exit_mac;
    private Context mContext;
    int ret = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_macbulid);
        initView();
    }

    private void initView() {
        mContext = this;
        gsData.initDevice();
        //E2PFileRW.initDevice();
        tv_getmac = (TextView) findViewById(R.id.getmac);
        et_inputmac = (EditText) findViewById(R.id.inputmac);
        et_inputmac.setOnKeyListener(this);
        savemac = (Button) findViewById(R.id.savemac);
        savemac.setOnClickListener(this);
        bt_exit_mac = (Button) findViewById(R.id.bt_exit_mac);
        bt_exit_mac.setOnClickListener(this);
        String addr = gsData.getMacAddress().replace(":", "");
        //String addr=E2PFileRW.getMacAddress().replace(":","");
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String macaddress = wifiManager.getConnectionInfo().getMacAddress();

        System.out.println("vallen mac address" + macaddress);
        if (addr.equals("000000000000")) {
            addr = "";
        }
        tv_getmac.setText(addr);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.inputmac && keyCode == KeyEvent.KEYCODE_ENTER) {
            et_inputmac.clearFocus();
            savemac.requestFocus();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.savemac:
                if (et_inputmac.getText().toString().length() != 12) {
                    ret = 1;
                    showDialog();
                    return;
                }
                if (!et_inputmac.getText().toString().startsWith("28FD805")) {
                    ret = 2;
                    showDialog();
                    return;
                }
                if (tv_getmac.getText().toString().length() == 12) {
                    ret = 3;
                    showDialog();
                } else {
                    setMac();
                }
                break;
            case R.id.bt_exit_mac:
                Intent intent = new Intent(mContext, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                break;
        }


    }

    private void setMac() {
        savemac.setEnabled(false);
        int ret = gsData.setMacAddress(et_inputmac.getText().toString());
        //boolean ret=E2PFileRW.setMacAddress(inputmac.getText().toString());
        if (ret == 0) {
            Toast.makeText(this, "MAC烧录成功，将在重启后生效", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "MAC烧录失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog() {

        Builder dialog = new Builder(this);
        dialog.setTitle("警告！");
        if (ret == 1) {
            dialog.setMessage("MAC必须12位!");
        } else if (ret == 2) {
            dialog.setMessage("MAC地址不符合规范!");
        } else if (ret == 3) {
            dialog.setMessage("MAC已经存在，确认覆盖?");
        }


        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {

                if (ret == 1 || ret == 2) {

                } else {
                    setMac();
                }

            }
        });
        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });
        dialog.create();
        dialog.show();
    }
}