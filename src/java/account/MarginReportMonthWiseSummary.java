/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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
public class MarginReportMonthWiseSummary extends HttpServlet {

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
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final String mode = request.getParameter("mode");
        final String sr_cd = request.getParameter("sr_cd");
        final String type_cd = request.getParameter("type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        final int v_type = Integer.parseInt(request.getParameter("v_type"));
        final String branch_cd = request.getParameter("branch_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select m.model_name ,MONTHNAME(v.v_date) as month,v.branch_cd,sum(PUR_RATE) as PUR_RATE,sum(SALE_RATE) as SALE_RATE from tag t left join "
                        + "  VILSHD v on t.SALE_REF_NO = v.REF_NO  LEFT JOIN seriesmst s ON t.SR_CD=s.SR_CD LEFT JOIN modelmst m ON m.MODEL_CD=s.MODEL_CD"
                        + " where v.v_date>='" + from_date + "' AND m.MODEL_NAME IS NOT NULL and "
                        + " v.v_date<='" + to_date + "'  and v.is_del=0 and t.is_del=1 ";

                if (mode.equalsIgnoreCase("1")) {
                    sql += " and (PUR_RATE-SALE_RATE) <0";
                }

                if (v_type != 0) {
                    sql += " and v.v_type =" + (v_type - 1);
                }

                if (mode.equalsIgnoreCase("2")) {
                    sql += " and (PUR_RATE-SALE_RATE) =0";
                }

                if (mode.equalsIgnoreCase("3")) {
                    sql += " and (PUR_RATE-SALE_RATE) >0";
                }

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd='" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and v.branch_cd=" + branch_cd;
                }

                sql += " group by model_name,MONTH(v.V_DATE),branch_cd order by model_name,MONTH(v.V_DATE),branch_cd";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("MONTH", rsLocal.getString("month"));
                    object.addProperty("branch_cd", rsLocal.getString("branch_cd"));
                    object.addProperty("PUR_RATE", rsLocal.getDouble("PUR_RATE"));
                    object.addProperty("SALE_RATE", rsLocal.getDouble("SALE_RATE"));
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
