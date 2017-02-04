/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package journal;

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
import model.JournalVoucherModel;
import oldbUpdate.JournalVoucherUpdate;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateJournalVoucher extends HttpServlet {

    Library lb = Library.getInstance();
    DBHelper helper = DBHelper.GetDBHelper();

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
        TypeToken<List<JournalVoucherModel>> token = new TypeToken<List<JournalVoucherModel>>() {
        };
        List<JournalVoucherModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher((ArrayList<JournalVoucherModel>) detail));

    }

    private JsonObject saveVoucher(ArrayList<JournalVoucherModel> detail) {
        final JsonObject jResultObj = new JsonObject();
        Connection dataConnection = null;
        Library lb = Library.getInstance();
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                String sql = null;
                PreparedStatement psLocal = null;
                if (detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    sql = "INSERT INTO JVHD (VDATE, DR, CR, USER_ID, REMARK, branch_cd,REF_NO) "
                            + "VALUES (?, ?, ?, ?, '', ?, ?)";
//            ref_no = lb.generateCPNo(type);
                    detail.get(0).setRef_no(lb.generateKey(dataConnection, "JVHD", "REF_NO", "JV", 7));
                } else if (!detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    JournalVoucherUpdate cp = new JournalVoucherUpdate();
                    cp.deleteEntry(dataConnection, detail.get(0).getRef_no());
                    sql = "DELETE FROM JVDT WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "DELETE FROM payment WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                    psLocal = dataConnection.prepareStatement(sql);
                    psLocal.executeUpdate();

                    sql = "UPDATE JVHD SET VDATE=?, DR=?, CR=?, USER_ID=?, EDIT_NO=EDIT_NO+1, "
                            + "TIME_STAMP=CURRENT_TIMESTAMP, REMARK='',branch_cd=? WHERE REF_NO=?";
                }
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.setString(1, detail.get(0).getVdate());
                psLocal.setDouble(2, detail.get(0).getTot_dr());
                psLocal.setDouble(3, detail.get(0).getTot_cr());
                psLocal.setString(4, detail.get(0).getUser_id());
                psLocal.setString(5, detail.get(0).getBranch_cd());
                psLocal.setString(6, detail.get(0).getRef_no());
                psLocal.executeUpdate();

                sql = "Update JVHD set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + detail.get(0).getRef_no() + "'";
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.executeUpdate();

                sql = "INSERT INTO JVDT (SR_NO, AC_CD, AMT, DRCR, PART, REF_NO,IMEI) "
                        + "VALUES (?,?,?,?,?,?,?)";
                psLocal = dataConnection.prepareStatement(sql);
                for (int i = 0; i < detail.size(); i++) {

                    {
                        psLocal.setInt(1, i + 1);
                        psLocal.setString(2, detail.get(i).getAc_cd());
                        psLocal.setDouble(3, detail.get(i).getAmt());
                        psLocal.setInt(4, detail.get(i).getType());
                        psLocal.setString(5, detail.get(i).getPart());
                        psLocal.setString(6, detail.get(0).getRef_no());
                        psLocal.setString(7, detail.get(0).getImei());
                        psLocal.executeUpdate();

                    }
                }

                JournalVoucherUpdate cp = new JournalVoucherUpdate();
                cp.addEntry(dataConnection, detail.get(0).getRef_no());

                lb.closeStatement(psLocal);
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
