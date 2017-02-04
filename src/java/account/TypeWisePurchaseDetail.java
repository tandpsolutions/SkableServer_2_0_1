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
public class TypeWisePurchaseDetail extends HttpServlet {

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
        final String sr_cd = request.getParameter("sr_cd");
        final String type_cd = request.getParameter("type_cd");
        final String sub_type_cd = request.getParameter("sub_type_cd");
        final String brand_cd = request.getParameter("brnad_cd");
        final String model_cd = request.getParameter("model_cd");
        final String branch_cd = request.getParameter("branch_cd");
        final String day = request.getParameter("day");
        final String scheme_cd = request.getParameter("scheme_cd");
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                PreparedStatement pstLocal = null;
                String sql = "select v.branch_cd,v.ref_no,a.fname,s.sr_name,v.v_date,t.TYPE_NAME,v1.tag_no,(v1.qty) as pcs,(v1.RATE) as tot_sales,"
                        + " m.model_name,b.brand_name,v.remark,v1.mrp from LBRPHD v left join LBRPdt v1 on v.REF_NO=v1.REF_NO "
                        + " left join SERIESMST s on v1.SR_CD=s.SR_CD left join acntmst a on v.ac_cd=a.ac_cd "
                        + " left join MODELMST m on s.MODEL_CD=m.MODEL_CD left join TYPEMST t on m.TYPE_CD=t.TYPE_CD"
                        + " left join BRANDMST b on m.brand_cd=b.brand_cd where v.IS_DEL=0 "
                        + " and v.v_date>='" + from_date + "' "
                        + " and v.v_date<='" + to_date + "'";
                if (!type_cd.equalsIgnoreCase("")) {
                    sql += " and m.type_cd='" + type_cd + "' ";
                }
                if (!sub_type_cd.equalsIgnoreCase("")) {
                    sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                }
                if (!sr_cd.equalsIgnoreCase("")) {
                    sql += " and s.SR_CD='" + sr_cd + "'";
                } else if (!brand_cd.equalsIgnoreCase("")) {
                    sql += " and m.brand_cd='" + brand_cd + "'";
                } else if (!model_cd.equalsIgnoreCase("")) {
                    sql += " and s.model_cd='" + model_cd + "'";
                }
                if (!ac_cd.equalsIgnoreCase("")) {
                    sql += " and v.ac_cd='" + ac_cd + "'";
                }
                if (!branch_cd.equalsIgnoreCase("0")) {
                    sql += " and v.branch_cd=" + branch_cd;
                }
                if (day != null && !day.equalsIgnoreCase("")) {
                    sql += " and due_date <= v_Date and v_date > date_sub(DUE_DATE, interval " + day + " day) ";
                }

                if (!scheme_cd.equalsIgnoreCase("0")) {
                    sql += " and v.scheme_cd='" + scheme_cd + "'";
                }

                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("sr_name", viewDataRs.getString("sr_name"));
                    object.addProperty("v_date", viewDataRs.getString("v_date"));
                    object.addProperty("type_name", viewDataRs.getString("type_name"));
                    object.addProperty("pcs", viewDataRs.getInt("pcs"));
                    object.addProperty("tot_sales", viewDataRs.getDouble("tot_sales"));
                    object.addProperty("IMEI_NO", viewDataRs.getString("TAG_NO"));
                    object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    object.addProperty("model_name", viewDataRs.getString("model_name"));
                    object.addProperty("brand_name", viewDataRs.getString("brand_name"));
                    object.addProperty("remark", viewDataRs.getString("remark"));
                    object.addProperty("mrp", viewDataRs.getString("mrp"));
                    array.add(object);
                }

                if (scheme_cd.equalsIgnoreCase("0")) {
                    sql = "select  v.branch_cd,v.ref_no,a.fname,s.sr_name,v.v_date,t.TYPE_NAME,case when IMEI_NO ='' then SERAIL_NO else IMEI_NO end as IMEI_NO"
                            + ",(v1.qty) as pcs,(v1.RATE) as tot_sales,m.model_name,b.brand_name,v.remark from prhd v left join prdt v1 on"
                            + " v.REF_NO=v1.REF_NO "
                            + " left join SERIESMST s on v1.SR_CD=s.SR_CD left join acntmst a on v.ac_cd=a.ac_cd "
                            + " left join MODELMST m on s.MODEL_CD=m.MODEL_CD left join TYPEMST t on m.TYPE_CD=t.TYPE_CD"
                            + " left join BRANDMST b on m.brand_cd=b.brand_cd where v.IS_DEL=0 "
                            + " and v.v_date>='" + from_date + "' "
                            + " and v.v_date<='" + to_date + "'";
                    if (!type_cd.equalsIgnoreCase("")) {
                        sql += " and m.type_cd='" + type_cd + "' ";
                    }
                    if (!sub_type_cd.equalsIgnoreCase("")) {
                        sql += " and m.sub_type_cd='" + sub_type_cd + "' ";
                    }
                    if (!sr_cd.equalsIgnoreCase("")) {
                        sql += " and s.SR_CD='" + sr_cd + "'";
                    } else if (!brand_cd.equalsIgnoreCase("")) {
                        sql += " and m.brand_cd='" + brand_cd + "'";
                    } else if (!model_cd.equalsIgnoreCase("")) {
                        sql += " and s.model_cd='" + model_cd + "'";
                    }
                    if (!ac_cd.equalsIgnoreCase("")) {
                        sql += " and v.ac_cd='" + ac_cd + "'";
                    }
                    if (!branch_cd.equalsIgnoreCase("0")) {
                        sql += " and v.branch_cd=" + branch_cd;
                    }

                    pstLocal = dataConnection.prepareStatement(sql);
                    viewDataRs = pstLocal.executeQuery();
                    while (viewDataRs.next()) {
                        JsonObject object = new JsonObject();
                        object.addProperty("fname", viewDataRs.getString("fname"));
                        object.addProperty("sr_name", viewDataRs.getString("sr_name"));
                        object.addProperty("v_date", viewDataRs.getString("v_date"));
                        object.addProperty("type_name", viewDataRs.getString("type_name"));
                        object.addProperty("pcs", viewDataRs.getInt("pcs") * -1);
                        object.addProperty("tot_sales", viewDataRs.getDouble("tot_sales") * -1);
                        object.addProperty("IMEI_NO", viewDataRs.getString("IMEI_NO"));
                        object.addProperty("REF_NO", viewDataRs.getString("REF_NO"));
                        object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                        object.addProperty("model_name", viewDataRs.getString("model_name"));
                        object.addProperty("brand_name", viewDataRs.getString("brand_name"));
                        object.addProperty("remark", viewDataRs.getString("remark"));
                        object.addProperty("mrp", "0.00");
                        array.add(object);
                    }
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
