package com.android.internal.view;

public interface IInputContext {
//    void getTextBeforeCursor(InputConnectionCommandHeader header, int length, int flags,
//            AndroidFuture future /* T=CharSequence */);
//
//    void getTextAfterCursor(InputConnectionCommandHeader header, int length, int flags,
//            AndroidFuture future /* T=CharSequence */);
//
//    void getCursorCapsMode(InputConnectionCommandHeader header, int reqModes,
//            AndroidFuture future /* T=Integer */);
//
//    void getExtractedText(InputConnectionCommandHeader header, ExtractedTextRequest request,
//            int flags, AndroidFuture future /* T=ExtractedText */);
//
//    void deleteSurroundingText(InputConnectionCommandHeader header, int beforeLength,
//            int afterLength);
//    void deleteSurroundingTextInCodePoints(InputConnectionCommandHeader header, int beforeLength,
//            int afterLength);
//
//    void setComposingText(InputConnectionCommandHeader header, CharSequence text,
//            int newCursorPosition);
//
//    void setComposingTextWithTextAttribute(InputConnectionCommandHeader header,
//                CharSequence text, int newCursorPosition, TextAttribute textAttribute);
//
//    void finishComposingText(InputConnectionCommandHeader header);
//
//    void commitText(InputConnectionCommandHeader header, CharSequence text,
//                int newCursorPosition);
//
//    void commitTextWithTextAttribute(InputConnectionCommandHeader header, CharSequence text,
//            int newCursorPosition, TextAttribute textAttribute);
//
//    void commitCompletion(InputConnectionCommandHeader header, CompletionInfo completion);
//
//    void commitCorrection(InputConnectionCommandHeader header, CorrectionInfo correction);
//
//    void setSelection(InputConnectionCommandHeader header, int start, int end);
//
//    void performEditorAction(InputConnectionCommandHeader header, int actionCode);
//
//    void performContextMenuAction(InputConnectionCommandHeader header, int id);
//
//    void beginBatchEdit(InputConnectionCommandHeader header);
//
//    void endBatchEdit(InputConnectionCommandHeader header);
//
//    void sendKeyEvent(InputConnectionCommandHeader header, KeyEvent event);
//
//    void clearMetaKeyStates(InputConnectionCommandHeader header, int states);
//
//    void performSpellCheck(InputConnectionCommandHeader header);
//
//    void performPrivateCommand(InputConnectionCommandHeader header, String action,
//            Bundle data);
//
//    void setComposingRegion(InputConnectionCommandHeader header, int start, int end);
//
//    void setComposingRegionWithTextAttribute(InputConnectionCommandHeader header, int start,
//            int end, TextAttribute textAttribute);
//
//    void getSelectedText(InputConnectionCommandHeader header, int flags,
//            AndroidFuture future /* T=CharSequence */);
//
//    void requestCursorUpdates(InputConnectionCommandHeader header, int cursorUpdateMode,
//            int imeDisplayId, AndroidFuture future /* T=Boolean */);
//
//    void requestCursorUpdatesWithFilter(InputConnectionCommandHeader header,
//                int cursorUpdateMode, int cursorUpdateFilter, int imeDisplayId,
//                 AndroidFuture future /* T=Boolean */);
//
//    void commitContent(InputConnectionCommandHeader header, InputContentInfo inputContentInfo,
//            int flags, Bundle opts, AndroidFuture future /* T=Boolean */);
//
//    void getSurroundingText(InputConnectionCommandHeader header, int beforeLength,
//            int afterLength, int flags, AndroidFuture future /* T=SurroundingText */);
//
//    void setImeConsumesInput(InputConnectionCommandHeader header, boolean imeConsumesInput);
}