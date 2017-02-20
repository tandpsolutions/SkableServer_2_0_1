/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sales;

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
public class GetSalesDetailOLD extends HttpServlet {

    DBHelper helper = DBHelper.GetDBHelper();

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
        Connection dataConnection = null;

        final JsonObject jResultObj = new JsonObject();
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final String v_type = request.getParameter("v_type");
        final String branch_cd = request.getParameter("branch_cd");
        Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select l.ac_Cd,l.bank_charges,l.REF_NO,l.INV_NO,l.V_TYPE,concat(a.FNAME,' ',a.MNAME,' ',a.LNAME) as ac_name,l.V_DATE,l.NET_AMT,"
                        + " CASH_AMT,BANK_AMT,CARD_AMT,BAJAJ_AMT,SFID,REMARK,l.INS_AMT,l.BUY_BACK_AMT,l.REMARK,l.PART_NO "
                        + " from VILSHDLG l left join ACNTMST a on l.AC_CD=a.AC_CD left join paymentlg p on l.ref_no=p.ref_no where v_date>=? and v_date<=? "
                        + " and v_type=? and is_del=0 ";
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and branch_cd=" + branch_cd;
                }
                sql += " order by INV_NO,v_date";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, from_date);
                pstLocal.setString(2, to_date);
                pstLocal.setString(3, v_type);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
                    object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
                    object.addProperty("V_TYPE", rsLocal.getInt("V_TYPE"));
                    object.addProperty("AC_NAME", rsLocal.getString("ac_name"));
                    object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
                    object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
                    object.addProperty("CASH_AMT", rsLocal.getDouble("CASH_AMT"));
                    object.addProperty("BANK_AMT", rsLocal.getDouble("BANK_AMT"));
                    object.addProperty("CARD_AMT", rsLocal.getDouble("CARD_AMT"));
                    object.addProperty("BAJAJ_AMT", rsLocal.getDouble("BAJAJ_AMT"));
                    object.addProperty("INS_AMT", rsLocal.getDouble("INS_AMT"));
                    object.addProperty("BUY_BACK_AMT", rsLocal.getDouble("BUY_BACK_AMT"));
                    object.addProperty("SFID", rsLocal.getString("SFID"));
                    object.addProperty("REMARK", rsLocal.getString("REMARK").toUpperCase());
                    object.addProperty("bank_charges", rsLocal.getString("bank_charges"));
                    object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
                    object.addProperty("PART_NO", rsLocal.getString("PART_NO"));
                    array.add(object);
                }
//                response.getWriter().print(array.toString());
                lb.closeResultSet(rsLocal);
                lb.closeStatement(pstLocal);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.add("purchaseHeader", array);
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
