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

public class ChiSquare {

	private double ab;
	private double aNotB;
	private double notAB;
	private double notAnotB;
	
	
	
	public ChiSquare(double ab, double aNotB, double notAB, double notAnotB){
		this.ab = ab;
		this.aNotB = aNotB;
		this.notAB = notAB;
		this.notAnotB = notAnotB;
	}

	public double calculateValue(){
		double total = ab + aNotB + notAB + notAnotB;
		double numerator = total*Math.pow((ab*notAnotB) - (aNotB*notAB),2);
		double denominator = (ab+aNotB)*(ab+notAB)*(aNotB+notAnotB)*(notAB+notAnotB);
		return numerator/denominator;
	}

	public static boolean isSignificant(double ab, double aNotB, double notAB, double notAnotB){
		if(ab <= 5 || aNotB <= 5 || notAB <= 5 || notAnotB <= 5){
			return false;
		}
		double total = ab + aNotB + notAB + notAnotB;
		double numerator = total*Math.pow((ab*notAnotB) - (aNotB*notAB),2);
		double denominator = (ab+aNotB)*(ab+notAB)*(aNotB+notAnotB)*(notAB+notAnotB);
		return (numerator/denominator) > 10;
	}
	
	public static void main(String[] args) {
		ChiSquare cs = new ChiSquare(8,4667,15820,14287173);
		System.out.println("VALUE:" + cs.calculateValue());
		
	}
	
}

