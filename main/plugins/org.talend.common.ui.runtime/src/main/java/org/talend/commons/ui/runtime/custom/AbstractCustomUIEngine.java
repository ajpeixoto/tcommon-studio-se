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
public abstract class AbstractCustomUIEngine implements ICustomUIEngine {

    public AbstractCustomUIEngine() {
        // nothing to do
    }

    @Override
    public void run(ICustomUI ui) {
        doRun(ui);
    }

    protected void doRun(ICustomUI ui) {
        ui.run();
    }

}
