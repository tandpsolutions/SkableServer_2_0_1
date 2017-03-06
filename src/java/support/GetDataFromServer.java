/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author bhaumik
 */
public class GetDataFromServer extends HttpServlet {

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
        final String param_code = request.getParameter("param_code");
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        int switch_code = Integer.parseInt(param_code);
        if (dataConnection != null) {
            try {
                JsonArray array;
                switch (switch_code) {
                    case 1:
                        String mobile = request.getParameter("value");
                        array = lb.getAccountMaster(dataConnection, "MOBILE1", mobile);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("accountMaster", array);
                        break;
                    case 2:
                        String fname = request.getParameter("value");
                        array = lb.getAccountMaster(dataConnection, "fname", fname);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("accountMaster", array);
                        break;
                    case 3:
                        String sr_name = request.getParameter("value");
                        array = lb.getSeriesMaster(dataConnection, "sr_name", sr_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("seriesmaster", array);
                        break;
                    case 4:
                        String pur_ref_no = request.getParameter("ref_no");
                        array = lb.getPurchaseBill(dataConnection, pur_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 5:
                        if (request.getParameter("ref_no").startsWith("05")) {
                            array = lb.getTagNo(dataConnection, request.getParameter("ref_no"));
                        } else {
                            array = lb.getTagNoDCI(dataConnection, request.getParameter("ref_no"));
                        }
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 6:
                        array = lb.getTagNoDetail(dataConnection, request.getParameter("tag_list"), Boolean.parseBoolean(request.getParameter("only_stock")));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 7:
                        array = lb.getTaxMaster(dataConnection);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 8:
                        String brand_name = request.getParameter("value");
                        array = lb.getBrandmaster(dataConnection, "brand_name", brand_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 9:
                        String cash_ref_no = request.getParameter("value");
                        array = lb.getCashDetail(dataConnection, "ref_no", cash_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;

                    case 10:
                        String bank_ref_no = request.getParameter("value");
                        array = lb.getBankDetail(dataConnection, "ref_no", bank_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 11:
                        String grp_name = request.getParameter("value");
                        array = lb.getGroupMaster(dataConnection, "GROUP_NAME", grp_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 12:
                        String model_name = request.getParameter("value");
                        array = lb.getModelMaster(dataConnection, "model_name", model_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 13:
                        String memory_name = request.getParameter("value");
                        array = lb.getMemoryMaster(dataConnection, "memory_name", memory_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 14:
                        String color_name = request.getParameter("value");
                        array = lb.getColorMaster(dataConnection, "COLOUR_NAME", color_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 15:
                        array = lb.getTagNoDetailSales(dataConnection, request.getParameter("tag_list"), Boolean.parseBoolean(request.getParameter("only_stock")));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 16:
                        String pur_ret_ref_no = request.getParameter("VALUE");
                        array = lb.getPurchaseReturnBill(dataConnection, pur_ret_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 17:
                        String jor_ref_no = request.getParameter("value");
                        array = lb.getJournalDetail(dataConnection, "ref_no", jor_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 18:
                        String contra_ref_no = request.getParameter("value");
                        array = lb.getContraDetail(dataConnection, "ref_no", contra_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;

                    case 19:
                        String dc_ref_no = request.getParameter("value");
                        array = lb.getDCDetail(dataConnection, dc_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 20:
                        array = lb.getTagNoDetailSales(dataConnection, request.getParameter("tag_list"), Boolean.parseBoolean(request.getParameter("only_stock")), request.getParameter("loc"), request.getParameter("godown"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        jResultObj.add("old_data", lb.getOldStock(dataConnection, request.getParameter("tag_list"), ((Boolean.parseBoolean(request.getParameter("only_stock"))) ? "1" : "0"), request.getParameter("loc")));

                        break;
                    case 21:
                        array = lb.getLastRate(dataConnection, request.getParameter("ac_cd"), request.getParameter("sr_cd"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 22:
                        array = lb.getAccountmaster(dataConnection, request.getParameter("CARD_NO"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 23:
                        array = lb.getAccountmasterMobile(dataConnection, request.getParameter("VALUE"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 24:
                        String sale_ref_no = request.getParameter("VALUE");
                        array = lb.getSalesBill(dataConnection, sale_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;

                    case 25:
                        String sale_ret_ref_no = request.getParameter("VALUE");
                        array = lb.getSalesReturnBill(dataConnection, sale_ret_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 26:
                        String ac_cd = request.getParameter("value");
                        array = lb.getoldb2_4(dataConnection, ac_cd);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 27:
                        ac_cd = request.getParameter("value");
                        array = lb.getoldb2_3(dataConnection, ac_cd);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 28:
                        cash_ref_no = request.getParameter("value");
                        array = lb.getCashDetailRecpt(dataConnection, "ref_no", cash_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;

                    case 29:
                        bank_ref_no = request.getParameter("value");
                        array = lb.getBankDetailRcpt(dataConnection, "ref_no", bank_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 30:
                        bank_ref_no = request.getParameter("value");
                        array = lb.getDNDetail(dataConnection, "ref_no", bank_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 31:
                        bank_ref_no = request.getParameter("value");
                        array = lb.getCNDetail(dataConnection, "ref_no", bank_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 32:
                        String sale_ref_no_lg = request.getParameter("VALUE");
                        array = lb.getSalesBillOLD(dataConnection, sale_ref_no_lg);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 33:
                        String order_ref_no = request.getParameter("value");
                        array = lb.getOrderDetail(dataConnection, "ref_no", order_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 34:
                        String visitor_ref_no = request.getParameter("value");
                        array = lb.getVisitorDetail(dataConnection, "ref_no", visitor_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 35:
                        String tag_no = request.getParameter("value");
                        array = lb.GetTags(dataConnection, tag_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("tags", array);
                        break;
                    case 36:
                        array = lb.getLastRateSales(dataConnection, request.getParameter("ac_cd"), request.getParameter("sr_cd"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 37:
                        String tid_name = request.getParameter("value");
                        array = lb.getTidMaster(dataConnection, "tid_name", tid_name);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 38:
                        array = lb.getLastRateMRP(dataConnection, request.getParameter("sr_cd"));
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                        break;
                    case 39:
                        String qt_ref_no = request.getParameter("ref_no");
                        array = lb.getQuoteBill(dataConnection, qt_ref_no);
                        jResultObj.addProperty("result", 1);
                        jResultObj.addProperty("Cause", "success");
                        jResultObj.add("data", array);
                }
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
