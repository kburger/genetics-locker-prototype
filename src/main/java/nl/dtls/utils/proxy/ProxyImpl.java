/**
 * The MIT License
 * Copyright © 2017 DTL
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package nl.dtls.utils.proxy;
// This example is from _Java Examples in a Nutshell_. (http://www.oreilly.com)
// Copyright (c) 1997 by David Flanagan
// This example is provided WITHOUT ANY WARRANTY either expressed or implied.
// You may study, use, modify, and distribute it for non-commercial purposes.
// For any commercial use, see http://www.davidflanagan.com/javaexamples

import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.IOUtils;

/**
 * This class uses the Server class to provide a multi-threaded server 
 * framework for a relatively simple proxy service.  The main() method
 * starts up the server.  The nested Proxy class implements the 
 * Server.Service interface and provides the proxy service.
 **/
public class ProxyImpl implements Proxy{

	private SecretKeySpec secretKey;
	private Cipher cipher;
	private URLConnection uc;
	
	
	public static void main(String[] argv) throws MalformedURLException, ProxyException, UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException {
		ProxyImpl p = new ProxyImpl();
		URL url = p.obfuscateURL("http://127.0.0.1:8084/fairdatapoint/fdp/", new URL("http://data.wikipathways.org/20170810/gpml/wikipathways-20170810-gpml-Equus_caballus.zip"));
		
		System.out.println(url.toString());
		
		URL durl = p.resolveObfuscatedURL("dXZDEc5JR2iudgxsZerWeexbGmwLTfYwTAyT2VWFWJp%2FWX7ty7K6jqTA1M0DC6cwY8zwMyDoRR5%2FCmUOittp2qEF%2BM1QxuRyScua7QpuKyUjNhuOV%2FSasO9%2FMpXlb05X");
		
		System.out.println(durl.toString());
                
                String theString = "not init"; 

                try {
                    theString = IOUtils.toString(p.get(durl), "UTF-8");
                } catch (IOException ex) {
                    Logger.getLogger(ProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
                //System.out.println(theString);
              
	}
	
	//TODO create exception
	public ProxyImpl() throws ProxyException {
		
            try {
                byte[] key = fixSecret("=ThiSiSAn~Amzing.!", 16);
                this.secretKey = new SecretKeySpec(key, "AES");
                System.out.println("secret key");
                System.out.println(this.secretKey);
                this.cipher = Cipher.getInstance("AES");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException ex ) {
                Logger.getLogger(ProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw new ProxyException(ex.getMessage(), ex.getCause());
            }
		
	}
	
	private byte[] fixSecret(String s, int length) throws UnsupportedEncodingException {
		if (s.length() < length) {
			int missingLength = length - s.length();
			for (int i = 0; i < missingLength; i++) {
				s += " ";
			}
		}
		return s.substring(0, length).getBytes("UTF-8");
	}
	
	private String encryptAndEncode(String str) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		this.cipher.init(Cipher.ENCRYPT_MODE, this.secretKey);
		byte[] encrypted = cipher.doFinal(str.getBytes());
		String base64encrypt = Base64.getEncoder().encodeToString(encrypted);
		return URLEncoder.encode( base64encrypt , "UTF-8");
		//return encrypted.toString();
	}
	
	private String decryptAndDecode(String str) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
		this.cipher.init(Cipher.DECRYPT_MODE, this.secretKey);
		String base64encrypt = URLDecoder.decode(str, "UTF-8");
		byte[] encrypted = cipher.doFinal(Base64.getDecoder().decode(base64encrypt.getBytes()));
		//byte[] encrypted = cipher.doFinal(Base64.getDecoder().decode(str.getBytes()));
		//byte[] encrypted = cipher.doFinal(URLDecoder.decode(str, "UTF-8").getBytes());
		//byte[] encrypted = cipher.doFinal(str.getBytes());
		return new String(encrypted);
	}
	
	
	public URL obfuscateURL(String baseURL, URL remoteUrl) throws ProxyException {
		String obfuscatedURL;
		URL url = null;
		// baseurl = "http://127.0.0.1:8080/fdp";
		try {
			obfuscatedURL = encryptAndEncode(remoteUrl.toString());
			url = new URL(baseURL+ "/resource/"+URLEncoder.encode(obfuscatedURL, "UTF-8"));
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | MalformedURLException | UnsupportedEncodingException e) {
			ProxyException pe = new ProxyException("The url can't be obfuscated", e.getCause());
			throw pe;
		}
		
		return url;
	}
	
	public URL resolveObfuscatedURL(String remoteURL) throws ProxyException  {
		String clearURL = null;
		URL url;
	
		try {
			clearURL = decryptAndDecode(remoteURL);
                        System.out.println("clear"+clearURL);
			url = new URL(clearURL);
		} catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException | MalformedURLException | UnsupportedEncodingException e) {
			e.printStackTrace();
			ProxyException pe = new ProxyException("The url can't be unobfuscated", e.getCause());
			throw pe;
		}
		
		return url;
	}
	
	public InputStream get(URL url) throws ProxyException{
		InputStream inputStream;
		try {
			uc = url.openConnection();
			inputStream = uc.getInputStream();
			//InputStream in = new BufferedInputStream(raw);
		} catch (IOException e) {
			throw new ProxyException("The URL "+url+" is not available or is invalid", e.getCause());
		}
		
		String contentType = uc.getContentType();
		int contentLength = uc.getContentLength();
		
		//if (contentType.startsWith("text/") || contentLength == -1) {
		//  throw new IOException("This is not a binary file.");
		//}
		 
	    return inputStream;

	}
	
	public String getContentType() {
		//if(this.uc!=null)
		return this.uc.getContentType();
	}
        //switch to resource name
        public String getContentDisposition(){
            
            String raw = this.uc.getHeaderField("Content-Disposition");
            // raw = "attachment; filename=abc.jpg"
            if(raw != null && raw.indexOf("=") != -1) {
                String fileName = raw.split("=")[1]; //getting value after '='
                return fileName;
            } else {
                try {
                    URI uri = this.uc.getURL().toURI();
                    String path = uri.getPath();
                    String filename = path.substring(path.lastIndexOf('/') + 1);
                    return filename;
                } catch (URISyntaxException ex) {
                    Logger.getLogger(ProxyImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return null;
                }
            }
        }
	
	
}