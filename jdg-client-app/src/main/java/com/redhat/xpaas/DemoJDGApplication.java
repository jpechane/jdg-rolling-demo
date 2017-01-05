package com.redhat.xpaas;

import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import infinispan.autoconfigure.remote.InfinispanRemoteConfigurer;

@SpringBootApplication
public class DemoJDGApplication {

	@Configuration
	public static class ApplicationConfiguration {

		@Value("${hotrod.host}")
		private String host;
		
		@Value("${hotrod.port}")
		private int port;

		@Bean
		public InfinispanRemoteConfigurer infinispanRemoteConfigurer() {
			return () -> new ConfigurationBuilder()
					.addServer()
					.host(host)
					.port(port)
					.build();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(DemoJDGApplication.class, args);
	}
}
