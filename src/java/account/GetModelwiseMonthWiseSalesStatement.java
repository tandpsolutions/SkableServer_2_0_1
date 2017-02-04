/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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
public class GetModelwiseMonthWiseSalesStatement extends HttpServlet {

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
        final String before_date = request.getParameter("before_date");
        final String after_date = request.getParameter("after_date");
        final String equal_date = request.getParameter("equal_date");
        final String date_mode = request.getParameter("date_mode");
        final String before_rate = request.getParameter("before_rate");
        final String after_rate = request.getParameter("after_rate");
        final String rate_mode = request.getParameter("rate_mode");
        final String branch_cd = request.getParameter("branch_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "SELECT SR_ALIAS,t.branch_cd,SR_NAME,SUM(CASE WHEN DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 <=15  "
                        + " THEN 1 ELSE 0 END) AS day_15, "
                        + " SUM(CASE WHEN DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 <=45 and "
                        + " DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 >15 "
                        + " THEN 1 ELSE 0 END )AS day_45, "
                        + " SUM(CASE WHEN DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 <=90 and "
                        + " DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 >45 "
                        + " THEN 1 ELSE 0 END )AS day_90, "
                        + " SUM(CASE WHEN DATEDIFF( CASE WHEN l.v_date IS NOT NULL THEN l.v_date WHEN t.PUR_DATE IS NOT NULL THEN t.PUR_DATE ELSE '2016-04-01' END ,v.V_DATE)*-1 >90 "
                        + " THEN 1 ELSE 0 END ) AS day_100  "
                        + " FROM tag t LEFT JOIN SERIESMST s ON t.sr_cd=s.sr_cd LEFT JOIN MODELMST m ON s.model_cd=m.model_cd LEFT JOIN brandmst b ON m.brand_cd=b.brand_cd  "
                        + " LEFT JOIN lbrphd l ON l.ref_no=t.PUR_REF_NO LEFT JOIN vilshd v ON v.REF_NO=t.SALE_REF_NO"
                        + " LEFT JOIN acntmst a ON l.ac_cd=a.ac_cd WHERE t.is_del = 1  "
                        + " ";
                if (mode.equalsIgnoreCase("brand")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and b.BRAND_CD='" + code + "'";
                    }
                }
                if (mode.equalsIgnoreCase("series")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and s.sr_cd='" + code + "'";
                    }
                }
                if (mode.equalsIgnoreCase("model")) {
                    if (!code.equalsIgnoreCase("")) {
                        sql += " and m.model_cd='" + code + "'";
                    }
                }

                if (date_mode.equalsIgnoreCase("2")) {
                    sql += " and v.v_date <='" + before_date + "'";
                }

                if (date_mode.equalsIgnoreCase("3")) {
                    sql += " and v.v_date >='" + after_date + "'";
                }

                if (date_mode.equalsIgnoreCase("4")) {
                    sql += " and v.v_date ='" + equal_date + "'";
                }

                if (rate_mode.equalsIgnoreCase("2")) {
                    sql += " and PUR_RATE <" + before_rate + "";
                }

                if (rate_mode.equalsIgnoreCase("3")) {
                    sql += " and PUR_RATE >=" + after_rate + "";
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
                sql += " GROUP BY t.branch_cd,sr_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("day_15", rsLocal.getString("day_15"));
                    object.addProperty("day_45", rsLocal.getString("day_45"));
                    object.addProperty("day_90", rsLocal.getString("day_90"));
                    object.addProperty("day_100", rsLocal.getString("day_100"));
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
