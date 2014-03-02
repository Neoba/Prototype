package com.neoba.dsync.vcdiff;

import java.util.ArrayList;
import java.util.List;

public class RollingHash {

    int base = 257;
    int mod = 1000000007;
    int lastPower = 0;
    String lastString = "";
    int lastHash = 0;

/**
 * this function calculates the base raised to exponent value
 * modulo a large prime number. modulo is used to prevent large
 * numbers in the return value.
 * @param base the base to which is multiplied power times
 * @param power the value to which base is raised
 * @param modulo the prime number used in finding modulo
 * @return base^power in modulo
 */
    int moduloExponent(int base, int power, int modulo) {
        int toReturn = 1, i;
        for (i = 0; i < power; i += 1) {
            toReturn = (base * toReturn) % modulo;
        }
        return toReturn;
    }
/**
 * Rabin-Karp rolling hash
 * this function calculates the initial value of the hash,
 * so that additional characters(hash of) can be added to the 
 * current hash using nextHash.
 * @param toHash the string whose hash is to be calculated
 * @return the hash of the input string
 */
    int hash(String toHash) {
        int hash = 0;
        char[] hashArray = toHash.toCharArray();
        int i;
        int len = hashArray.length;
        for (i = 0; i < len; i += 1) {
            hash += ((int) (hashArray[i]) * moduloExponent(base, len - 1 - i, mod)) % mod;
            hash %= mod;
        }
        lastPower = moduloExponent(base, len - 1, mod);
        lastString = toHash;
        lastHash = hash;
        return hash;
    }

/**
 * Rabin-Karp rolling hash
 * nextHash is used once a hash is already calculated.
 * the first character's value*lastPower is decreased and 
 * new character's hash is added to the previous hash
 * @param toAdd
 * @return 
 */
    int nextHash(char toAdd) {
        int hash = lastHash;
        char[] tlsArray = lastString.toCharArray();
        List<Character> lsArray =new ArrayList<>();
        for(char c:tlsArray)
            lsArray.add(c);
        hash -= lastPower * ((int) (lsArray.get(0)));
        hash = hash * base + toAdd;
        hash %= mod;
        if (hash < 0) {
            hash += mod;
        }
        lsArray.remove(0);
        lsArray.add(toAdd);
        StringBuilder builder = new StringBuilder();
        for (char s : lsArray) {
            builder.append(s);
        }
        lastString = builder.toString();
        lastHash = hash;
        return hash;

    }

}
