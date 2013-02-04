package edu.utdallas.paged.sdb.graph;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.sdb.SDB;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.core.SDBRequest;
import com.hp.hpl.jena.sdb.graph.GraphSDB;
import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingRoot;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NiceIterator;

import edu.utdallas.paged.sdb.core.sqlnode.PagedSqlSelectBlock;
import edu.utdallas.paged.sdb.engine.PagedQueryEngineSDB;

/**
 * @see com.hp.hpl.jena.sdb.graph.GraphSDB
 * @author vaibhav
 */
public class PagedGraphSDB extends GraphSDB
{
    private GraphIterator gIter = null; 
    private final long initialLength = 3000000L;
    public static long length;
	
    public PagedGraphSDB(Store store, String uri)
    { 
    	super(store, Node.createURI(uri)) ; 
    	PagedGraphSDB.length = (long) Math.ceil( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * this.initialLength;
    }
    
    public PagedGraphSDB(Store store)
    { 
    	super(store, (Node)null) ; 
    	PagedGraphSDB.length = (long) Math.ceil( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * this.initialLength;
    }

    public PagedGraphSDB(Store store, Node graphNode)
    { 
    	super( store, graphNode ); 
    	PagedGraphSDB.length = (long) Math.ceil( Runtime.getRuntime().totalMemory() / ( 1024*1024*1024*1.0 ) ) * this.initialLength;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.graph.GraphSDB#find(TripleMatch)
     */
    @SuppressWarnings("unchecked")
	@Override
    protected ExtendedIterator graphBaseFind(TripleMatch m)
    {
        // Fake a query.
        @SuppressWarnings("unused")
		SDBRequest cxt = new SDBRequest(getStore(), new Query()) ;
        
        // If null, create and remember a variable, else use the node.
        final Node s = (m.getMatchSubject()==null)   ? Var.alloc("s")   :  m.getMatchSubject() ;
        
        final Node p = (m.getMatchPredicate()==null) ? Var.alloc("p")   :  m.getMatchPredicate() ;
        final Node o = (m.getMatchObject()==null)    ? Var.alloc("o")   :  m.getMatchObject() ;

        Triple triple = new Triple(s, p ,o) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(triple) ;
        Op op = new OpQuadPattern(graphNode, pattern) ;
        Plan plan = PagedQueryEngineSDB.getFactory().create(op, datasetStore, BindingRoot.create(), null) ;
        
        QueryIterator qIter = plan.iterator() ;
        
        if ( SDB.getContext().isTrue(SDB.streamGraphAPI) )
        {
            // ---- Safe version: 
            List<Binding> bindings = new ArrayList<Binding>() ;
            while ( qIter.hasNext() ) bindings.add(qIter.nextBinding()) ;
            qIter.close();
            
            // QueryIterPlainWrapper is just to make it ia QuyerIterator again.
            gIter = new GraphIterator(triple, new QueryIterPlainWrapper(bindings.iterator())) ;
        }
        else
        {
            // Dangerous version -- application must close iterator.
            gIter = new GraphIterator(triple, qIter) ;
        }
        return gIter;
    }

    @SuppressWarnings("unchecked")
	protected ExtendedIterator graphBaseFind(Triple t)
    {
    	// Fake a query.
        @SuppressWarnings("unused")
		SDBRequest cxt = new SDBRequest(getStore(), new Query()) ;
        
        // Evaluate as an algebra expression
        BasicPattern pattern = new BasicPattern() ;
        pattern.add(t) ;
        Op op = new OpQuadPattern(graphNode, pattern) ;
        Plan plan = PagedQueryEngineSDB.getFactory().create(op, datasetStore, BindingRoot.create(), null) ;
        
        QueryIterator qIter = plan.iterator() ;
        
        if ( SDB.getContext().isTrue(SDB.streamGraphAPI) )
        {
            // ---- Safe version: 
            List<Binding> bindings = new ArrayList<Binding>() ;
            while ( qIter.hasNext() ) bindings.add(qIter.nextBinding()) ;
            qIter.close();
            
            // QueryIterPlainWrapper is just to make it ia QuyerIterator again.
            gIter.pattern = t; gIter.qIter = new QueryIterPlainWrapper(bindings.iterator()) ;
        }
        else
        {
            // Dangerous version -- application must close iterator.
            gIter.pattern = t; gIter.qIter = qIter; ;
        }
        return gIter;
    }

    // Collect ugliness together.
    private static Triple bindingToTriple(Triple pattern,  
                                          Binding binding)
    {
        Node s = pattern.getSubject() ;
        Node p = pattern.getPredicate() ;
        Node o = pattern.getObject() ;
        
        Node sResult = s ;
        Node pResult = p ;
        Node oResult = o ;
        
        if ( Var.isVar(s) )
            sResult = binding.get(Var.alloc(s)) ;
        if ( Var.isVar(p) )
            pResult = binding.get(Var.alloc(p)) ;
        if ( Var.isVar(o) )
            oResult = binding.get(Var.alloc(o)) ;
        
        Triple resultTriple = new Triple(sResult, pResult, oResult) ;
        return resultTriple ;
    }

    @SuppressWarnings("unchecked")
	class GraphIterator extends NiceIterator
    {
        QueryIterator qIter ; 
        Triple current = null ;
        Triple pattern ;
        
        GraphIterator(Triple pattern, QueryIterator qIter)
        { 
            this.qIter = qIter ;
            this.pattern = pattern ;
        }
        
        @Override
        public void close()
        {
        	if ( qIter == null ) return;
            qIter.close() ;
        }
        
        @Override
        public boolean hasNext()
        {
        	if ( qIter == null ) return false;
            if ( qIter.hasNext() ) return true;
        	else
        	{
                if (PagedSqlSelectBlock.isStarted) 
                { PagedSqlSelectBlock.start += length; }
                else 
                { PagedSqlSelectBlock.start = 0; PagedSqlSelectBlock.isStarted = false; return false; }
                if (graphBaseFind(pattern).hasNext()) 
                { return true; }
                else 
                { PagedSqlSelectBlock.start = 0; PagedSqlSelectBlock.isStarted = false; return false; }
        	}
        }
        
        @Override
        public Triple next()
        {
            return ( current = bindingToTriple(pattern, qIter.nextBinding() ) ) ;
        }

        @Override
        public void remove()
        { 
            if ( current != null )
                delete(current) ;
        }
    }

    /**
     * @see com.hp.hpl.jena.sdb.graph.GraphSDB#queryHandler()
     */
    @Override
    public QueryHandler queryHandler()
    {
        return new PagedGraphQueryHandlerSDB(this, graphNode, datasetStore) ;
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