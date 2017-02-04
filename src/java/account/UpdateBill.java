/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class UpdateBill extends HttpServlet {

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
        final Library lb = Library.getInstance();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final String dr_doc_ref_no = (request.getParameter("dr_doc_ref_no"));
        final String cr_doc_ref_no = (request.getParameter("cr_doc_ref_no"));
        final String dr_doc_cd = (request.getParameter("DR_DOC_CD"));
        final String cr_doc_cd = (request.getParameter("CR_DOC_CD"));
        final String dr_inv_no = (request.getParameter("DR_INV_NO"));
        final String cr_inv_no = (request.getParameter("CR_INV_NO"));
        final double dr_amt = Double.parseDouble(request.getParameter("DR_AMT"));
        final double cr_amt = Double.parseDouble(request.getParameter("CR_AMT"));
        final int dr_sr_no = (int) Double.parseDouble(request.getParameter("DR_SR_NO"));
        final int cr_sr_no = (int) Double.parseDouble(request.getParameter("CR_SR_NO"));
        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                PreparedStatement pstLocal = null;
                String sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT-? where DOC_REF_NO=?";
                pstLocal = dataConnection.prepareStatement(sql);
                if (dr_amt + cr_amt >= 0) {
                    pstLocal.setDouble(1, cr_amt * -1);
                } else {
                    pstLocal.setDouble(1, dr_amt);
                }
                pstLocal.setString(2, dr_doc_ref_no);
                pstLocal.executeUpdate();

                sql = "update oldb2_4 set UNPAID_AMT=UNPAID_AMT+? where DOC_REF_NO=?";
                pstLocal = dataConnection.prepareStatement(sql);
                if (dr_amt + cr_amt >= 0) {
                    pstLocal.setDouble(1, cr_amt * -1);
                } else {
                    pstLocal.setDouble(1, dr_amt);
                }
                pstLocal.setString(2, cr_doc_ref_no);
                pstLocal.executeUpdate();

                sql = "INSERT INTO billadjst (doc_ref_no,DR_DOC_REF_NO,CR_DOC_REF_NO,DR_DOC_CD,CR_DOC_CD,DR_INV_NO,CR_INV_NO,AMT,dr_sr_no,cr_sr_no) values(?,?,?,?,?,?,?,?,?,?)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, lb.generateKey(dataConnection, "billadjst", "DOC_REF_NO", "BI", 7));
                pstLocal.setString(2, dr_doc_ref_no);
                pstLocal.setString(3, cr_doc_ref_no);
                pstLocal.setString(4, dr_doc_cd);
                pstLocal.setString(5, cr_doc_cd);
                pstLocal.setString(6, dr_inv_no);
                pstLocal.setString(7, cr_inv_no);
                pstLocal.setDouble(8, (dr_amt >= Math.abs(cr_amt)) ? Math.abs(cr_amt) : dr_amt);
                pstLocal.setInt(9, dr_sr_no);
                pstLocal.setInt(10, cr_sr_no);
                pstLocal.executeUpdate();

                dataConnection.commit();
                dataConnection.setAutoCommit(true);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
                try {
                    dataConnection.rollback();
                    dataConnection.setAutoCommit(true);
                } catch (Exception e) {

                }
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
                try {
                    dataConnection.rollback();
                    dataConnection.setAutoCommit(true);
                } catch (Exception e) {

                }
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
