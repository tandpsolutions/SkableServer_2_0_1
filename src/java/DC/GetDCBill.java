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
import support.Library;

/**
 *
 * @author bhaumik
 */
public class GetDCBill extends HttpServlet {

    DBHelper helper = DBHelper.GetDBHelper();
    Library lb = Library.getInstance();

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
        final String ref_no = request.getParameter("ref_no");
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }

        if (dataConnection != null) {
            try {
                String sql = "select v1.remark,v.REF_NO,v.INV_NO,v.V_DATE,v.V_TYPE,a.FNAME,a.AC_CD,p.MOBILE1,v.DET_TOT,v1.TAG_NO,v1.sr_cd,s.SR_NAME,v1.IMEI_NO"
                        + ",v1.SERAIL_NO,v1.RATE,v1.AMT,v1.PUR_TAG_NO,v1.QTY from DCHD v left join DCDT v1 on v.REF_NO = v1.REF_NO\n"
                        + " left join ACNTMST a on v.AC_CD=a.AC_CD left join PHBKMST p on a.AC_CD=p.AC_CD \n"
                        + " left join SERIESMST s on s.SR_CD=v1.SR_CD where v.ref_no=?";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, ref_no);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
                    object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
                    object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
                    object.addProperty("V_TYPE", rsLocal.getInt("V_TYPE"));
                    object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
                    object.addProperty("FNAME", rsLocal.getString("FNAME"));
                    object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
                    object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
                    object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
                    object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
                    object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
                    object.addProperty("QTY", rsLocal.getString("QTY"));
                    object.addProperty("RATE", rsLocal.getDouble("RATE"));
                    object.addProperty("AMT", rsLocal.getDouble("AMT"));
                    object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
                    object.addProperty("REMARK", rsLocal.getString("REMARK"));
                    array.add(object);
                }
//                response.getWriter().print(array.toString());
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
