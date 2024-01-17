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
package org.talend.designer.maven.preference;

import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.junit.Test;

public class TalendMavenPreferenceTest {

    @Test
    public void testDefaultM2eOffline() {
        assertTrue(getPreferenceStore().getBoolean(MavenPreferenceConstants.P_OFFLINE));
    }

    private IPreferenceStore getPreferenceStore() {
        return new ScopedPreferenceStore(InstanceScope.INSTANCE, IMavenConstants.PLUGIN_ID);
    }
}
