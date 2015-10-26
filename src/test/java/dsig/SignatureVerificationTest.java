package dsig;

import org.junit.Test;
import org.w3c.dom.Document;
import exception.SignatureValidationException;
import static org.junit.Assert.assertNotNull;
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
        Document receipt = null;
        try
        {
            receipt = ReceiptSignatureHelper.getSignedReceiptDoc(productId);
        }
        catch(Exception e)
        {
            assertNotNull("Error signing receipt", e);
        }
        SignatureVerification sigVer = null;
        try
        {
            sigVer = new SignatureVerification(null, new LocalCertificateRetrievalStrategy());
        }
        catch(Exception e)
        {
            assertNotNull("Error verifying receipt", e);
        }
        boolean valid = false;
        try
        {
            valid = sigVer.validateMSStoreReceipt(receipt);
        }
        catch (SignatureValidationException e)
        {
            assertNotNull("Error validating receipt", e);
        }
        assertTrue("Invalid receipt", valid);
    }
}
