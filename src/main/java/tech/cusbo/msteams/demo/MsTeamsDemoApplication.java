package tech.cusbo.msteams.demo;

import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsTeamsDemoApplication {

  @SneakyThrows
  public static void main(String[] args) {
    SpringApplication.run(MsTeamsDemoApplication.class, args);
  }

}
