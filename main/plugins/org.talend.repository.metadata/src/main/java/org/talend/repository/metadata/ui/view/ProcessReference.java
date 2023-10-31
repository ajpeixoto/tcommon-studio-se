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
package org.talend.repository.metadata.ui.view;

import org.talend.core.model.properties.Property;

public class ProcessReference extends AbsReference {

    private Property processProp;

    public ProcessReference(Property processProp) {
        this.processProp = processProp;
    }

    @Override
    public String getName() {
        return processProp.getLabel();
    }

}
