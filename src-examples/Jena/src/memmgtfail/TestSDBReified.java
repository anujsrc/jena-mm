package memmgtfail;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;

import edu.utdallas.paged.sdb.ExtendedSDBFactory;

public class TestSDBReified
{
	private static CreateModelReified cm = new CreateModelReified();
	private static int noOfAuthors = 0;
	private static int noOfPapers = 0;
	private static long startTime = 0L, endTime = 0L;
	private static Store store = null;
	
	public static void main(String[] args)
	{
		try
		{
			noOfAuthors = Integer.parseInt(args[0]);
			noOfPapers = Integer.parseInt(args[1]);
			System.out.println("no of authors = " + noOfAuthors + " noOfPapers = " + noOfPapers);
			Model model = null;
			if( args.length == 3 )
			{
				store = SDBFactory.connectStore(args[2]);
				store.getTableFormatter().create();
				model = SDBFactory.connectDefaultModel(store);
			}
			else
			{
				StoreDesc storeDesc = new StoreDesc(args[2], args[3]);
				Class.forName(args[4]);
				String jdbcURL = args[5]+args[6]; 
				SDBConnection conn = new SDBConnection(jdbcURL, args[7], args[8]) ;
				store = ExtendedSDBFactory.connectStore(conn, storeDesc) ;
				store.getTableFormatter().create();
				model = ExtendedSDBFactory.connectPagedDefaultModel(store);
			}

			startTime = System.currentTimeMillis();
			cm.createInMemModel(model, noOfAuthors, noOfPapers);
			endTime = System.currentTimeMillis();
			System.out.println("total time to create db with jena code = " + ((endTime - startTime)/1000L) + " seconds.");

			startTime = System.currentTimeMillis();
			com.hp.hpl.jena.rdf.model.StmtIterator iter = model.listStatements();
			long count = 0;
			while(iter.hasNext())
			{
				count++;
				@SuppressWarnings("unused")
				com.hp.hpl.jena.rdf.model.Statement stmt = iter.nextStatement();
			}
			iter.close();
			System.out.println("count of model list all = " + count);
			endTime = System.currentTimeMillis();
			System.out.println("total time to query db with jena code = " + ((endTime - startTime)/1000L) + " seconds.");
			store.getTableFormatter().truncate();
			store.close();
		}
		catch(Exception e) 
		{ store.getTableFormatter().truncate(); store.close(); System.out.println("Found exception " + e); }
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