package lubm.db;

import java.io.File;
import java.io.InputStream;

import lubm.mem.OWLFilenameFilter;

import org.mindswap.pellet.jena.PelletReasonerFactory;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.util.FileManager;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.db.PagedModelRDB;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;

public class LubmQ13 
{
	public static void main( String[] args )
	{
		try
		{
			String className = args[0];
			Class.forName(className);
			String DB_URL    = args[1];  			
			String DB_NAME   = args[2];				
			String DB_USER   = args[3];             
			String DB_PASSWD = args[4];             
			String DB        = args[5];             

			IDBConnection conn = new PagedDBConnection( DB_URL+DB_NAME, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ExtendedModelFactory.createPagedModelRDBMaker(conn) ;
			PagedModelRDB schema = (PagedModelRDB)maker.createModel(args[6]);
			schema.setDoDuplicateCheck(false);

			long startTime = System.currentTimeMillis(), endTime = 0L, count = 0L;
			schema.read("http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl");
			
			OntModel m = ExtendedModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC, schema );
			String sInputDirectory = args[7];
			File inputDirectory = new File(sInputDirectory);
			String[] sFilenames = inputDirectory.list(new OWLFilenameFilter());
			for (int i = 0; i < sFilenames.length; i++) 
			{
				InputStream in = FileManager.get().open(sInputDirectory+sFilenames[i]);
				if (in == null) { throw new IllegalArgumentException( "File: " + sFilenames[i] + " not found"); }
				m.read( in, "http://www.utdallas.edu/benchmark-test#", "RDF/XML-ABBREV");
				in.close();
			}
			endTime = System.currentTimeMillis();	
			System.out.println("time to read the model = " + (endTime-startTime)/1000 + " seconds.");
			
			startTime = System.currentTimeMillis();
			String queryString = 
				" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				" PREFIX ub: <http://www.lehigh.edu/~zhp2/2004/0401/univ-bench.owl#> " +
				" SELECT ?X " +
				" WHERE " +
				" { " +
				"		?X rdf:type ub:Person . " +
				"		<http://www.University0.edu> ub:hasAlumnus ?X " +
				" } ";

			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.create(query, m);
			ResultSet rs = qexec.execSelect();
			while( rs.hasNext() )
			{ count++; rs.nextSolution(); }
			qexec.close();
			endTime = System.currentTimeMillis();
			System.out.println("count = " + count);
			System.out.println("time to query = " + (endTime-startTime) + " milliseconds.");

			conn.cleanDB();conn.close();
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