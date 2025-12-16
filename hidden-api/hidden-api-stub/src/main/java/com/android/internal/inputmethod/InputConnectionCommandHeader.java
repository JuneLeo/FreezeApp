package com.android.internal.inputmethod;

public final class InputConnectionCommandHeader {
    /**
     * An identifier that is to be used when multiplexing multiple sessions into a single
     * {@link com.android.internal.view.IInputContext}.
     *
     * <p>This ID is considered to belong to an implicit namespace defined for each
     * {@link com.android.internal.view.IInputContext} instance.  Uniqueness of the session ID
     * across multiple instances of {@link com.android.internal.view.IInputContext} is not
     * guaranteed unless explicitly noted in a higher layer.</p>
     */
    public int mSessionId;


}