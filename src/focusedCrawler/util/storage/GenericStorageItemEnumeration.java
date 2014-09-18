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

import java.sql.Statement;

import java.sql.ResultSet;

import java.sql.SQLException;

import java.io.IOException;

import focusedCrawler.util.DataNotFoundException;


public class GenericStorageItemEnumeration implements StorageItemEnumeration {



    private ResultSet   rs;

    private Statement statement;



    private String table;

	private String codeField;

	private String fieldList;



    private int blockSize;

    private long initialCode;

    private int lastUsedCode=0;





    private GenericStorageItem result;



    public GenericStorageItemEnumeration() {

    }



    public void setItem(GenericStorageItem newResult) {

        result = newResult;

    }



    public int getBlockSize() {

        return blockSize;

    }



    public void setBlockSize(int newSize) {

        blockSize = newSize;

    }



    public String getTable() {

        return table;

    }

    public void setTable(String newTable){

        table = newTable;

    }



    public String getCodeField() {

        return codeField;

    }



    public void setCodeField(String newCode){

        codeField = newCode;

    }



	private String getFieldList() {

		System.out.println("-----------------field list-----------------");

		if (fieldList==null) {

	       String strResult = "";

	       for (int i=0;i<result.getFieldNames().length;i++) {

				 strResult += result.getFieldNames()[i];

				 if (i!=result.getFieldNames().length-1) {

					strResult+=",";

				 }

	       }

		   fieldList = strResult;

		}

		return fieldList;

	}



    public void setStatement(Statement newStatement) throws SQLException {

        statement = newStatement;

        ResultSet rs = statement.executeQuery("select max("+getCodeField()+") from "+getTable());

        if (rs.next()) {

            lastUsedCode = rs.getInt(1);

            System.out.println ("------MAX_CODE>> " + lastUsedCode);

        }

        else {

            throw new SQLException("Could not get last code!");

        }

        rs.close();

    }



    public Statement getStatement() {

        return statement;

    }



    public boolean hasNext() throws StorageException{

        try{

            boolean next = false;

//			System.out.println("-----------------has next ?-----------------");

            if((rs == null) || (!(next=rs.next()))){

//				System.out.println("-----------------antes de while-----------------");

                boolean canExit = false;

                while ((initialCode <=lastUsedCode) && !canExit) {

                    if(rs!=null){

                        rs.close();

                    }

					String sql = "select " +getFieldList() + " from " +getTable()+

                                       " where " +getCodeField()+ ">="  +initialCode+ " and "

                                                 +getCodeField()+ "<" +(initialCode+getBlockSize()) +

                                       " order by " + getCodeField();

					System.out.println("##sql>>"+sql);

                    rs = getStatement().executeQuery(sql);

                    initialCode+=getBlockSize();

					System.out.println("##blockSize::"+getBlockSize());

                    next =rs.next();

                    canExit = next;

                }

            }

            return next;

        }catch(SQLException sqle){

            throw new StorageException(sqle.getMessage());

        }

    }



    public StorageItem next() throws StorageException {

        try {

			for (int count=0;count<result.getFieldNames().length;count++) {

				result.setValue(result.getFieldNames()[count],getObject(rs,result.getFieldNames()[count]));

			}

            return result;

        }//try

        catch(SQLException sqle){

            throw new StorageException(sqle.getMessage());

        }//catch

        catch(DataNotFoundException sqle){

            throw new StorageException(sqle.getMessage());

        }//catch

        catch(IOException sqle){

            throw new StorageException(sqle.getMessage());

        }//catch



    }



	public Object getObject(ResultSet _rs, String _fieldName ) throws SQLException, DataNotFoundException, IOException {

	 int type = result.getTypeByName(_fieldName);

//         System.out.println("Tipo::"+type);



      switch (type) {

          case Field.BYTE_TYPE:

          {

             return new Byte(rs.getByte(_fieldName));

          }

          case Field.INT_TYPE:

          {

             return new Integer(rs.getInt(_fieldName));

          }

          case Field.LONG_TYPE:

          {

             return new Long(rs.getLong(_fieldName));

          }

          case Field.STRING_TYPE:

          {

             String word  = rs.getString(_fieldName);

             if (word!=null) {

                word = replaceSpecialChars(word);

             }

             return word;

          }

          case Field.DOUBLE_TYPE:

          {

             return new Double(rs.getDouble(_fieldName));

          }

          default:

          {

             throw new IOException("tipo inválido: " + type);

          }

		}

	}



    public void free() throws StorageException {

        try{

			if (rs!=null) {

               rs.close();

			}

			if (getStatement()!=null) {

               getStatement().close();

			}

        } //try

        catch(SQLException sqle){

            throw new StorageException(sqle);

        } //catch

    }



    private String replaceSpecialChars(String _line) {

           String line = _line;

//           System.out.println("replace em : "+line);

           while (line.lastIndexOf("\n")!=-1) {

              line = line.replace('\n',' ');

//              System.out.println("replace ql");

           }

           while (line.lastIndexOf("\t")!=-1) {

              line = line.replace('\t',' ');

//              System.out.println("replace tab");

           }

           while (line.lastIndexOf("\r")!=-1) {

              line = line.replace('\r',' ');

//              System.out.println("replace return");

           }

           return line.trim();

   }



}







