package com.iiitd.erp.persistent.entity.erp_db;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.time.LocalDateTime;

@Value
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude
public class Changelog implements Serializable {

    @JsonProperty("userId")
    String userId;

    @JsonProperty("userRole")
    String userRole;

    @JsonProperty("changeType")
    Action changeType;

    @JsonProperty("changedOn")
    LocalDateTime on;


    public enum Action{
        CREATE,MODIFY
    }
}

//
//change_log
//
//[
//        {
//            "userId":"",
//            "userRole":"",
//            "changeType":"",
//            "on":""
//        }
//        ]