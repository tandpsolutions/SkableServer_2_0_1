/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package account;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
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
public class BajajReport extends HttpServlet {

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
        final int branch_cd = Integer.parseInt(request.getParameter("branch_cd"));
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select v1.v_type,v1.branch_cd,v.ref_no,v1.inv_no,a.FNAME,s.SR_NAME,v1.V_DATE as ins_date,v.TAG_NO,p.BAJAJ_AMT,p.SFID,a1.fname as bajaj_name from vilsdt v "
                        + " left join SERIESMST s on v.SR_CD=s.SR_CD left join VILSHD v1 on v.REF_NO=v1.REF_NO left join acntmst a on v1.AC_CD=a.AC_CD "
                        + " left join payment p on v1.ref_no=p.ref_no left join acntmst a1 on p.bajaj_name=a1.ac_cd "
                        + " where v1.V_DATE>='" + from_date + "'"
                        + " and v1.V_DATE<='" + to_date + "' and v1.is_del=0 and p.bajaj_amt <>0 "
                        + " and (v.IMEI_NO<>'' or v.SERAIL_NO<>'') and v.is_main=1";
                if (branch_cd != 0) {
                    sql += " and v1.branch_cd=" + branch_cd;
                }
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and p.bajaj_name='" + ac_cd + "'";
                }
                sql += " order by v1.V_DATE";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("INV_NO", viewDataRs.getString("INV_NO"));
                    object.addProperty("V_TYPE", viewDataRs.getString("V_TYPE"));
                    object.addProperty("FNAME", viewDataRs.getString("FNAME"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("TAG_NO", viewDataRs.getString("TAG_NO"));
                    object.addProperty("INS_DATE", viewDataRs.getString("ins_date"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    object.addProperty("RATE", viewDataRs.getDouble("BAJAJ_AMT"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("SFID", viewDataRs.getString("SFID"));
                    object.addProperty("BAJAJ_NAME", viewDataRs.getString("BAJAJ_NAME"));
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
