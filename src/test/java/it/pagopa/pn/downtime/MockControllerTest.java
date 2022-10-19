package it.pagopa.pn.downtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = PnDowntimeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)

public class MockControllerTest extends AbstractMock {
	@Autowired
	private MockMvc mvc;

	@Autowired
	ObjectMapper mapper;

	@Test
	public void callCheckHealtcheck() throws Exception {
		// when
		MockHttpServletResponse response = mvc
				.perform(get("/healtcheck").accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();
		// then
				assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
	}

	public void callCheckOptions(String url, String callApi) throws Exception {
		MockHttpServletResponse response;
		if (url.equals(legalFactIdUrl)) {
			mockLegalFactId(client);
			response = mvc.perform(options(legalFactIdUrl.concat("PN_LEGAL_FACTS-0002-L83U-NGPH-WHUF-I87S")))
					.andReturn().getResponse();
		} else if (url.equals(eventsUrl)) {
			response = mvc.perform(options(url).accept(MediaType.APPLICATION_JSON).header("Auth", fakeHeader))
					.andReturn().getResponse();

		} else {
			response = mvc.perform(options(url).accept(MediaType.APPLICATION_JSON)).andReturn().getResponse();

		}

		assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
		assertThat(response.getHeader("Allow")).contains(callApi);
	}

	@Test
	public void callCheckOptionsCurrentStatus() throws Exception {
		callCheckOptions(currentStatusUrl, "GET");
	}

	@Test
	public void callCheckOptionsStatusUrl() throws Exception {
		callCheckOptions(statusUrl, "GET");
	}

	@Test
	public void callCheckOptionsHistoryStatus() throws Exception {
		callCheckOptions(historyStatusUrl, "GET");
	}

	@Test
	public void callCheckOptionsLegalFactId() throws Exception {
		callCheckOptions(legalFactIdUrl, "GET");
	}

	@Test
	public void callCheckOptionsEvents() throws Exception {
		mockUniqueIdentifierForPerson();
		callCheckOptions(eventsUrl, "POST");
	}

}
