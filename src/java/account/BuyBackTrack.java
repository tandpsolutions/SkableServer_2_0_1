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
public class BuyBackTrack extends HttpServlet {

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
        final String jv_from_date = request.getParameter("jv_from_date");
        final String jv_to_date = request.getParameter("jv_to_date");
        final String dc_from_date = request.getParameter("dc_from_date");
        final String dc_to_date = request.getParameter("dc_to_date");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.v_date,v.branch_cd,v.part_no,v.inv_No,v.ref_no,a.fname,s.sr_name as BUY_BACK_MODEL,v.v_date,'' as TYPE_NAME,BUY_BACK_MODEL as IMEI_NO,BUY_BACK_IMEI_NO,(1) as pcs,BUY_BACK_AMT as tot_sales from VILSHD v  "
                        + " left join acntmst a on v.ac_cd=a.ac_cd left join seriesmst s on v.buy_back_model=s.sr_cd left join modelmst m on s.model_cd=m.model_cd where v.IS_DEL=0 "
                        + " and v.v_date>='" + from_date + "' "
                        + " and v.v_date<='" + to_date + "'   and v.BUY_BACK_AMT <> 0 and v.part_no not like 'IMM%' ";

                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("sr_name", viewDataRs.getString("BUY_BACK_MODEL"));
                    object.addProperty("BUY_BACK_IMEI_NO", viewDataRs.getString("BUY_BACK_IMEI_NO"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("V_DATE", viewDataRs.getString("V_DATE"));
                    array.add(object);
                }

                pstLocal = null;
                sql = "SELECT * FROM jvhd j LEFT JOIN jvdt j1 ON j.REF_NO=j1.ref_no WHERE part <>'' AND ac_cd='A000001' "
                        + "AND j.VDATE>='" + jv_from_date + "' AND j.VDATE<='" + jv_to_date + "' and j.is_del=0";
                pstLocal = dataConnection.prepareStatement(sql);
                viewDataRs = pstLocal.executeQuery();

                JsonArray array_jv = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("PART", viewDataRs.getString("PART"));
                    object.addProperty("IMEI", viewDataRs.getString("IMEI"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("VDATE", viewDataRs.getString("VDATE"));
                    array_jv.add(object);
                }

                pstLocal = null;
                sql = "SELECT * FROM dchd j LEFT JOIN dcdt j1 ON j.REF_NO=j1.ref_no WHERE  j.V_DATE>='" + dc_from_date + "' and "
                        + " j.V_DATE<='" + dc_to_date + "' and j.is_del=0";
                pstLocal = dataConnection.prepareStatement(sql);
                viewDataRs = pstLocal.executeQuery();

                JsonArray array_dc = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("TAG_NO", viewDataRs.getString("TAG_NO"));
                    object.addProperty("REMARK", viewDataRs.getString("REMARK"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("V_DATE", viewDataRs.getString("V_DATE"));
                    array_dc.add(object);
                }

                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.add("data", array);
                jResultObj.add("data_jv", array_jv);
                jResultObj.add("data_dc", array_dc);
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
