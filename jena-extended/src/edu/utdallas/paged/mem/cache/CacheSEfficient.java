package edu.utdallas.paged.mem.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import edu.utdallas.paged.mem.PagedHashedTripleBunch;
import edu.utdallas.paged.mem.PagedNodeToTriplesMapBase;
import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * A specific implementation of the buffer based on caching the values of subjects, predicates and objects
 * as needed
 * @author vaibhav
 */
public class CacheSEfficient extends CacheBase
{
	/** Constructor */
	public CacheSEfficient()
	{ 
		super();
		this.luceneWriter = new PagedNTripleWriter(); this.id = ++CacheSubject.graphId; this.timestamp = System.currentTimeMillis();
		String delayFactorProperty = System.getProperty("property.delayFactor");
		if( delayFactorProperty != null)
		{
			try { this.delayFactor = new Integer(delayFactorProperty).intValue(); }
			catch (Exception e) { e.printStackTrace(); }
		}
		else if( ExtendedJenaParameters.delayFactor >= 0 )
		{ this.delayFactor = ExtendedJenaParameters.delayFactor; }
		else
		{ this.delayFactor = 10; }
	}
	
	/**
	 * Method to return the indexes
	 * @return The indexes as an array of Objects 
	 */
	public Object[] getFiles()
	{ Object[] files = {subF, predF, objF}; return files; }

	/**
	 * Method to update entries in the buffer based on the current triple
	 * @param subjects - the subjects data structure
	 * @param predicates - the predicates data structure
	 * @param objects - the objects data structure
	 * @param algoSub - the algorithm instance to be used with subjects
	 * @param algoPred - the algorithm instance to be used with predicates
	 * @param algoObj - the algorithm instance to be used with objects
	 */
	public void updateCache(PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects, CacheAlgorithmBase algoSub, CacheAlgorithmBase algoPred, CacheAlgorithmBase algoObj, Triple t )
	{
		Object subjectKey = subjects.getIndexingField(t); 
		CacheAlgorithmBase subjectCacheEntry = hashMap.get(subjectKey);
		if( hashMap.containsKey(subjectKey) ) { createCacheEntry( subjectCacheEntry ); }
		else { createCacheEntry( algoSub ); hashMap.put(subjectKey, algoSub); }

		Object objectKey = objects.getIndexingField(t);	
		CacheAlgorithmBase objectCacheEntry = hashMap.get(objectKey);
		if( hashMap.containsKey(objectKey) ) { createCacheEntry( objectCacheEntry ); }
		
		algoSub = algoPred = algoObj = null;
		subjectKey = objectKey = null;
		subjectCacheEntry = objectCacheEntry = null;
	}

	/**
	 * Method that actually creates the predicate and object Lucene indexes based on the RANDOM memory 
	 * management algorithm that we use
	 * @param storage - the predicate or object data structure
	 * @param isPred - if we are writing to the predicate Lucene index
	 * @param isObj - if we are writing to the object Lucene index
	 */
	@SuppressWarnings("unchecked")
	private void writePO( PagedNodeToTriplesMapBase storage, boolean isPred, boolean isObj )
	{
		try
		{
			int initialStorageSize = storage.size;
			List l = storage.bunchMap.keyIterator().toList(); Random randomGenerator = new Random();
			while( storage.size > (delayFactor * ( initialStorageSize/100 ) ) )
			{
				int i = randomGenerator.nextInt(l.size()); Object key = l.get(i);
				int index = storage.bunchMap.findPagedSlot(key); if( index > 0 ) continue;
				if( isPred && !isPredFileCreated )
				{
					predF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-pred-" + timestamp); predWriter = new IndexWriter(predF.toString(), new KeywordAnalyzer(), true);
					predWriter.setRAMBufferSizeMB( 0.5 );
					isPredFileCreated = true;
				}
				if( isObj && !isObjFileCreated )
				{
					objF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-obj-" + timestamp);   objWriter = new IndexWriter(objF.toString(), new KeywordAnalyzer(), true);
					objWriter.setRAMBufferSizeMB( 0.5 );
					isObjFileCreated = true;					
				}
				PagedHashedTripleBunch tb = (PagedHashedTripleBunch) storage.bunchMap.get(key);
				String luceneKey = null; ExtendedIterator iter = tb.iterator();
				while( iter.hasNext() )
				{
					Triple t = (Triple)iter.next(); String strTriple = PagedNTripleWriter.writeNTriple(t);
					if( isPred && luceneKey == null ) luceneKey = removePunctuation(t.getPredicate().toString());
					if( isPred ) { luceneWriter.writeTriple(predF, luceneKey, strTriple, predWriter, false, false, false); continue; }
					if( isObj && luceneKey == null ) luceneKey = removePunctuation(t.getObject().toString());
					if( isObj ) luceneWriter.writeTriple(objF, luceneKey, strTriple, objWriter, false, false, false);					
				}
				storage.size = removeTripleBunch( storage, tb, ~index ); l.remove(i);
			}
			if( isPred ) { predWriter.flush(); predWriter.optimize(); } if( isObj ) { objWriter.flush(); objWriter.optimize(); } 
		}
		catch( Exception e ) { e.printStackTrace(); }
	}
	
	/**
	 * Method that creates the subject Lucene index using the degree centrality algorithm
	 * @param hm - the cache size
	 * @param subjects - the subjects in-memory structure
	 */
	@SuppressWarnings("unchecked")
	private void writeS( LinkedHashMap<Object, CacheAlgorithmBase> hm, PagedNodeToTriplesMapBase subjects )
	{
		try
		{
			int initialStorageSize = subjects.size;
			CacheBase.size = hm.size(); List<CacheAlgorithmBase> tempList = new ArrayList<CacheAlgorithmBase>(); 
			tempList.addAll(hm.values()); Collections.sort(tempList); Iterator<CacheAlgorithmBase> valIter = tempList.iterator();
			while( valIter.hasNext() && subjects.size > (delayFactor * ( initialStorageSize/100 ) ) )
			{
				CacheAlgorithmBase value = valIter.next(); Object key = getKeyFromValue(hm, value);
				int index = subjects.bunchMap.findPagedSlot(key);
				if( index > 0 ) { hm.remove(key); continue; }
				if( !isSubFileCreated )
				{
					subF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-sub-" + timestamp);   subWriter = new IndexWriter(subF.toString(), new KeywordAnalyzer(), true);
					subWriter.setRAMBufferSizeMB( 0.5 );
					isSubFileCreated = true;
					subReader = IndexReader.open(subF.toString()); subSearcher = new IndexSearcher(subReader);
				}
				PagedHashedTripleBunch tb = (PagedHashedTripleBunch) subjects.bunchMap.get(key);
				String subStr = "", luceneKey = null; ExtendedIterator iter = tb.iterator();
				while( iter.hasNext() )
				{
					Triple t = (Triple)iter.next(); String strTriple = PagedNTripleWriter.writeNTriple(t);
					if( luceneKey == null ) luceneKey = removePunctuation(t.getSubject().toString());
					subStr += strTriple; 
				}
				luceneWriter.updateIndex(subF, subReader, subSearcher, luceneKey, subStr, subWriter, true, false, false); 
				subjects.size = removeTripleBunch( subjects, tb, ~index ); valIter.remove(); hm.remove(key);
			}			
			subWriter.flush(); subWriter.optimize();
			if( isSubFileCreated && !subReader.isCurrent() ) { subReader.close(); subReader = IndexReader.open(subF.toString()); subSearcher = new IndexSearcher(subReader); }
		}
		catch( Exception e ){ e.printStackTrace(); }
	}

	/**
	 * Method that actually creates the indexes and writes triples to the lucene indexes based on the memory 
	 * management algorithm that we use
	 * @param subjects - the subjects data structure
	 * @param predicates - the predicates data structure
	 * @param objects - the objects data structure
	 */
	public void writeToDisk( PagedNodeToTriplesMapBase subjects, PagedNodeToTriplesMapBase predicates, PagedNodeToTriplesMapBase objects )
	{
		writePO( predicates, true, false );
		if( writeObject ) writePO( objects, false, true );
		if( writeSubject ) writeS( hashMap, subjects );
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