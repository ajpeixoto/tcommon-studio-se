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

import org.eclipse.jface.dialogs.Dialog;
import org.talend.commons.ui.runtime.TalendUI;
import org.talend.commons.ui.runtime.TalendUI.IStudioRunnable;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public abstract class AbsBusinessHandler<T extends IBusinessHandler<?>> implements IBusinessHandler<T> {

    private Object openResult = Dialog.CANCEL;

    public AbsBusinessHandler() {
    }

    protected ICustomUI<T> getCustomUI() {
        return new UnsupportedCustomUI<T>((T) this, getUiKey());
    }

    @Override
    public T run(IStudioRunnable<T> studioRun) {
        return TalendUI.get().run(studioRun, getCustomUI());
    }

    @Override
    public T run(IStudioRunnable<T> studioRun, ICustomUI<T> stigmaUI) {
        return TalendUI.get().run(studioRun, stigmaUI);
    }

    @Override
    public boolean isModalDialog() {
        return true;
    }

    @Override
    public Object getOpenResult() {
        return openResult;
    }

    public void setOpenResult(Object openResult) {
        this.openResult = openResult;
    }

}
