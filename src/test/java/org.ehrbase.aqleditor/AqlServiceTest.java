package org.ehrbase.aqleditor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import org.apache.commons.lang3.StringUtils;
import org.ehrbase.aqleditor.dto.aql.QueryValidationResponse;
import org.ehrbase.aqleditor.dto.aql.Result;
import org.ehrbase.aqleditor.service.AqlService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AqlServiceTest {

  @InjectMocks
  private AqlService aqlService;

  private final String INVALID_QUERY = "Select TOP 13 FORWARD  where (o0/data[at0001]/events[at0at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude < 1.1)";
  private final String VALID_QUERY = "Select TOP 13 FORWARD o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude as Systolic__magnitude, e/ehr_id/value as ehr_id from EHR e contains OBSERVATION o0[openEHR-EHR-OBSERVATION.sample_blood_pressure.v1] where (o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude >= $magnitude and o0/data[at0001]/events[at0002]/data[at0003]/items[at0004]/value/magnitude < 1.1)";

  @Test
  public void shouldCorrectlyValidateAql1() {
    Result aql = Result.builder().q("invalid aql").build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), is("0"));
    assertThat(response.getStartLine(), is("1"));
    assertThat(response.getMessage(),
        containsStringIgnoringCase("AQL Parse exception: line 1: char 0"));
    assertThat(response.getError(),
        containsStringIgnoringCase("mismatched input 'invalid' expecting SELECT"));
  }

  @Test
  public void shouldCorrectlyValidateAql2() {

    Result aql = Result.builder().q(INVALID_QUERY).build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), is("23"));
    assertThat(response.getStartLine(), is("1"));
    assertThat(response.getMessage(),
        containsStringIgnoringCase("AQL Parse exception: line 1: char 23"));
    assertThat(response.getError(),
        containsStringIgnoringCase("mismatched input 'where' expecting {DISTINCT, FUNCTION_IDENTIFIER, EXTENSION_IDENTIFIER, IDENTIFIER, INTEGER, STRING}"));
  }

  @Test
  public void shouldCorrectlyValidateAql3() {
    Result aql = Result.builder().q(VALID_QUERY).build();
    QueryValidationResponse response = aqlService.validateAql(aql);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(true));
    assertThat(response.getMessage(), containsStringIgnoringCase("Query is valid"));
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenLineAndCharMissing() {

    QueryValidationResponse response = aqlService.buildResponse("AQL Parse exception:");

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(),
        containsStringIgnoringCase("AQL Parse exception:"));
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenEmpty() {

    QueryValidationResponse response = aqlService.buildResponse(StringUtils.EMPTY);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(), nullValue());
  }

  @Test
  public void shouldCorrectlyBuildErrorResponseWhenNull() {

    QueryValidationResponse response = aqlService.buildResponse(null);

    assertThat(response, notNullValue());
    assertThat(response.isValid(), is(false));

    assertThat(response.getStartColumn(), nullValue());
    assertThat(response.getStartLine(), nullValue());
    assertThat(response.getMessage(), nullValue());
  }
}
