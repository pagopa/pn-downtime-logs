package it.pagopa.pn.downtime.model;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
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
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.time.OffsetDateTime;

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

    @DynamoDBRangeKey
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDate() {
        return downtimeLogsId != null ? downtimeLogsId.getStartDate() : null;
    }

    public void setStartDate(OffsetDateTime startDate) {
        if (downtimeLogsId == null) {
            downtimeLogsId = new DowntimeLogsId();
        }
        downtimeLogsId.setStartDate(startDate);
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(OffsetDateTime endDate) {
        this.endDate = endDate;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = PnFunctionalityConverter.Converter.class)
    public PnFunctionality getFunctionality() {
        return functionality;
    }

    public void setFunctionality(PnFunctionality functionality) {
        this.functionality = functionality;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = PnFunctionalityStatusConverter.Converter.class)
    public PnFunctionalityStatus getStatus() {
        return status;
    }

    public void setStatus(PnFunctionalityStatus status) {
        this.status = status;
    }

    @DynamoDBAttribute
    public String getStartEventUuid() {
        return startEventUuid;
    }

    public void setStartEventUuid(String startEventUuid) {
        this.startEventUuid = startEventUuid;
    }

    @DynamoDBAttribute
    public String getEndEventUuid() {
        return endEventUuid;
    }

    public void setEndEventUuid(String endEventUuid) {
        this.endEventUuid = endEventUuid;
    }

    @DynamoDBAttribute
    public String getLegalFactId() {
        return legalFactId;
    }

    public void setLegalFactId(String legalFactId) {
        this.legalFactId = legalFactId;
    }

    @DynamoDBAttribute
    public Boolean getFileAvailable() {
        return fileAvailable;
    }

    public void setFileAvailable(Boolean fileAvailable) {
        this.fileAvailable = fileAvailable;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getFileAvailableTimestamp() {
        return fileAvailableTimestamp;
    }

    public void setFileAvailableTimestamp(OffsetDateTime fileAvailableTimestamp) {
        this.fileAvailableTimestamp = fileAvailableTimestamp;
    }

    @DynamoDBAttribute
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @DynamoDBAttribute
    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    @DynamoDBAttribute
    @DynamoDBTypeConverted(converter = OffsetDateTimeConverter.Converter.class)
    public OffsetDateTime getStartDateAttribute() {
        return startDateAttribute;
    }

    public void setStartDateAttribute(OffsetDateTime startDateAttribute) {
        this.startDateAttribute = startDateAttribute;
    }

    @DynamoDBAttribute
    public String getHtmlDescription() {
        return htmlDescription;
    }

    public void setHtmlDescription(String htmlDescription) {
        this.htmlDescription = htmlDescription;
    }
}
