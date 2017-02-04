/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user;

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
public class ApplyRights extends HttpServlet {

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
        Library lb = Library.getInstance();
        final Connection dataConnection = helper.getMainConnection();
        final String form_cd = request.getParameter("form_cd");
        final String user_grp_cd = request.getParameter("user_grp_cd");
        final String views = request.getParameter("VIEWS");
        final String adds = request.getParameter("ADDS");
        final String edits = request.getParameter("EDITS");
        final String deletes = request.getParameter("DELETES");
        final String prints = request.getParameter("PRINTS");
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {
                String sql = "update USER_RIGHTS set VIEWS=?,ADDS=?,EDITS=?,DELETES=?,PRINTS=? "
                        + " where USER_GRP_CD=" + user_grp_cd + " and FORM_CD=" + form_cd + "";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, views);
                pstLocal.setString(2, adds);
                pstLocal.setString(3, edits);
                pstLocal.setString(4, deletes);
                pstLocal.setString(5, prints);
                pstLocal.execute();

                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
            } finally{
                lb.closeConnection(dataConnection);
            }
            lb.closeConnection(dataConnection);
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
