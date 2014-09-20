/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import com.couchbase.client.protocol.views.Query;
import com.couchbase.client.protocol.views.Stale;
import com.couchbase.client.protocol.views.View;
import com.couchbase.client.protocol.views.ViewResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author root
 */
public class FacebookUser {

    private JSONObject facebook_obj;
    private String name, id, email;
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

    public String getEmail() {
        return email;
    }

    public FacebookUser(String access_token) throws IOException, MalformedURLException, JSONException {
        JSONObject facebook_obj = HTTPUtils.json_get("https://graph.facebook.com/v2.1/me?fields=id,name,email,friends&access_token=" + access_token);
        id = facebook_obj.getString("id");
        name = facebook_obj.getString("name");
        email = facebook_obj.getString("email");
        friends = new JSONArray();
        JSONArray friendspage = facebook_obj.getJSONObject("friends").getJSONArray("data");
        do {
            for (int i = 0; i < friendspage.length(); i++) {
                JSONObject temp = friendspage.getJSONObject(i);
                JSONObject extra = fbid_to_id(temp.getString("id"));
                if (extra != null) {
                    temp.put("username", extra.get("username"));
                    temp.put("sid", extra.get("id"));
                }
                friends.put(temp);
            }
            String url = facebook_obj.getJSONObject("friends").getJSONObject("paging").getString("next");
            friendspage = HTTPUtils.json_get(url).getJSONArray("data");

        } while (friendspage.length() != 0);
    }

    private JSONObject fbid_to_id(String fbid) throws JSONException {
        View view = Dsyncserver.cclient.getView("dev_neoba", "userstoid");
        Query query = new Query();
        query.setKey(fbid);
        query.setStale(Stale.FALSE);
        ViewResponse result = Dsyncserver.cclient.query(view, query);
        if (result != null) {
            JSONObject ob = new JSONObject(result.iterator().next().getValue());
            return ob;
        }
        return null;
    }

}
