/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sales;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingWorker;
import model.SalesControllerDetailModel;
import model.SalesControllerHeaderModel;
import oldbUpdate.SalesBillUpdate;
import support.DBHelper;
import support.Library;
import support.call;

/**
 *
 * @author bhaumik
 */
public class AddUpdateSalesBill extends HttpServlet {

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
        SalesControllerHeaderModel header = new Gson().fromJson(headerJson, SalesControllerHeaderModel.class);
        TypeToken<List<SalesControllerDetailModel>> token = new TypeToken<List<SalesControllerDetailModel>>() {
        };
        List<SalesControllerDetailModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher(header, (ArrayList<SalesControllerDetailModel>) detail));

    }

    private JsonObject saveVoucher(final SalesControllerHeaderModel header, final ArrayList<SalesControllerDetailModel> detail) {
        final JsonObject jResultObj = new JsonObject();
        final Connection dataConnection;
        String ref_no = header.getRef_no();
        dataConnection = helper.getConnMpAdmin();
        if (dataConnection != null) {
            try {
                int inv_no = 0;
                dataConnection.setAutoCommit(false);
                String sql = "";
                if (header.getRef_no().equalsIgnoreCase("")) {
                    header.setRef_no(lb.generateKey(dataConnection, "VILSHD", "REF_NO", "02", 7));
                    inv_no = lb.getLast(dataConnection, "INV_NO", "VILSHD", "v_type=" + header.getV_TYPE() + " and IS_DEL", "0") + 1;

                    sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                            + " VALUES (?,?,?,?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getRef_no());
                    pstLocal.setString(2, "SL");
                    pstLocal.setInt(3, inv_no);
                    pstLocal.setString(4, header.getV_DATE());
                    pstLocal.setString(5, header.getAC_CD());
                    pstLocal.setDouble(6, header.getNET_AMT());
                    pstLocal.setDouble(7, header.getNET_AMT());
                    pstLocal.setString(8, header.getDUE_DATE());
                    pstLocal.setDouble(9, 0.00);
                    pstLocal.executeUpdate();
                    sql = "INSERT INTO VILSHD (INV_NO,V_DATE,V_TYPE,PMT_MODE,AC_CD,DET_TOT,BRANCH_CD,"
                            + "TAX_AMT,ADD_TAX_AMT,NET_AMT,USER_ID,ADJST,BUY_BACK_MODEL,BUY_BACK_AMT,PART_NO,BUY_BACK_IMEI_NO,ADVANCE_AMT,INS_CD,INS_AMT,"
                            + "BANK_CHARGES,REMARK,PMT_DAYS,DUE_DATE,DISCOUNT,REF_CD,SM_CD,SCHEME_CD,REF_NO) "
                            + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                } else {
                    lb.generateLog(dataConnection, "VILSHD", "VILSHDLG", "ref_no", header.getRef_no());
                    lb.generateLog(dataConnection, "VILSDT", "VILSDTLG", "ref_no", header.getRef_no());
                    lb.generateLog(dataConnection, "PAYMENT", "PAYMENTLG", "ref_no", header.getRef_no());
                    if (header.getV_TYPE() == 3) {
                        new SalesBillUpdate().deleteEntryD(dataConnection, header.getRef_no());
                    } else {
                        new SalesBillUpdate().deleteEntry(dataConnection, header.getRef_no());
                    }
                    inv_no = Integer.parseInt(lb.getData(dataConnection, "inv_no", "VILSHD", "REF_NO", header.getRef_no(), 0));

                    sql = "update  oldb2_4 set DOC_DATE=?, AC_CD=?, TOT_AMT=?,UNPAID_AMT=UNPAID_AMT+? ,DUE_DATE=? where doc_Ref_no=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getV_DATE());
                    pstLocal.setString(2, header.getAC_CD());
                    pstLocal.setDouble(3, header.getNET_AMT());
                    pstLocal.setDouble(4, header.getNET_AMT());
                    pstLocal.setString(5, header.getDUE_DATE());
                    pstLocal.setString(6, header.getRef_no());
                    pstLocal.executeUpdate();
                    sql = "UPDATE VILSHD set INV_NO=?,V_DATE=?,V_TYPE=?,PMT_MODE=?,AC_CD=?"
                            + ",DET_TOT=?,BRANCH_CD=?,TAX_AMT=?,ADD_TAX_AMT=?,NET_AMT=?,USER_ID=?,EDIT_NO=EDIT_NO+1,ADJST=?,"
                            + "TIME_STAMP=CURRENT_TIMESTAMP,BUY_BACK_MODEL=?,BUY_BACK_AMT=?,part_no=?,buy_back_imei_no =?,ADVANCE_AMT=?,"
                            + "ins_cd=?,ins_amt=?,bank_charges=?,remark=?,pmt_days=? ,DUE_DATE=?,DISCOUNT=?,REF_CD=?,SM_CD=?,SCHEME_CD=? where ref_no=?";
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
                pstLocal.setString(13, header.getBuy_back_cd());
                pstLocal.setDouble(14, header.getBuy_back_amt());
                pstLocal.setString(15, header.getPart_no());
                pstLocal.setString(16, header.getBuy_back_imei());
                pstLocal.setDouble(17, header.getAdvance_amt());
                pstLocal.setString(18, header.getIns_cd());
                pstLocal.setDouble(19, header.getIns_amt());
                pstLocal.setDouble(20, header.getBank_charges());
                pstLocal.setString(21, header.getREMARK());
                pstLocal.setString(22, header.getPmt_days());
                pstLocal.setString(23, header.getDUE_DATE());
                pstLocal.setDouble(24, header.getDiscount());
                pstLocal.setString(25, header.getRef_cd());
                pstLocal.setString(26, header.getSm_cd());
                pstLocal.setString(27, header.getSCHEME_CD());
                pstLocal.setString(28, header.getRef_no());
                pstLocal.executeUpdate();

                sql = "Update VILSHD set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                sql = "DELETE FROM VILSDT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                sql = "DELETE FROM PAYMENT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO VILSDT (REF_NO,SR_NO,TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,QTY,RATE,DISC_RATE,MRP,AMT,PUR_TAG_NO,TAX_CD,"
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
                        + "REF_NO,USER_ID,vou_date,bajaj_name,bajaj_amt,SFID,bajaj_per ,bajaj_chg ,card_per, card_chg,card_no,tid_no )"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                pstLocal.setDouble(16, header.getBAJAJ_PER());
                pstLocal.setDouble(17, header.getBAJAJ_CHG());
                pstLocal.setDouble(18, header.getCARD_PER());
                pstLocal.setDouble(19, header.getCARD_CHG());
                pstLocal.setString(20, header.getCard_no());
                pstLocal.setString(21, header.getTid_no());
                pstLocal.executeUpdate();

                if (header.getV_TYPE() == 3) {
                    new SalesBillUpdate().addEntryD(dataConnection, header.getRef_no());
                } else {
                    new SalesBillUpdate().addEntry(dataConnection, header.getRef_no());
                }

                if (ref_no.equalsIgnoreCase("")) {
                    SwingWorker worker = new SwingWorker() {

                        @Override
                        protected Object doInBackground() throws Exception {
//                            lb.displaySalesVoucherPDF(header.getRef_no(), lb.getData(dataConnection, "email", "phbkmst", "AC_CD", header.getAC_CD(), dataConnection);
                            lb.displaySalesVoucherEmail(dataConnection, header, detail);
                            return null;
                        }
                    };
                    worker.execute();

                    worker = new SwingWorker() {

                        @Override
                        protected Object doInBackground() throws Exception {
                            call sms = new call();
                            sms.sendsms(lb.getData(dataConnection, "mobile1", "phbkmst", "AC_CD", header.getAC_CD(), 0), header.getNET_AMT() + "", header.getAc_name());
                            return null;
                        }
                    };
                    worker.execute();
                }
                dataConnection.commit();
                dataConnection.setAutoCommit(true);
                jResultObj.addProperty("ref_no", header.getRef_no());
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

    private String getMonth() {
        switch (Calendar.getInstance().get(Calendar.MONTH) + 1) {
            case 1:
                return "A";
            case 2:
                return "B";
            case 3:
                return "C";
            case 4:
                return "D";
            case 5:
                return "E";
            case 6:
                return "F";
            case 7:
                return "G";
            case 8:
                return "H";
            case 9:
                return "I";
            case 10:
                return "J";
            case 11:
                return "K";
            case 12:
                return "L";
            default:
                return "Z";
        }
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
