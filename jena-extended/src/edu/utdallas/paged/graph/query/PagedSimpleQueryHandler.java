package edu.utdallas.paged.graph.query;

import java.util.Set;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.BindingQueryPlan;
import com.hp.hpl.jena.graph.query.ExpressionSet;
import com.hp.hpl.jena.graph.query.Mapping;
import com.hp.hpl.jena.graph.query.PatternStage;
import com.hp.hpl.jena.graph.query.Query;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.graph.query.SimpleQueryPlan;
import com.hp.hpl.jena.graph.query.SimpleTreeQueryPlan;
import com.hp.hpl.jena.graph.query.Stage;
import com.hp.hpl.jena.graph.query.TreeQueryPlan;
import com.hp.hpl.jena.util.CollectionFactory;
import com.hp.hpl.jena.util.iterator.ClosableIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.mem.PagedGraphTripleStore;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskSpecific;
import edu.utdallas.paged.mem.util.iterator.PagedIterator;
import edu.utdallas.paged.mem.util.iterator.PagedWrappedIterator;

/**
 * A class that is used to query the underlying graph for this model
 * @author vaibhav
 */
public class PagedSimpleQueryHandler implements QueryHandler
{
	/** the Graph this handler is working for */
	protected Graph graph;
	
	/** the triple store this handler is working for */
	protected PagedGraphTripleStore store;

	/**
	 * Constructor
	 * @param graph - the graph this handler is working for
	 * @param store - the triple store this handler is working for
	 */
	public PagedSimpleQueryHandler( Graph graph, PagedGraphTripleStore store )
	{ this.graph = graph; this.store = store; }

	/**
	 * Method to create a pattern stage for the given query
	 * @param map - the mapping
	 * @param constraints - the set of constraints
	 * @param t - the array of triple patterns we are looking for
	 * @return a Stage instance
	 */
	public Stage patternStage( Mapping map, ExpressionSet constraints, Triple [] t )
	{ return new PatternStage( graph, map, constraints, t ); }

	/**
	 * Method to create the bindings given the set of nodes and the query
	 * @param q - the SPARQL query
	 * @param variables - the set of nodes
	 * @return a binding query plan
	 */
	public BindingQueryPlan prepareBindings( Query q, Node [] variables )   
	{ return new SimpleQueryPlan( graph, q, variables ); }

	/**
	 * Method to generate a query tree plan
	 * @param pattern - the pattern as a graph
	 * @return a tree query plan
	 */
	public TreeQueryPlan prepareTree( Graph pattern )
	{ return new SimpleTreeQueryPlan( graph, pattern ); }

	/**
	 * Method to find objects given the the node <code>s</code> and node <code>p</code>
	 * @param s - the subject node
	 * @param p - the predicate node
	 * @return a null iterator or an iterator that contains the object nodes
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator objectsFor( Node s, Node p )
	{ return objectsFor( graph, s, p, store ); }

	/**
	 * Method to find subjects given the the node <code>p</code> and node <code>o</code>
	 * @param p - the predicate node
	 * @param o - the object node
	 * @return a null iterator or an iterator that contains the subject nodes
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator subjectsFor( Node p, Node o )
	{ return subjectsFor( graph, p, o, store ); }
	
	/**
	 * Method to find predicates given the the node <code>s</code> and node <code>o</code>
	 * @param s - the subject node
	 * @param o - the object node
	 * @return a null iterator or an iterator that contains the predicate nodes
	 */	
	@SuppressWarnings("unchecked")
	public ExtendedIterator predicatesFor( Node s, Node o )
	{ return predicatesFor( graph, s, o, store ); }

	/**
	 * Method to find objects given the the graph, node <code>s</code>, node <code>p</code>, and the triple store
	 * @param g - the graph we want to search
	 * @param s - the subject node
	 * @param p - the predicate node
	 * @param store - the triple store for the graph
	 * @return a null iterator or an iterator that contains the object nodes
	 */
	@SuppressWarnings("unchecked")
	public static ExtendedIterator objectsFor( Graph g, Node s, Node p, PagedGraphTripleStore store )
	{ 
		PagedIterator.isAllNodeSearch = true;
		Set<Node> objects = CollectionFactory.createHashedSet();
		ClosableIterator it = g.find( s, p, Node.ANY );
		PagedIterator.isSpecificSearch = true;
		while (it.hasNext()) objects.add( ((Triple) it.next()).getObject() );
		PagedIterator.isSpecificSearch = false;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.create(s, p, Node.ANY), store, false, false, true ); 	
		return PagedWrappedIterator.createNoRemove( objects.iterator(), specific );
	}

	/**
	 * Method to find subjects given the the graph, node <code>p</code>, node <code>o</code>, and the triple store
	 * @param g - the graph we want to search
	 * @param p - the predicate node
	 * @param o - the object node
	 * @param store - the triple store for the graph
	 * @return a null iterator or an iterator that contains the subject nodes
	 */
	@SuppressWarnings("unchecked")
	public static ExtendedIterator subjectsFor( Graph g, Node p, Node o, PagedGraphTripleStore store )
	{ 
		PagedIterator.isAllNodeSearch = true;
		Set<Node> objects = CollectionFactory.createHashedSet();
		ClosableIterator it = g.find( Node.ANY, p, o );
		PagedIterator.isSpecificSearch = true;
		while (it.hasNext()) objects.add( ((Triple) it.next()).getSubject() );
		PagedIterator.isSpecificSearch = false;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.create(Node.ANY, p, o), store, true, false, false );	
		return PagedWrappedIterator.createNoRemove( objects.iterator(), specific );
	}

	/**
	 * Method to find predicates given the the graph, node <code>s</code>, node <code>o</code>, and the triple store
	 * @param g - the graph we want to search
	 * @param s - the subject node
	 * @param o - the object node
	 * @param store - the triple store for the graph
	 * @return a null iterator or an iterator that contains the predicate nodes
	 */
	@SuppressWarnings("unchecked")
	public static ExtendedIterator predicatesFor( Graph g, Node s, Node o, PagedGraphTripleStore store )
	{
		PagedIterator.isAllNodeSearch = true;
		Set<Node> predicates = CollectionFactory.createHashedSet();
		ClosableIterator it = g.find( s, Node.ANY, o );
		PagedIterator.isSpecificSearch = true;
		while (it.hasNext()) predicates.add( ((Triple) it.next()).getPredicate() );
		PagedIterator.isSpecificSearch = false;
		PagedGraphTripleStoreDiskBase specific = new PagedGraphTripleStoreDiskSpecific( Triple.create(s, Node.ANY, o), store, false, true, false );	
		return PagedWrappedIterator.createNoRemove( predicates.iterator(), specific );
	}

	/**
	 * @see com.hp.hpl.jena.graph.query.SimpleQueryHandler#containsNode(Node)
	 */
	public boolean containsNode( Node n )
	{
		return 
		graph.contains( n, Node.ANY, Node.ANY )
		|| graph.contains( Node.ANY, n, Node.ANY )
		|| graph.contains( Node.ANY, Node.ANY, n )
		;
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