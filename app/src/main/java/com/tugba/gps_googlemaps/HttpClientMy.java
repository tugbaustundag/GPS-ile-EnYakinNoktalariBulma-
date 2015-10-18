package com.tugba.gps_googlemaps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
public class HttpClientMy  {
    //RESTful Web servisini çağırıp, içerikleri sunucuya gönderen ve  sunucudan  data çekmeyi sağlayn methodu
    public static String callWebService(String wcfUrl,JSONObject jsonObject) {
        String jsonString = "";
        try {
            //Bağlantıyı sağlamak için HttpClient sınıfımızı tanımlıyoruz
            HttpClient httpClient = new DefaultHttpClient();
            HttpResponse response;
            //Post işlemi için sınıfımızı tanımlıyoruz...
            HttpPost post=new HttpPost();
            //Json objesinde tuttugumuz icerikleri String hale getirip, setEntity methoduna atıyoruz..
            HttpEntity httpEntity;
            StringEntity stringEntity=new StringEntity(jsonObject.toString(), HTTP.UTF_8);
            stringEntity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpEntity=stringEntity;
            post.setEntity(httpEntity);
            //RESTful Web Servisinin baglancağı url yi veriyoruz...
            post.setURI(new URI(wcfUrl));
            post.setHeader("Content-type", "application/json");
            // HttpEntity tutulan dataların HttpResponse tarafından çalıstırılmasını saglama..
            response=httpClient.execute(post);
            //response değişkenindeki  nesneyi, json string değerine çeviriyoruz...
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = rd.readLine()) != null) {
                sb.append(line + NL);
            }
            //Elde ettigimiz json string i değişkene atadık
            jsonString = sb.toString();
            rd.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonString;
    }
}