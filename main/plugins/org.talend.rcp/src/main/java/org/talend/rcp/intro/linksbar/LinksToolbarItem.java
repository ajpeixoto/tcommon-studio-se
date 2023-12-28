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
package org.talend.rcp.intro.linksbar;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.PluginChecker;
import org.talend.core.model.utils.TalendPropertiesUtil;
import org.talend.core.service.IExchangeService;
import org.talend.core.service.ITutorialsService;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.rcp.Activator;
import org.talend.rcp.i18n.Messages;

/**
 * DOC xtan class global comment. Detailled comment <br/>
 *
 */
public class LinksToolbarItem extends ContributionItem {

    private ToolItem toolitem;

    public static final String LEARN_URL = "<a href=\"https://help.talend.com\">Learn</a>"; //$NON-NLS-1$
    public static final String LEARN_ORIG_URL="https://help.talend.com";

    public static final String ASK_URL = "<a href=\"https://community.talend.com/\">Ask</a>"; //$NON-NLS-1$
    public static final String ASK_ORIG_URL ="https://community.talend.com";

    // private static final String SHARE_URL = "<a href=\"http://www.talendforge.org/exchange/\">Share</a>"; //$NON-NLS-1$

    public static final String EXCHANGE_URL = "<a href=\"http://www.talendforge.org/exchange/index.php\">Exchange</a>"; //$NON-NLS-1$
    public static final String EXCHANGE_ORIG_URL ="http://www.talendforge.org/exchange/index.php";

    public static final String VIDEOS_URL = "<a href=\"https://www.talendforge.org/tutorials\">Videos</a>"; //$NON-NLS-1$
    public static final String VIDEOS_ORIG_URL = "https://www.talendforge.org/tutorials";

    public static final String CLOUD_URL = "<a href=\"https://www.talend.com/free-trial/?utm_campaign=tos_di&utm_medium=studio&utm_source=toolbar&utm_campaign=dynamic_acronym\">Cloud</a>"; //$NON-NLS-1$
    public static final String CLOUD_ORIG_URL="https://www.talend.com/free-trial/?utm_campaign=tos_di&utm_medium=studio&utm_source=toolbar&utm_campaign=dynamic_acronym";

    protected static ImageRegistry registry = new ImageRegistry();

    public static final String COOLITEM_LINKS_ID = Activator.PLUGIN_ID + ".CoolItemLinks"; //$NON-NLS-1$

    @Override
    public void fill(ToolBar parent, int index) {
        // parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));

        toolitem = new ToolItem(parent, SWT.SEPARATOR, index);
        Control control = createControl(parent);
        toolitem.setControl(control);
        toolitem.setData(this);
        toolitem.setWidth(control.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
        toolitem.addListener(SWT.Dispose, new Listener() {

            @Override
            public void handleEvent(Event event) {
                if (event.type == SWT.Dispose) {
                    dispose();
                }
            }

        });
    }

    protected Control createControl(Composite parent) {

        final Composite composite = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(!PluginChecker.isTIS() ? 10 : 8, false);
        layout.marginHeight = 0;
        composite.setLayout(layout);

        // 1.learn
        Label learnLabel = new Label(composite, SWT.NONE);

        if (registry.get("demo") == null) { //$NON-NLS-1$
            registry.put("demo", Activator.getImageDescriptor("icons/demo.png").createImage()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        learnLabel.setImage(registry.get("demo")); //$NON-NLS-1$

        Link learn = new Link(composite, SWT.NONE);
        GridData learnGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        learnLabel.setLayoutData(learnGd);
        learn.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        learn.setText(LEARN_URL);
        learn.setToolTipText(Messages.getString("LinksToolbarItem_Learn")); //$NON-NLS-1$

        learn.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                openBrower(event.text);
            }
        });

        // 2.ask
        Label askLabel = new Label(composite, SWT.NONE);

        if (registry.get("protocol") == null) { //$NON-NLS-1$
            registry.put("protocol", Activator.getImageDescriptor("icons/irc_protocol.png").createImage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        askLabel.setImage(registry.get("protocol")); //$NON-NLS-1$

        Link ask = new Link(composite, SWT.NONE);
        GridData askGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        askLabel.setLayoutData(askGd);
        ask.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        ask.setText(ASK_URL);
        ask.setToolTipText(Messages.getString("LinksToolbarItem_7")); //$NON-NLS-1$

        ask.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                openBrower(event.text);
            }
        });

        // 4.videos
        Label videosLabel = new Label(composite, SWT.NONE);

        if (registry.get("videos") == null) { //$NON-NLS-1$
            registry.put("videos", Activator.getImageDescriptor("icons/videos_icon16x16.png").createImage()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        videosLabel.setImage(registry.get("videos")); //$NON-NLS-1$

        Link videos = new Link(composite, SWT.NONE);
        GridData videosGd = new GridData(SWT.FILL, SWT.FILL, true, true);
        videosLabel.setLayoutData(videosGd);
        videos.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        videos.setText(VIDEOS_URL);
        videos.setToolTipText(Messages.getString("LinksToolbarItem_videos")); //$NON-NLS-1$

        videos.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                ITutorialsService service = (ITutorialsService) GlobalServiceRegister.getDefault().getService(
                        ITutorialsService.class);
                service.openTutorialsDialog();
            }
        });

        if (!PluginChecker.isTIS()) {
            // 5.cloud
            Label cloudLabel = new Label(composite, SWT.NONE);

            if (registry.get("cloud") == null) { //$NON-NLS-1$
                registry.put("cloud", Activator.getImageDescriptor("icons/cloud.png").createImage()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            cloudLabel.setImage(registry.get("cloud")); //$NON-NLS-1$
            Link cloud = new Link(composite, SWT.NONE);
            GridData cloudGd = new GridData(SWT.FILL, SWT.FILL, true, true);
            cloudLabel.setLayoutData(cloudGd);
            cloud.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            String url = CLOUD_URL;
            // dynamic acronym
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IBrandingService.class)) {
                IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault()
                        .getService(IBrandingService.class);
                String edition = brandingService.getAcronym();
                url = url.replace("dynamic_acronym", edition);//$NON-NLS-1$
            }
            cloud.setText(url);
            cloud.setToolTipText(Messages.getString("LinksToolbarItem_cloud")); //$NON-NLS-1$

            cloud.addListener(SWT.Selection, new Listener() {

                @Override
                public void handleEvent(Event event) {
                    openBrower(event.text);
                }
            });
        }
        return composite;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.jface.action.ContributionItem#dispose()
     */
    @Override
    public void dispose() {
        toolitem = null;
        super.dispose();
    }

    protected void openBrower(String url) {
        Program.launch(url);
    }

}
