package edu.utdallas.paged.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.assembler.assemblers.ModelAssembler;
import com.hp.hpl.jena.assembler.assemblers.PrefixMappingAssembler;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PrefixMapping;

import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

/**
 * An assembler for the extended memory model
 * @author vaibhav
 */
public class PagedMemoryModelAssembler extends ModelAssembler implements PagedAssembler
{
	@Override
	public Object open( Assembler a, Resource root, Mode mode )
	{ 
		Model m = openModel( a, root, getInitialContent( a, root ), mode );
		addContent( root, m, getContent( a, root ) );
		m.setNsPrefixes( getPrefixMapping( a, root ) );
		return m; 
	}

	@Override
	protected Model openEmptyModel(Assembler a, Resource root, Mode irrelevant) 
	{
		return ExtendedModelFactory.createVirtMemModel( getReificationStyle(root) );
	}

	/**
	 * 
	 * @param a - the assembler
	 * @param root - the resource for this memory model assembler
	 * @return a PrefixMapping
	 */
	private PrefixMapping getPrefixMapping( Assembler a, Resource root )
	{
		return PrefixMappingAssembler.getPrefixes( a, root, PrefixMapping.Factory.create() );
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