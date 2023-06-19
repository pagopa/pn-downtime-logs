package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;


@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamoDBTypeConverted(converter = PnFunctionalityConverter.Converter.class)
public @interface PnFunctionalityConverter {

	String separator() default " ";

	public static class Converter implements DynamoDBTypeConverter<String, PnFunctionality> {
		@Override
		public String convert(final PnFunctionality o) {
			return o.getValue();
		}

		@Override
		public PnFunctionality unconvert(final String o) {
			return PnFunctionality.fromValue(o);
		}

	}
}