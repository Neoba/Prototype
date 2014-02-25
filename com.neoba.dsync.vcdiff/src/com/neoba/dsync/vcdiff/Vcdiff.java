package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.List;

public class Vcdiff {

    RollingHash hash;
    Dictionary dictText;
   public int blockSize;

   public Vcdiff() {
        this.hash = new RollingHash();
        this.dictText = new Dictionary();
        this.blockSize = 20;
        this.hash = new RollingHash();
    }

   public List<Object> encode(String dict, String target) {
        List<Object> diffString = new ArrayList<Object>();
        int targetLength;
        int targetIndex;
        int currentHash;
        String addBuffer = "";
        Block match;
        if (dict.equals(target)) {
            return diffString;
        }
        this.dictText.populateDictionary(new BlockText(dict, this.blockSize), this.hash);
        targetLength = target.length();
        targetIndex = 0;
        currentHash = -1;
        while (targetIndex < targetLength) {
            if (targetLength - targetIndex < this.blockSize) {
                diffString.add(addBuffer + target.substring(targetIndex, targetLength));
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
                        diffString.add(addBuffer);
                        addBuffer = "";
                    }
                    diffString.add(match.getOffset());
                    diffString.add(match.getText().length());
                    targetIndex += match.getText().length();
                    currentHash = -1;
                }
            }
        }
        return diffString;
    }

   public String decode(String dict, List<Object> diff) {
        List<String> output =new ArrayList<String>();
        String outs = "";
        int i;
        if (diff.size() == 0) {
            return dict;
        }
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
