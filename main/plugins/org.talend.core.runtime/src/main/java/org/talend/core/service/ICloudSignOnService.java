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
package org.talend.core.service;

import org.talend.core.GlobalServiceRegister;
import org.talend.core.IService;
import org.talend.signon.util.TokenMode;
import org.talend.signon.util.listener.LoginEventListener;

public interface ICloudSignOnService extends IService {

    TokenMode getToken(String authCode, String codeVerifier, String dataCenter) throws Exception;

    String generateCodeVerifier();

    String getCodeChallenge(String seed) throws Exception;

    boolean hasValidToken() throws Exception;

    String getTokenUser(String url, TokenMode token) throws Exception;

    void signonCloud(LoginEventListener listener) throws Exception;
    
    TokenMode getLatestToken() throws Exception;
    
    public boolean refreshToken() throws Exception;
    
    boolean isSignViaCloud();
    
    boolean isNeedShowSSOPage();
    
    public boolean showReloginDialog();
    
    public boolean isReloginDialogRunning();
    
    public void reload();
    
    public boolean isRelogined4CurrentTask(String clazz);

    public static ICloudSignOnService get() {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICloudSignOnService.class)) {
            return GlobalServiceRegister.getDefault().getService(ICloudSignOnService.class);
        }
        return null;
    }
}
