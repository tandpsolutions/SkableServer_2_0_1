/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DC;

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
public class GetDCHeader extends HttpServlet {

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
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select l.branch_cd,l.ac_cd,l.REF_NO,l.INV_NO,l.V_TYPE,concat(a.FNAME,' ',a.MNAME,' ',a.LNAME) as ac_name,l.V_DATE,'' as BILL_NO,l1.AMT,"
                        + "l1.IMEI_NO,l1.SERAIL_NO,l1.SR_NAME,l1.remark"
                        + " from DCHD l left join DCDT l1 on l.REF_NO=l1.REF_NO \n"
                        + "left join ACNTMST a on l.AC_CD=a.AC_CD where v_date>=? and v_date<=? and v_type=? and is_del=0";
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and l.branch_cd=" + branch_cd;
                }
                sql += " order by v_date,inv_no";
//                response.getWriter().print(from_date);
//                response.getWriter().print(to_date);
//                response.getWriter().print(sql);
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
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
                    object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
                    object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
                    object.addProperty("BILL_NO", rsLocal.getString("BILL_NO"));
                    object.addProperty("NET_AMT", rsLocal.getString("AMT"));
                    object.addProperty("REMARK", rsLocal.getString("REMARK"));
                    object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
                    object.addProperty("BRANCH_CD", rsLocal.getString("BRANCH_CD"));
                    array.add(object);
                }
//                response.getWriter().print(array.toString());
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
