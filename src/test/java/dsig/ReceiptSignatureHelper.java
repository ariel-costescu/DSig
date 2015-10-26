package dsig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Collections;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ReceiptSignatureHelper {
    
    final static String RSA_SHA256 = "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256";
    public static final String RECEIPT_TEMPLATE =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><Receipt Version=\"2.0\" xmlns=\"http://schemas.microsoft.com/windows/2012/store/receipt\"><ProductReceipt PurchasePrice=\"$0\" PurchaseDate=\"2014-04-29T16:51:30.120Z\" Id=\"f2d22698-863f-4483-a4f4-0dc7ee29a887\" AppId=\"ElectronicArtsMobile.NimbleExampleAppTest_q5ha1ztykcgvj\" ProductId=\"[ProductId]\" ProductType=\"Consumable\" PublisherUserId=\"TmMiuKxF8UgqpMWwVfANBOV97rjrHHI7UhJ4DO46sdk=\" PublisherDeviceId=\"6uaW2aefYvdmra0QSJrnPQFMNgdX7+0hudsmxjud8Tk=\" MicrosoftProductId=\"55918f94-3ef3-4490-8797-2a350ee00d1a\" MicrosoftAppId=\"1f488fe8-4a94-473c-a1a9-0787c5e8d64f\" /></Receipt>";

    public static final String RECEIPT_ID = "f2d22698-863f-4483-a4f4-0dc7ee29a887";

    private static XMLSignature signature;
    private static PrivateKey privKey;
    
    /**
     * Lazy init signature and private key
     * 
     * @throws Exception
     */
    private static void init() throws Exception  {
        if (signature == null) {
            XMLSignatureFactory signFactory = XMLSignatureFactory.getInstance("DOM");
            
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream certSource = ReceiptSignatureHelper.class.getClassLoader().getResourceAsStream("server.crt");
            Certificate cert = certFactory.generateCertificate(certSource);
            
            SignatureMethod sm = signFactory.newSignatureMethod(RSA_SHA256, null);
            CanonicalizationMethod cm = signFactory.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE , (C14NMethodParameterSpec) null);
            Transform t = signFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);
            Reference ref = signFactory.newReference(
                    "",
                    signFactory.newDigestMethod(DigestMethod.SHA256, null),
                    Collections.singletonList(t),
                    null, 
                    null
                );
            
            SignedInfo si = signFactory.newSignedInfo(cm, sm, Collections.singletonList(ref));
            
            KeyInfoFactory keyInfoFactory = signFactory.getKeyInfoFactory();
            KeyValue keyValue = keyInfoFactory.newKeyValue(cert.getPublicKey());
            KeyInfo ki = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));
            
            signature = signFactory.newXMLSignature (si, ki);
        }
        
        if (privKey == null) {
            InputStream pk = ReceiptSignatureHelper.class.getClassLoader().getResourceAsStream("server.pkcs8");
            byte[] encoded = IOUtils.toByteArray(pk);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
            
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privKey = kf.generatePrivate(keySpec);
        }
    }
    
    public static Document getSignedReceiptDoc(String productId) throws Exception {
        init();
        
        String receipt = RECEIPT_TEMPLATE.replaceAll("\\[ProductId\\]", productId);
        DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        byte[] bytes = receipt.getBytes(Charset.forName("UTF-8"));
        InputStream in = new ByteArrayInputStream(bytes);
        Document doc = docBuilder.parse(in);
        NodeList receiptNode = doc.getElementsByTagName("Receipt");
        Node parent = receiptNode.item(0);
        
        DOMSignContext dsc = new DOMSignContext(privKey, parent); 
        signature.sign(dsc);
        
        return doc;
    }
    
    //TODO Investigate how to transform the DOM into a String that passes core validation
    private static String getSignedReceipt(String productId) throws Exception {
        Document doc = getSignedReceiptDoc(productId);
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        //trans.setOutputProperty(OutputKeys.INDENT, "no");
        //trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
//        NodeList sig = doc.getElementsByTagName("Signature");
//        Node sigNode = sig.item(0);
//        DOMSource source = new DOMSource(sigNode);
        
        DOMSource source = new DOMSource(doc);
        
        trans.transform(source, result);
        
        String sigString = writer.toString();
        //For some odd reason, the above results in random line breaks which interfere with signature validation
        sigString = sigString.replaceAll("[\n\r\t]", "");
        
//        StringBuilder sb = new StringBuilder(receipt); 
//        int insert = receipt.lastIndexOf("</Receipt>");
//        sb.insert(insert, sigString);
//        receipt = sb.toString();
//        
//        return receipt;
        return sigString;
    }
}
