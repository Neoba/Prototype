package com.neoba.dsync.vcdiff;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

public class VcdiffBinary {

    RollingHash hash;
    Dictionary dictText;
    Charset charset;
    CharsetEncoder encoder;
    CharsetDecoder decoder;
    byte nullByte;
    public int blockSize;
    boolean enableCompression;

    public VcdiffBinary(boolean enableCompression) {
        this.hash = new RollingHash();
        this.dictText = new Dictionary();
        this.blockSize = 20;
        this.hash = new RollingHash();
        this.charset = Charset.forName("UTF-8");
        this.encoder = charset.newEncoder();
        this.decoder = charset.newDecoder();
        this.nullByte = 0x0;
        this.enableCompression=enableCompression;
    }

    public byte[] encode(String dict, String target) throws CharacterCodingException, FileNotFoundException, IOException {
        List<Object> diffArray = new ArrayList<>();
        int targetLength;
        int targetIndex;
        int currentHash;
        ByteBuffer buf;
        String addBuffer = "";
        Block match;
        if (dict.equals(target)) {
            if(enableCompression)
                return CompressionUtils.compress(new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
            else
                return new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
        }
        this.dictText.populateDictionary(new BlockText(dict, this.blockSize), this.hash);
        targetLength = target.length();
        targetIndex = 0;
        currentHash = -1;
        while (targetIndex < targetLength) {
            if (targetLength - targetIndex < this.blockSize) {
                diffArray.add(addBuffer + target.substring(targetIndex, targetLength));
                break;
            } else {
                if (currentHash == -1) {
                    currentHash = this.hash.hash(target.substring(targetIndex, targetIndex + this.blockSize));
                } else {
                    currentHash = this.hash.nextHash(target.charAt(targetIndex + (this.blockSize - 1)));
                    if (currentHash < 0) {
                        currentHash = this.hash.hash(target.substring(0, targetIndex + this.blockSize));
                    }
                }
                match = this.dictText.getMatch(currentHash, this.blockSize, target.substring(targetIndex));
                if (match == null) {
                    addBuffer += target.charAt(targetIndex);
                    targetIndex += 1;
                } else {
                    if (addBuffer.length() > 0) {
                        diffArray.add(addBuffer);
                        addBuffer = "";
                    }
                    diffArray.add(match.getOffset());
                    diffArray.add(match.getText().length());
                    targetIndex += match.getText().length();
                    currentHash = -1;
                }
            }
        }
        int stringsTotalBytes = 0;
        int index = 0;
        int len = diffArray.size();
        for (int i = 0; i < len; i++) {
            if (diffArray.get(i).getClass() == String.class) {
                diffArray.add(diffArray.get(i));
                stringsTotalBytes += (((String) diffArray.get(i)).length() + 1);
                diffArray.set(i, -1 * ++index);
            }
        }
        diffArray.add(0, index);
        diffArray.add(0, len);

        buf = ByteBuffer.allocate((len + 2) * 4 + stringsTotalBytes);
        for (int i = 0; i < len + 2; i++) {
            buf.putInt((int) diffArray.get(i));
        }
        if (len > 0) {
            for (int i = len + 2; i < len + 2 + index; i++) {
                buf.put(encoder.encode(CharBuffer.wrap((String) diffArray.get(i))));
                buf.put(nullByte);
            }
        }
        buf.flip();
        ByteBuffer finalbuf = ByteBuffer.wrap(enableCompression?CompressionUtils.compress(buf.array()):buf.array());
        return finalbuf.array();

    }

    public String decode(String dict, byte[] diffBuffer) throws FileNotFoundException, IOException, DataFormatException {
        List<String> output = new ArrayList<>();
        List<Object> diff = new ArrayList<>();
        byte[] data;
        String outs = "";
        int i;

        data = enableCompression?CompressionUtils.decompress(diffBuffer):diffBuffer;
        ByteBuffer buf = ByteBuffer.wrap(data);

        int len = (int) buf.getInt();
        int strcount = (int) buf.getInt(4);
        for (i = 8; i < (len + 2) * 4; i += 4) {
            diff.add((int) buf.getInt(i));
        }

        while (i < data.length) {
            String temp;
            temp = "";
            while (buf.get(i) != nullByte) {
                temp += (char) buf.get(i);
                i++;
            }
            i++;
            diff.add(temp);
        }

        for (i = 0; i < len; i++) {
            if ((int) diff.get(i) < 0) {
                diff.set(i, diff.get(len - 1 + (-1) * (int) diff.get(i)));

            }
        }
        if (len > 0)
            for (i = strcount + len - 1; i >= len; i--)
                diff.remove(i);

        if (diff.isEmpty())
            return dict;

        for (i = 0; i < diff.size(); i += 1) {
            if (diff.get(i).getClass() == Integer.class) {
                output.add(dict.substring((Integer) diff.get(i), (Integer) diff.get(i) + (Integer) diff.get(i + 1)));
                i += 1;
            } else if (diff.get(i).getClass() == String.class) {
                output.add((String) diff.get(i));
            }
        }
        for (String s : output) {
            outs += s;
        }
        return outs;
    }

}
