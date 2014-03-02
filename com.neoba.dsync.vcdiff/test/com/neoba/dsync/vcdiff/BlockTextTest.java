/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.dsync.vcdiff;

import java.util.List;
import junit.framework.TestCase;

/**
 *
 * @author Atul Vinayak
 */
public class BlockTextTest extends TestCase {
    
    public BlockTextTest(String testName) {
        super(testName);
    }

    /**
     * Test of getBlocks method, of class BlockText.
     */
    public void testBlockText() {
        System.out.println("BlockText: BlockText");
        //TODO: StringIndexOutOfBoundsException occurs for odd sized strings
        //FIX: add a conditional assertion for odd sized strings
        String testcase="abcdef";
        BlockText instance = new BlockText(testcase,2);
        for(Block i:instance.blocks)
            assertTrue(i.text.equals(testcase.substring(i.offset,i.offset+2)));
    }


    
}
