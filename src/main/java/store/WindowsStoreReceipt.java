package store;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class WindowsStoreReceipt {
    
    @XMLPath(path="/Receipt/@CertificateId")
    public final String certificateId; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@PurchasePrice")
    public final String purchasePrice; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@PurchaseDate")
    public final String purchaseDate; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@Id")
    public final String id; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@AppId")
    public final String appId;
    
    @XMLPath(path="/Receipt/ProductReceipt/@ProductId")
    public final String productId; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@ProductType")
    public final String productType; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@PublisherUserId")
    public final String publisherUserId;
    
    @XMLPath(path="/Receipt/ProductReceipt/@PublisherDeviceId")
    public final String publisherDeviceId; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@MicrosoftProductId")
    public final String microsoftProductId; 
    
    @XMLPath(path="/Receipt/ProductReceipt/@MicrosoftAppId")
    public final String microsoftAppId;
    
    public WindowsStoreReceipt(String certificateId, String purchasePrice,
            String purchaseDate, String id, String appId, String productId,
            String productType, String publisherUserId,
            String publisherDeviceId, String microsoftProductId,
            String microsoftAppId) {
        super();
        this.certificateId = certificateId;
        this.purchasePrice = purchasePrice;
        this.purchaseDate = purchaseDate;
        this.id = id;
        this.appId = appId;
        this.productId = productId;
        this.productType = productType;
        this.publisherUserId = publisherUserId;
        this.publisherDeviceId = publisherDeviceId;
        this.microsoftProductId = microsoftProductId;
        this.microsoftAppId = microsoftAppId;
    }
    
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME) 
    public @interface XMLPath {
        public String path();
    }
}
