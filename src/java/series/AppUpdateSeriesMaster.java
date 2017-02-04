/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package series;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.OPBSrVal;
import oldbUpdate.SeriesMasterDelete;
import oldbUpdate.SeriesMasterAdd;
import oldbUpdate.SeriesMasterAddDetail;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AppUpdateSeriesMaster extends HttpServlet {

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
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final Library lb = Library.getInstance();
        String sr_cd = request.getParameter("sr_cd");
        String sr_alias = request.getParameter("sr_alias");
        final String sr_name = request.getParameter("sr_name");
        final String model_cd = request.getParameter("model_cd");
        final String memory_cd = request.getParameter("memory_cd");
        final String opb_qty = request.getParameter("opb_qty");
        final String opb_val = request.getParameter("opb_val");
        final String color_cd = request.getParameter("color_cd");
        final String user_id = request.getParameter("user_id");
        final String detailJson = request.getParameter("detail");
        TypeToken<List<OPBSrVal>> token = new TypeToken<List<OPBSrVal>>() {
        };
        List<OPBSrVal> detail = new Gson().fromJson(detailJson, token.getType());
        final JsonObject jResultObj = new JsonObject();

        if (dataConnection != null) {
            try {
                String sql = "";
                if (sr_cd.equalsIgnoreCase("")) {
                    sql = "INSERT INTO SERIESMST (SR_ALIAS,SR_NAME,MODEL_CD,MEMORY_CD,COLOUR_CD,OPB_QTY,OPB_VAL,USER_ID ,SR_CD) VALUES (?,?,?,?,?,?,?,?,?)";
                    sr_cd = lb.generateKey(dataConnection, "SERIESMST", "SR_CD", "S", 7);
                } else {
                    SeriesMasterDelete smu = new SeriesMasterDelete(dataConnection);
                    smu.seriesUpdate(sr_cd, "1");
                    sql = "UPDATE SERIESMST SET SR_ALIAS=?,SR_NAME=?,MODEL_CD=?,MEMORY_CD=?,COLOUR_CD=?,OPB_QTY=?,OPB_VAL=?,USER_ID=?,EDIT_NO=EDIT_NO+1,TIME_STAMP=CURRENT_TIMESTAMP where SR_CD=?";
                }

                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, sr_alias);
                pstLocal.setString(2, sr_name);
                pstLocal.setString(3, model_cd);
                pstLocal.setString(4, memory_cd);
                pstLocal.setString(5, color_cd);
                pstLocal.setString(6, opb_qty);
                pstLocal.setString(7, opb_val);
                pstLocal.setString(8, user_id);
                pstLocal.setString(9, sr_cd);
                pstLocal.executeUpdate();

                if (!detail.isEmpty()) {
                    for (int i = 0; i < detail.size(); i++) {
                        if (detail.get(i).getRef_no().equalsIgnoreCase("")) {
                            detail.get(i).setRef_no(lb.generateKey(dataConnection, "TAG", "REF_NO", "T", 7));
                            sql = "insert into TAG (REF_NO,TAG_NO,SR_CD,IMEI_NO,SERAIL_NO,PUR_RATE,PUR_REF_NO,BASIC_PUR_RATE,GODOWN,"
                                    + "BRANCH_CD) values (?,?,?,?,?,?,?,?,?,?)";
                            pstLocal = dataConnection.prepareStatement(sql);
                            pstLocal.setString(1, detail.get(i).getRef_no());
                            pstLocal.setString(2, detail.get(i).getTag_no());
                            pstLocal.setString(3, sr_cd);
                            pstLocal.setString(4, detail.get(i).getImei());
                            pstLocal.setString(5, detail.get(i).getSerial());
                            pstLocal.setDouble(6, detail.get(i).getP_rate());
                            pstLocal.setString(7, "OPB");
                            pstLocal.setDouble(8, detail.get(i).getP_rate());
                            pstLocal.setString(9, "0");
                            pstLocal.setString(10, detail.get(i).getBranch_cd());
                            pstLocal.executeUpdate();
                        } else {
                            sql = "update TAG set PUR_RATE=?,BASIC_PUR_RATE=?,BRANCH_CD=? where ref_no=?";
                            pstLocal = dataConnection.prepareStatement(sql);
                            pstLocal.setDouble(1, detail.get(i).getP_rate());
                            pstLocal.setDouble(2, detail.get(i).getP_rate());
                            pstLocal.setString(3, detail.get(i).getBranch_cd());
                            pstLocal.setString(4, detail.get(i).getRef_no());
                            pstLocal.executeUpdate();
                        }
                    }
                }

                if (!detail.isEmpty()) {
                    for (int i = 0; i < detail.size(); i++) {
                        sql = "insert into opb_sr_val (SR_CD,SR_NO,TAG_NO,IMEI_NO,SERIAL_NO,P_RATE,BRANCH_CD,REF_NO) values (?,?,?,?,?,?,?,?)";
                        pstLocal = dataConnection.prepareStatement(sql);
                        pstLocal.setString(1, detail.get(i).getSr_cd());
                        pstLocal.setInt(2, detail.get(i).getSr_no());
                        pstLocal.setString(3, detail.get(i).getTag_no());
                        pstLocal.setString(4, detail.get(i).getImei());
                        pstLocal.setString(5, detail.get(i).getSerial());
                        pstLocal.setDouble(6, detail.get(i).getP_rate());
                        pstLocal.setString(7, detail.get(i).getBranch_cd());
                        pstLocal.setString(8, detail.get(i).getRef_no());
                        pstLocal.executeUpdate();
                    }
                }
                if (detail.isEmpty()) {
                    SeriesMasterAdd smu = new SeriesMasterAdd(dataConnection);
                    smu.seriesUpdateSingle(sr_cd, opb_qty, opb_val, "1");
                } else {
                    SeriesMasterAddDetail smu = new SeriesMasterAddDetail(dataConnection);
                    smu.seriesUpdate(sr_cd, detail);
                }
                sql = "DELETE FROM tag WHERE tag_no NOT IN(SELECT tag_no FROM opb_sr_val WHERE sr_cd=?) AND sr_cd=? and PUR_REF_NO='OPB'";
                pstLocal = dataConnection.prepareStatement(sql);
                pstLocal.setString(1, sr_cd);
                pstLocal.setString(2, sr_cd);
                pstLocal.executeUpdate();

                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.addProperty("sr_cd", sr_cd);
            } catch (SQLNonTransientConnectionException ex1) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", "Server is down");
            } catch (SQLException ex) {
                jResultObj.addProperty("result", -1);
                jResultObj.addProperty("Cause", ex.getMessage());
            } finally {
                lb.closeConnection(dataConnection);
            }
        }
        response.getWriter().print(jResultObj);
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
