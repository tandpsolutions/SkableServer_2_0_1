/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package account;

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
public class GetGroupSummary extends HttpServlet {

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
        final String grp_cd = request.getParameter("grp_cd");
        final double greater_then = Double.parseDouble(request.getParameter("greater_then"));
        final double less_then = Double.parseDouble(request.getParameter("less_then"));
        final DBHelper helper = DBHelper.GetDBHelper();
        final Connection dataConnection = helper.getConnMpAdmin();
        final JsonObject jResultObj = new JsonObject();
        final int mode = Integer.parseInt(request.getParameter("mode"));
        Library lb = Library.getInstance();
        if (dataConnection != null) {
            try {

                String sql = null;
                PreparedStatement pstLocal = null;
                sql = "select concat(fname , ' ' ,mname , ' ' , lname) as fname,opb,";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += " as dr, ";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += " as cr ,(opb+";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += "-";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "-";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") as bal,acntmst.ac_cd from oldb2_1,acntmst,GROUPMST "
                        + "where ACNTMST.GRP_CD=GROUPMST.grp_cd and ((opb+";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += "-";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "-";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") > 1 or (opb+";
                for (int i = 1; i <= 12; i++) {
                    sql += "DR_" + i + "+";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += "-";
                for (int i = 1; i <= 12; i++) {
                    sql += "CR_" + i + "-";
                }
                sql = sql.substring(0, sql.length() - 1);
                sql += ") < -1 )";
                if (!grp_cd.equalsIgnoreCase("")) {
                    sql += "and (ACNTMST.grp_cd='" + grp_cd + "' "
                            + "or acntmst.grp_cd in( select grp_cd from groupmst where head_grp='" + grp_cd + "')) ";
                }
                sql += " and acntmst.ac_cd=oldb2_1.ac_cd";

                if (mode == 0) {
                    sql += " and (opb+";
                    for (int i = 1; i <= 12; i++) {
                        sql += "DR_" + i + "+";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += "-";
                    for (int i = 1; i <= 12; i++) {
                        sql += "CR_" + i + "-";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += ") <> 0";
                } else if (mode == 1) {
                    sql += " and (opb+";
                    for (int i = 1; i <= 12; i++) {
                        sql += "DR_" + i + "+";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += "-";
                    for (int i = 1; i <= 12; i++) {
                        sql += "CR_" + i + "-";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += " ) <0";
                    if (less_then > 0) {
                        sql += " and (opb+";
                        for (int i = 1; i <= 12; i++) {
                            sql += "DR_" + i + "+";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += "-";
                        for (int i = 1; i <= 12; i++) {
                            sql += "CR_" + i + "-";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += " ) >=" + (less_then * -1);
                    }
                    if (greater_then > 0) {
                        sql += " and (opb+";
                        for (int i = 1; i <= 12; i++) {
                            sql += "DR_" + i + "+";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += "-";
                        for (int i = 1; i <= 12; i++) {
                            sql += "CR_" + i + "-";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += " ) <=" + (greater_then * -1);
                    }
                } else if (mode == 2) {
                    sql += " and (opb+";
                    for (int i = 1; i <= 12; i++) {
                        sql += "DR_" + i + "+";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += "-";
                    for (int i = 1; i <= 12; i++) {
                        sql += "CR_" + i + "-";
                    }
                    sql = sql.substring(0, sql.length() - 1);
                    sql += " ) >0";
                    if (less_then > 0) {
                        sql += " and (opb+";
                        for (int i = 1; i <= 12; i++) {
                            sql += "DR_" + i + "+";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += "-";
                        for (int i = 1; i <= 12; i++) {
                            sql += "CR_" + i + "-";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += " ) >=" + (greater_then);
                    }
                    if (greater_then > 0) {
                        sql += " and (opb+";
                        for (int i = 1; i <= 12; i++) {
                            sql += "DR_" + i + "+";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += "-";
                        for (int i = 1; i <= 12; i++) {
                            sql += "CR_" + i + "-";
                        }
                        sql = sql.substring(0, sql.length() - 1);
                        sql += " ) <=" + less_then;
                    }
                }
                sql += " order by fname";
                pstLocal = dataConnection.prepareStatement(sql);
                ResultSet viewDataRs = pstLocal.executeQuery();

                JsonArray array = new JsonArray();
                while (viewDataRs.next()) {
                    JsonObject object = new JsonObject();
                    object.addProperty("fname", viewDataRs.getString("fname"));
                    object.addProperty("opb", viewDataRs.getDouble("opb"));
                    object.addProperty("dr", viewDataRs.getDouble("dr"));
                    object.addProperty("cr", viewDataRs.getDouble("cr"));
                    object.addProperty("bal", viewDataRs.getDouble("bal"));
                    object.addProperty("ac_cd", viewDataRs.getString("ac_cd"));
                    array.add(object);
                }
                lb.closeResultSet(viewDataRs);
                lb.closeStatement(pstLocal);
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
