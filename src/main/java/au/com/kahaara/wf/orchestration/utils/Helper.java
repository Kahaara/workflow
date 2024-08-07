package au.com.kahaara.wf.orchestration.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Static helper class for generic helper style methods
 *
 * TODO: Move string functions to StringUtil class
 *
 * @author excdsn
 *
 */
public class Helper {

	/**
	 * What is the array position for a range value for the minimum range
	 */
	public static final int RANGEMINPOS=0;
	/**
	 * What is the array position for a range value for the maximum range
	 */
	public static final int RANGEMAXPOS=1;
	//private static final int DEFAULT_DPI = 500; // Most finger print scanners operate at 500 dpi

	/**private static Long currentSubjectGallerySize = 0L;
	private static Long currentActiveSubjectGallerySize = 0L;
	private static Long currentImageGallerySize = 0L;
	private static Long currentEnrolledImageGallerySize = 0L;
	private static Long currentActiveImageGallerySize = 0L;*/

	private static final XmlMapper xmlMapper = (XmlMapper) new XmlMapper()
			.registerModule(new JavaTimeModule())
			.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false)
			.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS,false)
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false)
			.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

	private Helper() {
		// No construction of object
	}

	/**
	 * Quick helper method to set a default if no value
	 *
	 * @param setterMethod The setter method to use
	 * @param value The value to set if present
	 * @param defaultVal The default if the value is null
	 */
	public static <T> void defaultIfEmpty(Consumer<T> setterMethod, T value, T defaultVal) {
	    if (value != null){
	        setterMethod.accept(value);
	    } else if (defaultVal != null) {
	        setterMethod.accept(defaultVal);
	    }
	}

	/**
	 * Is the string a number
	 *
	 * @param string the string to test
	 * @return true if a number
	 */
	public static boolean isNumber(String string) {
	     return string.matches("^\\d+$");
	 }



	public static int parseInteger(String s) {
		return Integer.decode(s);
	}
	public static float parseFloat(String s) {
		return Float.parseFloat(s);
	}
	public static double parseDouble(String s) {
		return Double.parseDouble(s);
	}

	/**
	 * Check to see if a value is null or zero length string
	 *
	 * @param value The string to safely test
	 *
	 * @return true when string is null or empty otherwise return false
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * compare a string for null or empty value
	 *
	 * @param str string to compare
	 *
	 * @return true when string not null or empty otherwise return false
	 */
	public static boolean isNotEmpty(String str)
	{
		String emptyStr= "";
		return str != null && !str.equals(emptyStr);

	}

	/**
	 * Checks if a String is whitespace, empty ("") or null.</p>
     *
	 * @param str String to test
	 *
	 * @return true when string null, empty or just whitespace otherwise return false
	 */
	public static boolean isStringBlank(String str) {
	    int strLen;
	    if (str == null || (strLen = str.length()) == 0) {
	        return true;
	    }
	    for (int i = 0; i < strLen; i++) {
	        if (!Character.isWhitespace(str.charAt(i))) {
	            return false;
	        }
	    }
	    return true;
	}

	/**
	 * Checks that a String is not whitespace only, not empty ("") and not null.</p>
	 *
	 *  Helper.isNotBlank(null)      = false
	 *  Helper.isNotBlank("")        = false
	 *  Helper.isNotBlank(" ")       = false
	 *  Helper.isNotBlank("bob")     = true
	 *  Helper.isNotBlank("  bob  ") = true
	 *
	 * @param str String to test
	 *
	 * @return true when string not null, not empty and not just whitespace otherwise return false
	 */
	public static boolean isStringNotBlank(String str) {
		return !isStringBlank(str);
	}

	/**
	 * @param s The string to camel case
	 * @return new string
	 */
	public static String toCamelCase(String s) {
		String[] parts = s.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String part : parts) {
			if (part != null && part.trim().length() > 0)
				sb.append(toProperCase(part));
			else
				sb.append(part).append(" ");
		}
		return sb.toString();
	}

	/**
	 * @param s The string to set to proper case
	 * @return new string
	 */
	public static String toProperCase(String s) {
		String temp = s.trim();
		String spaces = "";
		if (temp.length() != s.length()) {
			int startCharIndex = s.charAt(temp.indexOf(0));
			spaces = s.substring(0, startCharIndex);
		}
		temp = temp.substring(0, 1).toUpperCase() + spaces
				+ temp.substring(1).toLowerCase() + " ";
		return temp;

	}

	/**
	 * Simple method to only update when a value is present.
	 *
	 * @param setterMethod The setter moethod to use
	 * @param value the value to use in the setter
	 */
	public static <T> void updateValue(Consumer<T> setterMethod, T value) {
	    if (value != null){
	        setterMethod.accept(value);
	    }
	}

	/**
	 * Return a Float value if the BigDecimal is not null
	 *
	 * @param value The BigDecimal value
	 * @return A new float value
	 */
	public static Float setFloatFromBigDecimal(BigDecimal value) {
		if (value != null) {
			return value.floatValue();
		}
		return null;
	}

	/**
	 * Return a Double value if the BigDecimal is not null
	 *
	 * @param value The BigDecimal value
	 * @return A new Double value
	 */
	public static Double setDoubleFromBigDecimal(BigDecimal value) {
		if (value != null) {
			return value.doubleValue();
		}
		return null;
	}

	/**
	 * Return an Integer value if the BigDecimal is not null
	 *
	 * @param value The BigDecimal value
	 * @return A new Integer value
	 */
	public static Integer setIntegerFromBigDecimal(BigDecimal value) {
		if (value != null) {
			return value.intValue();
		}
		return null;
	}

	public static void closeResource(Closeable item, Logger log ) {
	    try {
	        if ( item != null ) {
	            item.close();
	        }
	    }
	    catch ( Exception e ) {
	        log.error("Unable to close resource {}",e.getMessage());
	    }
	}

	/**
	 * Get a file from a file resources and return as string
	 * @param path The URI
	 * @param log The logging descriptor
	 * @return The String content of the file
	 */
	public static String readUriToString(String path, Logger log) {
		// Find the file in the path

		String str = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;

		try {
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
			if (inputStream == null ) {
				inputStream = new FileInputStream(path);
			}
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader =new BufferedReader(inputStreamReader);
			str = bufferedReader.lines().collect(Collectors.joining("\n"));

		} catch (Exception e) {
			log.error("File resource %s not found as file. {} {}",path,e.getMessage());
		} finally {
			Helper.closeResource(bufferedReader, log);
			Helper.closeResource(inputStreamReader, log);
			Helper.closeResource(inputStream, log);
		}

		// Readers are required to be reset to null before being reused
		inputStreamReader = null;
		bufferedReader = null;

		if (inputStream != null && str == null) {

			try {
				inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader =new BufferedReader(inputStreamReader);
				str = bufferedReader.lines().collect(Collectors.joining("\n"));

			} catch (Exception e) {
				log.error("Resource %s not found at uri. {} {}",path,e.getMessage());
			} finally {
				Helper.closeResource(bufferedReader, log);
				Helper.closeResource(inputStreamReader, log);
				Helper.closeResource(inputStream, log);
			}
		}

		return str;
	}

	public static <T> String convertToXml(T object) throws IOException {
		String xml;

		try {
			xml = xmlMapper.writeValueAsString(object);
		} catch (Exception e) {
			throw new IOException(e);
		}

	    return xml;
	}

	public static <T> Object convertFromXml(Class<T> className, String xml) throws IOException {
		T o;

		try {
			o = xmlMapper.readValue(xml, className);
		} catch (Exception e) {
			throw new IOException(e);
		}

		return o;
	}

	/**
	 * Convert object to JSON String with JACKSON.
	 * @param object class object
	 * @return String
	 */
	public static <T> String convertToJsonString (T object) throws JsonProcessingException {
    	ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(object);
	}

	/**
	 * Convert JSON String to required object class with JACKSON.
	 * @param jsonString jsonString
	 * @param valueTypeObject valueTypeObject
	 * @return <T> Object
	 */
	public static <T> Object convertJsonStringToObject (String jsonString, Class<T> valueTypeObject) throws JsonProcessingException {
    	ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jsonString, valueTypeObject);
	}

	public static <T> Object convertJsonStringToObjectWithScalarValues (String jsonString, Class<T> valueTypeObject) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
		mapper.configure(MapperFeature.ALLOW_COERCION_OF_SCALARS, false);
		return mapper.readValue(jsonString, valueTypeObject);
	}

	public static byte[] convertToBytes(Object object) throws IOException {
	    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
	         ObjectOutput out = new ObjectOutputStream(bos)) {
	        out.writeObject(object);
	        return bos.toByteArray();
	    }
	}

	public static Object convertFromBytes(byte[] bytes) throws IOException, ClassNotFoundException {
	    try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
	    	 ObjectInputStream  in = new ObjectInputStream(bis)) {
	        return in.readObject();
	    }
	}

	/**
	 * Given a range string then return the numeric values as a Double list
	 * of 2 values. Range can be defined as 0.89- which means any value 0.89
	 * or greater, 0.89-0.92 which means 0.89 to 0.92 inclusive, -0.85 which
	 * means any raw score less than or equal to 0.87
	 *
	 * @param min Minimum value
	 * @param max Maximum value
	 * @param rangeStr The range string
	 * @return a Double[] which may contain nulls.
	 */
	public static Double[] fromRangeString(Double min, Double max, String rangeStr) {
		String[] rangeSplit = rangeStr.split("-");
		// Default to min and max allowed
		Double [] fromTo = new Double[2];
		fromTo[0] = min;
		fromTo[1] = max;
		if (rangeSplit.length > 0 && !rangeSplit[0].isEmpty()) {
			fromTo[0] = (double) Float.parseFloat(rangeSplit[0]);
		}
		if (rangeSplit.length > 1 && !rangeSplit[1].isEmpty()) {
			fromTo[1] = (double) Float.parseFloat(rangeSplit[1]);
		}
		return fromTo;
	}

	/**
	 * Wrapper around the java util encoder using the Basic encoder
	 * @see <a href="https://docs.oracle.com/javase/8/docs/api/java/util/Base64.html#basic">Base64 baseic</a>
	 * @param binaryInput A binary byte array
	 * @return A String with no line feed (line separator)character
	 */
	public static String encodeBase64(byte[] binaryInput) {
		return Base64.getEncoder().encodeToString(binaryInput);
	}

	/**
	 * Decode a String from either a Basic or MIME type encoding to a byte array
	 * @param base64Input The string representation of the bytes
	 * @return A byte array decoded from a string
	 */
	public static byte[] decodeBase64(String base64Input) {
		if (base64Input.contains("\r\n")) {
			return Base64.getMimeDecoder().decode(base64Input);
		} else {
			return Base64.getDecoder().decode(base64Input);
		}
	}

	/**
	 * This method is called from all places where BCS needs to write records to PACE which include the terminalId
	 * which is in a different format and length to the BCS appliance Id
	 *
	 * @param port
	 * @param raceId
	 * @return terminalId
	 */
	public static String convertApplianceToTerminalId(String port, String raceId) {
		String terminalId = port + "-SG3" + raceId;
		return terminalId;
	}

	/**
	 * Determines whether the first four characters of the supplied stringToTest are the same as the first four characters in the
	 * of the supplied stringToTestAgainst. Removes any non-alphabetic characters from the strings before doing the test.
	 * @param stringToTest the string supplied to test
	 * @param stringToTestAgainst the string that we need to match with
	 * @return true if they match and false otherwise
	 */
	public static boolean isFirstFourCharactersMatch(String stringToTest, String stringToTestAgainst) {
		if (Helper.isStringBlank(stringToTest) || Helper.isStringBlank(stringToTestAgainst)) {
			return false;
		}
		stringToTest = stringToTest.trim();
		stringToTestAgainst = stringToTestAgainst.trim();

		stringToTest = stringToTest.replaceAll("[^a-zA-Z]", "");
		stringToTestAgainst = stringToTestAgainst.replaceAll("[^a-zA-Z]", "");

		if (stringToTest.length() == 0 || stringToTestAgainst.length() == 0) {
			return false;
		}

		if (stringToTestAgainst.length() <= 4) {
			return stringToTestAgainst.equalsIgnoreCase(stringToTest);
		} else if (stringToTest.length() >= 4) {
			return stringToTest.substring(0, 4).equalsIgnoreCase(
					stringToTestAgainst.substring(0, 4));
		}
		return false;
	}

}
