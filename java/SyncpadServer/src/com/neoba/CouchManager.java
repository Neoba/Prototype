/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import java.util.ArrayList;
import java.util.UUID;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author Atul Vinayak
 */
public class CouchManager {

    public static void set_gcm_rid(String userid, String regid) throws JSONException {
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        user.put("gcm_registration", regid);
        Dsyncserver.cclient.replace(userid, user.toString());
    }

    private static String getGcmRegId(String userid) throws JSONException {
        return new JSONObject((String) Dsyncserver.cclient.get(userid)).getString("gcm_registration");
    }

    public static ArrayList<String> get_gcm_rids(String userid) throws JSONException {
        ArrayList<String> rids = new ArrayList<String>();
        if (Dsyncserver.usersessions.containsValue(userid)) {
            for (UUID k : Dsyncserver.usersessions.keySet()) {
                if (Dsyncserver.usersessions.get(k).equals(userid)) {
                    if (Dsyncserver.useragents.get(k) == 10) {
                        rids.add(getGcmRegId(userid));
                    }
                }
            }
        }
        return rids;
    }

}