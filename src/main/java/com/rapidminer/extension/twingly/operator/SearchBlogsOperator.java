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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.config.ConfigurationException;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;
import com.twingly.search.*;
import com.twingly.search.client.Client;
import com.twingly.search.client.UrlConnectionClient;
import com.twingly.search.domain.Post;
import com.twingly.search.domain.Result;

public class SearchBlogsOperator extends AbstractExampleSource {

	private static final String Query_TERM = "Query Term";
	// private InputPort inputPort;
	// private OutputPort outputPort;
	private static final String PARAMETER_CONFIG = "API Connection";
	private static final String SEARCH_TEXT = "Search Text";
	private static final String SEARCH_TITLE = "Search Title";
	private static final String SEARCH_URL = "Search URL ";
	private static final String SEARCH_AUTHOR = "Search Author";
	private static final String SEARCH_TAGS = "Search Tags";
	private static final String SEARCH_LINKS = "Search Links";
	private static final String SEARCH_BLOGNAME = "Search Blogname";
	private static final String SEARCH_BLOGURL = "Search BlogURL";
	private static final String TIMESPAN = "Timespan";
	private static final String MAXPAGE = "Maximum Number of Pages";
	private static final String SORTBY = "Sort By for search Results";
	private static final String SORTORDER = "Sort Order for search Results";

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

		ParameterType type = new ParameterTypeConfigurable(PARAMETER_CONFIG,
				"Choose a TWINGLY connection", "TWINGLY");
		type.setOptional(false);
		parameterTypes.add(type);

		type = new ParameterTypeString(Query_TERM,
				"Search Term", "");
		type.setOptional(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(TIMESPAN, "Time Span To Search",new String[]{"Anytime","h","12h","24h","w","m","3m"},0,false);
		parameterTypes.add(type);
		
		
		
		type = new ParameterTypeInt(MAXPAGE, "Maximum number of pages to bring back, each page is 1000 articles", 1, 100, 5,false);
		parameterTypes.add(type);

		

		type = new ParameterTypeCategory(SORTBY, "Sort By for Search",new String[]{"published","created","inlinks","twinglyrank"},0,false);
		parameterTypes.add(type);
	
		type = new ParameterTypeCategory(SORTORDER, "Sort Order for Search",new String[]{"asc","desc"},1,false);
		parameterTypes.add(type);
		
		
		type = new ParameterTypeBoolean(SEARCH_TEXT,"Search Term in Text of the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);

		type = new ParameterTypeBoolean(SEARCH_TITLE,"Search Term in title of the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_URL,"Search Term in url of the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_AUTHOR,"Search Term in author of the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_TAGS,"Search Term in tags for the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_LINKS,"Search Term in links for  the blog", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_BLOGNAME,"Search Term in blogs name ", true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeBoolean(SEARCH_BLOGURL,"Search Term in blog url ", true);
		type.setExpert(true);
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

		// String apiKey = "36A6496E-F0C3-46AE-948F-178DCFCCFB65";

		String apiKey;
		try {
			apiKey = ConfigurationManager
					.getInstance()
					.lookup("TWINGLY",
							getParameterAsString(PARAMETER_CONFIG),
							getProcess().getRepositoryAccessor())
					.getParameter("API_KEY");

			
			

			Attribute blogid = AttributeFactory.createAttribute("Blog Id",
					Ontology.NOMINAL);
			Attribute blogname = AttributeFactory.createAttribute("Blog Name",
					Ontology.NOMINAL);
			Attribute blogurl = AttributeFactory.createAttribute("Blog URL",
					Ontology.NOMINAL);

			Attribute id = AttributeFactory.createAttribute("ID",
					Ontology.NOMINAL);
			Attribute languagecode = AttributeFactory.createAttribute(
					"Language Code", Ontology.NOMINAL);
			Attribute locationcode = AttributeFactory.createAttribute(
					"Location Code", Ontology.NOMINAL);

			Attribute url = AttributeFactory.createAttribute("URL",
					Ontology.NOMINAL);
			Attribute author = AttributeFactory.createAttribute("Author ",
					Ontology.STRING);
			Attribute title = AttributeFactory.createAttribute("Title",
					Ontology.STRING);

			Attribute text = AttributeFactory.createAttribute("Text",
					Ontology.NOMINAL);
			Attribute tags = AttributeFactory.createAttribute("Tags",
					Ontology.NOMINAL);
			Attribute links = AttributeFactory.createAttribute("Links",
					Ontology.STRING);

			Attribute authority = AttributeFactory.createAttribute("Authority",
					Ontology.INTEGER);
			Attribute blogrank = AttributeFactory.createAttribute("Blog Rank",
					Ontology.INTEGER);
			Attribute inlinks = AttributeFactory.createAttribute("InLinks",
					Ontology.INTEGER);

			Attribute lat = AttributeFactory.createAttribute("Latitude",
					Ontology.REAL);
			Attribute longi = AttributeFactory.createAttribute("Longitude",
					Ontology.REAL);
			Attribute pubdate = AttributeFactory.createAttribute(
					"Published Date", Ontology.DATE_TIME);

			Attribute indexdate = AttributeFactory.createAttribute(
					"Indexed Date", Ontology.DATE_TIME);
			Attribute repubdate = AttributeFactory.createAttribute(
					"RePublished Date", Ontology.DATE_TIME);

			ExampleSetBuilder exampleSetBuilder =

			ExampleSets.from(blogid, blogname, blogurl, id, languagecode,
					locationcode, url, author, title, text, tags, links,
					authority, blogrank, inlinks, lat, longi, pubdate,
					indexdate, repubdate);

			double[] row = new double[20];

			// create Client object using your API key
			Client client = new UrlConnectionClient(apiKey);
			String query =  createSearchString(1);
			getLogger().info(">>>>>" + query + "<<<<<");

			
			
			Result result = client.makeRequest(query);

			// Get the total number of matches returned
			int numberOfMatchesReturned = result.getNumberOfMatchesReturned();
			// Get the total number of matches to the search query
			int numberOfMatchesTotal = result.getNumberOfMatchesTotal();
			// Get number of seconds it took to execute query
			double secondsElapsed = result.getSecondsElapsed();

			int pageCount=  getNumberofPages(result);
			
			
			
			getLogger().info(
					"Matches Returned"
							+ String.valueOf(numberOfMatchesReturned));
			getLogger().info(
					"Total number of Matches"
							+ String.valueOf(numberOfMatchesTotal));
			getLogger()
					.info("Seconds Elapsed" + String.valueOf(secondsElapsed));
			
			
			for(int pageCounter = 0; pageCounter < pageCount && pageCounter <  getParameterAsInt(MAXPAGE); pageCounter++)
			{
			 if(pageCounter == 0)
			 {
				 
			 }
			 else
			 {
					query =  createSearchString(pageCounter+1);
					getLogger().info(">>>>>" + query + "<<<<<");
					result = client.makeRequest(query);
			 }
			
			
			for (Post p : result.getPosts()) {

				//getLogger().info("adding row info");

				row[0] = blogid.getMapping().mapString(
						replaceNull(p.getBlogId()));
				row[1] = blogname.getMapping().mapString(
						replaceNull(p.getBlogName()));
				row[2] = blogurl.getMapping().mapString(
						replaceNull(p.getBlogUrl()));

				row[3] = id.getMapping().mapString(replaceNull(p.getId()));
				row[4] = languagecode.getMapping().mapString(
						replaceNull(p.getLanguageCode()));
				row[5] = locationcode.getMapping().mapString(
						replaceNull(p.getLocationCode()));

				row[6] = url.getMapping().mapString(replaceNull(p.getUrl()));
				row[7] = author.getMapping().mapString(
						replaceNull(p.getAuthor()));
				row[8] = title.getMapping()
						.mapString(replaceNull(p.getTitle()));

				row[9] = text.getMapping().mapString(replaceNull(p.getText()));
				if (p.getTags().isEmpty()) {
					row[10] = tags.getMapping().mapString("");
				} else {
					row[10] = tags.getMapping().mapString(
							StringUtils.join(p.getTags(), ','));
				}
				if (p.getTags().isEmpty()) {

					row[11] = links.getMapping().mapString("");
				} else {
					row[11] = links.getMapping().mapString(
							StringUtils.join(p.getLinks(), ','));
				}

				row[12] = p.getAuthority();
				row[13] = p.getBlogRank();
				row[14] = p.getInlinksCount();

				row[15] = p.getLatitude() == null ? 0 : p.getLatitude()
						.doubleValue();
				row[16] = p.getLongitude() == null ? 0 : p.getLongitude()
						.doubleValue();
				row[17] = p.getPublishedAt() == null ? 0 : p.getPublishedAt()
						.toInstant().toEpochMilli();

				row[18] = p.getIndexedAt() == null ? 0 : p.getIndexedAt()
						.toInstant().toEpochMilli();
				row[19] = p.getReindexedAt() == null ? 0 : p.getReindexedAt()
						.toInstant().toEpochMilli();

				exampleSetBuilder.addDataRow(new DoubleArrayDataRow(row));
			}
			
			}//morethan one page
			return exampleSetBuilder.build();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			getLogger().info(e.getMessage());

		}
		return null;
	}

	private int getNumberofPages(Result result) {
		// TODO Auto-generated method stub
		
		return (int) Math.ceil(result.getNumberOfMatchesTotal() / 1000);
		
		
	}

	private String createSearchString(Integer PageNumber) throws UndefinedParameterError {
		// TODO Auto-generated method stub
		StringBuilder builder = new StringBuilder();
		
		//QueryBuilder qb =  QueryBuilder.create(getParameterAsString(Query_TERM));
		
		
		if(!getParameterAsString(TIMESPAN).equals("Anytime"))
		{
			builder.append("tspan:"+ getParameter(TIMESPAN) + " ");
		}
		
		builder.append("sort:" + getParameter(SORTBY) + " ");
		builder.append("sort-order:" + getParameter(SORTORDER) + " ");
		
		builder.append("page:" + String.valueOf(PageNumber)+ " ");
		
		if(		getParameterAsBoolean(SEARCH_AUTHOR) 
				&& getParameterAsBoolean(SEARCH_BLOGNAME)
				&& getParameterAsBoolean(SEARCH_BLOGURL)
				&& getParameterAsBoolean(SEARCH_LINKS)
				&& getParameterAsBoolean(SEARCH_TAGS)
				&& getParameterAsBoolean(SEARCH_TEXT)
				&& getParameterAsBoolean(SEARCH_TITLE)
				&& getParameterAsBoolean(SEARCH_URL)
				
				)
				{
				//	builder.append( getParameterAsString(Query_TERM));
				}
		else
		{
			builder.append("field:");
			Integer added = 0;
			if(getParameterAsBoolean(SEARCH_AUTHOR)) {added=1; builder.append("author");}
			
			if(getParameterAsBoolean(SEARCH_BLOGNAME)) {
				if(added==0)
				{
					builder.append("blogname");
					added = 1;
				}
				else
				{
					builder.append("|blogname");
				}
				
			}
			
			if(getParameterAsBoolean(SEARCH_BLOGURL)) {
				if(added==0)
				{
					builder.append("blogurl");
					added = 1;
				}
				else
				{
					builder.append("|blogurl");
				}	
			}

			if(getParameterAsBoolean(SEARCH_LINKS)) {
				if(added==0)
				{
					builder.append("links");
					added = 1;
				}
				else
				{
					builder.append("|links");
				}	
			}
			
			if(getParameterAsBoolean(SEARCH_TAGS)) {
				if(added==0)
				{
					builder.append("tags");
					added = 1;
				}
				else
				{
					builder.append("|tags");
				}	
			}
			
			if(getParameterAsBoolean(SEARCH_TEXT)) {
				if(added==0)
				{
					builder.append("text");
					added = 1;
				}
				else
				{
					builder.append("|text");
				}	
			}
			
			if(getParameterAsBoolean(SEARCH_TITLE)) {
				if(added==0)
				{
					builder.append("title");
					added = 1;
				}
				else
				{
					builder.append("|title");
				}	
			}
			if(getParameterAsBoolean(SEARCH_URL)) {
				if(added==0)
				{
					builder.append("url");
					added = 1;
				}
				else
				{
					builder.append("|url");
				}	
			}
			
		
		
	}
		builder.append(" " + getParameterAsString(Query_TERM));
		return builder.toString();
	}
}


