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
package org.talend.core.model.migration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.talend.commons.utils.VersionUtils;
import org.talend.core.IService;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.MigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.migration.MigrationTaskExtensionEPReader;
import org.talend.core.model.repository.ERepositoryObjectType;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 *
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 *
 */
public interface IMigrationToolService extends IService {
    
    public void executeWorspaceTasks();

    public void initNewProjectTasks(Project project);

    public boolean needExecutemigration();

    public void executeMigration(boolean pluginModel);

    public void executeMigrationTasksForLogon(Project project, boolean beforeLogon, IProgressMonitor monitorWrap);

    /**
     * DOC ycbai Comment method "executeMigrationTasksForImport".
     *
     * @param project
     * @param item
     * @param migrationTasksToApply
     * @param monitor
     * @throws Exception
     */
    public void executeMigrationTasksForImport(Project project, Item item, List<MigrationTask> migrationTasksToApply,
            final IProgressMonitor monitor) throws Exception;

    /**
     * DOC ycbai Comment method "checkMigrationTasks".
     *
     * @param project
     * @return
     */
    public boolean checkMigrationTasks(org.talend.core.model.properties.Project project);

    /**
     * DOC ycbai Comment method "updateMigrationSystem".
     *
     * @param project
     * @param persistence
     */
    public void updateMigrationSystem(org.talend.core.model.properties.Project project, boolean persistence);

    public String getTaskId();
    
    /**
     * Execute lazy migrations for given item
     * @param project item's project
     * @param item given item
     */
    public void executeLazyMigrations(Project project, Item item) throws Exception;
    
    /**
     * Validate all of lazy migration tasks whether lazy migration task was performed on other types of item except
     * process items.
     * 
     * @return
     */
    public Set<String> validateLazyMigrations();
    
    public static final String SYS_PROP_CHECK_LAZY_MIGRATIONS = "lazyMigrationCheck";
    
    public static final String SYS_PROP_CHECK_LAZY_MIGRATIONS_DEFAULT = "true";

    public static final String SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS = "execOldTaskAsLazy";

    public static final String SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_DEFAULT = "true";
    
    public static final String SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_BREAKS = "execOldTaskAsLazyBreaks";
    
    public static final String SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_BREAKS_DEFAULT = "8.0.0";
    
    public static final MigrationTaskExtensionEPReader MIGRATION_TASK_EXT_READER = new MigrationTaskExtensionEPReader();

    public static boolean checkLazyMigrations() {
        String val = System.getProperty(SYS_PROP_CHECK_LAZY_MIGRATIONS, SYS_PROP_CHECK_LAZY_MIGRATIONS_DEFAULT);
        return Boolean.parseBoolean(val);
    }

    public static boolean execOldTaskAsLazy() {
        String val = System.getProperty(SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS, SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_DEFAULT);
        return Boolean.parseBoolean(val);
    }
    
    public static String getExecOldTaskAsLazyBreaks() {
        return System.getProperty(SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_BREAKS, SYS_PROP_EXEC_OLD_AS_LAZY_MIGRATIONS_BREAKS_DEFAULT);
    }
    
    public static boolean isLazyTypes(Collection<ERepositoryObjectType> types) {
        Set<ERepositoryObjectType> ts = new HashSet<ERepositoryObjectType>(types);
        Set<ERepositoryObjectType> extTypes = MIGRATION_TASK_EXT_READER.getObjectTypeExtensions(ERepositoryObjectType.getAllTypesOfProcess2());
        ts.removeAll(ERepositoryObjectType.getAllTypesOfProcess2());
        ts.removeAll(extTypes);
        return ts.size() == 0;
    }
    
    public static boolean containLazyTypes(Collection<ERepositoryObjectType> types) {
        Set<ERepositoryObjectType> ts = new HashSet<ERepositoryObjectType>(types);
        List<ERepositoryObjectType> lts = ERepositoryObjectType.getAllTypesOfProcess2();
        for (ERepositoryObjectType t : lts) {
            if (ts.contains(t)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean canRunAsLazy(IProjectMigrationTask t) {
        if (t instanceof AbstractItemMigrationTask) {
            if (VersionUtils.compareTo(t.getBreaks(), getExecOldTaskAsLazyBreaks()) >= 0 && containLazyTypes(((AbstractItemMigrationTask) t).getTypes())) {
                return true;
            }
        }
        return false;
    }

}
