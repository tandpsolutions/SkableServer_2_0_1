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
 * @author Bhaumik
 */
public class GetCardDetail extends HttpServlet {

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
        final int branch_cd = Integer.parseInt(request.getParameter("branch_cd"));
        final Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.ref_no,v_date,inv_no,a.FNAME,v.NET_AMT,p.CARD_AMT,p.tid_no,p.card_no from vilshd v,payment p,acntmst a, phbkmst p1\n"
                        + " where v.AC_CD=a.AC_CD and v.REF_NO=p.REF_NO and a.AC_CD = p1.AC_CD and v.IS_DEL=0 and v.v_date>='" + from_date + "' "
                        + " and v.v_date <='" + to_date + "' and card_amt <>0";
                if (branch_cd != 0) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", viewDataRs.getString("ref_no"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("net_amt", viewDataRs.getString("net_amt"));
                    object.addProperty("card_amt", viewDataRs.getString("card_amt"));
                    object.addProperty("tid_no", viewDataRs.getString("tid_no"));
                    object.addProperty("card_no", viewDataRs.getString("card_no"));
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
