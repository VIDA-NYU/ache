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
package focusedCrawler.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Vector;
import java.io.IOException;

public class Html2Txt
{
	boolean body_found = false;
	boolean in_body = false;
	boolean center = false;
	boolean pre = false;
	String href = "";
	int index = 0;
	boolean script = false;
	private HashMap<String, String> ansiTable;
	
	public Html2Txt(){
		ansiTable = new HashMap<String, String>();
		ansiTable.put("Agrave;","\u00C0");ansiTable.put("Aacute;","\u00C1");ansiTable.put("Acirc;","\u00C2");ansiTable.put("Atilde;","\u00C3");
		ansiTable.put("Auml;","\u00C4");ansiTable.put("Aring;","\u00C5");ansiTable.put("AElig;","\u00C6");ansiTable.put("Ccedil;","\u00C7");
		ansiTable.put("Egrave;","\u00C8");ansiTable.put("Eacute;","\u00C9");ansiTable.put("Ecirc;","\u00CA");ansiTable.put("Euml;","\u00CB");
		ansiTable.put("Igrave;","\u00CC");ansiTable.put("Iacute;","\u00CD");ansiTable.put("Icirc;","\u00CE");ansiTable.put("Iuml;","\u00CF");
		ansiTable.put("ETH;","\u00D0");	ansiTable.put("Ntilde;","\u00D1");ansiTable.put("Ograve;","\u00D2");ansiTable.put("Oacute;","\u00D3");
		ansiTable.put("Ocirc;","\u00D4");ansiTable.put("Otilde;","\u00D5");	ansiTable.put("Ouml;","\u00D6");ansiTable.put("times;","\u00D7");
		ansiTable.put("Oslash;","\u00D8");ansiTable.put("Ugrave;","\u00D9");ansiTable.put("Uacute;","\u00DA");ansiTable.put("Ucirc;","\u00DB");
		ansiTable.put("Uuml;","\u00DC");ansiTable.put("Yacute;","\u00DD");ansiTable.put("THORN;","\u00DE");ansiTable.put("szlig;","\u00DF");
		ansiTable.put("agrave;","\u00E0");ansiTable.put("aacute;","\u00E1");ansiTable.put("acirc;","\u00E2");ansiTable.put("atilde;","\u00E3");
		ansiTable.put("auml;","\u00E4");ansiTable.put("aring;","\u00E5");ansiTable.put("aelig;","\u00E6");ansiTable.put("ccedil;","\u00E7");
		ansiTable.put("egrave;","\u00E8");ansiTable.put("eacute;","\u00E9");ansiTable.put("ecirc;","\u00EA");ansiTable.put("euml;","\u00EB");
		ansiTable.put("igrave;","\u00EC");ansiTable.put("iacute;","\u00ED");ansiTable.put("icirc;","\u00EE");ansiTable.put("iuml;","\u00EF");
		ansiTable.put("eth;","\u00F0");ansiTable.put("ntilde;","\u00F1");ansiTable.put("ograve;","\u00F2");	ansiTable.put("oacute;","\u00F3");
		ansiTable.put("ocirc;","\u00F4");ansiTable.put("otilde;","\u00F5");ansiTable.put("ouml;","\u00F6");ansiTable.put("divide;","\u00F7");
		ansiTable.put("oslash;","\u00F8");ansiTable.put("ugrave;","\u00F9");ansiTable.put("uacute;","\u00FA");ansiTable.put("ucirc;","\u00FB");
		ansiTable.put("uuml;","\u00FC");ansiTable.put("yacute;","\u00FD");ansiTable.put("thorn;","\u00FE");	ansiTable.put("yuml;","\u00FF");
	}
	
	public String[] convert(String source) 	throws IOException{
		return this.convert(source,-1);
	}
	
	public String[] convert(String source, int limit) 	throws IOException{
		body_found = false;
		in_body = false;
		center = false;
		pre = false;
		href = "";
		index = 0;
		script = false;
		if(source.indexOf("html") == -1 && source.indexOf("HTML") == -1 && source.indexOf("<p>") == -1){
			pre = true;
		}
		Vector sentences = new Vector();
		StringBuffer result = new StringBuffer();
		StringBuffer result2 = new StringBuffer();
		//StringReader input = new StringReader(source);

//		try	{
			String text = null;
		    char    c = '\0';
			String temp = "";
			while (index < source.length()){ // Convert until EOF
				boolean newLine = false;
				c = source.charAt(index);
				temp = temp + c;
//				System.out.println(temp);
//				if(temp.indexOf("BR") != -1){
//					System.out.println(temp);
//				}				
				index++;
				text = "";
//				System.out.print((char)c);
				if (c == '<'){ // It's a tag!!
					String currentTag = getTag(source); // Get the rest of the tag
//					if(currentTag == null){
//						return new String[0];
//					}
//					System.out.println(currentTag);
					if(script){
						if(currentTag.indexOf("/script") != -1){
							script = false;
						}	
					}
					if(currentTag.indexOf("<script") != -1 || currentTag.indexOf("<SCRIPT") != -1){
						script = true;
					}
					text = convertTag(currentTag.toLowerCase());
					if(text.indexOf("\n") != -1){
						newLine = true;
					}
				}
				else if (c == '&'){
					String specialchar = getSpecial(source);
//					System.out.println(specialchar);
					String unicode = ansiTable.get(specialchar);
					if(unicode == null && specialchar != null && specialchar.startsWith("#") && specialchar.endsWith(";")){
						if(specialchar.startsWith("#x")){
							specialchar = specialchar.substring(2,specialchar.length()-1);
							int hex = Integer.parseInt("00"+specialchar,16);
							char uni = (char)hex;
							unicode =  uni + "";
						}else{
							int hex = Integer.parseInt(specialchar.substring(1,specialchar.length()-1));
							char uni = (char)hex;
							unicode =  uni + "";
						}
					}
					if(unicode != null){
						text = unicode;
					}
					else if (specialchar.equals("lt;") || specialchar.equals("#60") || specialchar.equals("laquo;"))
						text = "< ";
					else if (specialchar.equals("gt;") || specialchar.equals("#62") || specialchar.equals("raquo;"))
						text = " >";
					else if (specialchar.equals("amp;") || specialchar.equals("#38"))
						text = "&";
					else if (specialchar.equals("nbsp;"))
						text = " ";
					else if (specialchar.equals("quot;") || specialchar.equals("#34") || specialchar.equals("&ldquo;")
							|| specialchar.equals("&rdquo;"))
						text = "\"";
					else if (specialchar.equals("copy;") || specialchar.equals("#169"))
						text = "[Copyright]";
					else if (specialchar.equals("reg;") || specialchar.equals("#174"))
						text = "[Registered]";
					else if (specialchar.equals("trade;") || specialchar.equals("#153"))
						text = "[Trademark]";
					else
						text = "&" + specialchar;
				}
				else if (!pre && Character.isWhitespace((char)c)){
					StringBuffer s = in_body ? result : result2;
					if (s.length() > 0 && Character.isWhitespace(s.charAt(s.length()-1)))
						text = "";
					else text = " ";
				}
				else {
					text = "" + (char)c;
					if(text.indexOf("\n") != -1){
						newLine = true;
					}
				}
				StringBuffer s = in_body ? result : result2;
//				System.out.print(" " + s.toString());
				if(!script){
					s.append(text);	
				}
				if(newLine && !s.toString().equals("")){
					sentences.add(s.toString().trim());
					result = new StringBuffer();
					result2 = new StringBuffer();
//					if(limit != -1 && sentences.size() > limit){
//						break;
//					}
				}
				if(newLine){
//					System.out.print(s.toString());	
				}
			}
//		}
//		catch (Exception e)	{
//			e.printStackTrace();
//			throw e;
//		}
//		StringBuffer s = body_found ? result : result2;
//		return s.toString().trim();
		String[] finalResult = new String[sentences.size()];
		sentences.toArray(finalResult);
		return finalResult;
	}

	String getTag(String source) throws IOException	{
		StringBuffer result = new StringBuffer();
		int level = 1;
		result.append('<');
		boolean emptyStr = true;
		int quote = 0;
		boolean comment = false;
		int commentLevel = -1;
		int count = 0;
		while (level > 0 && index < source.length()){
			char c = source.charAt(index);
			count++;
//			if(count > 10000){
//				return null;
//			}
//			System.out.print((char)c);
//			System.out.println(index);
//			System.out.println(count);
//			System.out.println(source.length());
			if(comment){
				result.append((char)c);
				index++;
				if((result.toString().endsWith(">") && level == commentLevel) || result.toString().endsWith("-->") || result.toString().endsWith("/head>") || result.toString().endsWith("/style>")){
					break;
				}
			}else{
				if(quote == 0 && c == '"'){
					quote++;
				}else{
					if(quote == 1 && c == '"'){
						quote--;	
					}
				}
				index++;
//				if(script){
					if(result.toString().indexOf("/script") != -1 || 
							result.toString().indexOf("/SCRIPT") != -1){
						script = false;
						break;
					}	
//				}
				if (index >= source.length()) break; // EOF
				result.append((char)c);
				if((c == '<' || c == '>') && emptyStr){
					index--;
					break;
				}
				if (c == '<' && quote==0){ 
					level++; 
				}else if (c == '>'){
					level--;
				}
				if(c != ' '){
					emptyStr = false;
				}
			}
//			if(result.toString().indexOf("span") != -1){
//				System.out.println("TEST");
//			}
			if(result.toString().startsWith("<style")){
				comment = true;
			}
			if(result.toString().length() == 4){
				if(result.toString().startsWith("<!--")){
					comment = true;
					commentLevel = level;
				}
			}
		}
		return result.toString();

	}

	String getSpecial(String source) throws IOException	{
		StringBuffer result = new StringBuffer();
		int indexTemp = index;
		//r.mark(1);//Mark the present position in the stream
		

		char c = source.charAt(index);
		index++;
		
		while (Character.isLetter((char)c) || c == '#' || Character.isDigit((char)c)){
			result.append((char)c);
			indexTemp = index;
			c = source.charAt(index);
			index++;
		}
		if (c == ';') result.append(';');
		else index = indexTemp;

		return result.toString();
	}

	boolean isTag(String s1, String s2)	{
		s1 = s1.toLowerCase();
		String t1 = "<" + s2.toLowerCase() + ">";
		String t2 = "<" + s2.toLowerCase() + " ";

		return s1.startsWith(t1) || s1.startsWith(t2);
	}

	String convertTag(String t) throws IOException	{
		String result = "";

		if (isTag(t,"body"))
		{ in_body = true; body_found = true; }
		else if (isTag(t,"/body"))
		{ in_body = false; result = ""; }
		else if (isTag(t,"center"))
		{ result = ""; center = true; }
		else if (isTag(t,"/center"))
		{ result = ""; center = false; }
		else if (isTag(t,"pre"))
		{ result = ""; pre = true; }
		else if (isTag(t,"/pre"))
		{ result = ""; pre = false; }
		else if (isTag(t,"p") || isTag(t,"/p"))
		result = "\n";
		else if (isTag(t,"br") || isTag(t,"br/"))
		result = "\n";
		else if (isTag(t,"h1") || isTag(t,"h2") ||
isTag(t,"h3") ||isTag(t,"h4") || isTag(t,"h5") || isTag(t,"h6") ||
isTag(t,"h7"))
		result = " ";
		else if (isTag(t,"/h1") || isTag(t,"/h2") ||
isTag(t,"/h3") ||isTag(t,"/h4") || isTag(t,"/h5") || isTag(t,"/h6") ||
isTag(t,"/h7"))
		result = "*";
		else if (isTag(t,"/dl"))
		result = "";
		else if (isTag(t,"dd"))
		result = "  * ";
		else if (isTag(t,"dt") || isTag(t,"tr"))
		result = "\n";
		else if (isTag(t,"/table") || isTag(t,"/div"))
		result = "\n";
		else if (isTag(t,"li"))
		result = "\n  ";
		else if (isTag(t,"/li"))
		result = "\n";
		else if (isTag(t,"/ul"))
		result = "";
		else if (isTag(t,"/ol"))
		result = "";
		else if (isTag(t,"td"))
		result = " ";
		else if (isTag(t,"hr"))
		result = "\n_________________________________________";
		else if (isTag(t,"table"))
		result = "";
		else if (isTag(t,"/table"))
		result = "";
		else if (isTag(t,"form"))
		result = "";
		else if (isTag(t,"/form"))
		result = "";
//		else if (isTag(t,"b"))
//		result = "*";
//		else if (isTag(t,"/b"))
//		result = "*";
		else if (isTag(t,"i"))
		result = "\"";
		else if (isTag(t,"/i"))
		result = "\"";
		else if (isTag(t,"img"))
		{
		int idx = t.indexOf("alt=\"");
		if (idx != -1)
		{
		idx += 5;
		int idx2 = t.indexOf("\"",idx);
		//result = t.substring(idx,idx2);
		}
		}
//		else if (isTag(t,"a"))
//		{
//		int idx = t.indexOf("href=\"");
//		if (idx != -1)
//		{
//		idx += 6;
//		int idx2 = t.indexOf("\"",idx);
//		href = t.substring(idx,idx2);
//		}
//		else
//		{
//		href = "";
//		}
//		}
//		else if (isTag(t,"/a"))
//		{
//		if (href.length() > 0)
//		{
//		result = " [ " + href + " ]";
//		href = "";
//		}
//		}

		return result;
	}

	public static void main(String args[]) throws Exception	{
		FileInputStream fis = null;
		String s = null;

		try{
//			File file = new File(args[0]);
//			File[] files = file.listFiles();
//			for (int i = 0; i < files.length; i++) {
			File file = new File(args[0]);
			fis = new FileInputStream(file);
			byte buf[] = new byte[fis.available()];
				//bytes that can be read from this file input stream without blocking
		
			fis.read(buf);
			fis.close();
			fis = null;
			s = new String(buf);
			System.out.println("--" + file.toString());
				
			Html2Txt h = new Html2Txt();
//				OutputStream fout= new FileOutputStream(args[1] + files[i].getName());
//				OutputStream bout= new BufferedOutputStream(fout);
//				OutputStreamWriter outputLNS = new OutputStreamWriter(bout);
				String[] sentences = h.convert(s); 
				for (int j = 0; j < sentences.length; j++) {
					if(!sentences[j].equals("")){
//						outputLNS.write(sentences[j] + "\n");
						System.out.println(sentences[j]);	
					}
				}
////				fout.close();
////				bout.close();
//				outputLNS.close();
//			}
			
		}
		catch (Exception e)
		{
		if (fis != null) fis.close();
		throw e;
		}
	}
}