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
package focusedCrawler.link.classifier.builder.wrapper;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Vector;

import org.cyberneko.html.parsers.DOMParser;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import focusedCrawler.util.Page;



/**
 * <p>Title: </p>
 *
 * <p>Description: This class gets words related to links: url, anchor and around</p>
 *
 * <p>Copyright: Copyright (c) 2004</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ParserLink {

  private HashMap map;

  public ParserLink() {
    map = new HashMap();
  }

  public void extractLinks(Page page, String[] features) throws IOException,
      SAXException {

    String content = page.getContent();
    DOMParser parser = new DOMParser();

   parser.parse(new InputSource(new BufferedReader(new StringReader(content))));
   Document doc = parser.getDocument();
  }

  public void extract(String file) throws IOException, SAXException {

    DOMParser parser = new DOMParser();
    parser.parse(file);
    Document doc = parser.getDocument();
    parse(doc);
    Vector words = new Vector();
    NodeList list = doc.getElementsByTagName("a");
    for (int i = 0; i < list.getLength(); i++) {
      parse(list.item(i));
    }

  }

  public void parse(Node node) {
    NamedNodeMap attrs = node.getAttributes();
    String nodeName = node.getNodeName();
    String textBefore = "";
    String url = "";
    if(Node.TEXT_NODE == node.getNodeType()){
      textBefore = node.getNodeValue().trim();
    }
//    if(nodeName.equals("A")){
      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
          Node attr = attrs.item(i);
          String attrName = ( (attr.getNodeName().trim()).toLowerCase());
          String attrValue = ( (attr.getNodeValue().trim()).toLowerCase());
          if(attrName.equals("href")){
            url = attrValue;
          }
          System.out.println("TEST");
        }
      }
//    }

    NodeList children = node.getChildNodes();
      if (children != null) {
        int len = children.getLength();
        for (int i = 0; i < len; i++){
          parse(children.item(i));
        }
      }

  }


  public static void main(String[] args) {
    ParserLink parserlink = new ParserLink();
    try {
      parserlink.extract(args[0]);
    }
    catch (SAXException ex) {
    }
    catch (IOException ex) {
    }
  }
}
