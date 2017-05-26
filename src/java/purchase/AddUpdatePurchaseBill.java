/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package purchase;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SwingWorker;
import model.PurcahseControllerDetailModel;
import model.PurchaseControllerHeaderModel;
import oldbUpdate.PurchaseBillUpdate;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdatePurchaseBill extends HttpServlet {

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
                String ref_no = header.getRef_no();
                if (header.getRef_no().equalsIgnoreCase("")) {
                    header.setRef_no(lb.generateKey(dataConnection, "LBRPHD", "REF_NO", "05", 7));
                    inv_no = lb.getLast(dataConnection, "INV_NO", "LBRPHD", "IS_DEL", "0") + 1;

                    sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                            + " VALUES (?,?,?,?,?,?,?,?,?)";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getRef_no());
                    pstLocal.setString(2, "PB");
                    pstLocal.setInt(3, inv_no);
                    pstLocal.setString(4, header.getV_DATE());
                    pstLocal.setString(5, header.getAC_CD());
                    pstLocal.setDouble(6, header.getNET_AMT() * -1);
                    pstLocal.setDouble(7, header.getNET_AMT() * -1);
                    pstLocal.setString(8, header.getDUE_DATE());
                    pstLocal.setDouble(9, 0.00);
                    pstLocal.executeUpdate();
                    if (pstLocal != null) {
                        lb.closeStatement(pstLocal);
                    }
                    sql = "INSERT INTO LBRPHD (INV_NO,V_DATE,V_TYPE,PMT_MODE,BILL_DATE,BILL_NO,AC_CD,DET_TOT,BRANCH_CD,"
                            + "TAX_AMT,ADD_TAX_AMT,NET_AMT,USER_ID,ADJST,REMARK,fr_chg ,DUE_DATE,SCHEME_CD,REF_NO) "
                            + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                } else {
                    if (header.getV_TYPE() == 2) {
                        new PurchaseBillUpdate().deleteEntryD(dataConnection, header.getRef_no());
                    } else {
                        new PurchaseBillUpdate().deleteEntry(dataConnection, header.getRef_no());
                    }
                    inv_no = Integer.parseInt(lb.getData(dataConnection, "inv_no", "LBRPHD", "REF_NO", header.getRef_no(), 0));

                    sql = "update  oldb2_4 set DOC_DATE=?, AC_CD=?, TOT_AMT=?, DUE_DATE=? where doc_Ref_no=?";
                    PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, header.getV_DATE());
                    pstLocal.setString(2, header.getAC_CD());
                    pstLocal.setDouble(3, header.getNET_AMT() * -1);
                    pstLocal.setString(4, header.getDUE_DATE());
                    pstLocal.setString(5, header.getRef_no());
                    pstLocal.executeUpdate();
                    if (pstLocal != null) {
                        lb.closeStatement(pstLocal);
                    }
                    sql = "UPDATE LBRPHD set INV_NO=?,V_DATE=?,V_TYPE=?,PMT_MODE=?,BILL_DATE=?,BILL_NO=?,AC_CD=?"
                            + ",DET_TOT=?,BRANCH_CD=?,TAX_AMT=?,ADD_TAX_AMT=?,NET_AMT=?,USER_ID=?,EDIT_NO=EDIT_NO+1,ADJST=?,"
                            + "TIME_STAMP=CURRENT_TIMESTAMP,REMARK=?,fr_chg =?,DUE_DATE=?,SCHEME_CD=?  where ref_no=?";
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setInt(1, inv_no);
                pstLocal.setString(2, header.getV_DATE());
                pstLocal.setInt(3, header.getV_TYPE());
                pstLocal.setInt(4, header.getPMT_MODE());
                pstLocal.setString(5, header.getBILL_DATE());
                pstLocal.setString(6, header.getBILL_NO());
                pstLocal.setString(7, header.getAC_CD());
                pstLocal.setDouble(8, header.getDET_TOT());
                pstLocal.setInt(9, header.getBRANCH_CD());
                pstLocal.setDouble(10, header.getTAX_AMT());
                pstLocal.setDouble(11, header.getADD_TAX_AMT());
                pstLocal.setDouble(12, header.getNET_AMT());
                pstLocal.setString(13, header.getUSER_ID());
                pstLocal.setDouble(14, header.getADJST());
                pstLocal.setString(15, header.getREMARK());
                pstLocal.setDouble(16, header.getFRIEGHT_CHARGES());
                pstLocal.setString(17, header.getDUE_DATE());
                pstLocal.setString(18, header.getSCHEME_CD());
                pstLocal.setString(19, header.getRef_no());
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                sql = "Update lbrphd set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                sql = "DELETE FROM LBRPDT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                sql = "DELETE FROM PAYMENT WHERE REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                for (int i = 0; i < detail.size(); i++) {
                    if (lb.getData(dataConnection, "ref_no", "TAG", "ref_no", detail.get(i).getPUR_TAG_NO(), 0).equalsIgnoreCase("")) {
                        if (detail.get(i).getTAG_NO().equalsIgnoreCase("")) {
                            Date dt = new SimpleDateFormat("yyyy-MM-dd").parse(header.getV_DATE());
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(dt);
                            String pref = getMonth(cal) + (cal.get(Calendar.YEAR) + "").substring(2);
                            if (header.getV_TYPE() == 2) {
                                pref = "Z" + pref;
                            }
                            String tag = lb.generateKey(dataConnection, "tag", "tag_no", pref, 16);
                            detail.get(i).setTAG_NO(tag);
                        }
                        detail.get(i).setPUR_TAG_NO(lb.generateKey(dataConnection, "tag", "ref_no", "T", 7));
                        sql = "insert into TAG (TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,PUR_RATE,BASIC_PUR_RATE,pur_ref_no,DISC_RATE,NLC,MRP,BRANCH_CD,ref_no,IS_MAIN) "
                                + "values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
                        PreparedStatement pstUpdate = dataConnection.prepareStatement(sql);
                        pstUpdate.setString(1, detail.get(i).getTAG_NO());
                        pstUpdate.setString(2, lb.getSR_CD(dataConnection, detail.get(i).getSR_CD(), "C"));
                        pstUpdate.setString(3, detail.get(i).getIMEI_NO());
                        pstUpdate.setString(4, detail.get(i).getSERAIL_NO());
                        pstUpdate.setDouble(5, detail.get(i).getRATE());
                        pstUpdate.setDouble(6, detail.get(i).getRATE());
                        pstUpdate.setString(7, header.getRef_no());
                        pstUpdate.setDouble(8, detail.get(i).getDISC_PER());
                        pstUpdate.setDouble(9, detail.get(i).getNLC());
                        pstUpdate.setDouble(10, detail.get(i).getMRP());
                        pstUpdate.setInt(11, header.getBRANCH_CD());
                        pstUpdate.setString(12, detail.get(i).getPUR_TAG_NO());
                        pstUpdate.setInt(13, detail.get(i).getIsMain());
                        pstUpdate.executeUpdate();
                        if (pstLocal != null) {
                            lb.closeStatement(pstLocal);
                        }

                    } else {
                        sql = "update TAG set sr_cd=?,imei_no=?,SERAIL_NO=?,pur_rate=?,BASIC_PUR_RATE=?,DISC_RATE=?,NLC=?,MRP=? where ref_no=?";
                        PreparedStatement pstUpdate = dataConnection.prepareStatement(sql);
                        pstUpdate.setString(1, lb.getSR_CD(dataConnection, detail.get(i).getSR_CD(), "C"));
                        pstUpdate.setString(2, detail.get(i).getIMEI_NO());
                        pstUpdate.setString(3, detail.get(i).getSERAIL_NO());
                        pstUpdate.setDouble(4, detail.get(i).getRATE());
                        pstUpdate.setDouble(5, detail.get(i).getRATE());
                        pstUpdate.setDouble(6, detail.get(i).getDISC_PER());
                        pstUpdate.setDouble(7, detail.get(i).getNLC());
                        pstUpdate.setDouble(8, detail.get(i).getMRP());
                        pstUpdate.setString(9, detail.get(i).getPUR_TAG_NO());
                        pstUpdate.executeUpdate();
                        if (pstLocal != null) {
                            lb.closeStatement(pstLocal);
                        }

                    }
                }

                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO LBRPDT (REF_NO,SR_NO,TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,QTY,RATE,DISC_RATE,MRP,AMT,PUR_TAG_NO,TAX_CD,"
                            + "BASIC_AMT,TAX_AMT,ADD_TAX_AMT,IS_MAIN,NLC) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
                    pstLocal.setDouble(18, detail.get(i).getNLC());
                    pstLocal.executeUpdate();
                    if (pstLocal != null) {
                        lb.closeStatement(pstLocal);
                    }
                }

                sql = "INSERT INTO PAYMENT (CASH_AMT, BANK_CD, BANK_NAME, BANK_BRANCH, CHEQUE_NO, CHEQUE_DATE, BANK_AMT, CARD_NAME, CARD_AMT, REF_NO,USER_ID,vou_date)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, "0");

                pstLocal.setString(2, "");
                pstLocal.setString(3, "");
                pstLocal.setString(4, "");
                pstLocal.setString(5, "");
                pstLocal.setString(6, null);
                pstLocal.setDouble(7, 0.00);

                pstLocal.setString(8, "");
                pstLocal.setDouble(9, 0.00);
                pstLocal.setString(10, header.getRef_no());
                pstLocal.setString(11, header.getUSER_ID());
                pstLocal.setString(12, header.getV_DATE());

                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                if (header.getV_TYPE() != 2) {
                    new PurchaseBillUpdate().addEntry(dataConnection, header.getRef_no());
                } else {
                    new PurchaseBillUpdate().addEntryD(dataConnection, header.getRef_no());
                }

                sql = "delete from tag where REF_NO not in (select PUR_TAG_NO from LBRPDT where REF_NO ='" + header.getRef_no() + "') "
                        + "and PUR_REF_NO='" + header.getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();
                if (pstLocal != null) {
                    lb.closeStatement(pstLocal);
                }

                dataConnection.commit();
                dataConnection.setAutoCommit(true);

                if (ref_no.equalsIgnoreCase("")) {
                    SwingWorker worker = new SwingWorker() {

                        @Override
                        protected Object doInBackground() throws Exception {
//                            lb.displaySalesVoucherPDF(header.getRef_no(), lb.getData(dataConnection, "email", "phbkmst", "AC_CD", header.getAC_CD(), dataConnection);
                            lb.displayPurchaseVoucherEmail(header, detail);
                            return null;
                        }
                    };
                    worker.execute();
                }
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
            } catch (ParseException ex) {
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

    private String getMonth(Calendar cal) {

        switch (cal.get(Calendar.MONTH) + 1) {
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
