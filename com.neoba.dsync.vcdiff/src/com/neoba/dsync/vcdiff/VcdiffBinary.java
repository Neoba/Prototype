package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.List;

public class VcdiffBinary {

    RollingHash hash;
    Dictionary dictText;
    public int blockSize;

    public VcdiffBinary() {
        this.hash = new RollingHash();
        this.dictText = new Dictionary();
        this.blockSize = 20;
        this.hash = new RollingHash();
    }

    public List<Object> encode(String dict, String target) {
        List<Object> diffString = new ArrayList<>();
        int targetLength;
        int targetIndex;
        int currentHash;
        int runLength;
        String addBuffer = "";
        Block match;
        if (dict.equals(target)) {
            diffString.add(0);
            diffString.add(0);
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
        int index=0;
        int len=diffString.size();
        for(int i=0;i<len;i++){
            if(diffString.get(i).getClass()==String.class){
                diffString.add(diffString.get(i));
                diffString.set(i,-1*++index );
            }
        }
        diffString.add(0, index);
        diffString.add(0, len);
        return diffString;
    }

    public String decode(String dict, List<Object> diff) {
        List<String> output = new ArrayList<>();

        String outs = "";
        int i;
        int len=(int)diff.remove(0);
        int strcount=(int)diff.remove(0);
        for(i=0;i<len;i++){
            if((int)diff.get(i)<0){
                diff.set(i, diff.get(len-1+(-1)*(int)diff.get(i)));
                
            }
        }
        if(len>0) for(i=strcount+len-1;i>=len;i--){
            diff.remove(i);
        }
        if (diff.isEmpty()) {
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
