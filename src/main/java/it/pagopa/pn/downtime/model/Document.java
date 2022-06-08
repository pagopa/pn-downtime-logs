package it.pagopa.pn.downtime.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Document implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idDocumento;

    private String nomeDocumento;



}
