/*
############################################################################
##
## Copyright (C) 2006-2009 University of Utah. All rights reserved.
##
## This file is part of DeepPeep.
##
## This file may be used under the terms of the GNU General Public
## License version 2.0 as published by the Free Software Foundation
## and appearing in the file LICENSE.GPL included in the packaging of
## this file.  Please review the following to ensure GNU General Public
## Licensing requirements will be met:
## http://www.opensource.org/licenses/gpl-license.php
##
## If you are unsure which license is appropriate for your use (for
## instance, you are interested in developing a commercial derivative
## of DeepPeep), please contact us at deeppeep@sci.utah.edu.
##
## This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING THE
## WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
##
############################################################################
*/
package focusedCrawler.util.string;


public class StringHashFunctions {

    /**

     * usa a funcao lookup para o hashcode

     */

    public static final int hashCode(String str) {

        int hash = lookup(str, str.length(), 0x9e3779b9);

        if (hash == 0)

        	return str.hashCode();

        return hash;

    }





    /**

     * funcao baseada em gerador de numeros aleatorios

     */

    public static final int hashCodeU(String str) {

        int h = 0, i = 0, len = str.length(), a = 31415, b = 27183;

        for(; i < len; i++, a = a*b) {

            h = a * h + str.charAt(i);

        }

        return h;

    }





    /**

     * funcao baseada em gerador de numeros aleatorios

     */

    public static final int hashCodeN(String str) {

        int h = 0, i = 0, len = str.length(), a = 127;

        for(; i < len; i++) {

            h = a * h + str.charAt(i);

        }

        return h;

    }





    /**

     * --------------------------------------------------------------------

     * mix -- mix 3 32-bit values reversibly.

     * For every delta with one or two bit set, and the deltas of all three

     *   high bits or all three low bits, whether the original value of a,b,c

     *   is almost all zero or is uniformly distributed,

     * * If mix() is run forward or backward, at least 32 bits in a,b,c

     *   have at least 1/4 probability of changing.

     * * If mix() is run forward, every bit of c will change between 1/3 and

     *   2/3 of the time.  (Well, 22/100 and 78/100 for some 2-bit deltas.)

     * mix() takes 36 machine instructions, but only 18 cycles on a superscalar

     *   machine (like a Pentium or a Sparc).  No faster mixer seems to work,

     *   that's the result of my brute-force search.  There were about 2^^68

     *   hashes to choose from.  I only tested about a billion of those.

     * --------------------------------------------------------------------

     */

    private final static void mix(int[] x) {

        x[0] -= x[1]; x[0] -= x[2]; x[0] ^= (x[2]>>13);

        x[1] -= x[2]; x[1] -= x[0]; x[1] ^= (x[0]<<8);

        x[2] -= x[0]; x[2] -= x[1]; x[2] ^= (x[1]>>13);

        x[0] -= x[1]; x[0] -= x[2]; x[0] ^= (x[2]>>12);

        x[1] -= x[2]; x[1] -= x[0]; x[1] ^= (x[0]<<16);

        x[2] -= x[0]; x[2] -= x[1]; x[2] ^= (x[1]>>5);

        x[0] -= x[1]; x[0] -= x[2]; x[0] ^= (x[2]>>3);

        x[1] -= x[2]; x[1] -= x[0]; x[1] ^= (x[0]<<10);

        x[2] -= x[0]; x[2] -= x[1]; x[2] ^= (x[1]>>15);

    }





    /**

     * --------------------------------------------------------------------

     * lookup() -- hash a variable-length key into a 32-bit value

     *   k     : the key (the unaligned variable-length array of bytes)

     *   len   : the length of the key, counting by bytes

     *   level : can be any 4-byte value

     * Returns a 32-bit value.  Every bit of the key affects every bit of

     * the return value.  Every 1-bit and 2-bit delta achieves avalanche.

     * About 6len+35 instructions.

     *

     * The best hash table sizes are powers of 2.  There is no need to do

     * mod a prime (mod is sooo slow!).  If you need less than 32 bits,

     * use a bitmask.  For example, if you need only 10 bits, do

     *   h = (h & hashmask(10));

     * In which case, the hash table should have hashsize(10) elements.

     *

     * If you are hashing n strings (ub1 **)k, do it like this:

     *   for (i=0, h=0; i<n; ++i) h = lookup( k[i], len[i], h);

     *

     * By Bob Jenkins, 1996.  74512.261@compuserve.com.  You may use this

     * code any way you wish, private, educational, or commercial.

     *

     * See http://ourworld.compuserve.com/homepages/bob_jenkins/evahash.htm

     * Use for hash table lookup, or anything where one collision in 2^32 is

     * acceptable.  Do NOT use for cryptographic purposes.

     * --------------------------------------------------------------------

     */

    public final static int lookupInt(int[] k, int length, int level)

    {

        int len, p = 0;

        int x[] = new int[3];





        /* Set up the internal state */

        len = length;

        x[0] = x[1] = 0x9e3779b9;  /* the golden ratio; an arbitrary value */

        x[2] = level;           /* the previous hash value */





        /*---------------------------------------- handle most of the key */

        while (len >= 12)

        {

            x[0] += (k[p+0] +((int)k[p+1]<<8) +((int)k[p+2]<<16) +((int)k[p+3]<<24));

            x[1] += (k[p+4] +((int)k[p+5]<<8) +((int)k[p+6]<<16) +((int)k[p+7]<<24));

            x[2] += (k[p+8] +((int)k[p+9]<<8) +((int)k[p+10]<<16)+((int)k[p+11]<<24));

            mix(x);

            p += 12; len -= 12;

        }





        /*------------------------------------- handle the last 11 bytes */

        x[2] += length;

        switch(len)              /* all the case statements fall through */

        {

            case 11: x[2]+=((int)k[p+10]<<24);

            case 10: x[2]+=((int)k[p+9]<<16);

            case 9 : x[2]+=((int)k[p+8]<<8);

              /* the first byte of c is reserved for the length */

            case 8 : x[1]+=((int)k[p+7]<<24);

            case 7 : x[1]+=((int)k[p+6]<<16);

            case 6 : x[1]+=((int)k[p+5]<<8);

            case 5 : x[1]+=k[p+4];

            case 4 : x[0]+=((int)k[p+3]<<24);

            case 3 : x[0]+=((int)k[p+2]<<16);

            case 2 : x[0]+=((int)k[p+1]<<8);

            case 1 : x[0]+=k[p+0];

             /* case 0: nothing left to add */

        }





        mix(x);

        /*-------------------------------------------- report the result */

        return x[2];

    }





    public final static int lookup(String k, int length, int level)

    {

        return lookupArray(k, length, level, new int[3]);

    }





    public final static int lookupArray(String k, int length, int level, int x[])

    {

        int len, p = 0;

//        int x[] = new int[3];





        /* Set up the internal state */

        len = length;

        x[0] = x[1] = 0x9e3779b9;  /* the golden ratio; an arbitrary value */

        x[2] = level;           /* the previous hash value */





        /*---------------------------------------- handle most of the key */

        while (len >= 12)

        {

            x[0] += (k.charAt(p+0) +((int)k.charAt(p+1)<<8) +((int)k.charAt(p+2)<<16) +((int)k.charAt(p+3)<<24));

            x[1] += (k.charAt(p+4) +((int)k.charAt(p+5)<<8) +((int)k.charAt(p+6)<<16) +((int)k.charAt(p+7)<<24));

            x[2] += (k.charAt(p+8) +((int)k.charAt(p+9)<<8) +((int)k.charAt(p+10)<<16)+((int)k.charAt(p+11)<<24));

            mix(x);

            p += 12; len -= 12;

        }





        /*------------------------------------- handle the last 11 bytes */

        x[2] += length;

        switch(len)              /* all the case statements fall through */

        {

            case 11: x[2]+=((int)k.charAt(p+10)<<24);

            case 10: x[2]+=((int)k.charAt(p+9)<<16);

            case 9 : x[2]+=((int)k.charAt(p+8)<<8);

              /* the first byte of c is reserved for the length */

            case 8 : x[1]+=((int)k.charAt(p+7)<<24);

            case 7 : x[1]+=((int)k.charAt(p+6)<<16);

            case 6 : x[1]+=((int)k.charAt(p+5)<<8);

            case 5 : x[1]+=k.charAt(p+4);

            case 4 : x[0]+=((int)k.charAt(p+3)<<24);

            case 3 : x[0]+=((int)k.charAt(p+2)<<16);

            case 2 : x[0]+=((int)k.charAt(p+1)<<8);

            case 1 : x[0]+=k.charAt(p+0);

             /* case 0: nothing left to add */

        }

        mix(x);

        /*-------------------------------------------- report the result */

        return x[2];

    }

}