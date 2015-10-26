package dsig;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.List;
import javax.xml.crypto.AlgorithmMethod;
import javax.xml.crypto.KeySelector;
import javax.xml.crypto.KeySelectorException;
import javax.xml.crypto.KeySelectorResult;
import javax.xml.crypto.XMLCryptoContext;
import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyValue;

public class KeyValueSelector extends KeySelector
{
    @Override
    public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
                    AlgorithmMethod method, XMLCryptoContext context)
                    throws KeySelectorException
    {
        if (keyInfo == null)
        {
            throw new KeySelectorException("Null KeyInfo object!");
        }
        SignatureMethod sm = (SignatureMethod) method;
        List<?> list = keyInfo.getContent();

        for (int i = 0; i < list.size(); i++)
        {
            XMLStructure xmlStructure = (XMLStructure) list.get(i);
            if (xmlStructure instanceof KeyValue)
            {
                PublicKey pk = null;
                try
                {
                    pk = ((KeyValue) xmlStructure).getPublicKey();
                }
                catch (KeyException ke)
                {
                    throw new KeySelectorException(ke);
                }
                if (isCompatible(sm.getAlgorithm(), pk.getAlgorithm()))
                {
                    return new SimpleKeySelectorResult(pk);
                }
            }
        }
        throw new KeySelectorException("No KeyValue element found!");
    }

    /**
     * Check that the public key algorithm is compatible with the signature method.
     * The URI fragment from the signature method is in the format: (PublicKeyAlgo)-(DigestAlgo) 
     * 
     * @param smAlgo The signature method's algorithm URI.
     * @param pkAlgo The public key algorithm
     * @return
     */
    private boolean isCompatible(String smAlgo, String pkAlgo)
    {
        URI uri = null;
        try 
        {
            uri = new URI(smAlgo);
        }
        catch (URISyntaxException e)
        {
            return false;
        }
        
        String algFrag = uri.getFragment();
        String[] algParts = algFrag.split("-");
        if (algParts == null || algParts.length != 2) 
        {
            return false;
        }
        String keyAlgo= algParts[0];
        if (pkAlgo.equalsIgnoreCase(keyAlgo)) {
            return true;
        }
        return false;
    }

    private static class SimpleKeySelectorResult implements KeySelectorResult
    {
        private PublicKey pk;

        SimpleKeySelectorResult(PublicKey pk)
        {
            this.pk = pk;
        }

        public Key getKey()
        {
            return pk;
        }
    }
}
