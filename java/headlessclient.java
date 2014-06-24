
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.UUID;

public class headlessclient {

    public static void main(final String args[]) throws ClosedChannelException, IOException {

        final Selector selector = Selector.open();
        InetSocketAddress isa = new InetSocketAddress("localhost", 2810);
        SocketChannel sc = SocketChannel.open(isa);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);

        new Thread() {
            ByteBuffer buff = ByteBuffer.allocate(100);
            SocketChannel sc;

            @Override
            public void run() {

                try {
                    while (selector.select() > 0) {
                        for (SelectionKey sk : selector.selectedKeys()) {
                            selector.selectedKeys().remove(sk);
                            if (sk.isReadable()) {
                                sc = (SocketChannel) sk.channel();
                                int size = 0,count=0;
                                while ((size = sc.read(buff)) > 0) {
                                    count+=size;
                                    buff.flip();
                                }

                                if(buff.array()[1]==0x01){
                                    System.out.println("pong!");
                                }
                                else if(buff.array()[1]==0x02){
                                    UUID uuid=new UUID(buff.getLong(14),buff.getLong(6));
                                    System.out.println("created ["+uuid.toString()+"]");
                                    
                                }
                                buff.clear();
                                //printhex(buff.array(),count);

                            }
                        }

                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }.start();
        /**
         * shell starts here
         */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String cmd;
        String docname = null;
        ByteBuffer buff = null;
        while (true) {
            cmd = br.readLine();
            if (cmd.split(" ")[0].equals("create")) {
                try{
                docname = cmd.split(" ")[1];}
                catch(java.lang.ArrayIndexOutOfBoundsException e){
                    System.out.println("ERROR: No Document name provided");
                    continue;
                }
                buff = ByteBuffer.allocate(4 + 4 + docname.length());
                buff.put((byte) 0x01);
                buff.put((byte) 0x02);
                buff.putInt(docname.length());
                buff.put(docname.getBytes());

                buff.flip();
            }
            else if(cmd.split(" ")[0].equals("ping")){
                buff = ByteBuffer.allocate(4 + 4 + 4);
                buff.put((byte) 0x01);
                buff.put((byte) 0x01);
                buff.putInt(4);
                buff.put("PING".getBytes());

                buff.flip();
            }
            else {
                System.out.println("ERROR: Incorrect Command");
                continue;
            }

            try {
                sc.write(buff);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void printhex(byte[] b,int count) {
        int rem = count;
        String outs;
        int ran = 0;
        //System.out.println(count + "B dump");
        for (int i = 0; i < ((count / 10) + 1); i++) {
            for (int j = 0; (j < rem && j < 10); j++) {
                outs = String.format("%02X ", b[j + count - rem] & 0xff);

                System.out.print(outs);
                ran++;
            }
            for (int j = 0; j < 29 - (ran * 2 + ran - 1); j++) {
                System.out.print(" ");
            }
            ran = 0;
            for (int j = 0; (j < rem && j < 10); j++) {
                System.out.print((char) (b[j + count - rem] >= 30 && b[j + count - rem] <= 127 ? b[j + count - rem] : '.') + " ");
            }
            rem -= 10;

            System.out.println("");

        }

    }

}
