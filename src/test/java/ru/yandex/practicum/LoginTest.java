package ru.yandex.practicum;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.practicum.client.courier.CourierClient;
import ru.yandex.practicum.model.courier.Courier;
import ru.yandex.practicum.model.courier.courierforauth.CourierForAuth;
import ru.yandex.practicum.model.courier.courierforauth.CourierForAuthWithoutLogin;
import ru.yandex.practicum.model.courier.courierforauth.CourierForAuthWithoutPassword;
import ru.yandex.practicum.service.CourierGenerator;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;

@Slf4j
public class LoginTest {
    private static final String FIELD_MESSAGE = "message";
    public static final String FIELD_ID = "id";
    private static final String RESPONSE = "Получен ответ от сервера: {}";
    public static final String MESSAGE_NO_ENOUGH_DATA = "Недостаточно данных для входа";
    public static final String COURIER_AUTHORIZATION = "Авторизация курьера: {}";
    public static final String MESSAGE_COURIER_NOT_FOUND = "Учетная запись не найдена";

    private final CourierGenerator generator = new CourierGenerator();
    private final CourierClient courierClient = new CourierClient();

    // Поле для курьера, который будет создаваться перед каждым тестом
    private Courier courier;

    /**
     * Подготовка данных перед каждым тестом: создаём курьера и убеждаемся, что он успешно добавлен в систему.
     */
    @Before
    public void setUp() {
        courier = generator.getCourier();
        log.info("Создаём курьера для теста: login={}", courier.getLogin());

        Response response = courierClient.create(courier);

        if (response.statusCode() != HttpStatus.SC_CREATED) {
            log.error("Не удалось создать курьера. Статус: {}, тело: {}",
                    response.statusCode(), response.body().asString());
            throw new IllegalStateException("Окружение для теста не готово: не удалось создать курьера");
        }
        log.info("Курьер успешно создан для теста");
    }

    private void assertBadRequestNotEnoughData(Response response) {
        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .and()
                .body(FIELD_MESSAGE, equalTo(MESSAGE_NO_ENOUGH_DATA));
    }

    @Test
    @DisplayName("Корректная авторизация")
    public void courierCorrectLogin() {
        CourierForAuth courierForAuth = generator.getCourierForAuth(courier);
        log.info(COURIER_AUTHORIZATION, courierForAuth);

        Response response = courierClient.login(courierForAuth);
        log.info(RESPONSE + "\n", response.body().asString());

        response.then().assertThat().statusCode(HttpStatus.SC_OK)
                .and().body(FIELD_ID, allOf(notNullValue(), greaterThan(0)));
    }

    @Test
    @DisplayName("Авторизация без поля login")
    public void courierWithoutLogin() {
        CourierForAuthWithoutLogin courierForAuthWithoutLogin = generator.getCourierForAuthWithoutLogin(courier);
        log.info(COURIER_AUTHORIZATION, courierForAuthWithoutLogin);

        Response response = courierClient.loginWithoutLogin(courierForAuthWithoutLogin);
        log.info(RESPONSE + "\n", response.body().asString());

        assertBadRequestNotEnoughData(response);
    }

    @Test
    @DisplayName("Авторизация без поля password")
    public void courierWithoutPassword() {
        CourierForAuthWithoutPassword courierForAuthWithoutPassword = generator.getCourierForAuthWithoutPassword(courier);
        log.info(COURIER_AUTHORIZATION, courierForAuthWithoutPassword);

        Response response = courierClient.loginWithoutPassword(courierForAuthWithoutPassword);
        log.info(RESPONSE + "\n", response.body().asString());

        assertBadRequestNotEnoughData(response);
    }

    @Test
    @DisplayName("Авторизация с login = null")
    public void courierWithLoginNull() {
        CourierForAuth courierForAuthWithLoginNull = generator.getCourierForAuthWithLoginNull(courier);
        log.info(COURIER_AUTHORIZATION, courierForAuthWithLoginNull);

        Response response = courierClient.login(courierForAuthWithLoginNull);
        log.info(RESPONSE + "\n", response.body().asString());

        assertBadRequestNotEnoughData(response);
    }

    @Test
    @DisplayName("Авторизация с password = null")
    public void courierWithPasswordNull() {
        CourierForAuth courierForAuthWithPasswordNull = generator.getCourierForAuthWithPasswordNull(courier);
        log.info(COURIER_AUTHORIZATION, courierForAuthWithPasswordNull);

        Response response = courierClient.login(courierForAuthWithPasswordNull);
        log.info(RESPONSE + "\n", response.body().asString());

        assertBadRequestNotEnoughData(response);
    }

    @Test
    @DisplayName("Авторизация с несуществующей парой логин/пароль")
    public void courierNonExistent() {
        // Для этого теста нужен курьер, которого НЕ создавали в системе
        Courier courierNotCreate = generator.getCourier();
        CourierForAuth courierForAuthNotCreate = generator.getCourierForAuth(courierNotCreate);
        log.info(COURIER_AUTHORIZATION, courierNotCreate);

        Response response = courierClient.login(courierForAuthNotCreate);
        log.info(RESPONSE + "\n", response.body().asString());

        response.then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND)
                .and().body(FIELD_MESSAGE, equalTo(MESSAGE_COURIER_NOT_FOUND));
    }
}
