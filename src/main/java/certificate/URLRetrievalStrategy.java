package certificate;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.concurrent.Callable;
//import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import exception.SignatureValidationException;
import exception.SignatureValidationFailure;

public class URLRetrievalStrategy implements CertificateRetrievalStrategy
{

    private final String msStoreCertUrl;
    private final long timeout;

    public URLRetrievalStrategy(String msStoreCertUrl, long timeout)
    {
        this.msStoreCertUrl = msStoreCertUrl;
        this.timeout = timeout;
    }

    @Override
    public Certificate retrieveCertificate(String certificateId)
                    throws SignatureValidationException
    {
        Certificate cert = null;
        Throwable th = null;
        try
        {
            String replaced = msStoreCertUrl.replaceAll("\\[CertificateId\\]",
                certificateId);
            final URL certUri = new URL(replaced);
            
            FutureTask<Certificate> future = new FutureTask<Certificate>(new Callable<Certificate>()
            {

                @Override
                public Certificate call() throws Exception
                {
                    URLConnection conn = certUri.openConnection();
                    InputStream certSource = conn.getInputStream();
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    return certFactory.generateCertificate(certSource);
                }
            });

            cert = future.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (Exception e)
        {
            th = e;
        }
        if (th != null)
        {
            SignatureValidationException.fail(SignatureValidationFailure.FAILED_CERT_RETRIEVAL, th);
        }
        return cert;
    }

}
