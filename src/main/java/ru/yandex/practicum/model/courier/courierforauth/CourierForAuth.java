package ru.yandex.practicum.model.courier.courierforauth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class CourierForAuth {
    private String login;
    private String password;
}
