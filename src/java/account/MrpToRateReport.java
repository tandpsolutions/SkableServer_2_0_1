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
public class MrpToRateReport extends HttpServlet {

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
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        final String branch_cd = request.getParameter("branch_cd");
        final boolean on_hand = Boolean.parseBoolean(request.getParameter("on_hand"));
        final int type = Integer.parseInt(request.getParameter("type"));
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.branch_cd,v.ref_no,a.fname,s.sr_name,v.v_date,t.TYPE_NAME,"
                        + "v1.tag_no as IMEI_NO,(v1.qty) as pcs,v1.RATE,v1.MRP,BILL_NO,"
                        + "100-((v1.rate/v1.mrp)*100) AS per  from LBRPHD v left join LBRPdt v1 on v.REF_NO=v1.REF_NO "
                        + " left join SERIESMST s on v1.SR_CD=s.SR_CD left join acntmst a on v.ac_cd=a.ac_cd "
                        + " left join MODELMST m on s.MODEL_CD=m.MODEL_CD left join TYPEMST t on m.TYPE_CD=t.TYPE_CD"
                        + " left join tag t1 on v1.pur_tag_no=t1.ref_no where v.IS_DEL=0 "
                        + " and v.v_date>='" + from_date + "' "
                        + " and v.v_date<='" + to_date + "'";
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (type == 1) {
                    sql += " and 100-((v1.rate/v1.mrp)*100)>0";
                } else if (type == 2) {
                    sql += " and 100-((v1.rate/v1.mrp)*100)<0";
                } else if (type == 3) {
                    sql += " and 100-((v1.rate/v1.mrp)*100)=0";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd='" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and v.ac_cd='" + ac_cd + "'";
                }
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                if (on_hand) {
                    sql += " and t1.is_del=0";
                }

                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("sr_name", viewDataRs.getString("sr_name"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("type_name", viewDataRs.getString("type_name"));
                    object.addProperty("pcs", viewDataRs.getInt("pcs"));
                    object.addProperty("RATE", viewDataRs.getDouble("RATE"));
                    object.addProperty("MRP", viewDataRs.getDouble("MRP"));
                    object.addProperty("PER", viewDataRs.getDouble("PER"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("BILL_NO", viewDataRs.getString("BILL_NO"));
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
