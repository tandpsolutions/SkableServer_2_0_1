/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package login;

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
public class ValidateLogin extends HttpServlet {

    DBHelper helper = DBHelper.GetDBHelper();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Connection dataConnection = null;
        final JsonObject jResultObj = new JsonObject();
        final String username = request.getParameter("username");
        final String password = request.getParameter("password");
        final String branch_cd = request.getParameter("branch_cd");
        Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getMainConnection();
        }
        if (dataConnection != null) {
            try {
                String sql = "select USER_ID,USER_GRP_CD,BRANCH_CD from USERMST where user_name=? and PASSWORD=?";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, username);
                pstLocal.setString(2, password);
                ResultSet rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    if (rsLocal.getString("USER_GRP_CD").equalsIgnoreCase("1")) {
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("USER_ID", rsLocal.getString("USER_ID"));
                        jResultObj.addProperty("USER_GRP_CD", rsLocal.getString("USER_GRP_CD"));
                        jResultObj.addProperty("Cause", "Success");
                    } else {
                        if (rsLocal.getString("BRANCH_CD").equalsIgnoreCase(branch_cd)) {
                            jResultObj.addProperty("result", 1);
                            jResultObj.addProperty("USER_ID", rsLocal.getString("USER_ID"));
                            jResultObj.addProperty("USER_GRP_CD", rsLocal.getString("USER_GRP_CD"));
                            jResultObj.addProperty("Cause", "Success");
                        } else {
                            jResultObj.addProperty("result", 0);
                            jResultObj.addProperty("Cause", "Invalid Branch");
                        }
                    }
                } else {
                    jResultObj.addProperty("result", -1);
                    jResultObj.addProperty("Cause", "Username or password invalid. Please try again.");
                }
                lb.closeResultSet(rsLocal);
                lb.closeStatement(pstLocal);
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
            } finally {
                lb.closeConnection(dataConnection);
            }
        } else {
            jResultObj.addProperty("result", -1);
            jResultObj.addProperty("Cause", "Server is down");
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
