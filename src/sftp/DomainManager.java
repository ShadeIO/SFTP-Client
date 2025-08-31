package sftp;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import javax.json.*;

public class DomainManager {

    private Map<String, String> domainToIp = new TreeMap<>();
    private Map<String, String> ipToDomain = new HashMap<>();

    public void loadFromInputStream(InputStream is) {
        try (JsonReader reader = Json.createReader(is)) {
            JsonObject root = reader.readObject();
            JsonArray addresses = root.getJsonArray("addresses");


            for (JsonValue value : addresses) {
                JsonObject obj = value.asJsonObject();
                String domain = obj.getString("domain");
                String ip = obj.getString("ip");

                domainToIp.put(domain, ip);
                ipToDomain.put(ip, domain);
            }
        }
    }

    public java.util.Map<String,String> getAllSorted() {
        return new java.util.TreeMap<>(domainToIp); // копия
    }

    public void printAll() {
        System.out.println("\nList of domains:");
        for (String domain : domainToIp.keySet()) {
            System.out.println(domain + " -> " + domainToIp.get(domain));
        }
    }

    public boolean addPair(String domain, String ip) {
        // Проверка уникальности
        if (domainToIp.containsKey(domain) || ipToDomain.containsKey(ip)) {
            return false;
        }

        // Проверка IP (IPv4)
        if (!IpValidator.isValidIPv4(ip)) {
            return false;
        }

        domainToIp.put(domain, ip);
        ipToDomain.put(ip, domain);
        return true;
    }

    public void saveToStream(OutputStream os) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Map.Entry<String, String> entry : domainToIp.entrySet()) {
            arrayBuilder.add(Json.createObjectBuilder()
                    .add("domain", entry.getKey())
                    .add("ip", entry.getValue()));
        }

        JsonObject root = Json.createObjectBuilder()
                .add("addresses", arrayBuilder)
                .build();

        try (JsonWriter writer = Json.createWriter(os)) {
            writer.writeObject(root);
        }
    }


    public boolean removePair(String input) {
        if (domainToIp.containsKey(input)) {
            String ip = domainToIp.remove(input);
            ipToDomain.remove(ip);
            return true;
        } else if (ipToDomain.containsKey(input)) {
            String domain = ipToDomain.remove(input);
            domainToIp.remove(domain);
            return true;
        }
        return false;
    }



    public String getIpByDomain(String domain) {
        return domainToIp.get(domain);
    }

    public String getDomainByIp(String ip) {
        return ipToDomain.get(ip);
    }
}
