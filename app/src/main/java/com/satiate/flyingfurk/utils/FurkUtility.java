package com.satiate.flyingfurk.utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.util.Calendar;

/**
 * Created by Rishabh Bhatia on 29/10/16.
 */

public class FurkUtility {

    public static JSONObject convertXmlToJson(String xml)
    {
        JSONObject jsonResult = new JSONObject();
        try {
            jsonResult = XML.toJSONObject(xml);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonResult;
    }

    public static long getCurrentTimestamp()
    {
        return Calendar.getInstance().getTime().getTime();
    }
}
