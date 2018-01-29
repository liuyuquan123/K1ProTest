package stb.diagnose;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import stb.diagnose.util.E2PFileRW;


public class SNBuildActivity extends Activity implements OnKeyListener, OnClickListener {

    private Context mContext;
    private TextView getsn;
    private EditText inputsn;
    private Button savesn;
    private Button bt_exit_sn;
    int ret = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sn);
        initView();
    }

    private void initView() {
        mContext = this;
        E2PFileRW.initDevice();
        getsn = (TextView) findViewById(R.id.getsn);
        inputsn = (EditText) findViewById(R.id.inputsn);
        inputsn.setOnKeyListener(this);
        savesn = (Button) findViewById(R.id.savesn);
        savesn.setOnClickListener(this);
        bt_exit_sn = (Button) findViewById(R.id.bt_exit_sn);
        bt_exit_sn.setOnClickListener(this);
        getsn.setText(E2PFileRW.getMcidNumber());

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        if (v.getId() == R.id.inputsn && keyCode == KeyEvent.KEYCODE_ENTER) {
            inputsn.clearFocus();
            savesn.requestFocus();
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.savesn) {
            if (inputsn.getText().toString().length() != 16) {
                ret = 1;
                showDialog();
                return;
            }

            if (getsn.getText().toString().length() == 16) {
                ret = 3;
                showDialog();
            } else {
                setSN();
            }
        }

        if (v.getId() == R.id.bt_exit_sn) {
            Intent intent = new Intent(mContext, MainActivity.class);
            startActivity(intent);
            finish();
        }


    }

    private void setSN() {
        savesn.setEnabled(false);
        boolean isOk = E2PFileRW.setMcidNumber(inputsn.getText().toString());
        if (isOk) {
            Toast.makeText(this, "SN烧录成功,将在重启后生效", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "SN烧录失败", Toast.LENGTH_SHORT).show();
        }
    }

    public void showDialog() {

        Builder dialog = new Builder(this);
        dialog.setTitle("警告！");
        if (ret == 1) {
            dialog.setMessage("SN必须16位!");
        } else if (ret == 2) {
            dialog.setMessage("SN不符合规范!");
        } else if (ret == 3) {
            dialog.setMessage("SN已经存在，确认覆盖?");
        }


        dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                if (ret == 1 || ret == 2) {

                } else {
                    setSN();
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
