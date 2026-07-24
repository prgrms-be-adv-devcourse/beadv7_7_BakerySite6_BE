package com.openbake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 충전 요청 만료 배치(ChargeExpireScheduler)에 필요
public class OpenbakeApplication {
	public static void main(String[] args) {
		SpringApplication.run(OpenbakeApplication.class, args);
	}
}
