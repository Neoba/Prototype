/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neoba.dsync.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import openvcdiffjava.Openvcdiffjava;

public class NeobaDsyncServer {

    private Selector selector;
    private byte age = 4;
    private final byte ROLL_FWD_COUNT = 5;
    private final Charset charset = Charset.forName("UTF-8");
    private final int BUFF_SIZE = 10000;
    SocketChannel sc;
    byte[] delta = null;
    String dict = " ";

    public void init() throws Exception {
        selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 3000);
        server.socket().bind(isa);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            int count = 0, size = 0;
            for (SelectionKey key : selector.selectedKeys()) {
                selector.selectedKeys().remove(key);
                if (key.isAcceptable()) {
                    sc = server.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    key.interestOps(SelectionKey.OP_ACCEPT);

                    //sc.write(ByteBuffer.wrap(new byte[] {age}));
                    ByteBuffer welcome = null;

                    if (delta != null) {
                        welcome = ByteBuffer.allocate(9 + dict.length() + delta.length);
                        welcome.put(age);
                        welcome.putInt(delta.length);
                        welcome.put(delta);

                    } else {
                        welcome = ByteBuffer.allocate(9 + dict.length());
                        welcome.put(age);
                        welcome.putInt(0);

                    }
                    welcome.putInt(dict.length());
                    welcome.put(charset.encode(dict));
                    welcome.flip();

                    sc.write(welcome);
                }
                if (key.isReadable()) {
                    sc = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(BUFF_SIZE);
                    String content = "";
                    try {

                        while ((size = sc.read(buff)) > 0) {
                            count += size;
                            buff.flip();
                        }
                        if (count > 0) {
                            delta = new byte[count];
                            for (int i = 0; i < count; i++) {
                                delta[i] = buff.get(i);
                            }
                            content = new Openvcdiffjava().vcdiffDecode(dict, delta);
                            System.out.println("- " + content);
                            printhex(delta);
                            key.interestOps(SelectionKey.OP_READ);
                        }
                    } catch (IOException e) {
                        key.cancel();
                        if (key.channel() != null) {
                            key.channel().close();
                        }
                    }
                    if (count > 0 && delta.length > 0) {

                        for (SelectionKey sk : selector.keys()) {
                            Channel targetchannel = sk.channel();
                            if (targetchannel instanceof SocketChannel) {// && targetchannel!=sc) {
                                SocketChannel dest = (SocketChannel) targetchannel;

                                dest.write(ByteBuffer.wrap(delta));
                            }
                        }
                        age += 1;
                        if (age > ROLL_FWD_COUNT) {
                            dict = content;
                            age = 0;
                            delta = null;
                        }
                    }
                }
            }
        }
    }

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

    public static void main(String[] args) throws Exception {
        new NeobaDsyncServer().init();
    }
}
