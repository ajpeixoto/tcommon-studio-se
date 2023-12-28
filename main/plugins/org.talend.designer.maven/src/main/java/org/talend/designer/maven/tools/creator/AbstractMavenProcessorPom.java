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
package org.talend.designer.maven.tools.creator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.JobInfo;
import org.talend.core.model.process.ProcessUtils;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.utils.JavaResourcesHelper;
import org.talend.core.runtime.maven.MavenArtifact;
import org.talend.core.runtime.maven.MavenConstants;
import org.talend.core.runtime.maven.MavenUrlHelper;
import org.talend.core.runtime.process.LastGenerationInfo;
import org.talend.core.runtime.projectsetting.IProjectSettingTemplateConstants;
import org.talend.core.runtime.repository.build.IMavenPomCreator;
import org.talend.core.ui.ITestContainerProviderService;
import org.talend.core.utils.CodesJarResourceCache;
import org.talend.designer.core.model.utils.emf.talendfile.ProcessType;
import org.talend.designer.core.model.utils.emf.talendfile.RoutinesParameterType;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.template.ETalendMavenVariables;
import org.talend.designer.maven.tools.ProcessorDependenciesManager;
import org.talend.designer.maven.utils.JobUtils;
import org.talend.designer.maven.utils.PomIdsHelper;
import org.talend.designer.maven.utils.PomUtil;
import org.talend.designer.runprocess.IBigDataProcessor;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.repository.ProjectManager;

/**
 * DOC ggu class global comment. Detailled comment
 */
public abstract class AbstractMavenProcessorPom extends CreateMavenBundleTemplatePom implements IMavenPomCreator {

    private final IProcessor jobProcessor;

    private final ProcessorDependenciesManager processorDependenciesManager;

    private IFolder objectTypeFolder;

    private IPath itemRelativePath;

    private boolean syncCodesPoms;

    private boolean hasLoopDependency;

    public AbstractMavenProcessorPom(IProcessor jobProcessor, IFile pomFile, String bundleTemplateName) {
        super(pomFile, IProjectSettingTemplateConstants.PATH_STANDALONE + '/' + bundleTemplateName);
        Assert.isNotNull(jobProcessor);
        this.jobProcessor = jobProcessor;
        this.processorDependenciesManager = new ProcessorDependenciesManager(jobProcessor);

        // always ignore case.
        this.setIgnoreFileNameCase(true);
        // should only base on template.
        this.setBaseOnTemplateOnly(true);
    }

    protected IProcessor getJobProcessor() {
        return this.jobProcessor;
    }

    protected ProcessorDependenciesManager getProcessorDependenciesManager() {
        return processorDependenciesManager;
    }

    public IFolder getObjectTypeFolder() {
        return objectTypeFolder;
    }

    public void setObjectTypeFolder(IFolder objectTypeFolder) {
        this.objectTypeFolder = objectTypeFolder;
    }

    public IPath getItemRelativePath() {
        return itemRelativePath;
    }

    public void setItemRelativePath(IPath itemRelativePath) {
        this.itemRelativePath = itemRelativePath;
    }

    @Override
    protected void setAttributes(Model model) {
        //
        final IProcessor jProcessor = getJobProcessor();
        IProcess process = jProcessor.getProcess();
        Property property = jProcessor.getProperty();

        if (ProcessUtils.isTestContainer(process)) {
            if (GlobalServiceRegister.getDefault().isServiceRegistered(ITestContainerProviderService.class)) {
                ITestContainerProviderService testService = GlobalServiceRegister.getDefault()
                        .getService(ITestContainerProviderService.class);
                try {
                    property = testService.getParentJobItem(property.getItem()).getProperty();
                    process = testService.getParentJobProcess(process);
                } catch (PersistenceException e) {
                    ExceptionHandler.process(e);
                }
            }
        }

        Map<ETalendMavenVariables, String> variablesValuesMap = new HashMap<ETalendMavenVariables, String>();
        // no need check property is null or not, because if null, will get default ids.
        JobInfo lastMainJob = LastGenerationInfo.getInstance().getLastMainJob(); 
        if (JobUtils.isJob(property) && ProcessUtils.isChildRouteProcess(process) && lastMainJob != null) {
            variablesValuesMap.put(ETalendMavenVariables.JobGroupId, PomIdsHelper.getJobGroupId(lastMainJob.getProcessor().getProperty()));
            variablesValuesMap.put(ETalendMavenVariables.JobVersion, PomIdsHelper.getJobVersion(lastMainJob.getProcessor().getProperty()));	    	
        }else {
            variablesValuesMap.put(ETalendMavenVariables.JobGroupId, PomIdsHelper.getJobGroupId(property));
            variablesValuesMap.put(ETalendMavenVariables.JobVersion, PomIdsHelper.getJobVersion(property));	    	
        }
        
 
        variablesValuesMap.put(ETalendMavenVariables.TalendJobVersion, property.getVersion());


        if(ProcessUtils.isChildRouteProcess(process) || ProcessUtils.isRoutelet(property)) {
        	if(property.getParentItem() != null) {
            	String routeArtifactID = PomIdsHelper.getJobArtifactId(property.getParentItem().getProperty());
            	String jobArtifactID =  PomIdsHelper.getJobArtifactId(property);
            	String routeVersion = property.getParentItem().getProperty().getVersion().replace(".", "_");
            	
            	String jobName = (jobArtifactID.startsWith(routeArtifactID))? jobArtifactID : 
            		routeArtifactID + "_" + routeVersion + "_" + jobArtifactID;
        		
        		variablesValuesMap.put(ETalendMavenVariables.JobArtifactId, jobName);
        		variablesValuesMap.put(ETalendMavenVariables.JobName, jobName);
        		variablesValuesMap.put(ETalendMavenVariables.JobVersion, PomIdsHelper.getJobVersion(property.getParentItem().getProperty()));
        	} else {
                variablesValuesMap.put(ETalendMavenVariables.JobArtifactId, PomIdsHelper.getJobArtifactId(property));	
                final String jobName = JavaResourcesHelper.escapeFileName(process.getName());
                variablesValuesMap.put(ETalendMavenVariables.JobName, jobName);
        	}
        } else  {
            variablesValuesMap.put(ETalendMavenVariables.JobArtifactId, PomIdsHelper.getJobArtifactId(property));	
            final String jobName = JavaResourcesHelper.escapeFileName(process.getName());
            variablesValuesMap.put(ETalendMavenVariables.JobName, jobName);
        }
        
        if (property != null) {
            Project currentProject = ProjectManager.getInstance().getProject(property);
            variablesValuesMap.put(ETalendMavenVariables.ProjectName,
                    currentProject != null ? currentProject.getTechnicalLabel() : null);

            Item item = property.getItem();
            if (item != null) {
                ERepositoryObjectType itemType = ERepositoryObjectType.getItemType(item);
                if (itemType != null) {
                    variablesValuesMap.put(ETalendMavenVariables.JobType, itemType.getLabel());
                }
            }
        }

        this.setGroupId(ETalendMavenVariables.replaceVariables(model.getGroupId(), variablesValuesMap));
        this.setArtifactId(ETalendMavenVariables.replaceVariables(model.getArtifactId(), variablesValuesMap));
        this.setVersion(ETalendMavenVariables.replaceVariables(model.getVersion(), variablesValuesMap));
        this.setName(ETalendMavenVariables.replaceVariables(model.getName(), variablesValuesMap));

        super.setAttributes(model);
    }

    @Override
    protected Model createModel() {
        Model model = super.createModel();
        if (model != null) {
            Map<String, Object> templateParameters = PomUtil.getTemplateParameters(jobProcessor.getProperty());
            PomUtil.checkParent(model, this.getPomFile(), templateParameters);
            setupShade(model);
            addDependencies(model);
        }
        return model;
    }

    protected void setupShade(Model model) {
        if (jobProcessor instanceof IBigDataProcessor) {
            IBigDataProcessor bigDataProcessor = (IBigDataProcessor) jobProcessor;
            if (bigDataProcessor.needsShade()) {
                List<Plugin> plugins = model.getBuild().getPlugins();
                Plugin shade = null;
                for (Plugin plugin : plugins) {
                    if (plugin.getArtifactId().equals("maven-shade-plugin")) { //$NON-NLS-1$
                        shade = plugin;
                        break;
                    }
                }
                if (shade != null) {
                    plugins.remove(shade);
                }
                shade = new Plugin();
                shade.setGroupId("org.apache.maven.plugins"); //$NON-NLS-1$
                shade.setArtifactId("maven-shade-plugin"); //$NON-NLS-1$
                shade.setVersion("3.1.0"); //$NON-NLS-1$
                
                Dependency codecDep = new Dependency();
                codecDep.setGroupId("commons-codec");
                codecDep.setArtifactId("commons-codec");
                codecDep.setVersion("1.15");
                
                Dependency guavaDep = new Dependency();
                guavaDep.setGroupId("com.google.guava");
                guavaDep.setArtifactId("guava");
                guavaDep.setVersion("32.0.1-jre");
                
                Dependency ioDep = new Dependency();
                ioDep.setGroupId("commons-io");
                ioDep.setArtifactId("commons-io");
                ioDep.setVersion("2.8.0");
                
                Dependency sharedUtilsDep = new Dependency();
                sharedUtilsDep.setGroupId("org.apache.maven.shared");
                sharedUtilsDep.setArtifactId("maven-shared-utils");
                sharedUtilsDep.setVersion("3.3.3");
                
                Dependency mavenCoreDep = new Dependency();
                mavenCoreDep.setGroupId("org.apache.maven");
                mavenCoreDep.setArtifactId("maven-core");
                mavenCoreDep.setVersion("3.8.8");
                
                shade.getDependencies().add(guavaDep);
                shade.getDependencies().add(codecDep);
                shade.getDependencies().add(ioDep);
                shade.getDependencies().add(sharedUtilsDep);
                shade.getDependencies().add(mavenCoreDep);
                              
                List<PluginExecution> executions = shade.getExecutions();
                PluginExecution execution = new PluginExecution();
                executions.add(execution);
                execution.addGoal("shade"); //$NON-NLS-1$
                Xpp3Dom configuration = new Xpp3Dom("configuration"); //$NON-NLS-1$
                execution.setConfiguration(configuration);
                // disable the setup of minimize jar for now, as it could cause some other issues
                // like for example a Class.forName("oracle...." as there is no direct class dependency

                // Xpp3Dom minimizeJar = new Xpp3Dom("minimizeJar"); //$NON-NLS-1$
                // minimizeJar.setValue("true"); //$NON-NLS-1$
                // configuration.addChild(minimizeJar);

                // TDQ-18049 when shaded,avoid to conflict the same name class from different jars. e.g,"ImagePreloader"
                // in "fop-2.3.jar/META-INF/services/" and "xmlgraphics-commons-2.3.jar"
                Xpp3Dom transforms = new Xpp3Dom("transformers"); //$NON-NLS-1$
                Xpp3Dom transform = new Xpp3Dom("transform");
                transform
                        .setAttribute("implementation",
                                "org.apache.maven.plugins.shade.resource.ServicesResourceTransformer");
                transforms.addChild(transform);
                configuration.addChild(transforms);

                Xpp3Dom artifactSet = new Xpp3Dom("artifactSet"); //$NON-NLS-1$
                configuration.addChild(artifactSet);
                Xpp3Dom excludes = new Xpp3Dom("excludes"); //$NON-NLS-1$
                Set<ModuleNeeded> modules = bigDataProcessor.getShadedModulesExclude();
                if (!modules.isEmpty()) {
                    artifactSet.addChild(excludes);
                }

                for (ModuleNeeded module : modules) {
                    Xpp3Dom include = new Xpp3Dom("exclude"); //$NON-NLS-1$
                    excludes.addChild(include);
                    MavenArtifact mvnArtifact = MavenUrlHelper.parseMvnUrl(module.getMavenUri());
                    include.setValue(mvnArtifact.getGroupId() + ":" + mvnArtifact.getArtifactId()); //$NON-NLS-1$
                }
                
                //removing digital signatures from uber jar
                Xpp3Dom filters = new Xpp3Dom("filters"); //$NON-NLS-1$
                Xpp3Dom filter = new Xpp3Dom("filter"); //$NON-NLS-1$
                Xpp3Dom artifact = new Xpp3Dom("artifact"); //$NON-NLS-1$
                artifact.setValue("*:*");
                Xpp3Dom filterExcludes = new Xpp3Dom("excludes"); //$NON-NLS-1$
                Xpp3Dom excludeSF = new Xpp3Dom("exclude");
                excludeSF.setValue("META-INF/*.SF");
                Xpp3Dom excludeDSA = new Xpp3Dom("exclude");
                excludeDSA.setValue("META-INF/*.DSA");
                Xpp3Dom excludeRSA = new Xpp3Dom("exclude");
                excludeRSA.setValue("META-INF/*.RSA");

                filterExcludes.addChild(excludeSF);
                filterExcludes.addChild(excludeDSA);
                filterExcludes.addChild(excludeRSA);
                
                filter.addChild(artifact);
                filter.addChild(filterExcludes);
                filters.addChild(filter);
                configuration.addChild(filters);
                
                plugins.add(shade);
            }
        }
    }

    protected void addDependencies(Model model) {
        try {
            getProcessorDependenciesManager().updateDependencies(null, model);

            final List<Dependency> dependencies = model.getDependencies();

            // add codes to dependencies
            addCodesDependencies(dependencies);

            // add children jobs in dependencies
            addChildrenDependencies(dependencies);
        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
    }

    protected void addCodesDependencies(final List<Dependency> dependencies) {
        dependencies.addAll(getCodesDependencies());
        dependencies.addAll(getCodesJarDependencies());
    }

    protected List<Dependency> getCodesDependencies() {
        List<Dependency> dependencies = new ArrayList<Dependency>();
        String projectTechName = ProjectManager.getInstance().getProject(getJobProcessor().getProperty()).getTechnicalLabel();
        String codeVersion = PomIdsHelper.getCodesVersion(projectTechName);

        // routines
        String routinesGroupId = PomIdsHelper.getCodesGroupId(projectTechName, TalendMavenConstants.DEFAULT_CODE);
        String routinesArtifactId = TalendMavenConstants.DEFAULT_ROUTINES_ARTIFACT_ID;
        Dependency routinesDependency = PomUtil.createDependency(routinesGroupId, routinesArtifactId, codeVersion, null);
        dependencies.add(routinesDependency);

        // beans
        if (ProcessUtils.isRequiredBeans(jobProcessor.getProcess())) {
            String beansGroupId = PomIdsHelper.getCodesGroupId(projectTechName, TalendMavenConstants.DEFAULT_BEAN);
            String beansArtifactId = TalendMavenConstants.DEFAULT_BEANS_ARTIFACT_ID;
            Dependency beansDependency = PomUtil.createDependency(beansGroupId, beansArtifactId, codeVersion, null);
            dependencies.add(beansDependency);
        }
        return dependencies;
    }

    @SuppressWarnings("unchecked")
    protected List<Dependency> getCodesJarDependencies() {
        Property property = getJobProcessor().getProperty();
        if (property != null && getProcessType() != null && getProcessType().getParameters() != null) {
            return new ArrayList<>(createCodesJarDependencies(getProcessType().getParameters().getRoutinesParameter()));
        }
        return Collections.emptyList();
    }

    protected Set<Dependency> createCodesJarDependencies(List<RoutinesParameterType> routineParameters) {
        if (routineParameters == null) {
            return Collections.emptySet();
        }
        return routineParameters.stream().filter(r -> r.getType() != null)
                .map(r -> CodesJarResourceCache.getCodesJarById(r.getId())).filter(info -> info != null)
                .map(info -> PomUtil.createDependency(PomIdsHelper.getCodesJarGroupId(info), info.getLabel().toLowerCase(),
                        PomIdsHelper.getCodesJarVersion(info.getProjectTechName()), null))
                .collect(Collectors.toSet());
    }

    abstract protected ProcessType getProcessType();

    protected void addChildrenDependencies(final List<Dependency> dependencies) {
        String parentId = getJobProcessor().getProperty().getId();

        final Set<JobInfo> clonedChildrenJobInfors = getJobProcessor().getBuildFirstChildrenJobs();
        for (JobInfo jobInfo : clonedChildrenJobInfors) {
            if (jobInfo.getFatherJobInfo() != null && jobInfo.getFatherJobInfo().getJobId().equals(parentId)) {
                if (!validChildrenJob(jobInfo)) {
                    continue;
                }
                Property property;
                String groupId;
                String artifactId;
                String version;
                String type = null;
                if (!jobInfo.isJoblet()) {
                    property = jobInfo.getProcessItem().getProperty();
                    artifactId = PomIdsHelper.getJobArtifactId(jobInfo);
                    
                    JobInfo lastMainJob = LastGenerationInfo.getInstance().getLastMainJob();
                    if (lastMainJob != null && JobUtils.isJob(jobInfo) && JobUtils.isRoute(getJobProcessor().getProperty())) {
                        groupId = PomIdsHelper.getJobGroupId(lastMainJob.getProcessor().getProperty());
                        version =  PomIdsHelper.getJobVersion(lastMainJob.getProcessor().getProperty()); 
                    } else {
                        groupId = PomIdsHelper.getJobGroupId(property);	
                        version = PomIdsHelper.getJobVersion(property);
                    }

                    
                    // try to get the pom version of children job and load from the pom file.
                    String childPomFileName = PomUtil.getPomFileName(jobInfo.getJobName(), jobInfo.getJobVersion());
                    IProject codeProject = getJobProcessor().getCodeProject();
                    if (codeProject != null) {
                        try {
                            codeProject.refreshLocal(IResource.DEPTH_ONE, null); // is it ok or needed here ???
                        } catch (CoreException e) {
                            ExceptionHandler.process(e);
                        }
                        IFile childPomFile = codeProject.getFile(new Path(childPomFileName));
                        if (childPomFile.exists()) {
                            try {
                                Model childModel = MODEL_MANAGER.readMavenModel(childPomFile);
                                // try to get the real groupId, artifactId, version.
                                groupId = childModel.getGroupId();
                                artifactId = childModel.getArtifactId();
                                version = childModel.getVersion();
                            } catch (CoreException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                    }
                } else {
                    property = jobInfo.getJobletProperty();
                    groupId = PomIdsHelper.getJobletGroupId(property);
                    artifactId = PomIdsHelper.getJobletArtifactId(property);
                    version = PomIdsHelper.getJobletVersion(property);
                    type = MavenConstants.PACKAGING_POM;
                }
                Dependency d = PomUtil.createDependency(groupId, artifactId, version, type);
                dependencies.add(d);
            }
        }
    }

    protected boolean validChildrenJob(JobInfo jobInfo) {
        return true; // default, all are valid
    }

    /*
     * (non-Javadoc)
     *
     * @see org.talend.core.runtime.repository.build.IMavenPomCreator#needsyncCodesPoms(boolean)
     */
    @Override
    public void setSyncCodesPoms(boolean isMainJob) {
        this.syncCodesPoms = isMainJob;
    }

    public boolean needSyncCodesPoms() {
        return this.syncCodesPoms;
    }

    /**
     * Sets the hasLoopDependency.
     *
     * @param hasLoopDependency the hasLoopDependency to set
     */
    @Override
    public void setHasLoopDependency(boolean hasLoopDependency) {
        this.hasLoopDependency = hasLoopDependency;
    }

    /**
     * Getter for hasLoopDependency.
     *
     * @return the hasLoopDependency
     */
    public boolean hasLoopDependency() {
        return this.hasLoopDependency;
    }
}
