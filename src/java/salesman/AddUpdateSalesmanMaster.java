/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package salesman;

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
public class AddUpdateSalesmanMaster extends HttpServlet {

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
        String brand_cd = request.getParameter("sm_cd");
        final String brand_name = request.getParameter("sm_name");
        final String user_id = request.getParameter("user_id");
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                if (brand_cd.equalsIgnoreCase("")) {
                    brand_cd = lb.generateKey(dataConnection, "SMMST", "sm_cd", "SM", 7);
                    String sql = "insert into SMMST (SM_CD,SM_name,user_id) values(?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, brand_cd);
                    pstLocal.setString(2, brand_name);
                    pstLocal.setString(3, user_id);
                    pstLocal.executeUpdate();
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    String sql = "update SMMST set SM_name=?,edit_no=edit_no+1,user_id=?,time_stamp=current_timestamp where SM_CD=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, brand_name);
                    pstLocal.setString(2, user_id);
                    pstLocal.setString(3, brand_cd);
                    pstLocal.executeUpdate();
                }
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("sm_cd", brand_cd);
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
