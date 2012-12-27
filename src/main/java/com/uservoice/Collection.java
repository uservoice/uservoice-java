package com.uservoice;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Collection implements Iterable<JSONObject> {
    public static final int PER_PAGE = 100;
    private Client client;
    private int limit = Integer.MAX_VALUE;
    private int perPage;
    private JSONObject responseData = null;
    private String path;
    private Map<Integer, JSONArray> pages = new HashMap<Integer, JSONArray>();

    public Collection(Client client, String path, Integer limit) {
        this.client = client;
        this.path = path;
        if (limit != null) {
            this.limit = limit;
        }
        this.perPage = Math.min(this.limit, PER_PAGE);
    }

    public Collection(Client client, String path) {
        this(client, path, Integer.MAX_VALUE);
    }

    public int size() {
        if (responseData == null) {
            try {
                get(0);
            } catch (IndexOutOfBoundsException e) {
                /* Empty array */
            }
        }
        return Math.min(responseData.getInt("total_records"), limit);
    }

    public JSONObject get(int i) {
        if (i == 0 || (i > 0 && i < size())) {
            return loadPage((int) (i / (float) (PER_PAGE)) + 1).getJSONObject(i % PER_PAGE);
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Requests i'th page from UserVoice. Thread-safe and makes one HTTP
     * connection.
     * 
     * @param i
     *            and index between 1..(1/PER_PAGE) + 1
     * @return The page in a JSONArray
     * @throws APIError
     *             if something unexpected happened
     * @throws Unauthorized
     *             if the user didn't have permissions to request the page
     */
    public synchronized JSONArray loadPage(int i) {
        if (pages == null || pages.get(i) == null) {
            String url;
            if (path.contains("?")) {
                url = path + "&";
            } else {
                url = path + "?";
            }
            System.out.println(url);
            JSONObject result = client.get(url + "per_page=" + perPage + "&page=" + i);
            System.out.println(result);
            if (result != null && result.names().size() == 2 && !result.getJSONObject("response_data").isNullObject()) {
                responseData = result.getJSONObject("response_data");
                for (Object name : result.names()) {
                    if (!"response_data".equals(name)) {
                        pages.put(i, result.getJSONArray(name.toString()));
                    }
                }
                pages.put(i, pages.get(i));
            } else {
                throw new NotFound("The resource you requested is not a collection.");
            }
        }
        return pages.get(i);
    }

    public boolean isEmpty() throws APIError {
        return size() == 0;
    }

    public Iterator<JSONObject> iterator() {
        return new Iterator<JSONObject>() {
            int current = 0;

            public boolean hasNext() {
                return current < size();
            }

            public JSONObject next() {
                return get(current++);
            }

            public void remove() {
                throw new UnsupportedOperationException("Read-Only Collection");
            }
        };
    }

    public JSONObject[] toArray() {
        int arraySize = size();
        JSONObject[] array = new JSONObject[arraySize];
        for (int i = 0; i < arraySize; i++) {
            array[i] = get(i);
        }
        return array;
    }
}