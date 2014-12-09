package dsyncheadlessclient;

import static dsyncheadlessclient.syncpadlib.printhex;
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
 * @author neoba systems
 * 
 * syncpad-headless-client
 * -----------------------
 * 
 * This is a simple client program that can be used to work with the syncpad server
 * commands and their users:
 * 
 * 1- Ping: pings the server to check if it's alive
 *  syntax : "ping"
 * 
 * 2- create user: creates a new user in the server
 *  syntax : cu username password
 * 
 * 3- login: logs in the user and gets a cookie if successful
 *  syntax: login username password
 * 
 * -- these next commands require you to be logged in to execute --
 * 
 * 4- create document: creates a new document
 *  syntax: cr name
 * 
 * 5- edit : edits a document. diffing will be done automatically
 *  syntax: ed name edited-content-with-no-spaces
 * 
 * 6- follow: lets you follow a user
 *  syntax: follow username
 * 
 * 7- grant: grants access permissions[read/write/nothing] to followers. 
 *  syntax: grant docname [r/w/n] userid(number) [[r/w/n] userid2(number)] [[r/w/n] userid2(number)]
 * 
 *  this permissions list is absolute non-additive. if 'grant docname' is used, all existing
 *  permissions are removed. ignoring users with n works too.
 * 
 * 8- get digest: gets a digest, all the data(docs,permissions,usernames) for an existing user. usually done at login
 *  syntax- gd
 * 
 *  gd is the primary mechanism analogous to push in mobile phones. if a user followed you, doing gd 
 *  afterwords will show you the username of the user.
 * 
 * 9- delete: deletes a document that you own/shared with you
 *  syntax- del docname
 * 
 *  in phones, this will sends a push message forcing users to delete if you own the doc
 *  or changes the permission table in the owner's device if he shared it with you
 * 
 * 10- unfollow- unfollows a user you're already following
 *  syntax- unfollow username
 * 
 *  sends push message forcing mobile client to delete from follow table
 * 
 * 11- logout- properly logout from server. this causes deletion of cookie
 *  syntax- logout
 * 
 * -- these features are for testing phone clients with push capability --
 * 
 * 12- poke- push pokes a user. similar to the yo app(for testing push connection only)
 *  syntax- poke
 * 
 * -- newer facebook-specific features. ie requires a facebook access token.  --
 * * trying this out might cause an error in server side right now--
 * 
 * 13- create a facebook-integrated user. returns a list of usernames you might want to follow
 *  syntax- fcu username
 * 
 * 14- flogin - passwordless facebook login
 *  syntax- flogin
 * 
 * 
 * debuging
 * ========
 * set syncpadlib.DEBUG to true to see message payloads,request and response in binary
 * 
 * 
 * general message
 * ===============
 * 
 * we're currently trying to move functions from main() and design a unified return type
 * to syncpadlib.java to make development easier for android and other java-based clients
 * bear with us.
 * 
 */ 
public class DsyncHeadlessClient {

    /**
     * @param args the command line arguments
     */
    
    static UUID cookie = null;//UUID.fromString("0a3e8a08-630a-4082-b533-3a6cc2c73984");
    static HashMap<UUID, document> cache;
    static byte version = 0x02;
    static String access_token ="CAAU6ZCTzlZBUoBALZBh0yhHFIe8izzUxgR7FojrkHAxEBmZCxNMZCtAJvPaBlkofks5ZAbNhI0BGUD4tpSQ1aHomPjjODfuJkb1cjTnDMZAgimavYyMS9jWy3SAqRBwBUYXPMXKnInvsZC4ZAFPafvZBEIvGWt3CTiEBQrRRgF2432tLQQmqb7PZCc6Hr2gHJO4p82eumz5ZBXD4TiI4TZAW15TRU";
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
                    break;
                case "fcu":
                    System.out.println(syncpadlib.facebookCreateUser(cmd.split(" ")[1], access_token));
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
                    break;
                case "del":
                    UUID docuid = docs.get(cmd.split(" ")[1]);
                    if(syncpadlib.deleteNote(docuid.toString(), cookie)){
                        System.out.println("yep deteteld "+cmd.split(" ")[1]);
                        cache.remove(docuid);
                    }else{
                        System.out.println("nope counldnt delete "+cmd.split(" ")[1]);
                    }
                    break;
                case "flogin":
                    ArrayList<HashMap<String,String>> a=syncpadlib.facebookLoginUser(access_token, regid);
                    System.out.println(a);
                    if(a.get(0).get("result").equals("success")){
                        cookie=UUID.fromString(a.get(1).get("cookie"));
                    }else{
                        System.out.println("Unregireted user.. please sign up");
                    }
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    ArrayList<HashMap<String, String>> result = syncpadlib.createNote(cookie);
                    if(result.get(0).get("result").equals("success"))
                    {
                        System.out.println("created document :  "+UUID.fromString(result.get(1).get("id")));
                        docs.put(docname, UUID.fromString(result.get(1).get("id")));
                        cache.put( UUID.fromString(result.get(1).get("id")), new document(docname, new ArrayList(), -1, "", (byte) 2, true));
                    }else{
                        System.out.println("error: "+result.get(0).get("result"));
                    }
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                        System.out.println("creator: "+title);
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
                    break;
                case "dict":
                    buff = ByteBuffer.allocate(6 + 16+16 + 4);
                    buff.put(version);
                    buff.put((byte) 0x0D);
                    buff.putInt(cmd.split(" ")[1].length());
                    syncpadlib.putUUID(buff, cookie);
                    syncpadlib.putUUID(buff, UUID.fromString(cmd.split(" ")[1]));
                    buff.putInt(Integer.parseInt(cmd.split(" ")[2]));
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    byte[] aarray=null;
                    if (in.getInt(2) == 0xFFFF) {
                        int stitle = in.getInt(6);
                        aarray = new byte[stitle];

                        for (int j = 0; j < stitle; j++) {
                            aarray[j] = in.get(10 + j);
                        }
                    }
                    System.out.println(Charset.forName("UTF-8").decode(ByteBuffer.wrap(aarray)).toString());
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
                    break;
                case "logout":
                    buff = ByteBuffer.allocate(6 + 16);
                    buff.put(version);
                    buff.put((byte) 0x09);
                    buff.putInt(0xFFFF);
                    syncpadlib.putUUID(buff, cookie);
                    in = syncpadlib.sendPost(buff);
                    buff.clear();
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
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
                    if(syncpadlib.DEBUG){System.out.println("Res:");printhex(in.array(), in.array().length);}
                    break;
                case "exit":
                    System.exit(0);
                default:
                    System.out.println("bad command");
                    e = true;
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
}
