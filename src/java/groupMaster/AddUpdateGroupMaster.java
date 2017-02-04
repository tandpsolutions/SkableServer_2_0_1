/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package groupMaster;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
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
public class AddUpdateGroupMaster extends HttpServlet {

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
        final Connection dataConnection = helper.getConnMpAdmin();
        final Library lb = Library.getInstance();
        String grp_cd = request.getParameter("GRP_CD");
        final String grp_name = request.getParameter("GRP_NAME");
        final String acc_eff = request.getParameter("ACC_EFF");
        final String head_grp_name = request.getParameter("HEAD_GRP");
        final String user_id = request.getParameter("user_id");
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                if (grp_cd.equalsIgnoreCase("")) {
                    grp_cd = lb.generateKey(dataConnection, "GROUPMST", "grp_cd", "G", 7);
                    String sql = "insert into GROUPMST (Group_Name,User_id,head,head_grp,acc_eff,grp_cd)"
                            + "values (?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, grp_name);
                    pstLocal.setString(2, user_id);
                    pstLocal.setString(3, "1");
                    pstLocal.setString(4, lb.getData(dataConnection, "GRP_CD", "GROUPMST", "GROUP_NAME", head_grp_name, 0));
                    pstLocal.setString(5, acc_eff);
                    pstLocal.setString(6, grp_cd);
                    pstLocal.executeUpdate();
                } else if (!grp_cd.equalsIgnoreCase("")) {
                    String sql = "update GROUPMST set group_Name=?,user_id=?,head_grp=?,edit_No=edit_No+1,"
                            + "time_stamp=current_timestamp where grp_cd='" + grp_cd + "'";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, grp_name);
                    pstLocal.setString(2, user_id);
                    pstLocal.setString(3, lb.getData(dataConnection, "GRP_CD", "GROUPMST", "GROUP_NAME", head_grp_name, 0));
                    pstLocal.executeUpdate();
                }
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("grp_cd", grp_cd);
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
