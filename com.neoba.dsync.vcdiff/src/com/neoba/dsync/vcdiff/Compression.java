/*
 * Copyright 2014 Atul Vinayak.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.neoba.dsync.vcdiff;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Atul Vinayak
 */
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
