package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
public @interface OffsetDateTimeConverter {

	String separator() default " ";

	public static class Converter implements DynamoDBTypeConverter<String, OffsetDateTime> {
		@Override
		public String convert(final OffsetDateTime o) {
			return o != null ? o.toString() : "";
		}

		@Override
		public OffsetDateTime unconvert(final String o) {
			return o != null && !o.isEmpty() ? OffsetDateTime.parse(o,DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
		}

	}
}