package org.lab3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.lab3.shoppingcart.Product;
import org.lab3.shoppingcart.ShoppingCart;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShoppingCartTest {

    private List<Product> sampleCatalog;
    private ShoppingCart cart;

    @BeforeEach
    void setUp() {
        sampleCatalog = List.of(
                new Product("prod_101", "Ноутбук", 50000.0, 5),
                new Product("prod_102", "Смартфон", 25000.0, 10)
        );
        cart = new ShoppingCart(sampleCatalog);
    }

    @Test
    void testProductCreationAndValidation() {
        Product product = new Product("prod_102", "Смартфон", 25000.0, 10);
        assertThat(product.getId()).isEqualTo("prod_102");
        assertThat(product.getName()).isEqualTo("Смартфон");
        assertThat(product.getPrice()).isEqualTo(25000.0);
        assertThat(product.getStock()).isEqualTo(10);

        assertThatThrownBy(() -> new Product("prod_1", "Тест", -10.0, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Стоимость товара не может быть отрицательной");

        assertThatThrownBy(() -> new Product("prod_1", "Тест", 10.0, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Количество товара на складе не может быть отрицательным");
    }

    @Test
    void testCartInitWithMap() {
        Product p = new Product("prod_102", "Смартфон", 25000.0, 10);
        ShoppingCart cartFromMap = new ShoppingCart(Map.of("prod_102", p));
        assertThat(cartFromMap.getCatalog()).containsKey("prod_102");
    }

    @Test
    void testAddItemSuccessAndIncrement() {
        cart.addItem("prod_102", 2);
        assertThat(cart.getItems()).containsEntry("prod_102", 2);

        cart.addItem("prod_102", 3);
        assertThat(cart.getItems()).containsEntry("prod_102", 5);
    }

    @Test
    void testAddItemErrors() {
        assertThatThrownBy(() -> cart.addItem("prod_102", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("больше нуля");

        assertThatThrownBy(() -> cart.addItem("prod_102", -2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("больше нуля");

        assertThatThrownBy(() -> cart.addItem("unknown_id", 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("не найден в каталоге");

        assertThatThrownBy(() -> cart.addItem("prod_101", 6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Недостаточно товара");
    }

    @Test
    void testRemoveItemDecreaseAndFullRemoval() {
        cart.addItem("prod_102", 5);

        // Уменьшение количества
        cart.removeItem("prod_102", 2);
        assertThat(cart.getItems()).containsEntry("prod_102", 3);

        // Удаление через передачу количества >= текущего
        cart.removeItem("prod_102", 3);
        assertThat(cart.getItems()).doesNotContainKey("prod_102");

        // Полное удаление без указания количества
        cart.addItem("prod_101", 2);
        cart.removeItem("prod_101");
        assertThat(cart.getItems()).doesNotContainKey("prod_101");
    }

    @Test
    void testRemoveItemErrors() {
        assertThatThrownBy(() -> cart.removeItem("prod_102", 1))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("отсутствует в корзине");

        cart.addItem("prod_102", 2);
        assertThatThrownBy(() -> cart.removeItem("prod_102", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("больше нуля");
    }

    @Test
    void testGetTotalPrice() {
        assertThat(cart.getTotalPrice()).isEqualTo(0.0);

        cart.addItem("prod_101", 1); // 50000.0
        cart.addItem("prod_102", 2); // 50000.0
        assertThat(cart.getTotalPrice()).isEqualTo(100000.0);
    }

    @Test
    void testPromoCodeApplicationAndDiscount() {
        cart.addItem("prod_101", 1); // 50000.0
        cart.addItem("prod_102", 2); // 50000.0

        // Применение промокода через отдельный метод (скидка 10%)
        cart.applyPromoCode("SALE10");
        assertThat(cart.getAppliedPromo()).isEqualTo("SALE10");
        assertThat(cart.getPromoCodes()).containsKey("SALE10");
        assertThat(cart.getTotalPrice()).isEqualTo(90000.0);

        // Применение промокода напрямую при расчете стоимости (скидка 50%)
        assertThat(cart.getTotalPrice("HALF50")).isEqualTo(50000.0);
    }

    @Test
    void testPromoCodeErrors() {
        ShoppingCart customCart = new ShoppingCart(
                sampleCatalog,
                Map.of("INVALID_ZERO", 0.0, "INVALID_OVER", 150.0, "VALID15", 15.0)
        );
        customCart.addItem("prod_101", 1);

        // Несуществующий промокод
        assertThatThrownBy(() -> customCart.applyPromoCode("UNKNOWN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не найден");

        // Некорректный процент скидки (<= 0 или > 100)
        assertThatThrownBy(() -> customCart.applyPromoCode("INVALID_ZERO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("в диапазоне от 0 до 100");

        assertThatThrownBy(() -> customCart.getTotalPrice("INVALID_OVER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("в диапазоне от 0 до 100");

        // Валидный пользовательский промокод
        assertThat(customCart.getTotalPrice("VALID15")).isEqualTo(42500.0);
    }
}