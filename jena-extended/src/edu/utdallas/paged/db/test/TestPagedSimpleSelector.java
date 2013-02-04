package edu.utdallas.paged.db.test;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import edu.utdallas.paged.db.PagedModelRDB;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestPagedSimpleSelector extends TestCase
{
	public TestPagedSimpleSelector( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestPagedSimpleSelector.class ); }   

	Model model = null;    
	IDBConnection conn = null;

	protected void setUp() throws java.lang.Exception {

		conn = TestPagedConnection.makeAndCleanTestConnection();
		model = PagedModelRDB.createModel(conn, "MySQL"); 

		model.createResource()
		.addProperty(RDF.type, RDFS.Resource)
		.addProperty(RDFS.label, "foo")
		.addProperty(RDF.value, "123");
		model.createResource()
		.addProperty(RDF.type, RDFS.Resource)
		.addProperty(RDFS.label, "bar")
		.addProperty(RDF.value, "123");

	}

	protected void tearDown() throws java.lang.Exception {
		model.close();
		model = null;
		conn.cleanDB();
		conn.close();   	
	}

	public void testAll() {
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null));
		int i =0;
		while (iter.hasNext()) {
			i++;
			iter.next();
		}
		assertEquals(6, i);
	}

	public void testFindProperty() {
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, RDFS.label, (RDFNode) null));
		int i =0;
		while (iter.hasNext()) {
			i++;
			Statement stmt = iter.nextStatement();
			assertEquals(RDFS.label, stmt.getPredicate());
		}
		assertEquals(2, i);
	}

	public void testFindObject() {
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, RDFS.Resource));
		int i =0;
		while (iter.hasNext()) {
			i++;
			Statement stmt = iter.nextStatement();
			assertEquals(RDFS.Resource, stmt.getObject());
		}
		assertEquals(2, i);
	}

	public void testFindSubject() {
		StmtIterator iter = model.listStatements( new SimpleSelector(null, null, RDFS.Resource));
		assertTrue( iter.hasNext() );
		Resource subject = iter.nextStatement().getSubject();
		iter.close();
		iter = model.listStatements( new SimpleSelector(subject, null, (RDFNode) null));
		int i =0;
		while (iter.hasNext()) {
			i++;
			Statement stmt = iter.nextStatement();
			assertEquals(subject, stmt.getSubject());
		}
		assertEquals(3, i);
	}

	public void testFindPropertyAndObject() {
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, RDF.value, 123));
		int i =0;
		while (iter.hasNext()) {
			i++;
			Statement stmt = iter.nextStatement();
			assertEquals(RDF.value, stmt.getPredicate());
			assertEquals(123, stmt.getInt()); 
		}
		assertEquals(2, i);
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