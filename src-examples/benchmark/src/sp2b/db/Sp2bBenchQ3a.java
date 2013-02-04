package sp2b.db;

import java.io.FileInputStream;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

public class Sp2bBenchQ3a 
{
	public static void main(String[] args)
	{
		try
		{
			String className = args[0];
			Class.forName(className);
			String DB_URL    = args[1];  			// URL of database 
			String DB_NAME   = args[2];				// DB name
			String DB_USER   = args[3];             // database user id
			String DB_PASSWD = args[4];             // database password
			String DB        = args[5];             // database type

			IDBConnection conn = new PagedDBConnection( DB_URL+DB_NAME, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ExtendedModelFactory.createPagedModelRDBMaker(conn) ;
			PagedModelRDB model = (PagedModelRDB)maker.createModel(args[6]);
			model.setDoDuplicateCheck(false);
			long startTime = System.currentTimeMillis();
			model.read(new FileInputStream(args[7]), null, "N3");
			long endTime = System.currentTimeMillis();
			System.out.println("time to read in the model = " + (endTime-startTime)/1000 + " seconds.\n");
			
			startTime = System.currentTimeMillis(); long count = 0L;
			String queryString = 
			" PREFIX rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			" PREFIX bench: <http://localhost/vocabulary/bench/> " +
			" PREFIX swrc:  <http://swrc.ontoware.org/ontology#> " +
			" SELECT ?article " +
			" WHERE { " +
			"          ?article rdf:type bench:Article . " +
			"          ?article ?property ?value " +
			"          FILTER (?property=swrc:pages) " +
			"       } ";
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

			conn.cleanDB(); conn.close();
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