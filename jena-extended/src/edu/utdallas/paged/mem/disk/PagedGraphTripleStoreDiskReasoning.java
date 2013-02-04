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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;

import edu.utdallas.paged.mem.util.PagedNTripleReader;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;

/**
 * An implementation of Lucene search when we do reasoning
 * @author vaibhav
 */
public class PagedGraphTripleStoreDiskReasoning extends PagedGraphTripleStoreDiskBase
{
	/** A static index reader for the subject Lucene index */
	public static IndexReader subReader = null;

	/** A static index reader for the predicate Lucene index */
	public static IndexReader predReader = null;
	
	/** A static index reader for the object Lucene index */
	public static IndexReader objReader = null;
	
	/** A static index searcher for the subject Lucene index */
	public static Searcher subSearcher = null;
	
	/** A static index searcher for the predicate Lucene index */
	public static Searcher predSearcher = null;
	
	/** A static index searcher for the object Lucene index */
	public static Searcher objSearcher = null;
	
	/**
	 * Constructor
	 * @param tm - the triple pattern we are looking for
	 * @param subF - the subject lucene index
	 * @param predF - the predicate lucene index
	 * @param objF - the object lucene index
	 */
	public PagedGraphTripleStoreDiskReasoning( TripleMatch tm, File subF, File predF, File objF )
	{ 
		super( tm );
		this.subjectFile = subF; this.predicateFile = predF; this.objectFile = objF;
		try
		{
			if( subF != null && PagedGraphTripleStoreDiskReasoning.subReader == null ) { PagedGraphTripleStoreDiskReasoning.subReader = IndexReader.open(subF); PagedGraphTripleStoreDiskReasoning.subSearcher = new IndexSearcher( subReader ); }
			if( predF != null && PagedGraphTripleStoreDiskReasoning.predReader == null ) { PagedGraphTripleStoreDiskReasoning.predReader = IndexReader.open(predF); PagedGraphTripleStoreDiskReasoning.predSearcher = new IndexSearcher( predReader ); }
			if( objF != null && PagedGraphTripleStoreDiskReasoning.objReader == null ) { PagedGraphTripleStoreDiskReasoning.objReader = IndexReader.open(objF); PagedGraphTripleStoreDiskReasoning.objSearcher = new IndexSearcher( objReader ); }
		}
		catch( Exception e ) { e.printStackTrace(); }
		this.sm = this.tripleMatch.asTriple().getSubject(); this.pm = this.tripleMatch.asTriple().getPredicate(); this.om = this.tripleMatch.asTriple().getObject();
	}

	/**
	 * @see edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase#find()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator find() 
	{
		Iterator resIter = null; String subSearchString = removePunctuation( sm.toString() ), predSearchString = removePunctuation( pm.toString() ), objSearchString = removePunctuation( om.toString() );
		if( sm.isConcrete() && subjectFile != null ) 
		{ 
			resIter = read( subSearchString, subjectFile.toString() ); 
			if( resIter.hasNext() ) readLine = true;
		}
		else
			if( om.isConcrete() && objectFile != null ) 
			{
				resIter = read(  objSearchString, objectFile.toString() ); 
				if( resIter.hasNext() ) readLine = true;
			}
			else
				if( pm.isConcrete() && predicateFile != null ) 
				{
					resIter = read( predSearchString, predicateFile.toString() ); 
					if( resIter.hasNext() ) readLine = true;
				}
				else if( subjectFile != null && sm.matches(Node.ANY) && pm.matches(Node.ANY) && om.matches(Node.ANY) )
				{
					if(lineIndex == 10 && increment < incrementThreshold) { lineIndex = 1; increment++; } 
					String searchString = "id:" + lineIndex;
					for(int m = 0; m <= increment; m++) searchString += "?";
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
		if(readLine) return null;
		Iterator iter = null;
		try 
		{ 
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			Analyzer analyzer = new KeywordAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer);
			String tripleStr = ""; ArrayList<Triple> trArr = new ArrayList<Triple>();
			Query query = parser.parse(searchString);
			Hits hits = null;
			if( fileString.contains("sub") ) 
			{ 
				if( !PagedGraphTripleStoreDiskReasoning.subReader.isCurrent() ) 
				{
					PagedGraphTripleStoreDiskReasoning.subReader =  IndexReader.open(fileString);
					PagedGraphTripleStoreDiskReasoning.subSearcher = new IndexSearcher( PagedGraphTripleStoreDiskReasoning.subReader );
				}
				hits = PagedGraphTripleStoreDiskReasoning.subSearcher.search(query); 
			}
			if( fileString.contains("pred") ) 
			{ 
				if( !PagedGraphTripleStoreDiskReasoning.predReader.isCurrent() ) 
				{
					PagedGraphTripleStoreDiskReasoning.predReader =  IndexReader.open(fileString);
					PagedGraphTripleStoreDiskReasoning.predSearcher = new IndexSearcher( PagedGraphTripleStoreDiskReasoning.predReader );
				}
				hits = PagedGraphTripleStoreDiskReasoning.predSearcher.search(query); 
			}
			if( fileString.contains("obj") ) 
			{ 
				if( !PagedGraphTripleStoreDiskReasoning.objReader.isCurrent() ) 
				{
					PagedGraphTripleStoreDiskReasoning.objReader =  IndexReader.open(fileString);
					PagedGraphTripleStoreDiskReasoning.objSearcher = new IndexSearcher( PagedGraphTripleStoreDiskReasoning.objReader );
				}
				hits = PagedGraphTripleStoreDiskReasoning.objSearcher.search(query); 
			}
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
						if( sm.matches(sub) && pm.matches(pred) && om.matches(obj) )
							trArr.add(t);
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