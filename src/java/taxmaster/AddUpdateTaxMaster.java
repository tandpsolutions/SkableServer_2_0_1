/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package taxmaster;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumikshah
 */
public class AddUpdateTaxMaster extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    Library lb = Library.getInstance();
    private String ac_year = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final Library lb = Library.getInstance();
        String tax_cd = request.getParameter("tax_cd");
        final String tax_name = request.getParameter("tax_name");
        final double sgst = Double.parseDouble(request.getParameter("sgst"));
        final double cgst = Double.parseDouble(request.getParameter("cgst"));
        ac_year = "2016";
        final String user_id = request.getParameter("user_id");
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                if (tax_cd.equalsIgnoreCase("")) {
                    String init = "T";
                    tax_cd = lb.generateKey(dataConnection, "TAXMST", "tax_cd", init, 7);
                    String sql = "insert into taxmst (tax_cd,tax_name,tax_per,add_tax_per,tax_on_sales,tax_ac_cd,add_tax_ac_cd,add_tax_ac_cd1,user_id) values(?,?,?,?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, tax_cd);
                    pstLocal.setString(2, tax_name);
                    pstLocal.setDouble(3, sgst);
                    pstLocal.setDouble(4, cgst);
                    pstLocal.setInt(5, 1);
                    pstLocal.setString(6, saveVoucher(dataConnection, "S" + tax_name));
                    pstLocal.setString(7, saveVoucher(dataConnection, "C" + tax_name));
                    pstLocal.setString(8, saveVoucher(dataConnection, "I" + tax_name));
                    pstLocal.setString(9, user_id);
                    pstLocal.executeUpdate();
                } else if (!tax_cd.equalsIgnoreCase("")) {
                    String sql = "update taxmst set tax_name=?,tax_per=?,add_tax_per=?,edit_no=edit_no+1,user_id=?,time_stamp=current_timestamp where tax_cd=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, tax_name);
                    pstLocal.setDouble(2, sgst);
                    pstLocal.setDouble(3, cgst);
                    pstLocal.setString(4, user_id);
                    pstLocal.setString(5, tax_cd);
                    pstLocal.executeUpdate();
                }
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("tax_cd", tax_cd);
                dataConnection.commit();
                dataConnection.setAutoCommit(true);
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
            } finally {
                lb.closeConnection(dataConnection);
            }
        }
        response.getWriter().print(jResultObj);
    }

    private String saveVoucher(Connection dataConnection, String tax_name) throws SQLException {
        String grp_cd = "G000007";

        String alias = lb.generateKey(dataConnection, "ACNTMST", "ac_alias", "J", 5);
        String sql = "insert into ACNTMST (AC_CD,fname,mname,lname,grp_cd,contact_prsn,cst,pan,ref_by,user_id,ac_alias,TIN,card_no,OPB_AMT,OPB_EFF,gst_no) "
                + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        String ac_cd = (lb.generateKey(dataConnection, "ACNTMST", "AC_CD", "T", 7));
        pstLocal.setString(1, ac_cd);
        pstLocal.setString(2, tax_name);
        pstLocal.setString(3, "");
        pstLocal.setString(4, "");
        pstLocal.setString(5, grp_cd);
        pstLocal.setString(6, "");
        pstLocal.setString(7, "");
        pstLocal.setString(8, "");
        pstLocal.setString(9, "");
        pstLocal.setString(10, "1");
        pstLocal.setString(11, alias);
        pstLocal.setString(12, "");
        pstLocal.setString(13, "");
        pstLocal.setDouble(14, 0.00);
        pstLocal.setString(16, "");
        pstLocal.setInt(15, 0);
        pstLocal.execute();

        for (int i = 0; i < 1; i++) {
            sql = "insert into adbkmst values(?,?,?,?,?,?,?)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, ac_cd);
            pstLocal.setString(2, "");
            pstLocal.setString(3, "");
            pstLocal.setString(4, "");
            int code = 0;
            pstLocal.setInt(5, code);
            pstLocal.setInt(6, code);
            pstLocal.setInt(7, i + 1);
            pstLocal.execute();
        }

        sql = "insert into phbkmst values(?,?,?,?,?,?,?,0)";
        pstLocal = dataConnection.prepareStatement(sql);
        pstLocal.setString(1, ac_cd);
        pstLocal.setString(2, "");
        pstLocal.setString(3, "");
        pstLocal.setString(4, "");
        pstLocal.setString(5, "");
        pstLocal.setString(6, "");
        pstLocal.setString(7, "");
        pstLocal.execute();
        lb.closeStatement(pstLocal);
        createAccount(dataConnection, ac_cd);

        return ac_cd;
    }

    private boolean createAccount(Connection dataConnection, String ac_cd) throws SQLException {
        PreparedStatement pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_1 "
                + "(AC_CD, OPB) VALUES (?, ?)");
        pstLocal.setString(1, ac_cd);

        pstLocal.setDouble(2, 0.00);
        pstLocal.executeUpdate();

        pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_2 "
                + "(DOC_REF_NO, DOC_DATE, DOC_CD, AC_CD,  VAL, CRDR, PARTICULAR, OPP_AC_CD,REC_DATE)"
                + " VALUES ('', '" + ac_year + "-04-01" + "', 'OPB', ?, ?, ?, ?, '0','2016-04-01')");
        pstLocal.setString(1, ac_cd);
        pstLocal.setDouble(2, 0.00);
        pstLocal.setInt(3, 0);
        pstLocal.setString(4, "Opening Balance");
        pstLocal.executeUpdate();

        lb.closeStatement(pstLocal);

        long rec_no = lb.getRecNOFromOldb0_4(dataConnection, ac_cd);
        if (rec_no == -1) {
            String sql = "insert into oldb2_4 (DOC_REF_NO,DOC_CD,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST)"
                    + " values (?,'OPB',0,'" + ac_year + "-04-01',?,?,?,'2016-04-01',0.0)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, ac_cd);
            pstLocal.setString(2, ac_cd);
            pstLocal.setDouble(3, 0.00);
            pstLocal.setDouble(4, 0.00);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
        }

        return true;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
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
     * Handles the HTTP
     * <code>POST</code> method.
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
