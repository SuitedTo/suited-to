/** 
* Copyright 2011 The Apache Software Foundation
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
* 
* @author Felipe Oliveira (http://mashup.fm)
* 
*/

package net.oauth;

import models.OAuthConsumer;
import models.OAuthServiceProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A pool of OAuthConsumers that are constructed from Properties. Each consumer
 * has a name, which is a property of the OAuthConsumer. Other properties come
 * from Properties whose names are prefixed with the consumer's name. For
 * example, a consumer's credentials come from properties named
 * [name].consumerKey and [name].consumerSecret.
 * 
 * @author John Kristian
 */
public class ConsumerProperties {

	/**
	 * Gets the resource.
	 *
	 * @param name the name
	 * @param loader the loader
	 * @return the resource
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static URL getResource(String name, ClassLoader loader)
			throws IOException {
		URL resource = loader.getResource(name);
		if (resource == null) {
			throw new IOException("resource not found: " + name);
		}
		return resource;
	}

	/**
	 * Gets the properties.
	 *
	 * @param source the source
	 * @return the properties
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static Properties getProperties(URL source) throws IOException {
		InputStream input = source.openStream();
		try {
			Properties p = new Properties();
			p.load(input);
			return p;
		} finally {
			input.close();
		}
	}

	/**
	 * Instantiates a new consumer properties.
	 *
	 * @param resourceName the resource name
	 * @param loader the loader
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ConsumerProperties(String resourceName, ClassLoader loader)
			throws IOException {
		this(getProperties(getResource(resourceName, loader)));
	}

	/**
	 * Instantiates a new consumer properties.
	 *
	 * @param consumerProperties the consumer properties
	 */
	public ConsumerProperties(Properties consumerProperties) {
		this.consumerProperties = consumerProperties;
	}

	/** The consumer properties. */
	private final Properties consumerProperties;

	/** The pool. */
	private final Map<String, OAuthConsumer> pool = new HashMap<String, OAuthConsumer>();

	/** Get the consumer with the given name. */
	public OAuthConsumer getConsumer(String name) throws MalformedURLException {
		OAuthConsumer consumer;
		synchronized (pool) {
			consumer = pool.get(name);
		}
		if (consumer == null) {
			consumer = newConsumer(name);
		}
		synchronized (pool) {
			OAuthConsumer first = pool.get(name);
			if (first == null) {
				pool.put(name, consumer);
			} else {
				/*
				 * Another thread just constructed an identical OAuthConsumer.
				 * Use that one (and discard the one we just constructed).
				 */
				consumer = first;
			}
		}
		return consumer;
	}

	/**
	 * New consumer.
	 *
	 * @param name the name
	 * @return the o auth consumer
	 * @throws MalformedURLException the malformed url exception
	 */
	protected OAuthConsumer newConsumer(String name)
			throws MalformedURLException {
		String base = consumerProperties.getProperty(name
				+ ".serviceProvider.baseURL");
		URL baseURL = (base == null) ? null : new URL(base);
		OAuthServiceProvider serviceProvider = new OAuthServiceProvider(getURL(
				baseURL, name + ".serviceProvider.requestTokenURL"), getURL(
				baseURL, name + ".serviceProvider.userAuthorizationURL"),
				getURL(baseURL, name + ".serviceProvider.accessTokenURL"));
		OAuthConsumer consumer = new OAuthConsumer(
				consumerProperties.getProperty(name + ".callbackURL"),
				consumerProperties.getProperty(name + ".consumerKey"),
				consumerProperties.getProperty(name + ".consumerSecret"),
				serviceProvider);
		consumer.name = name;
		// if (baseURL != null) {
		// consumer.setProperty("serviceProvider.baseURL", baseURL);
		// }
		for (Map.Entry prop : consumerProperties.entrySet()) {
			String propName = (String) prop.getKey();
			if (propName.startsWith(name + ".consumer.")) {
				String c = propName.substring(name.length() + 10);
				// consumer.setProperty(c, prop.getValue());
			}
		}
		return consumer;
	}

	/**
	 * Gets the uRL.
	 *
	 * @param base the base
	 * @param name the name
	 * @return the uRL
	 * @throws MalformedURLException the malformed url exception
	 */
	private String getURL(URL base, String name) throws MalformedURLException {
		String url = consumerProperties.getProperty(name);
		if (base != null) {
			url = (new URL(base, url)).toExternalForm();
		}
		return url;
	}

}
