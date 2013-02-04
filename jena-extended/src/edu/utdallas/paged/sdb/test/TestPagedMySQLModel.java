package edu.utdallas.paged.sdb.test;

import junit.framework.TestSuite;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.Store;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;
import edu.utdallas.paged.sdb.store.PagedStoreCreator;

public class TestPagedMySQLModel 
{
	public static junit.framework.Test suite() 
	{
    	TestSuite ts = new TestSuite();
    	
    	ts.addTestSuite(TestPagedMySQLIndexModel.class);
    	ts.addTestSuite(TestPagedMySQLIndexQuadModel.class);
    	ts.addTestSuite(TestPagedMySQLHashModel.class);
    	ts.addTestSuite(TestPagedMySQLHashQuadModel.class);
    	
    	return ts;
	}
	
	public static class TestPagedMySQLIndexModel extends AbstractTestPagedModelSDB 
	{
		public TestPagedMySQLIndexModel(String name) { super(name);	}
		
		@Override
		public Model getModel() 
		{
			Store store = PagedStoreCreator.getIndexMySQL();
			return ExtendedSDBFactory.connectPagedDefaultModel(store);
		}	
	}
	
	public static class TestPagedMySQLIndexQuadModel extends AbstractTestPagedModelSDB 
	{
		public TestPagedMySQLIndexQuadModel(String name) { super(name);	}
		
		@Override
		public Model getModel() 
		{
			Store store = PagedStoreCreator.getIndexMySQL();	
			return ExtendedSDBFactory.connectPagedNamedModel(store, "http://example.com/graph");
		}
	}
	
	public static class TestPagedMySQLHashModel extends AbstractTestPagedModelSDB 
	{
		public TestPagedMySQLHashModel(String name) { super(name); }
		
		@Override
		public Model getModel() 
		{
			Store store = PagedStoreCreator.getHashMySQL();
			return ExtendedSDBFactory.connectPagedDefaultModel(store);
		}
	}
	
	public static class TestPagedMySQLHashQuadModel extends AbstractTestPagedModelSDB 
	{
		public TestPagedMySQLHashQuadModel(String name) { super(name); }
		
		@Override
		public Model getModel() 
		{
			Store store = PagedStoreCreator.getHashMySQL();
			return ExtendedSDBFactory.connectPagedNamedModel(store, "http://example.com/graph");
		}
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