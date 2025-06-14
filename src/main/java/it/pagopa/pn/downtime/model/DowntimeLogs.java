package it.pagopa.pn.downtime.model;

import java.io.Serializable;
import java.time.OffsetDateTime;

import org.springframework.data.annotation.Id;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
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

@NoArgsConstructor
@DynamoDBTable(tableName = "Downtime-DowntimeLogs")
@JsonIgnoreProperties
@ToString
public class DowntimeLogs implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
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

    @DynamoDBHashKey
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

    @DynamoDBRangeKey
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDate() {
        return downtimeLogsId != null ? downtimeLogsId.getStartDate() : null;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = PnFunctionalityConverter.Converter.class)
    public PnFunctionality getFunctionality() {
        return functionality;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = PnFunctionalityStatusConverter.Converter.class)
    public PnFunctionalityStatus getStatus() {
        return status;
    }

    @DynamoDBAttribute
    public String getStartEventUuid() {
        return startEventUuid;
    }

    @DynamoDBAttribute
    public String getEndEventUuid() {
        return endEventUuid;
    }

    @DynamoDBAttribute
    public String getLegalFactId() {
        return legalFactId;
    }

    @DynamoDBAttribute
    public Boolean getFileAvailable() {
        return fileAvailable;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getFileAvailableTimestamp() {
        return fileAvailableTimestamp;
    }

    @DynamoDBAttribute
    public String getUuid() {
        return uuid;
    }

    @DynamoDBAttribute
    public String getHistory() {
        return history;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDateAttribute() {
        return startDateAttribute;
    }

    @DynamoDBAttribute
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