package edu.utdallas.paged.sdb.compiler;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.compiler.SDB_QC;
import com.hp.hpl.jena.sdb.core.SDBConstants;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.sql.ResultSetJDBC;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * @see com.hp.hpl.jena.sdb.compiler.QC
 * @author vaibhav
 */
public class PagedQC extends SDB_QC
{
    private static Logger log = LoggerFactory.getLogger(SDB_QC.class) ;
    
    public static boolean fetchPrint = false ;
    public static boolean PrintSQL = false ;
    
    // ---- Execute an OpSQL.

    /**
     * @see com.hp.hpl.jena.sdb.compiler.QC#exec(com.hp.hpl.jena.sdb.compiler.OpSQL, SDBRequest, Binding, ExecutionContext)
     */
    public static QueryIterator exec(PagedOpSQL opSQL, SDBRequest request, Binding binding, ExecutionContext execCxt)
    {
        String sqlStmtStr = toSqlString(opSQL, request) ;
        
        if ( PrintSQL )
            System.out.println(sqlStmtStr) ;
        
        String str = null ;
        if ( execCxt != null )
            str = execCxt.getContext().getAsString(SDB.jdbcFetchSize) ;
        
        int fetchSize = SDBConstants.jdbcFetchSizeOff;
        
        if ( str != null )
            try { fetchSize = Integer.parseInt(str) ; }
            catch (NumberFormatException ex)
            { log.warn("Bad number for fetch size: "+str) ; }
        
        try {
            ResultSetJDBC jdbcResultSet = request.getStore().getConnection().execQuery(sqlStmtStr, fetchSize) ;
            try {
                // And check this is called once per SQL.
                if ( opSQL.getBridge() == null )
                    log.error("Null bridge") ;
                QueryIterator iter =  opSQL.getBridge().assembleResults(jdbcResultSet, binding, execCxt) ;
                if ( iter.hasNext() ) return iter;
                else return null;
            } finally {
                // ResultSet closed inside assembleResults or by the iterator returned.
                jdbcResultSet = null ;
            }
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException in executing SQL statement", ex) ;
        }
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.QC#toSqlString(com.hp.hpl.jena.sdb.compiler.OpSQL, SDBRequest)
     */
    public static String toSqlString(PagedOpSQL opSQL, 
                                     SDBRequest request)
    {
        SqlNode sqlNode = opSQL.getSqlNode() ;
        String sqlStmt = request.getStore().getSQLGenerator().generateSQL(request, sqlNode) ;
        return sqlStmt ; 
    }
}
/** Copyright (c) 2008-2010, The University of Texas at Dallas
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*     * Redistributions of source code must retain the above copyright
*       notice, this list of conditions and the following disclaimer.
*     * Redistributions in binary form must reproduce the above copyright
*       notice, this list of conditions and the following disclaimer in the
*       documentation and/or other materials provided with the distribution.
*     * Neither the name of the The University of Texas at Dallas nor the
*       names of its contributors may be used to endorse or promote products
*       derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY The University of Texas at Dallas ''AS IS'' AND ANY
* EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL The University of Texas at Dallas BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/