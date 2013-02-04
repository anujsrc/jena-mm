package edu.utdallas.paged.mem.util.iterator.test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.test.GraphTestBase;
import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskBase;
import edu.utdallas.paged.mem.disk.PagedGraphTripleStoreDiskGeneric;
import edu.utdallas.paged.mem.util.PagedNTripleWriter;
import edu.utdallas.paged.mem.util.iterator.PagedWrappedIterator;

/**
 * A JUnit test class for the iterator used in the extended model
 * @author vaibhav
 */
public class PagedWrappedIteratorTest extends GraphTestBase 
{
	/** The subject, predicate and object Lucene index writers **/
	private IndexWriter subWriter = null, predWriter = null, objWriter = null;
	
	/** A sample subject, predicate and object Node **/
	private Node subject, predicate, object;
	
	/** A triple written to the Lucene index **/
	private Triple t;
	
	/** The location of the Lucene indices on disk **/
	private	File subF, predF, objF = null;
	
	/** A triple in N-Triple format **/
	private String nTriple = null;
	
	/** 
	 * Constructor
	 * @param name - the name for this test
	 */
	public PagedWrappedIteratorTest(String name)
    { super(name); }
	
	/**
	 * Method to test the iterator when no Lucene indices are created
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public void testPagedWrappedWithNoFile() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		Triple tm = Triple.ANY;
		PagedGraphTripleStoreDiskBase pe = new PagedGraphTripleStoreDiskGeneric(tm, subF, predF, objF);
		PagedWrappedIterator pgi = new PagedWrappedIterator(Arrays.asList( new String [] {"bill", "and", "ben"} ).iterator(), pe, false);
		assertTrue( "wrapper has at least one element", pgi.hasNext() );
        assertEquals( "", "bill", pgi.next().toString() );
        assertTrue( "wrapper has at least two elements", pgi.hasNext() );
        assertEquals( "", "and", pgi.next().toString() );
        assertTrue( "wrapper has at least three elements", pgi.hasNext() );
        assertEquals( "", "ben", pgi.next().toString() );
        assertFalse( "wrapper is still not empty", pgi.hasNext() );
	}
	
	/**
	 * Method to test the iterator when Lucene indices are created
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	public void testPagedWrappedWithFile() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		createFile();
		
		Triple tm = Triple.ANY;
		PagedGraphTripleStoreDiskBase pe = new PagedGraphTripleStoreDiskGeneric(tm, subF, predF, objF);
		PagedWrappedIterator pgi = new PagedWrappedIterator(Arrays.asList( new String [] {"bill", "and", "ben"} ).iterator(), pe, false);
		
		assertTrue( "wrapper has at least one element", pgi.hasNext() );
        assertEquals( "", "bill", pgi.next().toString() );
        assertTrue( "wrapper has at least two elements", pgi.hasNext() );
        assertEquals( "", "and", pgi.next().toString() );
        assertTrue( "wrapper has at least three elements", pgi.hasNext() );
        assertEquals( "", "ben", pgi.next().toString() );
        assertTrue( "wrapper is still not empty", pgi.hasNext() );		
	}
	
	/**
	 * Method used to create the Lucene indices for testing purposes
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 */
	private void createFile() throws CorruptIndexException, LockObtainFailedException, IOException
	{
		subject = NodeCreateUtils.create(PrefixMapping.Standard, "http://something#JohnSmith");
		predicate = NodeCreateUtils.create(PrefixMapping.Standard, VCARD.NICKNAME.toString());
		object = NodeCreateUtils.create(PrefixMapping.Standard, "Johnny");
		t = Triple.create(subject, predicate, object);
		
		subF = new File(System.getProperty("java.io.tmpdir")+"/test-sub");
		predF = new File(System.getProperty("java.io.tmpdir")+"/test-pred");
		objF = new File(System.getProperty("java.io.tmpdir")+"/test-obj");
		subWriter = new IndexWriter(subF.toString(), new StandardAnalyzer(), true); subWriter.setMergeFactor(1000);
		predWriter = new IndexWriter(this.predF.toString(), new StandardAnalyzer(), true); predWriter.setMergeFactor(1000);
		objWriter = new IndexWriter(this.objF.toString(), new StandardAnalyzer(), true); objWriter.setMergeFactor(1000);

		nTriple = PagedNTripleWriter.writeNTriple(t);
		PagedNTripleWriter luceneWriter = new PagedNTripleWriter();
		luceneWriter.writeTriple(subF, removePunctuation(subject.toString()), nTriple, subWriter, true, false, false);
		luceneWriter.writeTriple(predF, removePunctuation(predicate.toString()), nTriple, predWriter, false, false, false);
		luceneWriter.writeTriple(objF, removePunctuation(object.toString()), nTriple, objWriter, false, false, false);
		
		subWriter.close(); predWriter.close(); objWriter.close();
	}
	
	/**
	 * 
	 * @param input - the input string whose punctuations we want to remove
	 * @return
	 */
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