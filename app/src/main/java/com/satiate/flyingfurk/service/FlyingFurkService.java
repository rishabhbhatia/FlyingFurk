package com.satiate.flyingfurk.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.satiate.flyingfurk.R;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

/**
 * Created by Rishabh Bhatia on 27/10/16.
 */

public class FlyingFurkService extends Service implements FloatingViewListener {


    private static final int NOTIFICATION_ID = 9083150;
    private FloatingViewManager mFloatingViewManager;

    private WindowManager windowManager;
    boolean mHasDoubleClicked = false;
    long lastPressTime;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mFloatingViewManager != null) {
            return START_STICKY;
        }


        final LayoutInflater inflater = LayoutInflater.from(this);
        final LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.flying_furk, null, false);
        final ImageView iconView = (ImageView) linearLayout.findViewById(R.id.iv_throw_a_f);
        iconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                sendAFurk();

                iconView.startAnimation(AnimationUtils.loadAnimation(FlyingFurkService.this, R.anim.rotate));
                YoYo.with(Techniques.Shake)
                        .duration(3200)
                        .playOn(iconView);
            }
        });

        mFloatingViewManager = new FloatingViewManager(this, this);
        mFloatingViewManager.setFixedTrashIconImage(R.mipmap.ic_launcher);
        mFloatingViewManager.setDisplayMode(FloatingViewManager.DISPLAY_MODE_SHOW_ALWAYS);
        mFloatingViewManager.setActionTrashIconImage(R.mipmap.ic_launcher);
        final FloatingViewManager.Options options = new FloatingViewManager.Options();
        options.overMargin = (int) (16 * getScreenMetrics(FlyingFurkService.this).density);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);


        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 100;

//        mFloatingViewManager.addViewToWindow(iconView, options);

        windowManager.addView(linearLayout, params);

        View.OnTouchListener touchListener = new View.OnTouchListener() {
            private WindowManager.LayoutParams paramsF = params;
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        // Get current time in nano seconds.
                        long pressTime = System.currentTimeMillis();


                        // If double click...
                        if (pressTime - lastPressTime <= 300) {
                            createNotification();
                            FlyingFurkService.this.stopSelf();
                            mHasDoubleClicked = true;
                        } else {     // If not double click....
                            mHasDoubleClicked = false;
                        }
                        lastPressTime = pressTime;
                        initialX = paramsF.x;
                        initialY = paramsF.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                    case MotionEvent.ACTION_MOVE:
                        paramsF.x = initialX + (int) (event.getRawX() - initialTouchX);
                        paramsF.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(linearLayout, paramsF);
                        break;
                }
                return false;
            }
        };

        linearLayout.setOnTouchListener(touchListener);
        iconView.setOnTouchListener(touchListener);

        startForeground(NOTIFICATION_ID, createNotification());

        return START_REDELIVER_INTENT;
    }

    private Notification createNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis());
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(R.string.app_name));
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MIN);
        builder.setCategory(NotificationCompat.CATEGORY_SERVICE);

        return builder.build();
    }

    private void sendAFurk() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setPackage("com.whatsapp");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Furk off");
        sendIntent.setType("text/plain");
        FlyingFurkService.this.startActivity(sendIntent);
    }

    public DisplayMetrics getScreenMetrics(Context context)
    {
        return context.getResources().getDisplayMetrics();
    }

    @Override
    public void onFinishFloatingView() {
        stopSelf();
    }
}
