/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package openvcdiffjava;

import java.nio.ByteBuffer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Atul Vinayak
 */
public class OpenvcdiffjavaTest {
    
    public OpenvcdiffjavaTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of vcdiffEncode method, of class Openvcdiffjava.
     */
    @Test
    public void testVcdiffEncode() {
        System.out.println("vcdiffEncode");
        ByteBuffer dictionary = null;
        ByteBuffer target = null;
        Openvcdiffjava instance = new Openvcdiffjava();
        byte[] expResult = null;
        byte[] result = instance.vcdiffEncode(dictionary, target);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of vcdiffDecode method, of class Openvcdiffjava.
     */
    @Test
    public void testVcdiffDecode() {
        System.out.println("vcdiffDecode");
        ByteBuffer dictionary = null;
        ByteBuffer delta = null;
        Openvcdiffjava instance = new Openvcdiffjava();
        String expResult = "";
        String result = instance.vcdiffDecode(dictionary, delta);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
