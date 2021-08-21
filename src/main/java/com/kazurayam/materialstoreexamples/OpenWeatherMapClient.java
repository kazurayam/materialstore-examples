package com.kazurayam.materialstoreexamples;

import com.google.common.collect.ImmutableMap;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Makes HTTP Request to the OpenWeatherMap service
 * to download weather forecast data in JSON.
 */
public class OpenWeatherMapClient {

    private final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast";

    OpenWeatherMapClient() {}

    String getOpenWeatherData(Map<String, String> param) throws IOException, InterruptedException, URISyntaxException {
        // find API_KEY to access OpenWeatherMap.
        String API_KEY = API_KEY();

        //System.out.printf("[OpenWeatherMapClient] API_KEY=%s\n", API_KEY);

        // will use Apache httpclient to interact with the OpenWeatherMap site
        CloseableHttpClient client = HttpClientBuilder.create().build();
        List<NameValuePair> baseParameters = new ArrayList<NameValuePair>();
        baseParameters.add(new BasicNameValuePair("appid", API_KEY));

        List<NameValuePair> nvpList = new ArrayList<NameValuePair>();
        param.keySet().forEach(key -> {
            String value = param.get(key);
            nvpList.add(new BasicNameValuePair(key, value));
        });
        //
        HttpGet httpGet = new HttpGet(BASE_URL);
        URI uri = new URIBuilder(httpGet.getURI())
                .addParameters(baseParameters)
                .addParameters(nvpList)
                .build();
        ((HttpRequestBase)httpGet).setURI(uri);
        CloseableHttpResponse response = client.execute(httpGet);
        assert response.getStatusLine().getStatusCode() == 200;
        String json = EntityUtils.toString(response.getEntity(), "UTF-8");
        client.close();
        return json;
    }

    /**
     * will retrieve it from the KeyChain storage of kazurayam's Mac Book Air.
     */
    private static final String API_KEY() throws IOException, InterruptedException {
        String API_KEY = new MyKeyChainAccessor().findPassword(
                "home.openweathermap.org/api_keys",
                "myFirstKey");
        //println "API_KEY: ${API_KEY}";
        assert API_KEY != null;
        return API_KEY;
    }




    /**
     *
     * @param args
     */
    static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
        OpenWeatherMapClient client = new OpenWeatherMapClient();
        client.getOpenWeatherData(ImmutableMap.of("id", "498817"));    // Saint Petersburg,ru
        client.getOpenWeatherData(ImmutableMap.of("q", "Hachinohe"));  // Hachinohe,jp
    }
}
