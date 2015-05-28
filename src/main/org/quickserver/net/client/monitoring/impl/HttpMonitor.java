/*
 * This file is part of the QuickServer library 
 * Copyright (C) QuickServer.org
 *
 * Use, modification, copying and distribution of this software is subject to
 * the terms and conditions of the GNU Lesser General Public License. 
 * You should have received a copy of the GNU LGP License along with this 
 * library; if not, you can download a copy from <http://www.quickserver.org/>.
 *
 * For questions, suggestions, bug-reports, enhancement-requests etc.
 * visit http://www.quickserver.org
 *
 */
package org.quickserver.net.client.monitoring.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import org.quickserver.net.client.BlockingClient;
import org.quickserver.net.client.DummyTrustManager;
import org.quickserver.net.client.Host;
import org.quickserver.net.client.monitoring.HostMonitor;
import org.quickserver.net.client.HttpHost;

/**
 *
 * @since 1.4.8
 * @author Akshathkumar Shetty
 */
public class HttpMonitor implements HostMonitor {
	private static final Logger logger = Logger.getLogger(HttpMonitor.class.getName());
        
        private static String providerForSSLContext;

    /**
     * @return the providerForSSLContext
     */
    public static String getProviderForSSLContext() {
        return providerForSSLContext;
    }

    /**
     * @param aProviderForSSLContext the providerForSSLContext to set
     */
    public static void setProviderForSSLContext(String aProviderForSSLContext) {
        providerForSSLContext = aProviderForSSLContext;
    }
	
	private SSLSocketFactory sslSocketFactory;

	private TrustManager[] trustManager;
	private SSLContext sslContext;

	public char monitor(Host host) {
		HttpHost httpHost = (HttpHost) host;
		HttpURLConnection http = null;
		BufferedInputStream bis = null;
		URL url = null;
		try {
			String urlString = null;

			if (httpHost.isSecure()) {
				urlString = "https://";
			} else {
				urlString = "http://";
			}
			urlString = urlString + httpHost.getInetAddress().getHostName()
					+ ":" + httpHost.getInetSocketAddress().getPort() + httpHost.getUri();
			
			url = new URL(urlString);			
			
			http = (HttpURLConnection) url.openConnection();
			if (httpHost.isSecure()) {
				HttpsURLConnection https = (HttpsURLConnection) http;

				makeSSLSocketFactory();

				https.setSSLSocketFactory(sslSocketFactory);
				https.setHostnameVerifier(vf);
			}
			http.setRequestMethod("GET");
			http.setDoOutput(true);
			http.setReadTimeout(httpHost.getTimeout());
			http.setInstanceFollowRedirects(false);

			http.connect();

			String httpResponseCode = "" + http.getResponseCode();

			if(httpHost.isValidHttpStatusCode(httpResponseCode)==false) {
				logger.log(Level.FINE, "StatusCode does not match.. got {0}, expected: {1} for host {2}", 
					new Object[]{httpResponseCode, httpHost.getHttpStatusCode(), 
						httpHost.getInetAddress().getHostName()});
				
				return Host.DOWN;
			}

			if (httpHost.getTextToExpect() != null) {		
				InputStream is = http.getErrorStream();
				if(is==null) {
					is = http.getInputStream();
				}
				bis = new BufferedInputStream(is);

				byte data[] = BlockingClient.readInputStream(bis);
				String textGot = null;

				if(data!=null) textGot = new String(data, "utf-8");

				if (textGot.indexOf(httpHost.getTextToExpect()) != -1) {
					return Host.ACTIVE;
				} else {
					logger.log(Level.FINE, "{0} Error: Text [{1}] Not found! Got: {2} for host {3}", 
						new Object[]{httpHost, httpHost.getTextToExpect(), textGot, 
							httpHost.getInetAddress().getHostName()});
					logger.log(Level.FINEST, "Got Data: {0}", textGot);
					return Host.DOWN;
				}
			} else {
				return Host.ACTIVE;
			}
		} catch (IOException e) {			
			logger.log(Level.FINE, "url {0}", new Object[]{url});
			logger.log(Level.FINE, httpHost+ " IOError: "+e, e);
			return Host.DOWN;
		} catch (Exception e) {
			logger.log(Level.WARNING, httpHost+" Error: "+e, e);
			return Host.ERROR;
		} finally {
			if(bis!=null) {
				try {
					bis.close();
				} catch (IOException ex) {
					Logger.getLogger(HttpMonitor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
			if (http != null) {
				http.disconnect();
				
			}
			host.setLastCheckedOn(new Date());
		}
	}

	public void makeSSLSocketFactory() throws Exception {
		if (sslContext == null && getSslSocketFactory() == null) {
                        if(getProviderForSSLContext()!=null) {
                            sslContext = SSLContext.getInstance("TLS", getProviderForSSLContext());
                        } else {
                            sslContext = SSLContext.getInstance("TLS");
                        }
			if (trustManager == null) {
				trustManager = new TrustManager[]{DummyTrustManager.getInstance()};
			}

			sslContext.init(new KeyManager[0], trustManager, new SecureRandom());
		}

		if (getSslSocketFactory() == null) {
			sslSocketFactory = sslContext.getSocketFactory();
		}
	}
	static HostnameVerifier vf = new HostnameVerifier() {

		public boolean verify(String hostName, SSLSession session) {
			//logger.warning("WARNING: hostname may not match the certificate host name :" + hostName);
			return true;
		}
	};
	
	

	
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}
}
