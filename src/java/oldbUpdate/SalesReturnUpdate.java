/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oldbUpdate;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import support.Library;
import support.SysEnv;

/**
 *
 * @author nice
 */
public class SalesReturnUpdate {

    Library lb = Library.getInstance();
    SysEnv clSysEnv = lb.companySetUp();

    public SalesReturnUpdate() {
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from SRHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String ac_cd = rsLocal.getString("AC_CD");
            String branch_cd = rsLocal.getString("BRANCH_CD");
            String inv_no = rsLocal.getString("INV_NO");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");
            double net_amt = rsLocal.getDouble("NET_AMT");
            double det_tot = rsLocal.getDouble("DET_TOT");

            sql = "select * from SRDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,INV_NO,sr_cd,BRANCH_CD,PRD_ST_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, inv_no);
                pstUpdate.setString(5, rsLocal.getString("SR_CD"));
                pstUpdate.setString(6, branch_cd);
                pstUpdate.setString(7, "0");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setString(9, rsLocal.getString("QTY"));
                pstUpdate.setString(10, "R");
                pstUpdate.setTimestamp(11, ts);
                pstUpdate.setDouble(12, rsLocal.getDouble("MRP"));
                pstUpdate.setString(13, rsLocal.getString("tag_no"));
                i += pstUpdate.executeUpdate();

                long rec_no = lb.getRecNOFromOldb0_1(con, rsLocal.getString("SR_CD"), branch_cd, "0");
                if (rec_no != -1) {
                    sqlUpdate = "update oldb0_1 set PPUR_" + (date.getMonth() + 1) + "=PPUR_" + (date.getMonth() + 1) + "+? where rec_no=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("QTY"));
                    pstUpdate.setLong(2, rec_no);
                    i += pstUpdate.executeUpdate();
                } else {
                    sql = "insert into OLDB0_1 (SR_CD,PPUR_" + (date.getMonth() + 1) + ",BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                    pstLocal = con.prepareStatement(sql);
                    pstLocal.setString(1, rsLocal.getString("SR_CD"));
                    pstLocal.setString(2, rsLocal.getString("QTY"));
                    pstLocal.setString(3, branch_cd);
                    pstLocal.setString(4, "0");
                    pstLocal.executeUpdate();
                }

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TAC"));
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TAC"));
                pstUpdate.setDouble(5, rsLocal.getDouble("TAX_AMT"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("ADD_TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TACA"));
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TACA"));
                pstUpdate.setDouble(5, rsLocal.getDouble("ADD_TAX_AMT"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("DISC_RATE"));
                pstUpdate.setString(2, clSysEnv.getDisc_ac());
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, clSysEnv.getDisc_ac());
                pstUpdate.setDouble(5, rsLocal.getDouble("DISC_RATE"));
                pstUpdate.setString(6, "1");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

                sql = "update TAG set IS_DEL=0 where ref_no=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
                pstUpdate.executeUpdate();
            }

            sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
            PreparedStatement pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, det_tot);
            pstUpdate.setString(2, clSysEnv.getSales_return_ac());
            i += pstUpdate.executeUpdate();

            sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                    + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setString(1, refNo);
            pstUpdate.setDate(2, date);
            pstUpdate.setString(3, "SR");
            pstUpdate.setString(4, clSysEnv.getSales_return_ac());
            pstUpdate.setDouble(5, det_tot);
            pstUpdate.setString(6, "0");
            pstUpdate.setString(7, "");
            pstUpdate.setString(8, ac_cd);
            pstUpdate.setTimestamp(9, ts);
            pstUpdate.setString(10, inv_no);
            i += pstUpdate.executeUpdate();

            sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, net_amt);
            pstUpdate.setString(2, ac_cd);
            pstUpdate.executeUpdate();

            sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                    + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setString(1, refNo);
            pstUpdate.setDate(2, date);
            pstUpdate.setString(3, "SR");
            pstUpdate.setString(4, ac_cd);
            pstUpdate.setDouble(5, net_amt);
            pstUpdate.setString(6, "1");
            pstUpdate.setString(7, "");
            pstUpdate.setString(8, clSysEnv.getSales_return_ac());
            pstUpdate.setTimestamp(9, ts);
            pstUpdate.setString(10, inv_no);
            i += pstUpdate.executeUpdate();

            sql = "select * from PAYMENT Where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                if (rsLocal.getDouble("CASH_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, clSysEnv.getCash_ac_cd());
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, clSysEnv.getCash_ac_cd());
                    pstUpdate.setString(5, rsLocal.getString("CASH_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("CASH_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, clSysEnv.getCash_ac_cd());
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("BANK_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BANK_CD"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("BANK_CD"));
                    pstUpdate.setString(5, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("BANK_CD"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("CARD_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("CARD_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("CARD_NAME"));
                    pstUpdate.setString(5, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("CARD_NAME"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("BAJAJ_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BAJAJ_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(5, rsLocal.getString("BAJAJ_NAME"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("BAJAJ_NAME"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                }
            }

            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        }
    }

    public void resetEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from SRHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String ac_cd = rsLocal.getString("AC_CD");
            String branch_cd = rsLocal.getString("BRANCH_CD");
            String inv_no = rsLocal.getString("INV_NO");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");
            double net_amt = rsLocal.getDouble("NET_AMT");
            double det_tot = rsLocal.getDouble("DET_TOT");

            sql = "select * from SRDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,INV_NO,sr_cd,BRANCH_CD,PRD_ST_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, inv_no);
                pstUpdate.setString(5, rsLocal.getString("SR_CD"));
                pstUpdate.setString(6, branch_cd);
                pstUpdate.setString(7, "0");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setString(9, rsLocal.getString("QTY"));
                pstUpdate.setString(10, "R");
                pstUpdate.setTimestamp(11, ts);
                pstUpdate.setDouble(12, rsLocal.getDouble("MRP"));
                pstUpdate.setString(13, rsLocal.getString("tag_no"));
                i += pstUpdate.executeUpdate();

                long rec_no = lb.getRecNOFromOldb0_1(con, rsLocal.getString("SR_CD"), branch_cd, "0");
                if (rec_no != -1) {
                    sqlUpdate = "update oldb0_1 set PPUR_" + (date.getMonth() + 1) + "=PPUR_" + (date.getMonth() + 1) + "+? where rec_no=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("QTY"));
                    pstUpdate.setLong(2, rec_no);
                    i += pstUpdate.executeUpdate();
                } else {
                    sql = "insert into OLDB0_1 (SR_CD,PPUR_" + (date.getMonth() + 1) + ",BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                    pstLocal = con.prepareStatement(sql);
                    pstLocal.setString(1, rsLocal.getString("SR_CD"));
                    pstLocal.setString(2, rsLocal.getString("QTY"));
                    pstLocal.setString(3, branch_cd);
                    pstLocal.setString(4, "0");
                    pstLocal.executeUpdate();
                }

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TAC"));
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TAC"));
                pstUpdate.setDouble(5, rsLocal.getDouble("TAX_AMT"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("ADD_TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TACA"));
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TACA"));
                pstUpdate.setDouble(5, rsLocal.getDouble("ADD_TAX_AMT"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("DISC_RATE"));
                pstUpdate.setString(2, clSysEnv.getDisc_ac());
                i += pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "SR");
                pstUpdate.setString(4, clSysEnv.getDisc_ac());
                pstUpdate.setDouble(5, rsLocal.getDouble("DISC_RATE"));
                pstUpdate.setString(6, "1");
                pstUpdate.setString(7, "");
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();

            }

            sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
            PreparedStatement pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, det_tot);
            pstUpdate.setString(2, clSysEnv.getSales_return_ac());
            i += pstUpdate.executeUpdate();

            sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                    + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setString(1, refNo);
            pstUpdate.setDate(2, date);
            pstUpdate.setString(3, "SR");
            pstUpdate.setString(4, clSysEnv.getSales_return_ac());
            pstUpdate.setDouble(5, det_tot);
            pstUpdate.setString(6, "0");
            pstUpdate.setString(7, "");
            pstUpdate.setString(8, ac_cd);
            pstUpdate.setTimestamp(9, ts);
            pstUpdate.setString(10, inv_no);
            i += pstUpdate.executeUpdate();

            sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, net_amt);
            pstUpdate.setString(2, ac_cd);
            pstUpdate.executeUpdate();

            sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                    + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setString(1, refNo);
            pstUpdate.setDate(2, date);
            pstUpdate.setString(3, "SR");
            pstUpdate.setString(4, ac_cd);
            pstUpdate.setDouble(5, net_amt);
            pstUpdate.setString(6, "1");
            pstUpdate.setString(7, "");
            pstUpdate.setString(8, clSysEnv.getSales_return_ac());
            pstUpdate.setTimestamp(9, ts);
            pstUpdate.setString(10, inv_no);
            i += pstUpdate.executeUpdate();

            sql = "select * from PAYMENT Where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                if (rsLocal.getDouble("CASH_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, clSysEnv.getCash_ac_cd());
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, clSysEnv.getCash_ac_cd());
                    pstUpdate.setString(5, rsLocal.getString("CASH_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("CASH_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, clSysEnv.getCash_ac_cd());
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("BANK_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BANK_CD"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("BANK_CD"));
                    pstUpdate.setString(5, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("BANK_CD"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("CARD_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("CARD_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("CARD_NAME"));
                    pstUpdate.setString(5, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("CARD_NAME"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("BAJAJ_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BAJAJ_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(5, rsLocal.getString("BAJAJ_NAME"));
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, ac_cd);
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                    sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                            + "val,crdr,particular,opp_ac_cd,time_stamp,INV_NO) values(?,?,?,?,?,?,?,?,?,?)";

                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, refNo);
                    pstUpdate.setDate(2, date);
                    pstUpdate.setString(3, "SR");
                    pstUpdate.setString(4, ac_cd);
                    pstUpdate.setString(5, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, "");
                    pstUpdate.setString(8, rsLocal.getString("BAJAJ_NAME"));
                    pstUpdate.setTimestamp(9, ts);
                    pstUpdate.setString(10, inv_no);
                    i += pstUpdate.executeUpdate();
                }
            }

            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        }
    }

    public void deleteEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from SRHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String ac_cd = rsLocal.getString("AC_CD");
            String branch_cd = rsLocal.getString("BRANCH_CD");
            double net_amt = rsLocal.getDouble("NET_AMT");
            double det_tot = rsLocal.getDouble("DET_TOT");

            sql = "select * from SRDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            PreparedStatement pstUpdate = null;
            String sqlUpdate = "delete from oldb0_2 where doc_ref_no=?";
            pstUpdate = con.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, refNo);
            i += pstUpdate.executeUpdate();
            while (rsLocal.next()) {
                long rec_no = lb.getRecNOFromOldb0_1(con, rsLocal.getString("sr_cd"), branch_cd, "0");
                sqlUpdate = "update oldb0_1 set PPUR_" + (date.getMonth() + 1) + "=PPUR_" + (date.getMonth() + 1) + "-? where rec_no=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("QTY"));
                pstUpdate.setLong(2, rec_no);
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TAC"));
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("ADD_TAX_AMT"));
                pstUpdate.setString(2, lb.getTaxCode(con, rsLocal.getString("TAX_CD"), "TACA"));
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("DISC_RATE"));
                pstUpdate.setString(2, clSysEnv.getDisc_ac());
                i += pstUpdate.executeUpdate();

                sql = "update TAG set IS_DEL=1 where ref_no=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
                pstUpdate.executeUpdate();

            }

            sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, det_tot);
            pstUpdate.setString(2, clSysEnv.getSales_return_ac());
            i += pstUpdate.executeUpdate();

            sql = "update oldb2_4 set TOT_AMT=TOT_AMT-? where DOC_REF_NO=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, net_amt * -1);
            pstUpdate.setString(2, refNo);
            i += pstUpdate.executeUpdate();

            sql = "delete from oldb2_2 where doc_ref_no=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setString(1, refNo);
            i += pstUpdate.executeUpdate();

            sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, net_amt);
            pstUpdate.setString(2, ac_cd);
            pstUpdate.executeUpdate();

            sql = "select * from PAYMENT Where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                if (rsLocal.getDouble("CASH_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, clSysEnv.getCash_ac_cd());
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CASH_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                } else if (rsLocal.getDouble("BANK_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BANK_CD"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BANK_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BANK_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();

                } else if (rsLocal.getDouble("CARD_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("CARD_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("CARD_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("CARD_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();
                } else if (rsLocal.getDouble("BAJAJ_AMT") > 0) {
                    sql = "update oldb2_1 set CR_" + (date.getMonth() + 1) + "=CR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT"));
                    pstUpdate.setString(2, rsLocal.getString("BAJAJ_NAME"));
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setDouble(1, rsLocal.getDouble("BAJAJ_AMT") * -1);
                    pstUpdate.setString(2, refNo);
                    i += pstUpdate.executeUpdate();

                    sql = "update oldb2_1 set DR_" + (date.getMonth() + 1) + "=DR_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sql);
                    pstUpdate.setString(1, rsLocal.getString("BAJAJ_AMT"));
                    pstUpdate.setString(2, ac_cd);
                    i += pstUpdate.executeUpdate();
                }
            }

            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        }
    }
}
