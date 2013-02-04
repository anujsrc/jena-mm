package edu.utdallas.paged.rdf.model.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.hp.hpl.jena.graph.test.NodeCreateUtils;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.ReificationStyle;
import com.hp.hpl.jena.util.CollectionFactory;

import edu.utdallas.paged.graph.test.PagedGraphTestBase;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

public class PagedModelTestBase extends PagedGraphTestBase
{
	public PagedModelTestBase(String name)
	{ super(name); }

	protected static Model aModel = extendedModel();

	protected static final Model empty = ExtendedModelFactory.createVirtMemModel();

	protected static Model extendedModel()
	{
		Model result = ExtendedModelFactory.createVirtMemModel();
		result.setNsPrefixes( PrefixMapping.Extended );
		return result;
	}

	protected static String nice( RDFNode n )
	{ return nice( n.asNode() ); }

	/**
      create a Statement in a given Model with (S, P, O) extracted by parsing a string.

      @param m the model the statement is attached to
      @param an "S P O" string. 
      @return m.createStatement(S, P, O)
	 */   
	public static Statement statement( Model m, String fact )
	{
		StringTokenizer st = new StringTokenizer( fact );
		Resource sub = resource( m, st.nextToken() );
		Property pred = property( m, st.nextToken() );
		RDFNode obj = rdfNode( m, st.nextToken() );
		return m.createStatement( sub, pred, obj );    
	}    

	public static Statement statement( String fact )
	{ return statement( aModel, fact ); }

	public static RDFNode rdfNode( Model m, String s )
	{ return m.asRDFNode( NodeCreateUtils.create( m, s ) ); }

	@SuppressWarnings("unchecked")
	public static RDFNode rdfNode( Model m, String s, Class c )
	{ return rdfNode( m, s ).as(  c  );  }

	protected static Resource resource()
	{ return ResourceFactory.createResource(); }

	public static Resource resource( String s )
	{ return resource( aModel, s ); }

	public static Resource resource( Model m, String s )
	{ return (Resource) rdfNode( m, s ); }

	public static Property property( String s )
	{ return property( aModel, s ); }

	public static Property property( Model m, String s )
	{ return (Property) rdfNode( m, s ).as( Property.class ); }

	public static Literal literal( Model m, String s )
	{ return (Literal) rdfNode( m, s ).as( Literal.class ); }

	/**
      Create an array of Statements parsed from a semi-separated string.

      @param m a model to serve as a statement factory
      @param facts a sequence of semicolon-separated "S P O" facts
      @return a Statement[] of the (S P O) statements from the string
	 */
	@SuppressWarnings("unchecked")
	public static Statement [] statements( Model m, String facts )
	{
		ArrayList sl = new ArrayList();
		StringTokenizer st = new StringTokenizer( facts, ";" );
		while (st.hasMoreTokens()) sl.add( statement( m, st.nextToken() ) );  
		return (Statement []) sl.toArray( new Statement[sl.size()] );
	}

	/**
      Create an array of Resources from a whitespace-separated string

      @param m a model to serve as a resource factory
      @param items a whitespace-separated sequence to feed to resource
      @return a RDFNode[] of the parsed resources
	 */
	@SuppressWarnings("unchecked")
	public static Resource [] resources( Model m, String items )
	{
		ArrayList rl = new ArrayList();
		StringTokenizer st = new StringTokenizer( items );
		while (st.hasMoreTokens()) rl.add( resource( m, st.nextToken() ) );  
		return (Resource []) rl.toArray( new Resource[rl.size()] );
	}    

	/**
      Answer the set of resources given by the space-separated 
      <code>items</code> string. Each resource specification is interpreted
      as per <code>resource</code>.
	 */
	@SuppressWarnings("unchecked")
	public static Set resourceSet( String items )
	{
		Set result = new HashSet();
		StringTokenizer st = new StringTokenizer( items );
		while (st.hasMoreTokens()) result.add( resource( st.nextToken() ) );  
		return result;
	}

	/**
      add to a model all the statements expressed by a string.

      @param m the model to be updated
      @param facts a sequence of semicolon-separated "S P O" facts
      @return the updated model
	 */
	public static Model modelAdd( Model m, String facts )
	{
		StringTokenizer semis = new StringTokenizer( facts, ";" );
		while (semis.hasMoreTokens()) m.add( statement( m, semis.nextToken() ) );   
		return m;
	}

	/**
      makes a model initialised with statements parsed from a string.

      @param facts a string in semicolon-separated "S P O" format
      @return a model containing those facts
	 */
	public static Model modelWithStatements( String facts )
	{ return modelWithStatements( ReificationStyle.Standard, facts ); }

	/**
      makes a model with a given reiifcation style, initialised with statements parsed 
      from a string.

      @param style the required reification style
      @param facts a string in semicolon-separated "S P O" format
      @return a model containing those facts
	 */        
	public static Model modelWithStatements( ReificationStyle style, String facts )
	{ return modelAdd( createModel( style ), facts ); }

	/**
      make a model with a given reification style, give it Extended prefixes
	 */
	public static Model createModel( ReificationStyle style )
	{
		Model result = ModelFactory.createDefaultModel( style );
		result.setNsPrefixes( PrefixMapping.Extended );
		return result;
	}

	/**
      Answer a default model; it exists merely to abbreviate the rather long explicit
      invocation.

   	@return a new default [aka memory-based] model
	 */ 
	public static Model createMemModel()
	{ return ModelFactory.createDefaultModel(); }

	/**
      test that two models are isomorphic and fail if they are not.

      @param title a String appearing at the beginning of the failure message
      @param wanted the model value that is expected
      @param got the model value to check
      @exception if the models are not isomorphic
	 */    
	@SuppressWarnings("unchecked")
	public static void assertIsoModels( String title, Model wanted, Model got )
	{
		if (wanted.isIsomorphicWith( got ) == false)
		{
			Map map = CollectionFactory.createHashedMap();
			fail( title + ": expected " + nice( wanted.getGraph(), map ) + "\n but had " + nice( got.getGraph(), map ) );
		}
	}        

	/**
      Fail if the two models are not isomorphic. See assertIsoModels(String,Model,Model).
	 */
	public static  void assertIsoModels( Model wanted, Model got )
	{ assertIsoModels( "models must be isomorphic", wanted, got ); }
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