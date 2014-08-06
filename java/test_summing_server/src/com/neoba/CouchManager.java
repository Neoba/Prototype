/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Atul Vinayak
 */
public class CouchManager {
    public static void setGcmRegId(String userid,String regid) throws JSONException{
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        user.put("gcm_registration",regid);
        Dsyncserver.cclient.replace(userid, user.toString());
    }
    public static String getGcmRegId(String userid) throws JSONException{
        return new JSONObject((String) Dsyncserver.cclient.get(userid)).getString("gcm_registration");
    }

}
