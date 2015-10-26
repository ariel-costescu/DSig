package certificate;

/**
 * Policy for determining how to chose the certificate when validating a digitally signed receipt
 */
public enum CertExternalCertificatePolicy
{
    /**
     * Allow only external certificates, as specified in the certificateId field of the receipt
     */
    EXTERNAL_ONLY(0),
    /**
     * Allow both external and internal cert
     */
    EXTERNAL_AND_INTERNAL(1),
    /**
     * Allow only internal certificate as specified in the KeyValue field of the signature
     */
    INTERNAL_ONLY(2);

    public int dbValue;

    CertExternalCertificatePolicy(int dbValue)
    {
        this.dbValue = dbValue;
    }

    public static CertExternalCertificatePolicy fromDbValue(int dbValue)
    {
        switch (dbValue)
        {
        case 0:
        {
            return EXTERNAL_ONLY;
        }
        case 1:
        {
            return EXTERNAL_AND_INTERNAL;
        }
        case 2:
        {
            return INTERNAL_ONLY;
        }
        default:
        {
            return EXTERNAL_ONLY;
        }
        }
    }
}
