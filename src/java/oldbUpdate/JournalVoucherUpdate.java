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
public class JournalVoucherUpdate {

    Library lb = null;

    public JournalVoucherUpdate() {
        lb = new Library();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;
        String sql = "select * from JVHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("vdate");
            Date rec_date = rsLocal.getDate("rec_date");
            String branch_cd = rsLocal.getString("branch_cd");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");
            sql = "select * from JVDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsLocal = pstLocal.executeQuery();
            String drac = "0", crac = "0";
            int drcnt = 0, crcnt = 0;
            while (rsLocal.next()) {
                if (rsLocal.getInt("DRCR") == 1) {
                    drcnt++;
                    drac = rsLocal.getString("AC_CD");
                } else {
                    crcnt++;
                    crac = rsLocal.getString("AC_CD");
                }
            }
            if (!(drcnt == 1 || crcnt == 1)) {
                drac = "0";
                crac = "0";
            }
            rsLocal.beforeFirst();
            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb2_2 (doc_ref_no,doc_date,doc_cd,ac_cd,"
                        + "val,crdr,particular,opp_ac_cd,time_stamp,branch_cd,rec_date) values(?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "JV");
                pstUpdate.setString(4, rsLocal.getString("AC_CD"));
                pstUpdate.setString(5, rsLocal.getString("AMT"));
                if (rsLocal.getInt("DRCR") == 1) {
                    pstUpdate.setString(6, "0");
                    pstUpdate.setString(7, rsLocal.getString("PART") + " " + rsLocal.getString("IMEI"));
                    pstUpdate.setString(8, crac);
                } else {
                    pstUpdate.setString(6, "1");
                    pstUpdate.setString(7, rsLocal.getString("PART") + " " + rsLocal.getString("IMEI"));
                    pstUpdate.setString(8, drac);
                }
                pstUpdate.setTimestamp(9, ts);
                pstUpdate.setString(10, branch_cd);
                pstUpdate.setDate(11, rec_date);
                i += pstUpdate.executeUpdate();

                if (rsLocal.getInt("DRCR") == 0) {
                    sqlUpdate = "update oldb2_1 set cr_" + (date.getMonth() + 1) + "=cr_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("AMT"));
                    pstUpdate.setString(2, rsLocal.getString("AC_CD"));
                    i += pstUpdate.executeUpdate();
                } else {
                    sqlUpdate = "update oldb2_1 set dr_" + (date.getMonth() + 1) + "=dr_" + (date.getMonth() + 1) + "+? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("AMT"));
                    pstUpdate.setString(2, rsLocal.getString("AC_CD"));
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
        String sql = "select * from JVHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("VDATE");
            sql = "select * from JVDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            while (rsLocal.next()) {
                String sqlUpdate = "delete from oldb2_2 where doc_ref_no='" + refNo + "'";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.executeUpdate();

                if (rsLocal.getInt("DRCR") == 0) {
                    sqlUpdate = "update oldb2_1 set cr_" + (date.getMonth() + 1) + "=cr_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("AMT"));
                    pstUpdate.setString(2, rsLocal.getString("AC_CD"));
                    i += pstUpdate.executeUpdate();
                } else {
                    sqlUpdate = "update oldb2_1 set dr_" + (date.getMonth() + 1) + "=dr_" + (date.getMonth() + 1) + "-? where ac_CD=?";
                    pstUpdate = con.prepareStatement(sqlUpdate);
                    pstUpdate.setString(1, rsLocal.getString("AMT"));
                    pstUpdate.setString(2, rsLocal.getString("AC_CD"));
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
