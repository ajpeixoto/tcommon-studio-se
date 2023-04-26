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

import org.talend.commons.ui.runtime.TalendUI.IStudioRunnable;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface IBusinessHandler<T extends IBusinessHandler<?>> {

    public static final int OK = 0;

    public static final int CANCEL = 1;

    String getUiKey();

    boolean isModalDialog();

    Object getOpenResult();

    T run(IStudioRunnable<T> studioRun);

    T run(IStudioRunnable<T> studioRun, ICustomUI<T> stigmaUI);

}
