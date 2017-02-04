/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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

/**
 *
 * @author bhaumik
 */
public class GetGeneralLedgerSummary extends HttpServlet {

    private CachedRowSet crsMain = null;
    String ac_cd = "";
    Connection dataConnection = null;
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
        DBHelper helper = DBHelper.GetDBHelper();
        dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        ac_cd = request.getParameter("ac_cd");
        try {
            if (dataConnection != null) {
                makeQuery();
                JsonArray array = new JsonArray();
                while (crsMain.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("MONTH", crsMain.getString("MONTH"));
                    object.addProperty("DR", crsMain.getDouble("DR"));
                    object.addProperty("CR", crsMain.getDouble("CR"));
                    object.addProperty("AMT", crsMain.getDouble("AMT"));
                    array.add(object);
                }
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
        response.getWriter().print(jResultObj);
    }

    private void makeQuery() throws SQLException {
        crsMain = getABlankCachedRowSetOfSevenColumns();
        for (int i = 4; i <= 12 || i <= 3; i++) {
            crsMain.moveToInsertRow();
            double prevBal = getPrewviousBal(i - 1, ac_cd);
            for (int j = 1; j <= 2; j++) {
                String sql = "select sum(val) "
                        + " from oldb2_2 "
                        + "where crdr=" + (j - 1) + " and "
                        + " month(doc_date) = " + (i) + "  and ac_cd='" + ac_cd + "'";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRS = pstLocal.executeQuery();
                if (viewDataRS.next()) {
                    crsMain.updateDouble(j, lb.isNumber2(viewDataRS.getString(1)));
                }
            }
            if (prevBal < 0) {
                if (crsMain.getDouble(1) != 0.0) {
                    crsMain.updateDouble(1, crsMain.getDouble(1) - prevBal);
                }
            } else {
                if (crsMain.getDouble(2) != 0) {
                    crsMain.updateDouble(2, crsMain.getDouble(2) + prevBal);
                }
            }
            crsMain.updateString(3, (Double.parseDouble(lb.getField("select sum(val) from oldb2_2 where doc_cd='OPB' and ac_cd='" + ac_cd + "'", dataConnection)) + crsMain.getDouble(1) - crsMain.getDouble(2)) + "");
            crsMain.updateString(4, lb.getMonth(i + "", "n"));
            crsMain.insertRow();
            crsMain.moveToCurrentRow();
            crsMain.last();
            if (i == 12) {
                i = 0;
            } else if (i == 3) {
                break;
            }
        }
        crsMain.beforeFirst();
    }

    private CachedRowSet getABlankCachedRowSetOfSevenColumns() {

        CachedRowSet crsBlank = null;

        try {

            PreparedStatement pstBlank = dataConnection.prepareStatement("Select '0' as CR, "
                    + "'0' as DR, '0' as AMT,'1' as month from dual", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

    private double getPrewviousBal(int month, String ac_cd) {
        String sql = "select ac_cd,doc_ref_no,doc_cd,doc_date,val,crdr"
                + " from oldb2_2 where month(doc_date) = " + month + " AND AC_CD=?";

        double opbRs = 0.00;
        PreparedStatement pstlocal = null;
        ResultSet rsLocal = null;
        try {
            pstlocal = dataConnection.prepareStatement(sql);
            pstlocal.setString(1, ac_cd);
            rsLocal = pstlocal.executeQuery();
            while (rsLocal.next()) {

                if (rsLocal.getString("crdr").equalsIgnoreCase("0")) {
                    opbRs += rsLocal.getDouble("val");
                } else {
                    opbRs -= rsLocal.getDouble("val");
                }
            }

        } catch (SQLException ex) {
        } finally {
            lb.closeResultSet(rsLocal);
            lb.closeStatement(pstlocal);
        }
        return opbRs;
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
