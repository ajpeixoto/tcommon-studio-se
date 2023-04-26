// ============================================================================
//
// Copyright (C) 2006-2023 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.ui.swt.dialogs;

import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.custom.AbsBusinessHandler;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.EEditSelection;
import org.talend.commons.ui.swt.dialogs.ModelSelectionDialog.ESelectionType;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public class ModelSelectionBusinessHandler extends AbsBusinessHandler<ModelSelectionBusinessHandler> {

    private static final String UI_KEY = "ModelSelectionDialog";

    private Shell parentShell;

    private ESelectionType selectionType;

    private boolean isReadOnly;

    private EEditSelection optionValue;

    public ModelSelectionBusinessHandler(Shell parentShell, ESelectionType selectionType, boolean isReadOnly) {
        this.parentShell = parentShell;
        this.selectionType = selectionType;
        this.isReadOnly = isReadOnly;
    }

    @Override
    public String getUiKey() {
        return UI_KEY;
    }

    public Shell getParentShell() {
        return parentShell;
    }

    public void setParentShell(Shell parentShell) {
        this.parentShell = parentShell;
    }

    public ESelectionType getSelectionType() {
        return selectionType;
    }

    public void setSelectionType(ESelectionType selectionType) {
        this.selectionType = selectionType;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public EEditSelection getOptionValue() {
        return this.optionValue;
    }

    public void setOptionValue(EEditSelection optionValue) {
        this.optionValue = optionValue;
    }

}
