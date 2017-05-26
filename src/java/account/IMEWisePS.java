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
import support.Library;

/**
 *
 * @author bhaumik
 */
public class IMEWisePS extends HttpServlet {

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
        final String model_cd = request.getParameter("model_cd");
        final String brand_cd = request.getParameter("brand_cd");
        final String sr_cd = request.getParameter("sr_cd");
        final String ac_cd = request.getParameter("ac_cd");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String bill_no = request.getParameter("bill_no");
        final String branch_cd = request.getParameter("branch_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select t.branch_cd,a.FNAME,a1.FNAME as SALE_PARTY,s.SR_ALIAS,l.V_DATE,TAG_NO,SR_NAME,PUR_RATE,SALE_RATE,"
                        + " b.BRAND_NAME,t.MRP from tag t left join  SERIESMST s on t.sr_cd=s.sr_cd"
                        + " left join MODELMST m on s.model_cd=m.model_cd left join brandmst b on m.brand_cd=b.brand_cd "
                        + " left join LBRPHD l on l.REF_NO=t.PUR_REF_NO left join ACNTMST a on l.AC_CD=a.AC_CD "
                        + " left join vilshd v on v.REF_NO=t.SALE_REF_NO left join ACNTMST a1 on v.AC_CD=a1.AC_CD"
                        + " where t.TAG_NO <> ''";
                if (bill_no.equalsIgnoreCase("")) {
                    sql += " and l.v_date>='" + from_date + "' and "
                            + " l.v_date<='" + to_date + "'";
                } else {
                    sql += " and l.inv_no=" + bill_no;
                }
                if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and m.model_cd ='" + model_cd + "'";
                }
                if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd ='" + brand_cd + "'";
                }

                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and l.ac_cd ='" + ac_cd + "'";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.sr_cd ='" + sr_cd + "'";
                }
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd ='" + type_cd + "'";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd ='" + sub_type_cd + "'";
                }
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and t.branch_cd =" + branch_cd;
                }
                sql += " order by l.v_date,TAG_NO,s.sr_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (rsLocal.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("FNAME", rsLocal.getString("FNAME"));
                    object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
                    object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
                    object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
                    object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
                    object.addProperty("PUR_RATE", rsLocal.getDouble("PUR_RATE"));
                    object.addProperty("SALE_RATE", rsLocal.getDouble("SALE_RATE"));
                    object.addProperty("SALE_PARTY", rsLocal.getString("SALE_PARTY"));
                    object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
                    object.addProperty("MRP", rsLocal.getString("MRP"));
                    object.addProperty("BRANCH_CD", rsLocal.getString("BRANCH_CD"));
                    array.add(object);
                }
                lb.closeResultSet(rsLocal);
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
