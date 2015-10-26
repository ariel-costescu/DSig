package config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import certificate.CertExternalCertificatePolicy;

public class Configuration
{
    private static Properties props;
    
    public static final String DEFAULT_CERT_URL =
                    "https://go.microsoft.com/fwlink/p/?linkid=246509&cid=[CertificateId]";
    public static final long DEFAULT_CERT_CACHE_EXPIRATION = 7;
    public static final long DEFAULT_CERT_TIMEOUT_MILLIS = 30000;
    public static final int DEFAULT_EXTERNAL_CERTIFICATE_POLICY = 
                    CertExternalCertificatePolicy.EXTERNAL_AND_INTERNAL.dbValue;
    
    static 
    {
        Properties defaults = new Properties();
        defaults.put("CERT_URL", DEFAULT_CERT_URL);
        defaults.put("CERT_CACHE_EXPIRATION", DEFAULT_CERT_CACHE_EXPIRATION);
        defaults.put("CERT_TIMEOUT_MILLIS", DEFAULT_CERT_TIMEOUT_MILLIS);
        defaults.put("EXTERNAL_CERTIFICATE_POLICY", DEFAULT_EXTERNAL_CERTIFICATE_POLICY);
        props = new Properties(defaults);
        String propsPath = System.getProperty("propsPath");
        if (propsPath != null)
        {
            try
            {
                props.load(new FileInputStream(propsPath));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public static String getCertUrl() 
    {
        return props.getProperty("CERT_URL");
    }
    
    public static long getCertCacheExpiration() 
    {
        long value = DEFAULT_CERT_CACHE_EXPIRATION;
        String prop = props.getProperty("CERT_CACHE_EXPIRATION");
        try 
        {
            value = Long.parseLong(prop);
        }
        catch(NumberFormatException e) {}
        return value;
    }
    
    public static long getCertTimeoutMillis() 
    {
        long value = DEFAULT_CERT_TIMEOUT_MILLIS;
        String prop = props.getProperty("CERT_TIMEOUT_MILLIS");
        try 
        {
            value = Long.parseLong(prop);
        }
        catch(NumberFormatException e) {}
        return value;
    }

    public static int getExternalCertificatePolicy()
    {
        int value = DEFAULT_EXTERNAL_CERTIFICATE_POLICY;
        String prop = props.getProperty("EXTERNAL_CERTIFICATE_POLICY");
        try 
        {
            value = Integer.parseInt(prop);
        }
        catch(NumberFormatException e) {}
        return value;
    }
}
