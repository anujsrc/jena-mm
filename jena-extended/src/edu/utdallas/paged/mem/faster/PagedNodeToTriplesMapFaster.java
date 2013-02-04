package edu.utdallas.paged.mem.faster;

import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.query.Applyer;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Matcher;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.mem.MatchOrBind;
import com.hp.hpl.jena.mem.TripleBunch;
import com.hp.hpl.jena.mem.faster.ProcessedTriple;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

import edu.utdallas.paged.mem.PagedGraphTripleStore;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.PagedHashCommon;
import edu.utdallas.paged.mem.PagedHashedTripleBunch;
import edu.utdallas.paged.mem.PagedNodeToTriplesMapBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskSparql;
import edu.utdallas.paged.mem.util.iterator.PagedIterator;

/**
 * The class that maintains the in-memory subject, predicate and object indexes
 * using a doubly hashed data structure
 * @author vaibhav
 */
public class PagedNodeToTriplesMapFaster extends PagedNodeToTriplesMapBase
{
	/**
	 * Constructor
	 * @param indexField - the indexing field of the triple
	 * @param f2 - the second field
	 * @param f3 - the third field
	 */
	public PagedNodeToTriplesMapFaster(Field indexField, Field f2, Field f3) { super(indexField, f2, f3); }

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#add(Triple)
	 */
	public boolean add( Triple t ) 
	{
		Object o = getIndexingField( t );
		TripleBunch s = bunchMap.get( o );
		if (s == null) bunchMap.put( o, s = new PagedHashedTripleBunch() );
		if (s.contains( t ))
			return false;
		else
		{
			s.add( t );
			size += 1; 
			bunchMap.put(o, s);
			return true; 
		} 
	}

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#remove(Triple)
	 */
	public boolean remove( Triple t )
	{ 
		Object o = getIndexingField( t );
		TripleBunch s = bunchMap.get( o );
		if (s == null || !s.contains( t ))
			return false;
		else
		{
			s.remove( t );
			size -= 1;
			if (s.size() == 0) bunchMap.remove( o );
			return true;
		} 
	}

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#iterator(Object, com.hp.hpl.jena.mem.HashCommon.NotifyEmpty)
	 */
	@SuppressWarnings("unchecked")
	public Iterator iterator( Object o, PagedHashCommon.NotifyEmpty container ) 
	{
		// System.err.println( ">> BOINK" ); // if (true) throw new JenaException( "BOINK" );
		TripleBunch s = (TripleBunch) bunchMap.get( o );
		return s == null ? NullIterator.instance() : s.iterator( container );
	}

	public class NotifyMe implements PagedHashCommon.NotifyEmpty
	{
		protected final Object key;

		public NotifyMe( Object key )
		{ this.key = key; }

		// TODO fix the way this interacts (badly) with iteration and CMEs.
		public void emptied()
		{ if (false) throw new JenaException( "BOOM" ); /* System.err.println( ">> OOPS" ); */ bunchMap.remove( key ); }
	}

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#contains(Triple)
	 */
	public boolean contains( Triple t )
	{ 
		TripleBunch s = (TripleBunch) bunchMap.get( getIndexingField( t ) );
		return s == null ? false :  s.contains( t );
	}    

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#containsBySameValueAs(Triple)
	 */
	public boolean containsBySameValueAs( Triple t )
	{ 
		TripleBunch s = (TripleBunch) bunchMap.get( getIndexingField( t ) );
		return s == null ? false :  s.containsBySameValueAs( t );
	}

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#iterator(Node, Node, Node)
	 */
	@SuppressWarnings("unchecked")
	public ExtendedIterator iterator( Node index, Node n2, Node n3 )
	{
		Object indexValue = index.getIndexingValue();
		TripleBunch s = (TripleBunch) bunchMap.get( indexValue );
		//       System.err.println( ">> ntmf::iterator: " + (s == null ? (Object) "None" : s.getClass()) );
		if( s != null && PagedGraphTripleStoreBase.readKnowledgeBase ) PagedIterator.foundInMem = true;
		return s == null
		? NullIterator.instance()
				: f2.filterOn( n2 ).and( f3.filterOn( n3 ) )
				.filterKeep( s.iterator( new NotifyMe( indexValue ) ) )
				;
	}    

	/**
	 * Method that returns an Applyer for a fixed object in the SPARQL sub-query
	 * @param Q - the processed triple
	 * @param store - the current triple store
	 * @param indexSearcher - the only lucene index searcher instance
	 * @return an Applyer instance
	 */
	public Applyer createFixedOApplyer( final ProcessedTriple Q, final PagedGraphTripleStore store, final PagedGraphTripleStoreDiskSparql indexSearcher )
	{        
		final TripleBunch ss = (TripleBunch) bunchMap.get( Q.O.node.getIndexingValue() );
		if ( ss == null && !store.cache.isFileCreated && !store.cache.isObjFileCreated )
			return Applyer.empty;
		else
		{
			return new Applyer() 
			{
				final MatchOrBind x = MatchOrBind.createSP( Q );

				public void applyToTriples( Domain d, Matcher m, StageElement next )
				{
					if( ss != null ) ss.app( d, next, x.reset(d) ); 
					if( store.cache.isFileCreated || store.cache.isObjFileCreated )
						indexSearcher.run( PagedGraphTripleStoreDiskBase.removePunctuation(Q.O.node.toString()), null, indexSearcher.objSearcher, x.reset(d), d, next);
				}
			};
		}
	}

	/**
	 * Method that returns an Applyer for a bound object in the SPARQL sub-query
	 * @param pt - the processed triple
	 * @param store - the current triple store
	 * @param indexSearcher - the only lucene index searcher instance
	 * @return an Applyer instance
	 */
	public Applyer createBoundOApplyer( final ProcessedTriple pt, final PagedGraphTripleStore store, final PagedGraphTripleStoreDiskSparql indexSearcher )
	{        
		return new Applyer()
		{
			final MatchOrBind x = MatchOrBind.createSP( pt );

			public void applyToTriples( Domain d, Matcher m, StageElement next )
			{
				TripleBunch c = (TripleBunch) bunchMap.get( pt.O.finder( d ).getIndexingValue() );
				if (c != null) c.app( d, next, x.reset(d) );
				if( store.cache.isFileCreated || store.cache.isObjFileCreated )
					indexSearcher.run( PagedGraphTripleStoreDiskBase.removePunctuation(pt.O.finder(d).toString()), null, indexSearcher.objSearcher, x.reset(d), d, next);
			}
		};
	}

	/**
	 * Method that returns an Applyer for a bound subject in the SPARQL sub-query
	 * @param pt - the processed triple
	 * @param store - the current triple store
	 * @param indexSearcher - the only lucene index searcher instance
	 * @return an Applyer instance
	 */
	public Applyer createBoundSApplyer( final ProcessedTriple pt, final PagedGraphTripleStore store, final PagedGraphTripleStoreDiskSparql indexSearcher )
	{
		return new Applyer()
		{
			final MatchOrBind x = MatchOrBind.createPO( pt );

			public void applyToTriples( Domain d, Matcher m, StageElement next )
			{
				TripleBunch c = (TripleBunch) bunchMap.get( pt.S.finder( d ) );
				if (c != null) c.app( d, next, x.reset(d) );
				if( store.cache.isFileCreated || store.cache.isSubFileCreated )
					indexSearcher.run( PagedGraphTripleStoreDiskBase.removePunctuation(pt.S.finder(d).toString()), null, indexSearcher.subSearcher, x.reset(d), d, next) ;
			}
		};
	}

	/**
	 * Method that returns an Applyer for a fixed subject in the SPARQL sub-query
	 * @param Q - the processed triple
	 * @param store - the current triple store
	 * @param indexSearcher - the only lucene index searcher instance
	 * @return an Applyer instance
	 */
	public Applyer createFixedSApplyer( final ProcessedTriple Q, final PagedGraphTripleStore store, final PagedGraphTripleStoreDiskSparql indexSearcher )
	{
		final TripleBunch ss = (TripleBunch) bunchMap.get( Q.S.node );
		if ( ss == null && !store.cache.isFileCreated && !store.cache.isSubFileCreated )
			return Applyer.empty;
		else
		{
			return new Applyer() 
			{
				final MatchOrBind x = MatchOrBind.createPO( Q );

				public void applyToTriples( Domain d, Matcher m, StageElement next )
				{ 
					if( ss != null ) ss.app( d, next, x.reset(d) );
					if( store.cache.isFileCreated || store.cache.isSubFileCreated )
						indexSearcher.run( PagedGraphTripleStoreDiskBase.removePunctuation(Q.S.node.toString()), null, indexSearcher.subSearcher, x.reset(d), d, next);
				}
			};
		}
	}

	/**
	 * Method to return the triple bunch given its index
	 * @param index - the index of the triple bunch we want
	 * @return the TripleBunch instance
	 */
	public TripleBunch get( Object index )
	{ return (TripleBunch) bunchMap.get( index ); }

	/**
	 * @see com.hp.hpl.jena.mem.faster.NodeToTriplesMapFaster#iteratorForIndexed(Object)
	 */
	@SuppressWarnings("unchecked")
	public Iterator iteratorForIndexed( Object y )
	{ return get( y ).iterator();  }
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