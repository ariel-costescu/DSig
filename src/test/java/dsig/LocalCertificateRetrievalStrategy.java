package dsig;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import org.junit.Assert;
import certificate.CertificateRetrievalStrategy;
import exception.SignatureValidationException;

public class LocalCertificateRetrievalStrategy implements CertificateRetrievalStrategy
{
    public static final String CERT_ID = "A656B9B1B3AA509EEA30222E6D5E7DBDA9822DCD";
    public static final String CERT_SOURCE =
        "-----BEGIN CERTIFICATE-----\n" +
        "MIIDFDCCAgCgAwIBAgIQrih3cQuSeL1CgpLFusfJsTAJBgUrDgMCHQUAMB8xHTAb\n" +
        "BgNVBAMTFElhcFJlY2VpcHRQcm9kdWN0aW9uMB4XDTEyMDIxNzAxMTYyNFoXDTM5\n" +
        "MTIzMTIzNTk1OVowHzEdMBsGA1UEAxMUSWFwUmVjZWlwdFByb2R1Y3Rpb24wggEi\n" +
        "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDb0CeltVqOOIJiwNGgAr7Z0K4r\n" +
        "AYsHCa1oSFPJXtokz134bi2neJ8bHIKAnT0kwa3xViUxwp3+OZd2t2PshDv0ucZ5\n" +
        "dus6WCnuAw/MHVAodgLQMqYiKeM7VTIi3S1s3iV/66Y8KP7jH3CmE2XCXOQae+bQ\n" +
        "UuyGsTit0ScU7+MofODoNhvONs54n/K1WVnct2wWBpn8GGAS+l2mzOF0jXbMSjtz\n" +
        "7wuK77GeydG+x9paLuHIyCso7tjOqv/lvol5IIX0VnC5G2vC6dWR6MkNL5FzLXns\n" +
        "SuQgoYEUZXPlXJhsmv6oyyenaP0PpYJZcCLLVi1L2hcVo8B2DIEg3I3t8ch/AgMB\n" +
        "AAGjVDBSMFAGA1UdAQRJMEeAEHGLK3BRpCWDa2vU50kI73ehITAfMR0wGwYDVQQD\n" +
        "ExRJYXBSZWNlaXB0UHJvZHVjdGlvboIQrih3cQuSeL1CgpLFusfJsTAJBgUrDgMC\n" +
        "HQUAA4IBAQC4jmOu0H3j7AwVBvpQzPMLBd0GTimBXmJw+nruE+0Hh/0ywGTFNE+K\n" +
        "cQ21L4v+IuP8iMh3lpOcPb23ucuaoNSdWi375/KxrW831dbh+goqCZP7mWbxpnSn\n" +
        "FnuV+R1VPsQjdS+0tg5gjDKNMSx/2fH8krLAkidJ7rvUNmtEWMeVNk0/ZM/ECino\n" +
        "bMSSwbqUuc9Qql9T1epe+xv34a6eek+m4W0VXnLSuKhQS5jdILsyeJWHROZF5mrh\n" +
        "3DQuS0Ll5FzKmJxHf0hyXAo03SSA+x3JphAU4oYbkE9nRTU1tR6iq1D9ZxfQmvzm\n" +
        "IbMfyJ/y89PLs/ewHopSK7vQmGFjfjIl\n" +
        "-----END CERTIFICATE-----";
    
    /**
     * Avoid spamming the MS Cert service while running tests, just use a local cert
     */
    @Override
    public Certificate retrieveCertificate(String certificateId) throws SignatureValidationException
    {
        if (certificateId.equals(CERT_ID))
        {
            try
            {
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                byte[] bytes = CERT_SOURCE.getBytes(Charset.forName("UTF-8"));
                InputStream certSource = new ByteArrayInputStream(bytes);
                Certificate cert = certFactory.generateCertificate(certSource);
                return cert;
            }
            catch (CertificateException e)
            {
                Assert.assertNull("Exception occured while initializing signUtil: " + e.getMessage(), e);
            }
        }
        return null;
    }
}