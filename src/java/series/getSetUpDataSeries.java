/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package series;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.OPBSrVal;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class getSetUpDataSeries extends HttpServlet {

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
        String sr_cd = request.getParameter("sr_cd");
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select SR_CD,MODEL_CD,MEMORY_CD,COLOUR_CD,OPB_QTY,OPB_VAL from seriesmst where sr_cd='" + sr_cd + "'";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("SR_CD", rsLocal.getString("SR_CD"));
                    jResultObj.addProperty("MODEL_CD", rsLocal.getString("MODEL_CD"));
                    jResultObj.addProperty("MEMORY_CD", rsLocal.getString("MEMORY_CD"));
                    jResultObj.addProperty("COLOUR_CD", rsLocal.getString("COLOUR_CD"));
                    jResultObj.addProperty("OPB_QTY", rsLocal.getString("OPB_QTY"));
                    jResultObj.addProperty("OPB_VAL", rsLocal.getString("OPB_VAL"));
                }
                sql = "select * from opb_sr_val where sr_cd='" + sr_cd + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                rsLocal = pstLocal.executeQuery();
                final ArrayList<OPBSrVal> detail = new ArrayList<OPBSrVal>();
                while (rsLocal.next()) {
                    OPBSrVal data = new OPBSrVal();
                    data.setRef_no(rsLocal.getString("ref_no"));
                    data.setSr_cd(rsLocal.getString("SR_CD"));
                    data.setSerial(rsLocal.getString("SERIAL_NO"));
                    data.setImei(rsLocal.getString("IMEI_NO"));
                    data.setTag_no(rsLocal.getString("TAG_NO"));
                    data.setP_rate(rsLocal.getDouble("P_RATE"));
                    data.setBranch_cd(rsLocal.getString("BRANCH_CD"));
                    detail.add(data);
                }
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("data", new Gson().toJson(detail));
                jResultObj.addProperty("Cause", "success");
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
