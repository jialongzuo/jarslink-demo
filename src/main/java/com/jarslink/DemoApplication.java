package com.jarslink;

import com.alibaba.sofa.boot.Bootstrap;
import com.alibaba.sofa.context.TenantContext;
import com.alibaba.sofa.dto.Response;
import com.alibaba.sofa.test.customer.*;
import com.alipay.jarslink.api.*;
import com.alipay.jarslink.api.impl.ModuleLoaderImpl;
import com.alipay.jarslink.api.impl.ModuleManagerImpl;
import com.alipay.jarslink.api.impl.ModuleServiceImpl;
import com.alipay.jarslink.api.impl.SpringModule;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@ComponentScan(value = {"com.alibaba.sofa","com.jarslink"})
@PropertySource(value = {"/application.properties"})
@Controller
public class DemoApplication {

	@Autowired
	ModuleManager moduleManager;

	@Autowired
	ModuleLoader moduleLoader;


	@Autowired
	private CustomerServiceI customerService;

	@Autowired
	private DemoBootstrap bootstrap;

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@RequestMapping("/initModule")
	public String testA(){
		//定义模块信息
		ModuleConfig moduleConfigA=buildModuleConfig(true);
		//生成模块，包含此模块的springContext ，自定义 classloader，缓存所有Action接口方法
		Module moduleA=moduleLoader.load(moduleConfigA);
		//将此module注册到module管理器
		moduleManager.register(moduleA);
//		//从管理器中查找制定module
//		moduleA=moduleManager.find("module1");
//		moduleA.doAction("helloworld",new ModuleConfig());
		bootstrap.refresh((SpringModule)moduleA);
		return "a";
	}


	@RequestMapping("/sofa")
	public String testSofa(){
		TenantContext.set(Constants.TENANT_ID, Constants.BIZ_3);
		AddCustomerCmd addCustomerCmd = new AddCustomerCmd();
		CustomerCO customerCO = new CustomerCO();
		customerCO.setCompanyName("alibaba");
		customerCO.setSource(Constants.SOURCE_RFQ);
		customerCO.setCustomerType(CustomerType.IMPORTANT);
		addCustomerCmd.setCustomerCO(customerCO);

		//2. Execute
		Response response = customerService.addCustomer(addCustomerCmd);

		//3. Expect Success
		System.out.println(response.isSuccess());
		return "a";
	}

	@RequestMapping("/removeModule")
	public String removeModule(){
		Module moduleA=moduleManager.find("module1");
		bootstrap.remove((SpringModule)moduleA);
		moduleManager.remove(moduleA.getName());
		moduleA.destroy();
		return "remove";
	}
	@Bean(initMethod = "init")
	public DemoBootstrap bootstrap() {
		DemoBootstrap bootstrap = new DemoBootstrap();
		List<String> packagesToScan  = new ArrayList<>();
		packagesToScan.add("com.alibaba.sofa.test");
		bootstrap.setPackages(packagesToScan);
		return bootstrap;
	}

	@Bean
	public ModuleLoader registerModuleLoader(){
		return new ModuleLoaderImpl();
	}

	@Bean
	public ModuleManager registerModuleManager(){
		return new ModuleManagerImpl();
	}

	@Bean
	public ModuleService registerModuleService(){
		ModuleServiceImpl moduleService=new ModuleServiceImpl();
		moduleService.setModuleLoader(moduleLoader);
		moduleService.setModuleManager(moduleManager);
		return moduleService;
	}

//	@Bean
//	public ModuleRefreshSchedulerImpl registerModuleRefreshScheduler(){
//		ModuleRefreshSchedulerImpl moduleRefreshScheduler= new ModuleRefreshSchedulerImpl();
//		moduleRefreshScheduler.setModuleLoader(moduleLoader);
//		moduleRefreshScheduler.setModuleManager(moduleManager);
//		return moduleRefreshScheduler;
//	}

	public static ModuleConfig buildModuleConfig(boolean enabled) {
		URL demoModule = Thread.currentThread().getContextClassLoader().getResource("jarslink-module-demo-1.0.0.jar");
		ModuleConfig moduleConfig = new ModuleConfig();
		moduleConfig.setName("module1");
		moduleConfig.setEnabled(enabled);
		//moduleConfig.setOverridePackages(ImmutableList.of("com.alibaba.sofa.test.customerA.convertor"));
		moduleConfig.setVersion("1.0.0.20170621");
		Map<String, Object> properties = new HashMap();
		properties.put("url", "127.0.0.1");
		moduleConfig.setProperties(properties);
		moduleConfig.setModuleUrl(ImmutableList.of(demoModule));
		return moduleConfig;
	}
}
