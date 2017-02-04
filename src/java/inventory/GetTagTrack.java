/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package inventory;

import com.google.gson.JsonArray;
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
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class GetTagTrack extends HttpServlet {

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
        final String tag_no = request.getParameter("tag_no");
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {
                String sql = "select tag_no,o.inv_no, "
                        + " doc_ref_no,DOC_CD,DOC_DATE,case when fname is null then '' else concat(FNAME,' ',mname,' ',lname) end as ac_name,"
                        + " case when TRNS_ID='I' then PCS else 0 end as issue,case when TRNS_ID= 'O' or TRNS_ID='R' then PCS else 0 end as receipt"
                        + ",0 as opb, s.SR_NAME,o.branch_cd "
                        + " from OLDB0_2 o left join ACNTMST a  on o.AC_CD=a.AC_CD left join lbrphd l on o.doc_ref_no=l.ref_no left join vilshd v"
                        + " on v.ref_no=o.doc_ref_no left join seriesmst s on o.sr_cd=s.sr_cd where tag_no=? order by o.time_stamp";
                PreparedStatement pstLocal = dataConnection.prepareStatement(sql);

                pstLocal.setString(1, tag_no);
                ResultSet viewDataRs = pstLocal.executeQuery();
                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("tag_no", viewDataRs.getString("tag_no"));
                    object.addProperty("inv_no", viewDataRs.getString("inv_no"));
                    object.addProperty("doc_ref_no", viewDataRs.getString("doc_ref_no"));
                    object.addProperty("doc_cd", viewDataRs.getString("doc_cd"));
                    object.addProperty("doc_date", viewDataRs.getString("doc_date"));
                    object.addProperty("ac_name", viewDataRs.getString("ac_name"));
                    object.addProperty("issue", viewDataRs.getDouble("issue"));
                    object.addProperty("receipt", viewDataRs.getDouble("receipt"));
                    object.addProperty("opb", viewDataRs.getDouble("opb"));
                    object.addProperty("SR_NAME", viewDataRs.getString("SR_NAME"));
                    object.addProperty("branch_cd", viewDataRs.getString("branch_cd"));
                    array.add(object);
                }
                lb.closeResultSet(viewDataRs);
                lb.closeStatement(pstLocal);
                lb.closeConnection(dataConnection);
                jResultObj.addProperty("result", 1);
                jResultObj.addProperty("Cause", "success");
                jResultObj.add("data", array);
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
