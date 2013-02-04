package edu.utdallas.paged.sdb.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.compiler.ConditionCompiler;
import com.hp.hpl.jena.sdb.compiler.SDB_QC;
import com.hp.hpl.jena.sdb.compiler.QuadBlockCompiler;
import com.hp.hpl.jena.sdb.compiler.QueryCompiler;
import com.hp.hpl.jena.sdb.compiler.rewrite.QuadBlockRewriteCompiler;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.core.sqlnode.SqlNode;
import com.hp.hpl.jena.sdb.store.SQLBridge;
import com.hp.hpl.jena.sdb.store.SQLBridgeFactory;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.OpVars;
import com.hp.hpl.jena.sparql.algebra.OpVisitorBase;
import com.hp.hpl.jena.sparql.algebra.OpWalker;
import com.hp.hpl.jena.sparql.algebra.Transform;
import com.hp.hpl.jena.sparql.algebra.Transformer;
import com.hp.hpl.jena.sparql.algebra.op.OpExt;
import com.hp.hpl.jena.sparql.algebra.op.OpModifier;
import com.hp.hpl.jena.sparql.core.Var;

/**
 * @see com.hp.hpl.jena.sdb.compiler.QueryCompilerMain
 * @author vaibhav
 */
public abstract class PagedQueryCompilerMain implements QueryCompiler
{
    protected SDBRequest request ;
    
    public PagedQueryCompilerMain(SDBRequest request)
    { 
        this.request = request ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.QueryCompilerMain#compile(Op)
     */
    public Op compile(final Op op)
    {
        QuadBlockCompiler quadCompiler = createQuadBlockCompiler() ;
        if ( request.getContext().isTrue(SDB.useQuadRewrite) )
            quadCompiler = new QuadBlockRewriteCompiler(request, quadCompiler) ;
        
        Transform t = new PagedTransformSDB(request, quadCompiler) ;
        Op op2 = Transformer.transform(t, op) ;
        
        // Modifiers: the structure is:
        //    slice
        //      distinct/reduced
        //        project
        //          order
        //            [toList]
        
        // Find the first non-modifier. WRONG with SqlSelectBlocks.
        Op patternOp = op2 ;
        while ( patternOp instanceof OpModifier )
            patternOp = ((OpModifier)patternOp).getSubOp() ;
        
        boolean patternIsOneSQLStatement = PagedQC.isOpSQL(patternOp) ;
            
        // To be removed : project handling in SqlNodesFinisher:: transform SDB should do this.
        // See XYZ below
        
        // Find all OpSQL nodes and put a bridge round them.
        OpWalker.walk(op2, new SqlNodesFinisher(patternIsOneSQLStatement)) ;
        return op2 ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.compiler.QueryCompilerMain#createQuadBlockCompiler()
     */
    public abstract QuadBlockCompiler createQuadBlockCompiler() ;
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.QueryCompilerMain#getConditionCompiler()
     */
    public ConditionCompiler getConditionCompiler()
    {
        return null ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.compiler.QueryCompilerMain.SQLNodeFinisher
     */
    // Find variables that need to be returned. 
    private class SqlNodesFinisher extends OpVisitorBase
    {
        private boolean justProjectVars ;
        SqlNodesFinisher(boolean justProjectVars)
        { this.justProjectVars = justProjectVars ; }
        
        /**
         * @see com.hp.hpl.jena.sparql.algebra.OpVisitorBase#visit(OpExt)
         */
        @Override
        public void visit(OpExt op)
        {
            if ( ! ( op instanceof PagedOpSQL ) )
            {
                super.visit(op) ;
                return ;
            }
            
            PagedOpSQL opSQL = (PagedOpSQL)op ;

            // XYZ
            List<Var> projectVars = null ;
                        
            if ( justProjectVars && request.getQuery() != null )
                // Need project vars and also the ORDER BY (for external sorting)
                projectVars = SDB_QC.queryOutVars(request.getQuery()) ;
            else
            {
                // All variables.
                Collection<Var> tmp = OpVars.patternVars(opSQL.getOriginal()) ;
                projectVars = new ArrayList<Var>(tmp) ;
            }
                    
            SqlNode sqlNode = opSQL.getSqlNode() ;
            
            SQLBridgeFactory f = request.getStore().getSQLBridgeFactory() ;
            
            SQLBridge bridge = f.create(request, sqlNode, projectVars) ;
            bridge.build();
            sqlNode = bridge.getSqlNode() ;
            
            opSQL.setBridge(bridge) ;
            opSQL.resetSqlNode(sqlNode) ;
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