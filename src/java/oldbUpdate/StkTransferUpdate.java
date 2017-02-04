/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oldbUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import support.Library;
import support.SysEnv;

/**
 *
 * @author nice
 */
public class StkTransferUpdate {

    Library lb = null;
    SysEnv clSysEnv;

    public StkTransferUpdate() {
        lb = Library.getInstance();
        clSysEnv = lb.companySetUp();
    }

    public void addEntry(Connection con, String refNo) throws SQLException {
        int i = 0;

        String sql = "select * from stktrfhd  where ref_no='" + refNo + "'";
        PreparedStatement pstLocal = con.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            String v_type = rsLocal.getString("TO_LOC");

            sql = "select * from stktrfdt where ref_no='" + refNo + "'";
            pstLocal = con.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();

            while (rsLocal.next()) {
                String sqlUpdate = "update tag set godown =" + v_type + " where ref_no=?";
                PreparedStatement pstUpdate = con.prepareStatement(sqlUpdate);
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
