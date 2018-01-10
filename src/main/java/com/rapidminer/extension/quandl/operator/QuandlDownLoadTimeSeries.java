package com.rapidminer.extension.quandl.operator;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import com.rapidminer.RapidMiner;
import com.rapidminer.core.io.data.source.DataSourceConfiguration;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleReader;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.io.remote.RemoteFile;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.operator.io.URLExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.studio.io.data.internal.file.csv.CSVDataSource;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.WebServiceTools;
import com.rapidminer.tools.config.ConfigurationException;
import com.rapidminer.tools.config.ConfigurationManager;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

public class QuandlDownLoadTimeSeries extends URLExampleSource {

	private static final String PARAMETER_CONFIG = "Select Connection";
	private static final String DATABASE_CODE = "Database Code";
	private static final String DATASET_CODE = "DataSet Code";
	private static final String LIMIT = "Number of rows to limit";
	private static final String COLUMN_INDEX = "Specific column number";
	private static final String START_DATE = "Start Date (yyyy-mm-dd)";
	private static final String END_DATE = "End Date (yyyy-mm-dd)";
	private static final String ORDER = "Sort Order";
	private static final String COLLAPSE = "Collapse";
	private static final String TRANSFORM = "Transform";

	private String TimeSeriesBaseURL  = "https://www.quandl.com/api/v3/datasets";
	private OperatorDescription odesc;
	
	public QuandlDownLoadTimeSeries(OperatorDescription description) {
		super(description);
	//	odesc
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExampleSet createExampleSet() throws OperatorException {
		// TODO Auto-generated method stub
		

		try {
			String APIKey =	ConfigurationManager
					.getInstance()
					.lookup("QUANDL",
							getParameterAsString(PARAMETER_CONFIG),
							getProcess().getRepositoryAccessor())
					.getParameter("API_KEY");
			

			String url = TimeSeriesBaseURL + "/" + getParameterAsString(DATABASE_CODE) + "/" + getParameterAsString(DATASET_CODE) + "/data.csv?";
			getLogger().info(url);
	//		boolean someparameteradded = false;
			
	//		if(getParameterAsInt(LIMIT)> 0)
	//			{url = url + "limit=" + getParameterAsInt(LIMIT) + '&'; }
	//		getLogger().info(">>" + url + "<<");	
	//		if(getParameterAsInt(COLUMN_INDEX)>0 )
	//			{url = url + "column_index=" + getParameterAsInt(COLUMN_INDEX) + '&';}
	//		getLogger().info(">>" + url + "<<");
			
			
			try{
			
			if(!getParameterAsString(START_DATE).isEmpty()){url = url  + "state_date=" +  getParameterAsString(START_DATE) + '&'; }
			}
			catch(Exception e)
			{
				
			}
			getLogger().info(">>" + url + "<<");
			
			try{
			if(!getParameterAsString(END_DATE).isEmpty()){url = url  + "end_date=" +  getParameterAsString(END_DATE) + '&'; }
			getLogger().info(">>" + url + "<<");
			}
			catch(Exception e)
			{
				
			}
		//	if(!getParameterAsString(END_DATE).equals("")){url = url  + "end_date=" +  getParameterAsString(END_DATE) + '&'; }
			
			url = url + "order=" +  getParameterAsString(ORDER) + '&';
			getLogger().info(">>" + url + "<<");
			
			if(!getParameterAsString(TRANSFORM).equals("none")){url = url + "transform=" + getParameter(TRANSFORM) + '&';}
			getLogger().info(">>" + url + "<<");
			if(!getParameterAsString(COLLAPSE).equals("none")){url = url + "collapse=" + getParameter(COLLAPSE) + '&';}
			getLogger().info(">>" + url + "<<");
			url = url + "api_key=" + APIKey;
			
			getLogger().info(">>" + url + "<<");
			
			setParameter(PARAMETER_URL, url);
			setParameter(PARAMETER_COLUMN_SEPARATORS, ",");
			setParameter(PARAMETER_DECIMAL_POINT_CHARACTER,".");
			setParameter(PARAMETER_READ_ATTRIBUTE_NAMES,"true");
		
			ExampleSet exampleset =	super.createExampleSet();

		
//			Attribute database = AttributeFactory.createAttribute("Database Code",
//					Ontology.NOMINAL);
//		 
//			Attribute dataset = AttributeFactory.createAttribute("Dataset Code",
//					Ontology.NOMINAL);
//			
//			Attribute newdate = AttributeFactory.createAttribute("NewDate",Ontology.DATE);
//			
//			Attribute olddate = exampleset.getAttributes().get("Date");
//			exampleset.getAttributes().addRegular(database);
//			exampleset.getAttributes().addRegular(dataset);
//			
//		
//			String datasetcode = getParameterAsString(DATASET_CODE);
//			String databasecode = getParameterAsString(DATABASE_CODE);
//			
//			try {
//			ExampleSet exampleset2 = 	ExampleSets.from((List<Attribute>) exampleset.getAttributes())
//				.withColumnFiller(dataset,i->dataset.getMapping().mapString(datasetcode))
//				.withColumnFiller(database,i->database.getMapping().mapString(databasecode))
//				.build();
//			 return exampleset2;
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			
			return exampleset; 
		 
			
		} catch (ConfigurationException e) {
				getLogger().info(e.getMessage());
			e.printStackTrace();
		}

		
		
		
		return null;
	}

	@Override
	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();
		for(ParameterType type2 : parameterTypes)
		{
			type2.setHidden(true);
			type2.setOptional(true);
		 	
		}
		
	//	getParameterType(PARAMETER_URL).setOptional(true);
		
		ParameterType type = new ParameterTypeConfigurable(PARAMETER_CONFIG,
				"Choose a QUANDL connection", "QUANDL");
		type.setOptional(false);
		
		
		parameterTypes.add(type);
		
		
		type = new ParameterTypeString(DATABASE_CODE, "Code identifying the database to which the dataset belongs.",false,false);
		parameterTypes.add(type);
		
		type = new ParameterTypeString(DATASET_CODE, "Code identifying the dataset.",false,false);
		parameterTypes.add(type);
		
		type = new ParameterTypeInt(LIMIT, "Use limit=n to get the first n rows of the dataset. Use limit=1 to get just the latest row",1,10000);
		type.setOptional(true);
		type.setExpert(false);
		parameterTypes.add(type);
		
	//	type = new ParameterTypeInt(COLUMN_INDEX, "	Request a specific column. Column 0 is the date column and is always returned. Data begins at column 1.",1,50);
	//	type.setOptional(true);
	//	type.setExpert(false);
	//	parameterTypes.add(type);
		
		type = new ParameterTypeString(START_DATE, "Retrieve data rows on and after the specified start date.Please use format yyyy-mm-dd",true,true);
		type.setOptional(true);
		type.setExpert(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeString(END_DATE, "Retrieve data rows up to and including the specified end date.Please use format yyyy-mm-dd",true,true);
		type.setOptional(true);
		type.setExpert(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(ORDER, "Return data in ascending or descending order of date. Default is desc.",new String[]{"asc","desc"},1,true);
		type.setOptional(true);
		type.setExpert(false);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(COLLAPSE, "Change the sampling frequency of the returned data. Default is none; i.e., data is returned in its original granularity.",new String[]{"none","daily","weekly","monthly","quarterly","annual"},0,true);
		type.setOptional(true);
		type.setExpert(false);
		parameterTypes.add(type);
		
			type = new ParameterTypeCategory(TRANSFORM, "Perform elementary calculations on the data prior to downloading. Default is none. .",new String[]{"none","diff","rdiff","rdiff_from","cumul","normalize"},0,true);
			type.setOptional(true);
		parameterTypes.add(type);
		
		return parameterTypes;
	}
	
}