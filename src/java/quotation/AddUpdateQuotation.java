/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package quotation;

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
import model.PurcahseControllerDetailModel;
import model.PurchaseControllerHeaderModel;
import support.DBHelper;
import support.Library;

/**
 *
 * @author indianic
 */
public class AddUpdateQuotation extends HttpServlet {

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

        final String headerJson = request.getParameter("header");
        final String detailJson = request.getParameter("detail");
        PurchaseControllerHeaderModel header = new Gson().fromJson(headerJson, PurchaseControllerHeaderModel.class);
        TypeToken<List<PurcahseControllerDetailModel>> token = new TypeToken<List<PurcahseControllerDetailModel>>() {
        };
        List<PurcahseControllerDetailModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher(header, (ArrayList<PurcahseControllerDetailModel>) detail));

    }

    private JsonObject saveVoucher(final PurchaseControllerHeaderModel header, final ArrayList<PurcahseControllerDetailModel> detail) {
        final JsonObject jResultObj = new JsonObject();
        Connection dataConnection = null;
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        if (dataConnection != null) {
            try {
                int inv_no = 0;
                dataConnection.setAutoCommit(false);
                String sql = "";
                if (header.getRef_no().equalsIgnoreCase("")) {
                    header.setRef_no(lb.generateKey(dataConnection, "quotationhd", "REF_NO", "Q", 10));
                    inv_no = lb.getLast(dataConnection, "INV_NO", "quotationhd", "IS_DEL", "0") + 1;
                    sql = "INSERT INTO quotationhd (INV_NO,V_DATE,AC_CD,BRANCH_CD,"
                            + "NET_AMT,USER_ID,REMARK,DUE_DATE,REF_NO) "
                            + "VALUES (?,?,?,?,?,?,?,?,?)";
                } else {
                    inv_no = Integer.parseInt(lb.getData(dataConnection, "inv_no", "quotationhd", "REF_NO", header.getRef_no(), 0));
                    sql = "UPDATE LBRPHD set INV_NO=?,V_DATE=?,AC_CD=?"
                            + ",BRANCH_CD=?,NET_AMT=?,USER_ID=?,EDIT_NO=EDIT_NO+1,TIME_STAMP=CURRENT_TIMESTAMP,REMARK=?,DUE_DATE=? where ref_no=?";
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setInt(1, inv_no);
                pstLocal.setString(2, header.getV_DATE());
                pstLocal.setString(3, header.getAC_CD());
                pstLocal.setInt(4, header.getBRANCH_CD());
                pstLocal.setDouble(5, header.getNET_AMT());
                pstLocal.setString(6, header.getUSER_ID());
                pstLocal.setString(7, header.getREMARK());
                pstLocal.setString(8, header.getDUE_DATE());
                pstLocal.setString(9, header.getRef_no());
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                sql = "DELETE FROM quotationdt WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO quotationdt (REF_NO,SR_NO,SR_CD,QTY,RATE,DISC_per,MRP,amount) VALUES (?,?,?,?,?,?,?,?)";
                    pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getRef_no());
                    pstLocal.setInt(2, i + 1);
                    pstLocal.setString(3, detail.get(i).getSR_CD());
                    pstLocal.setInt(4, detail.get(i).getQTY());
                    pstLocal.setDouble(5, detail.get(i).getRATE());
                    pstLocal.setDouble(6, detail.get(i).getDISC_PER());
                    pstLocal.setDouble(7, detail.get(i).getMRP());
                    pstLocal.setDouble(8, detail.get(i).getAMT());
                    pstLocal.executeUpdate();
                    if (pstLocal != null) {
                        lb.closeStatement(pstLocal);
                    }
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
