/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DC;

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
import model.SalesBillDetail;
import oldbUpdate.DCInwardUpdate;
import oldbUpdate.DCOutwardpdate;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateDCDetail extends HttpServlet {

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
        TypeToken<List<SalesBillDetail>> token = new TypeToken<List<SalesBillDetail>>() {
        };
        List<SalesBillDetail> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher((ArrayList<SalesBillDetail>) detail));

    }

    private JsonObject saveVoucher(final ArrayList<SalesBillDetail> detail) {
        String ref_no = detail.get(0).getRef_no();
        final JsonObject jResultObj = new JsonObject();
        final Connection dataConnection = helper.getConnMpAdmin();

        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                String sql = "";
                if (ref_no.equalsIgnoreCase("")) {
                    sql = "INSERT INTO DCHD (INV_NO,V_DATE,V_TYPE,AC_CD,DET_TOT,USER_ID,REF_NO) VALUES (?,?,?,?,?,?,?)";
                    detail.get(0).setRef_no(lb.generateKey(dataConnection, "DCHD", "REF_NO", "DCI", 7));
                } else {
                    if (detail.get(0).getV_type().equalsIgnoreCase("0")) {
                        new DCOutwardpdate().deleteEntry(dataConnection, ref_no);
                    } else {
                        new DCInwardUpdate().deleteEntry(dataConnection, ref_no);
                    }
                    sql = "UPDATE DCHD set ref_no=?,V_DATE=?,V_TYPE=?,AC_CD=?"
                            + ",DET_TOT=?,USER_ID=?,EDIT_NO=EDIT_NO+1,TIME_STAMP=CURRENT_TIMESTAMP where ref_no=?";
                }
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                if (ref_no.equalsIgnoreCase("")) {
                    pstLocal.setInt(1, lb.getLast(dataConnection, "INV_NO", "DCHD", "v_type=" + detail.get(0).getV_type() + " and IS_DEL", "0") + 1);
                } else {
                    pstLocal.setString(1, detail.get(0).getRef_no());
                }
                pstLocal.setString(2, detail.get(0).getV_date());
                pstLocal.setString(3, detail.get(0).getV_type());
                pstLocal.setString(4, detail.get(0).getAc_cd());
                pstLocal.setDouble(5, detail.get(0).getDet_tot());
                pstLocal.setString(6, detail.get(0).getUser_id());
                pstLocal.setString(7, detail.get(0).getRef_no());
                pstLocal.executeUpdate();

                sql = "Update DCHD set INIT_TIMESTAMP = TIME_STAMP where ref_no='" + detail.get(0).getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                sql = "DELETE FROM DCDT WHERE REF_NO='" + detail.get(0).getRef_no() + "'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.executeUpdate();

                if (detail.get(0).getV_type().equalsIgnoreCase("1")) {
                    for (int i = 0; i < detail.size(); i++) {
                        if (detail.get(i).getPur_tag_no().equalsIgnoreCase("")) {
                            if (detail.get(i).getTag_no().equalsIgnoreCase("")) {
                                String pref = getMonth() + (Calendar.getInstance().get(Calendar.YEAR) + "").substring(2);
                                String tag = lb.generateKey(dataConnection, "tag", "tag_no", pref, 16);
                                detail.get(i).setTag_no(tag);
                            }
                            detail.get(i).setPur_tag_no(lb.generateKey(dataConnection, "tag", "ref_no", "T", 7));
                            sql = "insert into TAG (TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,PUR_RATE,BASIC_PUR_RATE,pur_ref_no,DISC_RATE,MRP,BRANCH_CD,ref_no,IS_MAIN,PUR_DATE) "
                                    + "values (?,?,?,?,?,?,?,?,?,?,?,1,?)";
                            PreparedStatement pstUpdate = dataConnection.prepareStatement(sql);
                            pstUpdate.setString(1, detail.get(i).getTag_no());
                            pstUpdate.setString(2, detail.get(i).getSr_cd());
                            pstUpdate.setString(3, detail.get(i).getImei_no());
                            pstUpdate.setString(4, detail.get(i).getSerial_no());
                            pstUpdate.setDouble(5, detail.get(i).getRate());
                            pstUpdate.setDouble(6, detail.get(i).getRate());
                            pstUpdate.setString(7, detail.get(0).getRef_no());
                            pstUpdate.setDouble(8, 0.00);
                            pstUpdate.setDouble(9, 0.00);
                            pstUpdate.setInt(10, 1);
                            pstUpdate.setString(11, detail.get(i).getPur_tag_no());
                            pstUpdate.setString(12, detail.get(i).getV_date());
                            pstUpdate.executeUpdate();
                            if (pstLocal != null) {
                                lb.closeStatement(pstLocal);
                            }
                        } else {
                            sql = "update TAG set sr_cd=?,imei_no=?,SERAIL_NO=?,pur_rate=?,BASIC_PUR_RATE=?,DISC_RATE=?,MRP=?,PUR_DATE=? where ref_no=?";
                            PreparedStatement pstUpdate = dataConnection.prepareStatement(sql);
                            pstUpdate.setString(1, detail.get(i).getSr_cd());
                            pstUpdate.setString(2, detail.get(i).getImei_no());
                            pstUpdate.setString(3, detail.get(i).getSerial_no());
                            pstUpdate.setDouble(4, detail.get(i).getRate());
                            pstUpdate.setDouble(5, detail.get(i).getRate());
                            pstUpdate.setDouble(6, 0.00);
                            pstUpdate.setDouble(7, 0.00);
                            pstUpdate.setString(8, detail.get(i).getV_date());
                            pstUpdate.setString(9, detail.get(i).getPur_tag_no());
                            pstUpdate.executeUpdate();
                            if (pstLocal != null) {
                                lb.closeStatement(pstLocal);
                            }
                        }
                    }
                }
                for (int i = 0; i < detail.size(); i++) {
                    sql = "INSERT INTO DCDT (REF_NO,SR_NO,TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,QTY,RATE,AMT,PUR_TAG_NO,remark) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                    pstLocal = dataConnection.prepareStatement(sql);
                    pstLocal.setString(1, detail.get(0).getRef_no());
                    pstLocal.setInt(2, i + 1);
                    pstLocal.setString(3, detail.get(i).getTag_no());
                    pstLocal.setString(4, detail.get(i).getSr_cd());
                    pstLocal.setString(5, detail.get(i).getImei_no());
                    pstLocal.setString(6, detail.get(i).getSerial_no());
                    pstLocal.setInt(7, detail.get(i).getQty());
                    pstLocal.setDouble(8, detail.get(i).getRate());
                    pstLocal.setDouble(9, detail.get(i).getAmt());
                    pstLocal.setString(10, detail.get(i).getPur_tag_no());
                    pstLocal.setString(11, detail.get(i).getRemark());
                    pstLocal.executeUpdate();
                }
                if (detail.get(0).getV_type().equalsIgnoreCase("0")) {
                    new DCOutwardpdate().addEntry(dataConnection, detail.get(0).getRef_no());
                } else {
                    new DCInwardUpdate().addEntry(dataConnection, detail.get(0).getRef_no());
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
