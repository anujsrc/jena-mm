package edu.utdallas.paged.sdb.compiler;

import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.IndentedWriter;
import com.hp.hpl.jena.sparql.util.NodeIsomorphismMap;

/**
 * @see com.hp.hpl.jena.sdb.compiler.OpSQL
 * @author vaibhav
 */
public class PagedOpSQL extends OpExt
{
    private SqlNode sqlNode ;
    private Op originalOp ;
    private SQLBridge bridge = null ; 
    private SDBRequest request ;
 
    /** Constructor **/
    public PagedOpSQL(SqlNode sqlNode, Op original, SDBRequest request)
    {
    	super( "SQL" );
        // Trouble is, we may have to throw the SqlNode away because of substitution.  What a waste!
        this.request = request ;
        this.sqlNode = sqlNode ;
        this.originalOp = original ;
        // Only set at the top, eventually, when we know the projection variables.
        this.bridge = null ;
    }
	
    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#copy()
     */
    public PagedOpSQL copy() 
    { return this ; }      // We're immutable - return self.
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#eval(QueryIterator, ExecutionContext)
     */    
    @Override
    public QueryIterator eval(QueryIterator input, ExecutionContext execCxt)
    { return new PagedQueryIterSQL(this, input, execCxt) ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#exec(ExecutionContext)
     */
    public QueryIterator exec(ExecutionContext execCxt)
    { return exec(BindingRoot.create(), execCxt) ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#exec(Binding, ExecutionContext)
     */
    public QueryIterator exec(Binding parent, ExecutionContext execCxt)
    {
        if ( parent == null )
            parent = BindingRoot.create() ;
        QueryIterator qIter = PagedQC.exec(this,
                                      getRequest(),
                                      parent, 
                                      execCxt) ;
        return qIter ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#getOriginal()
     */
    public Op getOriginal()     { return originalOp ; }
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#effectiveOp()
     */
    public Op effectiveOp()     { return originalOp ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#hashCode()
     */
    @Override
    public int hashCode()
    {
        return sqlNode.hashCode() ^ 0x1 ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#equalTo(Op, NodeIsomorphismMap)
     */
    @Override
    public boolean equalTo(Op other, NodeIsomorphismMap labelMap)
    {
        // SqlNodes don't provide structural equality (yet?).
        if ( ! ( other instanceof PagedOpSQL ) ) return false ;
        PagedOpSQL opSQL = (PagedOpSQL)other ;
        return sqlNode.equals(opSQL.sqlNode) ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#getRequest()
     */
    public SDBRequest getRequest() { return request ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#output(IndentedWriter)
     */
    @Override
    public void output(IndentedWriter out)
    {
        out.print(Plan.startMarker) ;
        out.println("OpSQL --------") ;
        out.incIndent() ;
        sqlNode.output(out) ;
        out.decIndent() ;
        out.ensureStartOfLine() ;
        out.print("--------") ;
        out.print(Plan.finishMarker) ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#toSQL()
     */
    public String toSQL()
    {
       return PagedQC.toSqlString(this, request) ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#getSqlNode()
     */
    public SqlNode getSqlNode()
    {
        return sqlNode ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#resetSqlNode(SqlNode)
     */
    public void resetSqlNode(SqlNode sqlNode2)
    { sqlNode = sqlNode2 ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#getBridge()
     */
    public SQLBridge getBridge()            { return bridge ; }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.OpSQL#setBridge(SQLBridge)
     */
    public void setBridge(SQLBridge bridge) { this.bridge = bridge ; }

	@Override
	public void outputArgs(IndentedWriter out, SerializationContext sCxt) 
	{
        out.print("'''") ;
        sqlNode.output(out) ;
        out.print("'''") ;		
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