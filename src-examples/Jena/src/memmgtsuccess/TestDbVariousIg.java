package memmgtsuccess;

import java.sql.SQLException;

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.impl.LiteralImpl;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.vocabulary.VCARD;

import edu.utdallas.paged.db.PagedDBConnection;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

public class TestDbVariousIg 
{
	private static CreateModel cm = new CreateModel();
	private static int noOfAuthors = 0;
	private static int noOfPapers = 0;
	private static long startTime = 0L, endTime = 0L;
	private static IDBConnection conn = null;
	public static void main(String[] args)
	{
		try
		{
			noOfAuthors = Integer.parseInt(args[0]);
			noOfPapers = Integer.parseInt(args[1]);
			System.out.println("no of authors = " + noOfAuthors + " noOfPapers = " + noOfPapers); 	
			String className = args[2];		// path of driver class
			Class.forName (className);              // Load the Driver
			String DB_URL    = args[3];  		// URL of database 
			String DB_NAME   = args[4];
			String DB_USER   = args[5];             // database user id
			String DB_PASSWD = args[6];             // database password
			String DB        = args[7];             // database type

			// Create database connection
			conn = new PagedDBConnection ( DB_URL+DB_NAME, DB_USER, DB_PASSWD, DB );

			//clean the db
			conn.cleanDB();

			// create or open the default model
			ExtendedJenaParameters.initialThreshold = 10000;
			ModelMaker maker = ExtendedModelFactory.createPagedModelRDBMaker(conn) ;
			Model model = maker.createDefaultModel();

			startTime = System.currentTimeMillis();
			cm.createInMemModel(model, noOfAuthors, noOfPapers);
			endTime = System.currentTimeMillis();
			System.out.println("total time to create db with modified code = " + ((endTime - startTime)/1000L) + " seconds.");

			//list all statements in a model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.StmtIterator iter1 = model.listStatements();
			long count = 0;
			while(iter1.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Statement stmt = iter1.nextStatement(); }
			iter1.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all statements in model \t\t\t\t\t= " + count + ",\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list all statements for the first node in the model
			startTime = System.currentTimeMillis();
			iter1 = model.getResource("http://somewhere/johnsmith/-1").listProperties();
			count = 0;
			while(iter1.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Statement stmt = iter1.nextStatement(); }
			iter1.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all statements for the first node in model \t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list a particular statement in a model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.StmtIterator iter2 = model.listStatements(model.getResource("http://somewhere/johnsmith/-1"), VCARD.FN, "John Smith-1");
			count = 0;
			while(iter2.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Statement stmt = iter2.nextStatement(); }
			iter2.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of statement given subject, predicate and object \t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list all subjects in a model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.ResIterator iter3 = model.listSubjects();
			count = 0;
			while(iter3.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Resource stmt = iter3.nextResource(); }
			iter3.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all subjects \t\t\t\t\t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list all subjects with VCARD.FN as a property in the model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.ResIterator iter4 = model.listSubjectsWithProperty(VCARD.FN);
			count = 0;
			while(iter4.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Resource stmt = iter4.nextResource(); }
			iter4.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all subjects with a given property \t\t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list the subjects with a particular property and object in a model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.ResIterator iter5 = model.listSubjectsWithProperty(VCARD.FN, "John Smith-1");
			count = 0;
			while(iter5.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Resource stmt = iter5.nextResource(); }
			iter5.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of subject with a given property and object \t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list the resources with VCARD.FN as a property in the model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.ResIterator iter6 = model.listResourcesWithProperty(VCARD.FN);
			count = 0;
			while(iter6.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Resource stmt = iter6.nextResource(); }
			iter6.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all resources with a given property \t\t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list the subjects with a particular property and object in a model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.ResIterator iter7 = model.listResourcesWithProperty(VCARD.FN, new LiteralImpl( Node.createLiteral("John Smith-1", "", false ), (ModelCom) model));
			count = 0;
			while(iter7.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.Resource stmt = iter7.nextResource(); }
			iter7.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all resources with a given property and object \t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list the objects with a particular property in the model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.NodeIterator iter8 = model.listObjectsOfProperty(VCARD.FN);
			count = 0;
			while(iter8.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.RDFNode stmt = iter8.nextNode(); }
			iter8.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all objects with a given property \t\t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list the objects with a particular subject and property in the model
			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.NodeIterator iter9 = model.listObjectsOfProperty(model.getResource("http://somewhere/johnsmith/-1"), VCARD.FN);
			count = 0;
			while(iter9.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.RDFNode stmt = iter9.nextNode(); }
			iter9.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of object with a given subject and property \t\t\t= " + count + ",\t\t\ttime = " + (endTime - startTime) + " milliseconds."); 

			//list all objects in a model
			startTime = System.currentTimeMillis();
			iter9 = model.listObjects();
			count = 0;
			while(iter9.hasNext())
			{ count++; @SuppressWarnings("unused") com.hp.hpl.jena.rdf.model.RDFNode stmt = iter9.nextNode(); }
			iter9.close();
			endTime = System.currentTimeMillis();
			System.out.println("count of all objects \t\t\t\t\t\t\t= " + count + ",\t\ttime = " + (endTime - startTime) + " milliseconds.");		

			conn.cleanDB(); conn.close();
		}
		catch(Exception e) 
		{
			try
			{ conn.cleanDB(); conn.close(); System.out.println("Found Exception " + e); }
			catch(SQLException sqle) { System.out.println("Found exception " + sqle); }
		}
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