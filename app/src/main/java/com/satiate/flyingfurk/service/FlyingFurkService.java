package com.satiate.flyingfurk.service;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Environment;
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
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.satiate.flyingfurk.FurkApplication;
import com.satiate.flyingfurk.R;
import com.satiate.flyingfurk.models.DrumpfQuotes;
import com.satiate.flyingfurk.models.FurkOff;
import com.satiate.flyingfurk.models.Giphy;
import com.satiate.flyingfurk.models.GiphyData;
import com.satiate.flyingfurk.models.GiphyMetadata;
import com.satiate.flyingfurk.models.YesNoMaybeFurk;
import com.satiate.flyingfurk.network.VolleyErrorHelper;
import com.satiate.flyingfurk.utils.Const;
import com.satiate.flyingfurk.utils.FurkUtility;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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
    private FFmpeg fFmpeg;

    private ArrayList<String> furkOffs = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(furkOffs == null)
        {
            populateFurkOffsList();
        }

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

                loadFFMpegBinary();

//                makeYesNoMaybeFurkRequest();
//                makeRandomFurkOffRequest();
//                makeRandomDrumpfQuoteRequest();
                makeRandomGiphyRequest();
//                makeRandomCatJPGRequest();
//                makeRandomCatPNGRequest();
//                makeRandomCatGIFRequest();
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

    private void populateFurkOffsList()
    {
        furkOffs = new ArrayList<>();

        furkOffs.add(Const.FURK_FURKOFF_ASKME_API);
        furkOffs.add(Const.FURK_FURKOFF_AWESOME_API);
        furkOffs.add(Const.FURK_FURKOFF_BACKOFF_API);
        furkOffs.add(Const.FURK_FURKOFF_BASICFU_API);
        furkOffs.add(Const.FURK_FURKOFF_BYE_API);
        furkOffs.add(Const.FURK_FURKOFF_CHAINSAWGENTLE_API);
        furkOffs.add(Const.FURK_FURKOFF_CHOKE_API);
        furkOffs.add(Const.FURK_FURKOFF_COMEINORFOFF_API);
        furkOffs.add(Const.FURK_FURKOFF_COOL_API);
        furkOffs.add(Const.FURK_FURKOFF_CRAP_API);
        furkOffs.add(Const.FURK_FURKOFF_DIABETES_API);
        furkOffs.add(Const.FURK_FURKOFF_DIKBAG_API);
        furkOffs.add(Const.FURK_FURKOFF_DOUGHNUT_API);
        furkOffs.add(Const.FURK_FURKOFF_EMPTYHEAD_API);
        furkOffs.add(Const.FURK_FURKOFF_EVERYONEFOFF_API);
        furkOffs.add(Const.FURK_FURKOFF_FASCINATINGSHUTUP_API);
        furkOffs.add(Const.FURK_FURKOFF_FEVERYTHING_API);
        furkOffs.add(Const.FURK_FURKOFF_FFAMILY_API);
        furkOffs.add(Const.FURK_FURKOFF_FLYING_API);
        furkOffs.add(Const.FURK_FURKOFF_FSAKE_API);
        furkOffs.add(Const.FURK_FURKOFF_FTHAT_API);
        furkOffs.add(Const.FURK_FURKOFF_FTHIS_API);
        furkOffs.add(Const.FURK_FURKOFF_FTHISSHITPARTICULAR_API);
        furkOffs.add(Const.FURK_FURKOFF_FTS_API);
        furkOffs.add(Const.FURK_FURKOFF_GIVEZERO_API);
        furkOffs.add(Const.FURK_FURKOFF_HERO_API);
        furkOffs.add(Const.FURK_FURKOFF_HORSE_API);
        furkOffs.add(Const.FURK_FURKOFF_KEEPCALM_API);
        furkOffs.add(Const.FURK_FURKOFF_KINGRAGE_API);
        furkOffs.add(Const.FURK_FURKOFF_LOOKINGFORFURK_API);
        furkOffs.add(Const.FURK_FURKOFF_MAYBE_API);
        furkOffs.add(Const.FURK_FURKOFF_ME_API);
        furkOffs.add(Const.FURK_FURKOFF_MEGA_API);
        furkOffs.add(Const.FURK_FURKOFF_MORNING_API);
        furkOffs.add(Const.FURK_FURKOFF_MYLIFE_API);
        furkOffs.add(Const.FURK_FURKOFF_NO_API);
        furkOffs.add(Const.FURK_FURKOFF_NOFURKLOOK_API);
        furkOffs.add(Const.FURK_FURKOFF_NOTASINGLEF_API);
        furkOffs.add(Const.FURK_FURKOFF_NUGGET_API);
        furkOffs.add(Const.FURK_FURKOFF_OFF_API);
        furkOffs.add(Const.FURK_FURKOFF_PROBLEMDUDE_API);
        furkOffs.add(Const.FURK_FURKOFF_RAGE_API);
        furkOffs.add(Const.FURK_FURKOFF_READMANUAL_API);
        furkOffs.add(Const.FURK_FURKOFF_RETARD_API);
        furkOffs.add(Const.FURK_FURKOFF_RIDICULOUS_API);
        furkOffs.add(Const.FURK_FURKOFF_SHAKESPEAREF_API);
        furkOffs.add(Const.FURK_FURKOFF_SHUTUP_API);
        furkOffs.add(Const.FURK_FURKOFF_SPLAT_API);
        furkOffs.add(Const.FURK_FURKOFF_THANK_API);
        furkOffs.add(Const.FURK_FURKOFF_THINKING_API);
        furkOffs.add(Const.FURK_FURKOFF_THUMBS_API);
        furkOffs.add(Const.FURK_FURKOFF_TOO_API);
        furkOffs.add(Const.FURK_FURKOFF_UTHINKIGIVEAF_API);
        furkOffs.add(Const.FURK_FURKOFF_WHATTF_API);
        furkOffs.add(Const.FURK_FURKOFF_WHYBECAUSE_API);
        furkOffs.add(Const.FURK_FURKOFF_XMASF_API);
        furkOffs.add(Const.FURK_FURKOFF_ZERO_API);
    }

    private void makeYesNoMaybeFurkRequest()
    {
        makeJsonObjectRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_YESNOMAYBE_API,
                Const.FURK_YESNOMAYBE_API_TAG, new HashMap<String, String>(), null);
    }

    private void makeRandomFurkOffRequest()
    {
        String randomFurk = furkOffs.get(new Random().nextInt(furkOffs.size()));
        Log.d(Const.TAG, "random furk off is: "+randomFurk);

        HashMap<String,String> headers = new HashMap<>();
        headers.put("Accept", "application/json");

        makeJsonObjectRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_FURKOFF_BASE_API+randomFurk,
                Const.FURK_FURKOFF_TAG, headers, null);
    }

    private void makeRandomDrumpfQuoteRequest()
    {
        makeJsonObjectRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_DRUMPF_BASE_API+Const.FURK_DRUMPF_RANDOM_API,
                Const.FURK_DRUMPF_TAG, new HashMap<String, String>(), null);
    }

    private void makeRandomGiphyRequest()
    {
        HashMap<String,String> headers = new HashMap<>();
        headers.put("api_key", Const.GIPHY_KEY);

        makeJsonObjectRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_GIPHY_BASE_API+Const.FURK_GIPHY_RANDOM_API,
                Const.FURK_GIPHY_TAG, headers, null);
    }


    private void makeRandomCatJPGRequest()
    {
        makeStringRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_CAT_RANDOM_JPG_API,
                Const.FURK_CAT_JPG_TAG, new HashMap<String, String>());
    }

    private void makeRandomCatPNGRequest()
    {
        makeStringRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_CAT_RANDOM_PNG_API,
                Const.FURK_CAT_PNG_TAG, new HashMap<String, String>());
    }

    private void makeRandomCatGIFRequest()
    {
        makeStringRequest(FlyingFurkService.this, Request.Method.GET, Const.FURK_CAT_RANDOM_PNG_API,
                Const.FURK_CAT_GIF_TAG, new HashMap<String, String>());
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
                                      final HashMap<String, String> headers, JSONObject jsonObject)
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

                                        sendGifFurk(yesNoMaybeFurk.getImage());
                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case Const.FURK_FURKOFF_TAG:
                                    try {
                                        FurkOff furkOff = new FurkOff();

                                        if (response.has(Const.FURK_FURKOFF_MESSAGE) &&
                                                response.getString(Const.FURK_FURKOFF_MESSAGE) != null)
                                        {
                                            furkOff.setMessage(response.getString(Const.FURK_FURKOFF_MESSAGE));
                                        }

                                        if(furkOff.getMessage() != null && !furkOff.getMessage().trim().equalsIgnoreCase(""))
                                        {
                                            sendTextFurk(furkOff.getMessage());
                                        }
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                    break;
                                case Const.FURK_DRUMPF_TAG:
                                    try {
                                        DrumpfQuotes drumpfQuotes = new DrumpfQuotes();

                                        if (response.has(Const.FURK_DRUMPF_MESSAGE) &&
                                                response.getString(Const.FURK_DRUMPF_MESSAGE) != null)
                                        {
                                            drumpfQuotes.setMessage(response.getString(Const.FURK_DRUMPF_MESSAGE));
                                        }


                                        if(drumpfQuotes.getMessage() != null && !drumpfQuotes.getMessage().trim().equalsIgnoreCase(""))
                                        {
                                            sendTextFurk(drumpfQuotes.getMessage()+" ~Drumpf");
                                        }

                                    }catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                case Const.FURK_GIPHY_TAG:
                                    try {
                                        Giphy giphy = new Giphy();
                                        GiphyMetadata meta = new GiphyMetadata();
                                        GiphyData data = new GiphyData();

                                        if(response.has(Const.FURK_GIPHY_METADATA) && response.getJSONObject(Const.FURK_GIPHY_METADATA) != null
                                                && response.has(Const.FURK_GIPHY_DATA) && response.getJSONObject(Const.FURK_GIPHY_DATA) != null)
                                        {
                                            JSONObject metaJsonObject = response.getJSONObject(Const.FURK_GIPHY_METADATA);
                                            JSONObject dataJsonObject = response.getJSONObject(Const.FURK_GIPHY_DATA);

                                            if(metaJsonObject.has(Const.FURK_GIPHY_METADATA_STATUS) && metaJsonObject.
                                                    getInt(Const.FURK_GIPHY_METADATA_STATUS) == 200)
                                            {
                                                if(dataJsonObject.has(Const.FURK_GIPHY_DATA_IMAGE_URL) && dataJsonObject.
                                                        getString(Const.FURK_GIPHY_DATA_IMAGE_URL) != null)
                                                {
                                                    data.setImage_url(dataJsonObject.getString(Const.FURK_GIPHY_DATA_IMAGE_URL));
                                                }

                                                giphy.setData(data);

                                                sendGifFurk(data.getImage_url());
                                            }
                                        }

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
                    error.printStackTrace();
                    error = null;
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    return headers;
                }
            };

            jsonObjReq.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 0f));

            // Adding request to request queue
            FurkApplication.getInstance().addToRequestQueue(jsonObjReq, tag);

        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void makeStringRequest(final Context context, int method, final String reqUrl, final String tag,
                                  final HashMap<String, String> headers)
    {
        StringRequest request = new StringRequest(method, reqUrl,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {

                        JSONObject responseObject = FurkUtility.convertXmlToJson(response);
                        Log.d(Const.TAG, responseObject.toString());

                        switch (tag) {
                            case Const.FURK_CAT_JPG_TAG:
                                try {
                                    String url = getCatResponseUrl(responseObject);
                                    if(url != null)
                                    {
                                        sendImageFurk(url);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            case Const.FURK_CAT_PNG_TAG:
                                try {
                                    String url = getCatResponseUrl(responseObject);
                                    if(url != null)
                                    {
                                        sendImageFurk(url);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                            case Const.FURK_CAT_GIF_TAG:
                                try {
                                    String url = getCatResponseUrl(responseObject);
                                    if(url != null)
                                    {
                                        sendGifFurk(url);
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                break;
                        }
                    }

                    private String getCatResponseUrl(JSONObject responseObject) {
                        String url = null;

                        try {
                            if (responseObject.has(Const.FURK_CAT_RESPONSE) && responseObject.get(Const.FURK_CAT_RESPONSE) != null)
                            {
                                JSONObject respObject = responseObject.getJSONObject(Const.FURK_CAT_RESPONSE);
                                JSONObject imageObject = respObject.getJSONObject(Const.FURK_CAT_DATA).getJSONObject(Const.FURK_CAT_IMAGES).
                                        getJSONObject(Const.FURK_CAT_IMAGE);
                                url = imageObject.getString(Const.FURK_CAT_URL);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        return url;
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // handle error response
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return headers;
            }

            @Override
            protected Map<String, String> getParams() {
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(5000, 0, 0f));

        FurkApplication.getInstance().addToRequestQueue(request, tag);
    }

    private void sendTextFurk(String furk)
    {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        sendIntent.setPackage("com.whatsapp");
        sendIntent.putExtra(Intent.EXTRA_TEXT, furk);
        sendIntent.setType("text/plain");
        FlyingFurkService.this.startActivity(sendIntent);
    }

    private void sendImageFurk(final String imageUrl) {

        Glide.with(FlyingFurkService.this).load(imageUrl).asBitmap().listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

                try {
                    File file = new File(getApplicationContext().getCacheDir(), "test" + ".gif");

                    FileOutputStream fOut = new FileOutputStream(file);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resource.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    fOut.write(stream.toByteArray());
                    fOut.close();

                    file.setReadable(true, false);

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.setPackage("com.whatsapp");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    sendIntent.setType("image/jpeg");
                    FlyingFurkService.this.startActivity(sendIntent);
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        }).into(200, 200);

    }

    private void sendGifFurk(final String gifFurk) {

        Glide.with(FlyingFurkService.this).load(gifFurk).asGif().listener(new RequestListener<String, GifDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                e.printStackTrace();
                return false;
            }

            @Override
            public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                try {
                    File file = new File(getApplicationContext().getCacheDir(), "test" + ".gif");

                    FileOutputStream fOut = new FileOutputStream(file);
                    fOut.write(resource.getData());
                    fOut.close();

                    file.setReadable(true, false);

                   /* Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.setPackage("com.facebook.orca");
                    sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                    sendIntent.setType("image/gif");
                    FlyingFurkService.this.startActivity(sendIntent);*/
                    convertGifToVideo(file.getPath());
                }catch (Exception e){
                    e.printStackTrace();
                }

                return false;
            }
        }).into(200, 200);

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

    public void convertGifToVideo(String path)
    {
        /*String[] command = new String[]{"/system/bin/chmod", "777",
                "/data/data/com.satiate.flyingfurk/files/ffmpeg" };
        try {
            Runtime.getRuntime().exec(command);
            Log.d(Const.TAG, "command run success");
        } catch (IOException e) {
            e.printStackTrace();
        }*/


        try {
//            final String videoPath = path.replace(".gif",".mp4");
            final String videoPath = Environment.getExternalStorageDirectory()+"/test.mp4";
//            String[] cmd = {"-f", "gif", "-i", "-c:v", "libx264", "-pix", "_fmt", "yuv420p", "-movflags","+faststart",path,videoPath};
            String[] cmd = {"-i", path, "-y", "-movflags", "faststart", "-pix_fmt", "yuv420p", "-vf",
                    "scale=trunc(iw/2)*2:trunc(ih/2)*2", videoPath};

            Log.d(Const.TAG, "video output path is: "+videoPath);

            fFmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Log.d(Const.TAG, "video conversion started");
                }

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {
                    Log.d(Const.TAG, "failed to convert video "+message);
                }

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {
                    try {
                        Log.d(Const.TAG, "file converted to mp4");

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sendIntent.setPackage("com.whatsapp");
                        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(videoPath));
                        sendIntent.setType("video/mp4");
                        FlyingFurkService.this.startActivity(sendIntent);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }

    private void loadFFMpegBinary() {
        try {
            fFmpeg= FFmpeg.getInstance(FlyingFurkService.this);
            fFmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    Log.d(Const.TAG, "failed to load");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            Log.d(Const.TAG, "failed to load");
            e.printStackTrace();
        }
    }

}
