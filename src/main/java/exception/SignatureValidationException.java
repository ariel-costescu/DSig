package exception;

@SuppressWarnings("serial")
public class SignatureValidationException extends Exception
{

    private SignatureValidationFailure reason;

    public SignatureValidationFailure getReason()
    {
        return reason;
    }

    private SignatureValidationException(SignatureValidationFailure reason)
    {
        super(reason.getMessage());
        this.reason = reason;
    }

    private SignatureValidationException(SignatureValidationFailure reason,
                                         Throwable cause)
    {
        super(reason.getMessage(), cause);
        this.reason = reason;
    }

    /**
     * Throw a checked exception
     */
    public static void fail(SignatureValidationFailure reason)
                    throws SignatureValidationException
    {
        SignatureValidationException e = new SignatureValidationException(reason);
        throw e;
    }

    /**
     * Throw a checked exception
     */
    public static void fail(SignatureValidationFailure reason, Throwable root)
                    throws SignatureValidationException
    {
        SignatureValidationException e = new SignatureValidationException(reason, root);
        throw e;
    }

}
