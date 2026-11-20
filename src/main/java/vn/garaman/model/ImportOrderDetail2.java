package vn.garaman.model;

import java.math.BigDecimal;

public record ImportOrderDetail2(Long id, ImportOrder2 importOrder, SparePart2 sparePart, BigDecimal quantity, BigDecimal importPrice) {
}
