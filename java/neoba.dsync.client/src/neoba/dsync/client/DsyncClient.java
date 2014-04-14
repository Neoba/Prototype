/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package neoba.dsync.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.Channel;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 *
 * @author Atul Vinayak
 */


public class DsyncClient {
    
    private final String host;
    private final int port;
    public static void main(String args[]) throws InterruptedException, IOException{
        new DsyncClient("127.0.0.1",8001).run();
    }
    public DsyncClient(String host, int port){
        this.host=host;
        this.port=port;
    }
    
    public void run() throws InterruptedException, IOException{
        EventLoopGroup group = new NioEventLoopGroup();
        try{
            Bootstrap bootstrap = new Bootstrap().group(group).channel(NioSocketChannel.class).handler(new DsyncClientInitializer());
            Channel channel=bootstrap.connect(host,port).sync().channel();
            BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
            
            while(true){
                channel.write(in.readLine()+"\r\n");
            }
            
        }finally{
            group.shutdownGracefully();
        }
    }
}
