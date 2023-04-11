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
 * DOC cmeng  class global comment. Detailled comment
 */
public class MessageDialogBusinessHandler extends AbsBusinessHandler<MessageDialogBusinessHandler> {

    private static final String UI_KEY = "MessageDialog";

    private String title;

    private String message;

    private int dialogType;

    private boolean isModalDialog = true;

    public MessageDialogBusinessHandler(int dialogType) {
        super();
        this.dialogType = dialogType;
    }

    @Override
    protected ICustomUI<MessageDialogBusinessHandler> getCustomUI() {
        return new MessageDialogCustomUI(this);
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
