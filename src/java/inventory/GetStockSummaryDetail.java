/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.rowset.CachedRowSetImpl;
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
import javax.sql.rowset.CachedRowSet;
import support.DBHelper;
import support.Library;
import support.SysEnv;

/**
 *
 * @author bhaumik
 */
public class GetStockSummaryDetail extends HttpServlet {

    Library lb = Library.getInstance();
    ResultSet viewDataRS = null;
    private CachedRowSet crsMain = null;
    SysEnv clSysEnv = lb.companySetUp();

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
        final String sr_cd = request.getParameter("sr_cd");
        final JsonObject jResultObj = new JsonObject();
        if (dataConnection != null) {
            try {

                makeQuery(sr_cd, dataConnection);

                JsonArray array = new JsonArray();
                crsMain.beforeFirst();
                while (crsMain.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("Month", crsMain.getString("Month"));
                    object.addProperty("opening", crsMain.getString("opening"));
                    object.addProperty("purchase", crsMain.getString("purchase"));
                    object.addProperty("sales", crsMain.getString("sales"));
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
            } finally {
                lb.closeConnection(dataConnection);
            }
        }
        response.getWriter().print(jResultObj);
    }

    private void makeQuery(String sr_cd, Connection dataConnection) {
        try {
            String sql = "select o.* from OLDB0_1 o left join SERIESMST i on o.sr_cd=i.sr_cd "
                    + " where o.sr_cd='" + sr_cd + "'";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            viewDataRS = pstLocal.executeQuery();
            crsMain = getABlankCachedRowSetOfSevenColumns(dataConnection);
            if (viewDataRS.next()) {
                for (int i = 4; i <= 12 || i <= 3; i++) {
                    crsMain.moveToInsertRow();
                    String date = "01/";
                    if (i > 9) {
                        date += i;
                    } else {
                        date += "0" + i;
                    }
                    if (i <= 12 && i >= 4) {
                        date += "/" + clSysEnv.getAC_YEAR();
                    } else {
                        date += "/" + (int) (lb.isNumber2(clSysEnv.getAC_YEAR()) + 1);
                    }
                    double prevBal = lb.getOpeningStock(dataConnection, sr_cd, "PCS", date);
                    if (i == 4) {
                        prevBal += lb.isNumber2(lb.getData(dataConnection, "OPB_QTY", "SERIESMST", "sr_cd", viewDataRS.getString("sr_cd"), 0));
                    }
                    crsMain.updateDouble(1, prevBal);
                    crsMain.updateDouble(2, viewDataRS.getDouble("PPUR_" + i));
                    crsMain.updateDouble(3, viewDataRS.getDouble("PSAL_" + i));
                    crsMain.updateString(4, lb.getMonth(i + "", "N"));
                    crsMain.insertRow();
                    crsMain.moveToCurrentRow();
                    crsMain.last();

                    if (i == 12) {
                        i = 0;
                    } else if (i == 3) {
                        break;
                    }
                }
            }
            crsMain.beforeFirst();
        } catch (Exception ex) {
        }
    }

    private CachedRowSet getABlankCachedRowSetOfSevenColumns(Connection dataConnection) {

        CachedRowSet crsBlank = null;

        try {

            PreparedStatement pstBlank = dataConnection.prepareStatement("Select 0.00 as opening, 0.00 as purchase, 0.00 as sales, "
                    + " '1' as month from dual", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ResultSet rsBlank = pstBlank.executeQuery();

            crsBlank = new CachedRowSetImpl();
            crsBlank.populate(rsBlank);

            rsBlank.close();
            pstBlank.close();

            crsBlank.beforeFirst();
            crsBlank.beforeFirst();

            while (crsBlank.next()) {
                crsBlank.deleteRow();
            }

        } catch (Exception e) {
        }

        return crsBlank;

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
