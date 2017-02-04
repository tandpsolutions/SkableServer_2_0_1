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
public class ListBillsAdjsted extends HttpServlet {

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
        final String ac_cd = request.getParameter("ac_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Library lb = Library.getInstance();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "SELECT doc_ref_no,dr_doc_cd,dr_inv_no,cr_doc_cd,cr_inv_no,amt,DR_DOC_REF_NO,CR_DOC_REF_NO FROM billadjst WHERE "
                        + " DR_DOC_REF_NO IN (SELECT doc_ref_no FROM oldb2_2 WHERE ac_cd='" + ac_cd + "') OR "
                        + " CR_DOC_REF_NO IN (SELECT doc_ref_no FROM oldb2_2 WHERE ac_cd='" + ac_cd + "')";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("DOC_REF_NO", viewDataRs.getString("DOC_REF_NO"));
                    object.addProperty("DR_DOC_CD", viewDataRs.getString("DR_DOC_CD"));
                    object.addProperty("CR_DOC_CD", viewDataRs.getString("CR_DOC_CD"));
                    object.addProperty("DR_INV_NO", viewDataRs.getString("DR_INV_NO"));
                    object.addProperty("CR_INV_NO", viewDataRs.getString("CR_INV_NO"));
                    object.addProperty("TOT_AMT", viewDataRs.getString("AMT"));
                    array.add(object);
                }

                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.add("data", array);
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
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
