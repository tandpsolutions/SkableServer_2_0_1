/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelMaster;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
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
public class GetModel extends HttpServlet {

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
        final JsonObject jResultObj = new JsonObject();
        final String model_cd = request.getParameter("model_cd");
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select MODEL_CD,MODEL_NAME,m.BRAND_CD,BRAND_NAME,m.TAX_CD,t.TAX_NAME,m.TYPE_CD,t1.TYPE_NAME,t2.type_name as SUB_TYPE_NAME"
                        + ",m.sub_type_cd,hsn_code,t3.tax_name as GST_NAME,GST_CD from MODELMST m left join "
                        + " BRANDMST b on m.BRAND_CD=b.BRAND_CD"
                        + " left join TAXMST t on m.TAX_CD=t.TAX_CD "
                        + " left join TAXMST t3 on m.GST_CD=t3.TAX_CD "
                        + " left join TYPEMST t1 on m.TYPE_CD=t1.TYPE_CD"
                        + " left join TYPEMST t2 on m.SUB_TYPE_CD=t2.TYPE_CD where model_cd='" + model_cd + "'";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("MODEL_CD", rsLocal.getString("MODEL_CD"));
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("BRAND_CD", rsLocal.getString("BRAND_CD"));
                    object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
                    object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
                    object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
                    object.addProperty("TYPE_NAME", rsLocal.getString("TYPE_NAME"));
                    object.addProperty("TYPE_CD", rsLocal.getString("TYPE_CD"));
                    object.addProperty("SUB_TYPE_NAME", rsLocal.getString("SUB_TYPE_NAME"));
                    object.addProperty("SUB_TYPE_CD", rsLocal.getString("SUB_TYPE_CD"));
                    object.addProperty("HSN_CODE", rsLocal.getString("HSN_CODE"));
                    object.addProperty("GST_CD", rsLocal.getString("GST_CD"));
                    object.addProperty("GST_NAME", rsLocal.getString("GST_NAME") == null ? "" : rsLocal.getString("GST_NAME"));
                    array.add(object);
                }
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
