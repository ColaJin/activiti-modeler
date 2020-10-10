package com.flying.cattle.activiti;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@ComponentScan({"com.flying.cattle.activiti.modeler.editor","com.flying.cattle.activiti.config","com.flying.cattle.activiti.modeler","com.flying.cattle.activiti.modeler.editor.model"})
@EnableSwagger2
public class BootActivitiApplication {

	public static void main(String[] args) {
		SpringApplication.run(BootActivitiApplication.class, args);
	}

}
