package edu.utdallas.paged.assembler.test;

import com.hp.hpl.jena.assembler.test.ModelAssemblerTestBase;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.ReificationStyle;

import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

/**
 * @see com.hp.hpl.jena.assembler.test.ModelAssemblerTestBase
 * @author vaibhav
 */
public class PagedMemoryModelAssemblerTestBase extends ModelAssemblerTestBase
{
	/**
	 * Constructor
	 * @param name - the test name
	 */
	public PagedMemoryModelAssemblerTestBase(String name) 
	{ super(name); }

	@Override
	protected Resource resourceInModel( String string )
	{
		Model m = model( string );
		Resource r = resource( string.substring( 0, string.indexOf( ' ' ) ) );
		return (Resource) r.inModel( m );        
	}

	@Override
	protected Model model( String string )
	{ 
		Model result = createModel( ReificationStyle.Standard );
		setRequiredPrefixes( result );
		return modelAddFacts( result, string );
	}

	@Override
	protected Model model()
	{ return model( "" ); }

	/**
	 * Method to create a new extended memory model given a reification style
	 * @param style - the reification style
	 * @return a new extended memory model
	 */
	public static Model createModel(ReificationStyle style)
	{ return ExtendedModelFactory.createVirtMemModel(style); }
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