package com.creative.housefinder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.appdata.MydApplication;

public class SettingsActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_name, tv_number;

    private EditText ed_name, ed_number;

    private LinearLayout ll_container_name, ll_container_number, ll_container_btn;

    private ImageView img_edit_name, img_edit_number;

    private Button btn_save, btn_cancel;

    private static final int KEY_NO_EDIT = 0;
    private static final int KEY_NAME = 1;
    private static final int KEY_NUMBER = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initToolbar(true);

        init();

        updateUi(KEY_NO_EDIT);
    }

    private void init() {

        tv_name = findViewById(R.id.tv_name);
        ed_name = findViewById(R.id.ed_name);
        if (MydApplication.getInstance().getPrefManger().getName().isEmpty()) {
            tv_name.setText("Not set");
            ed_name.setHint("Name");
        } else {
            tv_name.setText(MydApplication.getInstance().getPrefManger().getName());
            ed_name.setText(MydApplication.getInstance().getPrefManger().getName());
        }

        tv_number = findViewById(R.id.tv_number);
        ed_number = findViewById(R.id.ed_number);
        if (MydApplication.getInstance().getPrefManger().getNumber().isEmpty()) {
            tv_number.setText("Not set");
            ed_number.setHint("Number");
        } else {
            tv_number.setText(MydApplication.getInstance().getPrefManger().getNumber());
            ed_number.setText(MydApplication.getInstance().getPrefManger().getNumber());
        }


        img_edit_name = findViewById(R.id.img_edit_name);
        img_edit_number = findViewById(R.id.img_edit_number);

        ll_container_name = findViewById(R.id.ll_container_name);
        ll_container_number = findViewById(R.id.ll_container_number);
        ll_container_btn = findViewById(R.id.ll_container_btn);


        btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(this);

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);


        img_edit_name = findViewById(R.id.img_edit_name);
        img_edit_name.setOnClickListener(this);

        img_edit_number = findViewById(R.id.img_edit_number);
        img_edit_number.setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        Log.d("DEBUG","its here");

        if (id == R.id.btn_save) {
            if (ed_name.getVisibility() == View.VISIBLE && ed_name.getText().toString().isEmpty()) {
                ed_name.setError("Required");
                return;
            }

            if (ed_number.getVisibility() == View.VISIBLE && ed_number.getText().toString().isEmpty()) {
                ed_number.setError("Required");
                return;
            }

            if (ed_name.getVisibility() == View.VISIBLE) {
                tv_name.setText(ed_name.getText().toString());
                MydApplication.getInstance().getPrefManger().setName(ed_name.getText().toString());
            }

            if (ed_number.getVisibility() == View.VISIBLE) {
                tv_number.setText(ed_number.getText().toString());
                MydApplication.getInstance().getPrefManger().setNumber(ed_number.getText().toString());
            }

            Toast.makeText(this,"Successfully saved",Toast.LENGTH_SHORT).show();

            updateUi(KEY_NO_EDIT);

        }

        if (id == R.id.btn_cancel) {
            updateUi(KEY_NO_EDIT);
        }

        if(id == R.id.img_edit_name){
            updateUi(KEY_NAME);
        }

        if(id == R.id.img_edit_number){
            updateUi(KEY_NUMBER);
        }
    }


    private void updateUi(int updateType) {


        switch (updateType) {
            case KEY_NAME:
                ll_container_name.setVisibility(View.GONE);
                ed_name.setVisibility(View.VISIBLE);
                ll_container_btn.setVisibility(View.VISIBLE);
                break;
            case KEY_NUMBER:
                ll_container_number.setVisibility(View.GONE);
                ed_number.setVisibility(View.VISIBLE);
                ll_container_btn.setVisibility(View.VISIBLE);
                break;
            case KEY_NO_EDIT:
                ll_container_name.setVisibility(View.VISIBLE);
                ll_container_number.setVisibility(View.VISIBLE);
                ed_number.setVisibility(View.GONE);
                ed_name.setVisibility(View.GONE);
                ll_container_btn.setVisibility(View.GONE);
                CommonMethods.hideKeyboardForcely(this,ed_name);
                CommonMethods.hideKeyboardForcely(this,ed_number);
                break;
        }
    }


}
