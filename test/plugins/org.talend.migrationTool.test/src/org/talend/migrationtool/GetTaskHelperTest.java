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
import org.talend.migration.IProjectMigrationTask;
import org.talend.migrationtool.model.GetTasksHelper;

/**
 * @author bhe created on Oct 16, 2023
 *
 */
public class GetTaskHelperTest {

    @Test
    public void testGetFakeLazyTasks() throws Exception {

        List<IProjectMigrationTask> lazyTasks = GetTasksHelper.getProjectTasks(null, true);

        Assert.assertFalse(lazyTasks == null || lazyTasks.isEmpty());

        IProjectMigrationTask found = null;
        for (IProjectMigrationTask t : lazyTasks) {
            if (StringUtils.equals(t.getId(), "org.talend.designer.components.localprovider.test.FakeLazyMigrationTask")) {
                found = t;
                break;
            }
        }

        Assert.assertNotNull(found);
        Assert.assertTrue(found.isLazy());
    }

}
