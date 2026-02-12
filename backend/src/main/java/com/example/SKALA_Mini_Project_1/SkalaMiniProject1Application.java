package com.example.SKALA_Mini_Project_1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.cdimascio.dotenv.Dotenv;


@SpringBootApplication
public class SkalaMiniProject1Application {

	public static void main(String[] args) {
		// .env 파일 로드
		Dotenv dotenv = Dotenv.configure()
				.directory("./") // .env 파일 위치 (루트 디렉토리)
				.ignoreIfMissing() // 파일이 없어도 에러 내지 않음 (실제 서버 환경 고려)
				.load();

		// 로드된 변수들을 시스템 속성으로 설정
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});


		// 2. Spring 실행 전 시스템 변수에 값이 있는지 강제로 확인
		System.out.println("=== 환경 변수 점검 ===");
		System.out.println("DB_URL: " + System.getProperty("DB_URL"));
		System.out.println("DB_USER: " + System.getProperty("DB_USER"));
		System.out.println("====================");

		SpringApplication.run(SkalaMiniProject1Application.class, args);
	}

}

