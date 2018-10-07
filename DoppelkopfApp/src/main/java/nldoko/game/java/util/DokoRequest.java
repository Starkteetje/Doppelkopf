package nldoko.game.java.util;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

class DokoRequest extends JsonRequest<String> {

    DokoRequest(int method, String url, JSONObject request, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, request.toString(), listener, errorListener);
    }
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String string = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            return Response.success(string, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }
}
