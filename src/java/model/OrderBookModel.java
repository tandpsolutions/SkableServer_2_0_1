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
public class OrderBookModel {

    private String ref_no;
    private String vdate;
    private String user_id;
    private String ac_cd;
    private double amt;
    private String remark;
    private String model_cd;
    private String memory_cd;
    private String color_cd;

    public String getRef_no() {
        return ref_no;
    }

    public void setRef_no(String ref_no) {
        this.ref_no = ref_no;
    }

    public String getVdate() {
        return vdate;
    }

    public void setVdate(String vdate) {
        this.vdate = vdate;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAc_cd() {
        return ac_cd;
    }

    public void setAc_cd(String ac_cd) {
        this.ac_cd = ac_cd;
    }

    public double getAmt() {
        return amt;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getModel_cd() {
        return model_cd;
    }

    public void setModel_cd(String model_cd) {
        this.model_cd = model_cd;
    }

    public String getMemory_cd() {
        return memory_cd;
    }

    public void setMemory_cd(String memory_cd) {
        this.memory_cd = memory_cd;
    }

    public String getColor_cd() {
        return color_cd;
    }

    public void setColor_cd(String color_cd) {
        this.color_cd = color_cd;
    }

}
