/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import junit.framework.TestCase;

/**
 *
 * @author Atul Vinayak
 */
public class DictionaryTest extends TestCase {
    
    public DictionaryTest(String testName) {
        super(testName);
    }

    /**
     * Test of put method, of class Dictionary.
     */
    public void testPut() {
        System.out.println("Dictionary: put");
        int key = 123;
        Block block = new Block("test",1);
        Dictionary instance = new Dictionary();
        instance.put(key, block);
        assertEquals(((ArrayList<Block>)instance.dictionary.get(123)).get(0).text,"test");
    }

    /**
     * Test of populateDictionary method, of class Dictionary.
     */
    public void testPopulateDictionary() {
        System.out.println("populateDictionary");
        String testcase="abcdab";
        BlockText dictText = new BlockText(testcase,2);
        RollingHash hasher = new RollingHash();
        Dictionary instance = new Dictionary();
        instance.populateDictionary(dictText, hasher);
        //instance.dictionary is in this format
        // {25543:[("cd",2)] , 25027:[("ab",0),("ab",4)]}
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25543)).get(0).text,"cd");
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25543)).get(0).offset,2);
        
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25027)).get(0).text,"ab");
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25027)).get(0).offset,0);
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25027)).get(1).text,"ab");
        assertEquals(((ArrayList<Block>)instance.dictionary.get(25027)).get(1).offset,4);
        
        
    }

    /**
     * Test of getMatch method, of class Dictionary.
     */
    public void testGetMatch() {
        System.out.println("getMatch");
        int hash = 0;
        int blockSize = 0;
        String target = "";
        Dictionary instance = new Dictionary();
        Block expResult = null;
        Block result = instance.getMatch(hash, blockSize, target);
        assertEquals(expResult, result);
        // TODO this function is still untested.
        // Deploy tests that cover all possible inputs
        
    }
    
}
