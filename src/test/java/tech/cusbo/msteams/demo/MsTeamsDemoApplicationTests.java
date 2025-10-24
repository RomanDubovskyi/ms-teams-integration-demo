package tech.cusbo.msteams.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
public class MsTeamsDemoApplicationTests {

	@Test
	public void contextLoads() {
	}

}
