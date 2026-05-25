package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnStatusUpdateEvent.SourceTypeEnum;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PnSourceTypeConverter {

	String separator() default " ";

	class Converter implements AttributeConverter<SourceTypeEnum> {
		@Override
		public AttributeValue transformFrom(SourceTypeEnum input) {
			return AttributeValue.builder().s(input.getValue()).build();
		}

		@Override
		public SourceTypeEnum transformTo(AttributeValue input) {
			return SourceTypeEnum.fromValue(input.s());
		}

		@Override
		public EnhancedType<SourceTypeEnum> type() {
			return EnhancedType.of(SourceTypeEnum.class);
		}

		@Override
		public AttributeValueType attributeValueType() {
			return AttributeValueType.S;
		}
	}
}