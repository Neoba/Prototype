package com.neoba.dsync.vcdiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compression {
    public static void CompressList(List<Object> diff, OutputStream outstream) throws IOException{
        final GZIPOutputStream gz=new GZIPOutputStream(outstream);
        final ObjectOutputStream oos=new ObjectOutputStream(gz);
        
        try{
            oos.writeObject(diff);
            oos.flush();
        }
        finally{
            oos.close();
            outstream.close();
        }
        
    }
    
    public static List<Object> DecompressList(InputStream instream) throws IOException, ClassNotFoundException{
        final GZIPInputStream gs=new GZIPInputStream(instream);
        final ObjectInputStream ois=new ObjectInputStream(gs);
        try{
            List<Object> ddiff=(List<Object>)ois.readObject();
            return ddiff;
        }
        finally{
            gs.close();
            ois.close();
        }
    }
}
