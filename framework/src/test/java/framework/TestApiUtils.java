package framework;

import org.mvss.karta.framework.restclient.*;

public class TestApiUtils {
    public static void main(String[] args) {
        try (RestClient restClient = new ApacheRestClient("http://192.168.1.4:27010", true)) {
            RestRequest restRequest =
                    ApacheRestRequest.requestBuilder().contentType(ContentType.APPLICATION_JSON).accept(ContentType.APPLICATION_JSON).build();

            try (RestResponse restResponse = restClient.get(restRequest, "/api/categoriesTree")) {
                String responseText = restResponse.getBody();

                System.out.println(responseText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
