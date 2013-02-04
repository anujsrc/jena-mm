package edu.utdallas.paged.assembler;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.assembler.AssemblerVocab;
import com.hp.hpl.jena.sdb.assembler.BadDescriptionException;
import com.hp.hpl.jena.sdb.assembler.DatasetStoreAssembler;
import com.hp.hpl.jena.sdb.assembler.MissingException;
import com.hp.hpl.jena.sparql.util.graph.GraphUtils;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;

/**
 * An assembler for the extended SDB model
 * @author vaibhav
 */
public class PagedSDBModelAssembler implements PagedAssembler
{
	/** @see com.hp.hpl.jena.assembler.sdb.assembler.DatasetStoreAssembler **/
    DatasetStoreAssembler datasetAssem = new DatasetStoreAssembler() ;
    
    @Override
    public Model open(Assembler a, Resource root, Mode mode)
    {
        Resource dataset = GraphUtils.getResourceValue(root, AssemblerVocab.pDataset) ;
        if ( dataset == null )
            throw new MissingException(root, "No dataset for model or graph") ;
        StoreDesc storeDesc = datasetAssem.openStore(a, dataset, mode) ;

        Resource x = GraphUtils.getResourceValue(root, AssemblerVocab.pNamedGraph1) ;
        if ( x != null && ! x.isURIResource() )
            throw new BadDescriptionException(root, "Graph name not a URI: "+x) ;
        
        if ( x == null )
            return ExtendedSDBFactory.connectPagedDefaultModel(storeDesc) ;
        else
            return ExtendedSDBFactory.connectPagedNamedModel(storeDesc, x) ;
    }

	@Override
	public Object open(Assembler a, Resource root) 
	{ return open( a, root, Mode.DEFAULT ); }

	@Override
	public Object open(Resource root) 
	{ return open( this, root, Mode.DEFAULT ); }

	@Override
	public Model openModel(Resource root) 
	{ return open( this, root, Mode.DEFAULT ); }

	@Override
	public Model openModel(Resource root, Mode mode) 
	{ return open( this, root, mode ); }
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