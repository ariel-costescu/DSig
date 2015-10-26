package dsig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.Certificate;
import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import store.WindowsStoreReceiptConverter;
import certificate.CertExternalCertificatePolicy;
import certificate.CertificateRetrievalStrategy;
import certificate.MSStoreCertificateCache;
import config.Configuration;
import exception.SignatureValidationException;
import exception.SignatureValidationFailure;

/**
 * Utility class for validating an XML Signature using the JSR 105 API.
 * 
 * Note: an instance of this class is not safe to reuse across multiple threads
 */
public class SignatureVerification {

    public static final String SIGNATURE_TAG = "Signature";
    public static final String KEYINFO_TAG = "KeyInfo";

    private DocumentBuilder builder;
    private final XMLSignatureFactory signFactory;
    private final WindowsStoreReceiptConverter receiptConverter;
    private final MSStoreCertificateCache certCache;

    public SignatureVerification(XMLSignatureFactory signFactory, CertificateRetrievalStrategy retrievalStrategy)
          throws SignatureValidationException
    {
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                        .newInstance();
        builderFactory.setNamespaceAware(true);

        receiptConverter = new WindowsStoreReceiptConverter();

        if (signFactory == null)
        {
            this.signFactory = XMLSignatureFactory.getInstance("DOM");
        }
        else
        {
            this.signFactory = signFactory;
        }
        try
        {
            builder = builderFactory.newDocumentBuilder();
        }
        catch (ParserConfigurationException e)
        {
            builder = null;
            SignatureValidationException.fail(SignatureValidationFailure.FAILED_INIT, e);
        }
        certCache = new MSStoreCertificateCache(retrievalStrategy);
    }

    public SignatureVerification() throws SignatureValidationException
    {
        this(null, null);
    }

    /**
     * Validate XML signature from a DOM representation of the document, using an external X.509
     * certificate
     * 
     * @return True, if core validation succeeds
     * @throws SignatureValidationException
     */
    public boolean validateMSStoreReceipt(String source)
                    throws SignatureValidationException
    {
        Document doc = getValidDocument(source);
        String certId = getCertId(doc);

        return validateSignature(doc, certId);
    }

    /**
     * Validate XML signature from a DOM representation of the document, using an external X.509
     * certificate
     * 
     * @return True, if core validation succeeds
     * @throws SignatureValidationException
     */
    public boolean validateMSStoreReceipt(Document source)
                    throws SignatureValidationException
    {
        String certId = getCertId(source);

        return validateSignature(source, certId);
    }

    public String getCertId(Document doc) throws SignatureValidationException
    {
        String certId = null;
        try
        {
            certId = receiptConverter.convertToReceipt(doc).certificateId;
        }
        catch (Exception e)
        {
            if (getMsCertCertificatePolicy().equals(CertExternalCertificatePolicy.EXTERNAL_ONLY))
            {
                SignatureValidationException.fail(SignatureValidationFailure.MISSING_CERT_ID, e);
            }
        }
        return certId;
    }

    /**
     * Attempt to parse the input as an XML document
     * 
     * @param source String representation of a receipt
     * @return A parsed DOM document
     * @throws SignatureValidationException when input is not valid, the exception reason will be
     *             SignatureValidationFailure.NOT_A_VALID_XML
     */
    public Document getValidDocument(String source)
                    throws SignatureValidationException
    {
        byte[] bytes = source.getBytes(Charset.forName("UTF-8"));
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        return getValidDocument(is);
    }

    public Document getValidDocument(InputStream source)
                    throws SignatureValidationException
    {
        Document doc = null;

        try
        {
            doc = builder.parse(source);
        }
        catch (IOException e)
        {
            SignatureValidationException.fail(SignatureValidationFailure.NOT_A_VALID_XML, e);
        }
        catch (SAXException e)
        {
            SignatureValidationException.fail(SignatureValidationFailure.NOT_A_VALID_XML, e);
        }

        return doc;
    }

    public CertExternalCertificatePolicy getMsCertCertificatePolicy()
    {
        int policy = Configuration.getExternalCertificatePolicy();
        return CertExternalCertificatePolicy.fromDbValue(policy);
    }

    private boolean validateSignature(Document doc, String certId)
                    throws SignatureValidationException
    {

        // Find Signature element
        NodeList nl = doc.getElementsByTagNameNS(XMLSignature.XMLNS,
            SIGNATURE_TAG);
        if (nl.getLength() == 0)
        {
            SignatureValidationException.fail(SignatureValidationFailure.MISSING_SIGNATURE_ELEM);
        }

        // Create a validation context around the signature that will use the
        // provided
        // certificate as a public key
        DOMValidateContext valContext = null;

        boolean missingCertId = certId == null || certId.isEmpty();
        NodeList keyInfo = doc.getElementsByTagNameNS(XMLSignature.XMLNS,
            KEYINFO_TAG);
        boolean missingKeyInfo = keyInfo == null || keyInfo.getLength() == 0;

        CertExternalCertificatePolicy policy = getMsCertCertificatePolicy();
        switch (policy)
        {
        case EXTERNAL_ONLY:
        {
            if (missingCertId)
            {
                SignatureValidationException.fail(SignatureValidationFailure.MISSING_CERT_ID);
            }
            else
            {
                valContext = getContextFromExternal(certId, nl);
            }
            break;
        }
        case EXTERNAL_AND_INTERNAL:
        {
            if (missingCertId && missingKeyInfo)
            {
                SignatureValidationException.fail(SignatureValidationFailure.MISSING_CERT_ID);
            }
            else
            {
                if (!missingCertId)
                {
                    valContext = getContextFromExternal(certId, nl);
                }
                else if (!missingKeyInfo)
                {
                    valContext = getContextFromInternal(certId, nl);
                }
            }
            break;
        }
        case INTERNAL_ONLY:
        {
            if (missingKeyInfo)
            {
                SignatureValidationException.fail(SignatureValidationFailure.MISSING_CERT_ID);
            }
            else
            {
                valContext = getContextFromInternal(certId, nl);
            }
            break;
        }
        default:
        {
            SignatureValidationException.fail(SignatureValidationFailure.WRONG_RETRIEVAL_POLICY);
        }
        }

        // unmarshal the XMLSignature
        XMLSignature signature = null;
        try
        {
            signature = signFactory.unmarshalXMLSignature(valContext);
        }
        catch (MarshalException e)
        {
            SignatureValidationException.fail(SignatureValidationFailure.FAILED_UNMARSHALLING, e);
        }

        // Validate the XMLSignature (generated above)
        boolean coreValidity = false;
        if (signature != null)
        {
            try
            {
               coreValidity = signature.validate(valContext);
            }
            catch (Exception e)
            {
                SignatureValidationException.fail(SignatureValidationFailure.FAILED_VALIDATION, e);
            }
        }

        return coreValidity;
    }

    private DOMValidateContext getContextFromExternal(String certId, NodeList nl)
                    throws SignatureValidationException
    {
        Certificate cert = certCache.getCertificate(certId);

        PublicKey key = cert.getPublicKey();
        return new DOMValidateContext(key, nl.item(0));
    }

    private DOMValidateContext getContextFromInternal(String certId, NodeList nl)
                    throws SignatureValidationException
    {
        return new DOMValidateContext(new KeyValueSelector(), nl.item(0));
    }
}
