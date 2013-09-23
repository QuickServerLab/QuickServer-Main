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

package org.quickserver.net.client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.logging.*;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * Blocking Client socket.
 * @author Akshathkumar Shetty
 * @since 1.4.7
 */
public class BlockingClient implements ClientService {
	private static final Logger logger = Logger.getLogger(BlockingClient.class.getName());
	private static String charset = "ISO-8859-1";
	private static boolean debug = false;
	private static final int _CR = 13;  
	private static final int _LF = 10;
	
	public static boolean isDebug() {
		return debug;
	}

	public static void setDebug(boolean aDebug) {
		debug = aDebug;
	}

	private String host = "localhost";
	private int port = 0;	

	private Socket socket;
	
	private boolean secure;
	private boolean useDummyTrustManager;
	private TrustManager[] trustManager;
	private SSLContext sslContext;
	private SSLSocketFactory sslSocketFactory;
	private InputStream clientAuthKeystoreInputStream;
	private char[] clientAuthKeystorePassword;
	private char[] clientAuthKeyPassword;
	
	private OutputStream out;
	private BufferedOutputStream b_out;
	private ObjectOutputStream o_out;

	private InputStream in;
	private BufferedInputStream b_in;
	private BufferedReader br;
	private ObjectInputStream o_in;

	public void setCharset(String c) {
		charset = c;
	}
	public String getCharset() {
		return charset;
	}	

	public int getMode() {
		return ClientService.BLOCKING;
	}

	public void connect(String host, int port) throws Exception {
		this.host = host;
		this.port = port;

		if(isDebug()) logger.finest("Connecting to "+host+":"+port);
		
		if(isSecure()) {
			makeSSLSocketFactory();
			socket = getSslSocketFactory().createSocket(host, port);
		} else {
			socket = new Socket(host, port);
		}

		in = socket.getInputStream();
		out = socket.getOutputStream();
		if(isDebug()) logger.fine("Connected");
	}

	public boolean isConnected() {
		if(socket==null) return false;
		return socket.isConnected();
	}

	public void close() throws IOException {
		if(isDebug()) logger.fine("Closing");
		
		if(out!=null) {
			if(isDebug()) logger.finest("Closing output streams");
			try {
				out.flush();
			} catch(IOException ioe) {
				logger.finest("Flushing output streams failed: "+ioe);
			}
			/*
			if(socket!=null && isSecure()==false) {
				socket.shutdownOutput();
			}
			 */

			try {
				if(o_out != null) {
					o_out.close();
				}
			} catch(IOException ioe) {
				logger.finest("o_out stream close failed: "+ioe);
			}

			try {
				if(b_out != null) {
					b_out.close();
				}
			} catch(IOException ioe) {
				logger.finest("b_out stream close failed: "+ioe);
			}

			try {
				out.close();
			} catch(IOException ioe) {
				logger.finest("out stream close failed: "+ioe);
			}
		}

		if(in!=null) {
			if(isDebug()) logger.finest("Closing input streams");
			/*
			if(socket!=null && isSecure()==false) {
				socket.shutdownInput();
			}
			 */

			if(o_in != null) {
				try {
					o_in.close();
				} catch(IOException ioe) {
					logger.finest("o_in stream close failed: "+ioe);
				}
			} 
			if(b_in != null) {
				try {
					b_in.close();
				} catch(IOException ioe) {
					logger.finest("b_in stream close failed: "+ioe);
				}
			}	
			if(br != null) {
				try {
					br.close();
				} catch(IOException ioe) {
					logger.finest("b_in stream close failed: "+ioe);
				}
			}
			try {
				in.close();
			} catch(IOException ioe) {
				logger.finest("in stream close failed: "+ioe);
			}
		}

		if(socket!=null) {
			socket.close();
			socket = null;
		}
	}
	
	public void sendByte(int data) throws IOException {
		if(isDebug()) logger.fine("Sending byte");
		checkBufferedOutputStream();
		b_out.write(data);
		b_out.flush();
	}

	public void sendBytes(byte[] data) throws IOException {
		if(isDebug()) logger.fine("Sending bytes: "+data.length);
		checkBufferedOutputStream();
		b_out.write(data);
		b_out.flush();
	}

	public void sendBytes(String data, String _charset) throws IOException {
		if(isDebug()) logger.fine("Sending: "+data);
		checkBufferedOutputStream();
		if(_charset==null) _charset = charset;
		byte d[] = data.getBytes(_charset);
		b_out.write(d, 0 , d.length);
		b_out.flush();
	}

	public void sendLine(String data, String _charset) throws IOException {
		if(isDebug()) logger.fine("Sending: "+data);
		checkBufferedOutputStream();
		if(_charset==null) _charset = charset;
		byte d[] = data.getBytes(_charset);
		b_out.write(d, 0 , d.length);
		d = "\r\n".getBytes(_charset);
		b_out.write(d, 0 , d.length);
		b_out.flush();
	}

	public void sendObject(Object data) throws IOException {
		checkObjectOutputStream();
		o_out.writeObject(data);
		o_out.flush();
	}
	
	public int readByte() throws IOException {
		checkBufferedInputStream();
		return b_in.read();
	}

	public byte[] readBytes() throws IOException {
		checkBufferedInputStream();
		return readInputStream(b_in);
	}
	
	public byte[] readBytes(int countToRead) throws IOException {
		checkBufferedInputStream();
		return readInputStream(b_in, countToRead);
	}

	public String readBytes(String _charset) throws IOException {
		byte data[] = readBytes();
		if(data==null) return null;
		if(_charset==null) _charset = charset;
		return new String(data, _charset);
	}

	public String readLine() throws IOException {
		checkBufferedReader();
		return br.readLine();
	}
	
	 public String readCRLFLine() throws IOException { 
		checkBufferedInputStream();
		
		StringBuilder sb = new StringBuilder();  
		int _ch = -1;
		do {
			_ch = in.read();
			if(_ch==-1) {
				return null;
			}
			if(_ch==_CR) {
				_ch = in.read();
				if(_ch==_LF) {
					break;
				} else {
					sb.append((char) _CR);  
					sb.append((char) _ch);
				}
			} else {
				sb.append((char) _ch);  
			}
		} while(true);		
		
		return(new String(sb));  
   } 

	public Object readObject() throws IOException, ClassNotFoundException {
		checkObjectInputStream();
		return o_in.readObject();
	}

	public Socket getSocket() {
		return socket;
	}

	private void checkObjectOutputStream() throws IOException {
		if(o_out==null) {
			b_out = null;
			o_out = new ObjectOutputStream(out);
			o_out.flush();
		}
	}
	private void checkBufferedOutputStream() throws IOException {
		if(b_out==null) {
			o_out = null;
			b_out = new BufferedOutputStream(out);
		}
	}

	private void checkBufferedInputStream() throws IOException {
		if(b_in==null) {
			br = null;
			o_in = null;
			b_in = new BufferedInputStream(in);
		}
	}
	private void checkBufferedReader() throws IOException {
		if(br==null) {
			b_in = null;
			o_in = null;
			br = new BufferedReader(new InputStreamReader(in, charset));
		}
	}
	private void checkObjectInputStream() throws IOException {
		if(o_in==null) {
			b_in = null;
			br = null;
			o_in = new ObjectInputStream(in);
		}
	}
	
	public static byte[] readInputStream(InputStream _in, int countToRead) 
			throws IOException {
		byte data[] = new byte[countToRead];
		if(_in==null) {
			throw new IOException("InputStream can't be null!");
		}
		
		int count = 0;
		int dataRead = 0;
		int dataLeftToRead = countToRead - dataRead;
		
		while(true) {
			count = _in.read(data, dataRead, dataLeftToRead);
			if(count==-1) {
				if(dataRead==countToRead) {
					break;
				} else {
					throw new IOException("we have eof!");
				}
			} else {
				dataRead = dataRead + count;
				dataLeftToRead = countToRead - dataRead;
			}
			
			if(dataRead>=countToRead) {
				break;
			}			
		}//while
		return data;
	}

	public static byte[] readInputStream(InputStream _in) throws IOException {
		byte data[] = null;
		if(_in==null)
			throw new IOException("InputStream can't be null!");
		
		int s = _in.read();
		if(s==-1) {
			return null; //Connection lost
		}
		int alength = _in.available();
		if(alength > 0) {
			data = new byte[alength+1];	
			data[0] = (byte) s;
			int len = _in.read(data, 1, alength);
			if(len < alength) {
				data = copyOf(data, len+1);
			}
		} else {
			data = new byte[1];
			data[0] = (byte) s;
		}
		return data;
	}
	
	private static byte[] copyOf(byte data[], int len) {
		byte newdate[] = new byte[len];
		System.arraycopy(data, 0, newdate, 0, len);
		return newdate;
	}
	
	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public boolean isUseDummyTrustManager() {
		return useDummyTrustManager;
	}

	public void setUseDummyTrustManager(boolean useDummyTrustManager) {
		this.useDummyTrustManager = useDummyTrustManager;
	}

	public TrustManager[] getTrustManager() {
		return trustManager;
	}

	public void setTrustManager(TrustManager[] trustManager) {
		this.trustManager = trustManager;
	}

	public SSLContext getSslContext() {
		return sslContext;
	}

	public void setSslContext(SSLContext sslContext) {
		this.sslContext = sslContext;
	}
	
	public SSLSocketFactory getSslSocketFactory() {
		return sslSocketFactory;
	}

	public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
		this.sslSocketFactory = sslSocketFactory;
	}

	public void makeSSLSocketFactory() throws Exception {
		if(getSslContext()==null && getSslSocketFactory()==null) {
			SSLContext context = SSLContext.getInstance("SSLv3");
			if(getTrustManager()==null && isUseDummyTrustManager()) {
				setTrustManager(new TrustManager[]{DummyTrustManager.getInstance()});
			}
			
			KeyManager km[] = null;
			if(getClientAuthKeystoreInputStream()!=null) {
				KeyStore keyStore = KeyStore.getInstance("JKS");
				keyStore.load(getClientAuthKeystoreInputStream(), getClientAuthKeystorePassword());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(keyStore, getClientAuthKeyPassword());
				km = kmf.getKeyManagers();
			} else {
				km = new KeyManager[0];
			}

			context.init(km, getTrustManager(), new SecureRandom());
			setSslContext(context);
		}
		
		if(getSslSocketFactory()==null) {
			SSLSocketFactory factory = getSslContext().getSocketFactory();
			setSslSocketFactory(factory);
		}
	}

	public InputStream getClientAuthKeystoreInputStream() {
		return clientAuthKeystoreInputStream;
	}

	public void setClientAuthKeystoreInputStream(InputStream clientAuthKeystoreInputStream) {
		this.clientAuthKeystoreInputStream = clientAuthKeystoreInputStream;
	}

	public char[] getClientAuthKeystorePassword() {
		return clientAuthKeystorePassword;
	}

	public void setClientAuthKeystorePassword(char clientAuthKeystorePassword[]) {
		this.clientAuthKeystorePassword = clientAuthKeystorePassword;
	}

	public char[] getClientAuthKeyPassword() {
		return clientAuthKeyPassword;
	}

	public void setClientAuthKeyPassword(char[] clientAuthKeyPassword) {
		this.clientAuthKeyPassword = clientAuthKeyPassword;
	}
}
