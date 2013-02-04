package edu.utdallas.paged.mem;

import java.io.File;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.Triple.Field;
import com.hp.hpl.jena.graph.impl.TripleStore;
import com.hp.hpl.jena.graph.query.Applyer;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.Matcher;
import com.hp.hpl.jena.graph.query.QueryNode;
import com.hp.hpl.jena.graph.query.QueryTriple;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.mem.faster.ProcessedTriple;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.algorithm.NodeStampDegreeCentrality;
import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.cache.CacheBase;
import edu.utdallas.paged.mem.cache.CacheSEfficient;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskGeneric;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskSparql;
import edu.utdallas.paged.mem.faster.PagedNodeToTriplesMapFaster;
import edu.utdallas.paged.mem.graph.impl.PagedGraph;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * The triple store that contains the triple indexed triples.
 * @author vaibhav
 */
@SuppressWarnings("unchecked")
public class PagedGraphTripleStore extends PagedGraphTripleStoreBase implements TripleStore 
{
	/**
	 * variable to check if the PagedGraphTripleStoreDiskBase instance is already created
	 */
	boolean isFirstTime = true;
	
	/**
	 * An instance of PagedGraphTripleStoreDiskBase used to search for the triple,
	 * ( Node.ANY, Node.ANY, Node.ANY ) in a SPARQL query on the lucene index
	 */
	PagedGraphTripleStoreDiskBase general;
	
	/**
	 * An instance of PagedGraphTripleStoreDiskSparql used to search for SPARQL triples
	 * on the lucene index
	 */
	PagedGraphTripleStoreDiskSparql indexSearcher = null;
	
	/**
	 * Constructor
	 * @param parent - The specific paged graph used in the triple store
	 */
	public PagedGraphTripleStore( PagedGraph parent )
	{ 
		super( parent,
				new PagedNodeToTriplesMapFaster( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
				new PagedNodeToTriplesMapFaster( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
				new PagedNodeToTriplesMapFaster( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
		); 
	}

	/**
	 * Constructor
	 * @param parent - The graph used in the triple store
	 */
	public PagedGraphTripleStore( Graph parent )
	{ 
		super( parent,
				new PagedNodeToTriplesMapFaster( Field.fieldSubject, Field.fieldPredicate, Field.fieldObject ),
				new PagedNodeToTriplesMapFaster( Field.fieldPredicate, Field.fieldObject, Field.fieldSubject ),
				new PagedNodeToTriplesMapFaster( Field.fieldObject, Field.fieldSubject, Field.fieldPredicate )
		); 
	}
	
	/**
	 * Method to get the buffer strategy used by this triple store, the default is
	 * the CacheSEfficient strategy. 
	 */
	public CacheBase getCache()
	{
		CacheBase tc = null;
		
		//The buffer can be specified as a system property or using a static variable,
		//if none of the above are used, the default is CacheSEfficient
		String cacheProperty = System.getProperty("property.cache");
		if( cacheProperty != null)
		{
			try { tc = (CacheBase)Class.forName(cacheProperty).newInstance(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if( ExtendedJenaParameters.buffer != null )
		{
			try { tc = (CacheBase)Class.forName(ExtendedJenaParameters.buffer).newInstance(); }
			catch (Exception e) { e.printStackTrace(); }			
		}
		else { tc = new CacheSEfficient(); }
		return tc;		
	}
	
	/**
	 * Method to get the memory management algorithm used by the buffer strategy,
	 * the default is the degree centrality algorithm
	 */
	public CacheAlgorithmBase getAlgorithm()
	{ 
		CacheAlgorithmBase tc = null;
		
		//The memory management algorithm can be specified as a system property or using a static variable,
		//if none of the above are used, the default is degree centrality
		String algorithmProperty = System.getProperty("property.algorithm");
		if( algorithmProperty != null)
		{
			try { tc = (CacheAlgorithmBase)Class.forName(algorithmProperty).newInstance(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if( ExtendedJenaParameters.algorithm != null )
		{
			try { tc = (CacheAlgorithmBase)Class.forName(ExtendedJenaParameters.algorithm).newInstance(); }
			catch (Exception e) { e.printStackTrace(); }			
		}
		else
		{ tc = new NodeStampDegreeCentrality(); }
		return tc;
	}

	/**
	 * Method to return the subjects data structure for this triple store
	 * @return the subjects data structure
	 */
	public PagedNodeToTriplesMapFaster getSubjects()
	{ return (PagedNodeToTriplesMapFaster) subjects; }

	/**
	 * Method to return the predicates data structure for this triple store
	 * @return the predicates data structure
	 */
	public PagedNodeToTriplesMapFaster getPredicates()
	{ return (PagedNodeToTriplesMapFaster) predicates; }

	/**
	 * Method to return the objects data structure for this triple store
	 * @return the objects data structure
	 */
	public PagedNodeToTriplesMapFaster getObjects()
	{ return (PagedNodeToTriplesMapFaster) objects; }

	/**
	 * Method to create applyers for every processed triple of a SPARQL query
	 * @param pt - a processed triple that is a sub-part of the SPARQL query 
	 * @return an applyer for the specific processed triple
	 */
	public Applyer createApplyer( ProcessedTriple pt )
	{
		//Create only a single instance of the indexSearcher to be used across all SPARQL queries.
		if ( indexSearcher == null && ( cache.isFileCreated || cache.isSubFileCreated || cache.isPredFileCreated || cache.isObjFileCreated ) )
		{
			if( cache.isFileCreated || cache.isSubFileCreated  ) indexSearcher = new PagedGraphTripleStoreDiskSparql( (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache.isObjFileCreated ) indexSearcher = new PagedGraphTripleStoreDiskSparql( null, (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
			else if( cache.isPredFileCreated ) indexSearcher = new PagedGraphTripleStoreDiskSparql( null, (File)cache.getFiles()[1], null );
		}
		if (pt.hasNoVariables())
			return containsApplyer( pt );
		if (pt.S instanceof QueryNode.Fixed) 
			return getSubjects().createFixedSApplyer( pt, this, indexSearcher );
		if (pt.O instanceof QueryNode.Fixed) 
			return getObjects().createFixedOApplyer( pt, this, indexSearcher );
		if (pt.S instanceof QueryNode.Bound) 
			return getSubjects().createBoundSApplyer( pt, this, indexSearcher );
		if (pt.O instanceof QueryNode.Bound) 
			return getObjects().createBoundOApplyer( pt, this, indexSearcher );
		return varSvarOApplyer( pt );
	}

	/**
	 * Method to create an applyer when the processed triple has no variables
	 * @param pt - the processed triple
	 * @return an applyer for this processed triple
	 */
	protected Applyer containsApplyer( final ProcessedTriple pt )
	{ 
		return new Applyer()
		{
			public void applyToTriples( Domain d, Matcher m, StageElement next )
			{
				Triple t = new Triple( pt.S.finder( d ), pt.P.finder( d ), pt.O.finder( d ) );
				if (subjects.containsBySameValueAs(t)) next.run( d );
				if( cache.isFileCreated || cache.isSubFileCreated )
					indexSearcher.run( PagedGraphTripleStoreDiskBase.removePunctuation(pt.S.finder(d).toString()), t, indexSearcher.subSearcher, null, d, next);
			}    
		};
	}

	/**
	 * Method to create an applyer when the processed triple has both a subject and object variable
	 * @param pt - the processed triple
	 * @return an applyer for this processed triple
	 */
	protected Applyer varSvarOApplyer( final QueryTriple pt )
	{ 
		return new Applyer()
		{
			protected final QueryNode p = pt.P;

			public Iterator find( Domain d )
			{
				Node P = p.finder( d );
				if (P.isConcrete())
					return predicates.iterator( P, Node.ANY, Node.ANY );
				else
					return subjects.iterateAll();
			}

			public void applyToTriples( Domain d, Matcher m, StageElement next )
			{
				//First check the in-memory subgraph
				Iterator it = find( d );
				while (it.hasNext())
					if (m.match( d, (Triple) it.next() )) 
						next.run( d );
				
				//Then check the lucene index for Triple.ANY
				Triple t = Triple.create(pt.S.finder(d), pt.P.finder(d), pt.O.finder(d));
				while( ( cache.isFileCreated || cache.isSubFileCreated ) && pt.S.finder(d).matches(Node.ANY) && pt.P.finder(d).matches(Node.ANY) && pt.O.finder(d).matches(Node.ANY))
				{
					if( isFirstTime ) general = new PagedGraphTripleStoreDiskGeneric( t, (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
					isFirstTime = false;
					
					it = general.find();
					if( it== null || !it.hasNext() ) break;
					while (it.hasNext())
						if (m.match( d, (Triple) it.next() )) 
							next.run( d );					
				}
				
				//Check the lucene index for ( Node.ANY, P, Node.ANY )
				if( ( cache.isFileCreated || cache.isSubFileCreated || cache.isPredFileCreated || cache.isObjFileCreated ) && ( !pt.S.finder(d).matches(Node.ANY) || !pt.P.finder(d).matches(Node.ANY) || !pt.O.finder(d).matches(Node.ANY)))
				{
					if( cache.isFileCreated || cache.isSubFileCreated  ) general = new PagedGraphTripleStoreDiskGeneric( t, (File)cache.getFiles()[0], (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
					else if( cache.isObjFileCreated ) general = new PagedGraphTripleStoreDiskGeneric( t, null, (File)cache.getFiles()[1], (File)cache.getFiles()[2] );
					else if( cache.isPredFileCreated ) general = new PagedGraphTripleStoreDiskGeneric( t, null, (File)cache.getFiles()[1], null );
					it = general.find();
					while (it!= null && it.hasNext())
						if (m.match( d, (Triple) it.next() )) 
							next.run( d );					
				}				
			}
		};
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