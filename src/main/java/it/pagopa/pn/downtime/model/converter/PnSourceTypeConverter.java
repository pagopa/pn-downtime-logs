package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

import it.pagopa.pn.downtime.pn_downtime_logs.model.PnStatusUpdateEvent.SourceTypeEnum;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamoDBTypeConverted(converter = PnSourceTypeConverter.Converter.class)
public @interface PnSourceTypeConverter {

	String separator() default " ";

	public static class Converter implements DynamoDBTypeConverter<String, SourceTypeEnum> {
		@Override
		public String convert(final SourceTypeEnum o) {
			return o.getValue();
		}

		@Override
		public SourceTypeEnum unconvert(final String o) {
			return SourceTypeEnum.fromValue(o);
		}

	}
}