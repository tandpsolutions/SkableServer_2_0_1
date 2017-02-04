/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cashPR;

import model.CashPaymentReceiptModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.List;
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
public class AddUpdateCashVoucher extends HttpServlet {

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
        final String detailJson = request.getParameter("detail");
        TypeToken<List<CashPaymentReceiptModel>> token = new TypeToken<List<CashPaymentReceiptModel>>() {
        };
        List<CashPaymentReceiptModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher((ArrayList<CashPaymentReceiptModel>) detail));

    }

    private JsonObject saveVoucher(ArrayList<CashPaymentReceiptModel> detail) {
        final JsonObject jResultObj = new JsonObject();
        Connection dataConnection = null;
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                String sql = null;
                PreparedStatement psLocal = null;
                if (detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    sql = "INSERT INTO CPRHD (VDATE, TOT_BAL, AC_CD, USER_ID, CTYPE, branch_cd,REF_NO) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
//            ref_no = lb.generateCPNo(type);
                    if (detail.get(0).getType() == 0) {
                        detail.get(0).setRef_no(lb.generateKey(dataConnection, "CPRHD", "REF_NO", "CP", 7));
                    } else {
                        detail.get(0).setRef_no(lb.generateKey(dataConnection, "CPRHD", "REF_NO", "CR", 7));
                    }
                } else if (!detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    if (detail.get(0).getType() == 0) {
                        CashPaymentUpdate cp = new CashPaymentUpdate();
                        cp.deleteEntry(dataConnection, detail.get(0).getRef_no());
                    } else if (detail.get(0).getType() == 1) {
                        CashReciept cr = new CashReciept();
                        cr.deleteEntry(dataConnection, detail.get(0).getRef_no());
                    }
                    sql = "DELETE FROM CPRDT WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "DELETE FROM payment WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "UPDATE CPRHD SET VDATE=?, TOT_BAL=?, AC_CD=?, USER_ID=?, CTYPE=?, "
                            + "EDIT_NO=EDIT_NO+1, TIME_STAMP=CURRENT_TIMESTAMP,branch_cd=? WHERE REF_NO=?";
                }
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.setString(1, detail.get(0).getVdate());
                psLocal.setDouble(2, detail.get(0).getTot_amt());
                psLocal.setString(3, detail.get(0).getAc_cd());
                psLocal.setString(4, detail.get(0).getUser_id());
                psLocal.setInt(5, detail.get(0).getType());
                psLocal.setString(6, detail.get(0).getBranch_cd());
                psLocal.setString(7, detail.get(0).getRef_no());
                psLocal.executeUpdate();

                sql = "Update CPRHD set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + detail.get(0).getRef_no() + "'";
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.executeUpdate();

                sql = "INSERT INTO CPRDT (SR_NO, DOC_REF_NO, BAL, REMARK, REF_NO) "
                        + "VALUES (?, ?, ?, ?, ?)";
                psLocal = dataConnection.prepareStatement(sql);
                for (int i = 0; i < detail.size(); i++) {

                    {
                        psLocal.setInt(1, i + 1);
                        psLocal.setString(2, detail.get(i).getDoc_ref_no());
                        psLocal.setDouble(3, detail.get(i).getAmt());
                        psLocal.setString(4, detail.get(i).getRemark());
                        psLocal.setString(5, detail.get(0).getRef_no());
                        psLocal.executeUpdate();

                        sql = "INSERT INTO PAYMENT (CASH_AMT, BANK_CD, BANK_NAME, BANK_BRANCH, CHEQUE_NO, CHEQUE_DATE, BANK_AMT, CARD_NAME, CARD_AMT, REF_NO,USER_ID,vou_date)"
                                + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                        if (detail.get(i).getType() == 0) {
                            pstLocal.setDouble(1, detail.get(i).getAmt() * -1);
                        } else {
                            pstLocal.setDouble(1, detail.get(i).getAmt() * 1);
                        }
                        pstLocal.setString(2, "");
                        pstLocal.setString(3, "");
                        pstLocal.setString(4, "");
                        pstLocal.setString(5, "");
                        pstLocal.setString(6, null);
                        pstLocal.setDouble(7, 0.00);
                        pstLocal.setString(8, "");
                        pstLocal.setDouble(9, 0.00);
                        pstLocal.setString(10, detail.get(0).getRef_no());
                        pstLocal.setString(11, detail.get(i).getUser_id());
                        pstLocal.setString(12, detail.get(i).getVdate());
                        pstLocal.executeUpdate();

                    }
                }

                if (detail.get(0).getType() == 0) {
                    CashPaymentUpdate cp = new CashPaymentUpdate();
                    cp.addEntry(dataConnection, detail.get(0).getRef_no());
                } else if (detail.get(0).getType() == 1) {
                    CashReciept cr = new CashReciept();
                    cr.addEntry(dataConnection, detail.get(0).getRef_no());
                }

                dataConnection.commit();
                dataConnection.setAutoCommit(true);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
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
            } finally{
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
