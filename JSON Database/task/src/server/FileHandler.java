package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileHandler {

    private final ReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;
    private final Path path;
    private final Gson gson;

    public FileHandler() {
        lock = new ReentrantReadWriteLock();
        path = Paths.get(System.getProperty("user.dir") + "/src/server/data/db.json");
        // path = Paths.get(System.getProperty("user.dir") + "/JSON Database/task/src/server/data/db.json");
        gson = new Gson();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    public Response readFromKey(List<String> keys) {
        Response response = null;
        try (Reader reader = Files.newBufferedReader(path)) {
            readLock.lock();

            JsonObject json = gson.fromJson(reader, JsonObject.class);

            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                if (!json.has(key)) {
                    response = Response.builder()
                            .response("ERROR")
                            .reason("No such key")
                            .build();
                    break;
                } else if (i == keys.size() - 1) {
                    response = Response.builder()
                            .response("OK")
                            .value(json.get(key))
                            .build();
                } else {
                    json = json.getAsJsonObject(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            response = Response.builder()
                    .response("ERROR")
                    .reason("Something went wrong while reading the database")
                    .build();
        } finally {
            readLock.unlock();
        }
        return response;
    }

    public Response writeToDatabase(List<String> keys, JsonElement item) {
        Response response;

        JsonObject json = null;
        try (Reader reader = Files.newBufferedReader(path)) {
            readLock.lock();
            json = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        if (json == null) {
            json = new JsonObject();
        }

        JsonObject element = null;

        // make sure element exists
        if (json.has(keys.get(0))) {
            if (keys.size() == 1) {
                json.add(keys.get(0), item);
            } else {
                element = json.getAsJsonObject(keys.get(0));
            }
        } else {
            element = new JsonObject();
            if (keys.size() == 1) {
                json.add(keys.get(0), item);
            } else {
                json.add(keys.get(0), element);
            }
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            for (int i = 1; i < keys.size(); i++) {
                String key = keys.get(i);
                // make sure element exists
                if (element.has(key)) {
                    if (i == keys.size() - 1) {
                        element.add(key, item);
                    } else {
                        element = element.getAsJsonObject(keys.get(i));
                    }
                } else {
                    if (i == keys.size() - 1) {
                        element.add(key, item);
                    } else {
                        element.add(key, new JsonObject());
                        element = element.getAsJsonObject(key);
                    }
                }
            }
            writeLock.lock();
            gson.toJson(json, writer);

            response = Response.builder()
                    .response("OK")
                    .build();
        } catch (IOException e) {
            e.printStackTrace();
            response = Response.builder()
                    .response("ERROR")
                    .reason("Something went wrong while writing from the database")
                    .build();
        } finally {
            writeLock.unlock();
        }
        return response;
    }

    public Response deleteFromDataBase(List<String> keys) {
        Response response;
        JsonObject json = null;
        try (Reader reader = Files.newBufferedReader(path)) {
            readLock.lock();
            json = gson.fromJson(reader, JsonObject.class);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            readLock.unlock();
        }

        if (json == null) {
            json = new JsonObject();
        }

        JsonObject element;

        // make sure element exists
        if (json.has(keys.get(0))) {
            if (keys.size() == 1) {
                json.remove(keys.get(0));
                element = json;
            } else {
                element = json.getAsJsonObject(keys.get(0));
            }
        } else {
            element = new JsonObject();
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            for (int i = 1; i < keys.size(); i++) {
                String key = keys.get(i);
                if (element.has(key)) {
                    if (i == keys.size() - 1) {
                        element.remove(key);
                    } else {
                        element = element.getAsJsonObject(keys.get(i));
                    }
                } else {
                    break;
                }
            }
            writeLock.lock();
            gson.toJson(json, writer);

            response = Response.builder()
                    .response("OK")
                    .build();

        } catch (IOException e) {
            e.printStackTrace();
            response = Response.builder()
                    .response("ERROR")
                    .reason("Something went wrong while deleting from the database")
                    .build();
        } finally {
            writeLock.unlock();
        }
        return response;
    }

}
