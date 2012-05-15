/*
jWeather(TM) is a Java library for parsing raw weather data
Copyright (C) 2004 David Castro

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

For more information, please email arimus@users.sourceforge.net
 */
package net.sf.jweather.metar;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * Responsible for downloading the METAR reports
 * 
 * <p>
 * <code>
 * String metarData = MetarFetcher.fetch("KLAX");<br>
 * </code>
 * </p>
 * 
 * @author David Castro, dcastro@apu.edu
 * @version $Revision: 1.4 $
 * @see <a href="Metar.html">Metar</a>
 */
public class MetarFetcher {
	private static Logger log = null;
	private static String metarData = null;

	final static String httpMetarURL = "http://weather.noaa.gov/pub/data/observations/metar/stations/";
	// final static String httpMetarHostname = "weather.noaa.gov";
	// final static int httpMetarPort = 80;
	// final static String httpMetarPath =
	// "/pub/data/observations/metar/stations/";

	static {
		log = Logger.getLogger("net.sf.jweather");
		log.debug("MetarFetcher: instantiated");
	}

	public static String fetch(String station) {
		return fetch(station, 0);
	}

	public static String fetch(String station, int timeout) {
		metarData = null;

		// create the http client
		HttpClient client = new DefaultHttpClient();

		// set the timeout is specified
		if (timeout != 0) {
			log.debug("MetarFetch: setting timeout to '" + timeout
					+ "' milliseconds");
			long start = System.currentTimeMillis();
			// FIXME client.setConnectionTimeout(timeout);
			long end = System.currentTimeMillis();
			if (end - start < timeout) {
				// FIXME client.setTimeout((int) (end - start));
			} else {
				return null;
			}
		}

		// create the http method we will use
		HttpGet method = new HttpGet(httpMetarURL + station + ".TXT");
		// connect to the NOAA site, retrying up to the specified num
		HttpResponse statusCode = null;
		// for (int attempt = 0; statusCode == -1 && attempt < 3; attempt++) {
		try {
			// execute the get method
			log.debug("MetarFetcher: downloading data for station '" + station
					+ "'");
			statusCode = client.execute(method);
		} catch (ClientProtocolException e) {
			log.error("a recoverable exception occurred, " + "retrying."
					+ e.getMessage());
		} catch (IOException e) {
			throw new RuntimeException("failed to download file", e);
		}
		// }

		// check that we didn't run out of retries
		if (statusCode.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			log.error("failed to download station data for '" + station + "'");
			return null;
		} else {
			HttpEntity entity = statusCode.getEntity();
			try {
				metarData = EntityUtils.toString(entity) + "\n";
			} catch (ParseException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// release the connection
			method.releaseConnection();

			log.debug("MetarFetcher: metar data: " + metarData);
		}

		return metarData;
	}
}
