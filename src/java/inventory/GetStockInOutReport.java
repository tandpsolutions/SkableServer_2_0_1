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
public class GetStockInOutReport extends HttpServlet {

    private final DBHelper helper = DBHelper.GetDBHelper();

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
        final String from_loc = request.getParameter("from_loc");
        final String to_date = request.getParameter("to_date");
        final Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "SELECT s.ref_no,s.v_date,s.inv_no,s.from_loc,s.to_loc,s2.SR_NAME,s.approve_by,s.approve_time,t.TAG_NO "
                        + " FROM stktrfouthd s LEFT JOIN stktrfoutdt s1 ON s.ref_no=s1.ref_no LEFT JOIN tag t ON s1.pur_tag_no=t.REF_NO "
                        + " LEFT JOIN seriesmst s2 ON t.SR_CD=s2.sr_cd where s.v_date>=? and s.v_date<=? and approve_by is not null";
                if (from_loc != null && !from_loc.equalsIgnoreCase("")) {
                    sql += " and s.from_loc=" + from_loc;
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, from_date);
                pstLocal.setString(2, to_date);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", rsLocal.getString("ref_no"));
                    object.addProperty("v_date", rsLocal.getString("v_date"));
                    object.addProperty("inv_no", rsLocal.getString("inv_no"));
                    object.addProperty("from_loc", rsLocal.getString("from_loc"));
                    object.addProperty("to_loc", rsLocal.getString("to_loc"));
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
                    object.addProperty("approve_by", lb.getUserName(rsLocal.getString("approve_by")));
                    object.addProperty("approve_time", (rsLocal.getString("approve_time")));
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
