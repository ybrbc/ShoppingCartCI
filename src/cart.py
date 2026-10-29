from dataclasses import dataclass


@dataclass
class Product:
    """Класс товара интернет-магазина."""
    id: str
    name: str
    price: float
    stock: int

    def __post_init__(self) -> None:
        if self.price < 0:
            raise ValueError("Стоимость товара не может быть отрицательной")
        if self.stock < 0:
            raise ValueError("Количество товара на складе не может быть отрицательным")


class ShoppingCart:
    """Класс корзины покупок."""

    def __init__(self, catalog: dict[str, Product] | list[Product]) -> None:
        if isinstance(catalog, list):
            self.catalog: dict[str, Product] = {product.id: product for product in catalog}
        else:
            self.catalog = catalog
        # Хранение товаров в виде пар "ID_товара": количество_в_корзине
        self.items: dict[str, int] = {}

    def add_item(self, product_id: str, quantity: int) -> None:
        """Добавляет товар в корзину или увеличивает его количество."""
        if quantity <= 0:
            raise ValueError("Количество добавляемого товара должно быть больше нуля")

        if product_id not in self.catalog:
            raise KeyError(f"Товар с ID '{product_id}' не найден в каталоге")

        product = self.catalog[product_id]
        new_quantity = self.items.get(product_id, 0) + quantity

        if new_quantity > product.stock:
            raise ValueError(
                f"Недостаточно товара '{product.name}' на складе "
                f"(доступно: {product.stock}, запрошено: {new_quantity})"
            )

        self.items[product_id] = new_quantity

    def remove_item(self, product_id: str, quantity: int | None = None) -> None:
        """Уменьшает количество товара в корзине или полностью удаляет его."""
        if product_id not in self.items:
            raise KeyError(f"Товар с ID '{product_id}' отсутствует в корзине")

        if quantity is None:
            del self.items[product_id]
            return

        if quantity <= 0:
            raise ValueError("Количество удаляемого товара должно быть больше нуля")

        if quantity >= self.items[product_id]:
            del self.items[product_id]
        else:
            self.items[product_id] -= quantity

    def get_total_price(self) -> float:
        """Рассчитывает общую стоимость всех товаров в корзине."""
        return sum(
            self.catalog[product_id].price * quantity
            for product_id, quantity in self.items.items()
        )
