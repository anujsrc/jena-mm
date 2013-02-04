package edu.utdallas.paged.db.test;

import java.util.*;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.shared.*;
import edu.utdallas.paged.db.PagedModelRDB;
import junit.framework.*;

public class TestPagedPrefixMapping extends AbstractTestPrefixMapping 
{
	private List<Model> models = null;
	private IDBConnection theConnection = null;
	private static int count = 0;

	public TestPagedPrefixMapping(String name) { super(name); }

	public static TestSuite suite() {
		return new TestSuite(TestPagedPrefixMapping.class);
	}

	public void setUp() 
	{
		theConnection = TestPagedConnection.makeAndCleanTestConnection();
		models = new ArrayList<Model>();
	}

	@SuppressWarnings("unchecked")
	public void tearDown() 
	{
		// close all the models we opened
		Iterator it = models.iterator();
		while(it.hasNext()) {
			Model m = (Model)it.next();
			m.close();
		}

		try {
			theConnection.close();
		} catch (Exception e) {
			throw new JenaException(e);
		}
	}

	private String getModelName()
	{ return "test" + count++; }

	private Model getModel()
	{
		Model model = PagedModelRDB.createModel( theConnection, getModelName() );
		models.add( model );
		return model;
	}

	public PrefixMapping getMapping() {
		Model model = getModel();
		return model.getGraph().getPrefixMapping();
	}

	public void testPrefixesPersist()
	{
		String name = "prefix-testing-model-persist"; 
		Model m = PagedModelRDB.createModel( theConnection, name );
		m.setNsPrefix( "hello", "eh:/someURI#" );
		m.setNsPrefix( "bingo", "eh:/otherURI#" );
		m.setNsPrefix( "yendi", "eh:/otherURI#" );
		m.close();
		Model m1 = PagedModelRDB.open( theConnection, name );
		assertEquals( "eh:/someURI#", m1.getNsPrefixURI( "hello" ) );
		assertEquals( "eh:/otherURI#", m1.getNsPrefixURI( "yendi" ) );
		assertEquals( "eh:/otherURI#", m1.getNsPrefixURI( "bingo" ) );
		m1.close();
	}

	public void testPrefixesRemoved()
	{
		String name = "prefix-testing-model-remove"; 
		Model m = PagedModelRDB.createModel( theConnection, name );
		m.setNsPrefix( "hello", "eh:/someURI#" );
		m.setNsPrefix( "there", "eg:/otherURI#" );
		m.removeNsPrefix( "hello" );
		assertEquals( null, m.getNsPrefixURI( "hello" ) );
		m.close();
		Model m1 = PagedModelRDB.open( theConnection, name );
		assertEquals( null, m1.getNsPrefixURI( "hello" ) );
		assertEquals( "eg:/otherURI#", m1.getNsPrefixURI( "there" ) );
		m1.close();
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