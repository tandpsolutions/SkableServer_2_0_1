/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * @author bhaumik
 */
public class BranchWisePendingCollecionReport extends HttpServlet {

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
        final JsonObject jResultObj = new JsonObject();
        final int branch_cd = Integer.parseInt(request.getParameter("branch_cd"));
        final String ref_cd = (request.getParameter("ref_cd"));
        final int v_type = Integer.parseInt(request.getParameter("v_type"));
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "SELECT v.REMARK,v.INV_NO,o.DOC_REF_NO,o.DOC_DATE,o.AC_CD,o.UNPAID_AMT,o.DUE_DATE,v.BRANCH_CD,a.FNAME,v.buy_back_model,v.buy_back_amt,r.ref_name,"
                        + " o.sr_no FROM "
                        + "oldb2_4 o LEFT JOIN vilshd v ON v.REF_NO=o.DOC_REF_NO LEFT JOIN acntmst a ON a.AC_CD=v.AC_CD left join refmst r on v.ref_cd=r.ref_cd"
                        + " WHERE doc_ref_no LIKE '02%' AND o.UNPAID_AMT >0 and v.is_del=0 and v.v_date>='" + from_date + "' and v.v_date<='" + to_date + "'";
                if (branch_cd != 0) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                if (v_type != -1) {
                    sql += " and v.v_type=" + v_type;
                }
                if (!ref_cd.equalsIgnoreCase("0")) {
                    sql += " and v.REF_CD='" + ref_cd + "'";
                }
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("DOC_DATE", viewDataRs.getString("DOC_DATE"));
                    object.addProperty("INV_NO", viewDataRs.getString("INV_NO"));
                    object.addProperty("DOC_REF_NO", viewDataRs.getString("DOC_REF_NO"));
                    object.addProperty("AC_CD", viewDataRs.getString("AC_CD"));
                    object.addProperty("SR_NO", viewDataRs.getString("SR_NO"));
                    object.addProperty("DOC_CD", "");
                    object.addProperty("UNPAID_AMT", viewDataRs.getDouble("UNPAID_AMT"));
                    object.addProperty("DUE_DATE", viewDataRs.getString("DUE_DATE"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    object.addProperty("FNAME", viewDataRs.getString("FNAME"));
                    object.addProperty("BUY_BACK_MODEL", viewDataRs.getString("BUY_BACK_MODEL"));
                    object.addProperty("BUY_BACK_AMT", viewDataRs.getString("BUY_BACK_AMT"));
                    object.addProperty("REMARK", viewDataRs.getString("REMARK"));
                    object.addProperty("REF_NAME", (viewDataRs.getString("REF_NAME") == null) ? "" : viewDataRs.getString("REF_NAME"));
                    array.add(object);
                }
                lb.closeResultSet(viewDataRs);
                lb.closeStatement(pstLocal);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.add("data", array);
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
