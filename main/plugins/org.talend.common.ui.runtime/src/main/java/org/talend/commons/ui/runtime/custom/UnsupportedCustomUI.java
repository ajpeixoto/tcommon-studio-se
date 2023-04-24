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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class UnsupportedCustomUI<T> extends AbstractCustomUI<T> {

    private static final String UI_KEY = "UnsupportedDialog";

    private String dialogName;

    private String message;

    private T model;

    public UnsupportedCustomUI(String dialogName, T model) {
        super(UI_KEY, true);
        this.dialogName = dialogName;
        this.model = model;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Exception().printStackTrace(pw);
        this.message = sw.toString();
    }

    public UnsupportedCustomUI(String dialogName, String message, T model) {
        super(UI_KEY, true);
        this.dialogName = dialogName;
        this.message = message;
        this.model = model;
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
    protected T collectDialogData() {
        return model;
    }

    @Override
    public T getModel() {
        return model;
    }

}
