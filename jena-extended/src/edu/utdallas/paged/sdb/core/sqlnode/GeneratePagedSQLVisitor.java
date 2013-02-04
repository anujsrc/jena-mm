package edu.utdallas.paged.sdb.core.sqlnode;

import java.util.List;

import com.hp.hpl.jena.query.ARQ;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.core.Annotations;
import com.hp.hpl.jena.sdb.core.sqlexpr.SqlExprList;
import com.hp.hpl.jena.sdb.core.sqlnode.ColAlias;
import com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLVisitor;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sparql.util.IndentedWriter;

import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlNodeVisitor;

/**
 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLVisitor
 * @author vaibhav
 */
public class GeneratePagedSQLVisitor extends GenerateSQLVisitor implements PagedSqlNodeVisitor
{
    protected IndentedWriter out ;
    int levelSelectBlock = 0 ;
    
    // Per Generator
    public boolean outputAnnotations = ARQ.getContext().isTrueOrUndef(SDB.annotateGeneratedSQL) ;
    private static final int annotationColumn = 40 ;
    private static boolean commentSQLStyle = true ;

	public GeneratePagedSQLVisitor(IndentedWriter out) { super(out); this.out = out; }

	/**
	 * @see com.hp.hpl.jena.sdb.core.sqlnode.GenerateSQLVisitor#visit(com.hp.hpl.jena.sdb.core.sqlnode.SqlSelectBlock)
	 */
	@Override
	public void visit(PagedSqlSelectBlock pagedSqlSelectBlock) 
	{
	       levelSelectBlock++ ;
	        
	        if ( levelSelectBlock > 1 )
	        {
	            // Alias needed.
//	            SqlRename rename = SqlRename.view("X", sqlSelectBlock) ;
//	            rename.visit(this) ;
//	            levelSelectBlock-- ;
//	            return ;
	        }
	        
	        out.print("SELECT ") ;
	        if ( pagedSqlSelectBlock.getDistinct() )
	            out.print("DISTINCT ") ;
	        if ( annotate(pagedSqlSelectBlock) ) 
	            out.ensureStartOfLine() ;
	        out.incIndent() ;
	        print(pagedSqlSelectBlock.getCols()) ;
	        out.decIndent() ;
	        out.ensureStartOfLine() ;

	        // FROM
	        out.print("FROM") ;
	        if ( ! pagedSqlSelectBlock.getSubNode().isTable() )
	            out.println();
	        else
	            out.print(" ");
	        out.incIndent() ;
	        outputNode(pagedSqlSelectBlock.getSubNode(), true) ;
	        //sqlSelectBlock.getSubNode().visit(this) ;
	        out.decIndent() ;
	        out.ensureStartOfLine() ;

	        // WHERE
	        if ( pagedSqlSelectBlock.getConditions().size() > 0 )
	            genWHERE(pagedSqlSelectBlock.getConditions()) ;

	        // LIMIT/OFFSET
	        out.ensureStartOfLine() ;
	        genLimitOffset(pagedSqlSelectBlock) ;
	        levelSelectBlock-- ;		
	}

    protected void genLimitOffset(PagedSqlSelectBlock pagedSqlSelectBlock)
    {
        if ( pagedSqlSelectBlock.getLength() >= 0 )
            out.println("LIMIT "+pagedSqlSelectBlock.getLength()) ;
        if ( pagedSqlSelectBlock.getStart() >= 0 )
            out.println("OFFSET "+pagedSqlSelectBlock.getStart()) ;
        
    }
    
    private void print(List<ColAlias> cols)
    {
        String sep = "" ;
        if ( cols.size() == 0 )
        {
            // Can happen - e.g. query with no variables.
            //log.info("No SELECT columns") ;
            out.print("*") ;
        }

        // Put common prefix on same line
        String currentPrefix = null ; 
        String splitMarker = "." ;

        for ( ColAlias c : cols )
        {
            out.print(sep) ;
            
            // Choose split points.
            String cn = c.getColumn().getFullColumnName() ;
            int j = cn.lastIndexOf(splitMarker) ;
            if ( j == -1 )
                currentPrefix = null ;
            else
            {
                String x = cn.substring(0, j) ;
                if ( currentPrefix != null && ! x.equals(currentPrefix) )
                    out.println() ;

                currentPrefix = x ;
            }
            sep = ", " ;
            out.print(c.getColumn().getFullColumnName()) ;
          
            if ( c.getAlias() != null )
            {
                out.print(aliasToken()) ;
                out.print(c.getAlias().getColumnName()) ;
            }
        }
    }

    private void genWHERE(SqlExprList conditions)
    {
        out.print("WHERE") ;
        out.print(" ") ;
        out.incIndent() ;
        conditionList(conditions) ;
        out.decIndent() ;
    }

    private void outputNode(SqlNode sqlNode, boolean mayNeedBrackets)
    {
        if ( sqlNode.isTable() )
        {
            sqlNode.visit(this) ;
            return ;
        }
        //boolean brackets = ( mayNeedBrackets && ( sqlNode.isSelectBlock() || sqlNode.isCoalesce() ) ) ;
        
        boolean brackets = false ;
        brackets = brackets || (mayNeedBrackets && sqlNode.isCoalesce()) ;
        
        // Work harder? ready for a better test.
        brackets = brackets || ( mayNeedBrackets && sqlNode.isSelectBlock()) ;
        
        
        // Need brackets if the subpart is a SELECT
        
        if ( brackets )
        {
            out.print("( ") ;
            out.incIndent() ;
        }
        sqlNode.visit(this) ;
        if ( brackets )
        {
            out.decIndent() ;
            out.ensureStartOfLine() ;
            out.print(")") ;
        }
            // Every derived table (SELECT ...) must have an alias.
            // Is there a more principled way to do this? .isDerived?
//            if ( sqlNode.isRestrict() || sqlNode.isProject())
//                out.print(+sqlNode.getAliasName()) ;
        if ( sqlNode.getAliasName() != null )
        {
            out.print(aliasToken()) ;
            out.print(sqlNode.getAliasName()) ;
        }
        annotate(sqlNode) ;
    }

    private boolean annotate(Annotations sqlNode)
    { return annotate(sqlNode, annotationColumn) ; }

    // return true if annotation was output and it runs to end-of-line  
    private boolean annotate(Annotations sqlNode, int indentationColumn)
    {
        if ( ! outputAnnotations )
            return false ;
        
        boolean first = true ;
        for ( String s : sqlNode.getNotes() )
        {
            if ( !first ) out.println();
            first = false; 
            out.pad(indentationColumn, true) ;
            if ( commentSQLStyle )
            {
                out.print(" -- ") ; out.print(s) ;
            }else{
                out.print(" /* ") ; out.print(s) ; out.print(" */") ;
            }
        }
        return !commentSQLStyle || !first ;
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