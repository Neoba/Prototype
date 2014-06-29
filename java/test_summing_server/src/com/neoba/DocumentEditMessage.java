/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import io.netty.buffer.ByteBuf;
import static io.netty.buffer.Unpooled.buffer;
import java.io.IOException;
import java.util.UUID;
import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author atul
 */
class DocumentEditMessage implements Message{

    public DocumentEditMessage(UUID doc, byte[] diff) throws JSONException, IOException, VcdiffDecodeException, VcdiffEncodeException {
        JSONObject json = new JSONObject((String) Dsyncserver.cclient.get(doc.toString()));
        JSONArray diffarray = null;

        System.out.println("diff: ");
        Utils.printhex(diff, diff.length);

        int age = (int) json.get("age");
        age += 1;
        String dict = json.getString("dict");
        if (age >= 5) {
            age = 0;

            dict = new VcdiffDecoder(dict, diff).decode();
            json.put("dict", dict);
            System.out.println("ok--shiftted " + dict + "\nnew diff:");
            diff = new VcdiffEncoder(dict, dict).encode();
            Utils.printhex(diff, diff.length);

        }
        diffarray = new JSONArray();
        for (byte b : diff) {
            diffarray.put(b);
        }

        json.put("diff", diffarray);

        System.out.println("Did you mean: " + new VcdiffDecoder(dict, diff).decode());

        json.put("age", age);
        Dsyncserver.cclient.replace(doc.toString(), json.toString());

    }

    @Override
    public ByteBuf result() {
        ByteBuf reply = buffer(2 + 4);
        reply.writeByte(Constants.VERSION);
        reply.writeByte(Constants.DOCUMENT_EDIT);
        reply.writeInt(0xFFFF);
        return reply;
    }

}
