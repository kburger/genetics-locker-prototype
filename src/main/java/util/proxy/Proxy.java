package util.proxy;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

public interface Proxy {
	URL obfuscateURL(URL remoteUrl) throws ProxyException;
	URL resolveObfuscatedURL(String remoteURL) throws ProxyException;
	InputStream get(URL url) throws ProxyException;;
}
