package nldoko.game.java.util;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import nldoko.game.java.data.GameClass;
import nldoko.game.java.data.PlayerClass;

public class Uploader {

    public static void requestToken(String url, String username, String password, Context context, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        HashMap<String, String> params = new HashMap<>();
        params.put("username", username);
        params.put("password", password);

        DokoRequest request_json = new DokoRequest(Request.Method.POST, url, new JSONObject(params), listener, errorListener);

        queue.add(request_json);
    }

    public static void uploadGame(String url, Context context, GameClass game, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String token = context.getSharedPreferences("DokoServerTokenStorage", Context.MODE_PRIVATE).getString("token", "");

        String json;
        try {
            json = getJSONStringFromGame(game);
        } catch (Exception e) {
            VolleyError error = new VolleyError("Game could not be converted to JSON.");
            errorListener.onErrorResponse(error);
            return;
        }
        HashMap<String, String> params = new HashMap<>();
        params.put("token", token);
        params.put("json", json);

        DokoRequest request_json = new DokoRequest(Request.Method.POST, url, new JSONObject(params), listener, errorListener);

        queue.add(request_json);
    }

    private static JSONObject getJSONFromGame(GameClass game) throws JSONException {
        JSONObject json = new JSONObject();
        List<PlayerClass> players = game.getPlayers();

        JSONArray playerArray = getPlayerJSONArrayFromList(players, game.getRoundCount());
        if (playerArray.length() != 4) {
            throw new JSONException("Not 4 players.");
        }
        json.put("players", playerArray);
        json.put("date", game.getCreateDate("YYYY-MM-dd"));

        return json;
    }

    private static JSONArray getPlayerJSONArrayFromList(List<PlayerClass> players, int roundCount) throws JSONException {
        players = players.stream().filter(player -> player.getName() != null && !player.getName().equals("")).collect(Collectors.toList());
        JSONArray array = new JSONArray();
        for (int i = 0; i < players.size(); i++) {
            JSONObject playerJSON = new JSONObject();
            PlayerClass player = players.get(i);

            playerJSON.put("name", player.getName());
            JSONArray pointHistory = new JSONArray();
            for (int j = 0; j < roundCount; j++) {
                pointHistory.put(j, player.getPointHistoryPerRound(j));
            }
            playerJSON.put("points", pointHistory);
            playerJSON.put("final", player.getPoints());

            array.put(i, playerJSON);
        }
        return array;
    }

    private static String getJSONStringFromGame(GameClass game) throws JSONException {
        return getJSONFromGame(game).toString();
    }
}