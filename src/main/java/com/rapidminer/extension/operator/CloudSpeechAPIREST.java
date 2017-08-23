package com.rapidminer.extension.operator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.protobuf.ByteString;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.utils.ExampleSetBuilder;
import com.rapidminer.example.utils.ExampleSets;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.ports.OutputPort;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.Ontology;

/**
 * @author Daniel Grzech
 */
public class CloudSpeechAPIREST extends Operator {
	/**
	 * Logger instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(CloudSpeechAPIREST.class.getName());

	/**
	 * API Key for Google Cloud Platform project.
	 */
	private static final String API_KEY = "API key";
	/**
	 * Type of request--local or remote--corresponding to the Google Cloud
	 * Speech service recognize or longrunningrecognize methods.
	 */
	private static final String REQUEST_TYPE = "request type";
	/**
	 * Path to audio file to be transcribed.
	 */
	private static final String PATH_TO_AUDIO_FILE = "audio file";
	/**
	 * URI to Cloud Storage Bucket with the audio file to be transcribed.
	 */
	private static final String REMOTE_URI = "Cloud Storage Bucket URI";
	/**
	 * Sample rate in Hz of the file to be transcribed.
	 */
	private static final String SAMPLE_RATE = "sample rate (Hz)";
	/**
	 * Language code of the language used in the audio file to be transcribed.
	 */
	private static final String LANGUAGE = "language code";
	/**
	 * Whether to apply a profanity filter in the transcription.
	 */
	private static final String PROFANITY_FILTER = "profanity filter";

	private final OutputPort outputExampleSetPort = getOutputPorts().createPort("example set");

	@Resource
	private WebServiceContext context;

	public CloudSpeechAPIREST(OperatorDescription description) throws UndefinedParameterError {
		super(description);
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		ParameterTypeString key = new ParameterTypeString(API_KEY, "Google Cloud Platform API key.", false, false);

		String[] types = { "local", "remote" };
		ParameterTypeCategory requestType = new ParameterTypeCategory(REQUEST_TYPE,
				"Whether to transcribe a local file or a file stored on GCS.", types, 0);
		requestType.setOptional(false);
		requestType.setExpert(false);

		String[] audioFileExtensions = { "mp3", "wav", "flac" };
		ParameterTypeFile audioFile = new ParameterTypeFile(PATH_TO_AUDIO_FILE, "Audio file to process.", true,
				audioFileExtensions);
		audioFile.setExpert(false);
		int[] index = { 0 };
		audioFile.registerDependencyCondition(new EqualTypeCondition(this, requestType.getKey(), types, false, index));

		ParameterTypeString remoteURL = new ParameterTypeString(REMOTE_URI,
				"URI that points to a file which contains audio. Only Google Cloud Storage URIs are supported.", true,
				false);
		int[] activationIndex = { 1 };
		remoteURL.registerDependencyCondition(
				new EqualTypeCondition(this, requestType.getKey(), types, false, activationIndex));

		String[] encodings = { "FLAC", "LINEAR16", "MULAW", "AMR", "AMR_WB", "OGG_OPUS", "SPEEX_WITH_HEADER_BYTE" };
		ParameterTypeCategory encoding = new ParameterTypeCategory("encoding",
				"Specifies the encoding scheme of the supplied audio (of type AudioEncoding). If you have a choice in codec, prefer a lossless encoding such as FLAC or LINEAR16 for best performance.",
				encodings, 0);
		encoding.setOptional(false);
		encoding.setExpert(false);

		ParameterTypeInt sampleRateHertz = new ParameterTypeInt(SAMPLE_RATE,
				"Specifies the sample rate in Hz of the supplied audio. The sampleRateHertz field is optional for FLAC and WAV files where the sample rate is included in the file header.",
				0, Integer.MAX_VALUE);

		String[] languages = { "en-US", "fr-FR", "de-DE", "es-ES" };
		ParameterTypeStringCategory languageCode = new ParameterTypeStringCategory(LANGUAGE,
				"Contains the language and region/locale to use for speech recognition of the supplied audio. The language code must be a BCP-47 identifier. Note that language codes typically consist of primary language tags and secondary region subtags to indicate dialects.",
				languages);

		ParameterTypeBoolean profanityFilter = new ParameterTypeBoolean(PROFANITY_FILTER,
				"Indicates whether to filter out profane words or phrases. Words filtered out will contain their first letter and asterisks for the remaining characters (e.g. f***). The profanity filter operates on single words, it does not detect abusive or offensive speech that is a phrase or a combination of words.",
				false, false);

		List<ParameterType> parameters = new ArrayList<ParameterType>();
		parameters.add(key);
		parameters.add(requestType);
		parameters.add(audioFile);
		parameters.add(remoteURL);
		parameters.add(encoding);
		parameters.add(sampleRateHertz);
		parameters.add(languageCode);
		parameters.add(profanityFilter);

		return parameters;
	}

	/**
	 * Sends a POST request to Google Cloud Speech API in order to call the
	 * recognize method.
	 *
	 * @param key
	 *            API key to use.
	 * @param requestBody
	 *            JSON with the body of the request to send.
	 */
	private void recognize(String key, JSONObject requestBody) throws Exception {
		String tempURL = "https://speech.googleapis.com/v1/speech:recognize?key=" + key;

		URL url = new URL(tempURL);

		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/json");

		OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
		LOGGER.log(Level.FINEST, requestBody.toString());
		wr.write(requestBody.toString());
		wr.flush();
		wr.close();
		httpURLConnection.connect();

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
		String output = "";

		while ((output = br.readLine()) != null) {
			sb.append(output);
		}

		JSONObject response = new JSONObject(sb.toString());
		LOGGER.log(Level.FINEST, response.toString());
		parseResultRecognize(response);

	}

	/**
	 * Parses the JSON with the response of the Google Cloud Speech recognize
	 * method to an ExampleSet delivered at the output port of the operator,
	 * containing the transcription and the confidence.
	 *
	 * @param response
	 *            Response of the Google Cloud Speech service to parse.
	 */
	private void parseResultRecognize(JSONObject response) {
		JSONArray results = response.getJSONArray("results");
		JSONArray alternatives = results.getJSONObject(0).getJSONArray("alternatives");

		for (int i = 0; i < alternatives.length(); i++) {
			double confidence = alternatives.getJSONObject(i).getDouble("confidence");
			String transcript = alternatives.getJSONObject(i).getString("transcript");

			Attribute confidenceAttribute = AttributeFactory.createAttribute("confidence", Ontology.STRING);
			Attribute transcriptStringAttribute = AttributeFactory.createAttribute("transcript", Ontology.STRING);

			ExampleSetBuilder exampleSetBuilder = ExampleSets.from(confidenceAttribute, transcriptStringAttribute);

			double[] values = new double[3];
			values[0] = confidenceAttribute.getMapping().mapString(Double.toString(confidence));
			values[1] = transcriptStringAttribute.getMapping().mapString(transcript);

			exampleSetBuilder.addDataRow(new DoubleArrayDataRow(values));
			ExampleSet exampleSet = exampleSetBuilder.build();

			outputExampleSetPort.deliver(exampleSet);
		}

	}

	/**
	 * Checks the JSON {@link response} to get the status of the associated
	 * operation.
	 * 
	 * @param response
	 *            Response of the Google Cloud Platform server with the status
	 *            of the operation.
	 * @returns Boolean which indicates whether the operation has finished.
	 */
	private boolean isDone(JSONObject response) {
		boolean isDone;

		try {
			isDone = response.getBoolean("done");
		} catch (org.json.JSONException e) {
			isDone = false;
		}

		return isDone;
	}

	/**
	 * Sends a POST request to Google Cloud Speech API in order to call the
	 * longrunningrecognize method.
	 *
	 * @param key
	 *            API key to use.
	 * @param requestBody
	 *            JSON with the body of the request to send.
	 */
	private void longRunningRecognize(String key, JSONObject requestBody) throws Exception {
		String tempURL = "https://speech.googleapis.com/v1/speech:longrunningrecognize?key=" + key;

		URL url = new URL(tempURL);

		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setDoOutput(true);
		httpURLConnection.setDoInput(true);
		httpURLConnection.setRequestMethod("POST");
		httpURLConnection.setRequestProperty("Content-Type", "application/json");

		OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
		LOGGER.log(Level.FINEST, requestBody.toString());
		wr.write(requestBody.toString());
		wr.flush();
		wr.close();
		httpURLConnection.connect();

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
		String output = "";

		while ((output = br.readLine()) != null) {
			sb.append(output);
		}

		JSONObject operation = new JSONObject(sb.toString());
		boolean isDone = isDone(operation);

		while (!isDone) {
			Thread.sleep(10000);

			operation = queryOperation(operation.getString("name"), key);
			isDone = isDone(operation);
		}

		JSONObject response = operation;
		parseResultLongRunningRecognize(response);

	}

	/**
	 * Parses the JSON with the response of the Google Cloud Speech
	 * longrunningrecognize method to an ExampleSet delivered at the output port
	 * of the operator, containing the transcription and the confidence.
	 *
	 * @param response
	 *            Response of the Google Cloud Speech service to parse.
	 */
	private void parseResultLongRunningRecognize(JSONObject response) {
		JSONObject responseJSON = response.getJSONObject("response");
		JSONArray results = responseJSON.getJSONArray("results");
		JSONArray alternatives = results.getJSONObject(0).getJSONArray("alternatives");

		for (int i = 0; i < alternatives.length(); i++) {
			double confidence = alternatives.getJSONObject(i).getDouble("confidence");
			String transcript = alternatives.getJSONObject(i).getString("transcript");

			Attribute confidenceAttribute = AttributeFactory.createAttribute("confidence", Ontology.STRING);
			Attribute transcriptStringAttribute = AttributeFactory.createAttribute("transcript", Ontology.STRING);

			ExampleSetBuilder exampleSetBuilder = ExampleSets.from(confidenceAttribute, transcriptStringAttribute);

			double[] values = new double[3];
			values[0] = confidenceAttribute.getMapping().mapString(Double.toString(confidence));
			values[1] = transcriptStringAttribute.getMapping().mapString(transcript);

			exampleSetBuilder.addDataRow(new DoubleArrayDataRow(values));
			ExampleSet exampleSet = exampleSetBuilder.build();

			outputExampleSetPort.deliver(exampleSet);
		}

	}

	/**
	 * Sends a GET request to Google Cloud Speech API in order to query the
	 * status of the operation {@link operationName}.
	 *
	 * @param operationName
	 *            Name of the operation whose status to query.
	 * @param key
	 *            API key to use in the query.
	 * 
	 * @returns JSONObject JSON string with the response of the server.
	 */
	private JSONObject queryOperation(String operationName, String key) throws IOException, InterruptedException {
		HttpURLConnection connection = (HttpURLConnection) new URL(
				"https://speech.googleapis.com/v1/operations/" + operationName + "?key=" + key).openConnection();
		connection.setRequestMethod("GET");
		connection.connect();

		StringBuilder sb = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String output = "";

		while ((output = br.readLine()) != null) {
			sb.append(output);
		}

		JSONObject response = new JSONObject(sb.toString());
		return response;
	}

	@Override
	public void doWork() throws OperatorException {
		JSONObject configJSON;
		JSONObject audioJSON;
		JSONObject requestBody;

		String fileName;
		String remoteURL;
		String key = this.getParameterAsString("API key");
		String language = this.getParameterAsString("language code");
		String encoding = this.getParameterAsString("encoding");
		int sampleRateHertz = this.getParameterAsInt("sample rate (Hz)");
		boolean profanityFilter = this.getParameterAsBoolean("profanity filter");

		configJSON = new JSONObject().put("encoding", encoding).put("sampleRateHertz", sampleRateHertz)
				.put("languageCode", language).put("profanityFilter", profanityFilter);

		if (this.getParameterAsString("request type").equals("local")) {
			try {
				fileName = this.getParameterAsString("audio file");
				java.nio.file.Path path = Paths.get(fileName);
				byte[] data = Files.readAllBytes(path);
				byte[] dataBase64 = Base64.encodeBase64(data);
				ByteString audioBytes = ByteString.copyFrom(dataBase64);

				audioJSON = new JSONObject().put("content", audioBytes.toStringUtf8());

				requestBody = new JSONObject().put("config", configJSON).put("audio", audioJSON);

				recognize(key, requestBody);

			} catch (Exception e) {
				e.printStackTrace();
			}

		} else {
			try {

				remoteURL = this.getParameterAsString("Cloud Storage Bucket URI");
				audioJSON = new JSONObject().put("uri", remoteURL);

				requestBody = new JSONObject().put("config", configJSON).put("audio", audioJSON);

				longRunningRecognize(key, requestBody);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}