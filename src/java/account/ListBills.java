/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
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
public class ListBills extends HttpServlet {

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
        final boolean sales = Boolean.parseBoolean((request.getParameter("sales")));

        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "SELECT * FROM oldb2_4 WHERE ac_cd='" + ac_cd + "' and unpaid_amt <> 0";
                if (sales) {
                    sql += " AND tot_amt>0";
                } else {
                    sql += " AND tot_amt<0";
                }

                sql += " order by DOC_DATE";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("DOC_REF_NO", viewDataRs.getString("DOC_REF_NO"));
                    object.addProperty("DOC_CD", viewDataRs.getString("DOC_CD"));
                    object.addProperty("INV_NO", viewDataRs.getString("INV_NO"));
                    object.addProperty("DOC_DATE", viewDataRs.getString("DOC_DATE"));
                    object.addProperty("TOT_AMT", viewDataRs.getString("TOT_AMT"));
                    object.addProperty("UNPAID_AMT", viewDataRs.getString("UNPAID_AMT"));
                    object.addProperty("SR_NO", viewDataRs.getString("SR_NO"));
                    if (lb.isDeleted(dataConnection, viewDataRs.getString("DOC_REF_NO"))) {
                        array.add(object);
                    }
                }

                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("balance", lb.getBalance(dataConnection, ac_cd));
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
