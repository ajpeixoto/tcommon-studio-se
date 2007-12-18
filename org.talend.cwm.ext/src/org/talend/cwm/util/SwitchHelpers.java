// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.cwm.util;

import org.talend.cwm.relational.TdCatalog;
import org.talend.cwm.relational.TdSchema;
import org.talend.cwm.relational.util.RelationalSwitch;

/**
 * @author scorreia
 * 
 * This class gives easy access to the correctly typed elements.
 */
public class SwitchHelpers {

    public static final RelationalSwitch<TdCatalog> CATALOG_SWITCH = new RelationalSwitch<TdCatalog>() {

        @Override
        public TdCatalog caseTdCatalog(TdCatalog object) {
            return object;
        }
    };

    public static final RelationalSwitch<TdSchema> SCHEMA_SWITCH = new RelationalSwitch<TdSchema>() {

        @Override
        public TdSchema caseTdSchema(TdSchema object) {
            return object;
        }

    };
}
