/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package visitor;

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
public class GetVisitorBookHeader extends HttpServlet {

    DBHelper helper = DBHelper.GetDBHelper();

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
        Connection dataConnection = null;

        final JsonObject jResultObj = new JsonObject();
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final String branch_cd = request.getParameter("branch_cd");
        final String is_del = request.getParameter("is_del");
        Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select ac_name,mobile_no,c.REF_NO,VDATE,REMARK,MODEL_NAME,MEMORY_NAME,color_name,sm_name,c.is_del,c.branch_cd from visitorbook c"
                        + " left join smmst s on s.sm_cd=c.attended_by "
                        + " where VDATE>=? and VDATE<=? ";
                if (is_del != null) {
                    if (!is_del.equalsIgnoreCase("-1")) {
                        sql += " and is_del=" + is_del;
                    }
                }

                if (branch_cd != null) {
                    if (!branch_cd.equalsIgnoreCase("0")) {
                        sql += " and branch_cd=" + branch_cd;
                    }
                }
                sql += " order by VDATE,ref_no";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, from_date);
                pstLocal.setString(2, to_date);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
                    object.addProperty("VDATE", rsLocal.getString("VDATE"));
                    object.addProperty("FNAME", rsLocal.getString("ac_name"));
                    object.addProperty("REMARK", rsLocal.getString("REMARK"));
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("COLOUR_NAME", rsLocal.getString("color_name"));
                    object.addProperty("MEMORY_NAME", rsLocal.getString("MEMORY_NAME"));
                    object.addProperty("AC_CD", rsLocal.getString("mobile_no"));
                    object.addProperty("sm_name", rsLocal.getString("sm_name"));
                    object.addProperty("is_del", rsLocal.getString("is_del"));
                    object.addProperty("branch_cd", rsLocal.getString("branch_cd"));
                    array.add(object);
                }
//                response.getWriter().print(array.toString());
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
