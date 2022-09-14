package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import it.pagopa.pn.downtime.pn_downtime_logs.model.PnFunctionalityStatus;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamoDBTypeConverted(converter = PnFunctionalityStatusConverter.Converter.class)
public @interface PnFunctionalityStatusConverter {

	String separator() default " ";

	public static class Converter implements DynamoDBTypeConverter<String, PnFunctionalityStatus> {
		@Override
		public String convert(final PnFunctionalityStatus o) {
			return o.getValue();
		}

		@Override
		public PnFunctionalityStatus unconvert(final String o) {
			return PnFunctionalityStatus.fromValue(o);
		}

	}
}