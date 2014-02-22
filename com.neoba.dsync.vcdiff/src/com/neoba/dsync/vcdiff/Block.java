/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba.dsync.vcdiff;

/**
 *
 * @author Atul Vinayak
 */
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
