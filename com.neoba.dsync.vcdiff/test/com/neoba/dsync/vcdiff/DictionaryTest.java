/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.neoba.dsync.vcdiff;

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
    }

    /**
     * Test of populateDictionary method, of class Dictionary.
     */
    public void testPopulateDictionary() {
        System.out.println("populateDictionary");
        BlockText dictText = null;
        RollingHash hasher = null;
        Dictionary instance = new Dictionary();
        instance.populateDictionary(dictText, hasher);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
//
//    /**
//     * Test of getMatch method, of class Dictionary.
//     */
//    public void testGetMatch() {
//        System.out.println("getMatch");
//        int hash = 0;
//        int blockSize = 0;
//        String target = "";
//        Dictionary instance = new Dictionary();
//        Block expResult = null;
//        Block result = instance.getMatch(hash, blockSize, target);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//    
}
