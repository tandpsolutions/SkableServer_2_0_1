/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package accountMaster;

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
public class GetAccountMaster extends HttpServlet {

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
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final String ac_name = request.getParameter("AC_NAME");
        final String grp_name = request.getParameter("GRP_NAME");
        final String zero = request.getParameter("ZERO");
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select a.OPB_AMT,a.OPB_EFF,a.AC_CD,FNAME,g.GROUP_NAME,g.GRP_CD,CST,TIN,a1.ADD1,p.MOBILE1,a.card_no,p.EMAIL,REF_BY,gst_no from ACNTMST a left join GROUPMST g on a.GRP_CD=g.GRP_CD \n"
                        + "left join ADBKMST a1 on a.AC_CD=a1.AC_CD left join PHBKMST p on a.AC_CD=p.AC_CD where fname <>''";
                if (ac_name != null) {
                    sql += " and fname like '%" + ac_name + "%'";
                }
                if (grp_name != null && !grp_name.equalsIgnoreCase("ALL")) {
                    sql += " and GROUP_NAME = '" + grp_name + "'";
                }
                if (zero != null) {
                    sql += " and (opb_amt > 1 || opb_amt < -1) ";
                }
                sql += " order by fname";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
                    object.addProperty("FNAME", rsLocal.getString("FNAME"));
                    object.addProperty("GROUP_NAME", rsLocal.getString("GROUP_NAME"));
                    object.addProperty("GRP_CD", rsLocal.getString("GRP_CD"));
                    object.addProperty("CST", rsLocal.getString("CST"));
                    object.addProperty("TIN", rsLocal.getString("TIN"));
                    object.addProperty("ADD1", rsLocal.getString("ADD1"));
                    object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
                    object.addProperty("EMAIL", rsLocal.getString("EMAIL"));
                    object.addProperty("CARD_NO", rsLocal.getString("CARD_NO"));
                    object.addProperty("OPB_AMT", rsLocal.getString("OPB_AMT"));
                    object.addProperty("OPB_EFF", rsLocal.getString("OPB_EFF"));
                    object.addProperty("REF_BY", rsLocal.getString("REF_BY"));
                    object.addProperty("GST_NO", rsLocal.getString("GST_NO"));
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
