package com.satiate.flyingfurk;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.satiate.flyingfurk.service.FlyingFurkService;

public class HomeFurkActivity extends AppCompatActivity {


    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_PERMISSION = 129;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkFurkPermission();
    }

    private void checkFurkPermission()
    {
        String permission[] = { Manifest.permission.SYSTEM_ALERT_WINDOW, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE };

        if(ActivityCompat.checkSelfPermission(HomeFurkActivity.this, Manifest.permission.SYSTEM_ALERT_WINDOW)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(HomeFurkActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(HomeFurkActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(HomeFurkActivity.this, permission, REQUEST_PERMISSION);
        }else
        {
            Log.d("rishabh", "i have permission");
            showFurkingWidget(HomeFurkActivity.this, true);
        }
    }

    @SuppressLint("NewApi")
    private void showFurkingWidget(HomeFurkActivity homeFurkActivity, boolean isShowOverlayPermission) {

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            homeFurkActivity.startService(new Intent(homeFurkActivity, FlyingFurkService.class));
            finish();
            return;
        }

        if (Settings.canDrawOverlays(homeFurkActivity)) {
            homeFurkActivity.startService(new Intent(homeFurkActivity, FlyingFurkService.class));
            finish();
            return;
        }

        if (isShowOverlayPermission) {
            final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + homeFurkActivity.getPackageName()));
            startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                try {
                    checkFurkPermission();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            break;
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }


    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK)
        {
            showFurkingWidget(HomeFurkActivity.this, false);
        }else
        {
            showFurkingWidget(HomeFurkActivity.this, true);
        }
    }
}
