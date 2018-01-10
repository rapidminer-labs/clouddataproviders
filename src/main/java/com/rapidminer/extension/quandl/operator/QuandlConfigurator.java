package com.rapidminer.extension.quandl.operator;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.config.AbstractConfigurator;

public class QuandlConfigurator extends AbstractConfigurator<QuandlConfigurable> {

	@Override
	public Class<QuandlConfigurable> getConfigurableClass() {
		return QuandlConfigurable.class;
	}

	@Override
	public String getTypeId() {
		return "QUANDL";
	}

	@Override
	public String getI18NBaseKey() {

		return "quandlconfig";
	}

	@Override
	public List<ParameterType> getParameterTypes(
			ParameterHandler parameterHandler) {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(new ParameterTypeString("API_KEY", "Enter your API KEY"));
		
		return parameters;
	}

}
