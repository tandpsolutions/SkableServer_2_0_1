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
public class SeriesMasterAdd {

    Connection dataConnection = null;
    Library lb = Library.getInstance();
    SysEnv clSysEnv = lb.companySetUp();

    public SeriesMasterAdd(Connection dataConnection) {
        this.dataConnection = dataConnection;
    }

    public void seriesUpdateSingle(String sr_cd, String opb, String opb_val, String branch_cd) throws SQLException {
        String sql = "";
        PreparedStatement pstLocal;
        long rec_no = lb.getRecNOFromOldb0_1(dataConnection, sr_cd, branch_cd, "0");
        if (rec_no != -1) {
            sql = "update OLDB0_1 set OPB=OPB+? where rec_no=?";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, opb);
            pstLocal.setLong(2, rec_no);
            pstLocal.executeUpdate();
        } else {
            sql = "insert into OLDB0_1 (SR_CD,OPB,BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, sr_cd);
            pstLocal.setString(2, opb);
            pstLocal.setString(3, branch_cd);
            pstLocal.setString(4, "0");
            pstLocal.executeUpdate();
        }

        sql = "insert into OLDB0_2 (DOC_REF_NO,DOC_DATE,DOC_CD,SR_CD,AC_CD,PCS,TRNS_ID,RATE,BRANCH_CD,PRD_ST_CD) values(?,?,?,?,?,?,?,?,?,?)";
        pstLocal = dataConnection.prepareStatement(sql);
        pstLocal.setString(1, "OPB");
        pstLocal.setString(2, "2015-03-31");
        pstLocal.setString(3, "OPB");
        pstLocal.setString(4, sr_cd);
        pstLocal.setString(5, "");
        pstLocal.setString(6, opb);
        pstLocal.setString(7, "O");
        pstLocal.setString(8, opb_val);
        pstLocal.setString(9, branch_cd);
        pstLocal.setString(10, "0");
        pstLocal.executeUpdate();

        lb.closeStatement(pstLocal);
    }
}
