package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionality;
import it.pagopa.pn.downtime.generated.openapi.server.v1.dto.PnFunctionalityStatus;
import it.pagopa.pn.downtime.model.converter.OffsetDateTimeConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityConverter;
import it.pagopa.pn.downtime.model.converter.PnFunctionalityStatusConverter;
import it.pagopa.pn.downtime.util.OffsetDateTimeSerializer;
import lombok.NoArgsConstructor;
import lombok.ToString;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@NoArgsConstructor
@DynamoDbBean
@JsonIgnoreProperties
@ToString
public class DowntimeLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    private DowntimeLogsId downtimeLogsId;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime endDate;
    private PnFunctionality functionality;
    private PnFunctionalityStatus status;
    private String startEventUuid;
    private String endEventUuid;
    private String legalFactId;
    private String uuid;
    private String history;
    private Boolean fileAvailable;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime fileAvailableTimestamp;
    @JsonSerialize(using = OffsetDateTimeSerializer.class)
    private OffsetDateTime startDateAttribute;
    private String htmlDescription;

    @DynamoDbPartitionKey
    public String getFunctionalityStartYear() {
        return downtimeLogsId != null ? downtimeLogsId.getFunctionalityStartYear() : null;
    }

    public void setFunctionalityStartYear(String functionalityStartYear) {
        if (downtimeLogsId == null) {
            downtimeLogsId = new DowntimeLogsId();
        }
        downtimeLogsId.setFunctionalityStartYear(functionalityStartYear);
    }

    public void setStartDate(OffsetDateTime startDate) {
        if (downtimeLogsId == null) {
            downtimeLogsId = new DowntimeLogsId();
        }
        downtimeLogsId.setStartDate(startDate);
    }

    @DynamoDbSortKey
    @DynamoDbConvertedBy(OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDate() {
        return downtimeLogsId != null ? downtimeLogsId.getStartDate() : null;
    }

    @DynamoDbConvertedBy(OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @DynamoDbConvertedBy(PnFunctionalityConverter.Converter.class)
    public PnFunctionality getFunctionality() {
        return functionality;
    }

    @DynamoDbConvertedBy(PnFunctionalityStatusConverter.Converter.class)
    public PnFunctionalityStatus getStatus() {
        return status;
    }

    public String getStartEventUuid() {
        return startEventUuid;
    }

    public String getEndEventUuid() {
        return endEventUuid;
    }

    public String getLegalFactId() {
        return legalFactId;
    }

    public Boolean getFileAvailable() {
        return fileAvailable;
    }

    @DynamoDbConvertedBy(OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getFileAvailableTimestamp() {
        return fileAvailableTimestamp;
    }

    public String getUuid() {
        return uuid;
    }

    @DynamoDbSecondaryPartitionKey(indexNames = {"invertedIndex"})
    public String getHistory() {
        return history;
    }

    @DynamoDbConvertedBy(OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDateAttribute() {
        return startDateAttribute;
    }

    public String getHtmlDescription() {
        return htmlDescription;
    }

    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    public void setFunctionality(PnFunctionality functionality) {
        this.functionality = functionality;
    }

    public void setStatus(PnFunctionalityStatus status) {
        this.status = status;
    }

    public void setStartEventUuid(String startEventUuid) {
        this.startEventUuid = startEventUuid;
    }

    public void setEndEventUuid(String endEventUuid) {
        this.endEventUuid = endEventUuid;
    }

    public void setLegalFactId(String legalFactId) {
        this.legalFactId = legalFactId;
    }

    public void setFileAvailable(Boolean fileAvailable) {
        this.fileAvailable = fileAvailable;
    }

    public void setFileAvailableTimestamp(OffsetDateTime fileAvailableTimestamp) {
        this.fileAvailableTimestamp = fileAvailableTimestamp;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public void setStartDateAttribute(OffsetDateTime startDateAttribute) {
        this.startDateAttribute = startDateAttribute;
    }
}
