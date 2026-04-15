package it.pagopa.pn.downtime.model.converter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PnFunctionalityConverter {

	String separator() default " ";

	class Converter implements AttributeConverter<PnFunctionality> {
		@Override
		public AttributeValue transformFrom(PnFunctionality input) {
			return AttributeValue.builder().s(input.getValue()).build();
		}

		@Override
		public PnFunctionality transformTo(AttributeValue input) {
			return PnFunctionality.fromValue(input.s());
		}

		@Override
		public EnhancedType<PnFunctionality> type() {
			return EnhancedType.of(PnFunctionality.class);
		}

		@Override
		public AttributeValueType attributeValueType() {
			return AttributeValueType.S;
		}
	}
}