package org.conv;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author Nassim MOUALEK
 * @since 23/04/2022
 */
public class DecodeMac {
    String baseUrl;
    String server;
    String username;
    String password;
    String token;
    String mac;
    String cmd;
    String streamId;
    //api.php?action=stream&sub=start&stream_ids[]=541

    public void startStream() {
        try {
            getLiveCategories();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getLiveCategories() {
        try {

            JSONObject aa = (JSONObject) getFormData(this.server, "type=account_info&action=get_main_info&mac="+this.mac );

            JSONArray a = (JSONArray) getFormData(this.server.replace("portal.php", "player_api.php") ,
                    "action=get_live_categories&username=" + URLEncoder.encode(this.username, "UTF-8")+"&password="+
                            URLEncoder.encode(this.password, "UTF-8"));
            int catId = Integer.parseInt((String) ((JSONObject)a.get(0)).get("category_id"));
            /*JSONArray b = (JSONArray) getFormData(this.server.replace("portal.php", "player_api.php") ,
                    "action=get_live_streams&category_id="+catId+"&username=" + URLEncoder.encode(this.username, "UTF-8")+"&password="+
                            URLEncoder.encode(this.password, "UTF-8"));*/
            //this.streamId = (Long) ((JSONObject)b.get(0)).get("stream_id");
            JSONObject c = (JSONObject) getFormData(this.server,
                    "type=itv&action=get_ordered_list&genre=600&force_ch_link_check=&fav=0&sortby=number&hd=0&p=0&JsHttpRequest=1-xml&from_ch_id=0");
            JSONArray data = (JSONArray) ((JSONObject) c.get("js")).get("data");
            int index = 5;
            String cmd = (String) ((JSONObject)data.get(index)).get("cmd");
            this.streamId = (String) ((JSONObject)data.get(index)).get("id");
            JSONObject c0 = (JSONObject) getFormData(this.server,
                    "type=itv&action=create_link&series=&forced_storage=0&disable_ad=0&download=0&force_ch_link_check=0&JsHttpRequest=1-xml&cmd="+cmd);
            c0.toJSONString();
            String gcmd = (String) ((JSONObject) c0.get("js")).get("cmd");
            String playToken = gcmd.substring(gcmd.indexOf("play_token")+"play_token=".length());
            String cmd_ = gcmd.replace("ffmpeg ", "");

            Object mm = getFormData(cmd_.substring(0, cmd_.indexOf('?')), cmd_.substring(cmd_.indexOf('?')+1));
            aa.toJSONString();
            c.toJSONString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public UserInfo getUserInfo() {
        try {
            JSONObject a_ = (JSONObject) getFormData(this.server.replace("portal.php", "player_api.php") ,
                    "username=" + URLEncoder.encode(this.username, "UTF-8")+"&password="+
                            URLEncoder.encode(this.password, "UTF-8"));
            JSONObject a = (JSONObject) a_.get("user_info");
            System.out.println(a.toJSONString());
            return new UserInfo((String) a.get("username"),
                    (String) a.get("password"),
                    (String) a.get("status"),
                    (String) a.get("exp_date"),
                    (String) a.get("max_connections"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public Info getData(String url, String mac) {
            URI xuri;
            try {
                xuri = new URI(url);
            } catch (URISyntaxException var4) {
                return null;
            }
            this.baseUrl = "http://" + xuri.getHost();
            this.server = "http://" + xuri.getHost();
            if (xuri.getPort() > 0) {
                this.server = this.server + ":" + xuri.getPort();
                this.baseUrl = this.baseUrl + ":" + xuri.getPort();
            }

            this.server = server + "/portal.php";

            try {
                new URL(this.server);
            } catch (MalformedURLException var3) {
                return null;
            }
            this.mac= mac;
            if (this.getToken()) {
                    if (this.getProfile()) {
                        if (this.getOrderedVODList()) {
                            if (this.getLink()) {
                                System.out.println(getUserInfo());
                                startStream();
                                return new Info(url, username, password);
                            } else {
                                throw new RuntimeException("ERROR: No Connection to Xtream Server\n");
                            }
                        } else {
                            throw new RuntimeException("ERROR: No Valid Account\n");
                        }
                    } else {
                        throw new RuntimeException("ERROR: No Connection to Xtream Server\n");
                    }
                } else {
                    throw new RuntimeException("ERROR: No Authorization Token\n");
                }

    }
    private Boolean getProfile() {
        try {
            JSONObject out = (JSONObject) this.getFormData(this.server, "type=stb&action=get_profile&JsHttpRequest=1-xml");
            System.out.println(out.toJSONString());
            return true;
        } catch (Exception var2) {
            return false;
        }
    }

    private Boolean getOrderedVODList() {
        try {
            JSONObject out = (JSONObject) this.getFormData(this.server, "action=get_ordered_list&type=vod&p=1&JsHttpRequest=1-xml");
            JSONObject js = (JSONObject)out.get("js");
            JSONArray res1 = (JSONArray)js.get("data");
            JSONObject res3 = (JSONObject)res1.get(1);
            this.cmd = res3.get("cmd").toString();
            return cmd.length() > 4 ? true : false;
        } catch (Exception var5) {
            return false;
        }
    }

    private Boolean getLink() {
        try {
            JSONObject out = (JSONObject) this.getFormData(this.server, "action=create_link&type=vod&cmd=" + this.cmd + "&JsHttpRequest=1-xml");
            JSONObject js = (JSONObject)out.get("js");
            String cmd_str = js.get("cmd").toString();
            String[] parts = cmd_str.split("/");
            if (parts.length > 4) {
                int idx_1 = parts.length - 2;
                int idx_2 = parts.length - 3;
                this.username = parts[idx_2];
                this.password = parts[idx_1];
                return true;
            } else {
                return false;
            }
        } catch (Exception var7) {
            return false;
        }
    }

    private Boolean getToken() {
        try {
            JSONObject out = (JSONObject) this.getFormData(this.server, "action=handshake&type=stb&token=");
            JSONObject js = (JSONObject)out.get("js");
            this.token = js.get("token").toString();
            System.out.println(token);
            return true;
        } catch (Exception var3) {
            return false;
        }
    }
    private Object postData(String url, String urlParameters) throws Exception {
        return getFormData("POST", url, urlParameters);
    }
    private Object getFormData(String url, String urlParameters) throws Exception {
        return getFormData("GET", url, urlParameters);
    }
    private Object getFormData(String method, String url, String urlParameters) throws Exception {
        URL obj = new URL(url + "?" + urlParameters);
        System.out.println(obj);
        HttpURLConnection con = (HttpURLConnection)obj.openConnection();
        con.setRequestMethod(method);
        con.setRequestProperty("User-Agent", "Lavf/57.73.100");
        String cookies = "mac=" + URLEncoder.encode(this.mac, "UTF-8") +
                "; stb_lang=en; timezone=Europe%2FParis; ";

        con.setRequestProperty("Cookie", cookies);
        con.setDoOutput(true);
        con.setInstanceFollowRedirects(true);

        if (this.token != null) {
            con.setRequestProperty("Authorization", "Bearer " + this.token);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuffer response = new StringBuffer();

        String inputLine;
        while((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();
        Object jout = JSONValue.parse(response.toString());
        return jout;
    }
}
