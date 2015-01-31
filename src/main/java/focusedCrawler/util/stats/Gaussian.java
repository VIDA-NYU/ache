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
package focusedCrawler.util.stats;

public class Gaussian {

   // return phi(x) = standard Gaussian pdf
   public static double phi(double x) {
       return Math.exp(-x*x / 2) / Math.sqrt(2 * Math.PI);
   }

   // return phi(x, mu, signma) = Gaussian pdf with mean mu and stddev sigma
   public static double phi(double x, double mu, double sigma) {
       return phi((x - mu) / sigma) / sigma;
   }

   // return Phi(z) = standard Gaussian cdf using Taylor approximation
   public static double Phi(double z) {
       if (z < -8.0) return 0.0;
       if (z >  8.0) return 1.0;
       double sum = 0.0, term = z;
       for (int i = 3; sum + term != sum; i += 2) {
           sum  = sum + term;
           term = term * z * z / i;
       }
       return 0.5 + sum * phi(z);
   }

   // return Phi(z, mu, sigma) = Gaussian cdf with mean mu and stddev sigma
   public static double Phi(double z, double mu, double sigma) {
       return Phi((z - mu) / sigma);
   } 

   // Compute z such that Phi(z) = y via bisection search
   public static double PhiInverse(double y) {
       return PhiInverse(y, .00000001, -8, 8);
   } 

   // bisection search
   private static double PhiInverse(double y, double delta, double lo, double hi) {
       double mid = lo + (hi - lo) / 2;
       if (hi - lo < delta) return mid;
       if (Phi(mid) > y) return PhiInverse(y, delta, lo, mid);
       else              return PhiInverse(y, delta, mid, hi);
   }



   // test client
   public static void main(String[] args) {
       double k = 159;
       double p = (double)736/(double)3480;
       double mu    = 387*p;
       System.out.println("prob=" + p);
       System.out.println("mean=" + mu);
       double sigma = Math.sqrt(mu*(1-p));
       System.out.println("std=" + sigma);
       System.out.println(Phi(k, mu, sigma));
//       double y = Phi(z);
//       StdOut.println(PhiInverse(y));
   }

}