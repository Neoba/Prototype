/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Atul Vinayak
 */
public class RollingHash {

    int primeBase = 257;
    int primeMod = 1000000007;
    int lastPower = 0;
    String lastString = "";
    int lastHash = 0;

    void RollingHash() {

    }

    int moduloExp(int base, int power, int modulo) {
        int toReturn = 1, i;
        for (i = 0; i < power; i += 1) {
            toReturn = (base * toReturn) % modulo;
        }
        return toReturn;
    }

    int hash(String toHash) {
        int hash = 0;
        char[] toHashArray = toHash.toCharArray();
        int i;
        int len = toHashArray.length;
        for (i = 0; i < len; i += 1) {
            hash += ((int) (toHashArray[i]) * moduloExp(primeBase, len - 1 - i, primeMod)) % primeMod;
            hash %= primeMod;
        }
        lastPower = moduloExp(primeBase, len - 1, primeMod);
        lastString = toHash;
        lastHash = hash;
        return hash;
    }

    int nextHash(char toAdd) {
        int hash = lastHash;
        char[] tlsArray = lastString.toCharArray();
        List<Character> lsArray =new ArrayList<Character>();
        for(char c:tlsArray)
            lsArray.add(c);
        hash -= lastPower * ((int) (lsArray.get(0)));
        hash = hash * primeBase + toAdd;
        hash %= primeMod;
        if (hash < 0) {
            hash += primeMod;
        }
        lsArray.remove(0);
        lsArray.add(toAdd);//Probable Error Point
        //lsArray.toArray(tlsArray);
        StringBuilder builder = new StringBuilder();
        for (char s : lsArray) {
            builder.append(s);
        }
        lastString = builder.toString();
        lastHash = hash;
        return hash;

    }

}
