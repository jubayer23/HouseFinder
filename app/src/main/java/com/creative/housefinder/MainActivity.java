package com.creative.housefinder;

import android.*;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.creative.housefinder.Utility.DeviceInfoUtils;
import com.creative.housefinder.Utility.GpsEnableTool;
import com.creative.housefinder.Utility.LastLocationOnly;
import com.creative.housefinder.Utility.RunnTimePermissions;
import com.creative.housefinder.alertbanner.AlertDialogForAnything;
import com.creative.housefinder.fragment.HouseListFragment;

public class MainActivity extends BaseActivity {

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
            int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
            int result2 = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            if (result == PackageManager.PERMISSION_GRANTED && result2 ==  PackageManager.PERMISSION_GRANTED) {
                houseListFragment = new HouseListFragment();
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.content_layout, houseListFragment, TAG_HOUSE_LIST_FRAGMENT)
                        .commit();
            }
        }
    }
}
