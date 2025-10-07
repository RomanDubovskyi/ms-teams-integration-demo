package tech.cusbo.msteams.demo;

import java.io.FileReader;
import java.util.Properties;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MsTeamsDemoApplication {

  @SneakyThrows
  public static void main(String[] args) {
    Properties props = new Properties();
    try (FileReader reader = new FileReader(".env")) {
      props.load(reader);
      props.forEach((k, v) -> System.setProperty(k.toString(), v.toString()));
    }
    SpringApplication.run(MsTeamsDemoApplication.class, args);
  }

}
