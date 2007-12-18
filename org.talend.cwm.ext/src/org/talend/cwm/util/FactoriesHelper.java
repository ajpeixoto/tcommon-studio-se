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

import java.util.ArrayList;
import java.util.List;

import org.talend.cwm.relational.RelationalPackage;
import org.talend.cwm.softwaredeployment.SoftwaredeploymentPackage;
import orgomg.cwm.foundation.softwaredeployment.impl.SoftwaredeploymentFactoryImpl;
import orgomg.cwm.foundation.typemapping.TypemappingPackage;
import orgomg.cwm.foundation.typemapping.impl.TypemappingFactoryImpl;
import orgomg.cwm.objectmodel.core.CorePackage;
import orgomg.cwm.objectmodel.core.impl.CoreFactoryImpl;
import orgomg.cwm.resource.relational.impl.RelationalFactoryImpl;

/**
 * @author scorreia
 */
public class FactoriesHelper {

    /**
     * Method "initializeAllFactories" calls static method init() for each of the factories in this project.
     */
    public static void initializeAllFactories() {
        // --- talend extension packages
        SoftwaredeploymentFactoryImpl.init();
        RelationalFactoryImpl.init();

        // CWM generated packages
        // TODO scorreia add other factories
        CoreFactoryImpl.init();
        TypemappingFactoryImpl.init();
    }

    /**
     * Method "getExtensions".
     * 
     * @return the list of file extensions
     */
    public static List<String> getExtensions() {
        List<String> extensions = new ArrayList<String>();
        // --- Talend extension packages
        extensions.add(SoftwaredeploymentPackage.eNAME);
        extensions.add(RelationalPackage.eNAME);

        // --- CWM generated packages
        extensions.add(CorePackage.eNAME);
        extensions.add(TypemappingPackage.eNAME);
        // TODO scorreia add other file extensions
        return extensions;
    }
}
