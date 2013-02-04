package edu.utdallas.paged.assembler.test;

import java.util.*;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.test.AssemblerTestBase;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.*;

import edu.utdallas.paged.assembler.PagedAssembler;
import edu.utdallas.paged.assembler.PagedAssemblerBase;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.db.RDBConstants;

/**
 * @see com.hp.hpl.jena.assembler.test.AssemblerTestBase
 * @author vaibhav
 */
public class TestPagedRDBAssemblerContents extends AssemblerTestBase
{
	/**
	 * Constructor
	 * @param name - the test name
	 */
	public TestPagedRDBAssemblerContents( String name )
	{ super( name ); }

	/** The database url **/
	private static final String url = RDBConstants.M_DB_URL;

	/** The database user **/
	private static final String user = RDBConstants.M_DB_USER;
	
	/** The database password **/
	private static final String password = RDBConstants.M_DB_PASSWD;
	
	/** The database type **/
	private static final String type = RDBConstants.M_DB;
	
	/** The database driver **/
	private static final String driver = RDBConstants.M_DBDRIVER_CLASS; 

	/** Check if the driver exists **/
	static
	{
		try { Class.forName( driver ); } catch (Exception e) { throw new RuntimeException( e ); }
	}

	/**
	 * Method that tests if an empty model can be assembled
	 */
	public void testCreatesEmptyModel()
	{
		ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
		Resource root = resourceInModel( "db rdf:type ja:PagedRDBModel; db ja:connection C; db ja:modelName 'CreatesEmptyModel'" );
		PagedAssembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
		Model assembled = (Model) PagedAssembler.pagedRDBAssembler.open(ca, root, Mode.ANY);
		assertIsoModels( modelWithStatements( "" ), assembled );
		assembled.close();
	}

	/**
	 * Method that tests if a model with statements can be assembled
	 */
	public void testCreatesInitialisedModel()
	{
		ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
		Resource root = resourceInModel( "db rdf:type ja:PagedRDBModel; db ja:connection C; db ja:modelName 'CreatesInitialisedModel'; db ja:quotedContent X; X rdf:type T" );
		PagedAssembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
		Model assembled = (Model) PagedAssembler.pagedRDBAssembler.open( ca, root, Mode.ANY );
		assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
		assembled.close();
	}

	/**
	 * Method that tests if a model that exists with statements can be assembled
	 */
	public void testOpensAndInitialisesModel()
	{
		ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
		Resource root = resourceInModel( "db rdf:type ja:PagedRDBModel; db ja:connection C; db ja:modelName 'OpensAndInitialisesModel'; db ja:quotedContent X; X rdf:type T" );
		PagedAssembler ca = (PagedAssembler) new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
		Model assembled = (Model) PagedAssembler.pagedRDBAssembler.open( ca, root, Mode.ANY );
		assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
		assembled.close();
	}

	/**
	 * Method that tests if a model can be created and initialized with statements can be assembled
	 */
	public void testCreatesAndInitialisesModel()
	{
		ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
		ensureAbsent( cd, "CreatesAndInitialisesModel" );
		Resource root = resourceInModel( "db rdf:type ja:PagedRDBModel; db ja:connection C; db ja:modelName 'CreatesAndInitialisesModel'; db ja:initialContent Q; Q ja:quotedContent X; X rdf:type T" );
		PagedAssembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
		Model assembled = (Model) PagedAssembler.pagedRDBAssembler.open( ca, root, Mode.ANY );
		assertIsoModels( modelWithStatements( "X rdf:type T" ), assembled );
		assembled.close();
	}

	/**
	 * Method that tests if a model can be opened without initialization by an assembler
	 */
	public void testOpensAndDoesNotInitialiseModel()
	{
		ConnectionDescription cd = new ConnectionDescription(  "db", url, user, password, type );
		ensurePresent( cd, "OpensAndDoesNotInitialiseModel" );
		Resource root = resourceInModel( "db rdf:type ja:PagedRDBModel; db ja:connection C; db ja:modelName 'OpensAndDoesNotInitialiseModel'; db ja:initialContent Q; Q ja:quotedContent X; X rdf:type T" );
		PagedAssembler ca = new AssistantAssembler( Assembler.content ).with( resource( "C" ), cd );
		Model assembled = (Model) PagedAssembler.pagedRDBAssembler.open( ca, root, Mode.ANY );
		assertIsoModels( modelWithStatements( "" ), assembled );
		assembled.close();
	}

	/**
	 * Method that ensures that the given model exists in the database
	 * @param cd - the connection description for the database
	 * @param modelName - the model name
	 */
	private void ensurePresent( ConnectionDescription cd, String modelName )
	{
		IDBConnection ic = cd.getConnection();
		if (!ic.containsModel( modelName )) PagedModelRDB.createModel( ic, modelName ).close();
	}

	/**
	 * Method that ensures that the given model does not exist in the database
	 * @param cd - the connection description for the database
	 * @param modelName - the model name
	 */
	private void ensureAbsent( ConnectionDescription cd, String modelName )
	{
		IDBConnection ic = cd.getConnection();
		if (ic.containsModel( modelName )) PagedModelRDB.open( ic, modelName ).remove();
	}

	/**
	 * A private assembler used for testing
	 * @author vaibhav
	 */
	static class AssistantAssembler extends PagedAssemblerBase
	{
		/** an assembler object **/
		protected final Assembler general;
		
		/** a map that holds a resource and its corresponding connection description to the database **/
		protected final Map<Resource, Object> map = new HashMap<Resource, Object>();

		/**
		 * Constructor
		 * @param assembler - the input assembler
		 */
		public AssistantAssembler( Assembler assembler )
		{ this.general = assembler; }

		/**
		 * Method that returns an object of this class
		 * @param name - the resource name
		 * @param value - the connection description as an object
		 * @return an object of this class
		 */
		public AssistantAssembler with( Resource name, Object value )
		{
			map.put( name, value );
			return this;
		}

		@Override
		public Object open( PagedAssembler a, Resource root, Mode mode )
		{
			Object fromMap = map.get( root );
			return fromMap == null ? general.open( a, root, mode ) : fromMap;
		}

		@Override
		public Object open(Assembler a, Resource root, Mode mode) 
		{
			Object fromMap = map.get( root );
			return fromMap == null ? general.open( a, root, mode ) : fromMap;
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