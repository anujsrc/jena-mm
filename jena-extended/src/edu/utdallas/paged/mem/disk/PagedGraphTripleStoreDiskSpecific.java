package edu.utdallas.paged.mem.disk;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;

import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.util.PagedNTripleReader;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;

/**
 * A specific implementation of Lucene search when we want to look for specific triple patterns
 * within specific indexes
 * @author vaibhav
 */
public class PagedGraphTripleStoreDiskSpecific extends PagedGraphTripleStoreDiskBase
{
	/**
	 * Constructor
	 * @param tm - the triple pattern we are looking for
	 * @param pagedGraphTripleStoreBase - the triple store base for this model
	 * @param isSubSearch - are we looking for subjects ??
	 * @param isPredSearch - are we looking for predicates ??
	 * @param isObjSearch - are we looking for objects ??
	 */
	public PagedGraphTripleStoreDiskSpecific( TripleMatch tm, PagedGraphTripleStoreBase pagedGraphTripleStoreBase, boolean isSubSearch, boolean isPredSearch, boolean isObjSearch )
	{ 
		super( tm, pagedGraphTripleStoreBase );
		this.subjectFile = (File)p.cache.getFiles()[0]; this.predicateFile =  (File)p.cache.getFiles()[1]; this.objectFile =  (File)p.cache.getFiles()[2];
		this.sm = this.tripleMatch.asTriple().getSubject(); this.pm = this.tripleMatch.asTriple().getPredicate(); this.om = this.tripleMatch.asTriple().getObject();
		this.isSubjectSearch = isSubSearch; this.isPredicateSearch = isPredSearch; this.isObjectSearch = isObjSearch;
	}

	/**
	 * @see edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase#find()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator find() 
	{
		if(subjectFile == null && predicateFile == null && objectFile == null) return null;
		Iterator resIter = null;
		if( sm.isConcrete() && subjectFile != null && ( pm.matches(Node.ANY) || om.matches(Node.ANY) ) ) 
		{ resIter = read( removePunctuation(sm.toString()), subjectFile.toString() ); if( resIter.hasNext() )  readLine = true; }
		else
			if( om.isConcrete() && objectFile != null && ( sm.matches(Node.ANY) || pm.matches(Node.ANY) ) ) 
			{ resIter = read( removePunctuation( om.toString() ), objectFile.toString() ); if( resIter.hasNext() )  readLine = true; }
			else
				if( pm.isConcrete() && predicateFile != null && ( sm.matches(Node.ANY) || om.matches(Node.ANY) ) ) 
				{ resIter = read( removePunctuation(  pm.toString() ), predicateFile.toString() ); if( resIter.hasNext() )  readLine = true; }
				else if( subjectFile != null && sm.matches(Node.ANY) && pm.matches(Node.ANY) && om.matches(Node.ANY) )
				{
					if( lineIndex == 10 &&  increment < incrementThreshold) {  lineIndex = 1;  increment++; } 
					String searchString = "id:" +  lineIndex;
					for(int m = 0; m <=  increment; m++) searchString += "?";
					lineIndex += 1;
					String fileName = null;
					if( PagedNTripleWriter.sid > 10 ) fileName = subjectFile.toString();
					else if( PagedNTripleWriter.pid > 10 ) fileName = predicateFile.toString();
					else fileName = objectFile.toString();
					resIter = read( searchString, fileName );
					if( !resIter.hasNext() ) readLine = true; 
				}
		return resIter;		
	}

	/**
	 * @see edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase#read(String, String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator read(String searchString, String fileString) 
	{
		if( readLine ) return null;
		Iterator iter = null;
		try 
		{ 
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			IndexReader reader = Singleton.getInstance(fileString);
			Searcher searcher = Singleton.getInstance(reader);
			Analyzer analyzer = new KeywordAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer);
			String tripleStr = ""; ArrayList<Node> trArr = new ArrayList<Node>();
			Query query = parser.parse(searchString);
			Hits hits = searcher.search(query);
			for(int i=0; i<hits.length(); i++)
			{
				Document doc = hits.doc(i);
				tripleStr = doc.get("triples");
				if(tripleStr != null)
				{
					String[] splitTriples = tripleStr.split("\n");
					for (int j=0; j<splitTriples.length; j++)
					{				
						Triple t = PagedNTripleReader.readNTriple(splitTriples[j]);
						Node sub = t.getSubject(), pred = t.getPredicate(), obj = t.getObject();
						String objString = t.getObject().toString(), nodeObjString = null;
						if( !om.matches(Node.ANY) ) nodeObjString =  om.getLiteral().toString();
						Object subjectKey = p.subjects.getIndexingField(t), predicateKey = p.predicates.getIndexingField(t), objectKey = p.objects.getIndexingField(t); 
						if( p.cache.isFileCreated && p.subjects.bunchMap.get(subjectKey) == null && isSubjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(sub);
						else if( p.cache.isSubFileCreated && fileString.contains("sub") && p.subjects.bunchMap.get(subjectKey) == null && isSubjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(sub);
						else if( p.cache.isPredFileCreated && fileString.contains("pred") && p.subjects.bunchMap.get(predicateKey) == null && isSubjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(sub);
						else if( p.cache.isObjFileCreated && fileString.contains("obj") && p.objects.bunchMap.get(objectKey) == null && isSubjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(sub);
						if( isPredicateSearch && ( sm.matches(sub) &&  pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) ) 
							trArr.add(pred);
						if( p.cache.isFileCreated && p.objects.bunchMap.get(objectKey) == null && isObjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(obj);
						else if( p.cache.isSubFileCreated && fileString.contains("sub") && p.subjects.bunchMap.get(subjectKey) == null && isObjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(obj);
						else if( p.cache.isPredFileCreated && fileString.contains("pred") && p.predicates.bunchMap.get(predicateKey) == null && isObjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(obj);
						else if( p.cache.isObjFileCreated && fileString.contains("obj") && p.objects.bunchMap.get(objectKey) == null && isObjectSearch && ( sm.matches(sub) && pm.matches(pred) && om.matches(obj) || nodeObjString != null && nodeObjString.equalsIgnoreCase(objString) ) )
							trArr.add(obj);
						if( sm.matches(Node.ANY) && pm.matches(Node.ANY) &&  om.matches(Node.ANY) && isSubjectSearch )
							break;
					}
				}
			}
	    	iter = trArr.iterator();
		}
		catch(Exception e) { e.printStackTrace(); }
		return iter;
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