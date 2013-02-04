package edu.utdallas.paged.mem.util.test;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;

import junit.framework.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.utdallas.paged.mem.util.PagedNTripleReader;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;

public class PagedNTripleReaderTest extends TestCase
{
	private IndexWriter subWriter = null;
	private	File f = null;
	private Triple t;
	private String testString, nTriple = null;
	private Node subject, predicate, object;
	private PagedNTripleWriter luceneWriter = null;
	
	public void setUp() throws Exception 
	{
		subject = NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith");
		predicate = NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NICKNAME.toString());
		object = NodeCreateUtils.create(PrefixMapping.Standard, "Johnny");
		t = Triple.create(subject, predicate, object);
		
		f = new File(System.getProperty("java.io.tmpdir")+"/test");
		subWriter = new IndexWriter(f.toString(), new StandardAnalyzer(), true); subWriter.setMergeFactor(1000);
		nTriple = PagedNTripleWriter.writeNTriple(t);
		luceneWriter = new PagedNTripleWriter();
		luceneWriter.writeTriple(f, removePunctuation(subject.toString()), nTriple, subWriter, true, false, false);
		subWriter.close();
		try
		{
			BooleanQuery.setMaxClauseCount(Integer.MAX_VALUE);
			IndexReader reader = null;
			reader = IndexReader.open(f); 
			Searcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer();
			QueryParser parser = new QueryParser("uri", analyzer);
			Query query = parser.parse("httpsomethingJohnSmith");
			Hits hits = searcher.search(query);
			String tripleStr = "";
			for(int i=0; i<hits.length(); i++)
			{
				Document doc = hits.doc(i);
				tripleStr = doc.get("triples");
				if(tripleStr != null)
				{
					String[] splitTriples = tripleStr.split("\n");
					for (int j=0; j<splitTriples.length; j++)
					{
						t = PagedNTripleReader.readNTriple(splitTriples[j]);
					}
				}
			}
			searcher.close(); reader.close();
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void tearDown() throws Exception 
	{
		f.deleteOnExit();
	}
	
	private String removePunctuation(String input)
	{
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length(); i++) 
		{
			if (input.charAt(i) == 42 || (input.charAt(i) >= 65 && input.charAt(i) <= 90) || (input.charAt(i) >= 97 && input.charAt(i) <= 122) || (input.charAt(i) >= 48 && input.charAt(i) <= 57)) 
				sb = sb.append(input.charAt(i));
		}
		return sb.toString();
	}

	public void testReadReader() 
	{
		assertEquals("both triples are same", t.toString(), Triple.create(NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith"), 
				NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NICKNAME.toString()), 
				NodeCreateUtils.create(PrefixMapping.Standard, "Johnny")).toString());
	}

	public void testReadReaderString() 
	{
		assertEquals("both triples are same", t.toString(), Triple.create(NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith"), 
				NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NICKNAME.toString()), 
				NodeCreateUtils.create(PrefixMapping.Standard, "Johnny")).toString());
	}

	public void testGetURI() 
	{
		testString = PagedNTripleReader.getURI("test");
		assertTrue("test string is eh:/test",testString.equalsIgnoreCase("eh:/test"));
	}

	public void testReadResourceForTriple() 
	{
		String[] test = nTriple.split(" ");
		testString = PagedNTripleReader.readResourceForTriple(test[0]);
		assertTrue("resource read is john smith",testString == "http://something#JohnSmith");
	}

	public void testReadNodeForTriple() 
	{
		String[] test = nTriple.split(" ");
		testString = PagedNTripleReader.readNodeForTriple(test[2]);
		assertTrue("node read is johnny",testString.equalsIgnoreCase("eh:/Johnny"));
	}

	public void testCreateTypedLiteral() {
		testString = PagedNTripleReader.createTypedLiteral("en", "http://test");
		assertTrue("created typed literal",testString.equalsIgnoreCase("\"en\"^^http://test"));
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