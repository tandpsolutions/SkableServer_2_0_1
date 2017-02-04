/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockTransfer;

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
public class GetStockTransferPrintOUtside extends HttpServlet {

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
                String sql = "SELECT s.ref_no,s.v_date AS tr_date,s.from_loc AS from_loc ,s.to_loc,t.tag_no,i.SR_NAME,"
                        + " t.IMEI_NO AS imei_no,t.SERAIL_NO,1 AS pcs FROM stktrfouthd s LEFT JOIN stktrfoutdt s1 ON s.ref_no=s1.ref_no"
                        + " LEFT JOIN tag t ON s1.pur_tag_no=t.REF_NO LEFT JOIN seriesmst i ON t.SR_CD=i.SR_CD  where s.REF_NO='" + ref_no + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("ref_no", viewDataRs.getString("ref_no"));
                    object.addProperty("tr_date", viewDataRs.getString("tr_date"));
                    object.addProperty("from_loc", viewDataRs.getString("from_loc"));
                    object.addProperty("to_loc", viewDataRs.getString("to_loc"));
                    object.addProperty("tag_no", viewDataRs.getString("tag_no"));
                    object.addProperty("item_name", viewDataRs.getString("SR_NAME"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                    object.addProperty("SERAIL_NO", viewDataRs.getString("SERAIL_NO"));
                    object.addProperty("pcs", viewDataRs.getString("pcs"));
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
