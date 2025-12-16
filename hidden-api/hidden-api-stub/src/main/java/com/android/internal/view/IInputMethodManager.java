package com.android.internal.view;

import android.os.IBinder;
import android.os.ResultReceiver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodSubtype;
import android.window.ImeOnBackInvokedDispatcher;

import com.android.internal.inputmethod.IRemoteAccessibilityInputConnection;
import com.android.internal.inputmethod.InputBindResult;

import java.util.List;

public interface IInputMethodManager {
    void addClient(IInputMethodClient client, IInputContext inputContext,
                   int untrustedDisplayId);

    // TODO: Use ParceledListSlice instead
    List<InputMethodInfo> getInputMethodList(int userId);

    List<InputMethodInfo> getAwareLockedInputMethodList(int userId, int directBootAwareness);

    // TODO: Use ParceledListSlice instead
    List<InputMethodInfo> getEnabledInputMethodList(int userId);

    List<InputMethodSubtype> getEnabledInputMethodSubtypeList(String imiId,
                                                              boolean allowsImplicitlySelectedSubtypes);

    InputMethodSubtype getLastInputMethodSubtype();

    boolean showSoftInput(IInputMethodClient client, IBinder windowToken, int flags,
                          ResultReceiver resultReceiver, int reason);

    boolean hideSoftInput(IInputMethodClient client, IBinder windowToken, int flags,
                          ResultReceiver resultReceiver, int reason);

    // If windowToken is null, this just does startInput().  Otherwise this reports that a window
    // has gained focus, and if 'attribute' is non-null then also does startInput.
    // @NonNull
    InputBindResult startInputOrWindowGainedFocus(
            /* @StartInputReason */ int startInputReason,
                                    IInputMethodClient client, IBinder windowToken,
            /* @StartInputFlags */ int startInputFlags,
            /* @android.view.WindowManager.LayoutParams.SoftInputModeFlags */ int softInputMode,
                                    int windowFlags, EditorInfo attribute, IInputContext inputContext,
                                    IRemoteAccessibilityInputConnection remoteAccessibilityInputConnection,
                                    int unverifiedTargetSdkVersion, ImeOnBackInvokedDispatcher imeDispatcher);

    void showInputMethodPickerFromClient(IInputMethodClient client,
                                         int auxiliarySubtypeMode);

    void showInputMethodPickerFromSystem(IInputMethodClient client,
                                         int auxiliarySubtypeMode, int displayId);

    void showInputMethodAndSubtypeEnablerFromClient(IInputMethodClient client, String topId);

    boolean isInputMethodPickerShownForTest();

    InputMethodSubtype getCurrentInputMethodSubtype();

    void setAdditionalInputMethodSubtypes(String id, InputMethodSubtype[] subtypes);

    int getInputMethodWindowVisibleHeight(IInputMethodClient client);

    void reportVirtualDisplayGeometryAsync(IInputMethodClient parentClient,
                                           int childDisplayId, float[] matrixValues);

    void reportPerceptibleAsync(IBinder windowToken, boolean perceptible);

    /**
     * Remove the IME surface. Requires INTERNAL_SYSTEM_WINDOW permission.
     */
    void removeImeSurface();

    /**
     * Remove the IME surface. Requires passing the currently focused window.
     */
    void removeImeSurfaceFromWindowAsync(IBinder windowToken);

    void startProtoDump(byte[] protoDump, int source, String where);

    boolean isImeTraceEnabled();

    // Starts an ime trace.
    void startImeTrace();

    // Stops an ime trace.
    void stopImeTrace();

    /**
     * Start Stylus handwriting session
     **/
    void startStylusHandwriting(IInputMethodClient client);
}
