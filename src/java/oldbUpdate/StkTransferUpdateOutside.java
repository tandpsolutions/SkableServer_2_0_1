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
import support.Library;
import support.SysEnv;

/**
 *
 * @author nice
 */
public class StkTransferUpdateOutside {

    Library lb = null;
    SysEnv clSysEnv;

    public StkTransferUpdateOutside() {
        lb = Library.getInstance();
        clSysEnv = lb.companySetUp();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from stktrfouthd  where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            String from_loc = rsLocal.getString("from_loc");
            String to_loc = rsLocal.getString("to_loc");
            Date v_date = rsLocal.getDate("v_date");
            String inv_no = rsLocal.getString("inv_no");

            sql = "SELECT * FROM stktrfoutdt s LEFT JOIN tag t ON t.REF_NO=s.pur_tag_no where s.ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();

            while (rsLocal.next()) {
                String sqlUpdate = "update tag set branch_cd =" + to_loc + ",is_del=0 where ref_no=?";
                PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,INV_NO,sr_cd,BRANCH_CD,PRD_ST_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no) values(?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?)";
                pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, v_date);
                pstUpdate.setString(3, "STF");
                pstUpdate.setString(4, inv_no);
                pstUpdate.setString(5, rsLocal.getString("SR_CD"));
                pstUpdate.setString(6, from_loc);
                pstUpdate.setString(7, "0");
                pstUpdate.setString(8, to_loc);
                pstUpdate.setString(9, "1");
                pstUpdate.setString(10, "I");
                pstUpdate.setDouble(11, 0.00);
                pstUpdate.setString(12, rsLocal.getString("tag_no"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,INV_NO,sr_cd,BRANCH_CD,PRD_ST_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no) values(?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?,?)";
                pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, v_date);
                pstUpdate.setString(3, "STF");
                pstUpdate.setString(4, inv_no);
                pstUpdate.setString(5, rsLocal.getString("SR_CD"));
                pstUpdate.setString(6, to_loc);
                pstUpdate.setString(7, "0");
                pstUpdate.setString(8, from_loc);
                pstUpdate.setString(9, "1");
                pstUpdate.setString(10, "R");
                pstUpdate.setDouble(11, 0.00);
                pstUpdate.setString(12, rsLocal.getString("tag_no"));
                i += pstUpdate.executeUpdate();

                long rec_no = lb.getRecNOFromOldb0_1(con, rsLocal.getString("SR_CD"), from_loc, "0");
                if (rec_no != -1) {
                    sqlUpdate = "update oldb0_1 set PSAL_" + (v_date.getMonth() + 1) + "=PSAL_" + (v_date.getMonth() + 1) + "+? where rec_no=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, "1");
                    pstUpdate.setLong(2, rec_no);
                    i += pstUpdate.executeUpdate();
                } else {
                    sql = "insert into OLDB0_1 (SR_CD,PSAL_" + (v_date.getMonth() + 1) + ",BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                    pstLocal = con.prepareStatement(sql);
                    pstLocal.setString(1, rsLocal.getString("SR_CD"));
                    pstLocal.setString(2, "1");
                    pstLocal.setString(3, from_loc);
                    pstLocal.setString(4, "0");
                    pstLocal.executeUpdate();
                }

                rec_no = lb.getRecNOFromOldb0_1(con, rsLocal.getString("SR_CD"), to_loc, "0");
                if (rec_no != -1) {
                    sqlUpdate = "update oldb0_1 set PPUR_" + (v_date.getMonth() + 1) + "=PPUR_" + (v_date.getMonth() + 1) + "+? where rec_no=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, "1");
                    pstUpdate.setLong(2, rec_no);
                    i += pstUpdate.executeUpdate();
                } else {
                    sql = "insert into OLDB0_1 (SR_CD,PPUR_" + (v_date.getMonth() + 1) + ",BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                    pstLocal = con.prepareStatement(sql);
                    pstLocal.setString(1, rsLocal.getString("SR_CD"));
                    pstLocal.setString(2, "1");
                    pstLocal.setString(3, to_loc);
                    pstLocal.setString(4, "0");
                    pstLocal.executeUpdate();
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
