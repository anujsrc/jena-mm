package edu.utdallas.paged.rdf.model.impl;

import java.io.InputStream;

import com.hp.hpl.jena.enhanced.Personality;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFReaderF;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.rdf.model.impl.RDFReaderFImpl;

import edu.utdallas.paged.mem.MemToRDBThread;
import edu.utdallas.paged.mem.PagedGraphTripleStoreBase;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

/**
 * A class that extends ModelCom, used when we are using the unified model.
 * Specifically we make sure that we've finished copying all triples from the 
 * in-memory model to the RDB model before we start querying the model.
 * @author vaibhav
 */

public class ExtendedModelCom extends ModelCom
{
	/**
	 * A reader factory that provides various read functions
	 */
    private static final RDFReaderF readerFactory = new RDFReaderFImpl();

    /**
     * Constructor
     * @param base - the base graph for this model
     */
	public ExtendedModelCom( Graph base ) 
	{ super( base ); }
	
	/**
	 * Constructor
	 * @param base - the base graph for this model
	 * @param personality - the personality for the base graph
	 */
	@SuppressWarnings("unchecked")
	public ExtendedModelCom( Graph base, Personality personality )
	{ super( base, personality ); }
	
	/**
	 * Method that overrides the method from ModelCom to read A-Box triples from a file
	 * @param reader - the input stream reader
	 * @param base - the base uri to use
	 * @param lang - the language, e.g. "N3", "N-TRIPLE", "RDF/XML" etc
	 * @return a model
	 */
  	public Model read(InputStream reader, String base, String lang)
  	{
  		//Read triples from a file
  		readerFactory.getReader(lang).read(this, reader, base);
  		
  		//Make sure that if we use the unified model, we finish copying triples to the RDB model
  		//before we begin querying
        if( ExtendedJenaParameters.useUnifiedModel && PagedGraphTripleStoreBase.isUnifiedModel )
        {
        	while( !MemToRDBThread.subjects.isEmpty() && Thread.activeCount() > 1 )
        	{
        		try
        		{ Thread.sleep(1); }
        		catch( Exception e ) { e.printStackTrace(); }
        	}
        }
		return this;
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