package com.android.internal.inputmethod;

import com.android.internal.view.IInputMethodSession;

public final class InputBindResult {

    public int result;

    /**
     * The input method service.
     */
    public IInputMethodSession method;

//    /**
//     * The accessibility services.
//     */
//    public SparseArray<IAccessibilityInputMethodSession> accessibilitySessions;
//
//    /**
//     * The input channel used to send input events to this IME.
//     */
//    public final InputChannel channel;

    /**
     * The ID for this input method, as found in InputMethodInfo; null if
     * no input method will be bound.
     */
    public String id;

    /**
     * Sequence number of this binding.
     */
    public int sequence;

    private float[] mVirtualDisplayToScreenMatrixValues;

    /**
     * {@code true} if the IME explicitly specifies {@code suppressesSpellChecker="true"}.
     */
    public boolean isInputMethodSuppressingSpellChecker;


}