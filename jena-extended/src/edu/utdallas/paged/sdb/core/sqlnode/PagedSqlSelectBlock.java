package edu.utdallas.paged.sdb.core.sqlnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.sdb.core.AliasesSql;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.Scope;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlColumn;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.ColAlias;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNodeVisitor;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTable;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlTransform;

import edu.utdallas.paged.sdb.graph.PagedGraphSDB;;

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock
 * @author vaibhav
 */
public class PagedSqlSelectBlock extends PagedSqlNodeBase1
{
    private List<ColAlias> cols = new ArrayList<ColAlias>() ;
    
    private SqlExprList exprs = new SqlExprList() ;
    private static final int NOT_SET = -9 ; 
    public static long start = 0 ;
    public long length = PagedGraphSDB.length;
    private boolean distinct = false ;
    public static boolean isStarted = false;
    
    @SuppressWarnings("unused")
	private SqlTable vTable ;           // Naming base for renamed columns
    @SuppressWarnings("unused")
	private Scope idScope = null ;      // Scopes are as the wrapped SqlNode unless explicitly changed.
    @SuppressWarnings("unused")
	private Scope nodeScope = null ;

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#project(SDBRequest, SqlNode)
     */
    static public SqlNode project(SDBRequest request, SqlNode sqlNode)
    { return project(request, sqlNode, (ColAlias)null) ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#project(SDBRequest, SqlNode, ColAlias)
     */
    static public SqlNode project(SDBRequest request, SqlNode sqlNode, ColAlias col)
    {
        PagedSqlSelectBlock block = blockNoView(request, sqlNode) ;
        if ( col != null )
            block.add(col) ;
        return block ;
    }
  
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#getCols()
     */
    public List<ColAlias> getCols()       { return cols ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#add(ColAlias)
     */
    public void add(ColAlias c)           { _add(c) ; }
    
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#addAll(Collection)
     */
    public void addAll(Collection<ColAlias> vc)    
    { 
        for ( ColAlias c : vc )
            _add(c) ;
    }
    
    private void _add(ColAlias c)
    { 
        @SuppressWarnings("unused")
		SqlColumn col = c.getColumn() ;
        @SuppressWarnings("unused")
		SqlColumn aliasCol = c.getAlias() ;
        c.check(getAliasName()) ;
//        
//        if ( aliasCol.getTable() != null && aliasCol.getTable().getAliasName().equals(getAliasName()) )
//            throw new SDBInternalError("Attempt to project to a column with different alias: "+col+" -> "+aliasCol) ;
        cols.add(c) ;
    }    

    private static PagedSqlSelectBlock blockNoView(SDBRequest request, SqlNode sqlNode)
    {
        if ( sqlNode instanceof PagedSqlSelectBlock )
            return (PagedSqlSelectBlock)sqlNode ;
        return _create(request, sqlNode) ;
    }
    
    private static PagedSqlSelectBlock _create(SDBRequest request,SqlNode sqlNode)
    {
        String alias = sqlNode.getAliasName() ;
        //if ( ! sqlNode.isTable() )
        alias = request.generator(AliasesSql.SelectBlock).next() ;
        PagedSqlSelectBlock block = new PagedSqlSelectBlock(alias, sqlNode) ;
        addNotes(block, sqlNode) ;
        return block ;
    }
    
    private PagedSqlSelectBlock(String aliasName, SqlNode sqlNode)
    {
        super(aliasName, sqlNode) ;
        if ( aliasName != null )
            vTable = new SqlTable(aliasName) ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#getConditions()
     */
    public SqlExprList getConditions()      { return exprs ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#hasSlice()
     */
    public boolean hasSlice()               { return (start != NOT_SET )  || ( length != NOT_SET ) ; }
    
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#hasConditions()
     */
    public boolean hasConditions()          { return exprs.size() > 0 ; }
    
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#getCols()
     */
    public long getStart()                  { return start ; }
    
    public void setStart(long start)        { PagedSqlSelectBlock.start = start ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#getLength()
     */
    public long getLength()                 { return length ; }
    
    public void setLength(long length)      { this.length = length ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#getDistinct()
     */
    public boolean getDistinct()
    {
        return distinct ;
    }

    @SuppressWarnings("unused")
	private void setDistinct(boolean isDistinct)
    {
        this.distinct = isDistinct ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#apply(SqlTransform, SqlNode)
     */
    public SqlNode apply(PagedSqlTransform transform, SqlNode newSubNode)
    { return transform.transform(this, newSubNode) ; }
    
    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#copy(SqlNode)
     */
    @Override
    public SqlNode copy(SqlNode subNode)
    { return new PagedSqlSelectBlock(this.getAliasName(), subNode) ; }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#visit(SqlNodeVisitor)
     */
    public void visit(PagedSqlNodeVisitor visitor)
    { PagedSqlSelectBlock.isStarted = true; visitor.visit(this) ; }

    private static void addNotes(PagedSqlSelectBlock block, SqlNode sqlNode)
    {
        block.addNotes(sqlNode.getNotes()) ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#apply(SqlTransform, SqlNode)
     */
	public SqlNode apply(SqlTransform transform, SqlNode newSubNode) { return apply((PagedSqlTransform)transform, newSubNode); }

    /**
     * @see com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock#visit(SqlNodeVisitor)
     */
	public void visit(SqlNodeVisitor visitor) { visit((PagedSqlNodeVisitor)visitor); }
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