/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cashPR;

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
import oldbUpdate.CashPaymentUpdate;
import oldbUpdate.CashReciept;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class DeleteCashBill extends HttpServlet {

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
        final String ref_no = request.getParameter("ref_no");
        final int type = Integer.parseInt(request.getParameter("type"));
        response.getWriter().print(saveVoucher(ref_no, type));
    }

    private JsonObject saveVoucher(String ref_no, int type) {
        final JsonObject jResultObj = new JsonObject();
        Connection dataConnection = null;
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        if (dataConnection != null) {
            try {
                String sql = "SELECT doc_ref_no FROM billadjst WHERE dr_doc_ref_no='" + ref_no + "' OR cr_doc_ref_no='" + ref_no + "'";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                ResultSet rsLocal = pstLocal.executeQuery();
                if (rsLocal.next()) {
                    jResultObj.addProperty("result", -1);
                    jResultObj.addProperty("Cause", "Voucher is in adjustment process");
                } else {
                    dataConnection.setAutoCommit(false);
                    sql = null;
                    PreparedStatement psLocal = null;
                    if (type == 0) {
                        CashPaymentUpdate cp = new CashPaymentUpdate();
                        cp.deleteEntry(dataConnection, ref_no);
                    } else if (type == 1) {
                        CashReciept cr = new CashReciept();
                        cr.deleteEntry(dataConnection, ref_no);
                    }
                    sql = "DELETE FROM CPRDT WHERE REF_NO='" + ref_no + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "DELETE FROM payment WHERE REF_NO='" + ref_no + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "delete from  CPRHD WHERE REF_NO=?";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.setString(1, ref_no);
                    psLocal.executeUpdate();

                    dataConnection.commit();
                    dataConnection.setAutoCommit(true);
                    jResultObj.addProperty("result", 1);
                    jResultObj.addProperty("Cause", "success");
                }
            } catch (SQLNonTransientConnectionException ex1) {
                ex1.printStackTrace();
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                ex.printStackTrace();
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
                try {
                    dataConnection.rollback();
                    dataConnection.setAutoCommit(true);
                } catch (Exception e) {
                }
            } finally {
                lb.closeConnection(dataConnection);
            }
        }
        return jResultObj;
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
