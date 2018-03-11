package com.creative.housefinder;

import android.*;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.creative.housefinder.Utility.CommonMethods;
import com.creative.housefinder.Utility.DeviceInfoUtils;
import com.creative.housefinder.Utility.GpsEnableTool;
import com.creative.housefinder.Utility.LastLocationOnly;
import com.creative.housefinder.Utility.RunnTimePermissions;
import com.creative.housefinder.alertbanner.AlertDialogForAnything;
import com.creative.housefinder.appdata.MydApplication;
import com.creative.housefinder.fragment.HouseListFragment;
import com.creative.housefinder.model.House;
import com.creative.housefinder.model.Houses;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class MainActivity extends BaseActivity {

    private List<House> houses;

    private static final String TAG_HOUSE_LIST_FRAGMENT = "House List Fragment";
    private HouseListFragment houseListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initToolbar();


        if (savedInstanceState == null) {

            /**
             * This is marshmallow runtime Permissions
             * It will ask user for grand permission in queue order[FIFO]
             * If user gave all permission then check whether user device has google play service or not!
             * NB : before adding runtime request for permission Must add manifest permission for that
             * specific request
             * */
            if (RunnTimePermissions.requestForAllRuntimePermissions(this)) {
                if (!DeviceInfoUtils.isGooglePlayServicesAvailable(MainActivity.this)) {
                    AlertDialogForAnything.showAlertDialogWhenComplte(this, "Warning", "This app need google play service to work properly. Please install it!!", false);
                }

                //getSupportFragmentManager()
                //        .beginTransaction()
                //        .add(R.id.content_layout, new HouseListFragment(), TAG_HOME_FRAGMENT)
                //        .commit();


                houseListFragment = new HouseListFragment();
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_layout, houseListFragment, TAG_HOUSE_LIST_FRAGMENT)
                        .commit();
            }

        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RunnTimePermissions.PERMISSION_ALL) {
            // DeviceInfoUtils.checkMarshMallowPermission(this);
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int result2 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            int result3 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int result4 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (result == PackageManager.PERMISSION_GRANTED
                    && result2 == PackageManager.PERMISSION_GRANTED
                    && result3 == PackageManager.PERMISSION_GRANTED
                    && result4 == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "fragment attach");

                houseListFragment = new HouseListFragment();
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_layout, houseListFragment, TAG_HOUSE_LIST_FRAGMENT)
                        .commit();
            }
        }
    }


    public List<House> getHouses() {
        return houses;
    }

    public void setHouses(List<House> houses) {
        this.houses = houses;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (houseListFragment != null && houseListFragment.isAdded()) {
            houseListFragment.onActivityResult(requestCode, resultCode, data);
        }

    }


    public boolean onCreateOptionsMenu(Menu paramMenu) {
        getMenuInflater().inflate(R.menu.menu_main, paramMenu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem paramMenuItem) {

        switch (paramMenuItem.getItemId()) {

            case R.id.action_help:
                View menuItemView = findViewById(R.id.action_help);
                showPopUpWindow(menuItemView);
                //startActivity(new Intent(getActivity(), WishListActivity.class));
                // Toast.makeText(MainActivity.this,"Please publish your app on play store first!",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_setting:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;

            case R.id.action_export:
                showProgressDialog("Please wait...",true,false);
                CommonMethods.copyRawFileToExternalMemory2(this);
                dismissProgressDialog();
                AlertDialogForAnything.showAlertDialogWhenComplte(this,"Alert","File exported to My Files -> Tattletale folder in SD CARD",true);
                break;
            case R.id.action_import:
                //CommonMethods.copyRawFileToInternalMemory();
                new LongOperation().execute();
                //if(houseListFragment != null && houseListFragment.isAdded()){
                //    houseListFragment.importHouses();
                //}
                break;
        }

        return false;
    }

    private class LongOperation extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String line = "";
            StringBuilder text = new StringBuilder();
            try {
                // Create new file to copy into.
                //File file = new File(Environment.getExternalStorageDirectory() + java.io.File.separator + "NewFile.dat");
                String filename = CommonMethods.FILE_NAME;
                File exportDir = CommonMethods.DIRECTORY;
                File outFile = new File(exportDir, filename);


                BufferedReader br = new BufferedReader(new FileReader(outFile));

                while ((line = br.readLine()) != null) {
                    //Log.d("DEBUG",s);
                    text.append(line);
                    text.append('\n');
                }
                br.close();

               // Log.d("DEBUG",fileContent);
                String json = "{ \"houses\": " + text.toString() + "}";
                List<House> houses = MydApplication.gson.fromJson(json, Houses.class).getHouses();
                MydApplication.getInstance().getPrefManger().setHouses(houses);

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            dismissProgressDialog();
            Toast.makeText(MainActivity.this,"Successfully imported!",Toast.LENGTH_LONG).show();
            if(houseListFragment != null && houseListFragment.isAdded()){
                houseListFragment.importHouses();
            }
        }

        @Override
        protected void onPreExecute() {
            showProgressDialog("Please wait...",true,false);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }

    private PopupWindow popupwindow_obj;

    public void showPopUpWindow(View v) {
        popupwindow_obj = popupDisplay();
        popupwindow_obj.showAsDropDown(v, -40, 18);
    }

    public PopupWindow popupDisplay() {

        final PopupWindow popupWindow = new PopupWindow(this);
        // inflate your layout or dynamically add view
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.popup_window_info, null);

        popupWindow.setFocusable(true);
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        return popupWindow;
    }
}
