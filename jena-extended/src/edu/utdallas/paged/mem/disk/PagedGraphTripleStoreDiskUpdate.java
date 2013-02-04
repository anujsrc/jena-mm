package edu.utdallas.paged.mem.disk;

import java.io.File;
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
import org.apache.lucene.store.FSDirectory;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;

import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.mem.util.PagedNTripleReader;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;

/**
 * A specific implementation of Lucene search when we want to update the values of resources
 * @author vaibhav
 */
public class PagedGraphTripleStoreDiskUpdate extends PagedGraphTripleStoreDiskBase
{
	/**
	 * Constructor
	 * @param tm - the triple pattern we are looking for
	 * @param p - the triple store base of this model
	 */
	public PagedGraphTripleStoreDiskUpdate( TripleMatch tm, PagedGraphTripleStoreBase p )
	{ 
		super( tm, p ); 
		this.subjectFile = (File)p.cache.getFiles()[0]; this.predicateFile = (File)p.cache.getFiles()[1]; this.objectFile = (File)p.cache.getFiles()[2];
		this.sm = tripleMatch.asTriple().getSubject(); this.pm = tripleMatch.asTriple().getPredicate(); this.om = tripleMatch.asTriple().getObject();
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
		isSubjectSearch = true; 
		if( subjectFile != null ) resIter = read( removePunctuation( sm.toString() ), subjectFile.toString() );
		isSubjectSearch = false; 
		if( predicateFile != null ) resIter = read( removePunctuation( pm.toString() ), predicateFile.toString() );
		if( objectFile != null ) resIter = read( removePunctuation( om.toString() ), objectFile.toString() );
		return resIter;
	}

	/**
	 * @see edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase#read(String, String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Iterator read(String searchString, String fileString) 
	{
		try 
		{ 
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			IndexReader reader = Singleton.getInstance(fileString);
			Searcher searcher = Singleton.getInstance(reader);
			Analyzer analyzer = new KeywordAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer);
			String tripleStr = "";
			Query query = parser.parse(searchString);
			Hits hits = searcher.search(query);
	loop:	for(int i=0; i<hits.length(); i++)
			{
				Document doc = hits.doc(i);
				tripleStr = doc.get("triples");
				if(tripleStr != null)
				{
					String[] splitTriples = tripleStr.split("\n");
					for (int j=0; j<splitTriples.length; j++)
					{				
						Triple t = PagedNTripleReader.readNTriple(splitTriples[j]);
						if( isSubjectSearch ) add(t);
						if( !isSubjectSearch && p.cache.isFileCreated && PagedNTripleWriter.writeNTriple( tripleMatch.asTriple()).equalsIgnoreCase(tripleStr) ) add(t);
						if( !isSubjectSearch && ( p.cache.isSubFileCreated || p.cache.isPredFileCreated || p.cache.isObjFileCreated )&& PagedNTripleWriter.writeNTriple( tripleMatch.asTriple()).equalsIgnoreCase(t.toString()) ) 
						{
							add(t);
							IndexReader.unlock(FSDirectory.getDirectory(fileString));
							reader.deleteDocument(hits.id(i));
							reader.flush();
						}
					}
					if( PagedNTripleWriter.writeNTriple( tripleMatch.asTriple()).equalsIgnoreCase(tripleStr) || isSubjectSearch ) 
					{
						IndexReader.unlock(FSDirectory.getDirectory(fileString));
						reader.deleteDocument(hits.id(i));
						reader.flush();
						break loop;
					}
				}
			}
		}
		catch(Exception e) { e.printStackTrace(); }
		return null;
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