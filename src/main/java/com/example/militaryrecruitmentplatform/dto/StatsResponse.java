// StatsResponse.java
package com.example.militaryrecruitmentplatform.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsResponse {

    private long total;

    @JsonProperty("en_attente")
    private long enAttente;

    @JsonProperty("en_examen")
    private long enExamen;

    @JsonProperty("validees")
    private long validees;

    @JsonProperty("rejetees")
    private long rejetees;
}