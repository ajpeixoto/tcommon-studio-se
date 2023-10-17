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
package org.talend.migrationtool;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.properties.MigrationTask;
import org.talend.migration.IProjectMigrationTask;
import org.talend.migrationtool.model.GetTasksHelper;

/**
 * @author bhe created on Oct 16, 2023
 *
 */
public class GetTaskHelperTest {

    private static final String LAZY_TASK_ID = "org.talend.designer.components.localprovider.test.FakeLazyMigrationTask";

    @Test
    public void testGetFakeLazyTasks() throws Exception {

        List<IProjectMigrationTask> lazyTasks = GetTasksHelper.getProjectTasks(null, true);

        Assert.assertFalse(lazyTasks == null || lazyTasks.isEmpty());

        IProjectMigrationTask found = findLazyTask(lazyTasks);

        Assert.assertNotNull(found);
        Assert.assertTrue(found.isLazy());
    }

    public void testGetMigrationTasks() throws Exception {
        List<MigrationTask> migrationTasksBeforeLogon = GetTasksHelper.getMigrationTasks(true);
        List<MigrationTask> migrationTasksAfterLogon = GetTasksHelper.getMigrationTasks(false);

        MigrationTask foundLazy = findLazyTask(migrationTasksBeforeLogon);

        Assert.assertNull(foundLazy);

        foundLazy = findLazyTask(migrationTasksAfterLogon);

        Assert.assertNull(foundLazy);
    }

    public void testGetProjectTasks() throws Exception {
        List<IProjectMigrationTask> migrationTasksBeforeLogon = GetTasksHelper.getProjectTasks(true);
        List<IProjectMigrationTask> migrationTasksAfterLogon = GetTasksHelper.getProjectTasks(false);

        IProjectMigrationTask foundLazy = findLazyTask(migrationTasksBeforeLogon);

        Assert.assertNull(foundLazy);

        foundLazy = findLazyTask(migrationTasksAfterLogon);

        Assert.assertNull(foundLazy);
    }

    private <T> T findLazyTask(List<T> tasks) {
        T foundLazy = null;
        String taskId = null;
        for (T t : tasks) {
            if (t instanceof IProjectMigrationTask) {
                taskId = ((IProjectMigrationTask) t).getId();
            }
            if (StringUtils.equals(taskId, LAZY_TASK_ID)) {
                foundLazy = t;
                break;
            }
        }
        return foundLazy;
    }

}
