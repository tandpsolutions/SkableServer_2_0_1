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

/**
 *
 * @author bhaumik
 */
public class BuyBackRegister extends HttpServlet {

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
        final String ac_cd = request.getParameter("ac_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final String sr_cd = request.getParameter("sr_cd");
        final String type_cd = request.getParameter("type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.branch_cd,v.part_no,v.inv_No,v.ref_no,a.fname,s.sr_name as BUY_BACK_MODEL,v.v_date,'' as TYPE_NAME,BUY_BACK_MODEL as IMEI_NO,BUY_BACK_IMEI_NO,(1) as pcs,BUY_BACK_AMT as tot_sales from VILSHD v  "
                        + " left join acntmst a on v.ac_cd=a.ac_cd left join seriesmst s on v.buy_back_model=s.sr_cd left join modelmst m on s.model_cd=m.model_cd where v.IS_DEL=0 "
                        + " and v.v_date>='" + from_date + "' "
                        + " and v.v_date<='" + to_date + "'   and v.BUY_BACK_AMT <> 0";
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and s.brand_cd='" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and v.ac_cd='" + ac_cd + "'";
                }

                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("sr_name", viewDataRs.getString("BUY_BACK_MODEL"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("type_name", viewDataRs.getString("type_name"));
                    object.addProperty("pcs", viewDataRs.getInt("pcs"));
                    object.addProperty("tot_sales", viewDataRs.getDouble("tot_sales"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                    object.addProperty("BUY_BACK_IMEI_NO", viewDataRs.getString("BUY_BACK_IMEI_NO"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("INV_NO", viewDataRs.getString("INV_NO"));
                    object.addProperty("PART_NO", viewDataRs.getString("PART_NO"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
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
