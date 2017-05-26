/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

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
public class GetStockLedger extends HttpServlet {

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
        final String sr_cd = request.getParameter("sr_cd");
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final String branch_cd = request.getParameter("branch_cd");
        final String doc_cd = request.getParameter("doc_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = " SELECT tag_no,inv_no, branch_cd,  "
                        + " doc_ref_no,DOC_CD,DOC_DATE,CASE WHEN fname IS NULL THEN '' ELSE CONCAT(FNAME,' ',mname,' ',lname) END AS ac_name, "
                        + " CASE WHEN TRNS_ID='I' THEN PCS ELSE 0 END AS issue,CASE WHEN TRNS_ID= 'O' OR TRNS_ID='R' THEN PCS ELSE 0 END AS receipt,  "
                        + " (SELECT SUM(CASE WHEN TRNS_ID='I' THEN PCS*-1 ELSE PCS END) FROM OLDB0_2 WHERE SR_CD=? AND doc_date <? ";
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and branch_cd=" + branch_cd;
                }
                sql += ") AS opb,o.RATE "
                        + "  FROM OLDB0_2 o LEFT JOIN ACNTMST a  ON o.AC_CD=a.AC_CD WHERE DOC_DATE >=? AND DOC_DATE <=? AND SR_CD=? ";

                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and o.branch_cd=" + branch_cd;
                }
                if (!doc_cd.equalsIgnoreCase("ALL")) {
                    sql += " and doc_cd='" + doc_cd + "'";
                }
                sql += " ORDER BY doc_date,doc_ref_no";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);

                pstLocal.setString(1, sr_cd);
                pstLocal.setString(2, from_date);
                pstLocal.setString(3, from_date);
                pstLocal.setString(4, to_date);
                pstLocal.setString(5, sr_cd);
                ResultSet viewDataRs = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("tag_no", viewDataRs.getString("tag_no"));
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    object.addProperty("doc_ref_no", viewDataRs.getString("doc_ref_no"));
                    object.addProperty("doc_cd", viewDataRs.getString("doc_cd"));
                    object.addProperty("doc_date", viewDataRs.getString("doc_date"));
                    object.addProperty("ac_name", viewDataRs.getString("ac_name"));
                    object.addProperty("issue", viewDataRs.getDouble("issue"));
                    object.addProperty("receipt", viewDataRs.getDouble("receipt"));
                    object.addProperty("opb", viewDataRs.getDouble("opb"));
                    object.addProperty("RATE", viewDataRs.getDouble("RATE"));
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
