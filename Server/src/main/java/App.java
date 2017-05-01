import java.io.PrintWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.ArrayList;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public class App {
static int statusTimer = 8000;      // 60,000 ms is 1 minute, do this every minute... //TODO: Change back timer to 60,000
ArrayList<Service> services = new ArrayList<Service>();     // TODO: Do I need this, or should I keep it as a JsonArray since it's available?

/*
 * Returns the current date and time as a String for console log.
 * Looks like: '[Apr 27, 2017 4:35:42 PM]: '
 */
public String getTime() {
        Date d = new Date();
        return "[" + d.toLocaleString() + "]: ";
}

/*
 * Returns the JSON object of all the services as a String.
 */
public String getJSONAsString() {
        JsonObject json = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        for(Service service : services)
                jsonArray.add(service.toJSON());
        json.put("services", jsonArray);

        return json.toString(); // toString because unable to make JsonArray buffer
}

/*
 * Do a http GET and check the status of the provided Service object.
 */
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
                        System.out.println(getTime() + service.getName() + ": " + service.getStatus());
                });
}

/*
 * Sets some example data to the services.
 */
public void setExampleData(){
        //services.add(new Service("Google SE", "https://www.google.se"));
        services.add(new Service("Kry", "http://www.kry.se"));
        services.add(new Service("80d04373-098e-4d89-b744-26f4b9520252", "Google SE", "https://www.google.se", "FAIL", "2017-04-28 14:11"));
        System.out.println(getTime() + "Added services:");
        for(Service service : services)
                System.out.println(service.toJSON().toString());
}

/*
 * Reads the services saved to the file and adds it to the services array.
 */
public void readServicesFromFile(){
        System.out.println(getTime() + "Reading file...");
        try{
                String contents = new String(Files.readAllBytes(Paths.get("services.json")));
                JsonObject json = new JsonObject(contents);
                JsonArray array = json.getJsonArray("services");
                for(int i = 0; i < array.size(); i++) {
                        JsonObject jsonobject = array.getJsonObject(i);
                        services.add(new Service(
                                             jsonobject.getString("id"),
                                             jsonobject.getString("name"),
                                             jsonobject.getString("url"),
                                             jsonobject.getString("status"),
                                             jsonobject.getString("lastCheck")
                                             ));
                }
                System.out.println(getTime() + "Done reading file.");
        } catch (IOException e) {
                System.out.println(getTime() + "Failed to read file!");
        }

}

/*
 * Writes the services to a .json file.
 */
public void writeServicesToFile(){
        System.out.println(getTime() + "Writing file...");
        try{
                PrintWriter writer = new PrintWriter("services.json", "UTF-8");
                writer.print(getJSONAsString());
                writer.close();
                System.out.println(getTime() + "Done writing file.");
        } catch (IOException e) {
                System.out.println(getTime() + "Failed writing file!");
        }
}

/*
 * Starts the server to handle HTTP requests made to ../service
 */
public void startServer(){
        Vertx serverVertx = Vertx.vertx();
        System.out.println(getTime() + "Starting server...");

        io.vertx.core.http.HttpServer server = serverVertx.createHttpServer();
        io.vertx.ext.web.Router router = io.vertx.ext.web.Router.router(serverVertx);

        router.route().handler(io.vertx.ext.web.handler.BodyHandler.create());
        router.get("/service").handler(this::handleGetService);
        router.post("/service").handler(this::handleAddService);
        //router.delete("/service/:service_id").handler(this::handleDeleteService); //TODO: Regex

        serverVertx.createHttpServer().requestHandler(router::accept).listen(8080);
}

/*
 * Handles the HTTP GET request made to the server.
 * Returns the json-object "services" with all the services.
 */
public void handleGetService(io.vertx.ext.web.RoutingContext routingContext){
        routingContext.response().putHeader("content-type", "application/json").end(getJSONAsString());
}

/*
 * Handles the HTTP POST request made to the server.
 * Adds a new service with the name and url provided in the body.
 */
public void handleAddService(io.vertx.ext.web.RoutingContext routingContext){
        io.vertx.core.http.HttpServerResponse response = routingContext.response();
        JsonObject service = routingContext.getBodyAsJson();
        if (service == null) {
                //Something went wrong...
        } else {
                services.add(new Service(service.getString("name"), service.getString("url")));
                response.end();
        }
}

/*
 * Handles the HTTP DELETE requests made to the server.
 * Deletes the service with the provided service ID in .../service/{service_id}
 */
public void handleDeleteService(io.vertx.ext.web.RoutingContext routingContext){
        String serviceID = routingContext.request().uri();
        System.out.println(serviceID);
        // io.vertx.core.http.HttpServerResponse response = routingContext.response();
        // if (serviceID == null) {
        //   //Something went wrong...
        // } else {
        //   for(int i = 0; i < services.size(); i++){
        //     if(services.get(i).getIDAsString().equals(serviceID)){
        //       System.out.println(getTime() + "Found and removed: " + serviceID);
        //       services.remove(i);
        //     }
        //   }
        // }
}

/*
 * Starts the vertx client that checks the status of the services by an interval
 * defined in statusTimer.
 */
public void startClient(){
        Vertx clientVertx = Vertx.vertx();
        System.out.println(getTime() + "Starting client...");
        io.vertx.ext.web.client.WebClient client = io.vertx.ext.web.client.WebClient.create(clientVertx);
        clientVertx.setPeriodic(this.statusTimer, id -> {
                        writeServicesToFile(); // Write before checking, since the checks are done async. //TODO: Should be a better way.
                        System.out.println(getTime() + "Checking status of services...");
                        for(Service service : services)
                                checkStatus(service, client);
                });
}

/*
 * Starts the server that listens to requests and periodically checks the
 * status of services.
 */
public void start() {
        readServicesFromFile();
        startServer();
        startClient();
}

/*
 * Main method
 */
public static void main(String[] args) {
        App app = new App();
        //app.setExampleData();
        app.start();
}
}
