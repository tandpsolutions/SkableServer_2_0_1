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
public class Phonebook extends HttpServlet {

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

        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final String group_cd = request.getParameter("grp_cd");
        final int v_type = Integer.parseInt(request.getParameter("v_type"));
        final boolean sales = Boolean.parseBoolean(request.getParameter("sales"));
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                if (sales) {
                    String sql = "select distinct(a.ac_cd),a.fname,concat(a1.add1,a1.add2,a1.add3) as address,(MOBILE1),email from VILSHD v "
                            + " left join acntmst a on v.ac_cd=a.ac_cd left join phbkmst p1 on p1.ac_cd=a.ac_cd left join adbkmst a1 on a1.ac_cd=a.ac_cd"
                            + " where v.IS_DEL=0 AND LENGTH(mobile1)>8 "
                            + " and v.v_date>='" + from_date + "' "
                            + " and v.v_date<='" + to_date + "'";

                    if (!group_cd.equalsIgnoreCase("")) {
                        sql += " and a.grp_cd='" + group_cd + "' ";
                    }
                    if (v_type != 2) {
                        sql += " and v.v_type=" + v_type;
                    }

                    pstLocal = dataConnection.prepareStatement(sql);
                    ResultSet viewDataRs = pstLocal.executeQuery();

                    JsonArray array = new JsonArray();
                    while (viewDataRs.next()) {
                        JsonObject object = new JsonObject();
                        object.addProperty("fname", viewDataRs.getString("fname"));
                        object.addProperty("MOBILE1", (viewDataRs.getString("MOBILE1") == null) ? "" : viewDataRs.getString("MOBILE1"));
                        object.addProperty("address", (viewDataRs.getString("address") == null) ? "" : viewDataRs.getString("address"));
                        object.addProperty("EMAIL", (viewDataRs.getString("EMAIL") == null) ? "" : viewDataRs.getString("EMAIL"));
                        array.add(object);
                    }

                    jResultObj.addProperty("result", 1);
                    jResultObj.add("data", array);
                } else {
                    String sql = "select distinct(a.ac_cd),a.fname,concat(a1.add1,a1.add2,a1.add3) as address,(MOBILE1),email from"
                            + " acntmst a left join phbkmst p1 on p1.ac_cd=a.ac_cd left join adbkmst a1 on a1.ac_cd=a.ac_cd"
                            + " where LENGTH(mobile1)>8 ";

                    if (!group_cd.equalsIgnoreCase("")) {
                        sql += " and a.group_cd='" + group_cd + "' ";
                    }

                    pstLocal = dataConnection.prepareStatement(sql);
                    ResultSet viewDataRs = pstLocal.executeQuery();

                    JsonArray array = new JsonArray();
                    while (viewDataRs.next()) {
                        JsonObject object = new JsonObject();
                        object.addProperty("fname", viewDataRs.getString("fname"));
                        object.addProperty("MOBILE1", (viewDataRs.getString("MOBILE1") == null) ? "" : viewDataRs.getString("MOBILE1"));
                        object.addProperty("address", (viewDataRs.getString("address") == null) ? "" : viewDataRs.getString("address"));
                        object.addProperty("EMAIL", (viewDataRs.getString("EMAIL") == null) ? "" : viewDataRs.getString("EMAIL"));
                        array.add(object);
                    }

                    jResultObj.addProperty("result", 1);
                    jResultObj.add("data", array);
                }
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
