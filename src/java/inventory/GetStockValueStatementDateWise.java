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
public class GetStockValueStatementDateWise extends HttpServlet {

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
        final Library lb = Library.getInstance();
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {

                String sql = "select type_name,SR_ALIAS,SR_NAME,o.SR_CD,sum(case when TRNS_ID='I' then (PCS) else (0) end) as issue,\n"
                        + " sum(case when TRNS_ID='I' then (PCS*RATE) else (0) end) as issue_val,\n"
                        + " sum(case when TRNS_ID='R' then (PCS) else (0) end) as receipt,\n"
                        + " sum(case when TRNS_ID='R' then (PCS*RATE) else (0) end) as receipt_val,\n"
                        + "(select sum(case when trns_id='R' then pcs when trns_id='I' then pcs*-1 else pcs end) from oldb0_2 sub_o \n"
                        + " where sub_o.SR_CD=o.sr_cd and sub_o.DOC_DATE<'" + from_date + "' ) as opb,\n"
                        + " (select sum(case when trns_id='R' then pcs*rate when trns_id='I' then pcs*rate*-1 else pcs*rate end)\n"
                        + " from oldb0_2 sub_o where sub_o.SR_CD=o.sr_cd and sub_o.DOC_DATE<'" + from_date + "' ) as opb_val "
                        + " from OLDB0_2 o  \n"
                        + " left join SERIESMST s on o.SR_CD=s.SR_CD left join MODELMST m on s.MODEL_CD=m.MODEL_CD"
                        + " left join typemst t on m.type_cd=t.type_cd \n"
                            + " where o.DOC_DATE>='" + from_date + "' and o.DOC_DATE<='" + to_date + "' and doc_cd <>'STF' ";

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd = '" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                sql += " group by SR_ALIAS,SR_NAME,o.SR_CD ";
                sql += " order by s.sr_name,type_name";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("TYPE_NAME", viewDataRs.getString("TYPE_NAME"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    if (viewDataRs.getString("opb") == null) {
                        object.addProperty("OPB", 0);
                        object.addProperty("OPB_VAL", 0.00);
                    } else {
                        object.addProperty("OPB", viewDataRs.getString("opb"));
                        object.addProperty("OPB_VAL", viewDataRs.getString("opb_val"));
                    }
                    object.addProperty("PURCHASE", viewDataRs.getString("receipt"));
                    object.addProperty("PURCHASE_VAL", viewDataRs.getString("receipt_val"));
                    object.addProperty("SALES", viewDataRs.getString("issue"));
                    object.addProperty("SALES_VAL", viewDataRs.getString("issue_val"));
                    object.addProperty("SR_CD", viewDataRs.getString("SR_CD"));
                    array.add(object);
                }

                sql = "select type_name,SR_ALIAS,SR_NAME,o.SR_CD,sum(case when TRNS_ID='I' then (PCS) else (0) end) as issue,\n"
                        + "sum(case when TRNS_ID='I' then (PCS*RATE) else (0) end) as issue_val,\n"
                        + "sum(case when TRNS_ID='R' then (PCS) else (0) end) as receipt,\n"
                        + "sum(case when TRNS_ID='R' then (PCS*RATE) else (0) end) as receipt_val,"
                        + "sum(case when TRNS_ID='O' then (PCS) else (0) end) as opb,\n"
                        + "sum(case when TRNS_ID='O' then (PCS*RATE) else (0) end) as opb_val from OLDB0_2 o  \n"
                        + "left join SERIESMST s on o.SR_CD=s.SR_CD left join MODELMST m on s.MODEL_CD=m.MODEL_CD"
                        + " left join typemst t on m.type_cd=t.type_cd \n"
                        + " where o.DOC_DATE<'" + from_date + "'"
                        + " and s.sr_cd not in(select distinct(sr_cd) from oldb0_2 where doc_date >='" + from_date + "' "
                        + " and doc_date<='" + to_date + "') and doc_cd <>'STF' ";

                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd = '" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                sql += " group by SR_ALIAS,SR_NAME,o.SR_CD ";
                sql += " order by s.sr_name,type_name";
                pstLocal = dataConnection.prepareStatement(sql);
                viewDataRs = pstLocal.executeQuery();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("TYPE_NAME", viewDataRs.getString("TYPE_NAME"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("SR_ALIAS", viewDataRs.getString("SR_ALIAS"));
                    if (viewDataRs.getString("opb") == null) {
                        object.addProperty("OPB", 0 + viewDataRs.getInt("receipt") - viewDataRs.getInt("issue"));
                        object.addProperty("OPB_VAL", 0.00 + viewDataRs.getDouble("receipt_val") - viewDataRs.getDouble("issue_val"));
                    } else {
                        object.addProperty("OPB", viewDataRs.getInt("opb") + viewDataRs.getInt("receipt") - viewDataRs.getInt("issue"));
                        object.addProperty("OPB_VAL", viewDataRs.getDouble("opb_val") + viewDataRs.getDouble("receipt_val") - viewDataRs.getDouble("issue_val"));
                    }
                    object.addProperty("PURCHASE", 0);
                    object.addProperty("PURCHASE_VAL", 0.00);
                    object.addProperty("SALES", 0);
                    object.addProperty("SALES_VAL", 0.00);
                    object.addProperty("SR_CD", viewDataRs.getString("SR_CD"));
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
