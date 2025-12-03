package net.flaim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder().success(true).message("Success").data(data).build();
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder().success(true).message(message).data(data).build();
    }

    public static <T> BaseResponse<T> error(String message) {
        return BaseResponse.<T>builder().success(false).message(message).data(null).build();
    }
}