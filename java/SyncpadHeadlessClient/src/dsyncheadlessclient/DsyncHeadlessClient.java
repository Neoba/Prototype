package dsyncheadlessclient;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import net.dongliu.vcdiff.VcdiffDecoder;
import net.dongliu.vcdiff.VcdiffEncoder;

/**
 *
 * @author root
 */
public class DsyncHeadlessClient {

    /**
     * @param args the command line arguments
     */
    
    static UUID cookie = null;//UUID.fromString("0a3e8a08-630a-4082-b533-3a6cc2c73984");
    static HashMap<UUID, document> cache;
    static byte version = 0x02;
    static String access_token ="CAAU6ZCTzlZBUoBAKrknMVZCWmCX405KeZCJq5aEZAfO7fLOhvOBU9NQzPb0o8ecd6Wy3YH31hgLVkJTZAM2v5mqfX5Qc14BhW1oxKksWuEevbkiKB6V5A6R38N0ZABXuh3YEnh10Cay0xCwxPkh4UDKlIXgxocl64PaTTMHkBWELrXVTMHgY3VjdKIJEZC0jp9a41GIZBHKZCZAgBCWg8yOcNnqYx2pSHcAPPQiQnVpWi5bKGPjpDX8oDka";
    public static void main(String[] args) throws IOException, Exception {
        // TODO code application logic here
        HashMap<String, UUID> docs = new HashMap();
        cache = new HashMap();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ByteBuffer in = null, buff;
        String cmd, docname;
        String regid = "CONSOLE";
        System.out.println("Neoba Dsync headless-client REPL initializing..\nready..");
        while (true) {
            cmd = br.readLine();
            boolean e = false;
            switch (cmd.split(" ")[0]) {
                case "ping":
                    System.out.println(syncpadlib.ping() ? "Pong!" : "Nope!");
                    break;
                case "cu":
                    buff = ByteBuffer.allocate(6 + 20 + cmd.split(" ")[1].length());
                    buff.put(version);
                    buff.put((byte) 0x05);
                    buff.putInt(cmd.split(" ")[1].length());
                    buff.put(cmd.split(" ")[1].getBytes());
                    for (byte b : MessageDigest.getInstance("SHA").digest(cmd.split(" ")[2].getBytes())) {
                        buff.put(b);
                    }
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    break;
                case "fcu":
                    System.out.println(syncpadlib.facebookCreateUser(cmd.split(" ")[1], access_token));
                    break;
                case "del":
                    UUID docuid = docs.get(cmd.split(" ")[1]);
                    buff = null;
                    if (docuid != null) {
                        buff = ByteBuffer.allocate(2 + 4 + 16 + 16);
                        buff.put(version);
                        buff.put((byte) 0x0B);
                        buff.putInt(0x0000DE1E);
                        syncpadlib.putUUID(buff, cookie);
                        buff.putLong(docuid.getLeastSignificantBits());
                        buff.putLong(docuid.getMostSignificantBits());
                    }
                    in = syncpadlib.sendPost(buff);
                    if (in.getInt(2) == 0xFFFF) {
                        //cache.remove(docuid);
                        System.out.println("yup deleted");

                    } else {
                        System.err.println("som error");
                    }
                    buff.clear();
                    break;
                case "flogin":
                    
                    System.out.println();
                    break;
                case "login":
                    buff = ByteBuffer.allocate(6 + 20 + 1 + cmd.split(" ")[1].length() + 4 + regid.length());
                    buff.put(version);
                    buff.put((byte) 0x06);
                    buff.putInt(cmd.split(" ")[1].length());
                    buff.put(cmd.split(" ")[1].getBytes());
                    for (byte b : MessageDigest.getInstance("SHA").digest(cmd.split(" ")[2].getBytes())) {
                        buff.put(b);
                    }
                    buff.put((byte) 0x0C);
                    buff.putInt(regid.length());
                    buff.put(regid.getBytes());
                    in = syncpadlib.sendPost(buff);
                    if (in.getInt(2) == 0xFFFF) {
                        cookie = new UUID(in.getLong(14), in.getLong(6));
                        System.out.println("recived a cookie :) --> " + cookie);
                    } else {
                        System.err.println("error");
                    }
                    buff.clear();
                    break;
                case "cr":
                    docname = cmd.split(" ")[1];
                    buff = ByteBuffer.allocate(16 + 2 + 4 + 16);
                    buff.put(version);
                    buff.put((byte) 0x02);
                    buff.putInt(0xFFFF);
                    syncpadlib.putUUID(buff, cookie);
                    syncpadlib.putUUID(buff, UUID.randomUUID());
                    in = syncpadlib.sendPost(buff);
                    if (in.getInt(2) == 0xFFFF) {
                        UUID docid = new UUID(in.getLong(14), in.getLong(6));
                        System.out.println("created a new doc: " + docid.toString());
                        docs.put(docname, docid);
                        System.out.println("created [" + docid.toString() + "]");
                        cache.put(docid, new document(docname, new ArrayList(), -1, "", (byte) 2, true));
                    } else {
                        System.err.println("som error");
                    }
                    buff.clear();
                    break;

                case "ed":
                    System.out.println(docs);
                    docuid = docs.get(cmd.split(" ")[1]);
                    if (docuid != null) {
                        byte[] edit = new VcdiffEncoder(cache.get(docuid).dict, cmd.split(" ")[2]).encode();
                        buff = ByteBuffer.allocate(2 + 4 + 16 + 16 + edit.length + 4);
                        buff.put(version);
                        buff.put((byte) 0x03);
                        buff.putInt(edit.length);
                        syncpadlib.putUUID(buff, cookie);
                        buff.putLong(docuid.getLeastSignificantBits());
                        buff.putLong(docuid.getMostSignificantBits());
                        for (byte b : edit) {
                            buff.put(b);
                        }
                        buff.putInt(((document) cache.get(docuid)).age + 1);
                        in = syncpadlib.sendPost(buff);
                        buff.clear();
                        if (in.getInt(2) == 0xFFFF || in.getInt(2) == 0x8008) {

                            if (in.getInt(2) == 0x8008) {
                                ((document) cache.get(docuid)).age = in.getInt(6);
                                System.out.println("Syncfail recovery: correct version " + ((document) cache.get(docuid)).age);
                                int length = in.getInt(10);

                                StringBuilder dictnew = new StringBuilder();
                                for (int h = 0; h < length; h++) {
                                    dictnew.append((char) in.get(14 + h));
                                }
                                ((document) cache.get(docuid)).dict = dictnew.toString();
                                System.out.println("Syncfail recovery: correct dictionary " + ((document) cache.get(docuid)).dict);
                                edit = new VcdiffEncoder(cache.get(docuid).dict, cmd.split(" ")[2]).encode();
                            } else {
                                ((document) cache.get(docuid)).age += 1;
                            }

                            ((document) cache.get(docuid)).diff.clear();
                            for (byte b : edit) {
                                ((document) cache.get(docuid)).diff.add(b);
                            }

                            if (((document) cache.get(docuid)).age % 5 == 0) {
                                ((document) cache.get(docuid)).dict = new VcdiffDecoder(((document) cache.get(docuid)).dict, edit).decode();
                                System.out.println("dictionary updated.. new dict: \n" + ((document) cache.get(docuid)).dict);
                            }

                            System.out.println("version" + ((document) cache.get(docuid)).age);
                        } else {
                            System.err.println("some errr");
                        }
                        buff.clear();
                    }
                    break;
                case "gd":
                    buff = ByteBuffer.allocate(6 + 16);
                    buff.put(version);
                    buff.put((byte) 0x04);
                    buff.putInt(0xFFFF);
                    syncpadlib.putUUID(buff, cookie);
                    in = syncpadlib.sendPost(buff);
                    int docnum = 1; // hack to deprecate titles. Instead of titles, we use numbers.. really sorry
                    buff.clear();
                    int docount = in.getInt(2);
                    int base = 6;
                    for (int i = 0; i < docount; i++) {
                        UUID id = new UUID(in.getLong(base + 8), in.getLong(base));

                        int sdiff = in.getInt(base + 16);
                        ArrayList<Byte> diff = new ArrayList<>(sdiff);

                        for (int j = 0; j < sdiff; j++) {
                            diff.add(in.get(base + 20 + j));
                        }
                        int sdict = in.getInt(base + 20 + sdiff);
                        byte[] dictarray = new byte[sdict];

                        for (int j = 0; j < sdict; j++) {
                            dictarray[j] = in.get(base + 24 + sdiff + j);
                        }
                        String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(dictarray)).toString();
                        int stitle = in.getInt(base + 24 + sdiff + sdict);
                        byte[] titlearray = new byte[stitle];

                        for (int j = 0; j < stitle; j++) {
                            titlearray[j] = in.get(base + 28 + sdiff + sdict + j);
                        }

                        String title = Charset.forName("UTF-8").decode(ByteBuffer.wrap(titlearray)).toString();
                        int age = in.getInt(base + 28 + sdiff + sdict + stitle);
                        byte permission = in.get(base + 28 + sdiff + sdict + stitle + 4);
                        boolean owns = in.get(base + 28 + sdiff + sdict + stitle + 4 + 1) == 0x1;
                        base = base + 32 + sdiff + sdict + stitle + 1 + 1;
                        cache.put(id, new document(title, diff, age, dict, permission, owns));
                        docs.put(Integer.toString(docnum), id);
                        System.out.println(docnum + " is " + id);
                        docnum += 1;
                        System.out.println("added to cache " + cache.get(id).title + ":" + cache.get(id).permission + ":owns=" + owns);
                    }
                    int followerc = in.getInt(base);
                    base += 4;
                    for (int i = 0; i < followerc; i++) {
                        int strc = in.getInt(base);
                        base += 4;
                        byte[] ff = new byte[strc];

                        for (int j = 0; j < strc; j++) {
                            ff[j] = in.get(base + j);

                        }
                        base += strc;
                        String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff)).toString();
                        System.out.println("follower" + dict);
                        System.out.print(" with id" + in.getLong(base));
                        base += 8;
                    }
                    followerc = in.getInt(base);
                    base += 4;
                    System.out.println(followerc);
                    for (int i = 0; i < followerc; i++) {
                        int strc = in.getInt(base);
                        base += 4;
                        byte[] ff = new byte[strc];

                        for (int j = 0; j < strc; j++) {
                            ff[j] = in.get(base + j);

                        }
                        base += strc;
                        String dict = Charset.forName("UTF-8").decode(ByteBuffer.wrap(ff)).toString();
                        System.out.println("following" + dict);
                        System.out.println(" id " + in.getLong(base));
                        base += 8;
                    }

                    int ownc = in.getInt(base);
                    base += 4;
                    for (int i = 0; i < ownc; i++) {
                        UUID doc = new UUID(in.getLong(base + 8), in.getLong(base));
                        System.out.println("OWNS!--> " + doc.toString());
                        base += 16;
                        int reac = in.getInt(base);
                        base += 4;
                        for (int j = 0; j < reac; j++) {
                            System.out.println("READS!--> " + in.getLong(base));
                            base += 8;
                        }
                        reac = in.getInt(base);
                        base += 4;
                        for (int j = 0; j < reac; j++) {
                            System.out.println(in.getLong(base));
                            base += 8;
                        }
                    }
                    break;
                case "follow":
                    buff = ByteBuffer.allocate(6 + 16 + cmd.split(" ")[1].length());
                    buff.put(version);
                    buff.put((byte) 0x07);
                    buff.putInt(cmd.split(" ")[1].length());
                    syncpadlib.putUUID(buff, cookie);
                    buff.put(cmd.split(" ")[1].getBytes());
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    if (in.getInt(2) == 0xFFFF) {
                        System.out.println("followed " + in.getLong(6));
                    }
                    break;
                case "unfollow":
                    buff = ByteBuffer.allocate(6 + 16 + cmd.split(" ")[1].length());
                    buff.put(version);
                    buff.put((byte) 0x0C);
                    buff.putInt(cmd.split(" ")[1].length());
                    syncpadlib.putUUID(buff, cookie);
                    buff.put(cmd.split(" ")[1].getBytes());
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    if (in.getInt(2) == 0xFFFF) {
                        System.out.println("unfollowed " + in.getLong(6));
                        int count = in.getInt(6 + 8);
                        base = 6 + 8 + 4;
                        for (int i = 0; i < count; i++) {
                            UUID id = new UUID(in.getLong(base + 8), in.getLong(base));
                            base += 16;
                            System.out.println("removing " + id + " from cache");
                            cache.remove(id);
                        }
                    }
                    break;
                case "poke":
                    buff = ByteBuffer.allocate(6 + 16 + cmd.split(" ")[1].length());
                    buff.put(version);
                    buff.put((byte) 0x0A);
                    buff.putInt(cmd.split(" ")[1].length());
                    syncpadlib.putUUID(buff, cookie);
                    buff.put(cmd.split(" ")[1].getBytes());
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    if (in.getInt(2) == 0xFFFF) {
                        System.out.println("followed " + in.getLong(6));
                    }
                    break;
                case "logout":
                    buff = ByteBuffer.allocate(6 + 16);
                    buff.put(version);
                    buff.put((byte) 0x09);
                    buff.putInt(0xFFFF);
                    syncpadlib.putUUID(buff, cookie);
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    break;
                case "grant":
                    buff = ByteBuffer.allocate(6 + 16 + 16 + 9 * ((cmd.split(" ").length - 2)) / 2);
                    buff.put(version);
                    buff.put((byte) 0x08);
                    buff.putInt((cmd.split(" ").length - 2) / 2);
                    syncpadlib.putUUID(buff, cookie);
                    buff.putLong((docs.get(cmd.split(" ")[1])).getLeastSignificantBits());
                    buff.putLong((docs.get(cmd.split(" ")[1])).getMostSignificantBits());
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
                        System.out.println(Long.parseLong(cmd.split(" ")[i + 1]) + " " + cmd.split(" ")[i + 1]);
                    }
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    break;
                case "exit":
                    System.exit(0);
                default:
                    System.out.println("bad command");
                    e = true;
            }
            if (!e) {
                //System.out.println("Res:");
                //printhex(in.array(), in.array().length);
            }

        }

    }

    static class document {

        public ArrayList diff;
        public int age;
        public String dict;
        public String title;
        public byte permission;
        public boolean owns;

        document(String t, ArrayList d, int a, String di, byte perm, boolean owns) {
            this.diff = d;
            this.age = a;
            this.dict = di;
            this.title = t;
            this.permission = perm;
            this.owns = owns;
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
