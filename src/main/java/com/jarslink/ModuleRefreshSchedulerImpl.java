package com.jarslink;

import com.alipay.jarslink.api.ModuleConfig;
import com.alipay.jarslink.api.impl.AbstractModuleRefreshScheduler;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Created by zjl on 4/3/18.
 */
public class ModuleRefreshSchedulerImpl extends AbstractModuleRefreshScheduler {
    @Override
    public List<ModuleConfig> queryModuleConfigs() {
        return ImmutableList.of(DemoApplication.buildModuleConfig(true));
    }
//    public static ModuleConfig buildModuleConfig() {
//        URL demoModule = Thread.currentThread().getContextClassLoader().getResource("/jarslink-module-demo-1.0.0.jar");
//        ModuleConfig moduleConfig = new ModuleConfig();
//        moduleConfig.setName("demo");
//        moduleConfig.setEnabled(true);
//        moduleConfig.setVersion("1.0.0.20170621");
//        moduleConfig.setProperties(ImmutableMap.of("svnPath", new Object()));
//        moduleConfig.setModuleUrl(ImmutableList.of(demoModule));
//        return moduleConfig;
    }