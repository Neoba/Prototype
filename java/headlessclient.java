
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;
import net.dongliu.vcdiff.exception.VcdiffDecodeException;
import net.dongliu.vcdiff.exception.VcdiffEncodeException;

public class headlessclient {

    static HashMap<String, document> cache;
    static String docname = null;
    static Long userid;

    static class document {

        public ArrayList diff;
        public int age;
        public String dict;
        public String title;

        document(String t, ArrayList d, int a, String di) {
            this.diff = d;
            this.age = a;
            this.dict = di;
            this.title = t;
        }
    }
    static SocketChannel sc;

    public static void main(final String args[]) throws ClosedChannelException, IOException, VcdiffEncodeException, VcdiffDecodeException, NoSuchAlgorithmException {

        final Selector selector = Selector.open();
        InetSocketAddress isa = new InetSocketAddress("localhost", 2810);
        sc = SocketChannel.open(isa);
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ);
        cache = new HashMap();

        new Thread() {
            ByteBuffer buff = ByteBuffer.allocate(1000);
            SocketChannel sc;

            @Override
            public void run() {

                try {
                    while (selector.select() > 0) {
                        for (SelectionKey sk : selector.selectedKeys()) {
                            selector.selectedKeys().remove(sk);
                            if (sk.isReadable()) {
                                sc = (SocketChannel) sk.channel();
                                int size = 0, count = 0;
                                while ((size = sc.read(buff)) > 0) {
                                    count += size;

                                }

                                buff.flip();
                                printhex(buff.array(), count);
                                if (buff.array()[1] == 0x01) {
                                    //System.out.println("PONG!");
                                } else if (buff.array()[1] == 0x02) {
                                    UUID uuid = new UUID(buff.getLong(14), buff.getLong(6));
                                    System.out.println("created [" + uuid.toString() + "]");

                                    cache.put(uuid.toString(), new document(docname, new ArrayList(), 0, " "));
                                } else if (buff.array()[1] == 0x03) {
                                    if (buff.getInt(2) == 0xFFFF) {
                                        System.out.println("edit successful");

                                    } else {
                                        System.out.println("edit failed");
                                    }

                                } else if (buff.array()[1] == 0x04) {
                                    int docount = buff.getInt(2);
                                    int base = 6;
                                    for (int i = 0; i < docount; i++) {
                                        UUID id = new UUID(buff.getLong(base + 8), buff.getLong(base));

                                        int sdiff = buff.getInt(base + 16);
                                        ArrayList<Byte> diff = new ArrayList<>(sdiff);

                                        for (int j = 0; j < sdiff; j++) {
                                            diff.add(buff.get(base + 20 + j));
                                        }
                                        int sdict = buff.getInt(base + 20 + sdiff);
                                        byte[] dictarray = new byte[sdict];

                                        for (int j = 0; j < sdict; j++) {
                                            dictarray[j] = buff.get(base + 24 + sdiff + j);
                                        }
                                        String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(dictarray)).toString();
                                        int stitle = buff.getInt(base + 24 + sdiff + sdict);
                                        byte[] titlearray = new byte[stitle];

                                        for (int j = 0; j < stitle; j++) {
                                            titlearray[j] = buff.get(base + 28 + sdiff + sdict + j);
                                        }

                                        String title = Charset.forName("UTF-8").decode(ByteBuffer.wrap(titlearray)).toString();
                                        int age = buff.getInt(base + 28 + sdiff + sdict + stitle);
                                        base = base + 32 + sdiff + sdict + stitle;
                                        cache.put(id.toString(), new document(title, diff, age, dict));
                                        System.out.println("added to cache: " + id.toString() + " " + cache.get(id.toString()).title);
                                    }

                                } else if (buff.array()[1] == 0x05) {
                                    if (buff.getInt(2) == 0xFFFF) {
                                        System.out.println("Registered Successfully");

                                    } else {
                                        System.out.println("Something went wrong..");
                                    }
                                } else if (buff.array()[1] == 0x06) {
                                    if (buff.getInt(2) == 0xFFFF) {
                                        System.out.println("Logged in Successfully");
                                        userid = buff.getLong(6);
                                        System.out.println("id: " + userid);

                                    } else {
                                        System.out.println("Something went wrong..");
                                    }
                                } else if (buff.array()[1] == 0x07) {
                                    if (buff.getInt(2) == 0xFFFF) {
                                        System.out.println("Followed Successfully");
                                    } else {
                                        System.out.println("Something went wrong..");
                                    }
                                } else if (buff.array()[1] == 0x70) {
                                    System.out.println("Please Log In..");
                                }
                                buff.clear();

                            }
                        }

                    }
                } catch (IOException e) {
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(headlessclient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    ByteBuffer buff = ByteBuffer.allocate(6);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x01);
                    buff.putInt(0x50494E47);
                    buff.flip();
                    try {
                        sc.write(buff);
                    } catch (IOException ex) {
                        Logger.getLogger(headlessclient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }.start();

        /**
         * shell starts here
         */
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String cmd;

        ByteBuffer buff = null;
        while (true) {
            cmd = br.readLine();
            
            switch (cmd.split(" ")[0]) {
                case "cr":
                    try {
                        docname = cmd.split(" ")[1];
                    } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                        System.out.println("ERROR: No Document name provided");
                        continue;
                    }
                    buff = ByteBuffer.allocate(4 + 4 + docname.length());
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x02);
                    buff.putInt(docname.length());
                    buff.put(docname.getBytes());
                    buff.flip();
                    break;
                case "ping":
                    buff = ByteBuffer.allocate(6);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x01);
                    buff.putInt(0x50494E47);
                    buff.flip();
                    break;
                case "ed":
                    byte[] edit = new VcdiffEncoder(" ", cmd.split(" ")[2]).encode();
                    buff = ByteBuffer.allocate(2 + 4 + 16 + edit.length);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x03);
                    buff.putInt(edit.length);
                    UUID docuid = UUID.fromString(cmd.split(" ")[1]);
                    buff.putLong(docuid.getLeastSignificantBits());
                    buff.putLong(docuid.getMostSignificantBits());
                    ((document) cache.get(docuid.toString())).age += 1;
                    ((document) cache.get(docuid.toString())).diff.clear();
                    for (byte b : edit) {
                        buff.put(b);
                        ((document) cache.get(docuid.toString())).diff.add(b);
                    }
                    if (((document) cache.get(docuid.toString())).age >= 5) {
                        ((document) cache.get(docuid.toString())).age = 0;
                        ((document) cache.get(docuid.toString())).dict = new VcdiffDecoder(((document) cache.get(docuid.toString())).dict, edit).decode();
                        System.out.println("dictionary updated.. new dict: \n" + ((document) cache.get(docuid.toString())).dict);
                    }
                    buff.flip();
                    break;
                case "gd":
                    buff = ByteBuffer.allocate(2);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x04);
                    buff.flip();
                    break;
                case "cu":
                    buff = ByteBuffer.allocate(6 + 20 + cmd.split(" ")[1].length());
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x05);
                    buff.putInt(cmd.split(" ")[1].length());
                    buff.put(cmd.split(" ")[1].getBytes());
                    for (byte b : MessageDigest.getInstance("SHA").digest(cmd.split(" ")[2].getBytes())) {
                        buff.put(b);
                    }
                    buff.flip();
                    break;
                case "login":
                    buff = ByteBuffer.allocate(6 + 20 + cmd.split(" ")[1].length());
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x06);
                    buff.putInt(cmd.split(" ")[1].length());
                    buff.put(cmd.split(" ")[1].getBytes());
                    for (byte b : MessageDigest.getInstance("SHA").digest(cmd.split(" ")[2].getBytes())) {
                        buff.put(b);
                    }
                    buff.flip();
                    break;
                case "follow":
                    buff = ByteBuffer.allocate(6 + 8);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x07);
                    buff.putInt(0xFFFF);
                    buff.putLong(Long.parseLong(cmd.split(" ")[1]));
                    buff.flip();
                    break;
                case "grant":
                    
                    buff = ByteBuffer.allocate(6 + 16 + 9 * ((cmd.split(" ").length - 2))/2);
                    buff.put((byte) 0x01);
                    buff.put((byte) 0x08);
                    buff.putInt((cmd.split(" ").length - 2)/2);
                    
                    buff.putLong(UUID.fromString(cmd.split(" ")[1]).getLeastSignificantBits());
                    buff.putLong(UUID.fromString(cmd.split(" ")[1]).getMostSignificantBits());
                    
                    for (int i = 2; i < cmd.split(" ").length; i += 2) {
                        switch (cmd.split(" ")[i]) {
                            case "r":
                                buff.put((byte) 0x01);
                                break;
                            case "w":
                                buff.put((byte) 0x02);
                                break;
                            case "n":
                                buff.put((byte) 0x00);
                                break;
                        }

                        buff.putLong(Long.parseLong(cmd.split(" ")[i + 1]));
                        System.out.println(Long.parseLong(cmd.split(" ")[i + 1])+" "+cmd.split(" ")[i+1]);
                    }
                    buff.flip();
                    break;

                default:
                    System.out.println("ERROR: Incorrect Command");
                    continue;
            }

            try {
                sc.write(buff);
            } catch (IOException e) {
            }
        }

    }

    public static void printhex(byte[] b, int count) {
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
