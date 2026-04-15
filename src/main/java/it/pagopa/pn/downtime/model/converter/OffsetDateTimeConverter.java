package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface OffsetDateTimeConverter {

	String separator() default " ";

	class Converter implements AttributeConverter<OffsetDateTime> {
		@Override
		public AttributeValue transformFrom(OffsetDateTime input) {
			return AttributeValue.builder().s(input != null ? input.toString() : "").build();
		}

		@Override
		public OffsetDateTime transformTo(AttributeValue input) {
			String s = input.s();
			return (s != null && !s.isEmpty()) ? OffsetDateTime.parse(s, DateTimeFormatter.ISO_OFFSET_DATE_TIME) : null;
		}

		@Override
		public EnhancedType<OffsetDateTime> type() {
			return EnhancedType.of(OffsetDateTime.class);
		}

		@Override
		public AttributeValueType attributeValueType() {
			return AttributeValueType.S;
		}
	}
}