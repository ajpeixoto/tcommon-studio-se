// ============================================================================
//
// Copyright (C) 2006-2022 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven.ui.setting.preference;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.eclipse.m2e.core.internal.preferences.MavenPreferenceConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.designer.maven.ui.i18n.Messages;

/**
 * PCW
 */
public class TalendMavenPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private CustomBooleanFieldEditor m2eOffline;

    private Composite warningContainer;

    private StyledText warningMsgLabel;

    private Label warningImg;

    public Color warningColor = ColorUtils.getCacheColor(new RGB(255, 230, 217));

    private static final String M2E_OFFLINE = MavenPreferenceConstants.P_OFFLINE;

    private IPreferenceStore preferenceStore;

    public TalendMavenPreferencePage() {
        super(GRID);
        setPreferenceStore(getPreferenceStore());
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            // InstanceScope.INSTANCE added in 3.7
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, IMavenConstants.PLUGIN_ID);

        }
        return preferenceStore;
    }

    @Override
    public void init(IWorkbench workbench) {
        // nothing to do
    }

    @Override
    protected void initialize() {
        super.initialize();
        m2eOffline.setPreferenceStore(getPreferenceStore());
        m2eOffline.load();
    }

    @Override
    protected void createFieldEditors() {
        m2eOffline = new CustomBooleanFieldEditor(M2E_OFFLINE,
                Messages.getString("TalendMavenPreferencePage.maven.offline"), getFieldEditorParent());
        addField(m2eOffline);

        m2eOffline.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showWarning();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                // TODO Auto-generated method stub

            }

        });
    }

    @Override
    protected Control createContents(Composite parent) {
        super.createContents(parent);

        GridLayout layout = new GridLayout();
        GridData warningLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridData warningImgData = new GridData(0, SWT.TOP, false, true);
        warningContainer = new Composite(parent, SWT.None);
        layout = new GridLayout(2, false);
        warningContainer.setLayout(layout);
        warningLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        warningContainer.setLayoutData(warningLayoutData);
        // warningContainer.setBackground(warningColor);

        warningImg = new Label(warningContainer, SWT.None);
        warningImg.setImage(ImageProvider.getImage(EImage.WARNING_ICON));
        warningImg.setBackground(warningContainer.getBackground());
        warningImgData = new GridData(0, SWT.TOP, false, false);
        warningImg.setLayoutData(warningImgData);

        warningMsgLabel = new StyledText(warningContainer, SWT.WRAP);
        warningMsgLabel.setBackground(warningContainer.getBackground());
        String msg = Messages.getString("TalendMavenPreferencePage.maven.warn");
        warningMsgLabel.setText(msg);
        warningImgData = new GridData(0, SWT.TOP, false, false);
        warningMsgLabel.setLayoutData(warningImgData);
        StyleRange sr = new StyleRange();
        sr.fontStyle = SWT.BOLD;
        sr.foreground = warningColor;
        sr.length = warningMsgLabel.getLine(0).length();
        warningMsgLabel.setStyleRange(sr);

        showWarning();

        Composite hideComposite = new Composite(parent, SWT.None);
        hideComposite.setLayout(new FillLayout());

        return hideComposite;
    }

    private void showWarning() {
        boolean val = m2eOffline.getBooleanValue();
        warningContainer.setVisible(!val);
        GridData data = (GridData) warningContainer.getLayoutData();
        data.exclude = val;
        warningContainer.layout();
        warningContainer.getParent().layout();
    }

    public boolean performOk() {
        boolean oldV = getPreferenceStore().getBoolean(M2E_OFFLINE);
        super.performOk();
        if (oldV != m2eOffline.getBooleanValue()) {
            getPreferenceStore().putValue(M2E_OFFLINE, String.valueOf(m2eOffline.getBooleanValue()));
        }
        return true;
    }
    
    protected void performDefaults() {
        super.performDefaults();
        showWarning();
    }

    class CustomBooleanFieldEditor extends BooleanFieldEditor {

        public CustomBooleanFieldEditor(String name, String label, Composite parent) {
            super(name, label, parent);
        }

        public void addSelectionListener(SelectionListener l) {
            super.getChangeControl(getFieldEditorParent()).addSelectionListener(l);
        }

        public Button getChangeControl() {
            return super.getChangeControl(getFieldEditorParent());
        }
    }

}
