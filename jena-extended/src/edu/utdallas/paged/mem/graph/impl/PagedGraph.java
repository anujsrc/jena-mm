package edu.utdallas.paged.mem.graph.impl;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphStatisticsHandler;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Reifier;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.Applyer;
import com.hp.hpl.jena.graph.query.QueryHandler;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.mem.faster.GraphMemFaster;
import com.hp.hpl.jena.mem.faster.ProcessedTriple;
import com.hp.hpl.jena.shared.ReificationStyle;

import edu.utdallas.paged.mem.PagedGraphTripleStore;
import edu.utdallas.paged.mem.faster.PagedGraphMemFasterQueryHandler;
import edu.utdallas.paged.mem.faster.PagedNodeToTriplesMapFaster;

/**
 * The PagedGraph class that defines the underlying graph for the extended in-memory model
 * @author vaibhav
 *
 */
public class PagedGraph extends GraphMemFaster implements Graph
{
	/**
	 * Constructor
	 * @param style - the reification style for this graph
	 */
	public PagedGraph( ReificationStyle style )
	{ super( style ); }

	/**
	 * Method to create the triple store to be used by his graph
	 * @return a TripleStore instance
	 */
	@Override
	protected TripleStore createTripleStore() 
	{ return new PagedGraphTripleStore( this ); }

	/**
	 * Method to create the query handler used by this graph
	 * @return a QueryHandler instance
	 */
	public QueryHandler queryHandler()
	{ 
		if (queryHandler == null) queryHandler = new PagedGraphMemFasterQueryHandler( this );
		return queryHandler;
	}

	/**
	 * Method to create a statistics handler for this graph
	 * @return a GraphStatisticsHandler instance
	 */
	protected GraphStatisticsHandler createStatisticsHandler()
	{ return new PagedGraphMemFasterStatisticsHandler( (PagedGraphTripleStore) store, getReifier() ); }

	/**
	 * Method to return the triple store used by this graph
	 * @return a TripleStore instance for the current graph
	 */
	public TripleStore getStore()
	{ return this.store; }

	/**
	 * Static class that defines the statistics handler for this graph
	 * @author vaibhav
	 */
	public static class PagedGraphMemFasterStatisticsHandler implements GraphStatisticsHandler
	{
		/**
		 * The triple store used by the current graph
		 */
		public final PagedGraphTripleStore store;
		
		/**
		 * The reifier used by the graph
		 */
		public final Reifier reifier;

		/**
		 * Constructor
		 * @param store - the store for the current graph
		 * @param reifier - the reifier for the current graph
		 */
		public PagedGraphMemFasterStatisticsHandler( PagedGraphTripleStore store, Reifier reifier )
		{ this.store = store; this.reifier = reifier; }

		/**
		 * Static class directly taken from Jena
		 * @author vaibhav
		 */
		private static class C
		{	
			static final int NONE = 0;
			static final int S = 1, P = 2, O = 4;
			static final int SP = S + P, SO = S + O, PO = P + O;
			static final int SPO = S + P + O;
		}

		/**
   			@see com.hp.hpl.jena.mem.faster.GraphMemFaster#getStatistic(com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node, com.hp.hpl.jena.graph.Node)
		 */
		public long getStatistic( Node S, Node P, Node O )
		{
			if (reifier.size() > 0) return -1;
			int concrete = (S.isConcrete() ? C.S : 0) + (P.isConcrete() ? C.P : 0) + (O.isConcrete() ? C.O : 0);
			switch (concrete)
			{
				case C.NONE: return store.size();

				case C.S:    return countInMap( S, store.getSubjects() );

				case C.SP:   return countsInMap( S, store.getSubjects(), P, store.getPredicates() );

				case C.SO:   return countsInMap( S, store.getSubjects(), O, store.getObjects() );

				case C.P:    return countInMap( P, store.getPredicates() );

				case C.PO:   return countsInMap( P, store.getPredicates(), O, store.getObjects() );

				case C.O:    return countInMap( O, store.getObjects() );

				case C.SPO:  return store.contains( Triple.create( S, P, O ) ) ? 1 : 0;
			}
			return -1;
		}

		public long countsInMap( Node a, PagedNodeToTriplesMapFaster mapA, Node b, PagedNodeToTriplesMapFaster mapB )
		{
			long countA = countInMap( a, mapA ), countB = countInMap( b, mapB );
			return countA == 0 || countB == 0 ? 0 : -1L;
		}

		public long countInMap( Node n, PagedNodeToTriplesMapFaster map )
		{
			TripleBunch b = map.get( n.getIndexingValue() );
			return b == null ? 0 : b.size();
		}
	}

	/**
	 * Create an applyer given a ProcessedTriple that is a su-part of a SPARQL query
	 * @param pt - the processed triple
	 * @return an Applyer for the ProcessedTriple
	 */
	public Applyer createApplyer( ProcessedTriple pt )
	{
		Applyer plain = ((PagedGraphTripleStore) store).createApplyer( pt );
		return matchesReification( pt ) && hasReifications() ? withReification( plain, pt ) : plain;
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