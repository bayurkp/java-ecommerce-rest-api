package com.bay.server;

import com.bay.data.Database;
import com.bay.data.Result;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class Response {
    private final HttpExchange exchange;
    private final Database database = new Database();

    public Response(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public void handleGet(String tableName, String condition) throws IOException {
        Result result = database.select(tableName, condition);
        int statusCode = result.getStatusCode();

        if (result.isSuccess()) {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage() + "," +
                    "\"data\": " + result.getData() +
                    "}"
            );
        } else {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage() +
                    "}"
            );
        }
    }

    public void handleGet(String tableMaster, int id, String tableDetail) throws IOException, SQLException {
        Result resultParent = database.select(tableMaster, "id=" + id);
        String jsonResult = null;
        if (tableMaster.equals("users")) {
            if (tableDetail == null) {
                Result addresses = database.select("addresses", "user=" + id);
                jsonResult = database.joinJson(resultParent.getData(),
                        "addresses", addresses.getData());
            } else if (tableDetail.equals("products")) {
                Result products = database.select("addresses", "seller=" + id);
                jsonResult = database.joinJson(resultParent.getData(),
                        "addresses", products.getData());
            } else if (tableDetail.equals("orders")) {
                Result orders = database.select("orders", "buyer=" + id);
                jsonResult = database.joinJson(resultParent.getData(),
                        "orders", orders.getData());
            } else if (tableDetail.equals("reviews")) {
                String query = "SELECT id FROM orders WHERE buyer=" + id;
                Connection connection = database.connect();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(query);

                ArrayList<Integer> idOrders = new ArrayList<>();
                while (resultSet.next()) idOrders.add(resultSet.getInt("id"));

                for (int idOrder : idOrders) {
                    Result reviews = database.select("reviews", "`order`=" + idOrder);
                    jsonResult = database.joinJson(resultParent.getData(),
                            "reviews", reviews.getData());
                }
            }
        } else if (tableMaster.equals("orders") && tableDetail == null) {
            Result orderDetail = database.select("orderDetails", "`order`=" + id);
            System.out.println(orderDetail);
            jsonResult = database.joinJson(resultParent.getData(),
                    "orderDetails", orderDetail.getData());

            Result orderReview = database.select("orderReviews", "`order`=" + id);
            System.out.println(orderReview.getData());
            jsonResult = database.joinJson(jsonResult,
                    "orderDetails", orderReview.getData());
        }

        int statusCode = resultParent.getStatusCode();

        if (resultParent.isSuccess()) {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + resultParent.getMessage() + "," +
                    "\"data\": " + jsonResult +
                    "}"
            );
        }
    }

    public void handlePost(String tableName, JsonNode jsonNode) throws IOException {
        StringBuilder fieldKeys = new StringBuilder();
        StringBuilder fieldValues = new StringBuilder();

        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            fieldKeys.append(field.getKey());
            fieldKeys.append(",");

            fieldValues.append(field.getValue());
            fieldValues.append(",");
        }

        // Remove the comma (,) character at the end of the string
        fieldKeys.deleteCharAt(fieldKeys.length() - 1);
        fieldValues.deleteCharAt(fieldValues.length() - 1);

        Result result = database.insert(tableName, fieldKeys.toString(), fieldValues.toString());
        int statusCode = result.getStatusCode();

        if (result.isSuccess()) {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage() + "," +
                    "\"data\": " + result.getData() +
                    "}");
        } else {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage()  +
                    "}");
        }
    }

    public void handlePut(String tableName, int id, JsonNode jsonNode) throws IOException {
        StringBuilder fieldKeys = new StringBuilder();
        StringBuilder fieldValues = new StringBuilder();

        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            fieldKeys.append(field.getKey());
            fieldKeys.append(",");

            fieldValues.append(field.getValue());
            fieldValues.append(",");
        }

        // Remove the comma (,) character at the end of the string
        fieldKeys.deleteCharAt(fieldKeys.length() - 1);
        fieldValues.deleteCharAt(fieldValues.length() - 1);

        Result result = database.update(tableName, id, fieldKeys.toString(), fieldValues.toString());
        int statusCode = result.getStatusCode();

        if (result.isSuccess()) {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage() + "," +
                    "\"data\": " + result.getData() +
                    "}");
        } else {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage()  +
                    "}");
        }
    }

    public void handleDelete(String tableName, int id) throws IOException {
        Result result = database.delete(tableName, id);
        int statusCode = result.getStatusCode();

        if (result.isSuccess()) {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage() + "," +
                    "\"data\": " + result.getData() +
                    "}");
        } else {
            this.send(statusCode, "{" +
                    "\"status\": " + statusCode + "," +
                    "\"message\": " + result.getMessage()  +
                    "}");
        }
    }

    public void send(int statusCode, String jsonMessage) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        exchange.sendResponseHeaders(statusCode, jsonMessage.length());
        outputStream.write(jsonMessage.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
