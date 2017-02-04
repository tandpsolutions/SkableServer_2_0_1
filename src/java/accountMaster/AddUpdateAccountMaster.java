/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accountMaster;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.AccountMasterModel;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateAccountMaster extends HttpServlet {

    Library lb = Library.getInstance();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();

        final String acString = request.getParameter("ac_model");
        final JsonObject jResultObj = new JsonObject();
        AccountMasterModel acc = new Gson().fromJson(acString, AccountMasterModel.class);
        try {
            dataConnection.setAutoCommit(false);
            acc = saveVoucher(dataConnection, acc);
            dataConnection.commit();
            dataConnection.setAutoCommit(true);
            jResultObj.addProperty("result", 1);
            jResultObj.addProperty("Cause", "success");
            jResultObj.addProperty("ac_cd", acc.getAC_CD());
        } catch (SQLNonTransientConnectionException ex1) {
            jResultObj.addProperty("result", -1);
            jResultObj.addProperty("Cause", "Server is down");
            try {
                dataConnection.rollback();
                dataConnection.setAutoCommit(true);
            } catch (SQLException e) {

            }
        } catch (SQLException ex) {
            jResultObj.addProperty("result", -1);
            jResultObj.addProperty("Cause", ex.getMessage());
            try {
                dataConnection.rollback();
                dataConnection.setAutoCommit(true);
            } catch (SQLException e) {

            }
        } catch (Exception ex) {
            jResultObj.addProperty("result", -1);
            jResultObj.addProperty("Cause", ex.getMessage());
            try {
                dataConnection.rollback();
                dataConnection.setAutoCommit(true);
            } catch (SQLException e) {

            }
        }
        response.getWriter().print(jResultObj);
    }

    private AccountMasterModel saveVoucher(Connection dataConnection, AccountMasterModel acc) throws SQLException {
        String grp_cd = acc.getGRP_CD();

        if (acc.getAC_CD().equalsIgnoreCase("")) {
            String alias = lb.generateKey(dataConnection, "ACNTMST", "ac_alias", "J", 5);
            String sql = "insert into ACNTMST (AC_CD,fname,mname,lname,grp_cd,contact_prsn,cst,pan,ref_by,user_id,ac_alias,TIN,card_no,OPB_AMT,OPB_EFF) "
                    + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            acc.setAC_CD(lb.generateKey(dataConnection, "ACNTMST", "AC_CD", "A", 7));
            pstLocal.setString(1, acc.getAC_CD());
            pstLocal.setString(2, acc.getFNAME());
            pstLocal.setString(3, "");
            pstLocal.setString(4, "");
            pstLocal.setString(5, grp_cd);
            pstLocal.setString(6, "");
            pstLocal.setString(7, acc.getCST());
            pstLocal.setString(8, "");
            pstLocal.setString(9, (acc.getREF_BY() == null) ? "" : acc.getREF_BY());
            pstLocal.setString(10, "1");
            pstLocal.setString(11, alias);
            pstLocal.setString(12, acc.getTIN());
            pstLocal.setString(13, acc.getCARD_NO());
            pstLocal.setDouble(14, acc.getOPB_AMT());
            pstLocal.setInt(15, acc.getOPB_EFF());
            pstLocal.execute();

            sql = "insert into adbkmst values(?,?,?,?,?,?)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getAC_CD());
            pstLocal.setString(2, acc.getADD1());
            pstLocal.setString(3, "");
            pstLocal.setString(4, "");
            int code = 0;
            pstLocal.setInt(5, code);
            pstLocal.setInt(6, code);
            pstLocal.execute();

            sql = "insert into phbkmst values(?,?,?,?,?,?,?,0)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getAC_CD());
            pstLocal.setString(2, "");
            pstLocal.setString(3, "");
            pstLocal.setString(4, acc.getMOBILE1());
            pstLocal.setString(5, "");
            pstLocal.setString(6, acc.getEMAIL());
            pstLocal.setString(7, "");
            pstLocal.execute();
            lb.closeStatement(pstLocal);
            createAccount(dataConnection, acc);

        } else if (!acc.getAC_CD().equalsIgnoreCase("")) {
            String sql = "update acntmst set fname=?,grp_cd=?,"
                    + "cst=?,user_id=?,edit_no=edit_no+1, TIN=?,card_no=?,OPB_AMT=?,OPB_EFF=?,ref_by=? where ac_cd=?";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getFNAME());
            pstLocal.setString(2, grp_cd);
            pstLocal.setString(3, acc.getCST());
            pstLocal.setString(4, "1");
            pstLocal.setString(5, acc.getTIN());
            pstLocal.setString(6, acc.getCARD_NO());
            pstLocal.setDouble(7, acc.getOPB_AMT());
            pstLocal.setInt(8, acc.getOPB_EFF());
            pstLocal.setString(9, acc.getREF_BY());
            pstLocal.setString(10, acc.getAC_CD());
            pstLocal.execute();

            sql = "update adbkmst set add1=? where ac_cd=?";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getADD1());
            pstLocal.setString(2, acc.getAC_CD());
            pstLocal.execute();

            sql = "update phbkmst set mobile1=?,email=? where ac_cd=?";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getMOBILE1());
            pstLocal.setString(2, acc.getEMAIL());
            pstLocal.setString(3, acc.getAC_CD());
            pstLocal.execute();
            lb.closeStatement(pstLocal);
            editAC(dataConnection, acc);
        }

        if (acc.getRef_cd() != null && !acc.getRef_cd().equalsIgnoreCase("")) {
            String sql = "update vilshd set ref_cd=? where ac_cd=?";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getRef_cd());
            pstLocal.setString(2, acc.getAC_CD());
            pstLocal.execute();
        }
        return acc;
    }

    private boolean createAccount(Connection dataConnection, AccountMasterModel acc) throws SQLException {
        PreparedStatement pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_1 "
                + "(AC_CD, OPB) VALUES (?, ?)");
        pstLocal.setString(1, acc.getAC_CD());
        if (acc.getOPB_EFF() == 0) {
            pstLocal.setDouble(2, acc.getOPB_AMT());
        } else {
            pstLocal.setDouble(2, acc.getOPB_AMT() * -1);
        }
        pstLocal.executeUpdate();

        pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_2 "
                + "(DOC_REF_NO, DOC_DATE, DOC_CD, AC_CD,  VAL, CRDR, PARTICULAR, OPP_AC_CD,REC_DATE)"
                + " VALUES ('', '2016-04-01" + "', 'OPB', ?, ?, ?, ?, '0','2016-04-01')");
        pstLocal.setString(1, acc.getAC_CD());
        if (acc.getOPB_EFF() == 0) {
            pstLocal.setDouble(2, acc.getOPB_AMT());
        } else {
            pstLocal.setDouble(2, acc.getOPB_AMT() * -1);
        }
        pstLocal.setInt(3, 0);
        pstLocal.setString(4, "Opening Balance");
        pstLocal.executeUpdate();

        lb.closeStatement(pstLocal);

        long rec_no = lb.getRecNOFromOldb0_3(dataConnection, acc.getAC_CD());
        if (rec_no == -1) {
            if (acc.getOPB_EFF() == 0) {
                String sql = "insert into oldb2_4 (DOC_REF_NO,DOC_CD,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST)"
                        + " values (?,'OPB',0,'2016-04-01',?,?,?,'2016-04-01',0.0)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, acc.getAC_CD());
                pstLocal.setString(2, acc.getAC_CD());
                pstLocal.setDouble(3, acc.getOPB_AMT());
                pstLocal.setDouble(4, acc.getOPB_AMT());
                pstLocal.executeUpdate();
                lb.closeStatement(pstLocal);
            } else if (acc.getOPB_EFF() == 1) {
                String sql = "insert into oldb2_4 (DOC_REF_NO,DOC_CD,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST)"
                        + " values (?,'OPB',0,'2016-04-01',?,?,?,'2016-04-01',0.0)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, acc.getAC_CD());
                pstLocal.setString(2, acc.getAC_CD());
                pstLocal.setDouble(3, acc.getOPB_AMT() * -1);
                pstLocal.setDouble(4, acc.getOPB_AMT() * -1);
                pstLocal.executeUpdate();
                lb.closeStatement(pstLocal);
            }
        }

        return true;
    }

    private void editAC(Connection dataConnection, AccountMasterModel acc) throws SQLException {
        PreparedStatement pstLocal = dataConnection.prepareStatement("UPDATE OLDB2_1 "
                + "SET OPB=? WHERE AC_CD=? ");
        double opb = 0.00;
        if (acc.getOPB_EFF() == 0) {
            pstLocal.setDouble(1, acc.getOPB_AMT());
        } else {
            pstLocal.setDouble(1, acc.getOPB_AMT() * -1);
        }
        pstLocal.setString(2, acc.getAC_CD());
        pstLocal.executeUpdate();

        pstLocal = dataConnection.prepareStatement(""
                + "delete from oldb2_2 where ac_cd='" + acc.getAC_CD() + "' and doc_cd='OPB' ");
        pstLocal.executeUpdate();

        pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_2 "
                + "(DOC_REF_NO, DOC_DATE, DOC_CD, AC_CD,  VAL, CRDR, PARTICULAR, OPP_AC_CD,rec_date)"
                + " VALUES ('', '2016-04-01" + "', 'OPB', ?, ?, ?, ?, '0','2016-04-01')");
        pstLocal.setString(1, acc.getAC_CD());
        if (acc.getOPB_EFF() == 0) {
            pstLocal.setDouble(2, acc.getOPB_AMT());
            pstLocal.setInt(3, 0);
        } else {
            pstLocal.setDouble(2, acc.getOPB_AMT());
            pstLocal.setInt(3, 1);
        }
        pstLocal.setString(4, "Opening Balance");
        pstLocal.executeUpdate();

        lb.closeStatement(pstLocal);

        long rec_no_o_4 = lb.getRecNOFromOldb0_4(dataConnection, acc.getAC_CD());

        if (rec_no_o_4 == -1) {
            String sql = "insert into oldb2_4 (DOC_REF_NO,DOC_CD,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST)"
                    + " values (?,'OPB',0,'2016-04-01',?,?,?,'2016-04-01',0.0)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, acc.getAC_CD());
            pstLocal.setString(2, acc.getAC_CD());
            if (acc.getOPB_EFF() == 1) {
                pstLocal.setDouble(3, acc.getOPB_AMT());
                pstLocal.setDouble(4, acc.getOPB_AMT());
            } else {
                pstLocal.setDouble(3, acc.getOPB_AMT() * -1);
                pstLocal.setDouble(4, acc.getOPB_AMT() * -1);
            }
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
        } else {
            String sql = "update oldb2_4 set TOT_AMT=? where rec_no=" + rec_no_o_4;
            pstLocal = dataConnection.prepareStatement(sql);
            if (acc.getOPB_EFF() == 1) {
                pstLocal.setDouble(1, acc.getOPB_AMT());
            } else {
                pstLocal.setDouble(1, acc.getOPB_AMT() * -1);
            }
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
