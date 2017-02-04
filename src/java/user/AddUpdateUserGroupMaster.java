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
public class AddUpdateUserGroupMaster extends HttpServlet {

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
        final Connection dataConnection = helper.getMainConnection();
        final Library lb = Library.getInstance();
        final String user_grp = request.getParameter("USER_GRP");
        final String user_grp_cd = request.getParameter("USER_GRP_CD");
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                if (user_grp_cd.equalsIgnoreCase("")) {
                    String sql = "insert into USER_GRP_MST (USER_GRP) values(?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, user_grp);
                    pstLocal.executeUpdate();
                    addUserRightForm(dataConnection, lb.getData(dataConnection, "USER_GRP_CD", "USER_GRP_MST", "USER_GRP", user_grp, 0));
                } else if (!user_grp_cd.equalsIgnoreCase("")) {
                    String sql = "update USER_GRP_MST set USER_GRP=? where USER_GRP_CD=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, user_grp);
                    pstLocal.setString(2, user_grp_cd);
                    pstLocal.executeUpdate();
                }
                dataConnection.commit();
                dataConnection.setAutoCommit(true);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("USER_GRP_CD", lb.getData(dataConnection, "USER_GRP_CD", "USER_GRP_MST", "USER_GRP", user_grp, 0));
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

    private void addUserRightForm(Connection dataConnection, String user_grp_cd) throws SQLException {
        String sql = "select * from formmst";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        PreparedStatement pstUpdate = dataConnection.prepareStatement("insert into user_rights (USER_grp_CD,FORM_CD) values (?,?)");
        pstUpdate.setString(1, user_grp_cd);
        while (rsLocal.next()) {
            pstUpdate.setString(2, rsLocal.getString("FORM_CD"));
            pstUpdate.executeUpdate();
        }
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
