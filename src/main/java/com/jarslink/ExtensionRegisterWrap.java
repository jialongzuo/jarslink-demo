package com.jarslink;

import com.alibaba.sofa.boot.ExtensionRegister;
import com.alibaba.sofa.exception.InfraException;
import com.alibaba.sofa.extension.Extension;
import com.alibaba.sofa.extension.ExtensionCoordinate;
import com.alibaba.sofa.extension.ExtensionPointI;
import com.alibaba.sofa.extension.ExtensionRepository;
import com.alipay.jarslink.api.impl.SpringModule;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * Created by jialongZuo@hand-china.com on 4/12/18.
 */

@Component
public class ExtensionRegisterWrap {
    @Autowired
    private ExtensionRepository extensionRepository;
//
//    ExtensionRegister extensionRegister;
//
//    public ExtensionRegisterWrap(ExtensionRegister extensionRegister){
//        this.extensionRegister=extensionRegister;
//    }

    public void addModuleBean(Class<?> targetClz, SpringModule springModule){
        ConfigurableApplicationContext context=null;
        try {
            Field field=springModule.getClass().getDeclaredField("applicationContext");
            field.setAccessible(true);
            context=(ConfigurableApplicationContext) field.get(springModule);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        ExtensionPointI extension = (ExtensionPointI)context.getBean(targetClz);
        Extension extensionAnn = (Extension)targetClz.getDeclaredAnnotation(Extension.class);
        String extensionPoint = this.calculateExtensionPoint(targetClz);
        ExtensionCoordinate extensionCoordinate = new ExtensionCoordinate(extensionPoint, extensionAnn.bizCode(), extensionAnn.tenantId());
        ExtensionPointI preVal = (ExtensionPointI)this.extensionRepository.getExtensionRepo().put(extensionCoordinate, extension);
        if(preVal != null) {
            throw new InfraException("Duplicate registration is not allowed for :" + extensionCoordinate);
        }
    }

    public void removeModuleBean(Class<?> targetClz){
        Extension extensionAnn = (Extension)targetClz.getDeclaredAnnotation(Extension.class);
        String extensionPoint = this.calculateExtensionPoint(targetClz);
        ExtensionCoordinate extensionCoordinate = new ExtensionCoordinate(extensionPoint, extensionAnn.bizCode(), extensionAnn.tenantId());
        this.extensionRepository.getExtensionRepo().remove(extensionCoordinate);
    }
    private String calculateExtensionPoint(Class<?> targetClz) {
        Class[] interfaces = targetClz.getInterfaces();
        if(ArrayUtils.isEmpty(interfaces)) {
            throw new InfraException("Please assign a extension point interface for " + targetClz);
        } else {
            Class[] var3 = interfaces;
            int var4 = interfaces.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Class intf = var3[var5];
                String extensionPoint = intf.getSimpleName();
                if(StringUtils.contains(extensionPoint, "ExtPt")) {
                    return extensionPoint;
                }
            }

            throw new InfraException("Your name of ExtensionPoint for " + targetClz + " is not valid, must be end of " + "ExtPt");
        }
    }
}
