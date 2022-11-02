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
package org.talend.core.pendo;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Level;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.utils.network.IProxySelectorProvider;
import org.talend.commons.utils.network.NetworkUtil;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.nexus.HttpClientTransport;
import org.talend.core.pendo.PendoTrackDataUtil.TrackEvent;
import org.talend.core.pendo.properties.IPendoDataProperties;
import org.talend.core.runtime.CoreRuntimePlugin;
import org.talend.core.service.IRemoteService;
import org.talend.repository.model.RepositoryConstants;
import org.talend.utils.json.JSONObject;

/**
 * DOC jding  class global comment. Detailled comment
 */
public class PendoTrackSender {
    
    public static final String PROP_PENDO_LOCAL_CHECK = "talend.pendo.localDebug";

    public static final String PROP_PENDO_LOG_DATA = "talend.pendo.logRuntimeData";

    private static final String PREFIX_API = "api";

    private static final String PENDO_INFO = "/monitoring/pendo/info";

    private static final String PENDO_TRACK = "/monitoring/pendo/track";

    private static final String HEAD_AUTHORIZATION = "Authorization";

    private static final String HEAD_CONTENT_TYPE = "Content-Type";

    private static final String HEAD_PENDO_KEY = "x-pendo-integration-key";

    private static PendoTrackSender instance;

    private static String adminUrl;

    private static String apiBaseUrl;

    private static String pendoInfo;

    private PendoTrackSender() {
    }

    static {
        instance = new PendoTrackSender();
        RepositoryContext repositoryContext = getRepositoryContext();
        if (repositoryContext != null) {
            adminUrl = repositoryContext.getFields().get(RepositoryConstants.REPOSITORY_URL);
        }
    }

    public static PendoTrackSender getInstance() {
        if (StringUtils.isBlank(adminUrl)) {
            RepositoryContext repositoryContext = getRepositoryContext();
            if (repositoryContext != null) {
                adminUrl = repositoryContext.getFields().get(RepositoryConstants.REPOSITORY_URL);
            }
        }
        return instance;
    }

    public void sendToPendo(TrackEvent event, IPendoDataProperties properties) {
        Job job = new Job("send pendo track") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                try {
                    if (isTrackSendAvailable()) {
                        sendTrackData(event, properties);
                    }
                } catch (Exception e) {
                    // warning only
                    ExceptionHandler.process(e, Level.WARN);
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.setPriority(Job.INTERACTIVE);
        job.schedule();
    }

    public void sendTrackData(TrackEvent event, IPendoDataProperties properties) throws Exception {
        if (isPendoLocalDebug()) {
            ExceptionHandler.log(event.getEvent() + ":" + PendoTrackDataUtil.convertEntityJsonString(properties));
            return;
        }
        DefaultHttpClient client = null;
        CloseableHttpResponse response = null;
        IProxySelectorProvider proxySelectorProvider = null;
        try {
            String pendoInfo = getPendoInfo();
            if (StringUtils.isBlank(pendoInfo)) {
                throw new Exception("Pendo information is empty");
            }
            String pendoKey = getPendoKeyFromLicense();
            if (StringUtils.isBlank(pendoKey)) {
                throw new Exception("Pendo key is empty");
            }
            
            client = new DefaultHttpClient();
            String url = getBaseUrl() + PENDO_TRACK;
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader(HEAD_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
            httpPost.setHeader(HEAD_PENDO_KEY, pendoKey);

            proxySelectorProvider = HttpClientTransport.addProxy(client, new URI(url));

            EntityBuilder entityBuilder = EntityBuilder.create();
            String trackData = PendoTrackDataUtil.generateTrackData(pendoInfo, event, properties);
            entityBuilder.setText(trackData).setContentType(ContentType.APPLICATION_JSON);
            HttpEntity entity = entityBuilder.build();
            httpPost.setEntity(entity);
            response = client.execute(httpPost, HttpClientContext.create());
            StatusLine statusLine = response.getStatusLine();
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (isLogPendoData()) {
                ExceptionHandler.log(trackData);
            }
            if (HttpURLConnection.HTTP_OK != statusLine.getStatusCode()) {
                throw new Exception(statusLine.toString() + ", server message: [" + responseStr + "]");
            }
        } finally {
            HttpClientTransport.removeProxy(proxySelectorProvider);
            client.getConnectionManager().shutdown();
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    public boolean isTrackSendAvailable() throws Exception {
        if (isPendoLocalDebug() || checkTokenUsed(adminUrl) && NetworkUtil.isNetworkValid()) {
            return true;
        }
        return false;
    }

    public boolean isPendoLocalDebug() {
        return Boolean.TRUE.toString().equals(System.getProperty(PROP_PENDO_LOCAL_CHECK));
    }

    public boolean isLogPendoData() {
        return Boolean.TRUE.toString().equals(System.getProperty(PROP_PENDO_LOG_DATA));
    }

    private String getPendoInfo() throws Exception {
        if (StringUtils.isBlank(pendoInfo)) {
            pendoInfo = getPendoInfo(getBaseUrl(), getToken());
        }
        return pendoInfo;
    }

    private String getPendoInfo(String baseUrl, String token) throws Exception {
        DefaultHttpClient client = null;
        CloseableHttpResponse response = null;
        IProxySelectorProvider proxySelectorProvider = null;
        try {
            client = new DefaultHttpClient();

            String url = baseUrl + PENDO_INFO;

            HttpGet httpGet = new HttpGet(url);
            httpGet.setHeader(HEAD_AUTHORIZATION, "Bearer " + token);
            proxySelectorProvider = HttpClientTransport.addProxy(client, new URI(url));

            response = client.execute(httpGet, HttpClientContext.create());
            StatusLine statusLine = response.getStatusLine();
            String responseStr = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            if (HttpURLConnection.HTTP_OK != statusLine.getStatusCode()) {
                throw new Exception(statusLine.toString() + ", server message: [" + responseStr + "]");
            }
            return responseStr;
        } finally {
            HttpClientTransport.removeProxy(proxySelectorProvider);
            client.getConnectionManager().shutdown();
            if (response != null) {
                try {
                    response.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {
                    ExceptionHandler.process(e);
                }
            }
        }
    }

    private boolean checkTokenUsed(String adminUrl) throws Exception {
        if (StringUtils.isNotBlank(adminUrl) && GlobalServiceRegister.getDefault().isServiceRegistered(IRemoteService.class)) {
            IRemoteService service = GlobalServiceRegister.getDefault().getService(IRemoteService.class);
            return service.isTokenUsed(adminUrl);
        }
        return false;
    }

    public String getBaseUrl() throws Exception {
        return getBaseUrl(adminUrl, false);
    }

    public String getBaseUrl(String adminUrl, boolean token) throws Exception {
        if (StringUtils.isNotBlank(apiBaseUrl) && !token) {
            return apiBaseUrl;
        }

        try {
            URL url = new URL(adminUrl);
            // tmc.int.cloud.talend.com
            String authority = url.getAuthority();
            String regex = "(\\w*\\-*\\w*\\.?){2}\\.(talend.com)";
            Pattern  pattern= Pattern.compile(regex);
            Matcher match = pattern.matcher(authority);
            if (match.find()) {
                // int.cloud.talend.com
                authority = match.group(0);
                URL apiURL = new URL(url.getProtocol(), PREFIX_API + "." + authority, "");
                // https://api.int.cloud.talend.com
                apiBaseUrl = apiURL.toString();
            } else {
                throw new Exception("Can't match pendo url from " + adminUrl);
            }
        } catch (MalformedURLException e) {
            throw new Exception("Invalid url " + adminUrl, e.getCause());
        }

        return apiBaseUrl;
    }

    private String getToken() {
        return getRepositoryContext().getClearPassword();
    }

    public String getTmcUser(String url, String token) {
        try {
            String pendoInfo = getPendoInfo(getBaseUrl(url, true), token);
            if (StringUtils.isNotBlank(pendoInfo)) {
                JSONObject infoJson = new JSONObject(pendoInfo);
                return ((JSONObject) infoJson.get("visitor")).getString("id"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (Exception e) {
            ExceptionHandler.process(e);
        }
        return ""; //$NON-NLS-1$
    }

    private String getPendoKeyFromLicense() throws Exception {
        if (GlobalServiceRegister.getDefault().isServiceRegistered(IRemoteService.class)) {
            IRemoteService service = GlobalServiceRegister.getDefault().getService(IRemoteService.class);
            return service.getPendoKeyFromLicense();
        }
        return null;
    }

    private static RepositoryContext getRepositoryContext() {
        RepositoryContext repositoryContext = (RepositoryContext) CoreRuntimePlugin.getInstance().getContext()
                .getProperty(Context.REPOSITORY_CONTEXT_KEY);
        return repositoryContext;
    }

    public void setAdminUrl(String adminUrl) {
        PendoTrackSender.adminUrl = adminUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        PendoTrackSender.apiBaseUrl = apiBaseUrl;
    }

}
