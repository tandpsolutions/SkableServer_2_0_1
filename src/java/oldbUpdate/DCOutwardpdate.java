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
public class DCOutwardpdate {

    Library lb = null;
    SysEnv clSysEnv;

    public DCOutwardpdate() {
        lb = Library.getInstance();
        clSysEnv = lb.companySetUp();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from DCHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String ac_cd = rsLocal.getString("AC_CD");
            String inv_no = rsLocal.getString("INV_NO");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");
            double det_tot = rsLocal.getDouble("DET_TOT");

            sql = "select * from DCDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();

            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,SR_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no,inv_no) values(?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "DCI");
                pstUpdate.setString(4, rsLocal.getString("SR_CD"));
                pstUpdate.setString(5, ac_cd);
                pstUpdate.setString(6, rsLocal.getString("QTY"));
                pstUpdate.setString(7, "I");
                pstUpdate.setTimestamp(8, ts);
                pstUpdate.setDouble(9, rsLocal.getDouble("RATE"));
                pstUpdate.setString(10, rsLocal.getString("tag_no"));
                pstUpdate.setString(11, inv_no);
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update oldb0_1 set PSAL_" + (date.getMonth() + 1) + "=PSAL_" + (date.getMonth() + 1) + "+? where SR_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("QTY"));
                pstUpdate.setString(2, rsLocal.getString("SR_CD"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update tag set is_del=3 where ref_no=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
                i += pstUpdate.executeUpdate();

                sql = "update oldb2_1 set dr_" + (date.getMonth() + 1) + "=dr_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setDouble(1, rsLocal.getDouble("RATE"));
                pstUpdate.setString(2, ac_cd);
                pstUpdate.executeUpdate();

                sql = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,inv_no) values(?,?,?,?,?,?,?,?,?,?)";

                pstUpdate = con.prepareStatement(sql);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "DCI");
                pstUpdate.setString(4, ac_cd);
                pstUpdate.setDouble(5, rsLocal.getDouble("RATE"));
                pstUpdate.setString(6, "0");
                pstUpdate.setString(7, rsLocal.getString("remark"));
                pstUpdate.setString(8, clSysEnv.getSales_ac());
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, inv_no);
                i += pstUpdate.executeUpdate();
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

        String sql = "select * from DCHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String ac_cd = rsLocal.getString("AC_CD");
            double det_tot = rsLocal.getDouble("DET_TOT");

            sql = "select * from DCDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            PreparedStatement pstUpdate = null;
            String sqlUpdate = "delete from oldb0_2 where doc_ref_no=?";
            pstUpdate = con.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, refNo);
            i += pstUpdate.executeUpdate();
            while (rsLocal.next()) {
                sqlUpdate = "update oldb0_1 set PSAL_" + (date.getMonth() + 1) + "=PSAL_" + (date.getMonth() + 1) + "-? where SR_CD=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("QTY"));
                pstUpdate.setString(2, rsLocal.getString("SR_CD"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update tag set is_del=0 where ref_no=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
                i += pstUpdate.executeUpdate();
            }

            sqlUpdate = "delete from oldb2_2 where doc_ref_no=?";
            pstUpdate = con.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, refNo);
            i += pstUpdate.executeUpdate();

            sql = "update oldb2_1 set dr_" + (date.getMonth() + 1) + "=dr_" + (date.getMonth() + 1) + "-? where ac_CD=?";
            pstUpdate = con.prepareStatement(sql);
            pstUpdate.setDouble(1, det_tot);
            pstUpdate.setString(2, ac_cd);
            pstUpdate.executeUpdate();

            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        }
    }
}
