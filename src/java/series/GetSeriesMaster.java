/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package series;

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
public class GetSeriesMaster extends HttpServlet {

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
        final String sr_name = request.getParameter("SR_NAME");
        final String brand_cd = request.getParameter("brand_cd");
        final JsonObject jResultObj = new JsonObject();
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select TAX_NAME,t.TYPE_NAME,t2.type_name as sub_type_name,SR_CD,SR_ALIAS,SR_NAME,BRAND_NAME,MODEL_NAME,MEMORY_NAME"
                        + ",COLOUR_NAME,RAM_NAME,CAMERA_NAME,BATTERY_NAME"
                        + " from SERIESMST s left join modelmst m on s.MODEL_CD=m.MODEL_CD left join BRANDMST b on m.BRAND_CD=b.BRAND_CD"
                        + " left join MEMORYMST m1 on s.MEMORY_CD=m1.MEMORY_CD left join COLOURMST c on s.COLOUR_CD=c.COLOUR_CD"
                        + " left join typemst t on m.type_cd=t.type_cd left join typemst t2 on m.sub_type_cd=t2.type_cd"
                        + " left join taxmst t1 on m.tax_cd=t1.tax_cd"
                        + " left join rammst r1 on s.ram_cd=r1.ram_cd"
                        + " left join cameramst c1 on s.camera_cd=c1.camera_cd"
                        + " left join batterymst b1 on s.battery_cd=b1.battery_cd"
                        + "  where sr_name <> '' ";
                if (sr_name != null) {
                    sql += " and (sr_name like '%" + sr_name + "%' or sr_alias like '%" + sr_name + "%')";
                }
                if (!brand_cd.isEmpty()) {
                    sql += " and b.BRAND_CD='" + brand_cd + "'";
                }
                sql += " order by SR_NAME,sr_alias,type_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
                    object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("MEMORY_NAME", rsLocal.getString("MEMORY_NAME"));
                    object.addProperty("COLOUR_NAME", rsLocal.getString("COLOUR_NAME"));
                    object.addProperty("TYPE_NAME", rsLocal.getString("TYPE_NAME"));
                    object.addProperty("SUB_TYPE_NAME", rsLocal.getString("SUB_TYPE_NAME"));
                    object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
                    object.addProperty("RAM_NAME", rsLocal.getString("RAM_NAME"));
                    object.addProperty("CAMERA_NAME", rsLocal.getString("CAMERA_NAME"));
                    object.addProperty("BATTERY_NAME", rsLocal.getString("BATTERY_NAME"));
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
