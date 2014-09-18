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



import java.sql.SQLException;

import java.sql.Connection;

import java.sql.ResultSet;

import java.sql.Statement;

import java.sql.PreparedStatement;

import java.util.Vector;

import java.util.Enumeration;

import focusedCrawler.util.DataNotFoundException;
import focusedCrawler.util.distribution.CommunicationException;
import focusedCrawler.util.storage.StorageDefault;
import focusedCrawler.util.storage.StorageException;













public class GenericStorageItemStorage extends StorageDefault {



    private Connection con = null;



	private String table;

	private String fieldList;

	private String fieldSpotList;

	private String[] fields;

	private String codeField;



    private PreparedStatement pstmt_insert;

    private PreparedStatement pstmt_update;

    private Statement stmt_remove;

    private Statement stmt;



    public GenericStorageItemStorage() {

        super();

    } //PageItemStorage





    protected void setConnection(Connection con){

        this.con = con;

    }



	public String getTable() {

		return table;

	}



	public void setTable(String _table) {

		System.out.println("TABLE_NAME::"+_table);

		table = _table;

	}





	public void setFields(String[] _fields){

		fields = _fields;

	}



    protected Connection getConnection(){

        return con;

    }

    /**

     * Apaga todas as linhas da tabela

     */

    private void deleteRows() throws SQLException{

        stmt = getConnection().createStatement();

        String sql = "DELETE FROM "+getTable();

		System.out.println("sql>>"+sql);

        stmt.executeUpdate(sql);

        getConnection().commit();

        stmt.close();

        stmt = null;

    }



	private String getFieldList() {

		if (fieldList==null) {

	       String strResult = "";

	       for (int i=0;i<fields.length;i++) {

				 strResult += fields[i];

				 if (i!=fields.length-1) {

					strResult+=",";

				 }

	       }

		   fieldList = strResult;

		}

		return fieldList;

	}





	private String getFieldSpotList() {

		if (fieldList==null) {

	       String strResult = "";

	       for (int i=0;i<fields.length;i++) {

				 strResult += fields[i] + "= ?";

				 if (i!=fields.length-1) {

					strResult+=",";

				 }

	       }

		   fieldList = strResult;

		}

		return fieldList;

	}





	private String getSpotList() {

		if (fieldSpotList==null) {

	       String strResult = "";

	       for (int i=0;i<fields.length;i++) {

				 strResult += "?";

				 if (i!=fields.length-1) {

					strResult+=",";

				 }

	       }

		   fieldSpotList = strResult;

		}

		return fieldSpotList;

	}



    public Object[] insertArray(Object[] objs) throws CommunicationException, StorageException {

        for (int counter = 0; counter < objs.length; counter++) {

		   insert(objs[counter]);

         } //for

         return null;

    } //insertArray

    public Object insert(Object obj) throws CommunicationException, StorageException {

        GenericStorageItem info = (GenericStorageItem) obj;

        try {

            insertGenericItem(info);

        } //try

        catch(Exception erro) {

            throw new StorageException ("Problemas com bd :" + erro.getMessage(),erro );

        } //catch

        return null;

    } //insert



    private int insertGenericItem(GenericStorageItem info) throws SQLException, DataNotFoundException {

		if (fields==null) {

		   setFields(info.getFieldNames());

		}

        if (pstmt_insert == null) {

            String sql = "INSERT INTO "+getTable()+" ("+getFieldList()+") ";

            sql += "VALUES ("+getSpotList()+")";

			System.out.println("sql>>"+sql);

            pstmt_insert = getConnection().prepareStatement(sql);

        } //if

//		System.out.println("vai setPreparedStatement");

		setPreparedStatement(info,pstmt_insert);

//		System.out.println("saiu de setPreparedStatement");

        int resultado = pstmt_insert.executeUpdate();

        pstmt_insert.clearParameters();

        return resultado;

    }



    private void setPreparedStatement(GenericStorageItem info, PreparedStatement pstmt) throws DataNotFoundException, SQLException{

		Object o;

		int type;

//		System.out.println("num campos:"+fields.length);

		for(int count=0;count<fields.length;count++) {

		   o = info.getValue(fields[count]);

		   type = info.getTypeByName(fields[count]);

//		   System.out.println("campo:"+fields[count]+", valor:"+o+", contador(select):"+(count+1));



		   switch (type) {

				  case Field.BYTE_TYPE:

                  {

					pstmt.setByte(count+1,((Byte)o).byteValue());

					break;

                  }

                  case Field.INT_TYPE:

                  {

					pstmt.setInt(count+1,((Integer)o).intValue());

					break;

                  }

                  case Field.LONG_TYPE:

                  {

					pstmt.setLong(count+1,((Long)o).longValue());

					break;

                  }

                  case Field.STRING_TYPE:

                  {



					pstmt.setString(count+1,(String)o);

					break;

                  }

			      case Field.DOUBLE_TYPE:

                  {

//					System.out.println("Double_Value::"+((Double)o).doubleValue());

					pstmt.setDouble(count+1,((Double)o).doubleValue());

					break;

                  }

                  default:

                  {

					break;

                  }

		}//case

      }//for

  	}



    public Object[] updateArray(Object[] objs) throws CommunicationException, StorageException {

        for (int counter = 0; counter < objs.length; counter++) {

            update(objs[counter]);

        } //for

        return null;

    } //updateArray



    public Object update(Object obj) throws StorageException, CommunicationException {

        GenericStorageItem info = (GenericStorageItem) obj;

        try {

            updateGenericStorageItem(info);

        } //try

        catch(SQLException erro) {

            throw new StorageException ("Problemas com bd :" + erro.getMessage(),erro);

        } //catch

        return null;

    } //update



    private void updateGenericStorageItem(GenericStorageItem info) throws SQLException,StorageException {

        if(stmt == null)

            stmt = getConnection().createStatement();

		int code = ((Integer)info.getValue(codeField)).intValue();

		String sql = "select count(*) from "+getTable()+" where "+codeField+"="+code;

		System.out.println("sql>>"+sql);

        ResultSet rs = stmt.executeQuery(sql);



        rs.next();

        int number = rs.getInt(1);

        rs.close();

        if(number == 1){

            if (pstmt_update == null) {

                sql = "UPDATE "+getTable()+" SET "+getFieldSpotList()+" WHERE "+codeField+" = ?";

				System.out.println("sql>>"+sql);

                pstmt_update = getConnection().prepareStatement(sql);

            }

			try {

			    setPreparedStatement(info,pstmt_update);

			} catch (Exception e) {

				throw new StorageException(e.getMessage());

			}

            pstmt_update.executeUpdate();

            pstmt_update.clearParameters();

        }

    }



    public Object[] removeArray(Object[] objs) throws StorageException, CommunicationException {

        for (int counter = 0; counter < objs.length; counter++) {

            remove(objs[counter]);

        } //for

        return null;

    } //removeArray



    public Object remove(Object obj) throws StorageException, CommunicationException {

        GenericStorageItem info = (GenericStorageItem) obj;

        try{

            if (stmt_remove == null) {

                stmt_remove = con.createStatement();

            } //if

			int code = ((Integer)info.getValue(codeField)).intValue();

            stmt_remove.executeUpdate("delete from "+getTable()+" where "+codeField+"="+code);

        }//try

        catch(SQLException sqle){

            throw new StorageException(sqle);

        }//catch

        return null;

    } //remove



    public Object[] selectArray(Object[] objs) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //selectArray



    public Object select(Object obj) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //select



    public Enumeration selectEnumeration(Object obj) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //selectEnumeration



    public Object finalize(Object obj) throws StorageException, CommunicationException {

        return commit (obj);

    } //finalize



    public Object commit(Object obj) throws StorageException, CommunicationException {

        try {

            this.con.commit();

        } //try

        catch (SQLException erro) {

            throw new StorageException ("Nao conseguiu realizar commit: " +erro.getMessage());

        } //catch

        return null;

    } //commit



    public Object rollback(Object obj) throws StorageException, CommunicationException {

        try {

            this.con.rollback();

        } //try

        catch (SQLException erro) {

            throw new StorageException ("Nao conseguiu realizar commit: " +erro.getMessage());

        } //catch

        return null;

    } //rollback



    public Object removeResource(Object obj) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //removeResource



    public Object[] removeResourceArray(Object[] objs) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //removeResourceArray



    public Object addResource(Object obj) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //removeResource



    public Object[] addResourceArray(Object[] obj) throws StorageException, CommunicationException {

        throw new StorageException ("Metodo nao implementado!");

    } //removeResourceArray



    public static void main(String[] args) throws Exception {

/*        PageItem info = new PageItemImpl();

        info.setCode(1);

        info.setDescription("OI");

        info.setCentroid(new Integer[] {new Integer(1)});

        info.setCentroidLength(1);

        info.setTitle("Oscar");

        info.setLength(1);

        info.setModifiedDate(1023);

        info.setURL("http://www.batistinha.com.br");

        util.ParameterFile config = new com.radix.util.ParameterFile (args);

        Connection con = util.jdbc.PoolManager.getConnection(config);

        util.storage.Storage storage = new PageStorageSQL(con, new CentroidRepositorySQL(con));

        storage.insert(info);

        con.commit();

        System.in.read(new byte[12]);

        info.setCentroid(new Integer[] {new Integer(2), new Integer(3)});

        info.setCentroidLength(2);

        storage.update(info);

        con.commit();

        con.close();

*/

    } //main

}

