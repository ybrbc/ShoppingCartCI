import pytest
from src.cart import Product, ShoppingCart


@pytest.fixture
def sample_catalog() -> list[Product]:
    return [
        Product(id="prod_101", name="Ноутбук", price=50000.0, stock=5),
        Product(id="prod_102", name="Смартфон", price=25000.0, stock=10),
    ]


@pytest.fixture
def cart(sample_catalog: list[Product]) -> ShoppingCart:
    return ShoppingCart(sample_catalog)


def test_product_creation_and_validation() -> None:
    product = Product(id="prod_102", name="Смартфон", price=25000.0, stock=10)
    assert product.id == "prod_102"
    assert product.name == "Смартфон"
    assert product.price == 25000.0
    assert product.stock == 10

    with pytest.raises(ValueError, match="Стоимость товара не может быть    отрицательной"):
        Product(id="prod_1", name="Тест", price=-10.0, stock=5)

    with pytest.raises(ValueError, match="Количество товара на складе не может быть отрицательным"):
        Product(id="prod_1", name="Тест", price=10.0, stock=-1)


def test_cart_init_with_dict() -> None:
    p = Product(id="prod_102", name="Смартфон", price=25000.0, stock=10)
    cart_from_dict = ShoppingCart({"prod_102": p})
    assert "prod_102" in cart_from_dict.catalog


def test_add_item_success_and_increment(cart: ShoppingCart) -> None:
    cart.add_item("prod_102", 2)
    assert cart.items["prod_102"] == 2

    cart.add_item("prod_102", 3)
    assert cart.items["prod_102"] == 5


def test_add_item_errors(cart: ShoppingCart) -> None:
    with pytest.raises(ValueError, match="больше нуля"):
        cart.add_item("prod_102", 0)

    with pytest.raises(ValueError, match="больше нуля"):
        cart.add_item("prod_102", -2)

    with pytest.raises(KeyError, match="не найден в каталоге"):
        cart.add_item("unknown_id", 1)

    with pytest.raises(ValueError, match="Недостаточно товара"):
        cart.add_item("prod_101", 6)


def test_remove_item_decrease_and_full_removal(cart: ShoppingCart) -> None:
    cart.add_item("prod_102", 5)

    # Уменьшение количества
    cart.remove_item("prod_102", 2)
    assert cart.items["prod_102"] == 3

    # Удаление через передачу количества >= текущего
    cart.remove_item("prod_102", 3)
    assert "prod_102" not in cart.items

    # Полное удаление без указания количества (quantity=None)
    cart.add_item("prod_101", 2)
    cart.remove_item("prod_101")
    assert "prod_101" not in cart.items


def test_remove_item_errors(cart: ShoppingCart) -> None:
    with pytest.raises(KeyError, match="отсутствует в корзине"):
        cart.remove_item("prod_102", 1)

    cart.add_item("prod_102", 2)
    with pytest.raises(ValueError, match="больше нуля"):
        cart.remove_item("prod_102", 0)


def test_get_total_price(cart: ShoppingCart) -> None:
    assert cart.get_total_price() == 0.0

    cart.add_item("prod_101", 1)  # 50000.0
    cart.add_item("prod_102", 2)  # 50000.0
    assert cart.get_total_price() == 100000.0
