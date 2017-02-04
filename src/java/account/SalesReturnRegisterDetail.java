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
public class SalesReturnRegisterDetail extends HttpServlet {

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
        final int branch_cd = Integer.parseInt(request.getParameter("branch_cd"));
        final int pmt_mode = Integer.parseInt(request.getParameter("pmt_mode"));
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.ref_no,'Sales Return' as v_type,"
                        + " v.INV_NO,v.V_DATE,v.PMT_MODE,"
                        + " case when a.FNAME is null then '' else fname end as fname,s.SR_ALIAS,s.SR_NAME,v1.IMEI_NO,"
                        + " v1.SERAIL_NO,v1.QTY,v1.RATE,v1.BASIC_AMT,v1.TAX_AMT,v1.ADD_TAX_AMT,v1.DISC_RATE,v1.MRP,v1.AMT,v.NET_AMT,p.CASH_AMT,"
                        + " p.BANK_AMT,p.CARD_AMT,p.bajaj_amt,v.DET_TOT,v.branch_cd,"
                        + " t.TAX_NAME from SRHD v left join SRDT v1 on v.REF_NO =v1.REF_NO "
                        + " left join acntmst a on v.AC_CD=a.AC_CD left join SERIESMST s on s.SR_CD=v1.SR_CD left join PAYMENT p on v.REF_NO=p.REF_NO "
                        + " left join TAXMST t on v1.TAX_CD=t.TAX_CD where v_date >='" + from_date + "' and is_del=0 and v.v_date <='" + to_date + "'";
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and v.ac_cd='" + ac_cd + "' ";
                }
                if (pmt_mode != 2) {
                    sql += " and v.pmt_mode=" + pmt_mode;
                }

                if (branch_cd != 0) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                sql += " order by v.V_DATE,v.INV_NO,v1.tag_no,v1.is_main desc";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", viewDataRs.getString("REF_NO"));
                    object.addProperty("v_type", viewDataRs.getString("V_TYPE"));
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("fname", viewDataRs.getString("FNAME"));
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                    object.addProperty("SERAIL_NO", viewDataRs.getString("SERAIL_NO"));
                    object.addProperty("v_date", viewDataRs.getString("V_DATE"));
                    object.addProperty("QTY", viewDataRs.getInt("QTY"));
                    object.addProperty("RATE", viewDataRs.getDouble("RATE"));
                    object.addProperty("BASIC_AMT", viewDataRs.getDouble("BASIC_AMT"));
                    object.addProperty("tax_amt", viewDataRs.getDouble("TAX_AMT"));
                    object.addProperty("add_tax_amt", viewDataRs.getDouble("ADD_TAX_AMT"));
                    object.addProperty("DISC_RATE", viewDataRs.getDouble("DISC_RATE"));
                    object.addProperty("MRP", viewDataRs.getDouble("MRP"));
                    object.addProperty("AMT", viewDataRs.getDouble("AMT"));
                    object.addProperty("det_tot", viewDataRs.getDouble("DET_TOT"));
                    object.addProperty("CASH_AMT", viewDataRs.getDouble("CASH_AMT"));
                    object.addProperty("BANK_AMT", viewDataRs.getDouble("BANK_AMT"));
                    object.addProperty("CARD_AMT", viewDataRs.getDouble("CARD_AMT"));
                    object.addProperty("net_amt", viewDataRs.getDouble("NET_AMT"));
                    object.addProperty("tax_name", viewDataRs.getString("tax_name"));
                    object.addProperty("bajaj_amt", viewDataRs.getDouble("bajaj_amt"));
                    object.addProperty("branch_cd", viewDataRs.getDouble("branch_cd"));
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
