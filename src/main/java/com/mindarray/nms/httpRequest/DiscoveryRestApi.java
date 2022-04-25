package com.mindarray.nms.httpRequest;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DiscoveryRestApi {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscoveryRestApi.class);
    public static void attach(Router router) {
        LOGGER.debug("Discovery Rest API called");
        router.route().method(HttpMethod.POST).path("/discovery").blockingHandler(context -> {
            var credentials = new JsonObject();
             credentials = context.getBodyAsJson();
            String error = verifyCredential(credentials);
            HttpServerResponse response = context.response();
            response.setChunked(true);
            if(error.equals("No error")){
                LOGGER.info("Credentials verified successfully");
                response.write("Credentials verified successfully");
                boolean available = checkAvailability(credentials.getString("IP_Address"));
                if(available){
                    LOGGER.info("Initial Discovery successful");
                    response.write("\nInitial Discovery successful");
                    // Plugin call karke final discovery call karna baki hain
                }else{
                    response.write("\nError : Initial Discovery failed");
                    LOGGER.error("Initial Discovery failed");
                }
            }else{
                response.write("Error:"+error);
                LOGGER.error("Error"+ error);
            }
            response.end();
        });

    }

    private static boolean checkAvailability(String ip_address) {
        List<String> Commands = new ArrayList<>();
        Commands.add(0,"fping");
        Commands.add(1,"-c");
        Commands.add(2,"3");
        Commands.add(3,"-t");
        Commands.add(4,"1000");
        Commands.add(5,"-q");
        Commands.add(ip_address);
     ProcessBuilder builder = new ProcessBuilder();
     builder.command(Commands);
     builder.redirectErrorStream(true);
     Process process= null;
        try {
            process = builder.start();
        } catch (IOException e) {
            System.out.println("Unable to start builder for discovery");
        }
        assert process != null;
        var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line=null ;
        while (true){
            try {
                if ((line = reader.readLine()) == null)
                    line = reader.readLine();
                    break;

            } catch (IOException e) {
                System.out.println("Unable to read line");
            }
        }
        if(line == null){
            return false;
        }
        System.out.println(line);
        var pattern = Pattern.compile("%[a-zA-Z]+ = [0-9]\\/[0-9]\\/[0-9]%");
        var matcher = pattern.matcher(line);
        //System.out.println(matcher.group());

        if(matcher.find()){
           String match = matcher.group(0);
            var pattern1= Pattern.compile("\\/[0-9]%");
           var matcher1 = pattern1.matcher(match);
            if(matcher1.find()){
                return (matcher1.group(0).split("/")[1].split("%")[0]).equals("0");
            }else{
                return false ;
            }
        }else{
            System.out.println("Discovery Match not found");
            return false;
        }

    }

    private static String verifyCredential(JsonObject credentials) {
        if(credentials.containsKey("IP_Address")==false || credentials.containsKey("Metric_Type")==false){
            String error = "Wrong IP_Address or metric type. Pls verify the same and try again";
            return error;
        }

        else{
            if(credentials.getString("Metric_Type").equals("linux")){
                if(credentials.containsKey("username")==false || credentials.containsKey("password")==false)
                {
                    String error = "Username or Password not available for metric type linux";
                    return error;
                }
            }
            else if(credentials.getString("Metric_Type")=="windows"){
                if(credentials.containsKey("username")==false || credentials.containsKey("password")==false)
                {
                    String error = "Username or Password not available for metric type windows";
                    return error;
                }
            }
            else if(credentials.getString("Metric_Type")=="network_device"){
                if(credentials.containsKey("version")==false || credentials.containsKey("community")==false)
                {
                    String error = "version or community not available for metric type network devices";
                    return error;
                }
            }
        }

        String error = "No error";
        LOGGER.debug(error);
        return error;


    }
}
