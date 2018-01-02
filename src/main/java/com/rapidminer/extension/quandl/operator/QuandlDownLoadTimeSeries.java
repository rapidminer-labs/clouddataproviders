package com.rapidminer.extension.quandl.operator;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

public class QuandlDownLoadTimeSeries extends AbstractExampleSource {

	private static final String PARAMETER_CONFIG = "Select Connection";
	private static final String DATABASE_CODE = "Database Code";
	private static final String DATASET_CODE = "Enter DataSet Code";
	private static final String LIMIT = "Get only first n rows";
	private static final String COLUMN_INDEX = "Specific column number";
	private static final String START_DATE = "Start Date (Format yyyy-mm-dd)";
	private static final String END_DATE = "End Date (Format yyyy-mm-dd)";
	private static final String ORDER = "Sort Order";
	private static final String COLLAPSE = "Collapse";
	private static final String TRANSFORM = "Transform";

	public QuandlDownLoadTimeSeries(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ExampleSet createExampleSet() throws OperatorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ParameterType> getParameterTypes() {

		List<ParameterType> parameterTypes = super.getParameterTypes();

		ParameterType type = new ParameterTypeConfigurable(PARAMETER_CONFIG,
				"Choose a QUANDL connection", "QUANDL");
		type.setOptional(false);
		parameterTypes.add(type);
		
		
		type = new ParameterTypeString(DATABASE_CODE, "Code identifying the database to which the dataset belongs.",false,false);
		parameterTypes.add(type);
		
		type = new ParameterTypeString(DATASET_CODE, "Code identifying the dataset.",false,false);
		parameterTypes.add(type);
		
		type = new ParameterTypeInt(LIMIT, "Use limit=n to get the first n rows of the dataset. Use limit=1 to get just the latest row.",1,100000,true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeInt(COLUMN_INDEX, "	Request a specific column. Column 0 is the date column and is always returned. Data begins at column 1.",1,20,true);
		type.setExpert(true);
		parameterTypes.add(type);
		
		type = new ParameterTypeString(START_DATE, "Retrieve data rows on and after the specified start date.Please use format yyyy-mm-dd",true,true);
		parameterTypes.add(type);
		
		type = new ParameterTypeString(END_DATE, "Retrieve data rows up to and including the specified end date.Please use format yyyy-mm-dd",true,true);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(ORDER, "Return data in ascending or descending order of date. Default is desc.",new String[]{"asc","desc"},1,true);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(COLLAPSE, "Change the sampling frequency of the returned data. Default is none; i.e., data is returned in its original granularity.",new String[]{"none","daily","weekly","monthly","quarterly","annual"},0,true);
		parameterTypes.add(type);
		
		type = new ParameterTypeCategory(TRANSFORM, "Perform elementary calculations on the data prior to downloading. Default is none. .",new String[]{"none","diff","rdiff","rdiff_from","cumul","normalize"},0,true);
		parameterTypes.add(type);
		
		return parameterTypes;
	}
	
}