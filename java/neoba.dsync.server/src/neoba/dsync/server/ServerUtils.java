/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neoba.dsync.server;

/**
 *
 * @author Atul Vinayak
 */
public class ServerUtils {
        public static void printhex(byte[] b) {
        int rem = b.length;
        String outs;
        int ran = 0;
        System.out.println(b.length + "B dump");
        for (int i = 0; i < ((b.length / 10) + 1); i++) {
            for (int j = 0; (j < rem && j < 10); j++) {
                outs = String.format("%02X ", b[j + b.length - rem] & 0xff);

                System.out.print(outs);
                ran++;
            }
            for (int j = 0; j < 29 - (ran * 2 + ran - 1); j++) {
                System.out.print(" ");
            }
            ran = 0;
            for (int j = 0; (j < rem && j < 10); j++) {
                System.out.print((char) (b[j + b.length - rem] >= 30 && b[j + b.length - rem] <= 127 ? b[j + b.length - rem] : '.') + " ");
            }
            rem -= 10;

            System.out.println("");

        }

    }
}
