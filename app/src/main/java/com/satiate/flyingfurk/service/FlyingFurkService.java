package com.satiate.flyingfurk.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.satiate.flyingfurk.FurkApplication;
import com.satiate.flyingfurk.R;
import com.satiate.flyingfurk.models.YesNoMaybeFurk;
import com.satiate.flyingfurk.network.VolleyErrorHelper;
import com.satiate.flyingfurk.utils.Const;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

                makeFurkRequest();
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

    private void makeFurkRequest()
    {
        makeJsonObjectRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_YESNOMAYBE_API,
                Const.FURK_YESNOMAYBE_API_TAG, null, null);
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

    public void makeJsonObjectRequest(final Context context, int method, final String reqUrl, final String tag,
                                      final HashMap<String, String> params, JSONObject jsonObject)
    {
        try {

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(method,
                    reqUrl, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d(Const.TAG, "got response: "+response.toString());

                            switch (tag)
                            {
                                case Const.FURK_YESNOMAYBE_API_TAG:
                                    YesNoMaybeFurk yesNoMaybeFurk = new YesNoMaybeFurk();
                                    try {
                                        if(response.has(Const.FURK_YESNOMAYBE_ANSWER) && response.getString(Const.FURK_YESNOMAYBE_ANSWER) != null)
                                        {
                                            yesNoMaybeFurk.setAnswer(response.getString(Const.FURK_YESNOMAYBE_ANSWER));
                                        }

                                        if(response.has(Const.FURK_YESNOMAYBE_FORCED))
                                        {
                                            yesNoMaybeFurk.setForced(response.getBoolean(Const.FURK_YESNOMAYBE_FORCED));
                                        }

                                        if(response.has(Const.FURK_YESNOMAYBE_IMAGE) &&
                                                response.getString(Const.FURK_YESNOMAYBE_IMAGE) != null)
                                        {
                                            yesNoMaybeFurk.setImage(response.getString(Const.FURK_YESNOMAYBE_IMAGE));
                                        }

                                        sendMessage(yesNoMaybeFurk);
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                            }
                            response = null;
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(Const.TAG, "Error: " + VolleyErrorHelper.getMessage(error, context));
                    error = null;
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headerMap = new HashMap<>();
                    return headerMap;
                }

                @Override
                protected Map<String, String> getParams() {
                    return params;
                }
            };

//                jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(2000, 0, 0f));

            // Adding request to request queue
            FurkApplication.getInstance().addToRequestQueue(jsonObjReq, tag);

        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    private void sendMessage(final YesNoMaybeFurk yesNoMaybeFurk) {
//                Uri uri = Uri.parse("https://static.pexels.com/photos/87646/horsehead-nebula-dark-nebula-constellation-orion-87646.jpeg");

        Glide.with(FlyingFurkService.this).load(yesNoMaybeFurk.getImage()).asGif().listener(new RequestListener<String, GifDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                e.printStackTrace();
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Log.d(Const.TAG, "we got gif");

                try {
                    File file = new File(getApplicationContext().getCacheDir(), "test" + ".gif");

                    FileOutputStream fOut = new FileOutputStream(file);
                    fOut.write(resource.getData());
                    fOut.close();

                    file.setReadable(true, false);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.setPackage("com.facebook.orca");
                    //TODO text doesn't work
                    sendIntent.putExtra(Intent.EXTRA_TEXT, yesNoMaybeFurk.getAnswer());
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    sendIntent.setType("image/gif");
                    FlyingFurkService.this.startActivity(sendIntent);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        }).into(200, 200);

       /* try {
            File file = new File(getApplicationContext().getCacheDir(), "test" + ".gif");

            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write();
            fOut.close();

            file.setReadable(true, false);

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendIntent.setPackage("com.facebook.orca");
            sendIntent.putExtra(Intent.EXTRA_TEXT, yesNoMaybeFurk.getAnswer());
            sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
            sendIntent.setType("image/jpeg");
            FlyingFurkService.this.startActivity(sendIntent);
        }catch (Exception e){
            e.printStackTrace();
        }*/
    }

    private String saveToInternalStorage(Bitmap bitmapImage)
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

}
