/*
 * This Java source file was generated by the Gradle 'init' task.
 */
import java.util.Date;
import java.util.ArrayList;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class App {
    ArrayList<Service> services = new ArrayList<Service>();

    // Returns the current date and time as a String for console log.
    // Looks like: '[Apr 27, 2017 4:35:42 PM]: '
    public String getTime() {
      Date d = new Date();
      return "[" + d.toLocaleString() + "]: ";
    }

    // Returns the JSON object of all the services as a String.
    public String getJSONAsString() {
      JsonObject json = new JsonObject();
      JsonArray jsonArray = new JsonArray();

      for(Service service : services)
        jsonArray.add(service.toJSON());
      json.put("services", jsonArray);

      return json.toString();  // toString because unable to make JsonArray buffer
    }

    // Do a http GET and check the status of the provided Service object.
    private void checkStatus(Service service, io.vertx.ext.web.client.WebClient client){
      client
      .get(service.getHost(), "/")
      .send(ar -> {
        service.setCheckedNow();
        if (ar.succeeded())
          service.setStatus(true);
        else
          service.setStatus(false);
        // Because service is a reference to the object in services - it is updated
        // when the values of the object are changed.
        System.out.println(service.getName() + ": " + service.getStatus());
      });
    }

    // Sets some example data to the services.
    public void setExampleData(){
      services.add(new Service("Google SE", "https://www.google.se"));
      services.add(new Service("Kry", "http://www.kry.se"));
      System.out.println(getTime() + "Added services:");
      for(Service service : services)
        System.out.println(service.toJSON().toString());
    }

    // Starts the server that listens to requests and periodically checks the
    // status of services.
    public void startServer() {
      System.out.println(getTime() + "Starting server...");
      Vertx.vertx()
      .createHttpServer()
      .requestHandler(req -> req.response().putHeader("Content-Type", "application/json").end(getJSONAsString()))
      .listen(8080, handler -> {
        if (handler.succeeded()) {
          System.out.println(getTime() + "Server running on http://localhost:8080/");
        } else {
          System.err.println(getTime() + "Failed to listen on port 8080");
        }
      });

      Vertx clientVertx = Vertx.vertx();
      io.vertx.ext.web.client.WebClient client = io.vertx.ext.web.client.WebClient.create(clientVertx);
      clientVertx.setPeriodic(8000, id -> {  // 60,000 ms is 1 minute, do this every minute... //TODO: Change back timer to 60,000
        System.out.println(getTime() + "Checking status of services...");
        for(Service service : services)
          checkStatus(service, client);
      });
    }

    // Main method
    public static void main(String[] args) {
        App app = new App();
        app.setExampleData();
        app.startServer();
    }
}
