package sp2b.sdb;

import java.io.FileInputStream;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;

public class Sp2bBenchQ6 
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
			" SELECT ?yr ?name ?document " +
			" WHERE { " +
			"			?class rdfs:subClassOf foaf:Document . " +
			"			?document rdf:type ?class . " +
			"			?document dcterms:issued ?yr . " +
			"			?document dc:creator ?author . " +
			"			?author foaf:name ?name " +
			"			OPTIONAL { " +
			"						?class2 rdfs:subClassOf foaf:Document . " +
			"						?document2 rdf:type ?class2 . " +
			"						?document2 dcterms:issued ?yr2 . " +
			"						?document2 dc:creator ?author2 " +
			"						FILTER (?author=?author2 && ?yr2<?yr) " +
			"					 } FILTER (!bound(?author2)) " +
			"		} ";
			QueryExecution qexec = QueryExecutionFactory.create(queryString, model);
			ResultSet rs = qexec.execSelect();
			while( rs.hasNext() )
			{
				count++; rs.nextSolution();
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