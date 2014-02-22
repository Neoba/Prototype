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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 *
 * @author Atul Vinayak
 */
public class Test {

    protected static final String dd_ = "lenovolenovo";

    protected static final String tt_ = "lenovoklovoe";

    public static void main(String args[]) throws IOException, ClassNotFoundException {
        Vcdiff v;
        v = new Vcdiff();
        v.blockSize = 3;
        List<Object> delta = v.encode(dd_, tt_);
        System.out.println(delta);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Compression.CompressList(delta, out);
        
        InputStream in = new ByteArrayInputStream(out.toByteArray());
        List<Object> rediffed = Compression.DecompressList(in);
        
        System.out.println(out.size());
        String s = v.decode(dd_,rediffed);
        System.out.println(s);
    }
}
