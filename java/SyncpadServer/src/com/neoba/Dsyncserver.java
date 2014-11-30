package com.neoba;

import com.couchbase.client.CouchbaseClient;
import com.couchbase.client.protocol.views.DesignDocument;
import com.couchbase.client.protocol.views.View;
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
import java.util.HashMap;
import java.util.UUID;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author atul
 */
public class Dsyncserver {

    private final int port;
    public static CouchbaseClient cclient = null;
    public static HashMap<UUID, String> usersessions = new HashMap<UUID, String>();
    public static HashMap<UUID, Byte> useragents = new HashMap<UUID, Byte>();

    Logger logger = Logger.getLogger(Dsyncserver.class);

    public Dsyncserver(int port) {
        this.port = port;
        PropertyConfigurator.configure("logging.properties");
    }

    public void start() throws Exception {
        logger.info("kickstarting server..");
        ArrayList<URI> nodes = new ArrayList<URI>();
        try {
            nodes.add(URI.create("http://127.0.0.1:8091/pools"));
            cclient = new CouchbaseClient(nodes, "default", "");
        } catch (Exception e) {
            logger.fatal("Error connecting to Couchbase: " + e.getMessage());
            System.exit(1);
        }
        
        CouchViews cv = new CouchViews();
        HashMap<String,String> views=cv.getViews();
        for (String view : views.keySet()) {

                CouchManager.createView(view, views.get(view));
                logger.info("created view - " + view);
        }
        //System.out.println(CouchManager.getFacebookID("atulvi"));
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
                            ch.pipeline().addLast("httpAggregator", new HttpObjectAggregator(Constants.HTTP_MAX_BODY_SIZE));
                            ch.pipeline().addLast("httpEncoder", new HttpResponseEncoder());
                            ch.pipeline().addLast("handler", new DsyncserverHandler());
                            
                        }
                    });

            ChannelFuture f = b.bind().sync();
            logger.info(Dsyncserver.class.getName() + " started and listen on " + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {

        new Dsyncserver(2811).start();
    }

}
