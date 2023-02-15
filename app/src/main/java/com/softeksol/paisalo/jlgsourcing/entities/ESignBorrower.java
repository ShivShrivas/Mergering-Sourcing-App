package com.softeksol.paisalo.jlgsourcing.entities;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.softeksol.paisalo.jlgsourcing.DbIGL;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ModelContainer
@Table(database = DbIGL.class)
public class ESignBorrower extends BaseModel implements Serializable {
    @Column
    @PrimaryKey(autoincrement = true)
    public int id;

    @Column
    public int FiCode;

    @Column
    public String Creator;

    @Column
    public String PartyName;

    @Column
    public String FatherName;

    @Column
    public String AadharNo;

    @Column
    public String MobileNo;

    @Column
    public int SanctionedAmt;

    @Column
    public Date DtFin;

    @Column
    public int Period;

    @Column
    public double IntRate;

    @Column
    public String Address;

    @Column
    public String FoCode;

    @Column
    public String ESignSucceed;

    @Column
    public String documentPath;

    @Column
    public String KycUuid;

    @Column
    public String CityCode;

    @Column
    public Date DisbRequested;




    List<ESignGuarantor> eSignGuarantors;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "eSignGuarantors")
    public List<ESignGuarantor> getESignGuarantors() {
        if ((eSignGuarantors == null || eSignGuarantors.isEmpty())) {
            eSignGuarantors = SQLite.select()
                    .from(ESignGuarantor.class)
                    .where(ESignGuarantor_Table.FiCode.eq(FiCode)).and(ESignGuarantor_Table.Creator.eq(Creator))
                    .queryList();
        }
        return eSignGuarantors;
    }

    public List<ESigner> getESigners() {
        List<ESigner> eSigners = new ArrayList<>();
        eSigners.add(getESigner());
        ESigner eSigner;
        for (ESignGuarantor eSigneGuarantor : getESignGuarantors()) {
            eSigner = eSigneGuarantor.getESigner();
            eSigner.docPath = documentPath;
            eSigners.add(eSigner);
        }
        return eSigners;
    }

    private ESigner getESigner() {
        ESigner eSigner = new ESigner();
        eSigner.AadharNo = AadharNo;
        eSigner.Name = this.PartyName;
        eSigner.MobileNo = MobileNo;
        eSigner.FiCode = FiCode;
        eSigner.Creator = Creator;
        eSigner.Address = Address;
        eSigner.GrNo = 0;
        eSigner.ESignerType = "Borrower";
        eSigner.ESignSucceed = ESignSucceed;
        eSigner.docPath = documentPath;
        eSigner.FatherName = FatherName;
        eSigner.KycUuid = KycUuid;

        return eSigner;
    }

    public boolean hasAllESigned() {
        int signedCount = 0;
        for (ESigner eSigner : getESigners()) {
            if (eSigner.ESignSucceed.equals("Y")) signedCount++;
        }
        //Log.d("hasAllSigned Record",String.valueOf(signedCount));
        return signedCount == getESigners().size();
    }

    public boolean hasSomeSigned() {
        int signedCount = 0;
        for (ESigner eSigner : getESigners()) {
            if (eSigner.ESignSucceed.equals("Y")) signedCount++;
        }
        //Log.d("hasSomeSigned Record",String.valueOf(signedCount));
        return signedCount < getESigners().size();
    }
}
