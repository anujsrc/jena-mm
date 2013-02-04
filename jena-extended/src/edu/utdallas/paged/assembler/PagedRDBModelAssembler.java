package edu.utdallas.paged.assembler;

import com.hp.hpl.jena.assembler.*;
import com.hp.hpl.jena.assembler.assemblers.NamedModelAssembler;
import com.hp.hpl.jena.db.*;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.shared.*;

import edu.utdallas.paged.db.PagedGraphRDB;
import edu.utdallas.paged.db.PagedModelRDB;

/**
 * An assembler for the extended RDB model
 * @author vaibhav
 */
public class PagedRDBModelAssembler extends NamedModelAssembler implements PagedAssembler
{
	@Override
	protected Model openEmptyModel( Assembler a, Resource root, Mode mode )
	{ return openModel( a, root, Content.empty, mode ); }    

	@Override
	public Model openModel( Assembler a, Resource root, Content initial, Mode mode )
	{
		checkType( root, PagedJA.PagedRDBModel );
		String name = getModelName( root );
		ReificationStyle style = getReificationStyle( root );
		ConnectionDescription c = getConnection( a, root );
		Model m = openModel( root, c, name, style, initial, mode );
		return m;
	}

	/**
	 * Method that gets a connection description given an assembler and a resource
	 * @param a - the extended RDB model assembler
	 * @param root - a resource for the extended RDB model assembler
	 * @return a ConnectionDescription
	 */
	protected ConnectionDescription getConnection( Assembler a, Resource root )
	{
		Resource C = getRequiredResource( root, PagedJA.connection );
		return (ConnectionDescription) a.open( C );        
	}

	/**
	 * 
	 * @param root - the resource representing the extended RDB model assembler
	 * @param c - a connection description object
	 * @param name - the name for this RDB model
	 * @param style - the reification style for the RDB model
	 * @param initial - the initial content of the model
	 * @param mode - the mode of this assembler
	 * @return a Model
	 */
	public Model openModel( Resource root, ConnectionDescription c, String name, ReificationStyle style, Content initial, Mode mode )
	{
		IDBConnection ic = c.getConnection();
		return isDefaultName( name )
		? ic.containsDefaultModel() ? PagedModelRDB.open( ic ) : PagedModelRDB.createModel( ic )
				: openByMode( root, initial, name, mode, style, ic );
	}

	/**
	 * 
	 * @param root - the resource representing the extended RDB model assembler
	 * @param initial - the initial content of the model
	 * @param name - the name for this RDB model
	 * @param mode - the mode of this assembler
	 * @param style - the reification style for the RDB model
	 * @param ic - the database connection object
	 * @return
	 */
	private Model openByMode( Resource root, Content initial, String name, Mode mode, ReificationStyle style, IDBConnection ic )
	{
		if (ic.containsModel( name ))
		{
			if (mode.permitUseExisting( root, name )) return consModel( ic, name, style, false );
			throw new AlreadyExistsException( name );
		}
		else
		{
			if (mode.permitCreateNew( root, name )) return initial.fill( consModel( ic, name, style, true ) );
			throw new NotFoundException( name );
		}
	}

	/** The name for the default model **/
	private static final String nameForDefault = "DEFAULT";

	/** 
	 * Method that determines if a model is default or not
	 * @param name - the input name for a model
	 * @return true, if it is a default model, false otherwise
	 */
	private boolean isDefaultName( String name )
	{ return name.equals( nameForDefault ) || name.equals( "" ); }

	/**
	 * Method that constructs or opens a new RDB model
	 * @param c - the database connection
	 * @param name - the model name
	 * @param style - the reification style
	 * @param fresh - true if this a new model, false otherwise
	 * @return a new RDB model
	 */
	protected Model consModel( IDBConnection c, String name, ReificationStyle style, boolean fresh )
	{ return new PagedModelRDB( consGraph( c, name, style, fresh ) ); }

	/**
	 * Method that constructs or opens a new RDB model
	 * @param c - the database connection
	 * @param name - the model name
	 * @param style - the reification style
	 * @param fresh - true if this a new model, false otherwise
	 * @return a new RDB model
	 */
	protected PagedGraphRDB consGraph( IDBConnection c, String name, ReificationStyle style, boolean fresh )
	{        
		Graph p = c.getDefaultModelProperties().getGraph();
		int reificationStyle = PagedGraphRDB.styleRDB( style );
		return new PagedGraphRDB( c, name, (fresh ? p : null), reificationStyle, fresh );
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