package net.flaim.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @Setter @Getter private Object error;

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "Success", data, null);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(true, message, data, null);
    }

    public static BaseResponse<Void> success() {
        return new BaseResponse<>(true, "Success", null, null);
    }

    public static <T> BaseResponse<T> error(Object error) {
        return new BaseResponse<>(false, null, null, error);
    }

    public static <T> BaseResponse<T> error(String message, Object error) {
        return new BaseResponse<>(false, message, null, error);
    }
}