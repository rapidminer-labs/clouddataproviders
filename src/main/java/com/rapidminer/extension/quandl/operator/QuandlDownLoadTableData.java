package com.rapidminer.extension.quandl.operator;
/**
 * 
 * @author BhupendraPatil
 *
 */

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.AbstractExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.config.ParameterTypeConfigurable;

public class QuandlDownLoadTableData extends AbstractExampleSource {

	private static final String PARAMETER_CONFIG = "Select Connection";

	public QuandlDownLoadTableData(OperatorDescription description) {
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
		
		
		
		
		return parameterTypes;
	}
	
}
