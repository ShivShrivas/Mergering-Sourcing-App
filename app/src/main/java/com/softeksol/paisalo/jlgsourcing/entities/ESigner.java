package com.softeksol.paisalo.jlgsourcing.entities;

import java.io.Serializable;
import java.util.Date;


/**
 * Created by sachindra on 2015-10-01.
 */
public class ESigner implements Serializable {
    public int FiCode;
    public String Creator;
    public String AadharNo;

    public String ESignSucceed;
    public String ESignerType;
    public int GrNo;

    public String Name;
    public String FatherName;
    public String Address;
    public String MobileNo;
    public String KycUuid;

    public double Latitude;
    public double Longitude;
    public Date GeoDate;

    public String docPath;


}
