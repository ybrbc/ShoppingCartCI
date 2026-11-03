package org.lab3.shoppingcart;

import lombok.Getter;

@Getter
public class Product {
    private final String id;
    private final String name;
    private final double price;
    private final int stock;

    public Product(String id, String name, double price, int stock) {
        if (price < 0) {
            throw new IllegalArgumentException("Стоимость товара не может быть отрицательной");
        }
        if (stock < 0) {
            throw new IllegalArgumentException("Количество товара на складе не может быть отрицательным");
        }
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }
}