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
package org.talend.repository.ui.wizards.metadata.connection.database;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.talend.commons.utils.platform.PluginChecker;
import org.talend.core.database.EDatabaseTypeName;
import org.talend.core.database.conn.template.EDatabaseConnTemplate;
import org.talend.core.model.metadata.builder.database.ExtractMetaDataFromDataBase;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.service.ITCKUIService;
import org.talend.core.ui.branding.IBrandingConfiguration;
import org.talend.metadata.managment.utils.MetadataConnectionUtils;
import org.talend.repository.metadata.Activator;

// TODO rename and refactor
public class DatabaseImageHelper {

    public static List<String> displayDBTypeList;

    public static List<String> getDisplayDBTypes() {
        if (displayDBTypeList != null) {
            return displayDBTypeList;
        }
        displayDBTypeList = EDatabaseConnTemplate.getDBTypeDisplay();
        // added by dlin for 21721, only a temporary approach to resolve it -begin
        IWorkbenchWindow workBenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (workBenchWindow != null) {
            IWorkbenchPage page = workBenchWindow.getActivePage();
            if (page != null) {
                String perId = page.getPerspective().getId();
                if (StringUtils.isNotEmpty(perId)) {
                    // TDQ-5801
                    if (perId.equalsIgnoreCase(IBrandingConfiguration.PERSPECTIVE_DI_ID)
                            || perId.equalsIgnoreCase(IBrandingConfiguration.PERSPECTIVE_DQ_ID)) {
                        displayDBTypeList.removeIf(dbType -> "Microsoft SQL Server 2005/2008".equalsIgnoreCase(dbType));
                    }
                }
            }
        }
        // added by dlin for 21721, only a temporary approach to resolve it -end
        if (PluginChecker.isOnlyTopLoaded()) {
            displayDBTypeList.retainAll(MetadataConnectionUtils.getTDQSupportDBTemplate());
        }
        // filter provider not loaded types
        displayDBTypeList.removeIf(dbType -> {
            EDatabaseTypeName type = EDatabaseTypeName.getTypeFromDisplayName(dbType);
            if (type != null && type.isUseProvider()) {
                String dbtypeString = type.getXmlName();
                if (dbtypeString != null && ExtractMetaDataFromDataBase.getProviderByDbType(dbtypeString) == null) {
                    return true;
                }
            }
            return false;
        });
        // filter unsupported types
        displayDBTypeList.removeIf(dbType -> !EDatabaseTypeName.getTypeFromDisplayName(dbType).isSupport());

        // TODO not working, it shows snowflake jdbc page but now compv0 page
        ERepositoryObjectType snowflakeType = ERepositoryObjectType.valueOf("snowflake");
        if (snowflakeType != null) {
            displayDBTypeList.add(snowflakeType.getLabel());
        }
        if (ITCKUIService.get() != null) {
            ERepositoryObjectType jdbcType = ITCKUIService.get().getTCKRepositoryType("JDBCNew");
            if (jdbcType != null) {
                displayDBTypeList.add(jdbcType.getLabel());
            }
        }

        loadImages();

        displayDBTypeList.sort((a1, a2) -> a1.compareTo(a2));

        return displayDBTypeList;
    }

    private static void loadImages() {
        displayDBTypeList.stream().map(DatabaseImageHelper::getDBImageName).forEach(dbType -> {
            ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.BUNDLE_ID,
                    "$nl$/icons/db40/" + dbType + ".png");
            if (descriptor != null) {
                JFaceResources.getImageRegistry().put(dbType, descriptor.createImage());
            }
        });
    }

    public static String getDBImageName(String displayDBType) {
        return StringUtils.replace(displayDBType, " ", "_").toLowerCase();
    }

}
