package edu.utdallas.paged.extension;

import java.util.Iterator;

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

import edu.utdallas.paged.mem.CacheAlgorithmBase;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.faster.PagedNodeToTriplesMapFaster;

public class PagedTripleStoreForAlgorithm extends PagedGraphTripleStoreBase implements TripleStore
{
	public PagedTripleStoreForAlgorithm( PagedGraphForAlgorithm pagedGraphForAlgorithm )
	{ 
		super( pagedGraphForAlgorithm,
				new PagedNodeToTriplesMapFaster( Field.getSubject, Field.getPredicate, Field.getObject ),
				new PagedNodeToTriplesMapFaster( Field.getPredicate, Field.getObject, Field.getSubject ),
				new PagedNodeToTriplesMapFaster( Field.getObject, Field.getSubject, Field.getPredicate )
		); 
	}

	public CacheAlgorithmBase getAlgorithm()
	{
		CacheAlgorithmBase tc = null;
		String algorithmProperty = System.getProperty("property.algorithm");
		if( algorithmProperty != null)
		{
			try { tc = (CacheAlgorithmBase)Class.forName(algorithmProperty).newInstance(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else
		{
			tc = new NodeStampConnection();	
		}
		return tc;
	}
	
	public PagedNodeToTriplesMapFaster getSubjects()
	{ return (PagedNodeToTriplesMapFaster) subjects; }

	public PagedNodeToTriplesMapFaster getPredicates()
	{ return (PagedNodeToTriplesMapFaster) predicates; }

	public PagedNodeToTriplesMapFaster getObjects()
	{ return (PagedNodeToTriplesMapFaster) objects; }

	public Applyer createApplyer( ProcessedTriple pt )
	{
		if (pt.hasNoVariables())
			return containsApplyer( pt );
		if (pt.S instanceof QueryNode.Fixed) 
			return getSubjects().createFixedSApplyer( pt );
		if (pt.O instanceof QueryNode.Fixed) 
			return getObjects().createFixedOApplyer( pt );
		if (pt.S instanceof QueryNode.Bound) 
			return getSubjects().createBoundSApplyer( pt );
		if (pt.O instanceof QueryNode.Bound) 
			return getObjects().createBoundOApplyer( pt );
		return varSvarOApplyer( pt );
	}

	protected Applyer containsApplyer( final ProcessedTriple pt )
	{ 
		return new Applyer()
		{
			public void applyToTriples( Domain d, Matcher m, StageElement next )
			{
				Triple t = new Triple( pt.S.finder( d ), pt.P.finder( d ), pt.O.finder( d ) );
				if (objects.containsBySameValueAs( t )) next.run( d );
			}    
		};
	}

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
				Iterator it = find( d );
				while (it.hasNext())
					if (m.match( d, (Triple) it.next() )) 
						next.run( d );
			}
		};
	}
}
/** Copyright (c) 2008, The University of Texas at Dallas
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