// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.ui.runtime.custom;

/**
 * DOC cmeng class global comment. Detailled comment
 */
public class MessageDialogBusinessHandler extends AbsBusinessHandler<MessageDialogBusinessHandler> {

    /**
     * Constant for no image (value 0).
     *
     * @see #MessageDialog(Shell, String, Image, String, int, int, String...)
     */
    public static final int NONE = 0;

    /**
     * Constant for the error image, or a simple dialog with the error image and
     * a single OK button (value 1).
     *
     * @see #MessageDialog(Shell, String, Image, String, int, int, String...)
     * @see #open(int, Shell, String, String, int)
     */
    public static final int ERROR = 1;

    /**
     * Constant for the info image, or a simple dialog with the info image and a
     * single OK button (value 2).
     *
     * @see #MessageDialog(Shell, String, Image, String, int, int, String...)
     * @see #open(int, Shell, String, String, int)
     */
    public static final int INFORMATION = 2;

    /**
     * Constant for the question image, or a simple dialog with the question
     * image and Yes/No buttons (value 3).
     *
     * @see #MessageDialog(Shell, String, Image, String, int, int, String...)
     * @see #open(int, Shell, String, String, int)
     */
    public static final int QUESTION = 3;

    /**
     * Constant for the warning image, or a simple dialog with the warning image
     * and a single OK button (value 4).
     *
     * @see #MessageDialog(Shell, String, Image, String, int, int, String...)
     * @see #open(int, Shell, String, String, int)
     */
    public static final int WARNING = 4;

    /**
     * Constant for a simple dialog with the question image and OK/Cancel buttons (value 5).
     *
     * @see #open(int, Shell, String, String, int)
     * @since 3.5
     */
    public static final int CONFIRM = 5;

    /**
     * Constant for a simple dialog with the question image and Yes/No/Cancel buttons (value 6).
     *
     * @see #open(int, Shell, String, String, int)
     * @since 3.5
     */
    public static final int QUESTION_WITH_CANCEL = 6;

    private static final String UI_KEY = "MessageDialog";

    private String title;

    private String message;

    private int dialogType;

    private boolean isModalDialog = true;

    public MessageDialogBusinessHandler(int dialogType) {
        super();
        this.dialogType = dialogType;
    }

    public int getDialogType() {
        return dialogType;
    }

    public void setDialogType(int dialogType) {
        this.dialogType = dialogType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String getUiKey() {
        return UI_KEY;
    }

    @Override
    public boolean isModalDialog() {
        return this.isModalDialog;
    }

    public void setModalDialog(boolean modal) {
        this.isModalDialog = modal;
    }

}
