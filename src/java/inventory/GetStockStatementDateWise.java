/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

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
public class GetStockStatementDateWise extends HttpServlet {

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
        final String sr_cd = request.getParameter("sr_cd");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        final String from_date = request.getParameter("from_date");
        final String to_date = request.getParameter("to_date");
        final int including = Integer.parseInt(request.getParameter("including"));
        Library lb = Library.getInstance();
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {

                String sql = "select SR_ALIAS,SR_NAME,o.SR_CD,sum(case when TRNS_ID='I' then (PCS) else (0) end) as issue,"
                        + "sum(case when TRNS_ID='R' then (PCS) when TRNS_ID='O' then PCS else (0) end) as receipt from OLDB0_2 o "
                        + " left join SERIESMST s on o.SR_CD=s.SR_CD left join MODELMST m on s.MODEL_CD=m.MODEL_CD"
                        + " where o.DOC_DATE>='" + from_date + "' and o.DOC_DATE<='" + to_date + "'";

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    if (including == 0) {
                        sql += " and m.brand_cd in (" + brand_cd + ")";
                    } else {
                        sql += " and m.brand_cd not in (" + brand_cd + ")";
                    }
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                sql += " group by SR_ALIAS,SR_NAME,o.SR_CD ";
                sql += " order by s.sr_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    object.addProperty("OPB", lb.getBalanceStockByDate(dataConnection, viewDataRs.getString("sr_cd"), from_date, 1));
                    object.addProperty("PURCHASE", viewDataRs.getString("receipt"));
                    object.addProperty("SALES", viewDataRs.getString("issue"));
                    object.addProperty("SR_CD", viewDataRs.getString("SR_CD"));
                    array.add(object);
                }

                sql = "select SR_ALIAS,SR_NAME,o.SR_CD,sum(case when TRNS_ID='I' then (PCS*-1) else (PCS) end) as opb from OLDB0_2 o "
                        + " left join SERIESMST s on o.SR_CD=s.SR_CD left join MODELMST m on s.MODEL_CD=m.MODEL_CD"
                        + " where  o.DOC_DATE<'" + from_date + "' and s.sr_cd not in (select o1.sr_cd from oldb0_2 o1 left join seriesmst s1"
                        + " on o1.sr_cd=s1.sr_cd left join modelmst m1 on s1.model_cd=m1.model_cd "
                        + " where o1.DOC_DATE>='" + from_date + "' and o1.DOC_DATE<='" + to_date + "' ";

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m1.type_cd='" + type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s1.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m1.brand_cd in (" + brand_cd + ")";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s1.model_cd='" + model_cd + "'";
                }
                sql += ") ";
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd in (" + brand_cd + ")";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                sql += " group by SR_ALIAS,SR_NAME,o.SR_CD ";
                sql += " order by s.sr_name";
                pstLocal = dataConnection.prepareStatement(sql);
                viewDataRs = pstLocal.executeQuery();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    object.addProperty("OPB", viewDataRs.getDouble("OPB"));
                    object.addProperty("PURCHASE", 0.00);
                    object.addProperty("SALES", 0.00);
                    object.addProperty("SR_CD", viewDataRs.getString("SR_CD"));
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
