/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewDesign;
import com.couchbase.client.protocol.views.ViewResponse;
import com.couchbase.client.protocol.views.ViewRow;
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

    public static String getUsername(String userid) throws JSONException {
        JSONObject user = new JSONObject((String) Dsyncserver.cclient.get(userid));
        String username = user.getString("username");
        return username;
    }

    public static String getFacebookID(String username) {
        View view = Dsyncserver.cclient.getView("dev_neoba", "usernametofacebookid");
        Query query = new Query();
        query.setKey("\"" + username + "\"");
        query.setIncludeDocs(true);
        query.setStale(Stale.FALSE);

        ViewResponse result = Dsyncserver.cclient.query(view, query);
        for (ViewRow row : result) {
            String id = row.getValue();
            return id;
        }
        return null;

    }
    static boolean ddc=true;
    public static void createView(String viewName, String mapFunction) {
        DesignDocument designDoc;
        if(ddc)
        {
            designDoc = new DesignDocument("dev_neoba");
            ddc=!ddc;
        }
        else
            designDoc=Dsyncserver.cclient.getDesignDoc("dev_neoba");

        ViewDesign viewDesign = new ViewDesign(viewName, mapFunction);
        designDoc.getViews().add(viewDesign);
            Dsyncserver.cclient.createDesignDoc(designDoc);
    }

}
