package edu.utdallas.paged.mem;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.mem.GraphMemBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.graph.query.PagedSimpleQueryHandler;

/**
 * The query handler class for the extended in-memory model
 * @author vaibhav
 */
public class PagedGraphMemBaseQueryHandler extends PagedSimpleQueryHandler
{
	/**
	 * The store used by the current graph
	 */
	protected final PagedGraphTripleStore store;

	/**
	 * Constructor
	 * @param graph - the graph for which this query handler is created
	 */
	public PagedGraphMemBaseQueryHandler( GraphMemBase graph )
	{ super( graph, (PagedGraphTripleStore)graph.store ); this.store = (PagedGraphTripleStore) graph.store; }

	/**
	 * Method that looks for all objects that match a given subject and predicate
	 * @param s - the subject node
	 * @param p - the predicate node
	 * @return a null iterator or an iterator that contains objects whose parent triples contain the given subject and predicate
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator objectsFor( Node s, Node p )
	{ return bothANY( s, p ) ? findObjects() : super.objectsFor( s, p ); }

	/**
	 * Method that looks for all predicates that match a given subject and object
	 * @param s - the subject node
	 * @param o - the object node
	 * @return a null iterator or an iterator that contains predicates whose parent triples contain the given subject and object
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator predicatesFor( Node s, Node o )
	{ return bothANY( s, o ) ? findPredicates() : super.predicatesFor( s, o ); }

	/**
	 * Method that looks for all subjects that match a given predicate and object 
	 * @param p - the predicate node
	 * @param o - the object node
	 * @return a null iterator or an iterator that contains subjects whose parent triples contain the given predicate and object
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator subjectsFor( Node p, Node o )
	{ return bothANY( p, o ) ? findSubjects() : super.subjectsFor( p, o ); }

	/**
         Answer true iff both <code>a</code> and <code>b</code> are ANY wildcards
         or are null (legacy). 
	 */
	private boolean bothANY( Node a, Node b )
	{ return (a == null || a.equals( Node.ANY )) && (b == null || b.equals( Node.ANY )); }

	/**
	 * Method to find all predicates in this store
	 * @return an iterator that is null or contains all predicates
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator findPredicates()
	{ return store.listPredicates(); }

	/**
	 * Method to find all objects in this store
	 * @return an iterator that is null or contains all objects
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator findObjects()
	{ return store.listObjects(); }

	/**
	 * Method to find all subjects in this store
	 * @return an iterator that is null or contains all subjects
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator findSubjects()
	{ return store.listSubjects(); }
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