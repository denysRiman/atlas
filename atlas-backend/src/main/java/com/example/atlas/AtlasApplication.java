package com.example.atlas;

import com.example.atlas.service.BedrockConverseService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.regions.Region;

@SpringBootApplication
public class AtlasApplication {

	public static void main(String[] args) {
		SpringApplication.run(AtlasApplication.class, args);
	}

	@Bean
	public BedrockRuntimeClient client(@Value("${aws.region}") String region) {
		return BedrockRuntimeClient.builder()
				.region(Region.of(region))
				.build();
	}

	@Bean
    CommandLineRunner runner(BedrockConverseService bedrockConverseService) {
		return args -> {
			String answer = bedrockConverseService.converse(
					"Write a one-sentence bedtime story about a unicorn."
			);
			System.out.println(answer);
		};
	}

}
