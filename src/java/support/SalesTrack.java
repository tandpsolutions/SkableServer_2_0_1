/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

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

/**
 *
 * @author bhaumik
 */
public class SalesTrack extends HttpServlet {

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
        final String branch_cd = request.getParameter("branch_cd");
        final String imei = request.getParameter("imei");
        final String name = request.getParameter("name");
        final String mobile = request.getParameter("mobile");
        final String bill_no = request.getParameter("bill_no");
        Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select l.REF_NO,l.INV_NO,l.V_TYPE,concat(a.FNAME,' ',a.MNAME,' ',a.LNAME) as ac_name,l.V_DATE,l.NET_AMT,"
                        + " CASH_AMT,BANK_AMT,CARD_AMT,BAJAJ_AMT,SFID,REMARK, p1.mobile1 from VILSHD l left join ACNTMST a on l.AC_CD=a.AC_CD "
                        + " left join phbkmst p1 on p1.ac_cd=a.ac_cd left join vilsdt v1 on l.ref_no=v1.ref_no "
                        + " left join payment p on l.ref_no=p.ref_no where  "
                        + " is_del=0 ";
                if (!from_date.equalsIgnoreCase("") && !to_date.equalsIgnoreCase("")) {
                    sql += " and v_date>='" + from_date + "' and v_date<='" + to_date + "'";
                }
                if (!branch_cd.equalsIgnoreCase("")) {
                    sql += " and  l.branch_cd=" + branch_cd + "";
                }
                if (!name.equalsIgnoreCase("")) {
                    sql += " and  concat(a.FNAME,' ',a.MNAME,' ',a.LNAME) like '%" + name + "%'";
                }
                if (!imei.equalsIgnoreCase("")) {
                    sql += " and  v1.tag_no like '%" + imei + "%'";
                }
                if (!mobile.equalsIgnoreCase("")) {
                    sql += " and  p1.mobile1 like '%" + mobile + "%'";
                }

                if (!bill_no.equalsIgnoreCase("")) {
                    sql += " and l.inv_no = " + bill_no;
                }
                sql += " order by INV_NO,v_date";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
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
                    object.addProperty("SFID", rsLocal.getString("SFID"));
                    object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
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
