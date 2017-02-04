/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package order;

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
import model.OrderBookModel;
import support.DBHelper;
import support.Library;

/**
 *
 * @author bhaumik
 */
public class AddUpdateOrderBookVoucher extends HttpServlet {

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
        TypeToken<List<OrderBookModel>> token = new TypeToken<List<OrderBookModel>>() {
        };
        List<OrderBookModel> detail = new Gson().fromJson(detailJson, token.getType());
        response.getWriter().print(saveVoucher((ArrayList<OrderBookModel>) detail));

    }

    private JsonObject saveVoucher(ArrayList<OrderBookModel> detail) {
        final JsonObject jResultObj = new JsonObject();
        Connection dataConnection = null;
        if (dataConnection == null) {
            dataConnection = helper.getConnMpAdmin();
        }
        if (dataConnection != null) {
            try {
                dataConnection.setAutoCommit(false);
                String sql = null;
                PreparedStatement psLocal = null;
                if (detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    sql = "INSERT INTO orderbook (VDATE, amt, AC_CD, USER_ID, model_cd,memory_cd,colour_cd,remark,REF_NO) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                    detail.get(0).setRef_no(lb.generateKey(dataConnection, "orderbook", "REF_NO", "OB", 7));
                } else if (!detail.get(0).getRef_no().equalsIgnoreCase("")) {
                    sql = "UPDATE orderbook SET VDATE=?, amt=?, AC_CD=?, USER_ID=?, EDIT_NO=EDIT_NO+1, TIME_STAMP=CURRENT_TIMESTAMP"
                            + ",model_cd=?,memory_cd=?,colour_cd=?,remark=? "
                            + " WHERE REF_NO=?";
                }
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.setString(1, detail.get(0).getVdate());
                psLocal.setDouble(2, detail.get(0).getAmt());
                psLocal.setString(3, detail.get(0).getAc_cd());
                psLocal.setString(4, detail.get(0).getUser_id());
                psLocal.setString(5, detail.get(0).getModel_cd());
                psLocal.setString(6, detail.get(0).getMemory_cd());
                psLocal.setString(7, detail.get(0).getColor_cd());
                psLocal.setString(8, detail.get(0).getRemark());
                psLocal.setString(9, detail.get(0).getRef_no());
                psLocal.executeUpdate();

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
