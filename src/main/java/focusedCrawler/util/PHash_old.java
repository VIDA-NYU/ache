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

// $Id: PHash_old.java 2 2005-09-14 20:34:35Z lbarbosa $

import java.io.*;
import java.util.*;

public class PHash_old
{
  // Constant values for distinguishing different data types on disk
  static private final int stringKey = 1;
  static private final int intKey = 2;

  // Name of directory to store hash values in
  private String dataDirectory;

  // Placeholder object to indicate that an object is not yet
  // loaded
  private Object notLoaded = new Object();

  // Cache of persistenct values read from disk
  private Hashtable cache = new Hashtable();

  // Create a PHash, given the directory name
  public PHash_old( String dataDirectory ) throws IOException {

    this.dataDirectory = dataDirectory;

    // Read in all the keys
    readDir();
  }

  // Read in the keys (but not the values)
  private void readDir() throws IOException {

    // Open the directory
    File dir = new File( dataDirectory );

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
    String files[] = dir.list();
    for (int i=0; i<files.length; ++i) {
      cache.put( decodeKey( files[i] ), notLoaded );
    }
  }

  // Get a raw object, checking for error cases
  synchronized private Object get( String key ) throws IOException {

    // Error if the item isn't there at all
    if (!cache.containsKey( key ))
      throw new IOException( "PHash does not contain \""+key+"\"" );

    // Check to see if the value has been loaded
    // If not, read it off disk into the cache
    Object value = cache.get( key );
    if (value == notLoaded) {
      // Go to disk for the value
      value = readCacheElement( key );

      // Store it in the cache for future reads
      cache.put( key, value );
    }

    return value;
  }

  // Wrapper for integer values; exception if the value
  // isn't actually an integer
  synchronized int getInt( String key ) throws IOException {
    Object o = get( key );
    try {
      return ((Integer)o).intValue();
    } catch( ClassCastException cce ) {
      throw new IOException( o+" is not an integer" );
    }
  }

  // Wrapper for string values; exception if the value
  // isn't actually a string
  synchronized String getString( String key ) throws IOException {
    Object o = get( key );
    try {
      return (String)o;
    } catch( ClassCastException cce ) {
      throw new IOException( o+" is not a string" );
    }
  }

  // Store object in cache and write it to disk as well
  synchronized private void put( String key, Object value )
      throws IOException {
    cache.put( key, value );
    writeCacheElement( key, value );
  }

  // Wrapper for string values
  synchronized public void put( String key, String value )
      throws IOException {
    put( key, (Object)value );
  }

  // Wrapper for integer values
  synchronized public void put( String key, int i )
      throws IOException {
    put( key, new Integer( i ) );
  }

  // Write a value to disk
  synchronized private void writeCacheElement( Object key, Object value )
      throws IOException {

    // Open data stream to the correct file
    String filename = getFilenameForKey( key );
    FileOutputStream fout = new FileOutputStream( filename );
    DataOutputStream dout = new DataOutputStream( fout );

    // Write the type value and then the data
    if (value instanceof String) {
      dout.writeInt( stringKey );
      dout.writeUTF( (String)value );
    } else if (value instanceof Integer) {
      dout.writeInt( intKey );
      dout.writeInt( ((Integer)value).intValue() );
    } else {

      // Error if bad type (this should never happen but we need
      // to handle the case anyway)
      throw new RuntimeException( "Write: Bad value type: "+value );
    }

    // Close the file
    fout.close();
  }

  // Read a value from disk
  synchronized private Object readCacheElement( String key )
      throws IOException {

    // Open data stream to the correct file
    String filename = getFilenameForKey( key );
    FileInputStream fin = new FileInputStream( filename );
    DataInputStream din = new DataInputStream( fin );

    // Read the type first, so that we know what kind of data follows
    int type = din.readInt();

    // Then read the right data
    Object value=null;
    switch( type ) {
      case stringKey:
        value = din.readUTF();
        break;
      case intKey:
        int i = din.readInt();
        value = new Integer( i );
        break;
      default:

        // Error if bad type (this should never happen but we need
        // to handle the case anyway)
        throw new RuntimeException( "Read: Bad value type: "+type );
        //break;
    }

    // Close the file
    fin.close();

    return value;
  }

  // Format for value filename is <datadir>/<keyname>
  private String getFilenameForKey( Object key ) {
    String k = (String)key;
    return dataDirectory+"/"+encodeKey( k );
  }

  private String encodeKey( String key ) {
    String enc = "";
    for (int i=0; i<key.length(); ++i) {
      char c = key.charAt( i );
      if (Character.isLetterOrDigit( c )) {
        enc += c;
      } else {
        String hexEncoding = Integer.toHexString( (int)c );
        enc += ("%"+hexEncoding);
      }
    }

    return enc;
  }

  private String decodeKey( String key ) {
    String dec = "";
    for (int i=0; i<key.length(); ++i) {
      char c = key.charAt( i );
      if (c=='%') {
        String hexEncoding = "";
        hexEncoding += key.charAt( ++i );
        hexEncoding += key.charAt( ++i );
        int charVal = Integer.parseInt( hexEncoding, 16 );
        dec += (char)charVal;
      } else {
        dec += c;
      }
    }
    return dec;
  }
}
