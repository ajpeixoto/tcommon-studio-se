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

import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class MessageDialogCustomUI extends AbstractCustomUI<IMessageDialogResult> implements IMessageDialogResult {

    private static final String UI_KEY = "MessageDialog";

    private static final String OK = "ok";

    private static final String CANCEL = "cancel";

    private int dialogType = MessageDialog.NONE;

    private String title;

    private String message;

    private Object openResult;

    public MessageDialogCustomUI(int dialogType, String title, String message) {
        super(UI_KEY, true);
        this.dialogType = dialogType;
        this.title = title;
        this.message = message;
    }

    @Override
    protected IUIEvent createOpenEvent() {
        IUIEvent openEvent = super.createOpenEvent();
        Map<String, Object> params = openEvent.getParams();
        params.put(BuiltinParams.title.name(), this.title);
        params.put(BuiltinParams.message.name(), this.message);
        params.put("dialogType", mapDialogType(dialogType));
        return openEvent;
    }

    private String mapDialogType(int type) {
        switch (type) {
        case MessageDialog.CONFIRM:
            return "confirm";
        case MessageDialog.ERROR:
            return "error";
        case MessageDialog.INFORMATION:
            return "info";
        case MessageDialog.QUESTION:
            return "question";
        case MessageDialog.QUESTION_WITH_CANCEL:
            return "questionWithCancel";
        case MessageDialog.WARNING:
            return "warning";
        default:
            return "none";
        }
    }

    @Override
    protected IMessageDialogResult collectDialogData() {
        DefaultUIData uiData = createUIDataEvent("openResult");
        try {
            openResult = requestUIData(uiData).get();
            openResult = mapOpenResult(openResult);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return this;
    }

    private Object mapOpenResult(Object data) {
        Object result = data;
        switch (dialogType) {
        case MessageDialog.CONFIRM:
        case MessageDialog.ERROR:
        case MessageDialog.INFORMATION:
        case MessageDialog.WARNING:
            if (OK.equals(data)) {
                result = Boolean.TRUE;
            } else {
                result = Boolean.FALSE;
            }
            break;
        case MessageDialog.QUESTION:
            if (OK.equals(data)) {
                result = Boolean.TRUE;
            } else {
                result = Boolean.FALSE;
            }
            break;
        case MessageDialog.QUESTION_WITH_CANCEL:
            if (OK.equals(data)) {
                result = Boolean.TRUE;
            } else {
                result = Boolean.FALSE;
            }
            break;
        default:
            break;
        }
        return result;
    }

    @Override
    public IMessageDialogResult getModel() {
        return this;
    }

    @Override
    public Object getOpenResult() {
        return openResult;
    }

}
