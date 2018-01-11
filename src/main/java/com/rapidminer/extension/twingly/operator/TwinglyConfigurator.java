package com.rapidminer.extension.twingly.operator;
/**
 * 
 * @author BhupendraPatil
 *
 */

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.config.AbstractConfigurator;

public class TwinglyConfigurator extends AbstractConfigurator<TwinglyConfigurable> {

	@Override
	public Class<TwinglyConfigurable> getConfigurableClass() {
		return TwinglyConfigurable.class;
	}

	@Override
	public String getTypeId() {
		return "TWINGLY";
	}

	@Override
	public String getI18NBaseKey() {

		return "twinglyconfig";
	}

	@Override
	public List<ParameterType> getParameterTypes(
			ParameterHandler parameterHandler) {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(new ParameterTypeString("API_KEY", "Enter your API KEY"));
		
		return parameters;
	}

}
