package exception;

public enum SignatureValidationFailure
{
    NOT_A_VALID_XML("Not a valid XML document."), 
    MISSING_SIGNATURE_ELEM("Missing signature element."), 
    FAILED_UNMARSHALLING("Failed to unmarshall signature."), 
    FAILED_VALIDATION("Failed to validate signature."), 
    FAILED_INIT("Failed to initialize XMLSignatureUtil."), 
    FAILED_CERT_RETRIEVAL("Failed to retrieve certificate."), 
    MISSING_CERT_ID("Missing certificate id."), 
    WRONG_RETRIEVAL_POLICY("Wrong certificate retrieval policy"), 
    NOT_A_RECEIPT("Not a valid receipt."), 
    UNKNOWN_ERROR("Unkown error.");

    private String message;

    private SignatureValidationFailure(final String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return message;
    }

}
