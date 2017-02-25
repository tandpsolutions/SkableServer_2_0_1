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
public class CreditNoteReport extends HttpServlet {

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
        final String ac_cd = request.getParameter("ac_cd");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final String model_cd = (request.getParameter("model_cd"));
        final String brand_cd = (request.getParameter("brand_cd"));
        final String memory_cd = (request.getParameter("memory_cd"));
        final String type_cd = (request.getParameter("type_cd"));
        final boolean sales = Boolean.parseBoolean((request.getParameter("sales")));
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select '' as fname,s.sr_name,t.pur_date as v_date,t1.TYPE_NAME,(t.tag_no) as pcs,(t.PUR_RATE) as tot_sales,t.ref_no as pur_tag_no"
                        + ",sales.V_DATE as sales_date,0 as disc_per,0 as extra_support,0 as backend,0 as activation,0 as prize_drop"
                        + ",t.branch_cd from tag t "
                        + "  "
                        + " left join SERIESMST s on t.SR_CD=s.SR_CD left join MODELMST m on s.MODEL_CD=m.MODEL_CD left join TYPEMST t1 on m.TYPE_CD=t1.TYPE_CD "
                        + " left join VILSHD sales on t.SALE_REF_NO=sales.REF_NO where t.PUR_DATE>='" + from_date + "' "
                        + " and t.PUR_DATE<='" + to_date + "' ";
                if (sales) {

                } else {
                    sql += " and (sales.V_DATE>'" + to_date + "' or t.SALE_REF_NO='')";
                }
                if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd='" + brand_cd + "'";
                }
                if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and m.model_cd='" + model_cd + "'";
                }
                if (!memory_cd.equalsIgnoreCase("")) {
                    sql += " and s.MEMORY_CD='" + memory_cd + "'";
                }
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "'";
                }

                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and sales.ac_cd='" + ac_cd + "'";
                }
                sql += " and t.is_del <> -1 order by t.PUR_DATE";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("TYPE_NAME", viewDataRs.getString("TYPE_NAME"));
                    object.addProperty("sr_name", viewDataRs.getString("sr_name"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("type_name", viewDataRs.getString("type_name"));
                    object.addProperty("pcs", viewDataRs.getString("pcs"));
                    object.addProperty("tot_sales", viewDataRs.getDouble("tot_sales"));
                    object.addProperty("PUR_TAG_NO", viewDataRs.getString("PUR_TAG_NO"));
                    object.addProperty("sales_date", viewDataRs.getString("sales_date"));
                    object.addProperty("disc_per", viewDataRs.getString("disc_per"));
                    object.addProperty("extra_support", viewDataRs.getString("extra_support"));
                    object.addProperty("backend", viewDataRs.getString("backend"));
                    object.addProperty("activation", viewDataRs.getString("activation"));
                    object.addProperty("prize_drop", viewDataRs.getString("prize_drop"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
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
