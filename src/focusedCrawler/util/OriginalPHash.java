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

//import java.io.DataInputStream;
//import java.io
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileNotFoundException;

import java.util.Hashtable;
import java.util.Vector;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.io.DataOutputStream;

public class OriginalPHash {
        // Constant values for distinguishing different data types on disk

  static private final int stringKey = 1;

  static private final int intKey = 2;

  private static final String SIZE = "###SIZE###";

  private int MAX_SIZE_CACHE = 1;

  private int numOfElements;

  private int MAX_DIR_SIZE = 28000;

  private int countFilesDir = 0;

  private int numOfDirs = 0;

  private String currentDataDirectory;

  // Name of directory to store hash values in

  private String dataDirectory;

  private Hashtable cache = new Hashtable();

  private String[] keys = null;

  private boolean insertURL = false;

  private boolean saveEntry = true;

  // Create a PHash, given the directory name
  public OriginalPHash( String dataDirectory, int maxCacheSize, boolean insertURL) throws IOException {

    this.dataDirectory = dataDirectory;
    this.currentDataDirectory = dataDirectory;
    MAX_SIZE_CACHE = maxCacheSize;
    keys = new String[MAX_SIZE_CACHE];
    System.out.println("###"+keys.length);
    numOfElements = 0;
    // Read in all the keys
    this.insertURL = insertURL;
   // readDir();
  }


  public OriginalPHash( String dataDirectory, int maxCacheSize, boolean insertURL, String id) throws IOException, ClassNotFoundException {
    this( dataDirectory,  maxCacheSize, insertURL);
    System.out.println(">>> DIR:" + dataDirectory);
    File root = new File(dataDirectory).getParentFile();
    System.out.println(">>> ROOT:" + root.listFiles().length);
    countFilesDir = MAX_DIR_SIZE;
    numOfDirs = root.listFiles().length;
    File phashPers = new File(dataDirectory+"/"+id);
    if(phashPers.exists()){
      System.out.println(">>>>>>>>>>>FILE EXISTS");
      FileInputStream fin = new FileInputStream( phashPers );
      ObjectInputStream ois = new ObjectInputStream(fin);
      cache = (Hashtable)ois.readObject();
    }else{
      readDir();
    }
  }

  public void notSaveEntry(){
    this.saveEntry = false;
  }

  // Create a PHash, given the directory name
  public OriginalPHash(String dataDirectory, int maxCacheSize) throws IOException {

    this.dataDirectory = dataDirectory;
    this.currentDataDirectory = dataDirectory;
    MAX_SIZE_CACHE = maxCacheSize;
    keys = new String[MAX_SIZE_CACHE];
    System.out.println("###"+keys.length);
    numOfElements = 0;

  }

  public OriginalPHash(String dir) throws IOException {
    this(dir, 1000);
  }

  public String getDirectory() {
    return dataDirectory;
  }

  public void setNumOfDirs(int num){
    this.numOfDirs = num;
  }

  // Read in the keys (but not the values)

  private void readDir() throws IOException {

    System.out.println(">>>LOADING CACHE..." + dataDirectory);

    // Open the directory
    File dir = new File( dataDirectory );
    String root = dataDirectory.substring(0,dataDirectory.lastIndexOf("/"));
    File rootDir = new File(root);
    numOfDirs = rootDir.listFiles().length;
    System.out.println(">>>NUM_OF_DIRS:"+numOfDirs);
    currentDataDirectory = (rootDir.listFiles()[rootDir.listFiles().length-1]).getAbsolutePath();
    System.out.println(">>>CURRENT DIR:" + currentDataDirectory.toString());
    // Create it if it's not there
    if (!dir.exists()) {
      try {
        dir.mkdir();
      } catch( SecurityException se ) {
        throw new IOException( "Cannot create dir "+dataDirectory );
      }
    }

    // Error if it's not a directory or still doesn't exist
    if (!dir.exists() || !dir.isDirectory())
     throw new IOException( dataDirectory+" is not a directory" );
    // Read all the keys in the directory, and store them in the
    // cache with a value indicating that the values aren't yet loaded
    File[] files = dir.listFiles();
    System.out.println(">>>DIR:" + dir.toString());
    System.out.println("FILE SIZE:"+files.length);
    int count = 0;
    for (int i=0; i < files.length && count < MAX_SIZE_CACHE; ++i) {
      System.out.println("FILES:" + files[i].toString());
        String[] files1Level = files[i].list();
        count = count + files1Level.length;

        for (int j = 0; j < files1Level.length && count < MAX_SIZE_CACHE; j++) {
          try{
          String key = decodeKey( files[i].getName() + files1Level[j]);
//          System.out.println("key : " + key);
          if(key == null){
            continue;
          }
          Object value = get(key);
//          System.out.println("value : " + value);
          if(value != null){
            cache.put(key, value);
          }
        }catch(java.lang.NumberFormatException ex){

        }
      }
    }
    System.out.println(">>> CACHE SIZE" + cache.size());
    System.out.println(">>> TOTAL" + count);
  }


  // Get a raw object, checking for error cases

  public Object get(String key) throws IOException {

    Object value = cache.get(key);
    if (value == null && saveEntry){
      try {
        value = readCacheElement(key);
      }
      catch (FileNotFoundException ex) {
        value = null;
      }
      catch (ClassNotFoundException ex) {
        throw new IOException(ex.getMessage());
      }catch(java.lang.NumberFormatException ex){

      }
    }
   return value;
  }


  // Store object in cache and write it to disk as well

  private void putSize( String key, Object value )
      throws IOException {
    cache.put( key, value );
    writeCacheElement( key, value );
  }


  // Store object in cache and write it to disk as well

  public synchronized void put(String key, Object value)
      throws IOException {

      if(numOfElements == MAX_SIZE_CACHE){
        numOfElements = 0;
      }
      String oldKey = keys[numOfElements];
      if(oldKey != null){
        cache.remove(oldKey);
      }
      keys[numOfElements] = key;
      numOfElements++;
      cache.put(key, value);

      if(saveEntry){
        if(countFilesDir >= MAX_DIR_SIZE){
          currentDataDirectory = dataDirectory + numOfDirs;
          File tempDir = new File(currentDataDirectory);
          tempDir.mkdir();
          numOfDirs++;
//        System.out.println("AVERAGE PHASH:"+(double)((double)totalWrite/(double)countFilesDir));
          countFilesDir = 0;
          totalWrite = 0;
        }
        writeCacheElement( key, value );
      }

  }



  // Wrapper for string values

  public void put( String key, String value )
      throws IOException {
    put( key, (Object)value );
  }



  // Wrapper for integer values
  public void put( String key, int i )
      throws IOException {
    put( key, new Integer( i ) );
  }



  // Write a value to disk

//  synchronized private void writeCacheElement( Object key, Object value )
//
//      throws IOException {
//
//    // Open data stream to the correct file
//    String filename = getFilenameForKey( key );
//    FileOutputStream fout = new FileOutputStream( filename );
//    DataOutputStream dout = new DataOutputStream( fout );
//
//    // Write the type value and then the data
//    if (value instanceof String) {
//      dout.writeInt( stringKey );
//      dout.writeUTF( (String)value );
//    } else if (value instanceof Integer) {
//      dout.writeInt( intKey );
//      dout.writeInt( ((Integer)value).intValue() );
//    } else {
//      // Error if bad type (this should never happen but we need
//      // to handle the case anyway)
//      throw new RuntimeException( "Write: Bad value type: "+value );
//    }
//    // Close the file
//    fout.close();
//  }



  // Read a value from disk

//  synchronized private Object readCacheElement( String key ) throws
//      FileNotFoundException, IOException {
//
//    // Open data stream to the correct file
//    String filename = getFilenameForKey( key );
//    FileInputStream fin = new FileInputStream( filename );
//    DataInputStream din = new DataInputStream( fin );
//    // Read the type first, so that we know what kind of data follows
//    int type = din.readInt();
//
//    // Then read the right data
//    Object value=null;
//    switch( type ) {
//      case stringKey:
//        value = din.readUTF();
//        break;
//
//      case intKey:
//        int i = din.readInt();
//        value = new Integer( i );
//        break;
//
//      default:
//
//        // Error if bad type (this should never happen but we need
//        // to handle the case anyway)
//        throw new RuntimeException( "Read: Bad value type: "+type );
//        //break;
//    }
//    // Close the file
//    fin.close();
//    return value;
//  }



  // Format for value filename is <datadir>/<keyname>

  public String getFilenameForKey(Object key) throws MalformedURLException {
    return this.getFilenameForKey(currentDataDirectory,key, false);
  }

  public String getFilenameForKey(String dir, Object key, boolean isPut) throws MalformedURLException {
    String result = null;
    String k = (String)key;

    if(insertURL){
      URL url = new URL(k);
      String host = url.getHost();
      String file = url.getFile();
      if(file.equals("")){
        file = "/";
      }
      File dirTemp = new File(dir + "/" +encodeKey( "http://"+host ));
      if(!dirTemp.exists()){
        if(isPut){
          dirTemp.mkdir();
          countFilesDir++;
//          System.out.println(">>>>>>>>>>" + dir +"COUNTER:"+countFilesDir);
        }else{
          return null;
        }
      }
      result = dir + "/" + encodeKey( "http://"+host) + "/" + encodeKey(file);
    }else{
      result = dir+ "/" +encodeKey( k );
    }
    return result;
  }



  public static String encodeKey( String key ) {

//    String enc = "";
//    for (int i=0; i<key.length(); ++i) {
//      char c = key.charAt( i );
//      if (Character.isLetterOrDigit( c )) {
//        enc += c;
//      } else {
//        String hexEncoding = Integer.toHexString( (int)c );
//        enc += ("%"+hexEncoding);
//      }
//    }
    return URLEncoder.encode(key);
  }

  public static String decodeKey( String key )  {

    String dec = "";
    try{
      for (int i=0; i<key.length(); ++i) {
        char c = key.charAt( i );
        if (c=='%') {
          String hexEncoding = "";
          hexEncoding += key.charAt( ++i );
          char t = key.charAt( ++i );
          if (t != '%')
                  hexEncoding += t;
          else i--;
          int charVal = Integer.parseInt( hexEncoding, 16 );
          dec += (char)charVal;
        } else {
          dec += c;
        }
      }
    }catch(java.lang.NumberFormatException ex){
      System.out.println(">>>KEY"+key);
      ex.printStackTrace();
      return null;
    }catch(java.lang.StringIndexOutOfBoundsException ex){
      System.out.println(">>>KEY"+key);
      ex.printStackTrace();
      return null;
    }

    return dec;
  }


  public void updateCache(Hashtable newCache){
    this.cache = newCache;
  }


  public Hashtable getCache(){
    return this.cache;
  }



  public int size() throws IOException {

    Integer size = (Integer)get(SIZE);
    if(size == null){
      size = new Integer(0);
      putSize(SIZE,size);
    }
    return size.intValue();
  }

  public void incrementSize()throws IOException{
    int size = size();
    size++;
    putSize(SIZE,new Integer(size));
  }


  public Iterator getKeys(){
    Iterator keys = null;
    System.out.println("CACHE SIZE:"+cache.size());
    if(cache.size() > 0){
      keys = cache.keySet().iterator();
    }else{
      File dir = new File( dataDirectory );
      String[] files = dir.list();
      Vector tempList = new Vector();
      for (int i = 0; i < tempList.size(); i++) {
        tempList.add(files[i]);
      }
      keys = tempList.iterator();
    }
    return keys;
  }

  long totalWrite = 0;

  // Write a value to disk
    public void writeCacheElement( Object key, Object value )
        throws IOException {
      long init = System.currentTimeMillis();
      // Open data stream to the correct file
      String filename = getFilenameForKey(currentDataDirectory, (String)key,true);
      FileOutputStream fout = new FileOutputStream( filename );
      ObjectOutputStream oous = new ObjectOutputStream(fout);
      oous.writeObject(value);
      fout.close();
      oous.close();
      totalWrite = totalWrite + (System.currentTimeMillis() - init);
    }

  // Read a value from disk
    private Object readCacheElement( String key ) throws
        FileNotFoundException, IOException , ClassNotFoundException {

      // Open data stream to the correct file
      String k = (String)key;
      File file = null;
      String filename = getFilenameForKey(dataDirectory, key,false);
      if(filename != null){
        file = new File(filename);
      }else{
          for (int i = 0; i < numOfDirs; i++) {
            String dirTemp = dataDirectory + i;
            filename = getFilenameForKey(dirTemp,key,false);
//            System.out.println(">>>>>>>>>>>>>FILENAME"+filename);
            if(filename == null){
              continue;
            }
            file = new File(filename);
            if(file.exists()){
              break;
            }
          }
      }
//      System.out.println(">>>>>>>>>>>>>FILENAME"+filename);
      if(filename == null){
        return null;
      }else{
        FileInputStream fin = new FileInputStream( filename );
        ObjectInputStream ois = new ObjectInputStream(fin);
        Object value = ois.readObject();
        return value;
      }
  }

  public static void main(String[] args) {
    try {
      File file = new File(args[0]);
      File[] files = file.listFiles();
      for (int i = 0; i < files.length; i++) {
        FileInputStream fin = new FileInputStream(files[i]);
        ObjectInputStream ois = new ObjectInputStream(fin);
        Object value = ois.readObject();
        System.out.println("FILE:" + files[i].toString());
        System.out.println("VALUE: " + value);
//        FileOutputStream fout = new FileOutputStream("page_"+i,false);
//        DataOutputStream dout = new DataOutputStream(fout);
//        dout.writeBytes((String)value);
//        dout.close();
//        fout.close();
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }

  }


}



