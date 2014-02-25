package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dictionary {

    Map dictionary;
    BlockText dictionaryText;

    Dictionary() {
        this.dictionary = new HashMap();
        this.dictionaryText = null;
    }

    void put(int key, Block block) {
        List<Block> p = new ArrayList<Block>();
        List<Block> q = new ArrayList<Block>();
        if (!this.dictionary.containsKey(key)) {
            dictionary.put(key, p);
        }

        q = (List<Block>) this.dictionary.get(key);
        q.add((Block) block);
        this.dictionary.put(key, q);
    }

    void populateDictionary(BlockText dictText, RollingHash hasher) {
        this.dictionary = new HashMap();
        this.dictionaryText = dictText;
        List<Block> blocks = dictText.getBlocks();
        int len = blocks.size();
        for (int i = 0; i < len; i += 1) {
            put(hasher.hash(blocks.get(i).getText()), blocks.get(i));
        }
    }

    Block getMatch(int hash, int blockSize, String target) {
        List<Block> blocks;
        int i, len;
        String dictText;
        String targetText;
        int currentPointer;
        if (this.dictionary.containsKey(hash)) {
            blocks = (List<Block>) this.dictionary.get(hash);
            for (i = 0, len = blocks.size(); i < len; i += 1) {
                if (blocks.get(i).getText().equals(target.substring(0, blockSize))) {
                    if (this.dictionaryText != null && blocks.get(i).getNextBlock() == null) {
                        dictText = this.dictionaryText.getOriginalText().substring(blocks.get(i).getOffset() + blockSize);
                        targetText = target.substring(blockSize);
                        if (dictText.length() == 0 || targetText.length() == 0) {
                            return blocks.get(i);
                        }
                        currentPointer = 0;
                        while (currentPointer < dictText.length() && currentPointer < targetText.length()
                                && dictText.charAt(currentPointer) == targetText.charAt(currentPointer)) {
                            currentPointer += 1;
                        }
                        return new Block(blocks.get(i).getText() + dictText.substring(0, currentPointer), blocks.get(i).getOffset());
                    } else if (blocks.get(i).getNextBlock() != null) {
                        return blocks.get(i);
                    } else {
                        return blocks.get(i);
                    }
                }
            }
            return null;
        }
        return null;
    }
}
