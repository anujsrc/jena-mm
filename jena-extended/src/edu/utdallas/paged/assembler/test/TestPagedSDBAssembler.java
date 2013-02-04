package edu.utdallas.paged.assembler.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.sql.Connection;

import org.junit.Test;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.assemblers.AssemblerGroup;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStoreGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.util.FileManager;

import edu.utdallas.paged.assembler.PagedAssembler;
import edu.utdallas.paged.assembler.PagedJA;
import edu.utdallas.paged.sdb.ExtendedSDBFactory;

/**
 * A Junit test class for the extended SDB model assembler 
 * @author vaibhav
 */
public class TestPagedSDBAssembler 
{
	/** The directory containing the database connection description **/
    static final String dir = System.getProperty("user.home")+"/jena/testing/Assembler/" ;
    
    /**
     * JUnit test to check that the dataset and dataset graph exist
     */
    @Test public void dataset_1()
    {
        Dataset ds = DatasetFactory.assemble(dir+"dataset.ttl") ;
        assertNotNull(ds) ;
        DatasetGraph dsg = ds.asDatasetGraph() ;
        assertTrue( dsg instanceof DatasetStoreGraph ) ;
    }

    /**
     * JUnit test to check that the database connection exists
     */
    @Test public void connection_1()
    {
        Connection jdbc = ExtendedSDBFactory.createSqlConnection(dir+"connection.ttl") ;
        assertNotNull(jdbc) ;
    }
    
    /**
     * JUnit test to check that the SDB store exists
     */
    @Test public void store_1()
    {
        Store store = ExtendedSDBFactory.connectStore(dir+"store.ttl") ;
        assertNotNull(store) ;
    }
    
    /**
     * JUnit test to check that the default SDB model exists
     */
    @Test public void model_1()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource x = assem.getResource("http://example/test#graphDft") ;
        Assembler tempAssemblerGroup = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model = (Model) tempAssemblerGroup.open(x);
        assertNotNull(model) ;
    }

    /**
     * JUnit test to check that a named SDB model exists
     */
    @Test public void model_2()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource x = assem.getResource("http://example/test#graphNamed") ;
        Assembler tempAssemblerGroup = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model = (Model) tempAssemblerGroup.open(x);
        assertNotNull(model) ;
    }
    
    /**
     * JUnit test to check that a default and named SDB model exists
     */
    @Test public void model_3()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Assembler assemblerGroupDft = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Assembler assemblerGroupNamed = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model1 = (Model)assemblerGroupDft.open(xDft) ;
        Model model2 = (Model)assemblerGroupNamed.open(xNamed) ;
        assertNotNull(model1 != model2) ;
    }
        
    /**
     * JUnit test to check that a default model with statements is isomorphic with a named model
     */
    @Test public void model_4()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Assembler assemblerGroupDft = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Assembler assemblerGroupNamed = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model1 = (Model)assemblerGroupDft.open(xDft) ;
        Model model2 = (Model)assemblerGroupNamed.open(xNamed) ;
        
        Resource s = model1.createResource() ;
        Property p = model1.createProperty("http://example/p") ;
        Literal o = model1.createLiteral("foo") ;
        
        model1.add(s,p,o) ;
        assertTrue(model1.size() == 1 ) ;
        assertTrue(model2.size() == 0 ) ;
        assertFalse(model1.isIsomorphicWith(model2)) ;
    }
        
    /**
     * JUnit test to check that default models contain statements
     */
    @Test public void model_5()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xDft = assem.getResource("http://example/test#graphDft") ;
        
        Assembler assemblerGroupDft = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model2 = (Model)assemblerGroupDft.open(xDft) ;
        Model model3 = (Model)assemblerGroupDft.open(xDft) ;
        
        Resource s = model2.createResource() ;
        Property p = model2.createProperty("http://example/p") ;
        Literal o2 = model2.createLiteral("xyz") ;
        model2.add(s,p,o2) ;
        assertTrue(model3.contains(s,p,o2)) ;
    }
    
    /**
     * JUnit test to check that named models contain statements
     */
    @Test public void model_6()
    {
        Model assem = FileManager.get().loadModel(dir+"graph-assembler.ttl") ;
        Resource xNamed = assem.getResource("http://example/test#graphNamed") ;
        
        Assembler assemblerGroupNamed = AssemblerGroup.create().implementWith(PagedJA.PagedSDBModel, PagedAssembler.pagedSDBAssembler);
        Model model2 = (Model)assemblerGroupNamed.open(xNamed) ;
        Model model3 = (Model)assemblerGroupNamed.open(xNamed) ;

        Resource s = model2.createResource() ;
        Property p = model2.createProperty("http://example/p") ;
        Literal o2 = model2.createLiteral("xyz") ;
        model2.add(s,p,o2) ;
        assertTrue(model3.contains(s,p,o2)) ;
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