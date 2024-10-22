package com.tigo.workersupermarketott.core.domain.models.symphonica.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {
    private int id;
    private String name;
    private String lastname;
    private String username;
    private String email;
    private int idLenguaje;
    private int idSector;
    private String status;
    private int isRoot;
    private int isSuperAdmin;
    private int isAdmin;
    private int simultaneousConn;
    private int idDefaultOrganization;
}
