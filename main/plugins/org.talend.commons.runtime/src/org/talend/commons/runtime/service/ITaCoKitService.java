// ============================================================================
//
// Copyright (C) 2006-2021 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.commons.runtime.service;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.talend.commons.exception.CommonExceptionHandler;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.i18n.internal.Messages;


/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface ITaCoKitService {

    void start();

    boolean isStarted() throws Exception;

    void waitForStart();

    String reload(IProgressMonitor monitor) throws Exception;

    boolean isTaCoKitCar(File file, IProgressMonitor monitor) throws Exception;

    boolean isNeedMigration(String componentName, Map<String, String> properties);

    boolean isTaCoKitType(Object repoType);
    
    boolean isTaCoKitRepositoryNode(Object node);
    
    boolean isTaCoKitConnection(Object conn);

    Object getDatastoreFromDataset(Object repositoryViewObject);

    String getParentItemIdFromItem(Object Item);

    boolean isValueSelectionParameter(Object parameter);

    List<Map<String, Object>> convertToTable(String value);
    
    List<String> getValuesFromTableParameter(Object parameter, String... keys);
    
    List<String> getValuesFromTableParameterValue(String value, String ...keys);

    public static ITaCoKitService getInstance() {
        BundleContext bc = FrameworkUtil.getBundle(ITaCoKitService.class).getBundleContext();
        Collection<ServiceReference<ITaCoKitService>> tacokitServices = Collections.emptyList();
        try {
            tacokitServices = bc.getServiceReferences(ITaCoKitService.class, null);
        } catch (InvalidSyntaxException e) {
            CommonExceptionHandler.process(e);
        }

        if (tacokitServices != null) {
            if (1 < tacokitServices.size()) {
                ExceptionHandler.process(new Exception(
                        Messages.getString("ITaCoKitService.exception.multipleInstance", ITaCoKitService.class.getName()))); //$NON-NLS-1$
            }
            for (ServiceReference<ITaCoKitService> sr : tacokitServices) {
                ITaCoKitService tacokitService = bc.getService(sr);
                if (tacokitService != null) {
                    return tacokitService;
                }
            }
        }
        return null;
    }

}
