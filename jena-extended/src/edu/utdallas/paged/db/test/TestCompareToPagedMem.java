package edu.utdallas.paged.db.test;

import java.util.Iterator;

import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.db.test.Data;
import com.hp.hpl.jena.rdf.model.*;

import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

import junit.framework.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCompareToPagedMem extends TestCase
{    
	public TestCompareToPagedMem( String name )
	{ super( name ); }

	public static TestSuite suite()
	{ return new TestSuite( TestCompareToPagedMem.class ); }   

	static Logger logger = LoggerFactory.getLogger( TestCompareToPagedMem.class );

	Model modelrdf = null;    
	Model modelmem = null;

	IDBConnection conn = null;

	protected void setUp() throws java.lang.Exception 
	{
		conn = TestPagedConnection.makeAndCleanTestConnection();
		modelrdf = PagedModelRDB.createModel(conn);
		modelmem = ExtendedModelFactory.createVirtMemModel();
	}

	protected void tearDown() throws java.lang.Exception 
	{
		modelrdf.close();
		modelrdf = null;
		conn.cleanDB();
		conn.close();
		conn = null;
	}

	private void addRemove(Statement stmt) 
	{
		modelrdf.add(stmt);
		modelmem.add(stmt);

		assertTrue( modelmem.size() == 1);
		assertTrue( modelrdf.size() == 1);

		compareModels();

		modelrdf.remove(stmt);
		modelmem.remove(stmt);

		assertTrue( modelmem.size() == 0);
		assertTrue( modelrdf.size() == 0);

		compareModels();		
	}

	@SuppressWarnings("unchecked")
	private void compareModels() 
	{
		Iterator it = modelmem.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelrdf.contains(s)) {
				logger.error("Statment:"+s+" is in mem but not rdf");
				logModel(modelmem, "Mem");
				logModel(modelrdf, "RDF");
			}
			assertTrue( modelrdf.contains(s));
		}
		it = modelrdf.listStatements();
		while( it.hasNext()) {
			Statement s = (Statement)it.next();
			if( ! modelmem.contains(s)) {
				logger.error("Statment:"+s+" is in rdf but not memory");
				logModel(modelmem, "Mem");
				logModel(modelrdf, "RDF");
			}
			assertTrue( modelmem.contains(s));
		}
	}

	@SuppressWarnings("unchecked")
	private void logModel(Model m, String name) 
	{
		logger.debug("Model");
		Iterator it = m.listStatements();
		while( it.hasNext()) { 
			Statement s = (Statement)it.next();
			RDFNode object = s.getObject();
			if( object instanceof Literal )
				logger.debug(name+": "+s.getSubject()+s.getPredicate()+((Literal)object).getValue()+" "+((Literal)object).getDatatype()+" "+((Literal)object).getLanguage());
			else
				logger.debug(name+": "+it.next()); 	
		}
	}

	public void testAddRemoveURI() 
	{
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Resource o = modelrdf.createResource("test#object");

		addRemove( modelrdf.createStatement(s,p,o));    		
	}

	public void testAddRemoveLiteral() 
	{
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l = modelrdf.createLiteral("testLiteral");

		addRemove( modelrdf.createStatement(s,p,l));    	
	} 

	public void testAddRemoveHugeLiteral() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l = modelrdf.createLiteral(buffer.toString());

		addRemove( modelrdf.createStatement(s,p,l));    	
	} 

	public void testAddRemoveDatatype() 
	{
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l = modelrdf.createTypedLiteral("stringType");

		addRemove( modelrdf.createStatement(s,p,l));    	
	} 

	public void testAddRemoveHugeDatatype() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelrdf.createResource("test#subject");
		Property p = modelrdf.createProperty("test#predicate");
		Literal l2 = modelrdf.createTypedLiteral(buffer.toString());

		addRemove( modelrdf.createStatement(s,p,l2));    	
	} 

	public void testAddRemoveHugeLiteral2() 
	{
		String base = Data.strLong ;
		StringBuffer buffer = new StringBuffer(4096);
		while(buffer.length() < 4000 )
			buffer.append(base);
		Resource s = modelmem.createResource("test#subject");
		Property p = modelmem.createProperty("test#predicate");
		Literal l2 = modelmem.createLiteral(buffer.toString());
		Literal l3 = modelmem.createLiteral(buffer.toString()+".");

		Statement st1 = modelmem.createStatement(s,p,l2);
		Statement st2 = modelmem.createStatement(s,p,l3);
		modelrdf.add(st1);
		modelmem.add(st1);

		compareModels();

		modelrdf.add(st2);
		modelmem.add(st2);

		compareModels();

		modelrdf.remove(st2);
		modelmem.remove(st2);

		compareModels();

		modelrdf.remove(st1);
		modelmem.remove(st1);
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