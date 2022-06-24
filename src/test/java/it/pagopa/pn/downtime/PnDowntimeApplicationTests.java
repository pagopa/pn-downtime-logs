package it.pagopa.pn.downtime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import it.pagopa.pn.downtime.PnDowntimeApplication;


@SpringBootTest(classes = PnDowntimeApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)

public class PnDowntimeApplicationTests {
	@Test
	void main() {
		PnDowntimeApplication.main(new String[] {});
		Assertions.assertTrue(true);
	}
}
