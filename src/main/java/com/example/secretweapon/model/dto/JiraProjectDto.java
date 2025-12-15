package com.example.secretweapon.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraProjectDto {

    @JsonProperty("id")
    private String jiraId;

    @JsonProperty("key")
    private String key;

    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("lead")
    private JiraUserDto lead;

    public String toJson() {
        return String.format("{\"jiraId\": \"%s\", \"description\": \"%s\"}", jiraId, description);
    }

}