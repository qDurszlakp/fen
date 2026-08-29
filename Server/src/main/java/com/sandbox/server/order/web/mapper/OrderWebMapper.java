package com.sandbox.server.order.web.mapper;

import com.sandbox.server.order.domain.model.Order;
import com.sandbox.server.order.domain.model.OrderFilter;
import com.sandbox.server.order.web.dto.OrderDto;
import com.sandbox.server.order.web.dto.OrderFilterDto;
import org.springframework.stereotype.Component;

@Component
public class OrderWebMapper {

    public OrderDto toDto(Order order) {
        return new OrderDto(
                order.id().value(),
                order.version(),
                order.customer(),
                order.status(),
                order.currency(),
                order.channel(),
                order.note(),
                order.address(),
                order.items(),
                order.total(),
                order.createdAt()
        );
    }

    public Order toChanges(OrderDto dto) {
        return new Order(null, dto.version(), dto.customer(), dto.status(), dto.currency(), dto.channel(),
                dto.note(), dto.address(), dto.items(), null, null);
    }

    public OrderFilter toDomainFilter(OrderFilterDto dto) {
        return new OrderFilter(dto.customer(), dto.status(), dto.currency(), dto.channel(), dto.city(),
                dto.country(), dto.sku(), dto.minTotal(), dto.maxTotal(), dto.createdFrom(), dto.createdTo());
    }
}
