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
public class DailySalesStatementDetail extends HttpServlet {

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
                String sql = "SELECT v.ref_no,(p.bajaj_chg+p.card_chg) as bank_charges,v.branch_cd,v.inv_no,v.v_type,a.fname,v.v_date,(v.ins_amt) AS ins,(net_amt) AS net_amt,0.00 AS adjst,\n"
                        + " (v.BUY_BACK_AMT) AS buy_back,(p.CASH_AMT) AS cash,(p.CARD_AMT) AS card,(p.BANK_AMT) AS bank,(p.BAJAJ_AMT) AS bajaj\n"
                        + " ,sm_name FROM VILSHD v LEFT JOIN PAYMENT p ON v.REF_NO=p.REF_NO LEFT JOIN acntmst a ON v.ac_cd=a.ac_cd\n"
                        + " LEFT JOIN vilsdt v1 ON v.REF_NO=v1.REF_NO  left join smmst sm on sm.sm_cd=v.sm_cd  WHERE is_del =0 "
                        + " and v.v_date>='" + from_date + "' "
                        + " and v.v_date<='" + to_date + "'";
                if (mode != 2) {
                    sql += " and v.V_TYPE=" + mode;
                }
                if (branch_cd != 0) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                sql += "  GROUP BY v.REF_NO order by v.v_date,v.v_type,v.inv_no";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("v_type", viewDataRs.getString("v_type"));
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("ins", viewDataRs.getDouble("ins"));
                    object.addProperty("net_amt", viewDataRs.getDouble("net_amt"));
                    object.addProperty("buy_back", viewDataRs.getDouble("buy_back"));
                    object.addProperty("cash", viewDataRs.getDouble("cash"));
                    object.addProperty("card", viewDataRs.getString("card"));
                    object.addProperty("bank", viewDataRs.getString("bank"));
                    object.addProperty("bajaj", viewDataRs.getString("bajaj"));
                    object.addProperty("adjst", viewDataRs.getString("adjst"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    object.addProperty("bank_charges", viewDataRs.getString("bank_charges"));
                    object.addProperty("sm_name", (viewDataRs.getString("sm_name") == null) ? "" : viewDataRs.getString("sm_name"));
                    object.addProperty("ref_no", (viewDataRs.getString("ref_no") == null) ? "" : viewDataRs.getString("ref_no"));
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
