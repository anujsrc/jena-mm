package edu.utdallas.paged.mem.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.Searcher;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.PagedHashedTripleBunch;
import edu.utdallas.paged.mem.PagedNodeToTriplesMapBase;
import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;

/**
 * Abstract class that defines the base methods for any buffer
 * @author vaibhav
 */
public abstract class CacheBase 
{
	/**
	 * The actual buffer is maintained as a hash map
	 */
	public LinkedHashMap<Object, CacheAlgorithmBase> hashMap = null;

	/**
	 * The size of the current buffer
	 */
	public static int size = 0;
	
	/** A variable to test if Lucene indexes are already created */
	public boolean isFileCreated = false, isSubFileCreated = false, isPredFileCreated = false, isObjFileCreated = false;
	
	/** Check if the object Lucene index is created */ 
	public boolean writeObject = false;

	/** Check if the subject Lucene index is created */ 
	public boolean writeSubject = false;

	/** The temporary subject Lucene index */
	public File subF = null;

	/** The temporary predicate Lucene index */
	public File predF = null;
	
	/** The temporary object Lucene index */
	public File objF = null;

	/** An index writer to create the subject Lucene index */
	public IndexWriter subWriter;

	/** An index writer to create the predicate Lucene index */
	public IndexWriter predWriter;
	
	/** An index writer to create the object Lucene index */
	public IndexWriter objWriter;

	/** The delay factor determines the amount of triples to be left in memory */
	public int delayFactor = 0;

	/** The graph id */
	public static int graphId = 0;
	
	/** The current timestamp value **/
	public long timestamp = 0L;
	
	/** An identifier that determines the position of a given URI in a Lucene index **/
	public int id = 0;

	/** A N-Triple writer to write triples to a Lucene index */
	public PagedNTripleWriter luceneWriter = null;
	
	/** An index reader used in updating the subject Lucene index */
	IndexReader subReader = null;

	/** An index reader used in updating the predicate Lucene index */
	IndexReader predReader = null; 
	
	/** An index reader used in updating the object Lucene index */
	IndexReader objReader = null;
	
	/** An index searcher used in updating the subject Lucene index */
	Searcher subSearcher = null; 

	/** An index searcher used in updating the predicate Lucene index */
	Searcher predSearcher = null;
	
	/** An index searcher used in updating the object Lucene index */
	Searcher objSearcher = null;
	
	/** Constructor initializes the hash map with the key as the node (as an Object) and the memory management algorithm as the value */
	public CacheBase() { hashMap = new LinkedHashMap<Object, CacheAlgorithmBase>(10, 0.75f); }

	/** Method that returns the files used by this cache */
	public abstract Object[] getFiles();
	
	/** Method that updates the cache based on the current view of subjects and objects, the given algorithm, and, the triple t */
	public abstract void updateCache( PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects, CacheAlgorithmBase algoSub, CacheAlgorithmBase algoPred, CacheAlgorithmBase algoObj, Triple t ) ;
	
	/** Method that writes the triples to disk based on the accumulator size */
	public abstract void writeToDisk( PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects ) ;
	
	/** 
	 * Method to return the current time with homogenized to seconds
	 * @return A Calendar instance with the current time with the millisecond field cleared 
	 */
	public Calendar updateTime()
	{ Calendar time = Calendar.getInstance(); time.setTime( new Date() ); time.clear(Calendar.MILLISECOND); return time; }

	/** 
	 * Method to create a new cache or update an existing cache entry based on the updated connections 
	 * of this node and the current time
	 * @param algo - the memory management algorithm that we want to use with this buffer
	 * @return the updated cache entry 
	 */
	public CacheAlgorithmBase createCacheEntry( CacheAlgorithmBase algo )
	{ algo.setConnections(); if( PagedGraphTripleStoreBase.readKnowledgeBase ) { algo.setCurrentTime( updateTime() ); } return algo; }

	/** Method to remove punctuations */
	public String removePunctuation(String input)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++) 
		{ 
			if (input.charAt(i) == 42 || (input.charAt(i) >= 65 && input.charAt(i) <= 90) || (input.charAt(i) >= 97 && input.charAt(i) <= 122) || (input.charAt(i) >= 48 && input.charAt(i) <= 57)) 
				sb = sb.append(input.charAt(i));
		}
		return sb.toString();
	}

	/** Method to get the key value from the cache based on a value */
	public Object getKeyFromValue(LinkedHashMap<Object, CacheAlgorithmBase> hm, CacheAlgorithmBase value)
	{
		for(Object o:hm.keySet())
		{
			if(hm.get(o).equals(value)) 
				return o;
		}
		return null;
	}

	/** Method to remove the triples from memory and return the new size of the nodes structure */
	public int removeTripleBunch( PagedNodeToTriplesMapBase nodes, PagedHashedTripleBunch tb, int index )
	{
		int triplesSize = nodes.size() - tb.size(); tb.removeAll();
		nodes.bunchMap.setKey( index, null ); nodes.bunchMap.setTripleBunch( index, null ); nodes.bunchMap.setSize();
		return triplesSize;
	}
	
	/** Method to create an arraylist from the triples taken from the subject, predicate, and object memory structures */
	@SuppressWarnings("unchecked")
	public ArrayList<Triple> getTriples( Object key, PagedHashedTripleBunch subTb, PagedHashedTripleBunch predTb, PagedHashedTripleBunch objTb )
	{
		ExtendedIterator subIter = null, predIter = null, objIter = null; ArrayList<Triple> trArr = new ArrayList<Triple>();
		if( subTb != null ) { subIter = subTb.iterator(); }
		if( predTb != null ) { predIter = predTb.iterator(); }
		if( objTb != null ) { objIter = objTb.iterator(); }
		while( subIter != null && subIter.hasNext() ) { Triple t = (Triple)subIter.next(); if( !trArr.contains(t) ) { trArr.add( t ); } }
		while( predIter != null && predIter.hasNext() ) { Triple t = (Triple)predIter.next(); if( !trArr.contains(t) ) { trArr.add( t ); } }
		while( objIter != null && objIter.hasNext() ) { Triple t = (Triple)objIter.next(); if( !trArr.contains(t) ) { trArr.add( t ); } }
		return trArr;
	}
	
	/** Method to calculate the individual clustering coefficient for every node */
	public void calculateIndividualClustering( List<CacheAlgorithmBase> o, PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects, boolean isPredicateUsed, boolean isObjectUsed )
	{
		for( int x = 0; x < o.size(); x++ )
		{
			ArrayList<CacheAlgorithmBase> jNodes = new ArrayList<CacheAlgorithmBase>(); int fractionIJK = 0; PagedHashedTripleBunch iPredTb = null, iObjTb = null;
			CacheAlgorithmBase iNode = o.get(x); Object iKey = getKeyFromValue(hashMap, iNode);
			if( isPredicateUsed ) iPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(iKey); 
			if( isObjectUsed )    iObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(iKey); 
			ArrayList<Triple> iTriple = getTriples( iKey, (PagedHashedTripleBunch)subjects.bunchMap.get(iKey), iPredTb, iObjTb );
			
			for( int y = 0; y < iTriple.size(); y++ )
			{
				Triple a = iTriple.get(y);
				Object key = null;
				if( subjects.getIndexingField(a).equals(iKey) ) 		key = objects.getIndexingField(a); 
				if( objects.getIndexingField(a).equals(iKey) ) 		key = subjects.getIndexingField(a);
				if( hashMap.containsKey(key) && !jNodes.contains(hashMap.get(key)) ) 	jNodes.add( hashMap.get(key) );
			}
			
			for( int z = 0; z < jNodes.size(); z++ )
			{
				PagedHashedTripleBunch jPredTb = null, jObjTb = null;
				CacheAlgorithmBase jNode = jNodes.get(z); Object jKey = getKeyFromValue(hashMap, jNode);
				if( isPredicateUsed ) jPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(jKey); 
				if( isObjectUsed )    jObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(jKey); 
				ArrayList<Triple> jTriple = getTriples( jKey, (PagedHashedTripleBunch)subjects.bunchMap.get(jKey), jPredTb, jObjTb );
				for( int a = z+1; a < jNodes.size(); a++ )
				{
					PagedHashedTripleBunch kPredTb = null, kObjTb = null;
					CacheAlgorithmBase kNode = jNodes.get(a); Object kKey = getKeyFromValue(hashMap, kNode);
					if( isPredicateUsed ) kPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(kKey); 
					if( isObjectUsed )    kObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(kKey); 
					ArrayList<Triple> kTriple = getTriples( kKey, (PagedHashedTripleBunch)subjects.bunchMap.get(kKey), kPredTb, kObjTb );
					for( int m = 0; m < jTriple.size(); m++ )
					{
						Triple b = jTriple.get(m);
						for( int n = 0; n < kTriple.size(); n++ )
						{
							Triple c = kTriple.get(n);
							if( b.matches(c) ) { fractionIJK++; }
						}
					}
				}
			}
			if( jNodes.size() >= 2 ) hashMap.get(iKey).setIndividualCC(fractionIJK*1.0/((iTriple.size()*(iTriple.size()-1))/2));
		}
	}
	
	/** Method to calculate the transitive clustering coefficient for every node */
	public void calculateTransitiveClustering( List<CacheAlgorithmBase> o, PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects, boolean isPredicateUsed, boolean isObjectUsed )
	{
		for( int x = 0; x < o.size(); x++ )
		{
			ArrayList<CacheAlgorithmBase> jNodes = new ArrayList<CacheAlgorithmBase>(); int fractionIJK = 0, totalIJK = 0; PagedHashedTripleBunch iPredTb = null, iObjTb = null;
			CacheAlgorithmBase iNode = o.get(x); Object iKey = getKeyFromValue(hashMap, iNode);
			if( isPredicateUsed ) iPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(iKey); 
			if( isObjectUsed )    iObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(iKey); 
			ArrayList<Triple> iTriple = getTriples( iKey, (PagedHashedTripleBunch)subjects.bunchMap.get(iKey), iPredTb, iObjTb );

			for( int y = 0; y < iTriple.size(); y++ )
			{
				Triple a = iTriple.get(y);
				Object key = null;
				if( subjects.getIndexingField(a).equals(iKey) ) 		key = objects.getIndexingField(a); 
				if( objects.getIndexingField(a).equals(iKey) ) 		key = subjects.getIndexingField(a);
				if( hashMap.containsKey(key) && !jNodes.contains(hashMap.get(key)) ) 	jNodes.add( hashMap.get(key) );
			}
	
			for( int z = 0; z < jNodes.size(); z++ )
			{
				PagedHashedTripleBunch jPredTb = null, jObjTb = null;
				CacheAlgorithmBase jNode = jNodes.get(z); Object jKey = getKeyFromValue(hashMap, jNode); 
				if( isPredicateUsed ) jPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(jKey); 
				if( isObjectUsed )    jObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(jKey); 
				ArrayList<Triple> jTriple = getTriples( jKey, (PagedHashedTripleBunch)subjects.bunchMap.get(jKey), jPredTb, jObjTb );
				ArrayList<CacheAlgorithmBase> kNodes = new ArrayList<CacheAlgorithmBase>(); 
				for( int y = 0; y < jTriple.size(); y++ )
				{
					Triple a = jTriple.get(y);
					Object key = null;
					if( subjects.getIndexingField(a).equals(jKey) ) 		key = objects.getIndexingField(a); 
					if( objects.getIndexingField(a).equals(jKey) ) 		key = subjects.getIndexingField(a);
					if( hashMap.containsKey(key) && !kNodes.contains(hashMap.get(key)) && !iKey.equals(key) ) 	kNodes.add( hashMap.get(key) );
				}
				
				for( int a = 0; a < kNodes.size(); a++ )
				{
					totalIJK++;
					PagedHashedTripleBunch kPredTb = null, kObjTb = null;
					CacheAlgorithmBase kNode = kNodes.get(a); Object kKey = getKeyFromValue(hashMap, kNode);
					if( isPredicateUsed ) kPredTb = (PagedHashedTripleBunch)predicates.bunchMap.get(kKey); 
					if( isObjectUsed )    kObjTb = (PagedHashedTripleBunch)objects.bunchMap.get(kKey); 					
					ArrayList<Triple> kTriple = getTriples( kKey, (PagedHashedTripleBunch)subjects.bunchMap.get(kKey), kPredTb, kObjTb );
					for( int b = 0; b < kTriple.size(); b++ )
					{
						Triple m = kTriple.get(b);
						for( int c = 0; c < iTriple.size(); c++ )
						{
							Triple n = iTriple.get(c);
							if( m.matches(n) ) { fractionIJK++; }
						}
					}
				}
			}
			if( jNodes.size() >= 1 ) hashMap.get(iKey).setTransitiveCC(fractionIJK*1.0/totalIJK);
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