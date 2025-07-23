package org.samswi.caperandomizer.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CaperandomizerClient implements ClientModInitializer {
    public static List<Cape> capesList = new ArrayList<Cape>(50);
    public static HttpClient httpClient = HttpClient.newBuilder().build();
    public static Gson myGson = new Gson();
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
//            System.out.println(profileDataRequest.headers().toString());
            HttpResponse<String> profileDataResponse = httpClient.send(profileDataRequest, HttpResponse.BodyHandlers.ofString());
//            System.out.println(profileDataResponse.body());
//            System.out.println(profileDataResponse.statusCode());
            if (profileDataResponse.statusCode() == 200){
                JsonArray capesArray = myGson.fromJson(profileDataResponse.body(), JsonObject.class).get("capes").getAsJsonArray();
                capesList.clear();
                for (JsonElement i : capesArray){
                    JsonObject j = i.getAsJsonObject();
                    capesList.add(new Cape(j.get("id").getAsString(), j.get("url").getAsString(), j.get("alias").getAsString()));
                }
                System.out.println(capesList);
            }
            CaperandomizerClient.accessToken = accessToken;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void equipRandomCape(){
        try {
            if (capesList.size() == 0) return;
            equipCape(capesList.get((int)(Math.random()*capesList.size())));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void equipCape(Cape cape) throws IOException, InterruptedException {
        String jsonBody = "{\"capeId\":\"" + cape.id + "\"}";
        HttpRequest changeCapeRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
                .setHeader("Authorization", "Bearer " + accessToken)
                .setHeader("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        HttpResponse<String> capeChangeResponse = httpClient.send(changeCapeRequest, HttpResponse.BodyHandlers.ofString());
//        System.out.println(capeChangeResponse.body());
//        System.out.println(capeChangeResponse.statusCode());
        System.out.println("Attempted to equip: " + cape.name);
    }
}
