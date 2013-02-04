package extension;

import java.util.Random;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.DC_10;
import com.hp.hpl.jena.vocabulary.VCARD;

public class CreateModel
{
	public CreateModel(){}

	public void createInMemModel(Model model, int noOfAuthors, int noOfPapers)
	{
		Resource johnSmith;
		Resource jSmith_ppr1;

		//Create resources with authors
		for(int i=1; i<=noOfAuthors; i++)
		{
        		johnSmith 
        		= model.createResource("http://somewhere/johnsmith/-"+i)
            			.addProperty(VCARD.FN, "John Smith-"+i)
			    	.addProperty(VCARD.Country, "United States of America-"+i)
			    	.addProperty(VCARD.Given, "John-"+i)
			    	.addProperty(VCARD.Family, "Smith-"+i)
			    	.addProperty(VCARD.BDAY, "1-Jan-1950-"+i)
				.addProperty(VCARD.UID, "U"+i)
				.addProperty(VCARD.EMAIL, "johnsmith@utd.edu-"+i)
				.addProperty(VCARD.Suffix, "JR.-"+i)
				.addProperty(VCARD.Prefix, "Dr.-"+i)
				.addProperty(VCARD.Pobox, "10"+i)
				.addProperty(VCARD.NICKNAME, "Johnny-"+i)
				.addProperty(VCARD.ROLE, "Sys Developer-"+i)
				.addProperty(VCARD.TEL, "214-220-2232-"+i)
				.addProperty(VCARD.Locality, "Inglewood-"+i)
				.addProperty(VCARD.ADR, "2312 Preston Rd, Dallas, Tx, 75272-"+i)
				.addProperty(VCARD.Street, "Preston Rd-"+i)
				.addProperty(VCARD.Orgname, "UT Dallas-"+i)
				.addProperty(VCARD.Region, "East Dallas-"+i)
				.addProperty(VCARD.Pcode, "75272-"+i)
				.addProperty(VCARD.GROUP, "Developer-"+i)
				.addProperty(VCARD.Orgunit, "UT Dallas - Developer-"+i)
				.addProperty(VCARD.KEY, "112-343-3434-"+i)
				.addProperty(VCARD.PRODID, "1"+i)
				.addProperty(VCARD.LABEL, "Key member-"+i)
				.addProperty(VCARD.NOTE, "Important contributor-"+i);
		}
		System.out.println("finished creating authors");	
		Random numGen = new Random();
		for(int j=1; j<=noOfPapers; j++)
		{
			jSmith_ppr1
			= model.createResource("http://somewhere/johnsmith/ppr-"+j)
				.addProperty(DC_10.creator, model.getResource("http://somewhere/johnsmith/-"+(numGen.nextInt(noOfAuthors)+1)))
				.addProperty(DC_10.contributor, model.getResource("http://somewhere/johnsmith/-"+(numGen.nextInt(noOfAuthors)+1)))
				.addProperty(DC_10.date, (numGen.nextInt(12)+1)+"-"+(numGen.nextInt(28)+1)+"-"+(numGen.nextInt(38)+1970))
				.addProperty(DC_10.description, "Sem Web 1-"+j)
				.addProperty(DC_10.format, "PDF-"+j)
				.addProperty(DC_10.identifier, "ppr"+j)
				.addProperty(DC_10.language, "english-"+j)
				.addProperty(DC_10.publisher, "IEEE Computer Society-"+j)
				.addProperty(DC_10.relation, "-"+j)
				.addProperty(DC_10.rights, "Free to copy for class purposes-"+j)
				.addProperty(DC_10.source, "-"+j)
				.addProperty(DC_10.subject, "Semantic Web-"+j)
				.addProperty(DC_10.title, "Introductory topics to semantic web-"+j)
				.addProperty(DC_10.type, "Computer Science/Semantic Web-"+j);
		}
		System.out.println("finished creating papers");
	}
}
/** Copyright (c) 2008, The University of Texas at Dallas
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
