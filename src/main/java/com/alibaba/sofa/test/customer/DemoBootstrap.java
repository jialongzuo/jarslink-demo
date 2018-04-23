package com.alibaba.sofa.test.customer;

import com.alibaba.sofa.boot.*;
import com.alibaba.sofa.exception.InfraException;
import com.alipay.jarslink.api.impl.SpringModule;
import com.google.common.base.Throwables;
import com.jarslink.ExtensionRegisterWrap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by jialongZuo@hand-china.com on 4/12/18.
 */
public class DemoBootstrap  {

    @Getter
    @Setter
    private List<String> packages;
    private ClassPathScanHandler handler;

    @Autowired
    ExtensionRegisterWrap extensionRegisterWrap;

    @Autowired
    private RegisterFactory registerFactory;

    private Map<String,List<Class>> map=new HashMap();

    public void refresh(SpringModule springModule){
        ClassLoader moduleClassLoader=springModule.getChildClassLoader();
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try{
            Thread.currentThread().setContextClassLoader(moduleClassLoader);
            List<String> list=new ArrayList<>();
            list.add("com.alipay.jarslink.demo");
            Set<Class<?>> classSet = scanConfiguredPackages(list);
            for (Class<?> targetClz : classSet) {
                RegisterI register = registerFactory.getRegister(targetClz);
                if (null != register) {
                    if(register instanceof ExtensionRegister) {
                        extensionRegisterWrap.addModuleBean(targetClz,springModule);
                        if(map.containsKey(springModule.getName())){
                            map.get(springModule.getName()).add(targetClz);
                        }else{
                            List l1=new ArrayList<>();
                            l1.add(targetClz);
                            map.put(springModule.getName(),l1);
                        }
                    }
                }
            }
        }catch (Throwable e){
            CachedIntrospectionResults.clearClassLoader(moduleClassLoader);
            throw Throwables.propagate(e);
        }finally {
            //还原当前线程的ClassLoader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    public void remove(SpringModule springModule){
        map.get(springModule.getName()).forEach(v->{
            extensionRegisterWrap.removeModuleBean(v);
        });

    }


    public void init() {
        Set<Class<?>> classSet = scanConfiguredPackages(packages);
        registerBeans(classSet);
    }

    /**
     * @param classSet
     */
    private void registerBeans(Set<Class<?>> classSet) {
        for (Class<?> targetClz : classSet) {
            RegisterI register = registerFactory.getRegister(targetClz);
            if (null != register) {
                register.doRegistration(targetClz);
            }
        }

    }

    /**
     * Scan the packages configured in Spring xml
     *
     * @return
     */
    private Set<Class<?>> scanConfiguredPackages(List<String> packages) {
        if (packages == null) throw new InfraException("Command packages is not specified");

        String[] pkgs = new String[packages.size()];
        handler = new ClassPathScanHandler(packages.toArray(pkgs));

        Set<Class<?>> classSet = new TreeSet<>(new ClassNameComparator());
        for (String pakName : packages) {
            classSet.addAll(handler.getPackageAllClasses(pakName, true));
        }
        return classSet;
    }

}
