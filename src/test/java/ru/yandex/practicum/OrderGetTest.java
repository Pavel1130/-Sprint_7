package ru.yandex.practicum;

import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.junit.After;
import org.junit.Test;
import ru.yandex.practicum.client.order.OrderClient;
import ru.yandex.practicum.model.order.Order;
import ru.yandex.practicum.service.OrderGenerator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;

@Slf4j
public class OrderGetTest {
    private static final String RESPONSE = "Получен ответ от сервера: {}";
    public static final String FIELD_MESSAGE = "message";
    public static final String MESSAGE_ORDER_NOT_FOUND = "Заказ не найден";
    public static final String MESSAGE_NOT_ENOUGH_DATA = "Недостаточно данных для поиска";
    public static final String FIELD_TRACK = "track";

    private final OrderClient orderClient = new OrderClient();
    private final OrderGenerator generator = new OrderGenerator();
    private final UtilMethods util = new UtilMethods();

    // Поля, которые реально нужны классу (между методами)
    private Order order;
    private Integer trackId;

    @After
    public void delete() {
        if (trackId != null && trackId > 0) {
            util.cancelOrder(trackId);
        }
    }

    @Test
    @DisplayName("Получение заказа по номеру")
    public void getOrderByNumber() {
        createOrder();

        Response response = orderClient.getOrderByNumber(trackId);
        log.info(RESPONSE, response.body().asString());

        Order responseOrder = response.body().jsonPath().getObject("order", Order.class);
        log.info("Создан объект: {}\n", responseOrder);

        assertEquals("Полученный заказ не соответствует запрошенному", order, responseOrder);
    }

    @Test
    @DisplayName("Получение заказа по несуществующему номеру")
    public void getOrderByNonExistentNumber() {
        Integer failTrackId = Integer.MAX_VALUE;
        Response response = orderClient.getOrderByNumber(failTrackId);
        log.info(RESPONSE, response.body().asString());

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_NOT_FOUND)
                .and()
                .body(FIELD_MESSAGE, equalTo(MESSAGE_ORDER_NOT_FOUND));
    }

    @Test
    @DisplayName("Получение заказа без указания номера")
    public void getOrderWithoutNumber() {
        Response response = orderClient.getOrderByNumberWithoutNumber();
        log.info(RESPONSE, response.body().asString());

        response.then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .and()
                .body(FIELD_MESSAGE, equalTo(MESSAGE_NOT_ENOUGH_DATA));
    }

    private void createOrder() {
        // Все эти значения нужны только здесь — делаем их локальными
        String firstName = "Имя";
        String lastName = "Фамилия";
        String address = "Адрес";
        String metroStation = "Красные ворота";
        String phone = "79991231212";
        Integer rentTime = 2;
        String deliveryDate = "2023-07-17";
        String comment = "комментарий?";
        String[] color = new String[]{"black"};

        order = generator.getOrder(firstName, lastName, address, metroStation, phone,
                rentTime, deliveryDate, comment, color);

        Response response = orderClient.createOrder(order);
        trackId = response.body().path(FIELD_TRACK);
        log.info("Номер заказа: {}", trackId);
    }
}
