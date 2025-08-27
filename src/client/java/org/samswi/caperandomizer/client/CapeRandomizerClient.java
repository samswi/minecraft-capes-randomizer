package org.samswi.caperandomizer.client;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

public class CapeRandomizerClient implements ClientModInitializer {
    public static File configFolder = new File(FabricLoader.getInstance().getConfigDir() + "/cape_randomizer/");
    public static File capeTexturesFolder = new File(configFolder + "/cape_textures/");
    public static List<Cape> ownedCapesList = new ArrayList<Cape>();
    public static List<Cape> favoriteCapesList = new ArrayList<>();
    public static LinkedList<Cape> capesPull = new LinkedList<Cape>();
    public static HttpClient httpClient = HttpClient.newBuilder().build();
    public static boolean isOriginalCapeEmpty = false;
    public static Cape originalCape;
    public static Cape defaultCape;
    public static Cape currentCape;
    public static File favoriteCapesFile;
    public static JsonObject favoriteCapes;
    public static Gson myGson = new Gson();
    public static Gson prettyGson = new GsonBuilder().setPrettyPrinting().create();
    public static Logger LOGGER = LoggerFactory.getLogger("Cape Rand");
    private static String accessToken;

    @Override
    public void onInitializeClient() {
        configFolder.mkdirs();
        capeTexturesFolder.mkdirs();

        ClientCommandRegistrationCallback.EVENT.register(CapeRandomizerCommand::register);
    }

    public static void fillCapesAndEquipRandom(String accessToken){
        capeTexturesFolder.mkdirs();
        fillCapesList(accessToken);
        equipRandomCape();
    }

    public static void fillCapesList(String newAccessToken){
        if (newAccessToken.length() < 200) return;
        try {

            try {
                resetCape();
            }catch (Exception e){
                LOGGER.error("Failed to reset cape on new session");
            }

            HttpRequest profileDataRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
                    .setHeader("Authorization", "Bearer " + newAccessToken)
                    .GET()
                    .build();
            HttpResponse<String> profileDataResponse = httpClient.send(profileDataRequest, HttpResponse.BodyHandlers.ofString());

            String playerUUID = myGson.fromJson(profileDataResponse.body(), JsonObject.class).get("id").getAsString();
            favoriteCapesFile = new File(configFolder + "/" + playerUUID + ".json");
            if (favoriteCapesFile.exists()) {
                favoriteCapes = loadJsonFromFile(favoriteCapesFile);
            }
            else{
                favoriteCapesFile.createNewFile();
                favoriteCapes = new JsonObject();
            }


            StringBuilder capesString = new StringBuilder();
            if (profileDataResponse.statusCode() == 200){
                JsonArray capesArray = myGson.fromJson(profileDataResponse.body(), JsonObject.class).get("capes").getAsJsonArray();
                ownedCapesList.clear();
                originalCape = null;
                isOriginalCapeEmpty = true;
                capesPull = new LinkedList<>();
                for (JsonElement i : capesArray){
                    JsonObject j = i.getAsJsonObject();
                    Cape capeIterator = new Cape(j.get("id").getAsString(), j.get("url").getAsString(), j.get("alias").getAsString());
                    ownedCapesList.add(capeIterator);
                    if (j.get("state").getAsString().equals("ACTIVE")) {
                        originalCape = capeIterator;
                        isOriginalCapeEmpty = false;
                        capesString.append("(CURRENT) ");
                    }
                    capesString.append(capeIterator.name).append(", ");
                }
            }
            CapeRandomizerClient.accessToken = newAccessToken;
            if (ownedCapesList.isEmpty()){
                LOGGER.error("Failed to fetch capes or account doesn't own any");
                return;
            }
            if (!favoriteCapes.has("capes")) favoriteCapes.add("capes", new JsonObject());

            if (!favoriteCapes.has("default") && originalCape != null){
                favoriteCapes.addProperty("default", originalCape.id);
            }

            // Ensure JSON has all capes
            for (Cape cape : ownedCapesList){
                if (!favoriteCapes.getAsJsonObject("capes").has(cape.id)){
                    favoriteCapes.getAsJsonObject("capes").addProperty(cape.id, true);
                }
            }

            //Ensure JSON doesn't have non-existent capes
            for (Map.Entry<String, JsonElement> entry : favoriteCapes.getAsJsonObject("capes").entrySet()) {
                boolean foundCape = false;
                for (Cape cape : ownedCapesList){
                    if (cape.id.equals(entry.getKey())) {
                        foundCape = true;
                        break;
                    }
                }

                if (!foundCape) {
                    favoriteCapes.getAsJsonObject("capes").remove(entry.getKey());
                    LOGGER.info("Removed cape with id {} from favorite capes as it is no longer found amongst owned capes", entry.getKey());
                }
            }

            refreshFavoriteCapes();
            saveJsonToFile(favoriteCapes, favoriteCapesFile);

            LOGGER.info("Fetched {} capes: {}", ownedCapesList.size(), capesString);
            populateTextures();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void equipRandomCape(){
        if (ownedCapesList.size() <= 1) {
            LOGGER.warn("Not equipping any capes because you own less than 2 capes or there was an error while retrieving them");
            return;
        }
        if (favoriteCapesList.size() <= 1){
            LOGGER.warn("You have less than 2 capes selected as your favorites, no cape will be equipped");
            return;
        }
        if (capesPull.isEmpty()) {
            capesPull = new LinkedList<>(favoriteCapesList);
            Collections.shuffle(capesPull);
        }

        try {
            int selectedCapeIndex = (int)(Math.random()*capesPull.size());
            if(currentCape != null) if (currentCape.id.equals(capesPull.get(selectedCapeIndex).id)){
                equipRandomCape();
                return;
            }
            equipCape(capesPull.get(selectedCapeIndex));
            currentCape = capesPull.get(selectedCapeIndex).clone();
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

    public static void unequipCape() throws IOException, InterruptedException {
        HttpRequest unequipCapeRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
                .setHeader("Authorization", "Bearer " + accessToken)
                .DELETE()
                .build();
        httpClient.send(unequipCapeRequest, HttpResponse.BodyHandlers.ofString());
        LOGGER.info("Unequipped cape");
    }

    public static void resetCape() throws IOException, InterruptedException {
        if (defaultCape != null){
            equipCape(defaultCape);
        } else if (isOriginalCapeEmpty) {
            unequipCape();
        }
    }

    public static void populateTextures(){
        String[] texturesInFolderArray = capeTexturesFolder.list();

        if (texturesInFolderArray == null) return;

        for (Cape i : ownedCapesList){
            boolean foundFile = false;
            for (String j : texturesInFolderArray) {
                if (j.equals(i.id + ".png")) {
                    foundFile = true;
                    break;
                }
            }
            if (!foundFile){
                try (InputStream in = new URI(i.url).toURL().openStream()){
                    LOGGER.info("Downloading \"{}\"", i.name);
                    Files.copy(in, Path.of(capeTexturesFolder + "/" + i.id + ".png"));
                } catch (IOException e) {
                    // handle IOException
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }

            }
        }

    }

    public static JsonObject loadJsonFromFile(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    public static void saveJsonToFile(JsonObject object, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            prettyGson.toJson(object, writer);
        } catch (IOException e) {
            LOGGER.error("Could not save json to file!", e);
            throw new RuntimeException(e);
        }
    }

    public static void refreshFavoriteCapes(){
        favoriteCapesList.clear();
        for (Cape cape : ownedCapesList){
            if (favoriteCapes.getAsJsonObject("capes").get(cape.id).getAsBoolean()){
                favoriteCapesList.add(cape);
            }
            if (cape.id.equals(favoriteCapes.get("default").getAsString())) defaultCape = cape;
        }

        capesPull = new LinkedList<>(favoriteCapesList);

    }

}
