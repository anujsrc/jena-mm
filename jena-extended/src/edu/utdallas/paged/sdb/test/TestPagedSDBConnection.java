package edu.utdallas.paged.sdb.test;

import java.sql.Connection;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;
import edu.utdallas.paged.sdb.store.ExtendedStoreFactory;

public class TestPagedSDBConnection 
{
    public final String testDirPagedSDB = System.getProperty("user.home")+"/jena/testing/SDB/" ;

    @Test public void connection_1()
    {
        String desc = testDirPagedSDB+"mysql-hash.ttl" ;
        StoreDesc sDesc = StoreDesc.read(desc) ;
        
        Connection c = ExtendedSDBFactory.createSqlConnection(desc) ;
        
        SDBConnection conn1 = ExtendedSDBFactory.createConnection(c) ;
        Store store1 = ExtendedStoreFactory.create(sDesc, conn1) ;
        
        SDBConnection conn2 = ExtendedSDBFactory.createConnection(c) ;
        Store store2 = ExtendedStoreFactory.create(sDesc, conn2) ;
        
        Model model1 = ExtendedSDBFactory.connectDefaultModel(store1) ;
        Model model2 = ExtendedSDBFactory.connectDefaultModel(store2) ;
        
        Resource s = model1.createResource() ;
        Property p = model1.createProperty("http://example/p") ;
        
        model1.add(s, p, "model1") ;
        model2.add(s, p, "model2") ;
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