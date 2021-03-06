/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba.models;

import com.neoba.utils.HTTPUtils;
import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import com.neoba.Dsyncserver;
import java.io.IOException;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author root
 */
public class FacebookUser {

    private JSONObject facebook_obj;
    private String name, id;
    private JSONArray friends;

    public String getName() {
        return name;
    }

    public JSONArray getFriends() {
        return friends;
    }

    public String getId() {
        return id;
    }


    Logger logger=Logger.getLogger(FacebookUser.class);
    public FacebookUser(String access_token) throws IOException, MalformedURLException, JSONException {
        
        JSONObject facebook_obj =null;
        while(facebook_obj==null)
        {
            logger.info("Trying to connect to facebook..");
            facebook_obj=HTTPUtils.json_get("https://graph.facebook.com/v2.1/me?fields=id,name,email,friends&access_token=" + access_token);
        }
        logger.info("fbtoid access token "+access_token);
        id = facebook_obj.getString("id");
        name = facebook_obj.getString("name");
        //email = facebook_obj.getString("email");
        friends = new JSONArray();
        String url="";
        try{
            JSONArray friendspage = facebook_obj.getJSONObject("friends").getJSONArray("data");
            do {
            for (int i = 0; i < friendspage.length(); i++) {
                JSONObject temp = friendspage.getJSONObject(i);
                logger.info("fbtoid "+temp.getString("id"));
                JSONObject extra = fbid_to_id(temp.getString("id"));
                if (extra != null) {
                    temp.put("username", extra.get("username"));
                    temp.put("sid", extra.get("id"));    
                }
                friends.put(temp);
            }
            if(friendspage.length()!=0)
            {
                url = facebook_obj.getJSONObject("friends").getJSONObject("paging").getString("next");
                friendspage = HTTPUtils.json_get(url).getJSONArray("data");
            }

        } while (friendspage.length() != 0);
        }catch(Exception e){
            logger.error("Frineds unavailable in response!");
        }
        
    }

    private JSONObject fbid_to_id(String fbid) throws JSONException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "facebookidtoid");
        Query query = new Query();
        query.setKey("\""+fbid+"\"");
        query.setStale(Stale.FALSE);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        if (result.size()!=0) {
            JSONObject ob = new JSONObject(result.iterator().next().getValue());
            return ob;
        }
        return null;
    }
    
    public String getSyncpadId() throws JSONException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "facebookidtoid");
        Query query = new Query();
        query.setKey("\""+this.id+"\"");
        query.setStale(Stale.FALSE);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        if (result.size()!=0) {
            JSONObject ob = new JSONObject(result.iterator().next().getValue());
            return ob.getString("id");
        }
        return null;
    }

}
