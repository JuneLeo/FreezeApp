package com.android.internal.inputmethod;

import android.view.KeyEvent;
import android.view.inputmethod.TextAttribute;

import com.android.internal.infra.AndroidFuture;

public interface IRemoteAccessibilityInputConnection {
    void commitText(InputConnectionCommandHeader header, CharSequence text,
                    int newCursorPosition, TextAttribute textAttribute);

    void setSelection(InputConnectionCommandHeader header, int start, int end);

    void getSurroundingText(InputConnectionCommandHeader header, int beforeLength,
                            int afterLength, int flags, AndroidFuture future /* T=SurroundingText */);

    void deleteSurroundingText(InputConnectionCommandHeader header, int beforeLength,
                               int afterLength);

    void sendKeyEvent(InputConnectionCommandHeader header, KeyEvent event);

    void performEditorAction(InputConnectionCommandHeader header, int actionCode);

    void performContextMenuAction(InputConnectionCommandHeader header, int id);

    void getCursorCapsMode(InputConnectionCommandHeader header, int reqModes,
                           AndroidFuture future /* T=Integer */);

    void clearMetaKeyStates(InputConnectionCommandHeader header, int states);
}