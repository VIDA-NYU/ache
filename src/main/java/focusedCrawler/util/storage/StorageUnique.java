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
 package focusedCrawler.util.storage;



import java.sql.PreparedStatement;

import java.sql.Statement;

import java.sql.ResultSet;

import java.sql.Connection;

import java.sql.SQLException;



import java.util.Enumeration;

import java.util.Vector;

import java.util.HashMap;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;

public class StorageUnique extends StorageDefault {



    private Connection con = null;



    private PreparedStatement pstmt_code,

                              pstmt_desc,

                              pstmt_ins;





    private Statement         stmt_code_array;

    private StringBuffer      buf_code_array;

    private HashMap           map_code_array;



    private String  desc    =  "desc",

                    code    =  "code",

                    table   =  "table";

    private int maxDescSize;



    public StorageUnique() throws StorageException{

    }



    public void setConnection(Connection _con) {

        this.con = _con;

    }



    public void setTable(String _table) throws StorageException{

        if(_table==null) throw new StorageException("Must have a code field !");

        this.table = _table;

        updatePstmt();

    }



    public void setDescField(String _desc, int _maxDescSize) throws StorageException{

        if(_desc==null) throw new StorageException("Must have a desc field !");

        this.desc = _desc;

        this.maxDescSize = _maxDescSize;

        updatePstmt();

    }



    public void setCodeField(String _code) throws StorageException{

        if(_code==null) throw new StorageException("Must have a table !");

        this.code = _code;

        updatePstmt();

    }



    public Connection getConnection() {

        return con;

    }



    public int getMax() throws StorageException {

        int result = 0;

        ResultSet rs = null;

        Statement stmt = null;

        try{

            stmt = con.createStatement();

            rs = stmt.executeQuery("SELECT MAX("+code+") FROM "+table);

            if( rs.next() ) {

               result = rs.getInt(1);

            }

        }

        catch(SQLException e){

            throw new StorageException(e.getMessage());

        }

        finally {

            try {

                if (rs != null) {

                    rs.close();

                } //if

            } catch (SQLException e) {

                throw new StorageException(e.getMessage());

            }

            try {

                if (stmt != null) {

                    stmt.close();

                } //if

            } catch (SQLException e) {

                throw new StorageException(e.getMessage());

            }

        }

        return result;

    }





    private void updatePstmt() throws StorageException{

        try{

            if( pstmt_code != null ) {

                pstmt_code.close();

                pstmt_code = null;

                pstmt_code = con.prepareStatement("SELECT "+code+" FROM "+table+" WHERE "+desc+" = ?");

            }

            if (stmt_code_array != null) {

                stmt_code_array.close();

                stmt_code_array=null;

                stmt_code_array = con.createStatement();

            }

            if( pstmt_desc != null ) {

                pstmt_desc.close();

                pstmt_desc = null;

                pstmt_desc = con.prepareStatement("SELECT "+desc+" FROM "+table+" WHERE "+code+" = ?");

            }

            if( pstmt_ins != null ) {

                pstmt_ins.close();

                pstmt_ins = null;

                pstmt_ins  = con.prepareStatement("INSERT INTO "+table+" ("+code+", "+desc+") VALUES (?,?)");

            }

        }catch(SQLException e){

            throw new StorageException(e.getMessage());

        }

    }



    public Object insert(Object obj) throws StorageException, CommunicationException{

        if(obj instanceof UniqueRegister){

            UniqueRegister register = (UniqueRegister) obj;

            // Testando o valor maximo do registro

            if (register.getDesc().length() > maxDescSize ) {

                throw new StorageException("Descricao muito longa(max = " + maxDescSize + "): >>" + obj.toString());

            } //if

            try{

                if (pstmt_ins == null) {

                    pstmt_ins  = con.prepareStatement("INSERT INTO "+table+" ("+code+", "+desc+") VALUES (?,?)");

                }

                pstmt_ins.setInt(1,register.getCode());

                pstmt_ins.setString(2,register.getDesc());

                pstmt_ins.executeUpdate();

                pstmt_ins.clearParameters();

            }

            catch(SQLException e){

                e.printStackTrace();

                throw new StorageException(e.getMessage() + ">>" + obj.toString());

            }

        }

        else{

            throw new StorageException("Method must receive a com.radix.indexing.codeserver.uitl.UniqueRegister.");

        }

        return null;

    }



    public Object select(Object obj) throws StorageException,DataNotFoundException, CommunicationException{

        if(obj instanceof String){//quer o código

            return new Integer(getCode((String)obj));

        }else if(obj instanceof Integer){

            return getDesc(((Integer)obj).intValue());

        }else{

            throw new StorageException("Method must receive a String or a Integer.");

        }

    }



    public Object[] selectArray(Object[] obj) throws StorageException,DataNotFoundException, CommunicationException{

        if (obj.length==0) return new Object[0];

        if (obj[0] instanceof String) {

            return getCodeArray(obj);

        } else if (obj[0] instanceof Integer) {

            return getDescArray(obj);

        } else {

            throw new StorageException("Method must receive a String or a Integer.");

        }

    }



    public Enumeration selectEnumeration(Object obj) throws StorageException,DataNotFoundException, CommunicationException {

        ResultSet rs = null;

        Statement stmt = null;

        try{

            stmt = con.createStatement();

            rs = stmt.executeQuery("SELECT * FROM "+table);

            Vector v = new Vector();

            while( rs.next() ) {

                v.addElement(new UniqueRegister(rs.getString(2),rs.getInt(1)));

            }

            return v.elements();

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        finally {

            try {

                if( rs != null ) {

                    rs.close();

                    rs = null;

                }

            }

            catch (SQLException e) {

                e.printStackTrace();

            }

            try {

                if( stmt != null ) {

                    stmt.close();

                    stmt = null;

                }

            }

            catch (SQLException e) {

                e.printStackTrace();

            }

        }

    }



    private final static int MAX_SQL_LEN = 100;

    private Object[] getCodeArray(Object[] descKeys) throws StorageException, CommunicationException  {

        int len = descKeys.length;

        Object[] result = new Object[len];

        for(int i=0;i<len;i++) {

            try {

                result[i] = new Integer(getCode((String)descKeys[i]));

            } catch(DataNotFoundException e) {

                result[i] = null;

            }



        }

//        for(int i=0;i<len;) {

//            int s = Math.min(MAX_SQL_LEN, len-i);

//            Object[] keys2 = new Object[s];

//            System.arraycopy(descKeys, i, keys2, 0, s);

//            Object[] result2 = getCodeArrayInternal(keys2);

//            System.arraycopy(result2, 0, result, i, s);

//            i+=s;

//        }

        return result;

    }



    private Object[] getCodeArrayInternal(Object[] descKeys) throws StorageException {

        ResultSet rs = null;

        PreparedStatement pstmt = null;

        int len=descKeys.length;

        int found=0;

        try {

            if (stmt_code_array == null) {

                stmt_code_array = con.createStatement();

                map_code_array = new HashMap();

                buf_code_array = new StringBuffer();

            }

            Object[] result = new Object[len];

            if (len==0) return result;



            //

            // make the SQL

            //

            buf_code_array.setLength(0);

            buf_code_array.append("SELECT ").append(code).append(", ");

            buf_code_array.append(desc).append(" FROM ").append(table);

            buf_code_array.append(" WHERE ").append(desc).append(" IN (?");

            for(int i=1;i<len;i++) {

                buf_code_array.append(", ?");

            }

            buf_code_array.append(")");



            pstmt = con.prepareStatement(buf_code_array.toString());

            for(int i=0;i<len;i++) {

                pstmt.setString(i+1, (String) descKeys[i]);

            }

            rs = pstmt.executeQuery();

            map_code_array.clear();

            while(rs.next()) {

                int rcode = rs.getInt(1);

                String rdesc = rs.getString(2);

                map_code_array.put(rdesc, new Integer(rcode));

                found++;

            }



            for(int i=len-1;i>=0;i--) {

                result[i] = map_code_array.get(descKeys[i]);

            }

            return result;

        } catch(SQLException e) {

            throw new StorageException(e.getMessage(), e);

        } finally {

//            util.Log.log("Storageunique", "getCodeArray", "len="+len+" found="+found);

            if (rs != null) {

                try {rs.close(); }

                catch(SQLException e) {

                    throw new StorageException(e.getMessage(), e);

                }

            }

            if (pstmt != null) {

                try {pstmt.close();}

                catch(SQLException e) {

                    throw new StorageException(e.getMessage(), e);

                }

            }

        }



    }



    private Object[] getDescArray(Object[] code) throws StorageException, CommunicationException {

        int len = code.length;

        Object[] result = new Object[len];

        for(int i=len-1;i>=0;i--) {

            try {

                result[i] = select(code[i]);

            } catch(DataNotFoundException e) {

//                result[i] = null;

            }

        }

        return result;

    }



    /**

     * Return a url given a code

     * @return  int code,

     *          throws DataNotFoundStorageExcepiton otherwise

     *

     */

    private int getCode(String _desc)throws StorageException,DataNotFoundException{

        ResultSet rs = null;

        try{

            if (pstmt_code == null) {

                pstmt_code = con.prepareStatement("SELECT "+code+" FROM "+table+" WHERE "+desc+" = ?");

            }

            pstmt_code.setString(1,_desc);

            rs = pstmt_code.executeQuery();

            if( rs.next() ){

                return rs.getInt(1);

            }

            else {

                throw new DataNotFoundException("Code not found for desc '"+_desc+"'.");

            }

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        finally {

            if(rs != null) {

                try {

                    rs.close();

                    rs = null;

                }

                catch( SQLException sqle ) {

                    sqle.printStackTrace();

                    throw new StorageException(sqle.getMessage());

                }

            }

        }

    }





    /**

     * Returns a url given a code

     * @return  String url

     *          null   If the url could not be found

     */

    private String getDesc(int _code) throws StorageException,DataNotFoundException{

        ResultSet rs = null;

        try{

            if (pstmt_desc == null) {

                pstmt_desc = con.prepareStatement("SELECT "+desc+" FROM "+table+" WHERE "+code+" = ?");

            }

            pstmt_desc.setInt(1,_code);

            rs = pstmt_desc.executeQuery();

            if(rs.next()) {

                return rs.getString(1);

            }

            else {

                throw new DataNotFoundException("String not found for code '"+_code+"'.");

            }

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        finally {

            if(rs != null) {

                try {

                    rs.close();

                    rs = null;

                }

                catch( SQLException sqle ) {

                    sqle.printStackTrace();

                    throw new StorageException(sqle.getMessage());

                }

            }

        }

    }



    /**

     * Delete all registers of the table

     */

    public Object remove(Object obj) throws StorageException , CommunicationException{

        Statement stmt = null;

        try{

            stmt=con.createStatement();

            stmt.executeUpdate("DELETE FROM "+table);

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        finally {

            if (stmt != null) {

                try {

                    stmt.close();

                }

                catch (Exception ex) {

                    ex.printStackTrace();

                    throw new StorageException(ex.getMessage());

                }

            }

        }

        return null;

    }



    public Object commit(Object obj) throws StorageException , CommunicationException{

        try{

            con.commit();

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        return null;

    }



    public Object rollback(Object obj) throws StorageException, CommunicationException {

        try{

            con.rollback();

        }

        catch(SQLException e){

            e.printStackTrace();

            throw new StorageException(e.getMessage());

        }

        return null;

    }



    public Object finalize(Object obj) throws StorageException , CommunicationException{

        if (pstmt_code != null) {

            try {

                pstmt_code.close();

            } catch (SQLException e) { e.printStackTrace();}

            pstmt_code=null;

        }



        if (stmt_code_array != null) {

            try {

                stmt_code_array.close();

            }

            catch (Exception ex) {

                ex.printStackTrace();

            }

            stmt_code_array=null;

        }



        if (pstmt_desc!=null) {

            try {

                pstmt_desc.close();

            } catch (SQLException e) { e.printStackTrace();}

            pstmt_desc=null;

        }



        if (pstmt_ins != null) {

            try {

                pstmt_ins.close();

            } catch (SQLException e) {e.printStackTrace();}

            pstmt_ins=null;

        }

        return null;

    }

}