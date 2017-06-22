/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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
public class SnapShots extends HttpServlet {

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
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {
                String sql = "select case when sum(SALE_RATE-PUR_RATE) is null then 0.00 else sum(SALE_RATE-PUR_RATE) end from tag t left join VILSHD v on t.SALE_REF_NO = v.REF_NO "
                        + " where v.v_date>='" + from_date + "' and "
                        + " v.v_date<='" + to_date + "'  and t.is_del=1 ";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("margin_report", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("margin_report", "0.00");
                }

                sql = "select case when sum(v.NET_AMT) is null then 0.00 else sum(v.NET_AMT) end from "
                        + " VILSHD v where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("sales", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("sales", "0.00");
                }

                sql = "select case when sum(v.NET_AMT) is null then 0.00 else sum(v.NET_AMT) end from "
                        + " SRHD v where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("sales_return", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("sales_return", "0.00");
                }

                sql = "select case when sum(v.NET_AMT) is null then 0.00 else sum(v.NET_AMT) end from "
                        + " PRHD v where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("purchase_return", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("purchase_return", "0.00");
                }

                sql = "select case when sum(v.NET_AMT) is null then 0.00 else sum(v.NET_AMT) end from "
                        + " LBRPHD v where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("purchase", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("purchase", "0.00");
                }

                sql = "select case when sum(v.NET_AMT) is null then 0.00 else sum(v.NET_AMT) end from "
                        + " LBRPHD v where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("purchase", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("purchase", "0.00");
                }

                sql = "select case when sum(PUR_RATE) is null then 0.00 else sum(PUR_RATE) end from tag t where t.is_del = 0 and (IMEI_NO <>'' or SERAIL_NO <>'') and t.PUR_REF_NO <>'' ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("mobile", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("mobile", "0.00");
                }

                sql = "select case when sum(PUR_RATE) is null then 0.00 else sum(PUR_RATE) end from tag t where t.is_del = 0 and (IMEI_NO ='' and SERAIL_NO ='') and t.PUR_REF_NO <>'' ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("access", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("access", "0.00");
                }

                sql = "select sum(OPB+";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += "-";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "-";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") as bal from oldb2_1 where (OPB+";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += "-";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "-";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") > 1 ";

                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("debtors", rsLocal.getString(1));
                } else {
                    jResultObj.addProperty("debtors", "0.00");
                }
                sql = "select sum(card_chg) from vilshd v left join  payment p on v.ref_no=p.ref_no  where v.v_date>='" + from_date + "' and "
                        + " v.v_date<='" + to_date + "' ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("swipe_charge", rsLocal.getDouble(1));
                } else {
                    jResultObj.addProperty("swipe_charge", 0.00);
                }

                sql = "select sum(fr_chg) from lbrphd v where v.v_date>='" + from_date + "' and "
                        + " v.v_date<='" + to_date + "' ";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("fr_chg", rsLocal.getDouble(1));
                } else {
                    jResultObj.addProperty("fr_chg", 0.00);
                }

                jResultObj.addProperty("result", 1);
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
