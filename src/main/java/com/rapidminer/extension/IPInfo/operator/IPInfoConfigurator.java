package com.rapidminer.extension.IPInfo.operator;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.parameter.ParameterHandler;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.conditions.EqualStringCondition;
import com.rapidminer.tools.config.AbstractConfigurator;

public class IPInfoConfigurator extends AbstractConfigurator<IPInfoConfigurable> {

	
	
	public static final String PARAMETER_APIKEY = "API_Key";
	public static final String TYPE_ID = "IPINFO";
	//private static final String[] PARAMETER_AUTHTYPENAMES = new String[]{"API Key","OAuth Client ID","Service Account Key"};

	@Override
	public Class<IPInfoConfigurable> getConfigurableClass() {
		return IPInfoConfigurable.class;
	}

	@Override
	public String getTypeId() {
		return TYPE_ID;
	}

	@Override
	public String getI18NBaseKey() {

		return "ipinfoconfig";
	}

	@Override
	public List<ParameterType> getParameterTypes(
			ParameterHandler parameterHandler) {
		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(new ParameterTypeString("API_KEY", "Enter your API KEY"));
		
		return parameters;
	}

}
