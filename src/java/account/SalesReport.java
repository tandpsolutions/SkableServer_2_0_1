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
public class SalesReport extends HttpServlet {

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
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "SELECT v.v_date,t.TAX_NAME,SUM(v1.BASIC_AMT) AS basic,SUM(v1.tax_amt) AS tax,SUM(v1.ADD_TAX_AMT) AS add_tax,SUM(v1.DISC_RATE) AS disc,SUM(v1.AMT) AS amt \n"
                        + "FROM vilshd v LEFT JOIN vilsdt v1 ON v.REF_NO=v1.REF_NO LEFT JOIN seriesmst s ON v1.SR_CD=s.sr_cd LEFT JOIN modelmst m ON s.MODEL_CD=m.MODEL_CD\n"
                        + "LEFT JOIN taxmst t ON m.TAX_CD=t.TAX_CD\n"
                        + "WHERE v_type <> 1 AND v.IS_DEL=0 and v_date >='" + from_date + "' "
                        + "and v_date <='" + to_date + "' and is_del=0 GROUP BY v.V_DATE,t.TAX_NAME ORDER BY v.V_DATE";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("TAX_NAME", viewDataRs.getString("TAX_NAME"));
                    object.addProperty("basic", viewDataRs.getString("basic"));
                    object.addProperty("tax", viewDataRs.getString("tax"));
                    object.addProperty("add_tax", viewDataRs.getString("add_tax"));
                    object.addProperty("disc", viewDataRs.getString("disc"));
                    object.addProperty("amt", viewDataRs.getString("amt"));
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
