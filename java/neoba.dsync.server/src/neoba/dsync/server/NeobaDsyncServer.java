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
    private Charset charset = Charset.forName("UTF-8");
    SocketChannel sc;

    public void init() throws Exception {
        selector = Selector.open();
        ServerSocketChannel server = ServerSocketChannel.open();
        InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 3000);
        server.socket().bind(isa);
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        while (selector.select() > 0) {
            for (SelectionKey key : selector.selectedKeys()) {
                selector.selectedKeys().remove(key);
                if (key.isAcceptable()) {
                    sc = server.accept();
                    sc.configureBlocking(false);
                    sc.register(selector, SelectionKey.OP_READ);
                    key.interestOps(SelectionKey.OP_ACCEPT);
                }
                if (key.isReadable()) {
                    byte[] delta = null;
                    sc = (SocketChannel) key.channel();
                    ByteBuffer buff = ByteBuffer.allocate(9000);
                    String content = "";
                    try {
                        int count = 0, size = 0;
                        while ((size = sc.read(buff)) > 0) {
                            count += size;
                            buff.flip();
                        }
                        delta = new byte[count];
                        for (int i = 0; i < count; i++) {
                            delta[i] = buff.get(i);
                        }
                        System.out.println("===== " + new Openvcdiffjava().vcdiffDecode("", delta));
                        printhex(delta);
                        key.interestOps(SelectionKey.OP_READ);
                    } catch (IOException e) {
                        key.cancel();
                        if (key.channel() != null) {
                            key.channel().close();
                        }
                    }
                    if (delta.length > 0) {
                        for (SelectionKey sk : selector.keys()) {
                            Channel targetchannel = sk.channel();
                            if (targetchannel instanceof SocketChannel) {//  && targetchannel!=sc) {
                                SocketChannel dest = (SocketChannel) targetchannel;

                                dest.write(ByteBuffer.wrap(delta));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void printhex(byte[] b) {
        int rem = b.length;
        String outs;
        int ran=0;
        for (int i = 0; i < ((b.length / 10) + 1); i++) {
            for (int j = 0; (j < rem && j < 10); j++) {
                outs=Integer.toHexString(b[j + b.length - rem] >= 0 ? b[j + b.length - rem] : -1 * b[j + b.length - rem]) + " ";
                if( b[j + b.length - rem]==0)outs="00 ";
                System.out.print(outs.length()<=2?"0"+outs:outs);
                ran++;
            }
            if(ran<3)System.out.print("\t\t\t");
            if(ran<6)System.out.print("\t\t");
            //if(ran<7)Syst)System.ouem.out.print("\t");
            System.out.print("\t");
            ran=0;
            for (int j = 0; (j < rem && j < 10); j++) {
                System.out.print((char) (b[j + b.length - rem] >= 'a' && b[j + b.length - rem] <= 'z' ? b[j + b.length - rem] : '.') + " ");
            }
            rem -= 10;
            
            System.out.println("");
            
            
        }

    }

    public static void main(String[] args) throws Exception {
        new NeobaDsyncServer().init();
    }
}
