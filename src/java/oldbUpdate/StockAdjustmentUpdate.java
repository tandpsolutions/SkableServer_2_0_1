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
public class StockAdjustmentUpdate {

    Library lb = null;
    SysEnv clSysEnv;

    public StockAdjustmentUpdate() {
        lb = Library.getInstance();
        clSysEnv = lb.companySetUp();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from STKADJHD  where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");
            String inv_no = rsLocal.getString("inv_no");
            Timestamp ts = rsLocal.getTimestamp("INIT_TIMESTAMP");

            sql = "select * from STKADJDT  where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();

            while (rsLocal.next()) {
                String sqlUpdate = "insert into oldb0_2 (doc_ref_no,doc_date,doc_cd,SR_CD,ac_cd,"
                        + "PCS,TRNS_ID,time_stamp,rate,tag_no,inv_no) values(?,?,?,?,?,?,?,?,?,?,?)";

                PreparedStatement pstUpdate = null;
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, refNo);
                pstUpdate.setDate(2, date);
                pstUpdate.setString(3, "STK");
                pstUpdate.setString(4, rsLocal.getString("SR_CD"));
                pstUpdate.setString(5, "");
                pstUpdate.setInt(6, (int) Math.abs(rsLocal.getDouble("QTY")));
                if (rsLocal.getInt("qty") > 0) {
                    pstUpdate.setString(7, "R");
                } else {
                    pstUpdate.setString(7, "I");
                }
                pstUpdate.setTimestamp(8, ts);
                pstUpdate.setDouble(9, 0.00);
                pstUpdate.setString(10, rsLocal.getString("tag_no"));
                pstUpdate.setString(11, inv_no);
                i += pstUpdate.executeUpdate();

                if (rsLocal.getInt("qty") > 0) {
                    sqlUpdate = "update oldb0_1 set PPUR_" + (date.getMonth() + 1) + "=PPUR_" + (date.getMonth() + 1)
                            + "+? where SR_CD=?";
                } else {
                    sqlUpdate = "update oldb0_1 set PSAL_" + (date.getMonth() + 1) + "=PSAL_" + (date.getMonth() + 1)
                            + "+? where SR_CD=?";
                }
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setInt(1, (int) Math.abs(rsLocal.getDouble("QTY")));
                pstUpdate.setString(2, rsLocal.getString("SR_CD"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update tag set is_del=1 where ref_no=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
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

        String sql = "select * from STKADJHD where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            Date date = rsLocal.getDate("v_DATE");

            sql = "select * from STKADJDT where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            PreparedStatement pstUpdate = null;
            String sqlUpdate = "delete from oldb0_2 where doc_ref_no=?";
            pstUpdate = con.prepareStatement(sqlUpdate);
            pstUpdate.setString(1, refNo);
            i += pstUpdate.executeUpdate();
            while (rsLocal.next()) {
                if (rsLocal.getInt("qty") > 0) {
                    sqlUpdate = "update oldb0_1 set PPUR_" + (date.getMonth() + 1) + "=PPUR_" + (date.getMonth() + 1)
                            + "-? where SR_CD=?";
                } else {
                    sqlUpdate = "update oldb0_1 set PSAL_" + (date.getMonth() + 1) + "=PSAL_" + (date.getMonth() + 1)
                            + "-? where SR_CD=?";
                }
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setInt(1, (int) Math.abs(rsLocal.getDouble("QTY")));
                pstUpdate.setString(2, rsLocal.getString("SR_CD"));
                i += pstUpdate.executeUpdate();

                sqlUpdate = "update tag set is_del=0 where ref_no=?";
                pstUpdate = con.prepareStatement(sqlUpdate);
                pstUpdate.setString(1, rsLocal.getString("PUR_TAG_NO"));
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
}
