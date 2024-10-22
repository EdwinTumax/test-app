package com.tigo.workersupermarketott.core.domain.models.symphonica.add;

import java.util.ArrayList;
import java.util.Date;

public class Content {
	public String id;
    public String organizationCode;
    public Date creationDate;
    public Date lastUpdate;
    public String name;
    public String type;
    public String description;
    public boolean isServiceEnabled;
    public boolean hasStarted;
    public int startMode;
    public String region;
    public boolean isStateful;
    public String state;
    public ServiceSpecification serviceSpecification;
    public ArrayList<ServiceCharacteristic> serviceCharacteristics;
    public ArrayList<SupportingService> supportingServices;
    public ArrayList<Object> supportingResources;
    public ArrayList<RelatedParty> relatedParty;
    public ServiceOrder serviceOrder;
    public Object customerAccount;
    public Object product;
    public String publicIdentifier;
    public ArrayList<Place> places;
    public ArrayList<Link> links;
}
