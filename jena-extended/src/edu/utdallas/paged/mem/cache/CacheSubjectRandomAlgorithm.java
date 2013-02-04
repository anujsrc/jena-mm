package edu.utdallas.paged.mem.cache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import com.hp.hpl.jena.graph.Triple;

import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.PagedHashedTripleBunch;
import edu.utdallas.paged.mem.PagedNodeToTriplesMapBase;
import edu.utdallas.paged.mem.algorithm.CacheAlgorithmBase;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * A specific implementation of the buffer based on randomly caching the values of subjects only
 * @author vaibhav
 */
public class CacheSubjectRandomAlgorithm extends CacheBase
{	
	/** Constructor */
	public CacheSubjectRandomAlgorithm()
	{ 
		super();
		this.luceneWriter = new PagedNTripleWriter(); this.id = ++CacheSubjectRandomAlgorithm.graphId; this.timestamp = System.currentTimeMillis();
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
		Object subjectKey = subjects.getIndexingField(t); Object objectKey = objects.getIndexingField(t);
		
		CacheAlgorithmBase subjectCacheEntry = hashMap.get(subjectKey);
		if( hashMap.containsKey(subjectKey) ) { createCacheEntry( subjectCacheEntry ); }
		else { createCacheEntry( algoSub ); hashMap.put(subjectKey, algoSub); }
		
		CacheAlgorithmBase objectCacheEntry = hashMap.get(objectKey);
		if( hashMap.containsKey(objectKey) ) { createCacheEntry( objectCacheEntry ); }
		algoSub = algoPred = algoObj = null;
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
		try
		{
			CacheBase.size = hashMap.size(); List<CacheAlgorithmBase> tempList = new ArrayList<CacheAlgorithmBase>(); tempList.addAll(hashMap.values());
			Random randomGenerator = new Random();
			while( subjects.size > (delayFactor * ( PagedGraphTripleStoreBase.writeThreshold/100 ) ) )
			{
				int randomCacheEntry = randomGenerator.nextInt(CacheBase.size);
				CacheAlgorithmBase value = tempList.get(randomCacheEntry); Object key = getKeyFromValue(hashMap, value);
				if( key == null ) continue;
				int subIndex = subjects.bunchMap.findPagedSlot(key);
				if( subIndex > 0 ) { hashMap.remove(key); continue; }
				if(!isFileCreated)
				{
					subF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-sub-" + timestamp);   subWriter = new IndexWriter(subF.toString(), new KeywordAnalyzer(), true);
					//subWriter.setRAMBufferSizeMB( Runtime.getRuntime().totalMemory() / ( 1024.0*1024.0 ) );
					subWriter.setRAMBufferSizeMB( 0.5 );
					predF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-pred-" + timestamp); predWriter = new IndexWriter(predF.toString(), new KeywordAnalyzer(), true);
					//predWriter.setRAMBufferSizeMB( Runtime.getRuntime().totalMemory() / ( 1024.0*1024.0 ) );
					predWriter.setRAMBufferSizeMB( 0.5 );
					objF = new File(System.getProperty("java.io.tmpdir")+"/ss" + id + "-obj-" + timestamp);   objWriter = new IndexWriter(objF.toString(), new KeywordAnalyzer(), true);
					//objWriter.setRAMBufferSizeMB( Runtime.getRuntime().totalMemory() / ( 1024.0*1024.0 ) );
					objWriter.setRAMBufferSizeMB( 0.5 );
					isFileCreated = true;
					subReader = IndexReader.open(subF.toString()); subSearcher = new IndexSearcher(subReader);
				}
				PagedHashedTripleBunch subTb = (PagedHashedTripleBunch) subjects.bunchMap.get(key);
				String subStr = "", subject = null; ArrayList<Triple> trArr = getTriples( key, subTb, null, null );
				for( int i = 0; i < trArr.size(); i++ )
				{
					Triple t = trArr.get(i); String strTriple = PagedNTripleWriter.writeNTriple(t);
					subStr += strTriple; if( subject == null ) subject = removePunctuation(t.getSubject().toString());
					luceneWriter.writeTriple(predF, removePunctuation(t.getPredicate().toString()), strTriple, predWriter, false, false, false); predicates.remove( t ); 
					luceneWriter.writeTriple(objF, removePunctuation(t.getObject().toString()), strTriple, objWriter, false, false, false); objects.remove( t );
				}
				luceneWriter.updateIndex(subF, subReader, subSearcher, subject, subStr, subWriter, true, false, false); subjects.size = removeTripleBunch( subjects, subTb, ~subIndex );
				hashMap.remove(key);
			}			
			if( isFileCreated ) { subWriter.flush(); subWriter.optimize(); predWriter.flush(); predWriter.optimize(); objWriter.flush(); objWriter.optimize(); }
			if( subReader != null && !subReader.isCurrent() && isFileCreated ) { subReader.close(); subReader = IndexReader.open(subF.toString()); subSearcher = new IndexSearcher(subReader); }
		}
		catch(Exception e) { e.printStackTrace(); }		
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