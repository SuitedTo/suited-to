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

package net.oauth.signature;

import models.OAuthAccessor;
import net.oauth.OAuth;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import play.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A pair of algorithms for computing and verifying an OAuth digital signature.
 * <p>
 * Static methods of this class implement a registry of signature methods. It's
 * pre-populated with the standard OAuth algorithms. Appliations can replace
 * them or add new ones.
 * 
 * @author John Kristian
 */
public abstract class OAuthSignatureMethod {

	/**
	 * Add a signature to the message.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public void sign(OAuthMessage message) throws OAuthException, IOException,
			URISyntaxException {
		message.addParameter(new OAuth.Parameter("oauth_signature",
				getSignature(message)));
	}

	/**
	 * Check whether the message has a valid signature.
	 * 
	 * @throws URISyntaxException
	 * 
	 * @throws OAuthProblemException
	 *             the signature is invalid
	 */
	public void validate(OAuthMessage message) throws IOException,
			OAuthException, URISyntaxException {
		message.requireParameters("oauth_signature");
		String signature = message.getSignature();
		String baseString = getBaseString(message);
		Logger.info("Signature: %s, Base String: %s", signature, baseString);
		if (!isValid(signature, baseString)) {
			OAuthProblemException problem = new OAuthProblemException(
					"signature_invalid");
			problem.setParameter("oauth_signature", signature);
			problem.setParameter("oauth_signature_base_string", baseString);
			problem.setParameter("oauth_signature_method",
                    message.getSignatureMethod());
			throw problem;
		}
	}

	/**
	 * Gets the signature.
	 *
	 * @param message the message
	 * @return the signature
	 * @throws OAuthException the o auth exception
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	protected String getSignature(OAuthMessage message) throws OAuthException,
			IOException, URISyntaxException {
		String baseString = getBaseString(message);
		String signature = getSignature(baseString);
		// Logger log = Logger.getLogger(getClass().getName());
		// if (log.isLoggable(Level.FINE)) {
		// log.fine(signature + "=getSignature(" + baseString + ")");
		// }
		return signature;
	}

	/**
	 * Initialize.
	 *
	 * @param name the name
	 * @param accessor the accessor
	 * @throws OAuthException the o auth exception
	 */
	protected void initialize(String name, OAuthAccessor accessor)
			throws OAuthException {
		String secret = accessor.consumer.consumerSecret;
		if (name.endsWith(_ACCESSOR)) {
			// This code supports the 'Accessor Secret' extensions
			// described in http://oauth.pbwiki.com/AccessorSecret
			Object accessorSecret = accessor;
			if (accessorSecret == null) {
				accessorSecret = accessor.consumer.consumerSecret;
			}
			if (accessorSecret != null) {
				secret = accessorSecret.toString();
			}
		}
		if (secret == null) {
			secret = "";
		}
		setConsumerSecret(secret);
	}

	/** The Constant _ACCESSOR. */
	public static final String _ACCESSOR = "-Accessor";

	/** Compute the signature for the given base string. */
	protected abstract String getSignature(String baseString)
			throws OAuthException;

	/** Decide whether the signature is valid. */
	protected abstract boolean isValid(String signature, String baseString)
			throws OAuthException;

	/** The consumer secret. */
	private String consumerSecret;

	/** The token secret. */
	private String tokenSecret;

	/**
	 * Gets the consumer secret.
	 *
	 * @return the consumer secret
	 */
	protected String getConsumerSecret() {
		return consumerSecret;
	}

	/**
	 * Sets the consumer secret.
	 *
	 * @param consumerSecret the new consumer secret
	 */
	protected void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	/**
	 * Gets the token secret.
	 *
	 * @return the token secret
	 */
	public String getTokenSecret() {
		return tokenSecret;
	}

	/**
	 * Sets the token secret.
	 *
	 * @param tokenSecret the new token secret
	 */
	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	/**
	 * Gets the base string.
	 *
	 * @param message the message
	 * @return the base string
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static String getBaseString(OAuthMessage message)
			throws IOException, URISyntaxException {
		List<Map.Entry<String, String>> parameters;
		String url = message.URL;
		int q = url.indexOf('?');
		if (q < 0) {
			parameters = message.getParameters();
		} else {
			// Combine the URL query string with the other parameters:
			parameters = new ArrayList<Map.Entry<String, String>>();
			parameters.addAll(OAuth.decodeForm(message.URL.substring(q + 1)));
			parameters.addAll(message.getParameters());
			url = url.substring(0, q);
		}

		return OAuth.percentEncode(message.method.toUpperCase()) + '&'
				+ OAuth.percentEncode(normalizeUrl(url)) + '&'
				+ OAuth.percentEncode(normalizeParameters(parameters));
	}

	/**
	 * Normalize url.
	 *
	 * @param url the url
	 * @return the string
	 * @throws URISyntaxException the uRI syntax exception
	 */
	protected static String normalizeUrl(String url) throws URISyntaxException {
		URI uri = new URI(url);
		Logger.info("Url: %s, Scheme: %s", uri, uri.getScheme());
		String scheme = uri.getScheme().toLowerCase();
		String authority = uri.getAuthority().toLowerCase();
		boolean dropPort = (scheme.equals("http") && uri.getPort() == 80)
				|| (scheme.equals("https") && uri.getPort() == 443);
		if (dropPort) {
			// find the last : in the authority
			int index = authority.lastIndexOf(":");
			if (index >= 0) {
				authority = authority.substring(0, index);
			}
		}
		String path = uri.getRawPath();
		if (path == null || path.length() <= 0) {
			path = "/"; // conforms to RFC 2616 section 3.2.2
		}
		// we know that there is no query and no fragment here.
		return scheme + "://" + authority + path;
	}
    /*Filter out certain parameters.  oauth_signature shouldn't be included in the signature.  The rest of the strings
    are application-specific parameters*/
    private static final List<String> filteredParameterNames = Arrays.asList("oauth_signature", "body", "version", "entityKey", "message");
    /**
	 * Normalize parameters.
	 *
	 * @param parameters the parameters
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	protected static String normalizeParameters(
			Collection<? extends Map.Entry> parameters) throws IOException {
		if (parameters == null) {
			return "";
		}
		List<ComparableParameter> p = new ArrayList<ComparableParameter>(
				parameters.size());
		for (Map.Entry parameter : parameters) {
            if (!filteredParameterNames.contains(parameter.getKey())) {
				p.add(new ComparableParameter(parameter));
			}
		}
		Collections.sort(p);
		return OAuth.formEncode(getParameters(p));
	}



	/**
	 * Determine whether the given strings contain the same sequence of
	 * characters. The implementation discourages a <a
	 * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
	 */
	public static boolean equals(String x, String y) {
		if (x == null) {
			return y == null;
		} else if (y == null) {
			return false;
		} else if (y.length() <= 0) {
			return x.length() <= 0;
		}
		char[] a = x.toCharArray();
		char[] b = y.toCharArray();
		char diff = (char) ((a.length == b.length) ? 0 : 1);
		int j = 0;
		for (int i = 0; i < a.length; ++i) {
			diff |= a[i] ^ b[j];
			j = (j + 1) % b.length;
		}
		return diff == 0;
	}

	/**
	 * Determine whether the given arrays contain the same sequence of bytes.
	 * The implementation discourages a <a
	 * href="http://codahale.com/a-lesson-in-timing-attacks/">timing attack</a>.
	 */
	public static boolean equals(byte[] a, byte[] b) {
		if (a == null) {
			return b == null;
		} else if (b == null) {
			return false;
		} else if (b.length <= 0) {
			return a.length <= 0;
		}
		byte diff = (byte) ((a.length == b.length) ? 0 : 1);
		int j = 0;
		for (int i = 0; i < a.length; ++i) {
			diff |= a[i] ^ b[j];
			j = (j + 1) % b.length;
		}
		return diff == 0;
	}

	/**
	 * Decode base64.
	 *
	 * @param s the s
	 * @return the byte[]
	 */
	public static byte[] decodeBase64(String s) {
		byte[] b;
		try {
			b = s.getBytes(BASE64_ENCODING);
		} catch (UnsupportedEncodingException e) {
			System.err.println(e + "");
			b = s.getBytes();
		}
		return BASE64.decode(b);
	}

	/**
	 * Base64 encode.
	 *
	 * @param b the b
	 * @return the string
	 */
	public static String base64Encode(byte[] b) {
		byte[] b2 = BASE64.encode(b);
		try {
			return new String(b2, BASE64_ENCODING);
		} catch (UnsupportedEncodingException e) {
			System.err.println(e + "");
		}
		return new String(b2);
	}

	/**
	 * The character encoding used for base64. Arguably US-ASCII is more
	 * accurate, but this one decodes all byte values unambiguously.
	 */
	private static final String BASE64_ENCODING = "ISO-8859-1";
	
	/** The Constant BASE64. */
	private static final Base64 BASE64 = new Base64();

	/**
	 * New signer.
	 *
	 * @param message the message
	 * @param accessor the accessor
	 * @return the o auth signature method
	 * @throws IOException Signals that an I/O exception has occurred.
	 * @throws OAuthException the o auth exception
	 */
	public static OAuthSignatureMethod newSigner(OAuthMessage message,
			OAuthAccessor accessor) throws IOException, OAuthException {
		message.requireParameters(OAuth.OAUTH_SIGNATURE_METHOD);
		OAuthSignatureMethod signer = newMethod(message.getSignatureMethod(),
				accessor);
		signer.setTokenSecret(accessor.tokenSecret);
		return signer;
	}

	/** The factory for signature methods. */
	public static OAuthSignatureMethod newMethod(String name,
			OAuthAccessor accessor) throws OAuthException {
		try {
			Class methodClass = NAME_TO_CLASS.get(name);
			if (methodClass != null) {
				OAuthSignatureMethod method = (OAuthSignatureMethod) methodClass
						.newInstance();
				method.initialize(name, accessor);
				return method;
			}
			OAuthProblemException problem = new OAuthProblemException(
					OAuth.Problems.SIGNATURE_METHOD_REJECTED);
			String acceptable = OAuth.percentEncode(NAME_TO_CLASS.keySet());
			if (acceptable.length() > 0) {
				problem.setParameter("oauth_acceptable_signature_methods",
						acceptable.toString());
			}
			throw problem;
		} catch (InstantiationException e) {
			throw new OAuthException(e);
		} catch (IllegalAccessException e) {
			throw new OAuthException(e);
		}
	}

	/**
	 * Subsequently, newMethod(name) will attempt to instantiate the given
	 * class, with no constructor parameters.
	 */
	public static void registerMethodClass(String name, Class clazz) {
		if (clazz == null) {
			unregisterMethod(name);
		} else {
			NAME_TO_CLASS.put(name, clazz);
		}
	}

	/**
	 * Subsequently, newMethod(name) will fail.
	 */
	public static void unregisterMethod(String name) {
		NAME_TO_CLASS.remove(name);
	}

	/** The Constant NAME_TO_CLASS. */
	private static final Map<String, Class> NAME_TO_CLASS = new ConcurrentHashMap<String, Class>();
	static {
		registerMethodClass("HMAC-SHA1", HMAC_SHA1.class);
		registerMethodClass("PLAINTEXT", PLAINTEXT.class);
		registerMethodClass("RSA-SHA1", RSA_SHA1.class);
		registerMethodClass("HMAC-SHA1" + _ACCESSOR, HMAC_SHA1.class);
		registerMethodClass("PLAINTEXT" + _ACCESSOR, PLAINTEXT.class);
	}

	/** An efficiently sortable wrapper around a parameter. */
	private static class ComparableParameter implements
			Comparable<ComparableParameter> {

		/**
		 * Instantiates a new comparable parameter.
		 *
		 * @param value the value
		 */
		ComparableParameter(Map.Entry value) {
			this.value = value;
			String n = toString(value.getKey());
			String v = toString(value.getValue());
			key = OAuth.percentEncode(n) + ' ' + OAuth.percentEncode(v);
			// ' ' is used because it comes before any character
			// that can appear in a percentEncoded string.
		}

		/** The value. */
		final Map.Entry value;

		/** The key. */
		private final String key;

		/**
		 * To string.
		 *
		 * @param from the from
		 * @return the string
		 */
		private static String toString(Object from) {
			return (from == null) ? null : from.toString();
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(ComparableParameter that) {
			return key.compareTo(that.key);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return key;
		}

	}

	/** Retrieve the original parameters from a sorted collection. */
	private static List<Map.Entry> getParameters(
			Collection<ComparableParameter> parameters) {
		if (parameters == null) {
			return null;
		}
		List<Map.Entry> list = new ArrayList<Map.Entry>(parameters.size());
		for (ComparableParameter parameter : parameters) {
			list.add(parameter.value);
		}
		return list;
	}

}
