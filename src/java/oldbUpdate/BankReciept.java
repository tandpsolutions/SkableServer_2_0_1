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

/**
 *
 * @author nice
 */
public class BankReciept {

    Library lb = null;

    public BankReciept() {
        lb = new Library();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from BPRHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date chq_date = rsLocal.getDate("CHEQUE_DATE");
            Date rec_date = rsLocal.getDate("REC_DATE");
            String bank_cd = rsLocal.getString("BANK_CD");
            String ac_cd = rsLocal.getString("AC_CD");
            String chq_no = rsLocal.getString("CHEQUE_NO");
            String opp_bank_name = rsLocal.getString("opp_bank_name");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");
            String branch_cd = rsLocal.getString("branch_cd");
            sql = "select * from BPRDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,rec_date,chq_no,rec_bank_name,branch_cd) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, chq_date);
                pstUpdate.setString(3, "BR");
                pstUpdate.setString(4, ac_cd);
                pstUpdate.setString(5, rsLocal.getString("BAL"));
                pstUpdate.setString(6, "1");
                pstUpdate.setString(7, rsLocal.getString("REMARK"));
                pstUpdate.setString(8, bank_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setDate(10, rec_date);
                pstUpdate.setString(11, chq_no);
                pstUpdate.setString(12, opp_bank_name);
                pstUpdate.setString(13, branch_cd);
                i += pstUpdate.executeUpdate();

                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, chq_date);
                pstUpdate.setString(3, "BR");
                pstUpdate.setString(4, bank_cd);
                pstUpdate.setString(5, rsLocal.getString("BAL"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, rsLocal.getString("REMARK"));
                pstUpdate.setString(8, ac_cd);
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setDate(10, rec_date);
                pstUpdate.setString(11, chq_no);
                pstUpdate.setString(12, opp_bank_name);
                pstUpdate.setString(13, branch_cd);
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update oldb2_1 set Dr_" + (chq_date.getMonth() + 1) + "=dr_" + (chq_date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("BAL"));
                pstUpdate.setString(2, bank_cd);
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update oldb2_1 set cr_" + (chq_date.getMonth() + 1) + "=cr_" + (chq_date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("BAL"));
                pstUpdate.setString(2, ac_cd);
                i += pstUpdate.executeUpdate();

                if (!rsLocal.getString("DOC_REF_NO").equalsIgnoreCase("")) {
                    sqlUpdate = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("BAL"));
                    pstUpdate.setString(2, rsLocal.getString("DOC_REF_NO"));
                    i += pstUpdate.executeUpdate();
                } else {
                    long rec_no = lb.getRecNOFromOldb0_4(con, rsLocal.getString("REF_NO"), rsLocal.getString("SR_NO"));
                    if (rec_no == -1) {
                        sql = "insert into oldb2_4 (doc_ref_no,doc_cd,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST,SR_NO) values (?,?,?,?,?,?,?,?,?,?)";
                        pstUpdate = con.prepareStatement(sql);
                        pstUpdate.setString(1, rsLocal.getString("REF_NO"));
                        pstUpdate.setString(2, "BR");
                        pstUpdate.setString(3, "0");
                        pstUpdate.setDate(4, chq_date);
                        pstUpdate.setString(5, ac_cd);
                        pstUpdate.setDouble(6, rsLocal.getDouble("BAL") * -1);
                        pstUpdate.setDouble(7, rsLocal.getDouble("BAL") * -1);
                        pstUpdate.setDate(8, chq_date);
                        pstUpdate.setString(9, "0.00");
                        pstUpdate.setString(10, rsLocal.getString("SR_NO"));
                        pstUpdate.executeUpdate();
                    } else {
                        sql = "update oldb2_4 set TOT_AMT=TOT_AMT-?,UNPAID_AMT=UNPAID_AMT-?,DOC_DATE=?,DUE_DATE=? where rec_no=?";
                        pstUpdate = con.prepareStatement(sql);
                        pstUpdate.setDouble(1, rsLocal.getDouble("BAL"));
                        pstUpdate.setDouble(2, rsLocal.getDouble("BAL"));
                        pstUpdate.setDate(3, chq_date);
                        pstUpdate.setDate(4, chq_date);
                        pstUpdate.setLong(5, rec_no);
                        pstUpdate.executeUpdate();
                    }
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
        String sql = "select * from BPRHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            String bank_cd = rsLocal.getString("BANK_CD");
            String ac_cd = rsLocal.getString("ac_cd");
            Date chq_date = rsLocal.getDate("CHEQUE_DATE");

            sql = "select * from BPRDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            while (rsLocal.next()) {
                String sqlUpdate = "delete from oldb2_2 where doc_ref_no='" + refNo + "'";
                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update oldb2_1 set dr_" + (chq_date.getMonth() + 1) + "=dr_" + (chq_date.getMonth() + 1) + "-? where ac_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("BAL"));
                pstUpdate.setString(2, bank_cd);
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update oldb2_1 set cr_" + (chq_date.getMonth() + 1) + "=cr_" + (chq_date.getMonth() + 1) + "-? where ac_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("BAL"));
                pstUpdate.setString(2, ac_cd);
                i += pstUpdate.executeUpdate();

                if (!rsLocal.getString("DOC_REF_NO").equalsIgnoreCase("")) {
                    sqlUpdate = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("BAL"));
                    pstUpdate.setString(2, rsLocal.getString("DOC_REF_NO"));
                    i += pstUpdate.executeUpdate();
                } else {
                    long rec_no = lb.getRecNOFromOldb0_4(con, rsLocal.getString("REF_NO"), rsLocal.getString("SR_NO"));
                    if (rec_no != -1) {
                        sql = "update oldb2_4 set TOT_AMT=TOT_AMT+?,UNPAID_AMT=UNPAID_AMT+?,DOC_DATE=?,DUE_DATE=? where rec_no=?";
                        pstUpdate = con.prepareStatement(sql);
                        pstUpdate.setDouble(1, rsLocal.getDouble("BAL"));
                        pstUpdate.setDouble(2, rsLocal.getDouble("BAL"));
                        pstUpdate.setString(3, "0000-00-00");
                        pstUpdate.setString(4, "0000-00-00");
                        pstUpdate.setLong(5, rec_no);
                        pstUpdate.executeUpdate();
                    }
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
