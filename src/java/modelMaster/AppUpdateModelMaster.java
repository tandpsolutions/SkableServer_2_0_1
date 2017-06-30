/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelMaster;

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
public class AppUpdateModelMaster extends HttpServlet {

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
        String model_cd = request.getParameter("model_cd");
        final String model_name = request.getParameter("model_name");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String brand_cd = request.getParameter("brand_cd");
        final String tax_cd = request.getParameter("tax_cd");
        final String gst_cd = request.getParameter("GST_CD");
        final String user_id = request.getParameter("user_id");
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                if (model_cd.equalsIgnoreCase("")) {
                    model_cd = lb.generateKey(dataConnection, "MODELMST", "MODEL_CD", "M", 7);
                    String sql = "insert into MODELMST (MODEL_CD,MODEL_NAME,BRAND_CD,user_id,TAX_CD,type_cd,sub_type_cd,gst_cd) values(?,?,?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, model_cd);
                    pstLocal.setString(2, model_name);
                    pstLocal.setString(3, brand_cd);
                    pstLocal.setString(4, user_id);
                    pstLocal.setString(5, tax_cd);
                    pstLocal.setString(6, type_cd);
                    pstLocal.setString(7, sub_type_cd);
                    pstLocal.setString(8, gst_cd);
                    pstLocal.executeUpdate();
                } else if (!model_cd.equalsIgnoreCase("")) {
                    String sql = "update MODELMST set MODEL_NAME=?,BRAND_CD=?,edit_no=edit_no+1,user_id=?,time_stamp=current_timestamp,TAX_CD=?,type_cd=?"
                            + ",sub_type_cd=?,gst_cd=? where MODEL_CD=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, model_name);
                    pstLocal.setString(2, brand_cd);
                    pstLocal.setString(3, user_id);
                    pstLocal.setString(4, tax_cd);
                    pstLocal.setString(5, type_cd);
                    pstLocal.setString(6, sub_type_cd);
                    pstLocal.setString(7, gst_cd);
                    pstLocal.setString(8, model_cd);
                    pstLocal.executeUpdate();
                }
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("model_cd", model_cd);
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
