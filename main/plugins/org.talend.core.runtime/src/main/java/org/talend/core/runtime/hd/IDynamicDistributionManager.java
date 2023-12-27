// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.runtime.hd;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * DOC cmeng  class global comment. Detailled comment
 */
public interface IDynamicDistributionManager {

    public static final String USERS_DISTRIBUTIONS_ROOT_FOLDER = "dynamicDistributions"; //$NON-NLS-1$

    public static final String DISTRIBUTION_FILE_EXTENSION = "json"; //$NON-NLS-1$

    public String getUserStoragePath();

    public Collection<String> getPreferencePaths();

    public void reloadAllDynamicDistributions(IProgressMonitor monitor) throws Exception;

    public void reset(IProgressMonitor monitor);

    public boolean isLoaded();

    public void load(IProgressMonitor monitor, boolean resetModulesCache) throws Exception;

    public boolean isBuiltinDynamicDistribution(String dynamicDistributionId);

    public boolean isUsersDynamicDistribution(String dynamicDistributionId);

    public String getDynamicDistributionCacheVersion();
}
