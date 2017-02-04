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
public class CardWiseSalesDetailStatement extends HttpServlet {

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
        final String ac_cd = request.getParameter("ac_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.ins_amt,v.ref_no,case when v.V_TYPE=0 then 'Retail Invoice' else 'Tax Invoice' end as v_type,v.INV_NO,v.V_DATE,v.PMT_MODE,"
                        + " case when a.FNAME is null then '' else fname end as fname,s.SR_ALIAS,s.SR_NAME,v1.IMEI_NO,"
                        + " v1.SERAIL_NO,v1.QTY,v1.RATE,v1.AMT,v.DET_TOT,v1.TAX_AMT,v1.ADD_TAX_AMT,v.NET_AMT,p.CASH_AMT,p.BANK_AMT,p.CARD_AMT,p.bajaj_amt,t.TAX_NAME"
                        + " ,v.BUY_BACK_MODEL,v.BUY_BACK_AMT from VILSHD v left join VILSDT v1 on v.REF_NO =v1.REF_NO left join acntmst a on v.AC_CD=a.AC_CD "
                        + " left join SERIESMST s on s.SR_CD=v1.SR_CD left join PAYMENT p on v.REF_NO=p.REF_NO   left join TAXMST t on v1.TAX_CD=t.TAX_CD "
                        + " where is_del=0 and card_no<>''";
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and v.ac_cd='" + ac_cd + "' ";
                }
                sql += " order by v.V_DATE,v.INV_NO";
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
                    object.addProperty("AMT", viewDataRs.getDouble("AMT"));
                    object.addProperty("det_tot", viewDataRs.getDouble("DET_TOT"));
                    object.addProperty("tax_amt", viewDataRs.getDouble("TAX_AMT"));
                    object.addProperty("add_tax_amt", viewDataRs.getDouble("ADD_TAX_AMT"));
                    object.addProperty("CASH_AMT", viewDataRs.getDouble("CASH_AMT"));
                    object.addProperty("BANK_AMT", viewDataRs.getDouble("BANK_AMT"));
                    object.addProperty("CARD_AMT", viewDataRs.getDouble("CARD_AMT"));
                    object.addProperty("net_amt", viewDataRs.getDouble("NET_AMT"));
                    object.addProperty("tax_name", viewDataRs.getString("tax_name"));
                    object.addProperty("buy_back_model", viewDataRs.getString("buy_back_model"));
                    object.addProperty("ins_amt", viewDataRs.getDouble("ins_amt"));
                    object.addProperty("buy_back_amt", viewDataRs.getDouble("buy_back_amt"));
                    object.addProperty("bajaj_amt", viewDataRs.getDouble("bajaj_amt"));
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
