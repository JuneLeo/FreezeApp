package com.android.internal.view;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CursorAnchorInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;

public interface IInputMethodSession {
    void updateExtractedText(int token, ExtractedText text);

    void updateSelection(int oldSelStart, int oldSelEnd,
            int newSelStart, int newSelEnd,
            int candidatesStart, int candidatesEnd);

    void viewClicked(boolean focusChanged);

    void updateCursor(Rect newCursor);

    void displayCompletions(CompletionInfo[] completions);

    void appPrivateCommand(String action, Bundle data);

    void finishSession();

    void updateCursorAnchorInfo(CursorAnchorInfo cursorAnchorInfo);

    void removeImeSurface();

    void finishInput();

    void invalidateInput(EditorInfo editorInfo, IInputContext inputContext, int sessionId);
}