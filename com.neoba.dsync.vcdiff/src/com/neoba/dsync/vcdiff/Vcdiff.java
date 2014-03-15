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
        List<Object> diffString = new ArrayList<>();
        List<Object> rleDiffString = new ArrayList<>();
        int targetLength;
        int targetIndex;
        int currentHash;
        int runLength;
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
        //Begin Run Length Encoder
        for (int i = 0; i < diffString.size(); i++) {
            runLength = 1;
            while (i + 1 < diffString.size()
                    && diffString.get(i).getClass() == Integer.class
                    && diffString.get(i + 1).getClass() == Integer.class
                    && diffString.get(i) == diffString.get(i + 1)) {
                runLength++;
                i++;
            }
            if (runLength != 1) {
                rleDiffString.add(runLength * -1);
            }
            rleDiffString.add(diffString.get(i));
        }
        return rleDiffString;
    }

    public String decode(String dict, List<Object> rleDiff) {
        List<String> output = new ArrayList<>();
        List<Object> diff = new ArrayList<>();
        String outs = "";
        int i;
        if (rleDiff.isEmpty()) {
            return dict;
        }
        for (i = 0; i < rleDiff.size(); i++) {
            if(rleDiff.get(i).getClass()==Integer.class)
            if ((int)rleDiff.get(i) >= 0) {
                diff.add(rleDiff.get(i));
            } else {
                int t = -1 * (int)rleDiff.get(i);
                while (t-- != 0)
                    diff.add(rleDiff.get(i + 1));
                i++;
            }
            else{
                diff.add((String)rleDiff.get(i));
            }
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
