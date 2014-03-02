package com.neoba.dsync.vcdiff;

import junit.framework.TestCase;

public class RollingHashTest extends TestCase {
    
    public RollingHashTest(String testName) {
        super(testName);
    }

    /**
     * Test of moduloExponent method, of class RollingHash.
     */
    public void testModuloExponent() {
        System.out.println("testing RollingHashTest: moduloExponent");
        RollingHash instance = new RollingHash();
        assertEquals(instance.moduloExponent(0, 0, 0), 1);
        assertEquals(instance.moduloExponent(254, 242, 7), 4);
        assertEquals(instance.moduloExponent(1, 1, 1000000007), 1);
        assertEquals(instance.moduloExponent(257, 0, 1000000007), 1);
        assertEquals(instance.moduloExponent(257, 1, 1000000007), 257);
        assertEquals(instance.moduloExponent(257, 2, 1000000007), 66049);
        assertEquals(instance.moduloExponent(257, 3, 1000000007), 16974593);
        assertTrue(instance.moduloExponent(instance.base, 3, instance.mod)<=16974593);

    }

    /**
     * Test of hash method, of class RollingHash.
     */
    public void testHash() {
        System.out.println("testing RollingHashTest: hash");
        RollingHash instance = new RollingHash();   
        assertEquals(instance.hash(""), 0);
        assertEquals(instance.hash("a"), 97);
        assertEquals(instance.hash("ab"), 25027); //used later
        assertEquals(instance.hash("no"), 28381);
        assertEquals(instance.hash("test"), 975753401);
    }

    /**
     * Test of nextHash method, of class RollingHash.
     */
    public void testNextHash() {
        //TODO add more test cases
        System.out.println("testing RollingHashTest: nextHash");
        RollingHash instance = new RollingHash();
        instance.hash("ab");
        assertEquals(instance.nextHash('c'), (25027-257*97)*257+99); //(lastHash-lastPower*new_char)*base+new_character
    }
    
}
