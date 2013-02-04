package memmgtsuccess;

import com.hp.hpl.jena.rdf.model.Model;
import edu.utdallas.paged.rdf.model.ExtendedModelFactory;
import edu.utdallas.paged.shared.impl.ExtendedJenaParameters;

public class ConcurrentIg implements Runnable
{
	private static CreateModel cm = new CreateModel();
	private static int noOfAuthors = 0;
	private static int noOfPapers = 0;
	public int tId = 0;

	public ConcurrentIg(int threadNo)
	{ tId = threadNo; }	

	public void run() 
	{
		long startTime = System.currentTimeMillis();
		ExtendedJenaParameters.initialThreshold = 10000;
		Model model = ExtendedModelFactory.createVirtMemModel();
		cm.createInMemModel(model, noOfAuthors, noOfPapers);
		com.hp.hpl.jena.rdf.model.StmtIterator iter = model.listStatements();
		long count = 0;
		while(iter.hasNext())
		{
			count++;
			@SuppressWarnings("unused")
			com.hp.hpl.jena.rdf.model.Statement stmt = iter.nextStatement();
		}
		iter.close();
		System.out.println("count of model for thread: " + tId + " = " + count);
		long endTime = System.currentTimeMillis();
		System.out.println("Total time = " + (endTime - startTime) + " milliseconds.");
		System.out.println();
    }

    public static void main(String args[]) 
    {
    	noOfAuthors = Integer.parseInt(args[0]);
    	noOfPapers = Integer.parseInt(args[1]);

    	System.out.println("no of authors = " + noOfAuthors + " noOfPapers = " + noOfPapers); 
        (new Thread(new ConcurrentIg(1))).start();
        (new Thread(new ConcurrentIg(2))).start();
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