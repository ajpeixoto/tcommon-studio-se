package org.talend.designer.maven.ui.setting.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class TalendMavenPreferenceInitializer extends AbstractPreferenceInitializer {

    private IPreferenceStore preferenceStore;

    @Override
    public void initializeDefaultPreferences() {

        IPreferenceStore store = getPreferenceStore();

        store.setDefault(MavenPreferenceConstants.P_OFFLINE, true);

    }

    private IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            // InstanceScope.INSTANCE added in 3.7
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, IMavenConstants.PLUGIN_ID);

        }
        return preferenceStore;
    }

}
