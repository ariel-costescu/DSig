package certificate;

import java.security.cert.Certificate;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import org.javatuples.Pair;
import config.Configuration;
import exception.SignatureValidationException;
import exception.SignatureValidationFailure;

public class MSStoreCertificateCache
{
    public static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private final String msStoreCertUrl;
    private final long msStoreCertCacheTTL;
    private final long msStoreCertTimeoutMillis;

    private final CertificateRetrievalStrategy retrievalStrategy;

    private static final ConcurrentHashMap<String, Pair<Certificate, Date>> certificates =
                    new ConcurrentHashMap<String, Pair<Certificate, Date>>();

    public MSStoreCertificateCache(CertificateRetrievalStrategy retrievalStrategy)
    {
        msStoreCertUrl = Configuration.getCertUrl(); 
        msStoreCertCacheTTL = Configuration.getCertCacheExpiration();
        msStoreCertTimeoutMillis = Configuration.getCertTimeoutMillis();

        if (retrievalStrategy != null)
        {
            this.retrievalStrategy = retrievalStrategy;
        }
        else
        {
            this.retrievalStrategy = new URLRetrievalStrategy(msStoreCertUrl, msStoreCertTimeoutMillis);
        }
    }

    /**
     * Get MS Store certificate for a corresponding certificate id. Will retrieve a cached version
     * if there is already one stored that hasn't had it's TTL invalidated, or otherwise go to a MS
     * public API.
     * 
     * @param certificateId Cert id extracted from a receipt.
     * @return A parsed certificate
     * @throws SignatureValidationException If the MS Store API isn't reachable, an error is thrown
     */
    public Certificate getCertificate(String certificateId)
                    throws SignatureValidationException
    {
        Certificate cert = null;
        Date now = new Date();
        if (certificates.containsKey(certificateId))
        {
            Pair<Certificate, Date> cachedCert = certificates.get(certificateId);
            Date cacheDate = cachedCert.getValue1();
            long daysOld = (now.getTime() - cacheDate.getTime()) / MILLIS_IN_DAY;
            if (msStoreCertCacheTTL > daysOld)
            {
                return cachedCert.getValue0();
            }
            else
            {
                cert = retrievalStrategy.retrieveCertificate(certificateId);
            }
        }
        else
        {
            cert = retrievalStrategy.retrieveCertificate(certificateId);
        }
        // Either cache a new cert, or update the old entry if the TTL expired
        if (cert != null)
        {
            Pair<Certificate, Date> cachedCert = new Pair<Certificate, Date>(
                            cert, now);
            certificates.put(certificateId, cachedCert);
            return cert;
        }
        else
        {
            SignatureValidationException.fail(SignatureValidationFailure.FAILED_CERT_RETRIEVAL);
        }
        return null;
    }

    /**
     * Expires a cached certificate by setting it's epoch time to 0. If this corresponds to a cached
     * entry, it will not be removed immediately, to keep the expiration meter consistent (i.e. the
     * meter is incremented when an expired cert is first requested)
     * 
     * @param certificateId
     */
    public static void expireCachedCertificate(String certificateId)
    {
        if (certificates.containsKey(certificateId))
        {
            Pair<Certificate, Date> cachedCert = certificates
                            .get(certificateId);
            Date d = cachedCert.getValue1();
            d.setTime(0);
        }
    }

    public static void clearCache()
    {
        certificates.clear();
    }
}
