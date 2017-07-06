/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sales;

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
public class GetSalesBillPrint extends HttpServlet {

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
        final String ref_no = request.getParameter("ref_no");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                PreparedStatement pstLocal = null;
                String sql = "SELECT  v1.tag_no,PMT_DAYS,INS_AMT,v.BUY_BACK_AMT,v.ADVANCE_AMT,v.INV_NO,a.FNAME,a1.ADD1,p.MOBILE1,a.TIN,v.V_DATE,s.SR_NAME"
                        + ",v1.IMEI_NO,v1.SERAIL_NO,v1.QTY,v1.RATE as RATE,v1.MRP,v1.DISC_RATE,v1.AMT,p1.CASH_AMT,v1.BASIC_AMT as BASIC,v1.TAX_AMT as TAX,v1.ADD_TAX_AMT as ADD_TAX"
                        + ",p1.CARD_AMT,p1.BANK_AMT,p1.BAJAJ_AMT,v.DET_TOT,v.TAX_AMT,v.ADD_TAX_AMT,v.NET_AMT,v.V_TYPE,p1.CHEQUE_NO,p.EMAIL,v.discount"
                        + ",case when s1.sr_name is null then '' else s1.sr_name end as BUY_BACK_MODEL,v.PART_NO,BUY_BACK_IMEI_NO,t.tax_name  "
                        + " FROM vilshd v LEFT JOIN vilsdt v1 ON v.REF_NO=v1.REF_NO LEFT JOIN seriesmst s ON v1.SR_CD=s.SR_CD left join TAXMST t on v1.tax_cd=t.tax_cd "
                        + "LEFT JOIN acntmst a ON v.AC_CD=a.AC_CD LEFT JOIN adbkmst a1 ON a.AC_CD=a1.AC_CD LEFT JOIN phbkmst p ON p.AC_CD=a.AC_CD "
                        + "LEFT JOIN payment p1 ON v.REF_NO=p1.REF_NO left join seriesmst s1 on s1.sr_cd=v.buy_back_model where v.REF_NO='" + ref_no + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("INV_NO", viewDataRs.getInt("INV_NO"));
                    object.addProperty("FNAME", viewDataRs.getString("FNAME"));
                    object.addProperty("ADD1", viewDataRs.getString("ADD1"));
                    object.addProperty("MOBILE1", viewDataRs.getString("MOBILE1"));
                    object.addProperty("EMAIL", viewDataRs.getString("EMAIL"));
                    object.addProperty("TIN", viewDataRs.getString("TIN"));
                    object.addProperty("V_DATE", viewDataRs.getString("V_DATE"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                    object.addProperty("SERAIL_NO", viewDataRs.getString("SERAIL_NO"));
                    object.addProperty("QTY", viewDataRs.getInt("QTY"));
                    object.addProperty("RATE", viewDataRs.getDouble("RATE"));
                    object.addProperty("MRP", viewDataRs.getDouble("MRP"));
                    object.addProperty("AMT", viewDataRs.getDouble("AMT"));
                    object.addProperty("DISC_RATE", viewDataRs.getDouble("DISC_RATE"));
                    object.addProperty("BASIC", viewDataRs.getDouble("BASIC"));
                    object.addProperty("TAX", viewDataRs.getDouble("TAX"));
                    object.addProperty("TAX_NAME", viewDataRs.getString("TAX_NAME"));
                    object.addProperty("ADD_TAX", viewDataRs.getDouble("ADD_TAX"));
                    object.addProperty("V_TYPE", viewDataRs.getInt("V_TYPE"));
                    object.addProperty("DET_TOT", viewDataRs.getDouble("DET_TOT"));
                    object.addProperty("TAX_AMT", viewDataRs.getDouble("TAX_AMT"));
                    object.addProperty("ADD_TAX_AMT", viewDataRs.getDouble("ADD_TAX_AMT"));
                    object.addProperty("NET_AMT", viewDataRs.getDouble("NET_AMT"));
                    object.addProperty("CASH_AMT", viewDataRs.getDouble("CASH_AMT"));
                    object.addProperty("BANK_AMT", viewDataRs.getDouble("BANK_AMT"));
                    object.addProperty("CARD_AMT", viewDataRs.getDouble("CARD_AMT"));
                    object.addProperty("BAJAJ_AMT", viewDataRs.getDouble("BAJAJ_AMT"));
                    object.addProperty("TAG_NO", viewDataRs.getString("TAG_NO"));
                    object.addProperty("CHEQUE_NO", viewDataRs.getString("CHEQUE_NO"));
                    object.addProperty("ADVANCE_AMT", viewDataRs.getString("ADVANCE_AMT"));
                    object.addProperty("BUY_BACK_AMT", viewDataRs.getString("BUY_BACK_AMT"));
                    object.addProperty("BUY_BACK_IMEI", viewDataRs.getString("BUY_BACK_IMEI_NO"));
                    object.addProperty("BUY_BACK_MODEL", viewDataRs.getString("BUY_BACK_MODEL"));
                    object.addProperty("PART_NO", viewDataRs.getString("PART_NO"));
                    object.addProperty("PMT_DAYS", viewDataRs.getString("PMT_DAYS"));
                    object.addProperty("INS_AMT", viewDataRs.getString("INS_AMT"));
                    object.addProperty("DISCOUNT", viewDataRs.getString("DISCOUNT"));
                    object.addProperty("COMPANY_TIN", lb.companySetUp().getTin_no());
                    object.addProperty("COMPANY_CST", lb.companySetUp().getCst_No());
                    object.addProperty("COMP_ADDRESS1", lb.companySetUp().getAddress_1());
                    object.addProperty("COMP_ADDRESS2", lb.companySetUp().getAddress_2());
                    object.addProperty("COMP_ADDRESS3", lb.companySetUp().getAddress_3());
                    object.addProperty("COMP_EMAIL", lb.companySetUp().getEmail());
                    object.addProperty("COMP_MOBILE", lb.companySetUp().getPhone_no());
                    object.addProperty("tax_type", "0");
                    object.addProperty("HSN_CODE", "");
                    object.addProperty("GST_NO", "");
                    object.addProperty("COMPANY_GST_NO", "");
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
