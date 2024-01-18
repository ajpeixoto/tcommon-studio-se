// ============================================================================
//
// Copyright (C) 2006-2024 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.core.prefs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.talend.commons.exception.ExceptionHandler;

/**
 * DOC OTS  class global comment. Detailled comment
 */
public class HistoryStoreHelper {

    private static final HistoryStoreHelper instance = new HistoryStoreHelper();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private FutureTask<String> cleanTask;

    // for performance, clean in every 10 times for item import and migration
    private int delayCount = 0;

    private HistoryStoreHelper() {
    }

    public static HistoryStoreHelper getInstance() {
        return instance;
    }

    public void checkAndClean() {
        checkAndClean(true);
    }

    public void checkAndClean(boolean delay) {
        delayCount++;
        if ((delay && delayCount % 10 != 0) || isCleanTaskRunning()) {
            return;
        }

        try {
            cleanTask = new FutureTask<String>(() -> {
                try {
                    IWorkspace workspace = ResourcesPlugin.getWorkspace();
                    if (workspace instanceof Workspace) {
                        ((Workspace) workspace).getFileSystemManager().getHistoryStore().clean(new NullProgressMonitor());
                    }
                } catch (Exception e) {
                    ExceptionHandler.process(e);
                }
            }, null);
            executor.execute(cleanTask);
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }

    }

    public boolean isCleanTaskRunning() {
        return cleanTask != null && !cleanTask.isDone();
    }

    public void clearCache() {
        delayCount = 0;
    }

}
