package memmgtfail; 

import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelMaker;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestDbReified 
{
	private static CreateModelReified cm = new CreateModelReified();
	private static int noOfAuthors = 0;
	private static int noOfPapers = 0;
	private static long startTime = 0L, endTime = 0L;
	public static void main(String[] args)
	{
		try
		{
			noOfAuthors = Integer.parseInt(args[0]);
			noOfPapers = Integer.parseInt(args[1]);
			System.out.println("no of authors = " + noOfAuthors + " noOfPapers = " + noOfPapers); 	
			String className = args[2];           	// path of driver class
			Class.forName (className);            	// Load the Driver
			String DB_URL    = args[3];  		// URL of database 
			String DB_NAME   = args[4];
			String DB_USER   = args[5];             // database user id
			String DB_PASSWD = args[6];             // database password
			String DB        = args[7];             // database type

			// Create database connection
			IDBConnection conn = new DBConnection ( DB_URL+DB_NAME, DB_USER, DB_PASSWD, DB );
			ModelMaker maker = ModelFactory.createModelRDBMaker(conn) ;

			//clean the db
			conn.cleanDB();

			// create or open the default model
			Model model = maker.createDefaultModel();
			startTime = System.currentTimeMillis();	

			cm.createInMemModel(model, noOfAuthors, noOfPapers);
			endTime = System.currentTimeMillis();
			System.out.println("total time to create db with jena code = " + ((endTime - startTime)/1000L) + " seconds.");

			startTime = System.currentTimeMillis();	
			com.hp.hpl.jena.rdf.model.RSIterator iter = model.listReifiedStatements();
			long count = 0;
			while(iter.hasNext())
			{
				count++;
				@SuppressWarnings("unused")
				com.hp.hpl.jena.rdf.model.ReifiedStatement stmt = iter.nextRS();
			}
			iter.close();
			System.out.println("count of model list reified = " + count);
			endTime = System.currentTimeMillis();
			System.out.println("total time to query db with jena code = " + ((endTime - startTime)/1000L) + " seconds.");

			// Clean and close the database connection
			conn.cleanDB();
			conn.close();		
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