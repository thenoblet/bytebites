package gtp.bytebites.util.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public class ApiResponse<T> {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("errors")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<T> errors;

    public ApiResponse(boolean b, String success, T data, String timestamp, List<T> errors) {
        this.success = b;
        this.message = success;
        this.data = data;
        this.timestamp = timestamp;
        this.errors = errors;

    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(true, "Success", data,
                LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data,
                LocalDateTime.now().toString(), null);
    }

    public static <T> ApiResponse<T> error(String message, List<T> errors) {
        return new ApiResponse<>(false, message, null,
                LocalDateTime.now().toString(), errors);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null,
                LocalDateTime.now().toString(), null);
    }
}
