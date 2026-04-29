package com.example.authorization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ при успешном логине")
public class LoginResponse {

    @Schema(
            description = "JWT Access Token (используется для авторизации)",
            example = "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlcyI6W3siYXV0aG9yaXR5IjoiUk9MRV9VU0VSIn1dLCJ0eXBlIjoiYWNjZXNzIiwianRpIjoiMDk4MDZiOTQtMDE3NC00Y2RlLWExMTEtMTVmNGU1Nzg2NzkwIiwic3ViIjoic3RyaW5nIiwiaWF0IjoxNzc3MzE2MTk5LCJleHAiOjE3NzczMTcwOTl9.dBThwUedxV4uFhOhVAsGH2nSBukiililny5bC6wGAd1oiY4QwADeOfOwZHGnKurI_-BReM4Li5hH1YUi4SVFyQ6LgR9dbe7bkP5Sv4CNZdksCTLDQ_QgRW2YSRuxSjftILZrzvvdc-eSr0uw6NNdYkdrojTNRHLoSLAYnpJGov_3c35nD24t7pwppz_dt4Yw793VCSnDAVDqfzDgo-Cz0W0IchAXK6TefX10BeDGEfdRPaKtJbwKHquV_LC3GHv_XdTm9H4kWD_5ASmQkOrW-EyOE-MAdCXo64ddnGKvbW4-L9F3tkG-VBByJIkmGn75NTXzVu5nDp6n-awW28PpXA",
            required = true
    )
    private String accessToken;

    @Schema(
            description = "JWT Refresh Token (используется для обновления access токена)",
            example = "eyJhbGciOiJSUzI1NiJ9.eyJ0eXBlIjoicmVmcmVzaCIsImp0aSI6IjA5ODA2Yjk0LTAxNzQtNGNkZS1hMTExLTE1ZjRlNTc4Njc5MCIsInN1YiI6InN0cmluZyIsImlhdCI6MTc3NzMxNjE5OSwiZXhwIjoxNzc3MzE3MDk5fQ.xxxx",
            required = true
    )
    private String refreshToken;

    @Schema(
            description = "Тип токена",
            example = "Bearer",
            defaultValue = "Bearer"
    )
    private String tokenType = "Bearer";

    @Schema(
            description = "Время жизни access токена в секундах",
            example = "900"
    )
    private Long expiresIn;
}