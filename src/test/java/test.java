import io.vertx.core.json.JsonObject;

public class test {
    public static void main(String[] args) {
        JsonObject j1 = new JsonObject();
        j1.put("Data",1);
        JsonObject j2 = new JsonObject(j1.toString());
        j1.put("Data",2);
        System.out.println(j2);
    }
}
