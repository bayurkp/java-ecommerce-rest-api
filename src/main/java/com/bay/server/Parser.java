package com.bay.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class Parser {
    public static JsonNode parseJson(String json) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(json);
    }

    public static String parseInputStream(InputStream inputStream) throws IOException {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        int b;
        StringBuilder buffer = new StringBuilder();
        while ((b = bufferedReader.read()) != -1) {
            buffer.append((char) b);
        }

        bufferedReader.close();
        inputStreamReader.close();

        return buffer.toString();
    }

    public static String[] splitString(String text, String delimiter) {
        return Arrays.stream(text.split(delimiter)).filter(value -> value != null && value.length() > 0).toArray(String[]::new);
    }

    public static String parseRequestQuery(String requestQuery) {
        String[] queries = splitString(requestQuery, "&");
        String field = "";
        String condition = "";
        String value = "";
        try {
            for (String query : queries) {
                String queryKey = splitString(query, "=")[0];
                String queryValue = splitString(query, "=")[1];

                switch (queryKey) {
                    case "f":
                        field = queryValue;
                        break;
                    case "c":
                        switch (queryValue) {
                            case "greaterEqual":
                                condition = ">=";
                                break;
                            case "greater":
                                condition = ">";
                                break;
                            case "lessEqual":
                                condition = "<=";
                                break;
                            case "less":
                                condition = "<";
                                break;
                            case "equal":
                                condition = "=";
                                break;
                            case "notEqual":
                                condition = "<>";
                                break;
                            case "like":
                                condition = "LIKE";
                                break;
                            default:
                                return null;
                        }
                        break;
                    case "v":
                        value = queryValue;
                        break;
                    default:
                        return null;
                }
            }
            return field + " " + condition + " " + value;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) throws IOException {
        String jsonIn = "{\n" +
                "    \"id\": 1,\n" +
                "    \"firstName\": \"Bayu\",\n" +
                "    \"lastName\": \"Pratama\",\n" +
                "    \"email\": \"pratama00@mail.com\",\n" +
                "    \"phone_number\": \"081234567890\",\n" +
                "    \"type\": \"Buyer\"\n" +
                "}";
        InputStream json = new ByteArrayInputStream(jsonIn.getBytes(StandardCharsets.UTF_8));

        byte[] jsonBytes = json.readAllBytes();
        String jsonString = new String(jsonBytes, StandardCharsets.UTF_8);
        System.out.println(Parser.parseJson(jsonString).get("firstName").asText());
    }
}
