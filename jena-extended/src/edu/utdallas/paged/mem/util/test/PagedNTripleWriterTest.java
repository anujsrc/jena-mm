package edu.utdallas.paged.mem.util.test;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;

import junit.framework.*;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.utdallas.paged.mem.util.PagedNTripleWriter;

public class PagedNTripleWriterTest extends TestCase
{
	private IndexWriter subWriter = null;
	private Triple t;
	private Node subject, predicate, object;
	private	File f = null;
	private String nTriple = null;
	private PagedNTripleWriter luceneWriter = null;
	
	public void setUp() throws Exception 
	{
		f = new File(System.getProperty("java.io.tmpdir")+"/test");
		subWriter = new IndexWriter(f.toString(), new StandardAnalyzer(), true); subWriter.setMergeFactor(1000);
		luceneWriter = new PagedNTripleWriter();
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

	public void testWriteTripleAllResourceNodes() throws CorruptIndexException, IOException 
	{
		subject = NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith");
		predicate = NodeCreateUtils.create(PrefixMapping.Standard, VCARD.Orgunit.toString());
		object = NodeCreateUtils.create(PrefixMapping.Standard, "xsd:integer^^15");
		t = Triple.create(subject, predicate, object);		
		nTriple = PagedNTripleWriter.writeNTriple(t);
		assertEquals(true,luceneWriter.writeTriple(f, removePunctuation(subject.toString()), nTriple, subWriter, true, false, false));
		subWriter.close();
	}

	public void testWriteTripleWithAnonNode() throws CorruptIndexException, IOException 
	{
		subject = NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith");
		predicate = NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NOTE.toString());
		object = NodeCreateUtils.create(PrefixMapping.Standard, "_XXX");
		t = Triple.create(subject, predicate, object);
		nTriple = PagedNTripleWriter.writeNTriple(t);
		assertEquals(true,luceneWriter.writeTriple(f, removePunctuation(subject.toString()), nTriple, subWriter, true, false, false));
		subWriter.close();
	}

	public void testWriteTripleWithLiteralNode() throws CorruptIndexException, IOException 
	{
		subject = NodeCreateUtils.create(PrefixMapping.Standard, "http://something#Score");
		predicate = NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NICKNAME.toString());
		object = Node.createLiteral("Johnny");
		t = Triple.create(subject, predicate, object);
		nTriple = PagedNTripleWriter.writeNTriple(t);
		assertEquals(true,luceneWriter.writeTriple(f, removePunctuation(subject.toString()), nTriple, subWriter, true, false, false));
		subWriter.close();
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