/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

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
public class GetStockValueSummary extends HttpServlet {

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
        final String code = request.getParameter("code");
        final String mode = request.getParameter("mode");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String GD_CD = request.getParameter("GD_CD");
        final String branch_cd = request.getParameter("branch_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select b.BRAND_NAME,m.model_name,t.branch_cd,SR_NAME,sum(PUR_RATE) as value,count(*) as PCS from tag t left join "
                        + " SERIESMST s on t.sr_cd=s.sr_cd left join MODELMST m on s.model_cd=m.model_cd left join brandmst b on m.brand_cd=b.brand_cd"
                        + " left join lbrphd l on l.ref_no=t.PUR_REF_NO "
                        + "left join acntmst a on l.ac_cd=a.ac_cd"
                        + " where t.is_del = 0 and t.PUR_REF_NO <>'' ";
                if (mode.equalsIgnoreCase("brand")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and b.BRAND_CD='" + code + "'";
                    }
                } else if (mode.equalsIgnoreCase("series")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and s.sr_cd='" + code + "'";
                    }
                } else if (mode.equalsIgnoreCase("model")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and m.model_cd='" + code + "'";
                    }
                }

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "'";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "'";
                }
                if (!GD_CD.equalsIgnoreCase("2")) {
                    sql += " and godown =" + GD_CD;
                }
                if (!branch_cd.equalsIgnoreCase("")) {
                    sql += " and t.branch_cd='" + branch_cd + "' ";
                }
                sql += " group by b.BRAND_NAME,m.model_name,t.branch_cd,SR_NAME";
                sql += " order by sum(PUR_RATE) desc,b.BRAND_NAME,m.model_name,s.sr_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
                    object.addProperty("VALUE", rsLocal.getDouble("value"));
                    object.addProperty("PCS", rsLocal.getDouble("PCS"));
                    object.addProperty("branch_cd", rsLocal.getString("branch_cd"));
                    array.add(object);
                }
                lb.closeResultSet(rsLocal);
                lb.closeStatement(pstLocal);
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
