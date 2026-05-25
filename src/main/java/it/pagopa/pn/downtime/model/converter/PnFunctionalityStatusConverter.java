package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PnFunctionalityStatusConverter {

	String separator() default " ";

	class Converter implements AttributeConverter<PnFunctionalityStatus> {
		@Override
		public AttributeValue transformFrom(PnFunctionalityStatus input) {
			return AttributeValue.builder().s(input.getValue()).build();
		}

		@Override
		public PnFunctionalityStatus transformTo(AttributeValue input) {
			return PnFunctionalityStatus.fromValue(input.s());
		}

		@Override
		public EnhancedType<PnFunctionalityStatus> type() {
			return EnhancedType.of(PnFunctionalityStatus.class);
		}

		@Override
		public AttributeValueType attributeValueType() {
			return AttributeValueType.S;
		}
	}
}