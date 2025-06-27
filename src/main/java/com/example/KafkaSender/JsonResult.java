package com.example.KafkaSender;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JsonResult {

    private String success;
    private String message;

    public static JsonResult ok() {
        return new JsonResult("true", "Request success.");
    }

    public static JsonResult ok(String message) {
        return new JsonResult("true", message);
    }

    public static JsonResult fail(Exception e) {
        return new JsonResult("false", e.getMessage());
    }

}
