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

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.custom.UnsupportedCustomUI.IUnsupportedDialogResult;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class UnsupportedCustomUI extends AbstractCustomUI<IUnsupportedDialogResult> {

    private static final String UI_KEY = "UnsupportedDialog";

    private String dialogName;

    private String message;

    public UnsupportedCustomUI(String dialogName, String message) {
        super(UI_KEY, true);
        this.dialogName = dialogName;
        this.message = message;
    }

    @Override
    protected IUIEvent createOpenEvent() {
        IUIEvent openEvent = super.createOpenEvent();
        Map<String, Object> params = openEvent.getParams();
        params.put(BuiltinParams.name.name(), this.dialogName);
        params.put(BuiltinParams.message.name(), this.message);
        return openEvent;
    }

    @Override
    protected IUnsupportedDialogResult getDialogData() {
        UnsupportedDialogResult result = new UnsupportedDialogResult();
        DefaultUIData uiData = new DefaultUIData("openResult", getId());
        try {
            result.openResult = requestUIData(uiData).get();
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return result;
    }

    public static interface IUnsupportedDialogResult {

        Object getOpenResult();

    }

    public static class UnsupportedDialogResult implements IUnsupportedDialogResult {

        private Object openResult;

        @Override
        public Object getOpenResult() {
            return openResult;
        }

        public void setOpenResult(Object openResult) {
            this.openResult = openResult;
        }

    }

}
