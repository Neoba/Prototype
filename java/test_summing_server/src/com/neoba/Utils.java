/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;

/**
 *
 * @author atul
 */
public class Utils {

    static Logger logger = Logger.getLogger(Utils.class);

    public static void printhex(String title,byte[] b, int count) {
        int rem = count;
        String outs;
        int ran = 0;
        logger.debug("buffer : "+title);
        logger.debug("size : "+count+" bytes");
        StringBuilder sb ;
        for (int i = 0; i < ((count / 10) + 1); i++) {
            sb = new StringBuilder();
            for (int j = 0; (j < rem && j < 10); j++) {
                outs = String.format("%02X ", b[j + count - rem] & 0xff);
                sb.append(outs);
                ran++;
            }
            for (int j = 0; j < 29 - (ran * 2 + ran - 1); j++) {
                sb.append(" ");
            }
            ran = 0;
            for (int j = 0; (j < rem && j < 10); j++) {
                sb.append((char) (b[j + count - rem] >= 30 && b[j + count - rem] <= 127 ? b[j + count - rem] : '.') + " ");
            }
            rem -= 10;
            logger.debug(sb.toString());

        }
    }
    

}
