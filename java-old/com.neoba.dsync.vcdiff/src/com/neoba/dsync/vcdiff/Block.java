package com.neoba.dsync.vcdiff;

public class Block {

    String text;
    int offset;
    
    Block(String text, int offset) {
        this.text = text;
        this.offset = offset;
    }

    String getText() {
        return this.text;
    }

    int getOffset() {
        return this.offset;
    }
}
