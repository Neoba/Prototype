/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba.dsync.server;

import com.neoba.dsync.vcdiff.Vcdiff;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Atul Vinayak
 */
public class ServerMain extends Thread {

    private ServerSocket serverSocket;

    private String dictionary = "";
    private HashMap deltaHistory=new HashMap();

    public ServerMain(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public String getHash(String dictionary) throws NoSuchAlgorithmException {
        MessageDigest hash = MessageDigest.getInstance("MD5");
        hash.reset();
        hash.update(dictionary.getBytes());
        byte[] digest = hash.digest();
        BigInteger bigInt = new BigInteger(1, digest);
        String hashtext = bigInt.toString();
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext.substring(2, 12);
    }

    @Override
    public void run() {
        while (true) {
            try {
                Vcdiff v;
                Boolean f = false;
                v = new Vcdiff();
                v.blockSize = 3;
                Socket socket = serverSocket.accept();
                List<Object> delta = new ArrayList<>();
                try {
                    ObjectInputStream objectInput = new ObjectInputStream(socket.getInputStream());
                    Object ob = objectInput.readObject();
                    if (ob.getClass() == String.class) {
                        if (ob.equals("get_dictionary")) {
                            f = true;
                        }
                    } else {
                        delta = (List<Object>) ob;
                        //System.out.println(delta);
                        if (delta.isEmpty()) {
                            dictionary = "";
                            
                        } else if (delta.size() == 1) {
                            deltaHistory.clear();
                            dictionary = (String) delta.get(0);
                            deltaHistory.put(getHash(dictionary), delta);
                        } else {
                            dictionary = v.decode(dictionary, delta);
                            deltaHistory.put(getHash(dictionary), delta);
                            //System.out.println(dictionary);
                        }
                    }
                    System.out.println(deltaHistory);
                    //String hashtext=getHash(dictionary);
                    //System.out.println("entity version: " + hashtext);
                    ObjectOutputStream objectOutput = new ObjectOutputStream(socket.getOutputStream());
                    if (f) {
                        objectOutput.writeObject(dictionary);
                        System.out.println("Updated a client");
                    } else {
                        objectOutput.writeObject("Recived_delta");
                    }
                } catch (IOException e) {
                } catch (        ClassNotFoundException | NoSuchAlgorithmException ex) {
                    Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException e) {
            }
        }
    }

    public static void main(String[] args) {
        int port = 1234;
        try {
            Thread t = new ServerMain(port);
            t.start();
        } catch (IOException e) {
        }
    }
}
