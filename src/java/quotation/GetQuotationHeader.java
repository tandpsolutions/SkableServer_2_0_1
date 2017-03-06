/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quotation;

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
 * @author indianic
 */
public class GetQuotationHeader extends HttpServlet {

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
        Library lb =Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select l.ac_cd,l.remark,l.REF_NO,l.INV_NO,concat(a.FNAME,' ',a.MNAME,' ',a.LNAME) as ac_name,l.V_DATE,l.inv_no,l.net_amt  "
                        + " from quotationhd l left join ACNTMST a on l.AC_CD=a.AC_CD where v_date>=? and v_date<=? and is_del=0"
                        + " order by V_DATE,INV_NO";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, from_date);
                pstLocal.setString(2, to_date);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
                    object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
                    object.addProperty("AC_NAME", rsLocal.getString("ac_name"));
                    object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
                    object.addProperty("NET_AMT", rsLocal.getString("net_amt"));
                    object.addProperty("REMARK", rsLocal.getString("REMARK"));
                    object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
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
            } finally{
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
