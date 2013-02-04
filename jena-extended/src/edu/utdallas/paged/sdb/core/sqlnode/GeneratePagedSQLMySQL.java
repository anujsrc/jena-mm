package edu.utdallas.paged.sdb.core.sqlnode;

import com.hp.hpl.jena.sdb.core.JoinType;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlJoinInner;
import com.hp.hpl.jena.sparql.util.IndentedLineBuffer;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlNodeVisitor;

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLMySQL
 * @author vaibhav
 */
public class GeneratePagedSQLMySQL extends GeneratePagedSQL
{
    @Override
    protected PagedSqlNodeVisitor makeVisitor(IndentedLineBuffer buff)
    {
        return (PagedSqlNodeVisitor) new GeneratorPagedVisitorMySQL(buff.getIndentedWriter()) ;
    }
}

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLMySQL.GenerateVisitorMySQL
 * @author vaibhav
 */
class GeneratorPagedVisitorMySQL extends GeneratePagedSQLVisitor
{
    // STRAIGHT_JOIN stops the optimizer reordering inner join
    // It requires that the left table and right table are kept as left and right,
    // so a sequence of joins can not be reordered. 
    
    static final String InnerJoinOperatorStraight = "STRAIGHT_JOIN" ;
    static final String InnerJoinOperatorDefault = JoinType.INNER.sqlOperator() ;
    
    public GeneratorPagedVisitorMySQL(IndentedWriter out) { super(out) ; }

    @Override
    public void visit(SqlJoinInner join)
    { 
        join = rewrite(join) ;
        visitJoin(join, InnerJoinOperatorDefault) ;
    }   
    
    @Override
    protected void genLimitOffset(PagedSqlSelectBlock sqlSelectBlock)
    {        
        if ( sqlSelectBlock.getLength() >= 0 || sqlSelectBlock.getStart() >= 0 )
        {
            // MySQL synatx issue - need LIMIT even if only OFFSET
            long length = sqlSelectBlock.getLength() ;
            if ( length < 0 )
            {
                sqlSelectBlock.addNote("Require large LIMIT") ;
                length = Long.MAX_VALUE ;
            }
            out.println("LIMIT "+length) ;
            if ( sqlSelectBlock.getStart() >= 0 )
                out.println("OFFSET "+sqlSelectBlock.getStart()) ;
        }
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