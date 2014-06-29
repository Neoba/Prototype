/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba;

import com.couchbase.client.CouchbaseClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

/**
 *
 * @author atul
 */
public class Dsyncserver {

    private final int port;
    static CouchbaseClient cclient = null;
    static BidiMap usersessions=new TreeBidiMap();


    public Dsyncserver(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        ArrayList<URI> nodes = new ArrayList<URI>();
            //System.setProperty("viewmode", "development");
        // Add one or more nodes of your cluster (exchange the IP with yours)
        nodes.add(URI.create("http://127.0.0.1:8091/pools"));

        try {
            cclient = new CouchbaseClient(nodes, "default", "");
        } catch (Exception e) {
            System.err.println("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
    //cclient.add("idgenerator", 271397319);

        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ReadTimeoutHandler(120));
                            ch.pipeline().addLast(new DsyncserverHandler());
                        }
                    });

            ChannelFuture f = b.bind().sync();
            System.out.println(Dsyncserver.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {

        new Dsyncserver(2810).start();
    }

}
