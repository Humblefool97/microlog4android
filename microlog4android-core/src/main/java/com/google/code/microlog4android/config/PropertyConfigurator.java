/**
 * 
 */
package com.google.code.microlog4android.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;

import com.google.code.microlog4android.Appender;
import com.google.code.microlog4android.Level;
import com.google.code.microlog4android.Logger;

/**
 * 
 * @author Johan Karlsson
 */
public class PropertyConfigurator {

	public static String DEFAULT_PROPERTIES_FILENAME = "microlog.properties";

	/**
	 * The key for setting the root logger.
	 */
	public static final String ROOT_LOGGER_KEY = "microlog.rootLogger";

	/**
	 * The key for setting the logger.
	 */
	public static final String LOGGER_PREFIX_KEY = "microlog.logger";

	/**
	 * The key for setting the formatter.
	 */
	public static final String FORMATTER_PREFIX_KEY = "microlog.formatter";

	/**
	 * The key for setting the appender.
	 */
	public static final String APPENDER_PREFIX_KEY = "microlog.appender";

	/**
	 * The key for setting the level.
	 */
	public static final String LOG_LEVEL_PREFIX_KEY = "microlog.level";

	public static final String[] APPENDER_ALIASES = { "ConsoleAppender",
			"FileAppender" };

	public static final String[] APPENDER_CLASS_NAMES = {
			"com.google.code.microlog4android.appender.ConsoleAppender",
			"com.google.code.microlog4android.appender.FileAppender" };

	private static final HashMap<String, String> appenderAliases = new HashMap<String, String>(
			43);

	private Context context;

	private LoggerRepository loggerRepository;

	// Static initializer
	{
		for (int index = 0; index < APPENDER_ALIASES.length; index++) {
			appenderAliases.put(APPENDER_ALIASES[index],
					APPENDER_CLASS_NAMES[index]);
		}
	};

	private PropertyConfigurator(Context context) {
		this.context = context;
		loggerRepository = DefaultLoggerRepository.getInstance();
	}

	/**
	 * Create a configurator for the specified context.
	 * 
	 * @param context
	 *            the {@link Context} to get the configurator for.
	 * @return a configurator
	 */
	public static PropertyConfigurator getConfigurator(Context context) {
		if (context == null) {
			throw new IllegalArgumentException("The context must not be null");
		}

		return new PropertyConfigurator(context);
	}

	/**
	 * Configure using the
	 */
	public void configure() {
		configure(DEFAULT_PROPERTIES_FILENAME);
	}

	public void configure(String filename) {
		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
			InputStream inputStream = assetManager.open(filename);
			Properties properties = loadProperties(inputStream);
			startConfiguration(properties);
		} catch (IOException e) {
			System.err
					.println("Failed to open the microlog properties file. Hint: the file should be in the /assets directory "
							+ filename + " " + e);
		}
	}

	public void configure(int resId) {
		Resources resources = context.getResources();

		try {
			InputStream rawResource = resources.openRawResource(resId);
			Properties properties = loadProperties(rawResource);
			startConfiguration(properties);
		} catch (NotFoundException e) {
			System.err
					.println("Did not find the microlog properties resource. Hint: this should be in the /res/raw directory "
							+ e);
		} catch (IOException e) {
			System.err
					.println("Failed to read the microlog properties resource."
							+ e);
		}
	}

	/**
	 * Load the properties
	 * 
	 * @param inputStream
	 *            the {@link InputStream} to read from
	 * @return the {@link Properties} object containing the properties read from
	 *         the {@link InputStream}
	 * @throws IOException
	 *             if the loading fails.
	 */
	private Properties loadProperties(InputStream inputStream)
			throws IOException {
		Properties properties = new Properties();
		properties.load(inputStream);
		return properties;
	}

	/**
	 * Start the configuration
	 * 
	 * @param properties
	 */
	private void startConfiguration(Properties properties) {
		System.out.println("startConfiguration()");

		if (properties.containsKey(PropertyConfigurator.ROOT_LOGGER_KEY)) {
			System.out.println("Modern configuration, not yet supported");
		} else {
			configureSimpleStyle(properties);
		}
	}

	private void configureSimpleStyle(Properties properties) {

		setLevel(properties);

		String appenderString = properties.getProperty(
				PropertyConfigurator.APPENDER_PREFIX_KEY, "ConsoleAppender");
		List<String> appenderList = parseAppenderString(appenderString);
		setAppenders(appenderList);

		setFormatter(properties);
	}

	private void setLevel(Properties properties) {
		String levelString = (String) properties
				.get(PropertyConfigurator.LOG_LEVEL_PREFIX_KEY);
		Level level = stringToLevel(levelString);

		if (level != null) {
			loggerRepository.getRootLogger().setLevel(level);
			System.out.println("Root level: "
					+ loggerRepository.getRootLogger().getLevel());
		}

	}

	private List<String> parseAppenderString(String appenderString) {
		StringTokenizer tokenizer = new StringTokenizer(appenderString, ";,");
		List<String> appenderList = new ArrayList<String>();

		while (tokenizer.hasMoreElements()) {
			String appender = (String) tokenizer.nextElement();
			appenderList.add(appender);
		}

		return appenderList;
	}

	private void setAppenders(List<String> appenderList) {
		for (String string : appenderList) {
			addAppender(string);
		}
	}

	private void addAppender(String string) {
		
		Logger rootLogger = loggerRepository.getRootLogger();
		String className = appenderAliases.get(string);

		if (className == null) {
			className = string;
		}

		try {
			Class appenderClass = Class.forName(className);
			Appender appender = (Appender) appenderClass.newInstance();

			if (appender != null) {
				System.out.println("Adding appender "+appender.getClass().getName());
				rootLogger.addAppender(appender);
			}

		} catch (ClassNotFoundException e) {
			System.err.println("Failed to find appender class: "+e);
		} catch (IllegalAccessException e) {
			System.err.println("No access to appender class: "+e);
		} catch (InstantiationException e) {
			System.err.println("Failed to instantiate appender class: "+e);
		}
	}

	private void setFormatter(Properties properties) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Convert a <code>String</code> containing a level to a <code>Level</code>
	 * object.
	 * 
	 * @return the level that corresponds to the levelString if it was a valid
	 *         <code>String</code>, <code>null</code> otherwise.
	 */
	private Level stringToLevel(String levelString) {
		return Level.valueOf(levelString);
	}

}
