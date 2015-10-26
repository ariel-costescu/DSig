package store;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.NamespaceContext;
import com.google.common.base.Joiner;

public class StoreNamespaceContext implements NamespaceContext {

	public static final String PREFIX = "store";
	public static final String XMLNS ="http://schemas.microsoft.com/windows/2012/store/receipt";
	
	@Override
	public String getNamespaceURI(String prefix) {
		if (prefix.equals(PREFIX)) {
			return XMLNS;
		}
		else {
			return null;
		}
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (namespaceURI.equals(XMLNS)) {
			return PREFIX;
		}
		else {
			return null;
		}
	}

	@Override
	public Iterator<?> getPrefixes(String namespaceURI) {
		String prefix = getPrefix(namespaceURI);
		if (prefix == null) {
			return null;
		}
		else {
			List<String> l = new ArrayList<String>(1);
			l.set(0, prefix);
			return l.iterator();
		}
	}

	/**
	 * Prefix xpath elements with the default namespace.
	 * Excludes attributes and elements that are explicitly prefixed already.
	 */
	public static String getNamespaceAwareXPath(String xpath) {
		String[] parts = xpath.split("/");
		for (int i=1; i < parts.length; i++) {
			String s = parts[i];
			if (!s.isEmpty() && !s.contains(":") && !s.startsWith("@")) {
				s = PREFIX + ":" + s;
				parts[i] = s;
			}
		}
		return Joiner.on("/").join(parts);
		//return String.join("/", parts);
	}
}
