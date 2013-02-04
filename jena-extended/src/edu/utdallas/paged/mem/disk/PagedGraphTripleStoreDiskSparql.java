package edu.utdallas.paged.mem.disk;

import java.io.File;

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

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.query.Domain;
import com.hp.hpl.jena.graph.query.StageElement;
import com.hp.hpl.jena.mem.MatchOrBind;

import edu.utdallas.paged.mem.util.PagedNTripleReader;

/**
 * A class that allows us to query the Lucene indices with specific subjects or 
 * objects of SPARQL queries
 * @author vaibhav
 */
public class PagedGraphTripleStoreDiskSparql
{
	/** A static index reader for the subject Lucene index */
	public IndexReader subReader = null;

	/** A static index reader for the predicate Lucene index */
	public IndexReader predReader = null;
	
	/** A static index reader for the object Lucene index */
	public IndexReader objReader = null;
	
	/** A static index searcher for the subject Lucene index */
	public Searcher subSearcher = null;
	
	/** A static index searcher for the predicate Lucene index */
	public Searcher predSearcher = null;
	
	/** A static index searcher for the object Lucene index */
	public Searcher objSearcher = null;

	/**
	 * Constructor
	 * @param subF - the subject lucene index
	 * @param predF - the predicate lucene index
	 * @param objF - the object lucene index
	 */
	public PagedGraphTripleStoreDiskSparql( File subF, File predF, File objF )
	{ 
		try
		{
			if( subF != null ) { this.subReader = IndexReader.open(subF); this.subSearcher = new IndexSearcher( subReader ); }
			if( predF != null ) { this.predReader = IndexReader.open(predF); this.predSearcher = new IndexSearcher( predReader ); }
			if( objF != null ) { this.objReader = IndexReader.open(objF); this.objSearcher = new IndexSearcher( objReader ); }
		}
		catch( Exception e ) { e.printStackTrace(); }
	}

	/**
	 * Method that runs the given search string against the Lucene indices to obtain triples
	 * @param searchString - the search string
	 * @param tr - the triple pattern
	 * @param searcher  - the specific index searcher
	 * @param x - the object to compare our result against
	 * @param d - the domain of variables
	 * @param next - the next sub-part of the SPARQL query to execute
	 */
	public void run( String searchString, Triple tr, Searcher searcher, MatchOrBind x, Domain d, StageElement next ) 
	{
		try 
		{ 
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			Analyzer analyzer = new KeywordAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer);
			String tripleStr = null;
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
						if( x == null && tr.matches(t) ) next.run(d);
						else
							if( tr == null && x.matches(t) ) next.run(d); 
					}
				}
			}
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