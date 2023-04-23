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

import org.talend.core.model.metadata.IMetadataTable;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface IMetadataDialog {

    void setText(String title);

    void setInputReadOnly(boolean readonly);

    void setOutputReadOnly(boolean readonly);

    int open();

    IMetadataTable getInputMetaData();

    IMetadataTable getOutputMetaData();

}
