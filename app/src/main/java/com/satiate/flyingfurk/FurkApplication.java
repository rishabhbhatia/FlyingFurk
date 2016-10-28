package com.satiate.flyingfurk;

import android.app.Application;
import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.satiate.flyingfurk.utils.LruBitmapCache;

import static com.satiate.flyingfurk.utils.Const.TAG;

/**
 * Created by Rishabh Bhatia on 28/10/16.
 */

public class FurkApplication extends Application {


    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static FurkApplication furkApplication;



    @Override
    public void onCreate() {
        super.onCreate();

        furkApplication = this;
    }

    public static synchronized FurkApplication getInstance() {
        return furkApplication;
    }


    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        getRequestQueue();
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(this.mRequestQueue,
                    new LruBitmapCache());
        }
        return this.mImageLoader;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
