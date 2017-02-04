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
public class DailySalesStatement extends HttpServlet {

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
        final int mode = Integer.parseInt(request.getParameter("mode"));
        final int branch_cd = Integer.parseInt(request.getParameter("branch_cd"));
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "SELECT v.branch_cd,v.v_date,SUM(v.ins_amt) AS ins,SUM(NET_AMT) AS net_amt,"
                        + " SUM(v.BUY_BACK_AMT) AS buy_back,0.00 AS adjst,SUM(p.CASH_AMT) AS cash,SUM(p.CARD_AMT) AS card,SUM(p.BANK_AMT) AS bank,"
                        + " SUM(p.BAJAJ_AMT) AS bajaj,SUM(p.bajaj_chg+p.card_chg) AS BANK_CHARGES  FROM VILSHD v LEFT JOIN PAYMENT p ON v.REF_NO=p.REF_NO"
                        + "  where is_del =0 and v.v_date>='" + from_date + "' and v.v_date<='" + to_date + "'";
                if (mode != 2) {
                    sql += " and v.V_TYPE=" + mode;
                }
                if (branch_cd != 0) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                sql += " group by v.v_date,v.branch_cd";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("ins", viewDataRs.getDouble("ins"));
                    object.addProperty("net_amt", viewDataRs.getDouble("net_amt"));
                    object.addProperty("buy_back", viewDataRs.getDouble("buy_back"));
                    object.addProperty("cash", viewDataRs.getDouble("cash"));
                    object.addProperty("card", viewDataRs.getString("card"));
                    object.addProperty("bank", viewDataRs.getString("bank"));
                    object.addProperty("bajaj", viewDataRs.getString("bajaj"));
                    object.addProperty("adjst", viewDataRs.getString("adjst"));
                    object.addProperty("bank_charges", viewDataRs.getString("BANK_CHARGES"));
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
