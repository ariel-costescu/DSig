package store;

import java.lang.reflect.Field;
import java.util.HashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import store.WindowsStoreReceipt.XMLPath;

public class WindowsStoreReceiptConverter {
    
    private final XPath xPath;

    public WindowsStoreReceiptConverter() {
        
        XPathFactory xPathFactory = XPathFactory.newInstance();
        xPath = xPathFactory.newXPath();
        xPath.setNamespaceContext(new StoreNamespaceContext());
    }
    
    public WindowsStoreReceipt convertToReceipt(Document doc) throws Exception {
        HashMap<String, String> values = new HashMap<String, String>();
        for (Field f : WindowsStoreReceipt.class.getFields()) {
            XMLPath annotatedPath = f.getAnnotation(XMLPath.class);
            if (annotatedPath != null) {
                String path = StoreNamespaceContext.getNamespaceAwareXPath(annotatedPath.path());
                String value = xPath.evaluate(path, doc);
                if (value != null && value.isEmpty()) {
                    value = null;
                }
                values.put(f.getName(), value);
            }
        }

        String certificateId = values.get("certificateId");
        String purchasePrice = values.get("purchasePrice");
        String purchaseDate = values.get("purchaseDate");
        String id = values.get("id");
        String appId = values.get("appId");
        String productId = values.get("productId");
        String productType = values.get("productType");
        String publisherUserId = values.get("publisherUserId");
        String publisherDeviceId = values.get("publisherDeviceId");
        String microsoftProductId = values.get("microsoftProductId");
        String microsoftAppId = values.get("microsoftAppId");
        
        return new WindowsStoreReceipt(certificateId, purchasePrice, purchaseDate, id, appId, productId, productType, publisherUserId, publisherDeviceId, microsoftProductId, microsoftAppId);
    }
}
