package org.samswi.caperandomizer.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class CapeRandomizerClient implements ClientModInitializer {
    public static List<Cape> ownedCapesList = new ArrayList<Cape>(50);
    public static List<Cape> capesPull = new ArrayList<Cape>(50);
    public static HttpClient httpClient = HttpClient.newBuilder().build();
    public static Cape currentCape;
    public static Gson myGson = new Gson();
    public static Logger LOGGER = LoggerFactory.getLogger("Cape Rand");
    private static String accessToken;

    @Override
    public void onInitializeClient() {

    }

    public static void fillCapesList(String accessToken){
        if (accessToken.length() < 200) return;
        try {
            HttpRequest profileDataRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
                    .setHeader("Authorization", "Bearer " + accessToken)
                    .GET()
                    .build();
            HttpResponse<String> profileDataResponse = httpClient.send(profileDataRequest, HttpResponse.BodyHandlers.ofString());
            StringBuilder capesString = new StringBuilder();
            if (profileDataResponse.statusCode() == 200){
                JsonArray capesArray = myGson.fromJson(profileDataResponse.body(), JsonObject.class).get("capes").getAsJsonArray();
                ownedCapesList.clear();
                for (JsonElement i : capesArray){
                    JsonObject j = i.getAsJsonObject();
                    Cape capeIterator = new Cape(j.get("id").getAsString(), j.get("url").getAsString(), j.get("alias").getAsString());
                    ownedCapesList.add(capeIterator);
                    capesString.append(capeIterator.name).append(", ");
                    if (j.get("state").getAsString().equals("ACTIVE")){
                        currentCape = capeIterator;
                    }
                }
            }
            CapeRandomizerClient.accessToken = accessToken;
            if (ownedCapesList.isEmpty()){
                LOGGER.error("Failed to fetch capes or account doesn't own any");
                return;
            }
            for (Cape cape : ownedCapesList){
                capesString.append(cape.name).append(", ");
            }
            LOGGER.info("Fetched {} capes: {}", ownedCapesList.size(), capesString);
            capesPull = new ArrayList<>(ownedCapesList);

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void equipRandomCape(){
        if (ownedCapesList.size() <= 1) return;
        if (capesPull.isEmpty()) {
            capesPull = new ArrayList<>(ownedCapesList);
            capesPull.remove(currentCape);
        }

        try {
            int selectedCapeIndex = (int)(Math.random()*capesPull.size());
            equipCape(capesPull.get(selectedCapeIndex));
            currentCape = capesPull.get(selectedCapeIndex);
            capesPull.remove(selectedCapeIndex);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void equipCape(@NotNull Cape cape) throws IOException, InterruptedException {
        String jsonBody = "{\"capeId\":\"" + cape.id + "\"}";
        HttpRequest changeCapeRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
                .setHeader("Authorization", "Bearer " + accessToken)
                .setHeader("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> changeCapeResponse = httpClient.send(changeCapeRequest, HttpResponse.BodyHandlers.ofString());
        if (changeCapeResponse.statusCode() == 200 || changeCapeResponse.statusCode() == 204){
            LOGGER.info("Equipped \"{}\" cape", cape.name);
        }
        else{
            LOGGER.error("Failed to equip cape: {}", changeCapeResponse.statusCode());
        }
    }
}
