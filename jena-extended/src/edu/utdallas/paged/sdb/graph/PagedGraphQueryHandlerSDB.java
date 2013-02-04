package edu.utdallas.paged.sdb.graph;

import java.util.*;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.*;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import com.hp.hpl.jena.sparql.algebra.Op;
import com.hp.hpl.jena.sparql.algebra.op.OpQuadPattern;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.Plan;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import com.hp.hpl.jena.sparql.lib.iterator.Iter;
import com.hp.hpl.jena.sparql.lib.iterator.Transform;
import com.hp.hpl.jena.sdb.shared.SDBNotImplemented;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;

import edu.utdallas.paged.mem.util.iterator.PagedWrappedIterator;
import edu.utdallas.paged.sdb.engine.PagedQueryEngineSDB;

/**
 * @see com.hp.hpl.jena.sdb.graph.GraphQueryHandlerSDB
 * @author vaibhav
 */
public class PagedGraphQueryHandlerSDB extends SimpleQueryHandler
{
    DatasetStoreGraph datasetStore ;
    Node graphNode ;
    BasicPattern bgp = new BasicPattern() ;
    private Op op ;
    private Node[] variables ;
    private Map<Node, Integer> indexes ;
    
    public PagedGraphQueryHandlerSDB(Graph graph, Node graphNode, DatasetStoreGraph datasetStore)
    { 
        super(graph) ;
        this.datasetStore = datasetStore ;
        this.graphNode = graphNode ;
    }

    /**
     * @see com.hp.hpl.jena.sdb.graph.GraphQueryHandlerSDB#prepareTree(Graph)
     */
    @Override
    final public TreeQueryPlan prepareTree( Graph pattern )
    {
        throw new SDBNotImplemented("prepareTree - Chris says this will not be called") ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.graph.GraphQueryHandlerSDB#prepareBindings(Query, Node[])
     */    
    @Override
    public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
    {
        this.variables = variables ;
        this.indexes = new HashMap<Node, Integer>() ;
        int idx = 0 ;
        for ( Node v : variables )
            indexes.put(v, (idx++) ) ;

        List<Triple> pattern = q.getPattern() ;
        for ( Triple t : pattern )
            bgp.add(t) ;
        
        op = new OpQuadPattern(graphNode, bgp) ;
        return new BindingQueryPlanSDB() ;
    }
    
    /**
     * @see com.hp.hpl.jena.sdb.graph.GraphQueryHandlerSDB.BindingQueryPlanSDB
     */
    public class BindingQueryPlanSDB implements BindingQueryPlan
    {
    	/**
    	 * @see com.hp.hpl.jena.sdb.graph.GraphQueryHandlerSDB.BindingQueryPlanSDB#executeBindings()
    	 */
        // Iterator of domain objects
        @SuppressWarnings("unchecked")
		public ExtendedIterator executeBindings()
        {
            Plan plan = PagedQueryEngineSDB.getFactory().create(op, datasetStore, null, null) ;
            QueryIterator qIter = plan.iterator() ;

            Transform<Binding, Domain> b2d = new Transform<Binding, Domain>()
            {
                public Domain convert(Binding binding)
                {
                    Domain d = new Domain(variables.length) ;
                    for ( Node n : variables )
                    {     
                        Var v = Var.alloc(n) ;
                        Node value = binding.get(v) ;
                        // Miss?
                        int idx = indexes.get(v) ;
                        d.setElement(idx, value) ;
                    }
                    return d ;
                }
            };
            Iterator it = Iter.map(qIter, b2d) ;
            return PagedWrappedIterator.create(it, this, true) ;
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