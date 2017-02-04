/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package salesRetrurn;

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
import model.SalesReturnControllerDetailModel;
import model.SalesReturnControllerHeaderModel;
import oldbUpdate.SalesReturnUpdate;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateSalesReturnBill extends HttpServlet {

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
        SalesReturnControllerHeaderModel header = new Gson().fromJson(headerJson, SalesReturnControllerHeaderModel.class);
        TypeToken<List<SalesReturnControllerDetailModel>> token = new TypeToken<List<SalesReturnControllerDetailModel>>() {
        };
        List<SalesReturnControllerDetailModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher(header, (ArrayList<SalesReturnControllerDetailModel>) detail));

    }

    private JsonObject saveVoucher(SalesReturnControllerHeaderModel header, ArrayList<SalesReturnControllerDetailModel> detail) {
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
                    header.setRef_no(lb.generateKey(dataConnection, "SRHD", "REF_NO", "SR", 7));
                    inv_no = lb.getLast(dataConnection, "INV_NO", "SRHD", "IS_DEL", "0") + 1;

                    sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                            + " VALUES (?,?,?,?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getRef_no());
                    pstLocal.setString(2, "SR");
                    pstLocal.setInt(3, inv_no);
                    pstLocal.setString(4, header.getV_DATE());
                    pstLocal.setString(5, header.getAC_CD());
                    pstLocal.setDouble(6, header.getNET_AMT() * -1);
                    pstLocal.setDouble(7, header.getNET_AMT() * -1);
                    pstLocal.setString(8, header.getDUE_DATE());
                    pstLocal.setDouble(9, 0.00);
                    pstLocal.executeUpdate();
                    sql = "INSERT INTO SRHD (INV_NO,V_DATE,V_TYPE,PMT_MODE,AC_CD,DET_TOT,BRANCH_CD,"
                            + "TAX_AMT,ADD_TAX_AMT,NET_AMT,USER_ID,ADJST,ADVANCE_AMT,REMARK,PMT_DAYS,DUE_DATE,REF_NO)"
                            + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                } else {
                    new SalesReturnUpdate().deleteEntry(dataConnection, header.getRef_no());
                    inv_no = Integer.parseInt(lb.getData(dataConnection, "inv_no", "SRHD", "REF_NO", header.getRef_no(), 0));

                    sql = "update  oldb2_4 set DOC_DATE=?, AC_CD=?, TOT_AMT=?, DUE_DATE=? where doc_Ref_no=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getV_DATE());
                    pstLocal.setString(2, header.getAC_CD());
                    pstLocal.setDouble(3, header.getNET_AMT() * -1);
                    pstLocal.setString(4, header.getDUE_DATE());
                    pstLocal.setString(5, header.getRef_no());
                    pstLocal.executeUpdate();
                    sql = "UPDATE SRHD set INV_NO=?,V_DATE=?,V_TYPE=?,PMT_MODE=?,AC_CD=?"
                            + ",DET_TOT=?,BRANCH_CD=?,TAX_AMT=?,ADD_TAX_AMT=?,NET_AMT=?,USER_ID=?,EDIT_NO=EDIT_NO+1,ADJST=?,"
                            + "TIME_STAMP=CURRENT_TIMESTAMP,ADVANCE_AMT=?,remark=?,pmt_days=? ,DUE_DATE=?  where ref_no=?";
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setInt(1, inv_no);
                pstLocal.setString(2, header.getV_DATE());
                pstLocal.setInt(3, header.getV_TYPE());
                pstLocal.setInt(4, header.getPMT_MODE());
                pstLocal.setString(5, header.getAC_CD());
                pstLocal.setDouble(6, header.getDET_TOT());
                pstLocal.setInt(7, header.getBRANCH_CD());
                pstLocal.setDouble(8, header.getTAX_AMT());
                pstLocal.setDouble(9, header.getADD_TAX_AMT());
                pstLocal.setDouble(10, header.getNET_AMT());
                pstLocal.setString(11, header.getUSER_ID());
                pstLocal.setDouble(12, header.getADJST());
                pstLocal.setDouble(13, header.getAdvance_amt());
                pstLocal.setString(14, header.getREMARK());
                pstLocal.setString(15, header.getPmt_days());
                pstLocal.setString(16, header.getDUE_DATE());
                pstLocal.setString(17, header.getRef_no());
                pstLocal.executeUpdate();

                sql = "Update SRHD set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                sql = "DELETE FROM SRDT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                sql = "DELETE FROM PAYMENT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO SRDT (REF_NO,SR_NO,TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,QTY,RATE,DISC_RATE,MRP,AMT,PUR_TAG_NO,TAX_CD,"
                            + "BASIC_AMT,TAX_AMT,ADD_TAX_AMT,IS_MAIN) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getRef_no());
                    pstLocal.setInt(2, i + 1);
                    pstLocal.setString(3, detail.get(i).getTAG_NO());
                    pstLocal.setString(4, lb.getSR_CD(dataConnection, detail.get(i).getSR_CD(), "C"));
                    pstLocal.setString(5, detail.get(i).getIMEI_NO());
                    pstLocal.setString(6, detail.get(i).getSERAIL_NO());
                    pstLocal.setInt(7, detail.get(i).getQTY());
                    pstLocal.setDouble(8, detail.get(i).getRATE());
                    pstLocal.setDouble(9, detail.get(i).getDISC_PER());
                    pstLocal.setDouble(10, detail.get(i).getMRP());
                    pstLocal.setDouble(11, detail.get(i).getAMT());
                    pstLocal.setString(12, detail.get(i).getPUR_TAG_NO());
                    pstLocal.setString(13, detail.get(i).getTAX_CD());
                    pstLocal.setDouble(14, detail.get(i).getBASIC_AMT());
                    pstLocal.setDouble(15, detail.get(i).getTAX_AMT());
                    pstLocal.setDouble(16, detail.get(i).getADD_TAX_AMT());
                    pstLocal.setDouble(17, detail.get(i).getIsMain());
                    pstLocal.executeUpdate();
                }

                sql = "INSERT INTO PAYMENT (CASH_AMT, BANK_CD, BANK_NAME, BANK_BRANCH, CHEQUE_NO, CHEQUE_DATE, BANK_AMT, CARD_NAME, CARD_AMT, "
                        + "REF_NO,USER_ID,vou_date,bajaj_name,bajaj_amt,SFID)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setDouble(1, header.getCASH_AMT());
                pstLocal.setString(2, header.getBANK_CD());
                pstLocal.setString(3, header.getBANK_NAME());
                pstLocal.setString(4, header.getBANK_BRANCH());
                pstLocal.setString(5, header.getCHEQUE_NO());
                pstLocal.setString(6, header.getCHEQUE_DATE());
                pstLocal.setDouble(7, header.getBANK_AMT());
                pstLocal.setString(8, header.getCARD_NAME());
                pstLocal.setDouble(9, header.getCARD_AMT());
                pstLocal.setString(10, header.getRef_no());
                pstLocal.setString(11, header.getUSER_ID());
                pstLocal.setString(12, header.getV_DATE());
                pstLocal.setString(13, header.getBAJAJ_NAME());
                pstLocal.setDouble(14, header.getBAJAJ_AMT());
                pstLocal.setString(15, header.getSFID());
                pstLocal.executeUpdate();

                new SalesReturnUpdate().addEntry(dataConnection, header.getRef_no());

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
