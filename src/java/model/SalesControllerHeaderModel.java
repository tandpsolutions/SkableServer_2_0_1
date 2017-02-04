/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

/**
 *
 * @author bhaumik
 */
public class SalesControllerHeaderModel {

    private String ref_no;
    private int V_TYPE;
    private String V_DATE;
    private int PMT_MODE;
    private String AC_CD;
    private double DET_TOT;
    private double TAX_AMT;
    private double ADD_TAX_AMT;
    private double ADJST;
    private double NET_AMT;
    private String USER_ID;
    private String REMARK;
    private String ac_name;
    private String address;
    private int BRANCH_CD;
    private String DUE_DATE;
    private double CASH_AMT;
    private String BANK_CD;
    private String BANK_NAME;
    private String BANK_BRANCH;
    private String CHEQUE_NO;
    private String CHEQUE_DATE;
    private double BANK_AMT;
    private String CARD_NAME;
    private double CARD_AMT;
    private double CARD_PER;
    private double CARD_CHG;
    private String BAJAJ_NAME;
    private double BAJAJ_AMT;
    private double BAJAJ_PER;
    private double BAJAJ_CHG;
    private String SFID;
    private String pmt_days;
    private String buy_back_model;
    private double buy_back_amt;
    private String part_no;
    private String buy_back_imei;
    private double ins_amt;
    private String ins_cd;
    private double bank_charges;
    private String buy_back_cd;
    private double advance_amt;
    private double discount;
    private String ref_cd;
    private String sm_cd;
    private String SCHEME_CD;
    private String card_no;
    private String tid_no;

    public String getRef_cd() {
        return ref_cd;
    }

    public void setRef_cd(String ref_cd) {
        this.ref_cd = ref_cd;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getAdvance_amt() {
        return advance_amt;
    }

    public void setAdvance_amt(double advance_amt) {
        this.advance_amt = advance_amt;
    }

    public String getBuy_back_cd() {
        return buy_back_cd;
    }

    public void setBuy_back_cd(String buy_back_cd) {
        this.buy_back_cd = buy_back_cd;
    }

    public String getBuy_back_model() {
        return buy_back_model;
    }

    public void setBuy_back_model(String buy_back_model) {
        this.buy_back_model = buy_back_model;
    }

    public double getBuy_back_amt() {
        return buy_back_amt;
    }

    public void setBuy_back_amt(double buy_back_amt) {
        this.buy_back_amt = buy_back_amt;
    }

    public String getPart_no() {
        return part_no;
    }

    public void setPart_no(String part_no) {
        this.part_no = part_no;
    }

    public String getBuy_back_imei() {
        return buy_back_imei;
    }

    public void setBuy_back_imei(String buy_back_imei) {
        this.buy_back_imei = buy_back_imei;
    }

    public double getIns_amt() {
        return ins_amt;
    }

    public void setIns_amt(double ins_amt) {
        this.ins_amt = ins_amt;
    }

    public String getIns_cd() {
        return ins_cd;
    }

    public void setIns_cd(String ins_cd) {
        this.ins_cd = ins_cd;
    }

    public double getBank_charges() {
        return bank_charges;
    }

    public void setBank_charges(double bank_charges) {
        this.bank_charges = bank_charges;
    }

    public String getPmt_days() {
        return pmt_days;
    }

    public void setPmt_days(String pmt_days) {
        this.pmt_days = pmt_days;
    }

    public double getCASH_AMT() {
        return CASH_AMT;
    }

    public void setCASH_AMT(double CASH_AMT) {
        this.CASH_AMT = CASH_AMT;
    }

    public String getBANK_CD() {
        return BANK_CD;
    }

    public void setBANK_CD(String BANK_CD) {
        this.BANK_CD = BANK_CD;
    }

    public String getBANK_NAME() {
        return BANK_NAME;
    }

    public void setBANK_NAME(String BANK_NAME) {
        this.BANK_NAME = BANK_NAME;
    }

    public String getBANK_BRANCH() {
        return BANK_BRANCH;
    }

    public void setBANK_BRANCH(String BANK_BRANCH) {
        this.BANK_BRANCH = BANK_BRANCH;
    }

    public String getCHEQUE_NO() {
        return CHEQUE_NO;
    }

    public void setCHEQUE_NO(String CHEQUE_NO) {
        this.CHEQUE_NO = CHEQUE_NO;
    }

    public String getCHEQUE_DATE() {
        return CHEQUE_DATE;
    }

    public void setCHEQUE_DATE(String CHEQUE_DATE) {
        this.CHEQUE_DATE = CHEQUE_DATE;
    }

    public double getBANK_AMT() {
        return BANK_AMT;
    }

    public void setBANK_AMT(double BANK_AMT) {
        this.BANK_AMT = BANK_AMT;
    }

    public String getCARD_NAME() {
        return CARD_NAME;
    }

    public void setCARD_NAME(String CARD_NAME) {
        this.CARD_NAME = CARD_NAME;
    }

    public double getCARD_AMT() {
        return CARD_AMT;
    }

    public void setCARD_AMT(double CARD_AMT) {
        this.CARD_AMT = CARD_AMT;
    }

    public String getBAJAJ_NAME() {
        return BAJAJ_NAME;
    }

    public void setBAJAJ_NAME(String BAJAJ_NAME) {
        this.BAJAJ_NAME = BAJAJ_NAME;
    }

    public double getBAJAJ_AMT() {
        return BAJAJ_AMT;
    }

    public void setBAJAJ_AMT(double BAJAJ_AMT) {
        this.BAJAJ_AMT = BAJAJ_AMT;
    }

    public String getSFID() {
        return SFID;
    }

    public void setSFID(String SFID) {
        this.SFID = SFID;
    }

    public String getDUE_DATE() {
        return DUE_DATE;
    }

    public void setDUE_DATE(String DUE_DATE) {
        this.DUE_DATE = DUE_DATE;
    }

    public int getBRANCH_CD() {
        return BRANCH_CD;
    }

    public void setBRANCH_CD(int BRANCH_CD) {
        this.BRANCH_CD = BRANCH_CD;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAc_name() {
        return ac_name;
    }

    public void setAc_name(String ac_name) {
        this.ac_name = ac_name;
    }

    public String getREMARK() {
        return REMARK;
    }

    public void setREMARK(String REMARK) {
        this.REMARK = REMARK;
    }

    public String getRef_no() {
        return ref_no;
    }

    public void setRef_no(String ref_no) {
        this.ref_no = ref_no;
    }

    public int getV_TYPE() {
        return V_TYPE;
    }

    public void setV_TYPE(int V_TYPE) {
        this.V_TYPE = V_TYPE;
    }

    public String getV_DATE() {
        return V_DATE;
    }

    public void setV_DATE(String V_DATE) {
        this.V_DATE = V_DATE;
    }

    public int getPMT_MODE() {
        return PMT_MODE;
    }

    public void setPMT_MODE(int PMT_MODE) {
        this.PMT_MODE = PMT_MODE;
    }

    public String getAC_CD() {
        return AC_CD;
    }

    public void setAC_CD(String AC_CD) {
        this.AC_CD = AC_CD;
    }

    public double getDET_TOT() {
        return DET_TOT;
    }

    public void setDET_TOT(double DET_TOT) {
        this.DET_TOT = DET_TOT;
    }

    public double getTAX_AMT() {
        return TAX_AMT;
    }

    public void setTAX_AMT(double TAX_AMT) {
        this.TAX_AMT = TAX_AMT;
    }

    public double getADD_TAX_AMT() {
        return ADD_TAX_AMT;
    }

    public void setADD_TAX_AMT(double ADD_TAX_AMT) {
        this.ADD_TAX_AMT = ADD_TAX_AMT;
    }

    public double getADJST() {
        return ADJST;
    }

    public void setADJST(double ADJST) {
        this.ADJST = ADJST;
    }

    public double getNET_AMT() {
        return NET_AMT;
    }

    public void setNET_AMT(double NET_AMT) {
        this.NET_AMT = NET_AMT;
    }

    public String getUSER_ID() {
        return USER_ID;
    }

    public void setUSER_ID(String USER_ID) {
        this.USER_ID = USER_ID;
    }

    public double getCARD_PER() {
        return CARD_PER;
    }

    public void setCARD_PER(double CARD_PER) {
        this.CARD_PER = CARD_PER;
    }

    public double getCARD_CHG() {
        return CARD_CHG;
    }

    public void setCARD_CHG(double CARD_CHG) {
        this.CARD_CHG = CARD_CHG;
    }

    public double getBAJAJ_PER() {
        return BAJAJ_PER;
    }

    public void setBAJAJ_PER(double BAJAJ_PER) {
        this.BAJAJ_PER = BAJAJ_PER;
    }

    public double getBAJAJ_CHG() {
        return BAJAJ_CHG;
    }

    public void setBAJAJ_CHG(double BAJAJ_CHG) {
        this.BAJAJ_CHG = BAJAJ_CHG;
    }

    public String getSm_cd() {
        return sm_cd;
    }

    public void setSm_cd(String sm_cd) {
        this.sm_cd = sm_cd;
    }

    public String getSCHEME_CD() {
        return SCHEME_CD;
    }

    public void setSCHEME_CD(String SCHEME_CD) {
        this.SCHEME_CD = SCHEME_CD;
    }

    public String getTid_no() {
        return tid_no;
    }

    public void setTid_no(String tid_no) {
        this.tid_no = tid_no;
    }

    public String getCard_no() {
        return card_no;
    }

    public void setCard_no(String card_no) {
        this.card_no = card_no;
    }

}
