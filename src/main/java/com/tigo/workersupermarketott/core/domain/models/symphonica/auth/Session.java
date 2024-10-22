package com.tigo.workersupermarketott.core.domain.models.symphonica.auth;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Session {
    private String token;
    private String created;
    private String ttl;
    private User user;
    @SerializedName("_links")
    private Links links;
}
