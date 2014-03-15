package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.List;

public class BlockText {

    String originalText;
    int blockSize;
    List<Block> blocks;
    /**
     * creates a list of Blocks
     * @param originalText
     * @param blockSize 
     */
    BlockText(String originalText, int blockSize) {
        this.originalText = originalText;
        this.blockSize = blockSize;
        int i;
        int len = originalText.split("").length - 1;
        int endIndex;
        blocks = new ArrayList<>();

        for (i = 0; i < len; i += blockSize) {
            endIndex = i + blockSize >= len ? len : i + blockSize;
            blocks.add(new Block(originalText.substring(i, endIndex), i));
        }
    }

    List<Block> getBlocks() {
        return this.blocks;
    }

    String getOriginalText() {
        return this.originalText;
    }

    int getBlockSize() {
        return this.blockSize;
    }
}
