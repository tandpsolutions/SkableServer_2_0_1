/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import model.PurcahseControllerDetailModel;
import model.PurchaseControllerHeaderModel;
import model.SalesControllerDetailModel;
import model.SalesControllerHeaderModel;

/**
 *
 * @author bhaumik
 */
public class Library {

    private static Library ourInstance = new Library();
    DBHelper helper = DBHelper.GetDBHelper();

    public static Library getInstance() {
        return ourInstance;
    }

    public JsonArray getAccountMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select a.AC_CD,a.AC_ALIAS,a.FNAME,a.MNAME,a.LNAME,a1.ADD1,p.MOBILE1,a.TIN,a.ref_by,p.email from"
                + "  ACNTMST a left join adbkmst a1 on a.AC_CD=a1.AC_CD left join phbkmst p on a.AC_CD=p.AC_CD "
                + " where " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("AC_ALIAS", rsLocal.getString("AC_ALIAS"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("MNAME", rsLocal.getString("MNAME"));
            object.addProperty("LNAME", rsLocal.getString("LNAME"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("EMAIL", rsLocal.getString("EMAIL"));
            object.addProperty("BAL", getBalance(dataConnection, rsLocal.getString("AC_CD")));
            object.addProperty("ref_by", rsLocal.getString("ref_by"));
            array.add(object);
        }
        return array;
    }

    public void generateLog(Connection dataConnection, String fromTable, String toTable, String field, String value) throws SQLException {

        PreparedStatement psLocal = null;
        psLocal = dataConnection.prepareStatement("delete from " + toTable + " WHERE " + field + "='" + value + "'");
        psLocal.executeUpdate();

        psLocal = dataConnection.prepareStatement("INSERT INTO " + toTable + " SELECT *FROM " + fromTable + " WHERE " + field + "='" + value + "'");
        psLocal.executeUpdate();

    }

    public boolean isDeleted(final Connection dataConnection, final String ref_no) {
        if (ref_no.startsWith("02")) {
            if (getData(dataConnection, "IS_DEL", "VILSHD", "REF_NO", ref_no, 0).equalsIgnoreCase("0")) {
                return true;
            }
        } else if (ref_no.startsWith("05")) {
            if (getData(dataConnection, "IS_DEL", "LBRPHD", "REF_NO", ref_no, 0).equalsIgnoreCase("0")) {
                return true;
            }
        } else if (ref_no.startsWith("SR")) {
            if (getData(dataConnection, "IS_DEL", "SRHD", "REF_NO", ref_no, 0).equalsIgnoreCase("0")) {
                return true;
            }
        } else if (ref_no.startsWith("PR")) {
            if (getData(dataConnection, "IS_DEL", "PRHD", "REF_NO", ref_no, 0).equalsIgnoreCase("0")) {
                return true;
            }
        } else if (ref_no.startsWith("BP")) {
            if (!getData(dataConnection, "REF_NO", "BPRHD", "REF_NO", ref_no, 0).equalsIgnoreCase("")) {
                return true;
            }
        } else if (ref_no.startsWith("BR")) {
            if (!getData(dataConnection, "REF_NO", "BPRHD", "REF_NO", ref_no, 0).equalsIgnoreCase("")) {
                return true;
            }
        } else if (ref_no.startsWith("CP")) {
            if (!getData(dataConnection, "REF_NO", "CPRHD", "REF_NO", ref_no, 0).equalsIgnoreCase("")) {
                return true;
            }
        } else if (ref_no.startsWith("CR")) {
            if (!getData(dataConnection, "REF_NO", "CPRDT", "REF_NO", ref_no, 0).equalsIgnoreCase("")) {
                return true;
            }
        }
        return false;
    }

    public long getRecNOFromOldb0_4(Connection dataConnection, String ref_no, String sr_no) throws SQLException {
        String sql = "select rec_no  from OLDB2_4 o where o.doc_ref_no='" + ref_no + "' and sr_no=" + sr_no;
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getLong(1);
        }
        return -1;
    }

    public String generateKey(Connection dataConnection, String table, String sel_column, String where_col, String prefix, int length) {
        String code = "";
        long no = 0;
        try {
            PreparedStatement pstLocal = dataConnection.prepareStatement("SELECT MAX(" + sel_column + ") FROM " + table + " where " + where_col + " like '" + prefix + "%'");
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                if (rsLocal.getString(1) != null) {
                    String sno = rsLocal.getString(1).substring(prefix.length());
                    no = Integer.parseInt(sno);
                    no++;
                    for (int i = (no + "").length(); i < (length - prefix.length()); i++) {
                        code += "0";
                    }
                    code = prefix + code + no;
                } else {
                    code = prefix;
                    for (int i = 1; i < (length - prefix.length()); i++) {
                        code += "0";
                    }
                    code = code + "1";
                }
            }
            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return code;
    }

    public double getBalance(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "select opb+";
        for (int i = 1; i <= 12; i++) {
            sql += "DR_" + i + "+";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += "-";
        for (int i = 1; i <= 12; i++) {
            sql += "CR_" + i + "-";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += " from oldb2_1 WHERE AC_CD='" + ac_cd + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    private double getBalanceStock(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "select OPB+";
        for (int i = 1; i <= 12; i++) {
            sql += "PPUR_" + i + "+";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += "-";
        for (int i = 1; i <= 12; i++) {
            sql += "PSAL_" + i + "-";
        }
        sql = sql.substring(0, sql.length() - 1);
        sql += " from oldb0_1 WHERE SR_CD='" + ac_cd + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    public double getBalanceStockByDate(Connection dataConnection, String ac_cd, String date, int mode) throws SQLException {
        String sql = "select (sum(case when TRNS_ID='R' then PCS else 0 end) -sum(case when TRNS_ID='I' then pcs else 0 end)+sum(case when TRNS_ID='O' then pcs else 0 end)) as opb,"
                + "sum(case when TRNS_ID='R' then PCS*RATE when TRNS_ID='O' then PCS*RATE else 0 end) "
                + "from OLDB0_2 o where o.sr_cd='" + ac_cd + "' "
                + "and  o.DOC_DATE<'" + date + "' ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            if (mode == 1) {
                return rsLocal.getDouble(mode);
            } else {
                if (rsLocal.getDouble(1) == 0) {
                    return 0;
                } else {
                    return rsLocal.getDouble(2) / rsLocal.getDouble(1);
                }
            }
        }
        return 0.00;
    }

    public double getSalesStockByDate(Connection dataConnection, String ac_cd, String from_date, String to_date) throws SQLException {
        String sql = "select (sum(case when TRNS_ID='I' then pcs else 0 end)) as opb from OLDB0_2 o where o.sr_cd='" + ac_cd + "' "
                + "and  o.DOC_DATE>='" + from_date + "' and  o.DOC_DATE<='" + to_date + "' ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    public double getPurchaseStockByDate(Connection dataConnection, String ac_cd, String from_date, String to_date) throws SQLException {
        String sql = "select (sum(case when TRNS_ID='R' then pcs else 0 end)) as opb from OLDB0_2 o where o.sr_cd='" + ac_cd + "' "
                + "and  o.DOC_DATE>='" + from_date + "' and  o.DOC_DATE<='" + to_date + "' ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    public double getSalesStockValByDate(Connection dataConnection, String ac_cd, String from_date, String to_date) throws SQLException {
        String sql = "select (sum(case when TRNS_ID='I' then PCS*RATE else 0 end)) as opb from OLDB0_2 o where o.sr_cd='" + ac_cd + "' "
                + "and  o.DOC_DATE>='" + from_date + "' and  o.DOC_DATE<='" + to_date + "' ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    public double getPurchaseStockValByDate(Connection dataConnection, String ac_cd, String from_date, String to_date) throws SQLException {
        String sql = "select (sum(case when TRNS_ID='R' then PCS*RATE else 0 end)) as opb from OLDB0_2 o where o.sr_cd='" + ac_cd + "' "
                + "and  o.DOC_DATE>='" + from_date + "' and  o.DOC_DATE<='" + to_date + "' ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getDouble(1);
        }
        return 0.00;
    }

    public long getRecNOFromOldb0_1(Connection dataConnection, String sr_cd, String branch_cd, String prd_st_cd) throws SQLException {
        String sql = "select rec_no  from OLDB0_1 o where o.sr_cd='" + sr_cd + "' "
                + "and  o.branch_cd=" + branch_cd + " and  o.prd_st_cd=" + prd_st_cd + " ";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getLong(1);
        }
        return -1;
    }

    public long getRecNOFromOldb0_3(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "select rec_no  from OLDB2_3 o where o.ac_cd='" + ac_cd + "' "
                + "and  o.DOC_REF_NO='" + ac_cd + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getLong(1);
        }
        return -1;
    }

    public long getRecNOFromOldb0_4(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "select rec_no  from OLDB2_4 o where o.ac_cd='" + ac_cd + "' "
                + "and  o.DOC_REF_NO='" + ac_cd + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        if (rsLocal.next()) {
            return rsLocal.getLong(1);
        }
        return -1;
    }

    public String roundOffDoubleValue(Double strSource) {
        String str = "0";
        int temp = (int) Math.abs(strSource + 0.5);
        str = Convert2DecFmt(temp);
        return str;
    }

    public String Convert2DecFmt(double strSource) {
        String str = "0";
        try {

            NumberFormat formatter = new DecimalFormat("#0.00");
            str = formatter.format(strSource);
        } catch (Exception ex) {
        }
        return str;
    }

    public void displaySalesVoucherEmail(Connection dataConnection, SalesControllerHeaderModel header, ArrayList<SalesControllerDetailModel> detail) {

        try {
            String content = "";
            content += "<html><body>"
                    + "<label>" + getData(DBHelper.GetDBHelper().getMainConnection(), "BRANCH_NAME", "BRANCHMST", "BRANCH_CD", header.getBRANCH_CD() + "", 0) + "</label><br/>"
                    + "<label>SB</label><br/>"
                    + "<label>Name :</label><label>" + header.getAc_name() + "</label><br/>"
                    + "        <br/>"
                    + "        <table border=\"1\" width=\"100%\" align=\"center\">";
            content += " <tr>"
                    + "            <td>" + 1 + "</td>"
                    + "            <td>"
                    + "                " + detail.get(0).getSR_CD() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                 " + detail.get(0).getTAG_NO() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getQTY() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getRATE() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getAMT() + ""
                    + "            </td>"
                    + "        </tr>";
            for (int i = 1; i < detail.size(); i++) {
                content += " <tr>"
                        + "            <td>" + (i + 1) + "</td>"
                        + "            <td>"
                        + "                " + detail.get(i).getSR_CD() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                 " + detail.get(i).getTAG_NO() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getQTY() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getRATE() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getAMT() + ""
                        + "            </td>"
                        + "        </tr>";
            }
            content += "</table> <br/><br/>"
                    + "Payment Type " + ((header.getPMT_MODE() == 0) ? "Cash" : "Credit")
                    + "<br/><br/><table border=\"1\" width=\"100%\" align=\"center\">"
                    + "<tr>"
                    + "    <td>"
                    + "        Cash"
                    + "    </td>"
                    + "    <td>"
                    + header.getCASH_AMT()
                    + "    </td>"
                    + "</tr>"
                    + "<tr>"
                    + "    <td>"
                    + "        Bank"
                    + "    </td>"
                    + "    <td>"
                    + header.getBANK_AMT()
                    + "    </td>"
                    + "</tr>"
                    + "<tr>"
                    + "    <td>"
                    + "        Card"
                    + "    </td>"
                    + "    <td>"
                    + header.getCARD_AMT()
                    + "    </td>"
                    + "</tr>"
                    + "<tr>"
                    + "    <td>"
                    + "        Bajaj"
                    + "    </td>"
                    + "    <td>"
                    + header.getBAJAJ_AMT()
                    + "    </td>"
                    + "</tr>";
            content += "</table></body></html>";
            SendMailSSL se = new SendMailSSL("atik22cipearl@gmail.com");
            se.sendEmailContent(content);
            se = new SendMailSSL("atul22c@gmail.com");
            se.sendEmailContent(content);
            se = new SendMailSSL("jet_viral@yahoo.co.in");
            se.sendEmailContent(content);
            se = new SendMailSSL("manzilpatel007@gmail.com");
            se.sendEmailContent(content);
//            se = new SendMailSSL("mistryj01@gmail.com");
//            se.sendEmailContent(content);
        } catch (Exception ex) {
            System.out.println("Exception at displayKIVoucher " + ex.getMessage());
        }
    }

    public void displayPurchaseVoucherEmail(PurchaseControllerHeaderModel header, ArrayList<PurcahseControllerDetailModel> detail) {

        try {
            String content = "";
            content += "<html><body>"
                    + "<label>PB</label><br/>"
                    + "<label>" + header.getAc_name() + "</label><br/>"
                    + "        <br/>"
                    + "        <br/>"
                    + "        <table border=\"1\" width=\"100%\" align=\"center\">";
            content += " <tr>"
                    + "            <td>" + 1 + "</td>"
                    + "            <td>"
                    + "                " + detail.get(0).getSR_CD() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                 " + detail.get(0).getTAG_NO() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getQTY() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getRATE() + ""
                    + "            </td>"
                    + "            <td>"
                    + "                " + detail.get(0).getAMT() + ""
                    + "            </td>"
                    + "        </tr>";
            for (int i = 1; i < detail.size(); i++) {
                content += " <tr>"
                        + "            <td>" + (i + 1) + "</td>"
                        + "            <td>"
                        + "                " + detail.get(i).getSR_CD() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                 " + detail.get(i).getTAG_NO() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getQTY() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getRATE() + ""
                        + "            </td>"
                        + "            <td>"
                        + "                " + detail.get(i).getAMT() + ""
                        + "            </td>"
                        + "        </tr>";
            }
            content += "</table></body></html>";
            SendMailSSL se = new SendMailSSL("atik22cipearl@gmail.com");
            se.sendEmailContent(content);
            se = new SendMailSSL("atul22c@gmail.com");
            se.sendEmailContent(content);
            se = new SendMailSSL("jet_viral@yahoo.co.in");
            se.sendEmailContent(content);
            se = new SendMailSSL("manzilpatel007@gmail.com");
            se.sendEmailContent(content);
        } catch (Exception ex) {
            System.out.println("Exception at displayKIVoucher " + ex.getMessage());
        }
    }

    public JsonArray getSeriesMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select SR_CD,SR_NAME,t.tax_cd,t.tax_name,SR_ALIAS,t1.type_name,b.brand_name from SERIESMST s left join modelmst m on "
                + "s.model_cd=m.model_cd left join typemst t1 on t1.type_cd=m.type_cd left join brandmst b on m.brand_cd=b.brand_cd "
                + " left join taxmst t on m.tax_cd=t.tax_cd WHERE " + field + " like '%" + value + "%'"
                + "order by sr_name";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
            object.addProperty("TYPE_NAME", rsLocal.getString("TYPE_NAME"));
            object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
            object.addProperty("STOCK", getBalanceStock(dataConnection, rsLocal.getString("SR_CD")));
            array.add(object);
        }
        sql = "select SR_CD,SR_NAME,t.tax_cd,t.tax_name,SR_ALIAS,t1.type_name,b.brand_name from SERIESMST s left join modelmst m on "
                + "s.model_cd=m.model_cd left join typemst t1 on t1.type_cd=m.type_cd left join brandmst b on m.brand_cd=b.brand_cd "
                + " left join taxmst t on m.tax_cd=t.tax_cd WHERE SR_ALIAS like '%" + value + "%'";
        pstLocal = dataConnection.prepareStatement(sql);
        rsLocal = pstLocal.executeQuery();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
            object.addProperty("TYPE_NAME", rsLocal.getString("TYPE_NAME"));
            object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
            object.addProperty("STOCK", getBalanceStock(dataConnection, rsLocal.getString("SR_CD")));
            array.add(object);
        }
        return array;
    }

    public JsonArray getLastRate(Connection dataConnection, String ac_cd, String sr_cd) throws SQLException {
        String sql = "select rate from lbrphd l left join lbrpdt l1 on l.REF_NO=l1.REF_NO where l.IS_DEL=0 and l1.sr_cd='" + sr_cd + "' "
                + " and l.ac_cd='" + ac_cd + "' order by l.ref_no desc";
        final PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        final ResultSet rsLocal = pstLocal.executeQuery();
        final JsonArray array = new JsonArray();
        final JsonObject object = new JsonObject();
        if (rsLocal.next()) {
            object.addProperty("rate", rsLocal.getString("rate"));
            array.add(object);
        } else {
            object.addProperty("rate", "0.00");
            array.add(object);
        }

        return array;
    }

    public JsonArray getLastRateMRP(Connection dataConnection, String sr_cd) throws SQLException {
        String sql = "select MRP from lbrphd l left join lbrpdt l1 on l.REF_NO=l1.REF_NO where l.IS_DEL=0 and l1.sr_cd='" + sr_cd + "' "
                + " order by l.ref_no desc";
        final PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        final ResultSet rsLocal = pstLocal.executeQuery();
        final JsonArray array = new JsonArray();
        final JsonObject object = new JsonObject();
        if (rsLocal.next()) {
            object.addProperty("rate", rsLocal.getString("MRP"));
            array.add(object);
        } else {
            object.addProperty("rate", "0.00");
            array.add(object);
        }

        return array;
    }

    public JsonArray getLastRateSales(Connection dataConnection, String ac_cd, String sr_cd) throws SQLException {
        String sql = "select rate from vilshd l left join vilsdt l1 on l.REF_NO=l1.REF_NO where l.IS_DEL=0 and l1.sr_cd='" + sr_cd + "' "
                + " and l.ac_cd='" + ac_cd + "' order by l.ref_no desc";
        final PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        final ResultSet rsLocal = pstLocal.executeQuery();
        final JsonArray array = new JsonArray();
        final JsonObject object = new JsonObject();
        if (rsLocal.next()) {
            object.addProperty("rate", rsLocal.getString("rate"));
            array.add(object);
        } else {
            object.addProperty("rate", "0.00");
            array.add(object);
        }

        return array;
    }

    public double isNumber2(String text) {
        double ans = 0.00;
        try {
            if (text != null) {
                ans = Double.parseDouble(text);
            }
        } catch (Exception ex) {
//            printToLogFile("Error at isNumber in Library", ex);
        }
        return ans;
    }

    public String getMonth(String i, String tag) {
        if (tag.equalsIgnoreCase("n")) {
            if (i.equalsIgnoreCase("1")) {
                return "January";
            } else if (i.equalsIgnoreCase("2")) {
                return "February";
            } else if (i.equalsIgnoreCase("3")) {
                return "March";
            } else if (i.equalsIgnoreCase("4")) {
                return "April";
            } else if (i.equalsIgnoreCase("5")) {
                return "May";
            } else if (i.equalsIgnoreCase("6")) {
                return "June";
            } else if (i.equalsIgnoreCase("7")) {
                return "July";
            } else if (i.equalsIgnoreCase("8")) {
                return "August";
            } else if (i.equalsIgnoreCase("9")) {
                return "September";
            } else if (i.equalsIgnoreCase("10")) {
                return "October";
            } else if (i.equalsIgnoreCase("11")) {
                return "November";
            } else if (i.equalsIgnoreCase("12")) {
                return "December";
            }
        } else if (tag.equalsIgnoreCase("c")) {
            if (i.equalsIgnoreCase("January")) {
                return "01";
            } else if (i.equalsIgnoreCase("February")) {
                return "02";
            } else if (i.equalsIgnoreCase("March")) {
                return "03";
            } else if (i.equalsIgnoreCase("April")) {
                return "04";
            } else if (i.equalsIgnoreCase("May")) {
                return "05";
            } else if (i.equalsIgnoreCase("June")) {
                return "06";
            } else if (i.equalsIgnoreCase("July")) {
                return "07";
            } else if (i.equalsIgnoreCase("August")) {
                return "08";
            } else if (i.equalsIgnoreCase("September")) {
                return "09";
            } else if (i.equalsIgnoreCase("October")) {
                return "10";
            } else if (i.equalsIgnoreCase("November")) {
                return "11";
            } else if (i.equalsIgnoreCase("December")) {
                return "12";
            }
        }
        return "";
    }

    public double getOpeningStock(Connection dataConnection, String prd_cd, String column, String fromDate) {
        double stock = 0.00;
        try {
            String sql = "select sum(" + column + ") from oldb0_2 where sr_cd='" + prd_cd + "' and (TRNS_ID='R' or TRNS_ID='O') and doc_date <'" + ConvertDateFormetForDB(fromDate) + "'";
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                stock = rsLocal.getDouble(1);
            }
            sql = "select sum(" + column + ") from oldb0_2 where sr_cd='" + prd_cd + "' and TRNS_ID='I' and doc_date <'" + ConvertDateFormetForDB(fromDate) + "'";
            pstLocal = dataConnection.prepareStatement(sql);
            rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                stock -= rsLocal.getDouble(1);
            }
            closeResultSet(rsLocal);
            closeStatement(pstLocal);
        } catch (Exception ex) {
        }
        return stock;
    }

    public String ConvertDateFormetForDB(String strOrgDate) {
        //Changed
        String strConvDate = "";
        try {
            strOrgDate = strOrgDate.trim();
            if (!strOrgDate.startsWith("/")) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                java.util.Date dt = sdf.parse(strOrgDate);
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
                strConvDate = sdf2.format(dt);
            }
        } catch (Exception ex) {
        }
        return strConvDate;
    }

    public String getField(Connection dataConnection, String table, String column, String whField, String whValue) {
        String result = null;
        PreparedStatement psLocal = null;
        String sql = null;
        try {
            if (whField == null && whValue == null) {
                sql = "SELECT " + column + " FROM " + table;
                psLocal = dataConnection.prepareStatement(sql);
            } else {
                sql = "SELECT " + column + " FROM " + table + " WHERE " + whField + "=?";
                psLocal = dataConnection.prepareStatement(sql);
                psLocal.setString(1, whValue);
            }
            ResultSet rsLocal = psLocal.executeQuery();
            if (rsLocal.next()) {
                result = rsLocal.getString(1);
            }

        } catch (Exception ex) {
        }
        return result;
    }

    public String getField(String query, Connection con) {
        String data = null;
        PreparedStatement psLocal = null;
        ResultSet rsLocal = null;
        try {
            psLocal = con.prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rsLocal = psLocal.executeQuery();
            if (rsLocal.next()) {
                data = rsLocal.getString(1);
            }
        } catch (Exception ex) {
        } finally {
            closeResultSet(rsLocal);
            closeStatement(psLocal);
        }
        return data;
    }

    public JsonArray getTagNo(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "select TAG_NO from LBRPDT where REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getTagNoDCI(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "select TAG_NO from DCDT where REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getTagNoDetail(Connection dataConnection, String tagListForRandom, boolean flag) throws SQLException {
        String sql = "Select TAG_NO,s.sr_alias as SR_NAME,MRP,NLC from tag t LEFT JOIN SERIESMST  "
                + "s on t.SR_CD=s.SR_CD WHERE t.tag_no in (" + tagListForRandom + ")";
        if (flag) {
            sql += " and t.is_del=0";
        }
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("RATE", rsLocal.getString("MRP"));
            object.addProperty("NLC", rsLocal.getString("NLC"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getTagNoDetailSales(Connection dataConnection, String tagListForRandom, boolean flag) throws SQLException {
        String sql = "Select TAG_NO,s.sr_alias as SR_NAME,SR_NAME as ITEM_NAME,t.ref_no,s.sr_cd,t1.tax_cd"
                + ",t1.tax_name,t.IMEI_NO,t.SERAIL_NO,t.pur_rate,t.is_main "
                + " from tag t LEFT JOIN SERIESMST s on t.SR_CD=s.SR_CD left join modelmst m on "
                + " s.model_cd=m.model_cd left join taxmst t1 on m.tax_cd=t1.tax_cd "
                + " WHERE t.tag_no in (" + tagListForRandom + ")";
        if (flag) {
            sql += " and t.is_del=0";
        } else {
            sql += " and t.is_del=1";
        }
        sql += " order by t.is_main,t.PUR_REF_NO desc";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("ITEM_NAME", rsLocal.getString("ITEM_NAME"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("PUR_RATE", rsLocal.getDouble("PUR_RATE"));
            object.addProperty("IS_MAIN", rsLocal.getDouble("IS_MAIN"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getAccountmaster(Connection dataConnection, String card_no) throws SQLException {
        String sql = "select a.AC_CD,FNAME,CARD_NO,TIN,a1.ADD1,p.MOBILE1,a.REF_BY from acntmst a left join adbkmst a1 on a.AC_CD=a1.AC_CD "
                + "left join phbkmst p on a.AC_CD=p.AC_CD where CARD_NO ='" + card_no + "'";

        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("CARD_NO", rsLocal.getString("CARD_NO"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("REF_BY", rsLocal.getString("REF_BY"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getAccountmasterMobile(Connection dataConnection, String card_no) throws SQLException {
        String sql = "select a.AC_CD,FNAME,CARD_NO,TIN,a1.ADD1,p.MOBILE1,a.REF_BY from acntmst a left join adbkmst a1 on a.AC_CD=a1.AC_CD "
                + "left join phbkmst p on a.AC_CD=p.AC_CD where MOBILE1 ='" + card_no + "'";

        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("CARD_NO", rsLocal.getString("CARD_NO"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("REF_BY", rsLocal.getString("REF_BY"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getTagNoDetailSales(Connection dataConnection, String tagListForRandom, boolean flag, String branch, String loc) throws SQLException {
        String sql = "Select TAG_NO,s.sr_alias as SR_NAME,SR_NAME as ITEM_NAME,t.ref_no,s.sr_cd,t1.tax_cd"
                + ",t1.tax_name,t.IMEI_NO,t.SERAIL_NO,t.is_main,t.PUR_RATE,t.MRP "
                + " from tag t LEFT JOIN SERIESMST s on t.SR_CD=s.SR_CD left join modelmst m on "
                + " s.model_cd=m.model_cd left join taxmst t1 on m.tax_cd=t1.tax_cd "
                + " WHERE t.tag_no in (" + tagListForRandom + ") and t.branch_cd =" + branch;
        if (loc != null) {
            sql += " and godown=" + loc;
        }
        if (flag) {
            sql += " and t.is_del=0";
        } else {
            sql += " and t.is_del=1";
        }
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_NAME", rsLocal.getString("ITEM_NAME"));
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("ITEM_NAME", rsLocal.getString("ITEM_NAME"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("IS_MAIN", rsLocal.getString("is_main"));
            object.addProperty("PUR_RATE", rsLocal.getString("PUR_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getOldStock(Connection dataConnection, String tagListForRandom, String is_del, String loc) throws SQLException {
        String sql = "SELECT TAG_NO,case when l.v_date is null then PUR_DATE else l.V_DATE end as PUR_DATE,IMEI_NO,SERAIL_NO,s.SR_NAME FROM tag t LEFT JOIN lbrphd l ON "
                + " t.PUR_REF_NO=l.REF_NO left join seriesmst s on t.sr_cd=s.sr_cd "
                + " WHERE t.sr_cd =(SELECT sr_cd FROM tag  WHERE tag_no in (" + tagListForRandom + ") AND is_del=0 and is_main=1) "
                + " AND t.is_del=0 AND tag_no not in (" + tagListForRandom + ") AND (l.V_DATE <(SELECT CASE WHEN l.v_date IS NULL THEN t.PUR_DATE"
                + " ELSE l.v_date END AS pur_date FROM tag t  LEFT JOIN lbrphd l ON t.PUR_REF_NO=l.REF_NO WHERE tag_no in (" + tagListForRandom + ") "
                + " AND t.is_del=0 and t.is_main=1) OR (pur_date IS NOT NULL AND"
                + " pur_date < (SELECT CASE WHEN l.v_date IS NULL THEN t.PUR_DATE ELSE l.v_date END AS pur_date FROM tag t "
                + " LEFT JOIN lbrphd l ON t.PUR_REF_NO=l.REF_NO  "
                + " WHERE tag_no in (" + tagListForRandom + ") AND t.is_del=0 and t.is_main=1))) and t.branch_cd=" + loc;
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("PUR_DATE", rsLocal.getString("PUR_DATE"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            array.add(object);
        }

        return array;
    }

    public JsonArray getTaxMaster(Connection dataConnection) throws SQLException {
        String sql = "select TAX_CD,TAX_NAME,TAX_PER,ADD_TAX_PER,TAX_ON_SALES,TAX_AC_CD,ADD_TAX_AC_CD from TAXMST";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_PER", rsLocal.getString("TAX_PER"));
            object.addProperty("ADD_TAX_PER", rsLocal.getString("ADD_TAX_PER"));
            object.addProperty("TAX_ON_SALES", rsLocal.getString("TAX_ON_SALES"));
            object.addProperty("TAX_AC_CD", rsLocal.getString("TAX_AC_CD"));
            object.addProperty("ADD_TAX_AC_CD", rsLocal.getString("TAX_AC_CD"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getPurchaseBill(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "select l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.V_TYPE,l.PMT_MODE,l.BILL_DATE,l.BILL_NO,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DET_TOT,"
                + " a.TIN,l1.TAX_CD,l1.TAX_AMT,l1.ADD_TAX_AMT,t.TAX_NAME,t1.is_del as TAG_DEL,FR_CHG,l1.BASIC_AMT,l.DUE_DATE,l.BRANCH_CD,l1.IS_MAIN,"
                + " l.ADJST,l.NET_AMT,l.IS_DEL,l.EDIT_NO,l.USER_ID,l.TIME_STAMP,l1.SR_NO,l1.TAG_NO,s.SR_NAME,l1.IMEI_NO,l1.SERAIL_NO,l1.QTY,l1.RATE,"
                + " l1.AMT,l1.PUR_TAG_NO,l1.sr_cd,l1.disc_rate,l1.mrp,l1.NLC,sc.scheme_name"
                + "  from LBRPHD l left join lbrpdt l1 on l.REF_NO=l1.REF_NO left join SERIESMST s on s.SR_CD=l1.SR_CD "
                + " left join acntmst a on l.ac_cd=a.ac_cd left join adbkmst a1 on a.ac_cd=a1.ac_cd left join phbkmst p on a.ac_cd=p.ac_cd "
                + " left join taxmst t on l1.tax_cd=t.tax_cd left join tag t1 on l1.PUR_TAG_NO=t1.REF_NO"
                + " left join schememst sc on l.scheme_cd=sc.scheme_cd"
                + " where l.REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("TAX_TYPE",0);
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("V_TYPE", rsLocal.getInt("V_TYPE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("PMT_MODE", rsLocal.getInt("PMT_MODE"));
            object.addProperty("BILL_DATE", rsLocal.getString("BILL_DATE"));
            object.addProperty("BILL_NO", rsLocal.getString("BILL_NO"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_AMT", rsLocal.getDouble("TAX_AMT"));
            object.addProperty("ADD_TAX_AMT", rsLocal.getDouble("ADD_TAX_AMT"));
            object.addProperty("ADJST", rsLocal.getDouble("ADJST"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("FR_CHG", rsLocal.getDouble("FR_CHG"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("BASIC_AMT", rsLocal.getString("BASIC_AMT"));
            object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
            object.addProperty("TAG_DEL", rsLocal.getString("TAG_DEL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            object.addProperty("NLC", rsLocal.getString("NLC"));
            object.addProperty("SCHEME_NAME", rsLocal.getString("scheme_name"));
            object.addProperty("IS_MAIN", rsLocal.getInt("IS_MAIN"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getQuoteBill(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "select l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DUE_DATE,l.BRANCH_CD,l.NET_AMT,l.IS_DEL"
                + ",l.EDIT_NO,l.USER_ID,l.TIME_STAMP,l1.SR_NO,s.SR_NAME,s.SR_ALIAS,l1.QTY,l1.RATE,"
                + " l1.AMOUNT,l1.sr_cd,l1.disc_per,l1.mrp from quotationhd l left join quotationdt l1 on l.REF_NO=l1.REF_NO left join SERIESMST s on s.SR_CD=l1.SR_CD "
                + " left join acntmst a on l.ac_cd=a.ac_cd left join adbkmst a1 on a.ac_cd=a1.ac_cd left join phbkmst p on a.ac_cd=p.ac_cd "
                + " where l.REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("SR_ALIAS", rsLocal.getString("SR_ALIAS"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("AMT", rsLocal.getString("AMOUNT"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_PER"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getSalesBill(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "SELECT l.bank_charges,l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.V_TYPE,l.PMT_MODE,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DET_TOT, a.TIN,l1.TAX_CD,l1.TAX_AMT,"
                + " l1.ADD_TAX_AMT,t.TAX_NAME,t1.is_del AS TAG_DEL,t1.MRP as MRP1,l1.BASIC_AMT,l.DUE_DATE,l.BRANCH_CD,l1.IS_MAIN, l.ADJST,l.NET_AMT,l.IS_DEL,l.EDIT_NO,"
                + " l.USER_ID,l.TIME_STAMP,l1.SR_NO,l1.TAG_NO,s.SR_NAME,l1.IMEI_NO,l1.SERAIL_NO,l1.QTY,l1.RATE, l1.AMT,l1.PUR_TAG_NO,l1.sr_cd,l1.disc_rate,"
                + " l1.mrp,pmt.BANK_AMT,pmt.CHEQUE_NO,pmt.CHEQUE_DATE,card.FNAME AS CARD_NAME,pmt.CARD_AMT, bajaj.FNAME AS BAJAJ_NAME,pmt.BAJAJ_AMT,pmt.SFID,"
                + " pmt.BANK_CD,pmt.CARD_NAME AS CARD_CD,pmt.BAJAJ_NAME AS BAJAJ_CD, pmt.CASH_AMT,bank.FNAME AS OUR_BANK,pmt.BANK_NAME,pmt.BANK_BRANCH ,"
                + " s1.SR_CD as BUY_BACK_CD,s1.SR_NAME AS BUY_BACK_MODEL,l.BUY_BACK_AMT,l.BUY_BACK_IMEI_NO,l.PART_NO,s2.SR_CD as INS_CD,"
                + " s2.SR_NAME AS INS_MODEL,l.INS_AMT,l.PMT_DAYS,l.BANK_CHARGES, l.ADVANCE_AMT,l.DISCOUNT,t1.pur_rate,a.ref_by,r.REF_NAME,pmt.BAJAJ_PER"
                + ",pmt.BAJAJ_CHG,pmt.CARD_PER,pmt.CARD_CHG,sm.SM_NAME,sc.scheme_name,pmt.card_no,pmt.tid_no,l.add_sr_no "
                + " FROM vilshd l LEFT JOIN vilsdt l1 ON l.REF_NO=l1.REF_NO LEFT JOIN SERIESMST s ON s.SR_CD=l1.SR_CD  LEFT JOIN acntmst a ON l.ac_cd=a.ac_cd "
                + " LEFT JOIN adbkmst a1 ON a.ac_cd=a1.ac_cd LEFT JOIN phbkmst p ON a.ac_cd=p.ac_cd  LEFT JOIN taxmst t ON l1.tax_cd=t.tax_cd "
                + " LEFT JOIN tag t1 ON l1.PUR_TAG_NO=t1.REF_NO LEFT JOIN PAYMENT pmt ON l.REF_NO=pmt.REF_NO LEFT JOIN ACNTMST bank ON pmt.BANK_CD=bank.AC_CD "
                + " LEFT JOIN ACNTMST card ON pmt.CARD_NAME=card.AC_CD LEFT JOIN seriesmst s1 ON l.BUY_BACK_MODEL=s1.SR_CD "
                + " LEFT JOIN seriesmst s2 ON l.INS_CD=s2.SR_CD left join refmst r on r.ref_cd=l.ref_cd left join smmst sm on sm.sm_cd=l.sm_cd "
                + " LEFT JOIN ACNTMST bajaj ON pmt.BAJAJ_NAME=bajaj.AC_CD left join schememst sc on l.scheme_cd=sc.scheme_cd"
                + " WHERE l.REF_NO='" + ref_no + "' and a1.sr_no=l.add_sr_no";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("V_TYPE", rsLocal.getInt("V_TYPE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("PMT_MODE", rsLocal.getInt("PMT_MODE"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ref_by", rsLocal.getString("ref_by"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_AMT", rsLocal.getDouble("TAX_AMT"));
            object.addProperty("ADD_TAX_AMT", rsLocal.getDouble("ADD_TAX_AMT"));
            object.addProperty("ADJST", rsLocal.getDouble("ADJST"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("CASH_AMT", rsLocal.getString("CASH_AMT"));
            object.addProperty("OUR_BANK", rsLocal.getString("OUR_BANK"));
            object.addProperty("DISCOUNT", rsLocal.getDouble("DISCOUNT"));
            object.addProperty("MRP1", rsLocal.getDouble("MRP1"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("BANK_BRANCH", rsLocal.getString("BANK_BRANCH"));
            object.addProperty("BANK_AMT", rsLocal.getString("BANK_AMT"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("CARD_NAME", rsLocal.getString("CARD_NAME"));
            object.addProperty("CARD_AMT", rsLocal.getString("CARD_AMT"));
            object.addProperty("BAJAJ_NAME", rsLocal.getString("BAJAJ_NAME"));
            object.addProperty("BAJAJ_AMT", rsLocal.getString("BAJAJ_AMT"));
            object.addProperty("BAJAJ_PER", rsLocal.getString("BAJAJ_PER"));
            object.addProperty("BAJAJ_CHG", rsLocal.getString("BAJAJ_CHG"));
            object.addProperty("CARD_PER", rsLocal.getString("CARD_PER"));
            object.addProperty("CARD_CHG", rsLocal.getString("CARD_CHG"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("CARD_CD", rsLocal.getString("CARD_CD"));
            object.addProperty("BAJAJ_CD", rsLocal.getString("BAJAJ_CD"));
            object.addProperty("SFID", rsLocal.getString("SFID"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("BASIC_AMT", rsLocal.getString("BASIC_AMT"));
            object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
            object.addProperty("TAG_DEL", rsLocal.getString("TAG_DEL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("TAX_TYPE", "0");
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            object.addProperty("IS_MAIN", rsLocal.getInt("IS_MAIN"));
            object.addProperty("BUY_BACK_CD", rsLocal.getString("BUY_BACK_CD"));
            object.addProperty("CARD_NO", rsLocal.getString("CARD_NO"));
            object.addProperty("TID_NO", rsLocal.getString("TID_NO"));
            object.addProperty("BUY_BACK_MODEL", rsLocal.getString("BUY_BACK_MODEL"));
            object.addProperty("BUY_BACK_AMT", rsLocal.getString("BUY_BACK_AMT"));
            object.addProperty("BUY_BACK_IMEI_NO", rsLocal.getString("BUY_BACK_IMEI_NO"));
            object.addProperty("PART_NO", rsLocal.getString("PART_NO"));
            object.addProperty("INS_CD", rsLocal.getString("INS_CD"));
            object.addProperty("INS_MODEL", rsLocal.getString("INS_MODEL"));
            object.addProperty("INS_AMT", rsLocal.getString("INS_AMT"));
            object.addProperty("PMT_DAYS", rsLocal.getString("PMT_DAYS"));
            object.addProperty("add_sr_no", rsLocal.getString("add_sr_no"));
            object.addProperty("BANK_CHARGES", rsLocal.getString("BANK_CHARGES"));
            object.addProperty("ADVANCE_AMT", rsLocal.getString("ADVANCE_AMT"));
            object.addProperty("PUR_RATE", rsLocal.getString("PUR_RATE"));
            object.addProperty("REF_NAME", (rsLocal.getString("REF_NAME") == null) ? "" : rsLocal.getString("REF_NAME"));
            object.addProperty("SM_NAME", (rsLocal.getString("SM_NAME") == null) ? "" : rsLocal.getString("SM_NAME"));
            object.addProperty("SCHEME_NAME", (rsLocal.getString("SCHEME_NAME") == null) ? "" : rsLocal.getString("SCHEME_NAME"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getSalesBillOLD(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "SELECT l.bank_charges,l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.V_TYPE,l.PMT_MODE,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DET_TOT, a.TIN,l1.TAX_CD,l1.TAX_AMT,"
                + " l1.ADD_TAX_AMT,t.TAX_NAME,t1.is_del AS TAG_DEL,t1.MRP as MRP1,l1.BASIC_AMT,l.DUE_DATE,l.BRANCH_CD,l1.IS_MAIN, l.ADJST,l.NET_AMT,l.IS_DEL,l.EDIT_NO,"
                + " l.USER_ID,l.TIME_STAMP,l1.SR_NO,l1.TAG_NO,s.SR_NAME,l1.IMEI_NO,l1.SERAIL_NO,l1.QTY,l1.RATE, l1.AMT,l1.PUR_TAG_NO,l1.sr_cd,l1.disc_rate,"
                + " l1.mrp,pmt.BANK_AMT,pmt.CHEQUE_NO,pmt.CHEQUE_DATE,card.FNAME AS CARD_NAME,pmt.CARD_AMT, bajaj.FNAME AS BAJAJ_NAME,pmt.BAJAJ_AMT,pmt.SFID,"
                + " pmt.BANK_CD,pmt.CARD_NAME AS CARD_CD,pmt.BAJAJ_NAME AS BAJAJ_CD, pmt.CASH_AMT,bank.FNAME AS OUR_BANK,pmt.BANK_NAME,pmt.BANK_BRANCH ,"
                + " s1.SR_CD as BUY_BACK_CD,s1.SR_NAME AS BUY_BACK_MODEL,l.BUY_BACK_AMT,l.BUY_BACK_IMEI_NO,l.PART_NO,s2.SR_CD as INS_CD,"
                + " s2.SR_NAME AS INS_MODEL,l.INS_AMT,l.PMT_DAYS,l.BANK_CHARGES, l.ADVANCE_AMT,l.DISCOUNT,t1.pur_rate,a.ref_by,r.REF_NAME,pmt.BAJAJ_PER"
                + ",pmt.BAJAJ_CHG,pmt.CARD_PER,pmt.CARD_CHG,sm.SM_NAME,sc.scheme_name,pmt.card_no,pmt.tid_no,l.add_sr_no "
                + " FROM vilshd l LEFT JOIN vilsdt l1 ON l.REF_NO=l1.REF_NO LEFT JOIN SERIESMST s ON s.SR_CD=l1.SR_CD  LEFT JOIN acntmst a ON l.ac_cd=a.ac_cd "
                + " LEFT JOIN adbkmst a1 ON a.ac_cd=a1.ac_cd LEFT JOIN phbkmst p ON a.ac_cd=p.ac_cd  LEFT JOIN taxmst t ON l1.tax_cd=t.tax_cd "
                + " LEFT JOIN tag t1 ON l1.PUR_TAG_NO=t1.REF_NO LEFT JOIN PAYMENT pmt ON l.REF_NO=pmt.REF_NO LEFT JOIN ACNTMST bank ON pmt.BANK_CD=bank.AC_CD "
                + " LEFT JOIN ACNTMST card ON pmt.CARD_NAME=card.AC_CD LEFT JOIN seriesmst s1 ON l.BUY_BACK_MODEL=s1.SR_CD "
                + " LEFT JOIN seriesmst s2 ON l.INS_CD=s2.SR_CD left join refmst r on r.ref_cd=l.ref_cd left join smmst sm on sm.sm_cd=l.sm_cd "
                + " LEFT JOIN ACNTMST bajaj ON pmt.BAJAJ_NAME=bajaj.AC_CD left join schememst sc on l.scheme_cd=sc.scheme_cd"
                + " WHERE l.REF_NO='" + ref_no + "' and a1.sr_no=l.add_sr_no";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("V_TYPE", rsLocal.getInt("V_TYPE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("PMT_MODE", rsLocal.getInt("PMT_MODE"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ref_by", rsLocal.getString("ref_by"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_AMT", rsLocal.getDouble("TAX_AMT"));
            object.addProperty("ADD_TAX_AMT", rsLocal.getDouble("ADD_TAX_AMT"));
            object.addProperty("ADJST", rsLocal.getDouble("ADJST"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("CASH_AMT", rsLocal.getString("CASH_AMT"));
            object.addProperty("OUR_BANK", rsLocal.getString("OUR_BANK"));
            object.addProperty("DISCOUNT", rsLocal.getDouble("DISCOUNT"));
            object.addProperty("MRP1", rsLocal.getDouble("MRP1"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("BANK_BRANCH", rsLocal.getString("BANK_BRANCH"));
            object.addProperty("BANK_AMT", rsLocal.getString("BANK_AMT"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("CARD_NAME", rsLocal.getString("CARD_NAME"));
            object.addProperty("CARD_AMT", rsLocal.getString("CARD_AMT"));
            object.addProperty("BAJAJ_NAME", rsLocal.getString("BAJAJ_NAME"));
            object.addProperty("BAJAJ_AMT", rsLocal.getString("BAJAJ_AMT"));
            object.addProperty("BAJAJ_PER", rsLocal.getString("BAJAJ_PER"));
            object.addProperty("BAJAJ_CHG", rsLocal.getString("BAJAJ_CHG"));
            object.addProperty("CARD_PER", rsLocal.getString("CARD_PER"));
            object.addProperty("CARD_CHG", rsLocal.getString("CARD_CHG"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("CARD_CD", rsLocal.getString("CARD_CD"));
            object.addProperty("BAJAJ_CD", rsLocal.getString("BAJAJ_CD"));
            object.addProperty("SFID", rsLocal.getString("SFID"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("BASIC_AMT", rsLocal.getString("BASIC_AMT"));
            object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
            object.addProperty("TAG_DEL", rsLocal.getString("TAG_DEL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            object.addProperty("IS_MAIN", rsLocal.getInt("IS_MAIN"));
            object.addProperty("BUY_BACK_CD", rsLocal.getString("BUY_BACK_CD"));
            object.addProperty("CARD_NO", rsLocal.getString("CARD_NO"));
            object.addProperty("TID_NO", rsLocal.getString("TID_NO"));
            object.addProperty("BUY_BACK_MODEL", rsLocal.getString("BUY_BACK_MODEL"));
            object.addProperty("BUY_BACK_AMT", rsLocal.getString("BUY_BACK_AMT"));
            object.addProperty("BUY_BACK_IMEI_NO", rsLocal.getString("BUY_BACK_IMEI_NO"));
            object.addProperty("PART_NO", rsLocal.getString("PART_NO"));
            object.addProperty("INS_CD", rsLocal.getString("INS_CD"));
            object.addProperty("INS_MODEL", rsLocal.getString("INS_MODEL"));
            object.addProperty("INS_AMT", rsLocal.getString("INS_AMT"));
            object.addProperty("PMT_DAYS", rsLocal.getString("PMT_DAYS"));
            object.addProperty("add_sr_no", rsLocal.getString("add_sr_no"));
            object.addProperty("BANK_CHARGES", rsLocal.getString("BANK_CHARGES"));
            object.addProperty("ADVANCE_AMT", rsLocal.getString("ADVANCE_AMT"));
            object.addProperty("PUR_RATE", rsLocal.getString("PUR_RATE"));
            object.addProperty("REF_NAME", (rsLocal.getString("REF_NAME") == null) ? "" : rsLocal.getString("REF_NAME"));
            object.addProperty("SM_NAME", (rsLocal.getString("SM_NAME") == null) ? "" : rsLocal.getString("SM_NAME"));
            object.addProperty("SCHEME_NAME", (rsLocal.getString("SCHEME_NAME") == null) ? "" : rsLocal.getString("SCHEME_NAME"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getSalesReturnBill(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "SELECT l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.PMT_MODE,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DET_TOT, a.TIN,l1.TAX_CD,l1.TAX_AMT,"
                + " l1.ADD_TAX_AMT,t.TAX_NAME,t1.is_del AS TAG_DEL,l1.BASIC_AMT,l.DUE_DATE,l.BRANCH_CD,l1.IS_MAIN, l.ADJST,l.NET_AMT,l.IS_DEL,l.EDIT_NO,"
                + " l.USER_ID,l.TIME_STAMP,l1.SR_NO,l1.TAG_NO,s.SR_NAME,l1.IMEI_NO,l1.SERAIL_NO,l1.QTY,l1.RATE, l1.AMT,l1.PUR_TAG_NO,l1.sr_cd,l1.disc_rate,"
                + " t1.mrp,pmt.BANK_AMT,pmt.CHEQUE_NO,pmt.CHEQUE_DATE,card.FNAME AS CARD_NAME,pmt.CARD_AMT, bajaj.FNAME AS BAJAJ_NAME,pmt.BAJAJ_AMT,pmt.SFID,"
                + " pmt.BANK_CD,pmt.CARD_NAME AS CARD_CD,pmt.BAJAJ_NAME AS BAJAJ_CD, pmt.CASH_AMT,bank.FNAME AS OUR_BANK,pmt.BANK_NAME,pmt.BANK_BRANCH "
                + ",l.PMT_DAYS,l.ADVANCE_AMT FROM SRHD l LEFT JOIN SRDT l1 ON l.REF_NO=l1.REF_NO LEFT JOIN SERIESMST s ON s.SR_CD=l1.SR_CD  LEFT JOIN acntmst a ON l.ac_cd=a.ac_cd "
                + " LEFT JOIN adbkmst a1 ON a.ac_cd=a1.ac_cd LEFT JOIN phbkmst p ON a.ac_cd=p.ac_cd  LEFT JOIN taxmst t ON l1.tax_cd=t.tax_cd "
                + " LEFT JOIN tag t1 ON l1.PUR_TAG_NO=t1.REF_NO LEFT JOIN PAYMENT pmt ON l.REF_NO=pmt.REF_NO LEFT JOIN ACNTMST bank ON pmt.BANK_CD=bank.AC_CD "
                + " LEFT JOIN ACNTMST card ON pmt.CARD_NAME=card.AC_CD LEFT JOIN ACNTMST bajaj ON pmt.BAJAJ_NAME=bajaj.AC_CD "
                + " WHERE l.REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("PMT_MODE", rsLocal.getInt("PMT_MODE"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_AMT", rsLocal.getDouble("TAX_AMT"));
            object.addProperty("ADD_TAX_AMT", rsLocal.getDouble("ADD_TAX_AMT"));
            object.addProperty("ADJST", rsLocal.getDouble("ADJST"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("CASH_AMT", rsLocal.getString("CASH_AMT"));
            object.addProperty("OUR_BANK", rsLocal.getString("OUR_BANK"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("BANK_BRANCH", rsLocal.getString("BANK_BRANCH"));
            object.addProperty("BANK_AMT", rsLocal.getString("BANK_AMT"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("CARD_NAME", rsLocal.getString("CARD_NAME"));
            object.addProperty("CARD_AMT", rsLocal.getString("CARD_AMT"));
            object.addProperty("BAJAJ_NAME", rsLocal.getString("BAJAJ_NAME"));
            object.addProperty("BAJAJ_AMT", rsLocal.getString("BAJAJ_AMT"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("CARD_CD", rsLocal.getString("CARD_CD"));
            object.addProperty("BAJAJ_CD", rsLocal.getString("BAJAJ_CD"));
            object.addProperty("SFID", rsLocal.getString("SFID"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("BASIC_AMT", rsLocal.getString("BASIC_AMT"));
            object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
            object.addProperty("TAG_DEL", rsLocal.getString("TAG_DEL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            object.addProperty("IS_MAIN", rsLocal.getInt("IS_MAIN"));
            object.addProperty("PMT_DAYS", rsLocal.getString("PMT_DAYS"));
            object.addProperty("ADVANCE_AMT", rsLocal.getString("ADVANCE_AMT"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getoldb2_4(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "SELECT DOC_REF_NO,INV_NO,DOC_CD,DOC_DATE,UNPAID_AMT,DUE_DATE FROM oldb2_4 WHERE ac_cd='" + ac_cd + "' AND UNPAID_AMT <0";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("DOC_DATE", rsLocal.getString("DOC_DATE"));
            object.addProperty("UNPAID_AMT", rsLocal.getString("UNPAID_AMT"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            if (isDeleted(dataConnection, rsLocal.getString("DOC_REF_NO"))) {
                array.add(object);
            }
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getoldb2_3(Connection dataConnection, String ac_cd) throws SQLException {
        String sql = "SELECT DOC_REF_NO,INV_NO,DOC_CD,DOC_DATE,UNPAID_AMT,DUE_DATE FROM oldb2_4 WHERE ac_cd='" + ac_cd + "' AND UNPAID_AMT >0";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("DOC_DATE", rsLocal.getString("DOC_DATE"));
            object.addProperty("UNPAID_AMT", rsLocal.getString("UNPAID_AMT"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            if (isDeleted(dataConnection, rsLocal.getString("DOC_REF_NO"))) {
                array.add(object);
            }
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getPurchaseReturnBill(Connection dataConnection, String ref_no) throws SQLException {
        String sql = "SELECT l.remark,l.REF_NO,l.INV_NO,l.V_DATE,l.PMT_MODE,l.AC_CD,a.FNAME,a1.ADD1,p.MOBILE1,l.DET_TOT, a.TIN,l1.TAX_CD,l1.TAX_AMT,"
                + " l1.ADD_TAX_AMT,t.TAX_NAME,t1.is_del AS TAG_DEL,l1.BASIC_AMT,l.DUE_DATE,l.BRANCH_CD,l1.IS_MAIN, l.ADJST,l.NET_AMT,l.IS_DEL,l.EDIT_NO,"
                + " l.USER_ID,l.TIME_STAMP,l1.SR_NO,l1.TAG_NO,s.SR_NAME,l1.IMEI_NO,l1.SERAIL_NO,l1.QTY,l1.RATE, l1.AMT,l1.PUR_TAG_NO,l1.sr_cd,l1.disc_rate,"
                + " t1.mrp,pmt.BANK_AMT,pmt.CHEQUE_NO,pmt.CHEQUE_DATE,card.FNAME AS CARD_NAME,pmt.CARD_AMT, bajaj.FNAME AS BAJAJ_NAME,pmt.BAJAJ_AMT,pmt.SFID,"
                + " pmt.BANK_CD,pmt.CARD_NAME AS CARD_CD,pmt.BAJAJ_NAME AS BAJAJ_CD, pmt.CASH_AMT,bank.FNAME AS OUR_BANK,pmt.BANK_NAME,pmt.BANK_BRANCH "
                + ",l.PMT_DAYS,l.ADVANCE_AMT FROM PRHD l LEFT JOIN PRDT l1 ON l.REF_NO=l1.REF_NO LEFT JOIN SERIESMST s ON s.SR_CD=l1.SR_CD  LEFT JOIN acntmst a ON l.ac_cd=a.ac_cd "
                + " LEFT JOIN adbkmst a1 ON a.ac_cd=a1.ac_cd LEFT JOIN phbkmst p ON a.ac_cd=p.ac_cd  LEFT JOIN taxmst t ON l1.tax_cd=t.tax_cd "
                + " LEFT JOIN tag t1 ON l1.PUR_TAG_NO=t1.REF_NO LEFT JOIN PAYMENT pmt ON l.REF_NO=pmt.REF_NO LEFT JOIN ACNTMST bank ON pmt.BANK_CD=bank.AC_CD "
                + " LEFT JOIN ACNTMST card ON pmt.CARD_NAME=card.AC_CD LEFT JOIN ACNTMST bajaj ON pmt.BAJAJ_NAME=bajaj.AC_CD "
                + " WHERE l.REF_NO='" + ref_no + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("INV_NO", rsLocal.getInt("INV_NO"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("DUE_DATE", rsLocal.getString("DUE_DATE"));
            object.addProperty("BRANCH_CD", rsLocal.getInt("BRANCH_CD"));
            object.addProperty("PMT_MODE", rsLocal.getInt("PMT_MODE"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("ADD1", rsLocal.getString("ADD1"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            object.addProperty("DET_TOT", rsLocal.getDouble("DET_TOT"));
            object.addProperty("TAX_CD", rsLocal.getString("TAX_CD"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            object.addProperty("TAX_AMT", rsLocal.getDouble("TAX_AMT"));
            object.addProperty("ADD_TAX_AMT", rsLocal.getDouble("ADD_TAX_AMT"));
            object.addProperty("ADJST", rsLocal.getDouble("ADJST"));
            object.addProperty("NET_AMT", rsLocal.getDouble("NET_AMT"));
            object.addProperty("IS_DEL", rsLocal.getInt("IS_DEL"));
            object.addProperty("EDIT_NO", rsLocal.getInt("EDIT_NO"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("TAG_NO", rsLocal.getString("TAG_NO"));
            object.addProperty("SR_CD", rsLocal.getString("SR_CD"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getInt("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("CASH_AMT", rsLocal.getString("CASH_AMT"));
            object.addProperty("OUR_BANK", rsLocal.getString("OUR_BANK"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("BANK_BRANCH", rsLocal.getString("BANK_BRANCH"));
            object.addProperty("BANK_AMT", rsLocal.getString("BANK_AMT"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("CARD_NAME", rsLocal.getString("CARD_NAME"));
            object.addProperty("CARD_AMT", rsLocal.getString("CARD_AMT"));
            object.addProperty("BAJAJ_NAME", rsLocal.getString("BAJAJ_NAME"));
            object.addProperty("BAJAJ_AMT", rsLocal.getString("BAJAJ_AMT"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("CARD_CD", rsLocal.getString("CARD_CD"));
            object.addProperty("BAJAJ_CD", rsLocal.getString("BAJAJ_CD"));
            object.addProperty("SFID", rsLocal.getString("SFID"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("BASIC_AMT", rsLocal.getString("BASIC_AMT"));
            object.addProperty("PUR_TAG_NO", rsLocal.getString("PUR_TAG_NO"));
            object.addProperty("TAG_DEL", rsLocal.getString("TAG_DEL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("DISC_RATE", rsLocal.getString("DISC_RATE"));
            object.addProperty("MRP", rsLocal.getString("MRP"));
            object.addProperty("IS_MAIN", rsLocal.getInt("IS_MAIN"));
            object.addProperty("PMT_DAYS", rsLocal.getString("PMT_DAYS"));
            object.addProperty("ADVANCE_AMT", rsLocal.getString("ADVANCE_AMT"));
            array.add(object);
        }
        closeResultSet(rsLocal);
        closeStatement(pstLocal);
        closeConnection(dataConnection);
        return array;
    }

    public JsonArray getBrandmaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select BRAND_CD,BRAND_NAME from BRANDMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("BRAND_CD", rsLocal.getString("BRAND_CD"));
            object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getModelMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select MODEL_CD,MODEL_NAME,BRAND_NAME,t.TYPE_NAME,t1.TYPE_NAME as sub_type_name,t3.tax_name  from MODELMST m "
                + " left join BRANDMST b on m.brand_cd=b.brand_cd left join typemst t on m.type_cd=t.type_cd "
                + " left join typemst t1 on t1.type_cd=m.sub_type_cd left join taxmst t3 on t3.tax_cd=m.tax_cd"
                + " WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("MODEL_CD", rsLocal.getString("MODEL_CD"));
            object.addProperty("MODEL_NAME", rsLocal.getString("MODEL_NAME"));
            object.addProperty("BRAND_NAME", rsLocal.getString("BRAND_NAME"));
            object.addProperty("TYPE_NAME", rsLocal.getString("TYPE_NAME"));
            object.addProperty("SUB_TYPE_NAME", rsLocal.getString("SUB_TYPE_NAME"));
            object.addProperty("TAX_NAME", rsLocal.getString("TAX_NAME"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getMemoryMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select MEMORY_CD,MEMORY_NAME from MEMORYMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("MEMORY_CD", rsLocal.getString("MEMORY_CD"));
            object.addProperty("MEMORY_NAME", rsLocal.getString("MEMORY_NAME"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getColorMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select COLOUR_CD,COLOUR_NAME from COLOURMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("COLOUR_CD", rsLocal.getString("COLOUR_CD"));
            object.addProperty("COLOUR_NAME", rsLocal.getString("COLOUR_NAME"));
            array.add(object);
        }
        return array;
    }
    
    public JsonArray getRamMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select RAM_CD,RAM_NAME from RAMMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("RAM_CD", rsLocal.getString("RAM_CD"));
            object.addProperty("RAM_NAME", rsLocal.getString("RAM_NAME"));
            array.add(object);
        }
        return array;
    }
    
    public JsonArray getCameraMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select CAMERA_CD,CAMERA_NAME from cameramst WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("CAMERA_CD", rsLocal.getString("CAMERA_CD"));
            object.addProperty("CAMERA_NAME", rsLocal.getString("CAMERA_NAME"));
            array.add(object);
        }
        return array;
    }
    
    public JsonArray getBatteryMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select BATTERY_CD,BATTERY_NAME from batterymst WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("BATTERY_CD", rsLocal.getString("BATTERY_CD"));
            object.addProperty("BATTERY_NAME", rsLocal.getString("BATTERY_NAME"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getTidMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select TID_CD,TID_NAME from TIDMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("TID_CD", rsLocal.getString("TID_CD"));
            object.addProperty("TID_NAME", rsLocal.getString("TID_NAME"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getCashDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD FROM CPRHD "
                + " c LEFT JOIN CPRDT c1 ON c.REF_NO=c1.REF_NO LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getOrderDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c.amt,c.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,m.model_cd,m.model_name,m1.memory_cd"
                + ",m1.memory_name,c1.colour_cd,c1.colour_name FROM orderbook c LEFT JOIN ACNTMST a ON "
                + " c.AC_CD=a.AC_CD left join modelmst m on c.model_cd=m.model_cd left join memorymst m1 on c.memory_cd=m1.memory_cd"
                + " left join colourmst c1 on c1.colour_cd=c.colour_cd  where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("amt"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("model_cd", rsLocal.getString("model_cd"));
            object.addProperty("model_name", rsLocal.getString("model_name"));
            object.addProperty("memory_cd", rsLocal.getString("memory_cd"));
            object.addProperty("memory_name", rsLocal.getString("memory_name"));
            object.addProperty("colour_cd", rsLocal.getString("colour_cd"));
            object.addProperty("color_name", rsLocal.getString("colour_name"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getVisitorDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,ac_name,c.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,mobile_no,model_name,s.sm_name,c.follow_up_date"
                + ",c.rev_ref_no,c.branch_cd,c.is_del"
                + ",memory_name,color_name FROM visitorbook c left join smmst s on s.sm_cd=c.attended_by where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("AC_NAME", rsLocal.getString("AC_NAME"));
            object.addProperty("MOBILE_NO", rsLocal.getString("MOBILE_NO"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("model_name", rsLocal.getString("model_name"));
            object.addProperty("memory_name", rsLocal.getString("memory_name"));
            object.addProperty("color_name", rsLocal.getString("color_name"));
            object.addProperty("sm_name", rsLocal.getString("sm_name"));
            object.addProperty("rev_ref_no", rsLocal.getString("rev_ref_no"));
            object.addProperty("branch_cd", rsLocal.getInt("branch_cd"));
            object.addProperty("is_del", rsLocal.getInt("is_del"));
            object.addProperty("follow_up_date", rsLocal.getString("follow_up_date"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray GetTags(Connection dataConnection, String value) throws SQLException {
        String sql = "select tag_no from tag where  tag_no like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            array.add(rsLocal.getString("tag_no"));
        }
        return array;
    }

    public JsonArray getCashDetailRecpt(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c1.SR_NO,c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD FROM CPRHD "
                + " c LEFT JOIN CPRDT c1 ON c.REF_NO=c1.REF_NO LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("SR_NO", rsLocal.getString("SR_NO"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getJournalDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select c.REF_NO,VDATE,a.FNAME,c1.AMT,c1.PART,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,c1.DRCR,c1.IMEI from JVHD "
                + " c left join JVDT c1 on c.REF_NO=c1.REF_NO left join ACNTMST a on c1.AC_CD=a.AC_CD "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("PART", rsLocal.getString("PART"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("DRCR", rsLocal.getString("DRCR"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("IMEI", rsLocal.getString("IMEI"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getContraDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select c.REF_NO,VDATE,a.FNAME,c1.AMT,c1.PART,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,c1.DRCR from CONTRAHD "
                + " c left join CONTRADT c1 on c.REF_NO=c1.REF_NO left join ACNTMST a on c1.AC_CD=a.AC_CD "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("PART", rsLocal.getString("PART"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("DRCR", rsLocal.getString("DRCR"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getDCDetail(Connection dataConnection, String value) throws SQLException {
        String sql = "select INV_NO,V_TYPE,V_DATE,(a.FNAME||a.mname||a.lname) as ac_name,(a1.ADD1||a1.add2||a1.add3) as address,p.EMAIL,p.MOBILE1,"
                + " s.SR_NAME,v1.IMEI_NO,v1.SERAIL_NO,v1.QTY,v1.RATE,v1.AMT,v.DET_TOT,a.TIN from DCHD v left join acntmst a on v.AC_CD=a.AC_CD "
                + " left join adbkmst a1 on a.AC_CD=a1.AC_CD left join phbkmst p on a.AC_CD=p.AC_CD left join DCDT v1 on v.REF_NO=v1.REF_NO "
                + " left join SERIESMST s on v1.SR_CD=s.SR_CD left join payment p1 on v.ref_no=p1.REF_NO where v.REF_NO='" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("V_TYPE", rsLocal.getString("V_TYPE"));
            object.addProperty("V_DATE", rsLocal.getString("V_DATE"));
            object.addProperty("FNAME", rsLocal.getString("ac_name"));
            object.addProperty("ADDRESS", rsLocal.getString("address"));
            object.addProperty("EMAIL", rsLocal.getString("EMAIL"));
            object.addProperty("MOBILE1", rsLocal.getString("MOBILE1"));
            object.addProperty("SR_NAME", rsLocal.getString("SR_NAME"));
            object.addProperty("IMEI_NO", rsLocal.getString("IMEI_NO"));
            object.addProperty("SERAIL_NO", rsLocal.getString("SERAIL_NO"));
            object.addProperty("QTY", rsLocal.getString("QTY"));
            object.addProperty("RATE", rsLocal.getString("RATE"));
            object.addProperty("AMT", rsLocal.getString("AMT"));
            object.addProperty("DET_TOT", rsLocal.getString("DET_TOT"));
            object.addProperty("TIN", rsLocal.getString("TIN"));
            array.add(object);
        }
        return array;
    }

    public JsonArray getBankDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD,"
                + "a1.AC_CD as bank_cd,OPP_BANK_NAME,"
                + " a1.FNAME as bank_name,c.CHEQUE_NO,c.CHEQUE_DATE FROM BPRHD "
                + " c LEFT JOIN BPRDT c1 ON c.REF_NO=c1.REF_NO LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO"
                + " left join ACNTMST a1 on c.BANK_CD=a1.AC_CD  "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("OPP_BANK_NAME", rsLocal.getString("OPP_BANK_NAME"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getBankDetailRcpt(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD,"
                + "a1.AC_CD as bank_cd,OPP_BANK_NAME,"
                + " a1.FNAME as bank_name,c.CHEQUE_NO,c.CHEQUE_DATE FROM BPRHD "
                + " c LEFT JOIN BPRDT c1 ON c.REF_NO=c1.REF_NO LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO"
                + " left join ACNTMST a1 on c.BANK_CD=a1.AC_CD  "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("CHEQUE_NO", rsLocal.getString("CHEQUE_NO"));
            object.addProperty("CHEQUE_DATE", rsLocal.getString("CHEQUE_DATE"));
            object.addProperty("OPP_BANK_NAME", rsLocal.getString("OPP_BANK_NAME"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getDNDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD,"
                + " a1.AC_CD as bank_cd,a1.FNAME as bank_name FROM DNCNHD c LEFT JOIN DNCNDT c1 ON c.REF_NO=c1.REF_NO "
                + " LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO"
                + " left join ACNTMST a1 on c.BANK_CD=a1.AC_CD  "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("user_id")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getCNDetail(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "SELECT c.REF_NO,VDATE,a.FNAME,c1.BAL,c1.REMARK,c.USER_ID,c.EDIT_NO,c.TIME_STAMP,a.AC_CD,o.DOC_REF_NO,o.INV_NO,o.DOC_CD,"
                + " a1.AC_CD as bank_cd,a1.FNAME as bank_name FROM DNCNHD c LEFT JOIN DNCNDT c1 ON c.REF_NO=c1.REF_NO "
                + " LEFT JOIN ACNTMST a ON c.AC_CD=a.AC_CD LEFT JOIN oldb2_4 o ON c1.DOC_REF_NO=o.DOC_REF_NO"
                + " left join ACNTMST a1 on c.BANK_CD=a1.AC_CD  "
                + "where  c." + field + " = '" + value + "'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("REF_NO", rsLocal.getString("REF_NO"));
            object.addProperty("VDATE", rsLocal.getString("VDATE"));
            object.addProperty("FNAME", rsLocal.getString("FNAME"));
            object.addProperty("BAL", rsLocal.getString("BAL"));
            object.addProperty("REMARK", rsLocal.getString("REMARK"));
            object.addProperty("USER_ID", getUserName(rsLocal.getString("USER_ID")));
            object.addProperty("EDIT_NO", rsLocal.getString("EDIT_NO"));
            object.addProperty("TIME_STAMP", rsLocal.getString("TIME_STAMP"));
            object.addProperty("AC_CD", rsLocal.getString("AC_CD"));
            object.addProperty("DOC_REF_NO", rsLocal.getString("DOC_REF_NO"));
            object.addProperty("DOC_CD", rsLocal.getString("DOC_CD"));
            object.addProperty("INV_NO", rsLocal.getString("INV_NO"));
            object.addProperty("BANK_CD", rsLocal.getString("BANK_CD"));
            object.addProperty("BANK_NAME", rsLocal.getString("BANK_NAME"));
            object.addProperty("COMPANY_TIN", companySetUp().getTin_no());
            object.addProperty("COMPANY_CST", companySetUp().getCst_No());
            array.add(object);
        }
        return array;
    }

    public JsonArray getGroupMaster(Connection dataConnection, String field, String value) throws SQLException {
        String sql = "select GRP_CD,GROUP_NAME from GROUPMST WHERE " + field + " like '%" + value + "%'";
        PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
        ResultSet rsLocal = pstLocal.executeQuery();
        JsonArray array = new JsonArray();
        while (rsLocal.next()) {
            JsonObject object = new JsonObject();
            object.addProperty("GRP_CD", rsLocal.getString("GRP_CD"));
            object.addProperty("GROUP_NAME", rsLocal.getString("GROUP_NAME"));
            array.add(object);
        }
        return array;
    }

    public String generateKey(Connection dataConnection, String table, String column, String prefix, int length) {
        String code = "";
        long no = 0;
        try {
            PreparedStatement pstLocal = dataConnection.prepareStatement("SELECT MAX(" + column + ") FROM " + table + " where " + column + " like '" + prefix + "%'");
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                if (rsLocal.getString(1) != null) {
                    String sno = rsLocal.getString(1).substring(prefix.length());
                    no = Integer.parseInt(sno);
                    no++;
                    for (int i = (no + "").length(); i < (length - prefix.length()); i++) {
                        code += "0";
                    }
                    code = prefix + code + no;
                } else {
                    code = prefix;
                    for (int i = 1; i < (length - prefix.length()); i++) {
                        code += "0";
                    }
                    code = code + "1";
                }
            }
            if (rsLocal != null) {
                rsLocal.close();
            }
            if (pstLocal != null) {
                pstLocal.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return code;
    }

    public SysEnv companySetUp() {
        boolean flag = false;
        SysEnv clSysEnv = new SysEnv();
        Connection con = null;
        try {

            String sql = "select * from company_mst "
                    + "where upper(cmpn_name)='IPEARL' and"
                    + " ac_year='2015'";
            con = helper.getConnMpLogin();
            PreparedStatement pstLocal = con.prepareStatement(sql);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                clSysEnv.setAC_YEAR(rsLocal.getString("ac_year"));
                clSysEnv.setCmpn_name(rsLocal.getString("cmpn_name"));
                clSysEnv.setAddress_1(rsLocal.getString("address1"));
                clSysEnv.setAddress_2(rsLocal.getString("address2"));
                clSysEnv.setAddress_3(rsLocal.getString("address3"));
                clSysEnv.setCity(rsLocal.getString("city"));
                clSysEnv.setPincode(rsLocal.getString("pincode"));
                clSysEnv.setPhone_no(rsLocal.getString("phone_no"));
                clSysEnv.setFax_No(rsLocal.getString("fax_no"));
                clSysEnv.setCst_No(rsLocal.getString("cst_no"));
                clSysEnv.setTin_no(rsLocal.getString("tin_no"));
                clSysEnv.setPanNo(rsLocal.getString("pan_no"));
                clSysEnv.setShName(rsLocal.getString("sh_name"));
                clSysEnv.setEmail(rsLocal.getString("emailid"));
                clSysEnv.setCash_ac_cd(rsLocal.getString("cash_ac_cd"));
                clSysEnv.setKasar_cd(rsLocal.getString("kasar_ac"));
                clSysEnv.setPurchase_ac(rsLocal.getString("purchase_ac"));
                clSysEnv.setSales_ac(rsLocal.getString("sales_ac"));
                clSysEnv.setPurchase_return_ac(rsLocal.getString("purchase_return_ac"));
                clSysEnv.setSales_return_ac(rsLocal.getString("sales_return_ac"));
                clSysEnv.setCountry(rsLocal.getString("country"));
                clSysEnv.setState(rsLocal.getString("PROVIANCE"));
                clSysEnv.setBuy_back_ac(rsLocal.getString("buy_back_ac"));
                clSysEnv.setUrd_purchase(rsLocal.getString("urd_purchase"));
                clSysEnv.setDisc_ac(rsLocal.getString("disc_ac"));
                return clSysEnv;

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            closeConnection(con);
        }
        return clSysEnv;
    }

    public String getUserName(String user_id) {
        String user_name = "";
        Connection con = null;
        try {
            String sql = "select USER_NAME from USERMST "
                    + "where (USER_ID)=" + user_id;
            con = helper.getMainConnection();
            PreparedStatement pstLocal = con.prepareStatement(sql);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                user_name = rsLocal.getString("user_name");
            }
            closeResultSet(rsLocal);
            closeStatement(pstLocal);
        } catch (SQLException ex) {
            ex.printStackTrace();
            return user_name;
        } finally {
            closeConnection(con);
        }
        return user_name;
    }

    public String getTaxCode(Connection con, String val, String tag) {
        String returnVal = "";
        try {
            String sql = "";
            if (tag.equalsIgnoreCase("N")) {
                sql = "select tax_name from taxmst where tax_cd='" + val + "'";
            } else if (tag.equalsIgnoreCase("C")) {
                sql = "select tax_cd from taxmst where tax_name='" + val + "'";
            } else if (tag.equalsIgnoreCase("RC")) {
                sql = "select tax_cd from taxmst where tax_per=" + val;
            } else if (tag.equalsIgnoreCase("CR")) {
                sql = "select tax_per from taxmst where tax_cd='" + val + "'";
            } else if (tag.equalsIgnoreCase("CRA")) {
                sql = "select add_tax_per from taxmst where tax_cd='" + val + "'";
            } else if (tag.equalsIgnoreCase("CN")) {
                sql = "select tax_per from taxmst where tax_name='" + val + "'";
            } else if (tag.equalsIgnoreCase("TAC")) {
                sql = "select tax_ac_cd from taxmst where tax_cd='" + val + "'";
            } else if (tag.equalsIgnoreCase("TACA")) {
                sql = "select add_tax_ac_cd from taxmst where tax_cd='" + val + "'";
            } else if (tag.equalsIgnoreCase("TOS")) {
                sql = "select tax_on_sales from taxmst where tax_cd='" + val + "'";
            }
            PreparedStatement pstLocal = con.prepareStatement(sql);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                returnVal = rsLocal.getString(1);
            }
            closeResultSet(rsLocal);
            closeStatement(pstLocal);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return returnVal;
    }

    public void closeResultSet(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void closeStatement(PreparedStatement pst) {
        try {
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void closeConnection(Connection pst) {
        try {
            if (pst != null) {
                pst.close();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public int getLast(Connection dataConnection, String columnName, String tableName, String whereCol, String whereData) {
        int id = 0;
        String sql = "";
        try {
            sql = "select max(" + columnName + ") from " + tableName + " where " + whereCol + " =?";
            PreparedStatement ps = dataConnection.prepareStatement(sql);
            ps.setString(1, whereData);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

    public int getLast(Connection dataConnection, String columnName, String tableName) {
        int id = 0;
        String sql = "";
        try {
            sql = "select max(" + columnName + ") from " + tableName;
            PreparedStatement ps = dataConnection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return id;
    }

    public String getData(Connection dataConnection, String column, String table, String where, String whereData, int type) {
        String data = "";
        try {
            String sql = "";
            if (type == 0) {
                sql = "select " + column + " from " + table + " where " + where + "='" + whereData + "'";
            } else {
                sql = "select " + column + " from " + table + " where " + where + "=" + whereData + "";
            }
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                data = rsLocal.getString(1);
            }
            closeResultSet(rsLocal);
            closeStatement(pstLocal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return data;
    }

    public String getSR_CD(Connection dataConnection, String code, String tag) {
        String returnVal = "";
        String sql = "";
        if (tag.equalsIgnoreCase("C")) {
            sql = "select sr_cd from SERIESMST where sr_name=?";
        } else if (tag.equalsIgnoreCase("AC")) {
            sql = "select sr_cd from SERIESMST where sr_alias=?";
        } else if (tag.equalsIgnoreCase("N")) {
            sql = "select sr_name from SERIESMST where sr_cd=?";
        } else if (tag.equalsIgnoreCase("M")) {
            sql = "SELECT MODEL_CD FROM SERIESMST WHERE sr_cd=?";
        } else if (tag.equalsIgnoreCase("Mn")) {
            sql = "SELECT MODEL_CD FROM SERIESMST WHERE sr_name=?";
        }
        try {
            PreparedStatement pstLocal = dataConnection.prepareStatement(sql);
            pstLocal.setString(1, code);
            ResultSet rsLocal = pstLocal.executeQuery();
            if (rsLocal.next()) {
                returnVal = rsLocal.getString(1);
            }
            closeResultSet(rsLocal);
            closeStatement(pstLocal);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnVal;
    }
}
