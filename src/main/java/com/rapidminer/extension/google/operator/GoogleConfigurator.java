package com.rapidminer.extension.google.operator;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.tools.config.AbstractConfigurator;

public class GoogleConfigurator extends AbstractConfigurator<GoogleConfigurable> {

	public static final String PARAMETER_AUTHTYPE = "Authentication_Type";
	
	public static final String PARAMETER_APIKEY = "API_Key";
	public static final String TYPE_ID = "GOOGLE";
	//private static final String[] PARAMETER_AUTHTYPENAMES = new String[]{"API Key","OAuth Client ID","Service Account Key"};

	@Override
	public Class<GoogleConfigurable> getConfigurableClass() {
		return GoogleConfigurable.class;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public String getI18NBaseKey() {

		return "googleconfig";
	}

	@Override
	public List<ParameterType> getParameterTypes(
			ParameterHandler parameterHandler) {
		
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		final String[] PARAMETER_AUTHTYPENAMES = new String[]{"API Key"};
		ParameterType type = new ParameterTypeCategory(PARAMETER_AUTHTYPE, "Select Authentication Type", PARAMETER_AUTHTYPENAMES,0);
		//todo add support for other authentication type
		
		
		parameters.add(type);
		
		type = new ParameterTypeString(PARAMETER_APIKEY, "Enter your API KEY");
		
		type.registerDependencyCondition(new EqualStringCondition(parameterHandler, PARAMETER_AUTHTYPE, true, PARAMETER_AUTHTYPENAMES[0]));
		
		parameters.add(type);
		
		return parameters;
	}

}
