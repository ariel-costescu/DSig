package dsig;

import org.junit.Test;
import org.w3c.dom.Document;
import exception.SignatureValidationException;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SignatureVerificationTest
{

    /**
     * Test XML DSig using a DOM document as input
     * KeyInfo is contained in the receipt
     * 
     */
    @Test
    public void testSignatureVerificationDOM()
    {
        String productId = "100";
        Document receiptDOM = null;
        try
        {
            receiptDOM = ReceiptSignatureHelper.getSignedReceiptDoc(productId);
        }
        catch(Exception e)
        {
            assertNull("Error signing receipt", e);
        }
        SignatureVerification sigVer = null;
        try
        {
            sigVer = new SignatureVerification(null, new LocalCertificateRetrievalStrategy());
        }
        catch(Exception e)
        {
            assertNull("Error verifying receipt", e);
        }
        
        boolean valid = false;
        try
        {
            valid = sigVer.validateMSStoreReceipt(receiptDOM);
        }
        catch (SignatureValidationException e)
        {
            assertNull("Error validating receipt", e);
        }
        assertTrue("Invalid receipt", valid);
    }

    /**
     * Test XML DSig using an XML String as input
     * KeyInfo is contained in the receipt
     * 
     */
    @Test
    public void testSignatureVerificationString()
    {
        String productId = "100";
        String receipt = null;
        try
        {
            receipt = ReceiptSignatureHelper.getSignedReceipt(productId);
        }
        catch(Exception e)
        {
            assertNull("Error signing receipt", e);
        }
        SignatureVerification sigVer = null;
        try
        {
            sigVer = new SignatureVerification(null, new LocalCertificateRetrievalStrategy());
        }
        catch(Exception e)
        {
            assertNull("Error verifying receipt", e);
        }
        boolean valid = false;
        try
        {
            valid = sigVer.validateMSStoreReceipt(receipt);
        }
        catch (SignatureValidationException e)
        {
            assertNull("Error validating receipt", e);
        }
        assertTrue("Invalid receipt", valid);
    }
}
