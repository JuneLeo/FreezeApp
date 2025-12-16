package com.android.internal.inputmethod;

import android.view.inputmethod.EditorInfo;

public interface IAccessibilityInputMethodSession {
    void updateSelection(int oldSelStart, int oldSelEnd, int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd);

    void finishInput();

    void finishSession();

    void invalidateInput(EditorInfo editorInfo,
                         IRemoteAccessibilityInputConnection connection, int sessionId);
}