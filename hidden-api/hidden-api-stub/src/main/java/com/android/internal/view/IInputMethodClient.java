package com.android.internal.view;

import com.android.internal.inputmethod.InputBindResult;

public interface IInputMethodClient {
    void onBindMethod(InputBindResult res);

    void onBindAccessibilityService(InputBindResult res, int id);

    void onUnbindMethod(int sequence, int unbindReason);

    void onUnbindAccessibilityService(int sequence, int id);

    void setActive(boolean active, boolean fullscreen, boolean reportToImeController);

    void scheduleStartInputIfNecessary(boolean fullscreen);

    void reportFullscreenMode(boolean fullscreen);

    void updateVirtualDisplayToScreenMatrix(int bindSequence, float[] matrixValues);

    void setImeTraceEnabled(boolean enabled);

    void throwExceptionFromSystem(String message);
}