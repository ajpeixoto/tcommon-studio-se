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
package org.talend.librariesmanager.utils;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.junit.Assert;
import org.junit.Test;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.general.ModuleToInstall;
import org.talend.core.runtime.maven.MavenConstants;

/**
 * created by wchen on 2016骞�2鏈�18鏃� Detailled comment
 *
 */
public class RemoteModulesHelperTest {

    /**
     * Test method for
     * {@link org.talend.librariesmanager.utils.RemoteModulesHelper#getNotInstalledModulesRunnable(java.util.List, java.util.List, boolean)}
     * .
     *
     * @throws InterruptedException
     * @throws InvocationTargetException
     */
    @Test
    public void testGetNotInstalledModulesRunnableListOfModuleNeededListOfModuleToInstallBoolean()
            throws InvocationTargetException, InterruptedException {
        List<ModuleNeeded> neededModules = new ArrayList<ModuleNeeded>();
        ModuleNeeded m1 = new ModuleNeeded("tMysqlInput", "mysql.jar", "", true, null, null,
                "mvn:org.talend.libraries/mysql/6.0.0");
        ModuleNeeded m2 = new ModuleNeeded("tMysqlInput", "mysql.jar", "", true, null, null,
                "mvn:org.talend.libraries/mysql/6.1.0");
        ModuleNeeded m3 = new ModuleNeeded("tMyComponent1", "test.jar", "", true, null, null,
                "mvn:org.talend.libraries/test/6.0.0");
        ModuleNeeded m4 = new ModuleNeeded("tMyComponent2", "test.jar", "", true, null, null,
                "mvn:org.talend.libraries/test/6.0.0");
        ModuleNeeded m5 = new ModuleNeeded("tMyComponent3", "test.exe", "", true, null, null,
                "mvn:org.talend.libraries/test/6.0.0");
        neededModules.add(m1);
        neededModules.add(m2);
        neededModules.add(m3);
        neededModules.add(m4);
        neededModules.add(m5);
        List<ModuleToInstall> toInstall1 = new ArrayList<ModuleToInstall>();
        IRunnableWithProgress notInstalledModulesRunnable = RemoteModulesHelper.getInstance()
                .getNotInstalledModulesRunnable(neededModules, toInstall1, false, false, false);
        notInstalledModulesRunnable.run(new NullProgressMonitor());
        assertEquals(4, toInstall1.size());

    }

    @Test
    public void testGetNotInstalledModulesRunnableListForModuleNameURL() throws InvocationTargetException, InterruptedException {
        List<ModuleNeeded> neededModules = new ArrayList<ModuleNeeded>();
        ModuleNeeded m1 = new ModuleNeeded("test", "protobuf-java-2.6.1.jar", "", true, null, null,
                "mvn:com.google.protobuf/protobuf-java/2.6.1");
        neededModules.add(m1);
        List<ModuleToInstall> toInstall1 = new ArrayList<ModuleToInstall>();
        IRunnableWithProgress notInstalledModulesRunnable = RemoteModulesHelper.getInstance()
                .getNotInstalledModulesRunnable(neededModules, toInstall1, false, false, false);
        notInstalledModulesRunnable.run(new NullProgressMonitor());
        assertEquals(1, toInstall1.size());
        Assert.assertEquals("protobuf-java-2.6.1.jar", toInstall1.get(0).getName());
        Assert.assertEquals("mvn:com.google.protobuf/protobuf-java/2.6.1/jar", toInstall1.get(0).getMavenUri());

        neededModules.clear();
        ModuleNeeded m2 = new ModuleNeeded("test", "protobuf-java.jar", "", true, null, null,
                "mvn:org.talend.libraries/protobuf-java/6.0.0");
        neededModules.add(m2);
        toInstall1 = new ArrayList<ModuleToInstall>();
        notInstalledModulesRunnable = RemoteModulesHelper.getInstance().getNotInstalledModulesRunnable(neededModules, toInstall1,
                false, false, false);
        notInstalledModulesRunnable.run(new NullProgressMonitor());
        assertEquals(1, toInstall1.size());
        Assert.assertEquals("protobuf-java.jar", toInstall1.get(0).getName());
        Assert.assertEquals("mvn:org.talend.libraries/protobuf-java/6.0.0/jar", toInstall1.get(0).getMavenUri());

    }
    
    
    @Test
    public void testGetManualInstallModulesRunnableListForModuleNameURL() throws InvocationTargetException, InterruptedException {
        List<ModuleNeeded> neededModules = new ArrayList<ModuleNeeded>();
        ModuleNeeded m1 = new ModuleNeeded("test", "tdgssconfig-16.20.00.02.jar", "", true, null, null,
                "mvn:com.teradata/tdgssconfig/16.20.00.02/jar");
        neededModules.add(m1);
        List<ModuleToInstall> toInstall1 = new ArrayList<ModuleToInstall>();
        IRunnableWithProgress notInstalledModulesRunnable = RemoteModulesHelper.getInstance()
                .getNotInstalledModulesRunnable(neededModules, toInstall1, false, false, false);
        notInstalledModulesRunnable.run(new NullProgressMonitor());
        assertEquals(1, toInstall1.size());
        Assert.assertEquals(MavenConstants.DOWNLOAD_MANUAL, toInstall1.get(0).getDistribution());
        
        
        neededModules = new ArrayList<ModuleNeeded>();
        m1 = new ModuleNeeded("test", "tdgssconfig-16.20.00.02.jar", "", true, null, null,
                "mvn:com.teradata/tdgssconfig/16.20.00.02/jar");
        neededModules.add(m1);
        m1 = new ModuleNeeded("test", "terajdbc4-16.20.00.02.jar", "", true, null, null,
                "mvn:com.teradata/terajdbc4/16.20.00.02/jar");
        neededModules.add(m1); 
        List<ModuleToInstall> toInstall2 = new ArrayList<ModuleToInstall>();
        notInstalledModulesRunnable = RemoteModulesHelper.getInstance()
                .getNotInstalledModulesRunnable(neededModules, toInstall2, false, false, false);
        notInstalledModulesRunnable.run(new NullProgressMonitor());        
        assertEquals(2, toInstall2.size());
        Assert.assertEquals(MavenConstants.DOWNLOAD_MANUAL, toInstall2.get(0).getDistribution());
        Assert.assertEquals(MavenConstants.DOWNLOAD_MANUAL, toInstall2.get(1).getDistribution());
    }
}
