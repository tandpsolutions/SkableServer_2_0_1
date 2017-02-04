/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import oldbUpdate.BankPaymentUpdate;
import oldbUpdate.BankReciept;
import oldbUpdate.CashPaymentUpdate;
import oldbUpdate.CashReciept;
import oldbUpdate.ContraVoucherUpdate;
import oldbUpdate.DCOutwardpdate;
import oldbUpdate.JournalVoucherUpdate;
import oldbUpdate.PurchaseBillUpdate;
import oldbUpdate.PurchaseReturnUpdate;
import oldbUpdate.SalesBillUpdate;
import oldbUpdate.SalesReturnUpdate;
import oldbUpdate.SeriesMasterAdd;
import oldbUpdate.SeriesMasterAddDetail;

/**
 *
 * @author bhaumik
 */
public class ResetProcess extends HttpServlet {

    final DBHelper helper = DBHelper.GetDBHelper();

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
        final Connection dataConnection = helper.getConnMpAdmin();
        try {
            dataConnection.setAutoCommit(false);

            String sql = "update oldb2_1 set OPB=0.00,DR_4=0.00,CR_4=0.00,DR_5=0.00,CR_5=0.00,DR_6=0.00,CR_6=0.00,DR_7=0.00,CR_7=0.00,DR_8=0.00,CR_8=0.00,"
                    + "DR_9=0.00,CR_9=0.00,DR_10=0.00,CR_10=0.00,DR_11=0.00,CR_11=0.00,DR_12=0.00,CR_12=0.00,DR_1=0.00,CR_1=0.00,DR_2=0.00,CR_2=0.00,"
                    + "DR_3=0.00,CR_4=0.00";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
            System.out.println("Reset Account Summary");

            sql = "update oldb0_1 set OPB=0,PPUR_4=0,PSAL_4=0,PPUR_5=0,PSAL_5=0,PPUR_6=0,PSAL_6=0,PPUR_7=0,PSAL_7=0,PPUR_8=0,PSAL_8=0,"
                    + "PPUR_9=0,PSAL_9=0,PPUR_10=0,PSAL_10=0,PPUR_11=0,PSAL_11=0,PPUR_12=0,PSAL_12=0,PPUR_1=0,PSAL_1=0,PPUR_2=0,PSAL_2=0,"
                    + "PPUR_3=0,PSAL_4=0";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);

            System.out.println("Reset Stock Summary");

            sql = "delete from oldb2_4";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
            System.out.println("Reset Bill wise entry");

            sql = "delete from oldb0_2";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
            System.out.println("Reset Bill item ledger");

            sql = "delete from oldb2_2";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);
            System.out.println("Reset Bill account ledger");

            resetOpeningAccount(dataConnection);
            System.out.println("Reset Bill account opening");

            resetOPBStock(dataConnection);
            System.out.println("Reset Bill stock opening");

            addPurchaseBill(dataConnection);
            System.out.println("Reset Purchase Bill");

            addPurchaseReturnBill(dataConnection);
            System.out.println("Reset Purchase Return Bill");

            addSalesBill(dataConnection);
            System.out.println("Reset Sales Bill");

            addSalesReturnBill(dataConnection);
            System.out.println("Reset Sales Return Bill");

            addCashPayment(dataConnection);
            System.out.println("Reset Cash Payment Entry");

            addCashReceipt(dataConnection);
            System.out.println("Reset Cash Receipt Entry");

            addBankPayment(dataConnection);
            System.out.println("Reset Bank Payment Entry");

            addBankReceipt(dataConnection);
            System.out.println("Reset Bank Receipt Entry");

            addJV(dataConnection);
            System.out.println("Reset JV Entry");

            addContra(dataConnection);
            System.out.println("Reset Contra Entry");

            addDCUpdate(dataConnection);
            System.out.println("Reset DC Entry");

            dataConnection.commit();
            dataConnection.setAutoCommit(true);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            try {
                dataConnection.rollback();
                dataConnection.setAutoCommit(true);
            } catch (Exception e) {

            }
        } finally {
            lb.closeConnection(dataConnection);
        }

    }

    private void resetOpeningAccount(Connection dataConnection) throws SQLException {
        String sql = "select * from acntmst where opb_amt <> 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println(rsSel.getString("fname"));
            PreparedStatement pstLocal = dataConnection.prepareStatement("UPDATE OLDB2_1 "
                    + "SET OPB=? WHERE AC_CD=? ");
            if (rsSel.getInt("OPB_EFF") == 0) {
                pstLocal.setDouble(1, rsSel.getDouble("OPB_AMT"));
            } else {
                pstLocal.setDouble(1, rsSel.getDouble("OPB_AMT") * -1);
            }
            pstLocal.setString(2, rsSel.getString("AC_CD"));
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);

            pstLocal = dataConnection.prepareStatement(""
                    + "delete from oldb2_2 where ac_cd='" + rsSel.getString("AC_CD") + "' and doc_cd='OPB' ");
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);

            pstLocal = dataConnection.prepareStatement("INSERT INTO OLDB2_2 "
                    + "(DOC_REF_NO, DOC_DATE, DOC_CD, AC_CD,  VAL, CRDR, PARTICULAR, OPP_AC_CD)"
                    + " VALUES ('', '2016-04-01" + "', 'OPB', ?, ?, ?, ?, '0')");
            pstLocal.setString(1, rsSel.getString("AC_CD"));
            if (rsSel.getInt("OPB_EFF") == 0) {
                pstLocal.setDouble(2, rsSel.getDouble("OPB_AMT"));
            } else {
                pstLocal.setDouble(2, rsSel.getDouble("OPB_AMT") * -1);
            }
            pstLocal.setInt(3, 0);
            pstLocal.setString(4, "Opening Balance");
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);

            sql = "insert into oldb2_4 (DOC_REF_NO,DOC_CD,INV_NO,DOC_DATE,AC_CD,TOT_AMT,UNPAID_AMT,DUE_DATE,CUR_ADJST)"
                    + " values (?,'OPB',0,'2016-04-01',?,?,?,'2016-04-01',0.0)";
            pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, rsSel.getString("AC_CD"));
            pstLocal.setString(2, rsSel.getString("AC_CD"));
            if (rsSel.getInt("OPB_EFF") == 0) {
                pstLocal.setDouble(3, rsSel.getDouble("OPB_AMT"));
            } else {
                pstLocal.setDouble(3, rsSel.getDouble("OPB_AMT") * -1);
            }
            if (rsSel.getInt("OPB_EFF") == 0) {
                pstLocal.setDouble(4, rsSel.getDouble("OPB_AMT"));
            } else {
                pstLocal.setDouble(4, rsSel.getDouble("OPB_AMT") * -1);
            }
            pstLocal.executeUpdate();
            lb.closeStatement(pstLocal);

        }
        lb.closeResultSet(rsSel);
        lb.closeStatement(pstSel);
    }

    private void resetOPBStock(Connection dataConnection) throws SQLException {
        String sql = "select * from seriesmst where OPB_QTY <> 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println(rsSel.getString("SR_NAME"));
            if (lb.getData(dataConnection, "SR_CD", "opb_sr_val", "SR_CD", rsSel.getString("SR_CD"), 0).equalsIgnoreCase("")) {
                SeriesMasterAdd smu = new SeriesMasterAdd(dataConnection);
                smu.seriesUpdateSingle(rsSel.getString("SR_CD"), rsSel.getString("OPB_QTY"), rsSel.getString("OPB_VAL"), "1");
            } else {
                SeriesMasterAddDetail smu = new SeriesMasterAddDetail(dataConnection);
                smu.seriesUpdate(rsSel.getString("SR_CD"));
            }
        }
        lb.closeResultSet(rsSel);
        lb.closeStatement(pstSel);
    }

    private void addPurchaseBill(Connection dataConnection) throws SQLException {
        String sql = "select * from lbrphd where IS_DEL= 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("PB " + rsSel.getString("INV_NO"));
            sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, rsSel.getString("REF_NO"));
            pstLocal.setString(2, "PB");
            pstLocal.setInt(3, rsSel.getInt("INV_NO"));
            pstLocal.setString(4, rsSel.getString("V_DATE"));
            pstLocal.setString(5, rsSel.getString("AC_CD"));
            pstLocal.setDouble(6, rsSel.getDouble("NET_AMT") * -1);
            pstLocal.setDouble(7, rsSel.getDouble("NET_AMT") * -1);
            pstLocal.setString(8, rsSel.getString("DUE_DATE"));
            pstLocal.setDouble(9, 0.00);
            pstLocal.executeUpdate();
            if (pstLocal != null) {
                lb.closeStatement(pstLocal);
            }
            new PurchaseBillUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addPurchaseReturnBill(Connection dataConnection) throws SQLException {
        String sql = "select * from PRHD where IS_DEL= 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("PR " + rsSel.getString("INV_NO"));
            sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, rsSel.getString("REF_NO"));
            pstLocal.setString(2, "PR");
            pstLocal.setInt(3, rsSel.getInt("INV_NO"));
            pstLocal.setString(4, rsSel.getString("V_DATE"));
            pstLocal.setString(5, rsSel.getString("AC_CD"));
            pstLocal.setDouble(6, rsSel.getDouble("NET_AMT"));
            pstLocal.setDouble(7, rsSel.getDouble("NET_AMT"));
            pstLocal.setString(8, rsSel.getString("DUE_DATE"));
            pstLocal.setDouble(9, 0.00);
            pstLocal.executeUpdate();
            if (pstLocal != null) {
                lb.closeStatement(pstLocal);
            }
            new PurchaseReturnUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addSalesBill(Connection dataConnection) throws SQLException {
        String sql = "select * from VILSHD where IS_DEL= 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("SALES " + rsSel.getString("INV_NO"));
            sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, rsSel.getString("REF_NO"));
            pstLocal.setString(2, "SL");
            pstLocal.setInt(3, rsSel.getInt("INV_NO"));
            pstLocal.setString(4, rsSel.getString("V_DATE"));
            pstLocal.setString(5, rsSel.getString("AC_CD"));
            pstLocal.setDouble(6, rsSel.getDouble("NET_AMT"));
            pstLocal.setDouble(7, rsSel.getDouble("NET_AMT"));
            pstLocal.setString(8, rsSel.getString("DUE_DATE"));
            pstLocal.setDouble(9, 0.00);
            pstLocal.executeUpdate();
            if (pstLocal != null) {
                lb.closeStatement(pstLocal);
            }
            new SalesBillUpdate().resetEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addSalesReturnBill(Connection dataConnection) throws SQLException {
        String sql = "select * from SRHD where IS_DEL= 0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("SALES RETURN " + rsSel.getString("INV_NO"));
            sql = "INSERT INTO oldb2_4 (DOC_REF_NO, DOC_CD, INV_NO, DOC_DATE, AC_CD, TOT_AMT, UNPAID_AMT, DUE_DATE, CUR_ADJST)"
                    + " VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, rsSel.getString("REF_NO"));
            pstLocal.setString(2, "SR");
            pstLocal.setInt(3, rsSel.getInt("INV_NO"));
            pstLocal.setString(4, rsSel.getString("V_DATE"));
            pstLocal.setString(5, rsSel.getString("AC_CD"));
            pstLocal.setDouble(6, rsSel.getDouble("NET_AMT") * -1);
            pstLocal.setDouble(7, rsSel.getDouble("NET_AMT") * -1);
            pstLocal.setString(8, rsSel.getString("DUE_DATE"));
            pstLocal.setDouble(9, 0.00);
            pstLocal.executeUpdate();
            if (pstLocal != null) {
                lb.closeStatement(pstLocal);
            }
            new SalesReturnUpdate().resetEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addCashPayment(Connection dataConnection) throws SQLException {
        String sql = "select * from cprhd where  CTYPE=0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Cash Payment " + rsSel.getString("REF_NO"));
            new CashPaymentUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addCashReceipt(Connection dataConnection) throws SQLException {
        String sql = "select * from cprhd where  CTYPE=1";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Cash Receipt " + rsSel.getString("REF_NO"));
            new CashReciept().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addBankPayment(Connection dataConnection) throws SQLException {
        String sql = "select * from BPRHD where CTYPE=0";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Bank Payment " + rsSel.getString("REF_NO"));
            new BankPaymentUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addBankReceipt(Connection dataConnection) throws SQLException {
        String sql = "select * from BPRHD where CTYPE=1";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Bank Receipt " + rsSel.getString("REF_NO"));
            new BankReciept().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addJV(Connection dataConnection) throws SQLException {
        String sql = "select * from JVHD ";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("JV " + rsSel.getString("REF_NO"));
            new JournalVoucherUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addContra(Connection dataConnection) throws SQLException {
        String sql = "select * from contrahd ";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Contra " + rsSel.getString("REF_NO"));
            new ContraVoucherUpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
        }
    }

    private void addDCUpdate(Connection dataConnection) throws SQLException {
        String sql = "select * from dchd where is_del=0 ";
        PreparedStatement pstSel = dataConnection.prepareStatement(sql);
        ResultSet rsSel = pstSel.executeQuery();
        while (rsSel.next()) {
            System.out.println("Contra " + rsSel.getString("REF_NO"));
            new DCOutwardpdate().addEntry(dataConnection, rsSel.getString("REF_NO"));
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
