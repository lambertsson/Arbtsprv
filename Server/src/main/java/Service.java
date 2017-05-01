import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import io.vertx.core.json.JsonObject;

public class Service {
private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
private UUID id;
private String name, url, lastCheck;
private boolean status;

// Creates a new Service with name and url. ID is randomized, status set to FAIL
// and lastCheck to the current time.
public Service(String name, String url){
        this.id = UUID.randomUUID();
        this.name = name;
        this.url = url;
        this.status = false;
        this.lastCheck = df.format(new Date());
}

public Service(String id, String name, String url, String status, String lastCheck){
        this.id = UUID.fromString(id);
        this.name = name;
        this.url = url;
        this.status = (status.equals("OK")) ? true : false;
        this.lastCheck = lastCheck;
}

// Returns the UUID from Service.
public UUID getID(){
        return id;
}

// Returns the UUID as a String.
public String getIDAsString(){
        return id.toString();
}

// Returns the name as a String.
public String getName(){
        return name;
}

// Returns the URL as a String.
public String getURL(){
        return url;
}

// Returns the host as a String.
// Example: URL: http://www.google.se, HOST: google.se
public String getHost(){
        return url.substring((url.indexOf("://") + 3));
}

// Returns the status as a String. "OK" if true, "FAIL" if false;
public String getStatus(){
        if(status)
                return "OK";
        else
                return "FAIL";
}

// Returns the lastCheck as a String.
public String getLastCheck(){
        return lastCheck;
}

// Sets the lastCheck to provded date String.
public void setChecked(String date){
        this.lastCheck = date;
}

// Sets the lastCheck to current date and time.
public void setCheckedNow(){
        this.lastCheck = df.format(new Date());
}

// Sets the status to value of status parameter.
public void setStatus(boolean status){
        this.status = status;
}

// Returns the JSON representation of the Service object.
public JsonObject toJSON(){
        JsonObject json = new JsonObject();
        json.put("id", getIDAsString());
        json.put("name", name);
        json.put("url", url);
        json.put("status", getStatus());
        json.put("lastCheck", getLastCheck());
        return json;
}
}
