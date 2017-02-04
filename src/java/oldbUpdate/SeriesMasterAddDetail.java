/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package oldbUpdate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import model.OPBSrVal;
import support.Library;
import support.SysEnv;

/**
 *
 * @author LENOVO
 */
public class SeriesMasterAddDetail {

    Connection dataConnection = null;
    Library lb = Library.getInstance();
    SysEnv clSysEnv = lb.companySetUp();

    public SeriesMasterAddDetail(Connection dataConnection) {
        this.dataConnection = dataConnection;
    }

    public void seriesUpdate(String sr_cd, List<OPBSrVal> detail) throws SQLException {
        String sql = "";
        PreparedStatement pstLocal;
        for (int i = 0; i < detail.size(); i++) {
            long rec_no = lb.getRecNOFromOldb0_1(dataConnection, sr_cd, detail.get(i).getBranch_cd(), "0");
            if (rec_no != -1) {
                sql = "update OLDB0_1 set OPB=OPB+? where rec_no=?";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, "1");
                pstLocal.setLong(2, rec_no);
                pstLocal.executeUpdate();
            } else {
                sql = "insert into OLDB0_1 (SR_CD,OPB,BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, sr_cd);
                pstLocal.setString(2, "1");
                pstLocal.setString(3, detail.get(i).getBranch_cd());
                pstLocal.setString(4, "0");
                pstLocal.executeUpdate();
            }

            sql = "insert into OLDB0_2 (DOC_REF_NO,DOC_DATE,DOC_CD,SR_CD,AC_CD,PCS,TRNS_ID,RATE,BRANCH_CD,PRD_ST_CD,TAG_NO) values(?,?,?,?,?,?,?,?,?,?,?)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, "OPB");
            pstLocal.setString(2, "2016-04-01");
            pstLocal.setString(3, "OPB");
            pstLocal.setString(4, sr_cd);
            pstLocal.setString(5, "");
            pstLocal.setString(6, "1");
            pstLocal.setString(7, "O");
            pstLocal.setDouble(8, detail.get(i).getP_rate());
            pstLocal.setString(9, detail.get(i).getBranch_cd());
            pstLocal.setString(10, "0");
            pstLocal.setString(11, detail.get(i).getTag_no());
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
        }
    }

    public void seriesUpdate(String sr_cd) throws SQLException {
        String sql = "";
        PreparedStatement pstLocal;
        sql = "select * from OPB_SR_VAL where SR_CD='" + sr_cd + "'";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            long rec_no = lb.getRecNOFromOldb0_1(dataConnection, sr_cd, rsSel.getString("BRANCH_CD"), "0");
            if (rec_no != -1) {
                sql = "update OLDB0_1 set OPB=OPB+? where rec_no=?";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, "1");
                pstLocal.setLong(2, rec_no);
                pstLocal.executeUpdate();
                lb.closeStatement(pstLocal);
            } else {
                sql = "insert into OLDB0_1 (SR_CD,OPB,BRANCH_CD,PRD_ST_CD) values (?,?,?,?)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, sr_cd);
                pstLocal.setString(2, "1");
                pstLocal.setString(3, rsSel.getString("BRANCH_CD"));
                pstLocal.setString(4, "0");
                pstLocal.executeUpdate();
                lb.closeStatement(pstLocal);
            }

            sql = "insert into OLDB0_2 (DOC_REF_NO,DOC_DATE,DOC_CD,SR_CD,AC_CD,PCS,TRNS_ID,RATE,BRANCH_CD,PRD_ST_CD,TAG_NO) values(?,?,?,?,?,?,?,?,?,?,?)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, "OPB");
            pstLocal.setString(2, "2015-03-31");
            pstLocal.setString(3, "OPB");
            pstLocal.setString(4, sr_cd);
            pstLocal.setString(5, "");
            pstLocal.setString(6, "1");
            pstLocal.setString(7, "O");
            pstLocal.setDouble(8, rsSel.getDouble("P_RATE"));
            pstLocal.setString(9, rsSel.getString("BRANCH_CD"));
            pstLocal.setString(10, "0");
            pstLocal.setString(11, rsSel.getString("TAG_NO"));
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
        }
        lb.closeResultSet(rsSel);
        lb.closeStatement(pstSel);
    }
}
