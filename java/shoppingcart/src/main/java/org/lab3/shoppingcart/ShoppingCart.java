package org.lab3.shoppingcart;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Getter
public class ShoppingCart {

    public static final Map<String, Double> DEFAULT_PROMO_CODES = Map.of(
            "SALE10", 10.0,
            "SALE20", 20.0,
            "HALF50", 50.0
    );

    private final Map<String, Product> catalog;
    private final Map<String, Integer> items = new HashMap<>();
    private final Map<String, Double> promoCodes;
    private String appliedPromo;

    public ShoppingCart(Map<String, Product> catalog, Map<String, Double> promoCodes) {
        this.catalog = new HashMap<>(catalog);
        this.promoCodes = promoCodes != null
                ? new HashMap<>(promoCodes)
                : new HashMap<>(DEFAULT_PROMO_CODES);
    }

    public ShoppingCart(Map<String, Product> catalog) {
        this(catalog, null);
    }

    public ShoppingCart(List<Product> catalog, Map<String, Double> promoCodes) {
        this(catalog.stream().collect(Collectors.toMap(Product::getId, p -> p)), promoCodes);
    }

    public ShoppingCart(List<Product> catalog) {
        this(catalog, null);
    }

    public void addItem(String productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество добавляемого товара должно быть больше нуля");
        }

        Product product = catalog.get(productId);
        if (product == null) {
            throw new NoSuchElementException(String.format("Товар с ID '%s' не найден в каталоге", productId));
        }

        int newQuantity = items.getOrDefault(productId, 0) + quantity;
        if (newQuantity > product.getStock()) {
            throw new IllegalArgumentException(String.format(
                    "Недостаточно товара '%s' на складе (доступно: %d, запрошено: %d)",
                    product.getName(), product.getStock(), newQuantity
            ));
        }

        items.put(productId, newQuantity);
    }

    public void removeItem(String productId) {
        removeItem(productId, null);
    }

    public void removeItem(String productId, Integer quantity) {
        if (!items.containsKey(productId)) {
            throw new NoSuchElementException(String.format("Товар с ID '%s' отсутствует в корзине", productId));
        }

        if (quantity == null) {
            items.remove(productId);
            return;
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Количество удаляемого товара должно быть больше нуля");
        }

        int currentQuantity = items.get(productId);
        if (quantity >= currentQuantity) {
            items.remove(productId);
        } else {
            items.put(productId, currentQuantity - quantity);
        }
    }

    public void applyPromoCode(String code) {
        if (!promoCodes.containsKey(code)) {
            throw new IllegalArgumentException(String.format("Промокод '%s' не найден", code));
        }

        double discount = promoCodes.get(code);
        if (discount <= 0 || discount > 100) {
            throw new IllegalArgumentException("Скидка по промокоду должна быть в диапазоне от 0 до 100 процентов");
        }

        this.appliedPromo = code;
    }

    public double getTotalPrice() {
        return getTotalPrice(null);
    }

    public double getTotalPrice(String promoCode) {
        if (promoCode != null) {
            applyPromoCode(promoCode);
        }

        double total = items.entrySet().stream()
                .mapToDouble(entry -> catalog.get(entry.getKey()).getPrice() * entry.getValue())
                .sum();

        if (appliedPromo != null) {
            double discount = promoCodes.get(appliedPromo);
            return Math.round(total * (1.0 - discount / 100.0) * 100.0) / 100.0;
        }

        return total;
    }
}