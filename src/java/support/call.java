/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package support;

/**
 *
 * @author bhaumik
 */
public class call {

    public void sendsms(String number, String amt,String name) throws Exception {
        send_sms smsObj = new send_sms();
        smsObj.setparams("http://alerts.sinfini.com", "sms", "A0f090bc3b9c8b12064bf219df4681c64", "iPearl");
        smsObj.send_sms(number, "Dear, "+name+" Thank you for shoping at iPearl your invoice of " + amt + " has been generated."
                + " Do Like us https://goo.gl/cW2dEe", "");
    }
}
