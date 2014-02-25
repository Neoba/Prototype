package com.neoba.dsync.vcdiff;

public class Block {

    String text;
    int offset;
    Block nextBlock;

    Block(String text, int offset) {
        this.text = text;
        this.offset = offset;
        this.nextBlock = null;
    }

    String getText() {
        return this.text;
    }

    int getOffset() {
        return this.offset;
    }

    void setNextBlock(Block nextBlock) {
        this.nextBlock = nextBlock;
    }
    
    Block getNextBlock() {
        return this.nextBlock;
    }
}
