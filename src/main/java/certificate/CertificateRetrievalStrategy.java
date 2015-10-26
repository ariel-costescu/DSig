package certificate;

import java.security.cert.Certificate;
import exception.SignatureValidationException;

/**
 * Functional interface that allows multiple implementations to retrieve a certificate, given a cert
 * id
 * 
 */
public interface CertificateRetrievalStrategy
{

    public Certificate retrieveCertificate(String certificateId)
                    throws SignatureValidationException;
}
