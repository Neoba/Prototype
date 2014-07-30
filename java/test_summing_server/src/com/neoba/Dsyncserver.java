package com.neoba;

import com.couchbase.client.CouchbaseClient;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
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
                            ch.pipeline().addLast("httpDecoder", new HttpRequestDecoder());
                            ch.pipeline().addLast("httpAggregator",new HttpObjectAggregator(Constants.HTTP_MAX_BODY_SIZE));
                            ch.pipeline().addLast("httpEncoder", new HttpResponseEncoder());
                            ch.pipeline().addLast("handler", new DsyncserverHandler());
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

        new Dsyncserver(2811).start();
    }

}
