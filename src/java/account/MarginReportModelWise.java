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
public class MarginReportModelWise extends HttpServlet {

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
                String sql = " SELECT b.BRAND_NAME,v.branch_cd,MODEL_NAME,COUNT(t.tag_no) as pcs,SUM(SALE_RATE-PUR_RATE) AS profit,SUM(SALE_RATE-PUR_RATE)/COUNT(t.tag_no) AS AVG FROM tag t LEFT JOIN  SERIESMST s ON t.sr_cd=s.sr_cd \n"
                        + " LEFT JOIN MODELMST m ON s.model_cd=m.model_cd LEFT JOIN brandmst b ON m.brand_cd=b.brand_cd LEFT JOIN VILSHD v ON t.SALE_REF_NO = v.REF_NO"
                        + " where v.v_date>='" + from_date + "' and "
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
                if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd='" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and v.branch_cd=" + branch_cd;
                }

                sql += " group by b.brand_name,v.branch_cd,MODEL_NAME order by sum(SALE_RATE-PUR_RATE)/count(tag_no) desc";

//                String sql = "select t.TAG_NO,SR_NAME,PUR_RATE,SALE_RATE,b.BRAND_NAME from tag t left join "
//                        + " SERIESMST s on t.sr_cd=s.sr_cd left join MODELMST m on s.model_cd=m.model_cd left join brandmst b on "
//                        + "m.brand_cd=b.brand_cd left join vilshd v on t.sale_ref_no=v.ref_no  "
//                        + " where v.v_date>='" + from_date + "' and "
//                        + " v.v_date<='" + to_date + "' ";
//                sql += " order by SR_NAME";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
                    object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
                    object.addProperty("PROFIT", rsLocal.getDouble("PROFIT"));
                    object.addProperty("PCS", rsLocal.getDouble("PCS"));
                    object.addProperty("AVG", rsLocal.getDouble("AVG"));
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
