/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockTransfer;

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
import model.StockTransferDetail;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateStkTrDetailOutside extends HttpServlet {

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
        TypeToken<List<StockTransferDetail>> token = new TypeToken<List<StockTransferDetail>>() {
        };
        List<StockTransferDetail> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher((ArrayList<StockTransferDetail>) detail));

    }

    private JsonObject saveVoucher(final ArrayList<StockTransferDetail> detail) {
        final JsonObject jResultObj = new JsonObject();
        final Connection dataConnection = helper.getConnMpAdmin();

        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                String sql = "";
                sql = "INSERT INTO stktrfouthd (inv_no,V_DATE,from_loc,to_loc,USER_ID,REMARK,REF_NO) VALUES (?,?,?,?,?,?,?)";
                detail.get(0).setRef_no(lb.generateKey(dataConnection, "stktrfouthd", "REF_NO", "LENGTH(ref_no) >8 and REF_NO", "STOT", 10));
                detail.get(0).setInv_no(lb.getLast(dataConnection, "INV_NO", "stktrfouthd", "IS_DEL", "0") + 1);
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setInt(1, detail.get(0).getInv_no());
                pstLocal.setString(2, detail.get(0).getV_date());
                pstLocal.setString(3, detail.get(0).getFrom_loc());
                pstLocal.setString(4, detail.get(0).getTo_loc());
                pstLocal.setString(5, detail.get(0).getUser_id());
                pstLocal.setString(6, detail.get(0).getRemark());
                pstLocal.setString(7, detail.get(0).getRef_no());
                pstLocal.executeUpdate();

                sql = "DELETE FROM stktrfoutdt WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO stktrfoutdt (REF_NO,SR_NO,PUR_TAG_NO) VALUES (?,?,?)";
                    pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, detail.get(0).getRef_no());
                    pstLocal.setInt(2, i + 1);
                    pstLocal.setString(3, detail.get(i).getTag_no());
                    pstLocal.executeUpdate();

                    sql = "update tag set is_del=5 where ref_no=?";
                    pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, detail.get(i).getTag_no());
                    pstLocal.executeUpdate();
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
