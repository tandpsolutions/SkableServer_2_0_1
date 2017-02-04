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
public class GetStockSummary extends HttpServlet {

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
        final String sr_cd = request.getParameter("sr_cd");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        final String branch_cd = request.getParameter("branch_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final boolean isNagative = Boolean.parseBoolean(request.getParameter("isNagative"));
        final boolean is_zero = Boolean.parseBoolean(request.getParameter("is_zero"));
        final boolean is_not_negative = Boolean.parseBoolean(request.getParameter("is_not_negative"));
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                String sql = "select branch_cd,i.SR_ALIAS,i.sr_cd,SR_NAME,b.brand_name,m.model_name,OPB, (";
                for (int i = 1; i <= 12; i++) {
                    sql += "PPUR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") as purchase,(";
                for (int i = 1; i <= 12; i++) {
                    sql += "PSAL_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") as sales from OLDB0_1 o left join SERIESMST i on o.SR_CD=i.SR_CD left join modelmst m on i.model_cd=m.model_cd"
                        + " left join brandmst b on m.brand_cd=b.brand_cd where (OPB+(";
                for (int i = 1; i <= 12; i++) {
                    sql += "PPUR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") -(";
                for (int i = 1; i <= 12; i++) {
                    sql += "PSAL_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ")) ";
                if (isNagative) {
                    sql += " < 0 ";
                } else if (is_zero) {
                    sql += " = 0 ";
                } else if (is_not_negative) {
                    sql += " >= 0 ";
                } else {
                    sql += " <> 0 ";
                }
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!branch_cd.equalsIgnoreCase("")) {
                    sql += " and branch_cd='" + branch_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and o.SR_CD='" + sr_cd + "'";
                }
                if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and b.brand_cd='" + brand_cd + "'";
                }
                if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and i.model_cd='" + model_cd + "'";
                }
                sql += " and SR_NAME is not null ";
                sql += " order by b.brand_name,i.sr_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("MODEL_NAME", viewDataRs.getString("MODEL_NAME"));
                    object.addProperty("BRAND_NAME", viewDataRs.getString("BRAND_NAME"));
                    object.addProperty("OPB", viewDataRs.getString("OPB"));
                    object.addProperty("PURCHASE", viewDataRs.getString("PURCHASE"));
                    object.addProperty("SALES", viewDataRs.getString("SALES"));
                    object.addProperty("SR_CD", viewDataRs.getString("SR_CD"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    array.add(object);
                }
                lb.closeResultSet(viewDataRs);
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
