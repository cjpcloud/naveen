package com.nationsbenefits.igloo.iso8583.adapter;

import com.nationsbenefits.igloo.config.ConfigurationInitializer;
import com.nationsbenefits.igloo.event.publisher.annotation.EnableEventPublisher;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.nationsbenefits.igloo.iso8583.adapter", "com.nationsbenefits.igloo.iso8583.parser"})
@EnableEventPublisher
public class ISO8583AdapterApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(ISO8583AdapterApplication.class).initializers(new ConfigurationInitializer())
				.run(args);
	}

}
