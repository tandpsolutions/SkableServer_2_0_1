/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockTransfer;

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
public class GetStkTrfHeader extends HttpServlet {

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
        final String v_type = request.getParameter("v_type");
        final String loc = request.getParameter("loc");
        final Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "SELECT ref_no,inv_no,v_date,from_loc,to_loc,approve_by FROM stktrfhd s WHERE  v_date>=? AND v_date<=? ";
                if (v_type.equalsIgnoreCase("0")) {
                    sql += "and to_loc = ?";
                } else {
                    sql += "and to_loc = ?";
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, from_date);
                pstLocal.setString(2, to_date);
                pstLocal.setString(3, loc);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", rsLocal.getString("ref_no"));
                    object.addProperty("inv_no", rsLocal.getString("inv_no"));
                    object.addProperty("v_date", rsLocal.getString("v_date"));
                    object.addProperty("from_loc", rsLocal.getInt("from_loc"));
                    object.addProperty("to_loc", rsLocal.getInt("to_loc"));
                    if (rsLocal.getString("approve_by") == null) {
                        object.addProperty("approve_by", "Pending");
                    } else {
                        object.addProperty("approve_by", lb.getUserName(rsLocal.getString("approve_by")));
                    }
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
            } finally{
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
