package com.satiate.flyingfurk.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.satiate.flyingfurk.FurkApplication;
import com.satiate.flyingfurk.utils.Const;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rishabh Bhatia on 29/10/2016.
 */
public class VolleyUtils {

    public static void makeJsonObjectRequest(final Context context, int method, final String reqUrl, final String tag, final HashMap<String,String> params
            , JSONObject jsonObject)
    {
        try {

            JsonObjectRequest jsonObjReq = new JsonObjectRequest(method,
                    reqUrl, jsonObject,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            Log.d(Const.TAG, "got response: "+response.toString());
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

}
