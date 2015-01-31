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
package focusedCrawler.util.url.naming;



public class Map {



        protected String[] ids;   // identificador do atributo que fica diretamente na url

        protected String[] names; // nome do atributo utilizado para identifica-los apos um <code>resolve()</code>

        protected String[] types; // tipo do objeto, utilizado para reconstrucao do mesmo



    public Map() {

    }



    public Map(String[] ids,String[] names,String[] types) {

        this.ids   = ids;

        this.names = names;

        this.types = types;

    }



    public String[] getIds() {

        return ids;

    }

    public String[] getNames() {

        return names;

    }

    public String[] getTypes() {

        return types;

    }



    public void setIds(String[] newIds) {

        this.ids = newIds;

    }

    public void setNames(String[] newNames) {

        this.names = newNames;

    }

    public void setTypes(String[] newTypes) {

        this.types = newTypes;

    }



    public String idAt(int index) {

        return ids[index];

    }

    public String nameAt(int index) {

        return names[index];

    }

    public String typeAt(int index) {

        return types[index];

    }



//--- Funcoes de identificacao dos mapeamentos entre os identificadores, tipos, e nomes. -----//

    /**

    *   Dado o nome retorna seu id.

    */

    public String getNameForId(String id) {

        return getBijecao(id,ids,names);

    }



    /**

    *   Dado o tipo retorna seu id.

    */

    public String getTypeForId(String id) {

        return getBijecao(id,ids,types);

    }



    /**

    *   Dado o id retorna seu nome.

    */

    public String getIdForName(String name) {

        return getBijecao(name,names,ids);

    }



    /**

    *   Dado o tipo retorna seu nome.

    */

    public String getTypeForName(String name) {

        return getBijecao(name,names,types);

    }



    /**

    *   Retorna o resultado de uma bijecao entre arrays de elementos, buscando pelo valor do primeiro parametro.

    */

    private String getBijecao(String obj,String[] dominio,String[] imagem) {

        String resultado = null;

        if( obj != null )

          {

            boolean achou = false;

            for( int i = 0; i < dominio.length && !achou; i++ )

               {

                 if( dominio[i].equals(obj) )

                   {

                     resultado = imagem[i];

                     achou = true;

                   }

               }

          }

        return resultado;

    }



    /**

    *   Metodo que indica o padrao que deve ser utilizado para a codificacao dos objetos na URL.

    */

    public String formatString(Object obj) {

        return (obj != null ? obj.toString() : null);

    }



    /**

    *   Dados o identificador do objeto e sua representacao em String, determina o tipo do objeto e o instancia.

    *   Para a generalizacao desta classe necessecita-se apenas reescrever tal metodo para aceitar outro tipos

    *   de objetos.

    */

    protected Object parseForId(String id,String value) {

        return parseFromType(getTypeForId(id),value);

    }



    /**

    *   Dados o nome do objeto e sua representacao em String, determina o tipo do objeto e o instancia.

    *   Para a generalizacao desta classe necessecita-se apenas reescrever tal metodo para aceitar outro tipos

    *   de objetos.

    */

    protected Object parseForName(String name,String value) {

        return parseFromType(getTypeForName(name),value);

    }



    public static final Integer ERRO_INTEGER = new Integer(-1);

    public static final Long    ERRO_LONG    = new Long(-1);

    public static final Boolean ERRO_BOOLEAN = null;

    public static final String  ERRO_STRING  = null;

    /**

    *   Dados o tipo do objeto e sua representacao em String, o instancia o objeto.

    *   Para a generalizacao desta classe necessecita-se apenas reescrever tal metodo para aceitar outro tipos

    *   de objetos.

    */

    protected Object parseFromType(String type,String value) {

        Object resultado = null;

        if( "java.lang.Integer".equals(type) )

          {

            try {

                  resultado = new Integer(value);

                }

            catch(Exception exc)

                {

                  exc.printStackTrace();

                  resultado = ERRO_INTEGER;

                }

          }

        else if( "java.lang.Long".equals(type) )

          {

            try {

                  resultado = new Long(value);

                }

            catch(Exception exc)

                {

                  exc.printStackTrace();

                  resultado = ERRO_LONG;

                }

          }

        else if( "java.lang.Boolean".equals(type) )

          {

            try {

                  resultado = new Boolean(value);

                }

            catch(Exception exc)

                {

                  exc.printStackTrace();

                  resultado = ERRO_BOOLEAN;

                }

          }

        else if( "java.lang.String".equals(type) )

          {

            try {

                  resultado = new String(value);

                }

            catch(Exception exc)

                {

                  exc.printStackTrace();

                  resultado = ERRO_STRING;

                }

          }

        return resultado;

    }



}
