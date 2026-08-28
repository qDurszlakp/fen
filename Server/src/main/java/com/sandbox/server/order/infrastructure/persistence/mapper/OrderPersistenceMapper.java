package com.sandbox.server.order.infrastructure.persistence.mapper;

import com.sandbox.server.order.domain.model.Address;
import com.sandbox.server.order.domain.model.Item;
import com.sandbox.server.order.domain.model.Order;
import com.sandbox.server.order.domain.model.OrderId;
import com.sandbox.server.order.domain.model.OrderStatus;
import com.sandbox.server.order.infrastructure.persistence.OrderMongoDocument;
import com.sandbox.server.order.infrastructure.persistence.OrderMongoDocument.AddressDocument;
import com.sandbox.server.order.infrastructure.persistence.OrderMongoDocument.ItemDocument;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderPersistenceMapper {

    public Order toDomain(OrderMongoDocument document) {
        return new Order(
                new OrderId(document.id()),
                document.version(),
                document.customer(),
                document.status() != null ? OrderStatus.valueOf(document.status()) : null,
                document.currency(),
                document.channel(),
                document.note(),
                toAddress(document.address()),
                toItems(document.items()),
                document.total(),
                document.createdAt()
        );
    }

    public OrderMongoDocument toDocument(Order order) {
        return new OrderMongoDocument(
                order.id() != null ? order.id().value() : null,
                order.version(),
                order.customer(),
                order.status() != null ? order.status().name() : null,
                order.currency(),
                order.channel(),
                order.note(),
                toAddressDocument(order.address()),
                toItemDocuments(order.items()),
                order.total(),
                order.createdAt()
        );
    }

    private Address toAddress(AddressDocument doc) {
        return doc == null ? null : new Address(doc.street(), doc.city(), doc.postalCode(), doc.country());
    }

    private AddressDocument toAddressDocument(Address address) {
        return address == null ? null
                : new AddressDocument(address.street(), address.city(), address.postalCode(), address.country());
    }

    private List<Item> toItems(List<ItemDocument> docs) {
        return docs == null ? null
                : docs.stream().map(d -> new Item(d.sku(), d.name(), d.quantity(), d.price())).toList();
    }

    private List<ItemDocument> toItemDocuments(List<Item> items) {
        return items == null ? null
                : items.stream().map(i -> new ItemDocument(i.sku(), i.name(), i.quantity(), i.price())).toList();
    }
}
