package com.tigo.workersupermarketott.core.domain.models.symphonica.auth;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Token {
    private String status;
    private String errcode;
    private String errmessage;
    @SerializedName("_links")
    private Links links;
    @SerializedName("_embedded")
    private Embedded embedded;
}
