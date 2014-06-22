// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.model.metadata.builder.database;

import java.sql.SQLException;

import org.talend.core.model.metadata.builder.util.MetadataConnectionUtils;
import org.talend.cwm.softwaredeployment.TdSoftwareSystem;
import orgomg.cwm.foundation.typemapping.TypeSystem;

/**
 * @author scorreia
 * 
 * This class create softwaredeployment classes from a connection.
 */
public class SoftwareSystemBuilder extends CwmBuilder {

    private final TdSoftwareSystem softwareSystem;

    private TypeSystem typeSystem;

    // public SoftwareSystemBuilder(Connection conn) throws SQLException {
    // super(conn);
    // softwareSystem = initializeSoftwareSystem();
    // }
    public SoftwareSystemBuilder(org.talend.core.model.metadata.builder.connection.Connection conn) throws SQLException {
        super(conn);
        softwareSystem = initializeSoftwareSystem();
    }

    private TdSoftwareSystem initializeSoftwareSystem() throws SQLException {
        TdSoftwareSystem system = MetadataConnectionUtils.getSoftwareSystem(connection);
        // TODO scorreia uncomment this part when we know where to save the typeSystem.
        // this.typeSystem = DatabaseContentRetriever.getTypeSystem(connection);
        //
        // // --- add type systems: softwareSystem.getTypespace()
        // system.getTypespace().add(typeSystem);

        return system;
    }

    public TdSoftwareSystem getSoftwareSystem() {
        return this.softwareSystem;
    }

    public TypeSystem getTypeSystem() {
        return this.typeSystem;
    }

}
