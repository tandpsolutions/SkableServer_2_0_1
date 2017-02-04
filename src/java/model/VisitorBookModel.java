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
public class VisitorBookModel {

    private String ref_no;
    private String vdate;
    private String follow_up_date;
    private String user_id;
    private String ac_name;
    private String mobile;
    private String remark;
    private String model_name;
    private String memory_name;
    private String color_name;
    private String sm_cd;
    private int branch_cd;
    private int is_del;

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

    public String getFollow_up_date() {
        return follow_up_date;
    }

    public void setFollow_up_date(String follow_up_date) {
        this.follow_up_date = follow_up_date;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getAc_name() {
        return ac_name;
    }

    public void setAc_name(String ac_name) {
        this.ac_name = ac_name;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getModel_name() {
        return model_name;
    }

    public void setModel_name(String model_name) {
        this.model_name = model_name;
    }

    public String getMemory_name() {
        return memory_name;
    }

    public void setMemory_name(String memory_name) {
        this.memory_name = memory_name;
    }

    public String getColor_name() {
        return color_name;
    }

    public void setColor_name(String color_name) {
        this.color_name = color_name;
    }

    public String getSm_cd() {
        return sm_cd;
    }

    public void setSm_cd(String sm_cd) {
        this.sm_cd = sm_cd;
    }

    public int getBranch_cd() {
        return branch_cd;
    }

    public void setBranch_cd(int branch_cd) {
        this.branch_cd = branch_cd;
    }

    public int getIs_del() {
        return is_del;
    }

    public void setIs_del(int is_del) {
        this.is_del = is_del;
    }

}
