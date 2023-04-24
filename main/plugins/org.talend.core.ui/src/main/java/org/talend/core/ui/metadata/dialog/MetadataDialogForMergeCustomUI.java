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

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.custom.AbstractCustomUI;
import org.talend.commons.ui.runtime.custom.IUIEvent;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class MetadataDialogForMergeCustomUI extends AbstractCustomUI<IMetadataDialogForMerge> implements IMetadataDialogForMerge {

    private static final String UI_KEY = "MetadataDialog";

    private String title;

    private IMetadataTable outputMetaTable;

    private Object openResult;

    public MetadataDialogForMergeCustomUI(IMetadataTable outputMetaTable) {
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
    protected IMetadataDialog collectDialogData() {
        CompletableFuture<Object> openResultRequest = requestUIData(createUIDataEvent("openResult"));
        CompletableFuture<Object> outputMetaDataRequest = requestUIData(createUIDataEvent("output"));
        try {
            openResult = openResultRequest.get();
            List<Object> output = (List<Object>) outputMetaDataRequest.get();
//            Object outputObj = this.getUIEngine().readJson(output.toString());
//            List objList = this.getUIEngine().convertValue(outputObj, List.class);
            List<IMetadataColumn> listColumns = outputMetaTable.getListColumns();
            Class<? extends IMetadataColumn> clazz = listColumns.get(0).getClass();
            for (int i = 0; i < output.size(); i++) {
                Object obj = output.get(i);
                IMetadataColumn convertValue = this.getUIEngine().convertValue(obj, clazz);
                IMetadataColumn originalColumn = null;
                if (i < listColumns.size()) {
                    originalColumn = listColumns.get(i);
                } else {
                    originalColumn = new MetadataColumn();
                    listColumns.add(originalColumn);
                }
                originalColumn.updateWith(convertValue);
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

        return this;
    }

    @Override
    public int open() {
        return 0;
    }

    @Override
    public int getOpenResult() {
        return 0;
    }

    @Override
    public IMetadataTable getInputMetaData() {
        return null;
    }

    @Override
    public IMetadataTable getOutputMetaData() {
        return outputMetaTable;
    }

    @Override
    public IMetadataDialog getModel() {
        return this;
    }

}
