package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import java.sql.Date;

public class Product {
    public String productId;
    public String name;
    public String currency;
    public int recurrentFee;
    public int recurrentPeriod;
    public String recurrentTimeUnit;
    public Date expirationDate;
    public Date nextRevenewalDate;
    public Date lastRenewalDate;
    public String currentState;
}
