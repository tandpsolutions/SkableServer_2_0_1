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
 * @author LENOVO
 */
public class SeriesMasterDelete {

    Connection dataConnection = null;
    Library lb = Library.getInstance();
    SysEnv clSysEnv = lb.companySetUp();

    public SeriesMasterDelete(Connection dataConnection) {
        this.dataConnection = dataConnection;
    }

    public void seriesUpdate(String sr_cd, String branch_cd) throws SQLException {
        String sql = "select * from opb_sr_val where sr_cd=?";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        pstLocal.setString(1, sr_cd);
        ResultSet rsLocal = pstLocal.executeQuery();
        boolean flag = true;
        while (rsLocal.next()) {
            flag = false;
            long rec_no = lb.getRecNOFromOldb0_1(dataConnection, sr_cd, rsLocal.getString("branch_cd"), "0");
            sql = "update OLDB0_1 set OPB=OPB-1 where rec_no=?";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setLong(1, rec_no);
            pstLocal.executeUpdate();
        }
        if (flag) {
            long rec_no = lb.getRecNOFromOldb0_1(dataConnection, sr_cd, branch_cd, "0");
            sql = "update OLDB0_1 set OPB=OPB-? where rec_no=? ";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, lb.getData(dataConnection, "OPB_QTY", "seriesmst", "SR_CD", sr_cd, 0));
            pstLocal.setLong(2, rec_no);
            pstLocal.executeUpdate();
        }

        sql = "delete from OLDB0_2 where sr_cd=? and DOC_CD=?";
        pstLocal = dataConnection.prepareStatement(sql);
        pstLocal.setString(1, sr_cd);
        pstLocal.setString(2, "OPB");
        pstLocal.executeUpdate();

        sql = "delete from OPB_SR_VAL where sr_cd=? ";
        pstLocal = dataConnection.prepareStatement(sql);
        pstLocal.setString(1, sr_cd);
        pstLocal.executeUpdate();

        lb.closeResultSet(rsLocal);
        lb.closeStatement(pstLocal);
    }
}
