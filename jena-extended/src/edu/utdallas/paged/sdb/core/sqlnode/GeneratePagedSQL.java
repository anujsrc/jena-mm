package edu.utdallas.paged.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;

import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlNodeVisitor;
import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlSelectBlock;

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL
 * @author vaibhav
 */
public class GeneratePagedSQL extends GenerateSQL
{
	/**
	 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL#generateSQL(SDBRequest, SqlNode)
	 */
    public String generateSQL(SDBRequest request, SqlNode sqlNode)
    {
        // Top must be a project to cause the SELECT to be written
        sqlNode = ensureProject(request, sqlNode) ;
        return generatePartSQL(sqlNode) ;
    }
    
	/**
	 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL#generatePartSQL(SqlNode)
	 */
    public String generatePartSQL(SqlNode sqlNode)
    {
        IndentedLineBuffer buff = new IndentedLineBuffer() ;
        
        // Step one - rewrite the SQL node tree to have SelectBlocks, not the various SqlNodes
        // that contribute to a SELECT statement.
        
        // XXX Temp - the nodes this tranforms should not be generated now 
        //sqlNode = SqlTransformer.transform(sqlNode, new TransformSelectBlock()) ;

        // Step two - turn the SqlNode tree, with SqlSelectBlocks in it,
        // in an SQL string.
        PagedSqlNodeVisitor v = makeVisitor(buff) ;
        sqlNode.visit(v) ; 
        return buff.asString() ;
    }

	/**
	 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL#m
	 */
    protected PagedSqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return new GeneratePagedSQLVisitor(buff.getIndentedWriter()) ;
    }

	/**
	 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQL#ensureProject(SDBRequest, SqlNode)
	 */
    public static SqlNode ensureProject(SDBRequest request, SqlNode sqlNode)
    {
        if ( ! sqlNode.isSelectBlock() )
            sqlNode = PagedSqlSelectBlock.project(request, sqlNode) ;
        return sqlNode ;
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