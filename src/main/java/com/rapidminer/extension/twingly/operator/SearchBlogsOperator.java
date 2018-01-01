package com.rapidminer.extension.twingly.operator;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.operator.preprocessing.filter.Real2Integer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.twingly.search.*;
import com.twingly.search.client.Client;
import com.twingly.search.client.UrlConnectionClient;
import com.twingly.search.domain.Post;
import com.twingly.search.domain.Result;

public class SearchBlogsOperator extends AbstractExampleSource {

	private static final String SEARCH_TERM = "Search Term";
	// private InputPort inputPort;
	//	private OutputPort outputPort;

	/**
	 * Default operator constructor.
	 *
	 * @param description
	 *            the opreator description
	 */
	public SearchBlogsOperator(OperatorDescription description) {
		super(description);

	}

	

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> parameterTypes = super.getParameterTypes();
		ParameterType type = new ParameterTypeString(SEARCH_TERM, "Search Term", "");
		type.setOptional(false);
		parameterTypes.add(type);

		return parameterTypes;
	}

	
	public static Integer replaceNullInInteger(Integer input) {
		  return input == null ? 0 : input;
		}
	
	
	
	
	private String replaceNull(String input) {
		getLogger().fine("found a null");
		  return input == null ? "" : input;
		}
	
	@Override
	public ExampleSet createExampleSet() throws OperatorException {

		String apiKey = "36A6496E-F0C3-46AE-948F-178DCFCCFB65";
	        // create Client object using your API key
	        Client client = new UrlConnectionClient(apiKey);
	        
	        String query = getParameterAsString(SEARCH_TERM);

	        
	        Result result = client.makeRequest(query);
	        
	        	
	        
	        // Get the total number of matches returned
	        int numberOfMatchesReturned = result.getNumberOfMatchesReturned();
	        // Get the total number of matches to the search query
	        int numberOfMatchesTotal = result.getNumberOfMatchesTotal();
	        // Get number of seconds it took to execute query
	        double secondsElapsed = result.getSecondsElapsed();
	        
	        getLogger().info("Matches Returned" + String.valueOf(numberOfMatchesReturned));
	        getLogger().info("Total number of Matches" + String.valueOf(numberOfMatchesTotal));
	        getLogger().info("Seconds Elapsed" + String.valueOf(secondsElapsed));
	        
	        Attribute blogid = AttributeFactory.createAttribute("Blog Id", Ontology.NOMINAL);
	        Attribute blogname = AttributeFactory.createAttribute("Blog Name", Ontology.NOMINAL);
	        Attribute blogurl = AttributeFactory.createAttribute("Blog URL", Ontology.NOMINAL);
	       
	        Attribute id = AttributeFactory.createAttribute("ID", Ontology.NOMINAL);
	        Attribute languagecode = AttributeFactory.createAttribute("Language Code", Ontology.NOMINAL);
	        Attribute locationcode = AttributeFactory.createAttribute("Location Code", Ontology.NOMINAL);
	        
	        Attribute url = AttributeFactory.createAttribute("URL", Ontology.NOMINAL);
	        Attribute author = AttributeFactory.createAttribute("Author ", Ontology.STRING);
	        Attribute title = AttributeFactory.createAttribute("Title", Ontology.STRING);
	        
	        Attribute text = AttributeFactory.createAttribute("Text", Ontology.NOMINAL);
	        Attribute tags = AttributeFactory.createAttribute("Tags", Ontology.NOMINAL);
	        Attribute links = AttributeFactory.createAttribute("Links", Ontology.STRING);
	        
	        
	        Attribute authority = AttributeFactory.createAttribute("Authority", Ontology.INTEGER);
	        Attribute blogrank = AttributeFactory.createAttribute("Blog Rank", Ontology.INTEGER);
	        Attribute inlinks = AttributeFactory.createAttribute("InLinks", Ontology.INTEGER);

	        Attribute lat = AttributeFactory.createAttribute("Latitude", Ontology.REAL);
	        Attribute longi = AttributeFactory.createAttribute("Longitude", Ontology.REAL);
	        Attribute pubdate = AttributeFactory.createAttribute("Published Date", Ontology.DATE_TIME);

	        Attribute indexdate = AttributeFactory.createAttribute("Indexed Date", Ontology.DATE_TIME);
	        Attribute repubdate = AttributeFactory.createAttribute("RePublished Date", Ontology.DATE_TIME);
	        
	        
	        
			ExampleSetBuilder exampleSetBuilder = 
					
				
					
					ExampleSets.from(blogid,blogname,blogurl,
					id,languagecode, locationcode,
					url,author,title,
					text,tags,links, 
					authority,blogrank,inlinks,
					lat,longi,pubdate,
					indexdate,repubdate);

	        
			double[] row = new double[20];
	        
	        
	        for (Post p : result.getPosts()){
	        	
	        	getLogger().info("adding row info");
	        	
	        	row[0] = blogid.getMapping().mapString(replaceNull(p.getBlogId()));
	        	row[1] = blogname.getMapping().mapString(replaceNull(p.getBlogName()));
	        	row[2] = blogurl.getMapping().mapString(replaceNull(p.getBlogUrl()));

	        	row[3] = id.getMapping().mapString(replaceNull(p.getId()));
	        	row[4] = languagecode.getMapping().mapString(replaceNull(p.getLanguageCode()));
	        	row[5] = locationcode.getMapping().mapString(replaceNull(p.getLocationCode()));
	        	
	        	row[6] = url.getMapping().mapString(replaceNull(p.getUrl()));
	        	row[7] = author.getMapping().mapString(replaceNull(p.getAuthor()));
	        	row[8] = title.getMapping().mapString(replaceNull(p.getTitle()));
	        	
	        	row[9] = text.getMapping().mapString(replaceNull(p.getText()));
	        	if(p.getTags().isEmpty())
	        	{
	        		row[10] =  tags.getMapping().mapString("");
	        	}
	        	else
	        	{
	        		row[10] =  tags.getMapping().mapString(StringUtils.join(p.getTags(),','));	
	        	}
	        	if(p.getTags().isEmpty())
	        	{
	        	
	        		row[11] = links.getMapping().mapString("");
	        	}
	        	else
	        	{
	        		row[11] = links.getMapping().mapString(StringUtils.join(p.getLinks(),','));
	        	}
	        	
	        	
        	   	row[12] = p.getAuthority() ;
	        	row[13] = p.getBlogRank();
	        	row[14] = p.getInlinksCount();
	        	
	        	
	        	row[15] = p.getLatitude() == null ? 0 : p.getLatitude().doubleValue();
	        	row[16] = p.getLongitude() == null ? 0 : p.getLongitude().doubleValue();
	        	row[17] = p.getPublishedAt() == null ? 0:  p.getPublishedAt().toInstant().toEpochMilli();
	        	

	        	row[18] = p.getIndexedAt() == null ? 0 :  p.getIndexedAt().toInstant().toEpochMilli();
	        	row[19] = p.getReindexedAt()== null ? 0 : p.getReindexedAt().toInstant().toEpochMilli();
	        	
	        	
	            exampleSetBuilder.addDataRow(new DoubleArrayDataRow(row));
	        }
		
		return exampleSetBuilder.build();
	}

}
