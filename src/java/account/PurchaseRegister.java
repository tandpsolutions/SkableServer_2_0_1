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
public class PurchaseRegister extends HttpServlet {

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
        final int pmt_mode = Integer.parseInt(request.getParameter("pmt_mode"));
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.branch_cd,v.REF_NO,v.V_TYPE,v.BILL_NO as INV_NO,a.FNAME,v.V_DATE,v.DET_TOT,v.TAX_AMT,v.ADD_TAX_AMT,v.NET_AMT  from "
                        + " LBRPHD v left join acntmst a on v.AC_CD=a.AC_CD where v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 ";
                if (pmt_mode != 2) {
                    sql += " and pmt_mode=" + pmt_mode;
                }
                if (branch_cd != 0) {
                    sql += " and branch_cd=" + branch_cd;

                }
                if (mode != 2) {
                    sql += " and v_type=" + mode;
                }
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", viewDataRs.getString("REF_NO"));
                    object.addProperty("v_type", viewDataRs.getString("V_TYPE"));
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("fname", viewDataRs.getString("FNAME"));
                    object.addProperty("v_date", viewDataRs.getString("V_DATE"));
                    object.addProperty("det_tot", viewDataRs.getString("DET_TOT"));
                    object.addProperty("tax_amt", viewDataRs.getString("TAX_AMT"));
                    object.addProperty("add_tax_amt", viewDataRs.getString("ADD_TAX_AMT"));
                    object.addProperty("net_amt", viewDataRs.getString("NET_AMT"));
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
