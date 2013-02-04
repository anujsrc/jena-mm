package sp2b.mem;

import java.io.FileInputStream;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.rdf.model.Model;

import edu.utdallas.paged.query.PagedQueryExecutionFactory;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

public class Sp2bBenchQ12c
{
	public static void main(String[] args)
	{
		ExtendedJenaParameters.initialThreshold = 7500;
		Model model = ExtendedModelFactory.createVirtMemModel();
		long startTime = 0L, endTime = 0L;
		try
		{
			startTime = System.currentTimeMillis();
			model.read(new FileInputStream(args[0]), null, "N3");
			endTime = System.currentTimeMillis();
			System.out.println("time to read in the model = " + (endTime-startTime)/1000 + " seconds.");
			startTime = System.currentTimeMillis();
			String queryString = 
			" PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			" PREFIX person: <http://localhost/persons/> " +
			" PREFIX foaf: <http://xmlns.com/foaf/0.1/> " +
			" ASK { " +
			"		person:John_Q_Public rdf:type foaf:Person. " +
			"	  } ";
			QueryExecution qexec = PagedQueryExecutionFactory.create(queryString, model); 
			if ( qexec.execAsk() )
				System.out.println("executed ask query correctly");
			else
				System.out.println("did not execute ask query");
			qexec.close();
			endTime = System.currentTimeMillis();
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