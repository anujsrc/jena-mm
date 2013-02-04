package sp2b.sdb;

import java.io.FileInputStream;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;

public class Sp2bBenchQ7 
{
	public static void main(String[] args)
	{
		long startTime = 0L, endTime = 0L;
		try
		{
			Model model = null; Store store = null;
			if( args.length == 1 )
			{
				store = ExtendedSDBFactory.connectStore(args[0]);
				store.getTableFormatter().create();
				model = ExtendedSDBFactory.connectPagedDefaultModel(store);
			}
			else
			{
				StoreDesc storeDesc = new StoreDesc(args[0], args[1]);
				Class.forName(args[2]);
				String jdbcURL = args[3]+args[4]; 
				SDBConnection conn = new SDBConnection(jdbcURL, args[5], args[6]) ;
				store = ExtendedSDBFactory.connectStore(conn, storeDesc) ;
				store.getTableFormatter().create();
				model = ExtendedSDBFactory.connectPagedNamedModel(store, args[7]);
			}

			startTime = System.currentTimeMillis();
			model.read(new FileInputStream(args[8]), null, "N3");
			endTime = System.currentTimeMillis();
			System.out.println("time to read in the model = " + (endTime-startTime)/1000 + " seconds.\n");
			startTime = System.currentTimeMillis();

			long count = 0L;
			String queryString = 
			" PREFIX rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			" PREFIX rdfs:    <http://www.w3.org/2000/01/rdf-schema#> " +
			" PREFIX foaf:    <http://xmlns.com/foaf/0.1/> " +
			" PREFIX dc:      <http://purl.org/dc/elements/1.1/> " +
			" PREFIX dcterms: <http://purl.org/dc/terms/> " +
			" SELECT DISTINCT ?title " +
			" WHERE { " +
			" 			?class rdfs:subClassOf foaf:Document . " +
			"			?doc rdf:type ?class . " +
			"			?doc dc:title ?title . " +
			"			?bag2 ?member2 ?doc . " +
			" 			?doc2 dcterms:references ?bag2 " +
			"			OPTIONAL { " +
			"						?class3 rdfs:subClassOf foaf:Document ." +
			"					    ?doc3 rdf:type ?class3 . " +
			"						?doc3 dcterms:references ?bag3 . " +
			"						?bag3 ?member3 ?doc " +
			"						OPTIONAL { " +
			"									?class4 rdfs:subClassOf foaf:Document . " +
			"									?doc4 rdf:type ?class4 . " +
			"									?doc4 dcterms:references ?bag4 . " +
			"									?bag4 ?member4 ?doc3 " +
			"								 } FILTER (!bound(?doc4)) " +
			"				     } FILTER (!bound(?doc3)) " +
			"	   } ";
			QueryExecution qexec = QueryExecutionFactory.create(queryString, model);
			ResultSet rs = qexec.execSelect();
			while( rs.hasNext() )
			{
				count++; 
				QuerySolution rb = rs.nextSolution();
				RDFNode title = rb.getLiteral("?title");
				System.out.println(" title = " + title.toString());
			}
			qexec.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of found statements = " + count);
			System.out.println("time to query for results = " + (endTime-startTime) + " milliseconds.");
		}
		catch(Exception e) { e.printStackTrace(); }
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