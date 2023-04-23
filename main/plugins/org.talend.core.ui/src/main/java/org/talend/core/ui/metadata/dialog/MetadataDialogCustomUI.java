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
package org.talend.core.ui.metadata.dialog;

import java.util.Map;

import org.talend.commons.ui.runtime.custom.AbstractCustomUI;
import org.talend.commons.ui.runtime.custom.IUIEvent;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.ui.metadata.dialog.MetadataDialogCustomUI.IMetadataDialogResult;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class MetadataDialogCustomUI extends AbstractCustomUI<IMetadataDialogResult> implements IMetadataDialog {

    private static final String UI_KEY = "MetadataDialog";

    private String title;

    private IMetadataTable outputMetaTable;

    public MetadataDialogCustomUI(IMetadataTable outputMetaTable) {
        super(UI_KEY, true);
        this.outputMetaTable = outputMetaTable;
    }

    @Override
    protected IUIEvent createOpenEvent() {
        IUIEvent openEvent = super.createOpenEvent();
        Map<String, Object> params = openEvent.getParams();
        params.put(BuiltinParams.title.name(), this.title);
        params.put("outputMetaTable", outputMetaTable);
        return openEvent;
    }

    @Override
    public void setText(String title) {
        this.title = title;
    }

    @Override
    public void setInputReadOnly(boolean readonly) {

    }

    @Override
    public void setOutputReadOnly(boolean readonly) {

    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public IMetadataTable getInputMetaData() {
        return null;
    }

    @Override
    public IMetadataTable getOutputMetaData() {
        return null;
    }

    @Override
    protected IMetadataDialogResult getDialogData() {
        return null;
    }

    public static interface IMetadataDialogResult {

        Object getOpenResult();

    }

    public static class MetadataDialogResult implements IMetadataDialogResult {

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
