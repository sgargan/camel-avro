package org.apache.camel.component.avro;

import java.sql.Date;
import java.util.HashMap;

import org.apache.camel.dataformat.avro.generated.Confirmation;
import org.apache.camel.dataformat.avro.generated.Item;
import org.apache.camel.dataformat.avro.generated.Order;

public class TestArtifacts {

    public static class OrderBuilder{

        private Order order;

        public OrderBuilder(){
            order = new Order();
            order.orderItems = new HashMap<CharSequence, Item>();
        }

        public OrderBuilder withCustomerId(long customerId){
            this.order.customerId = customerId;
            return this;
        }

        public OrderBuilder withOrderId(long orderId){
            this.order.orderId = orderId;
            return this;
        }

        public Order build(){
            return order;
        }

        public OrderBuilder addItems(Item... items) {
            for(Item item: items){
                order.orderItems.put(item.name, item);
            }
            return this;
        }
    }

    public static class ItemBuilder{

        private Item item;

        public ItemBuilder(){
            item = new Item();
            item.name = "TastyBurger";
            item.sku = 1234;
            item.quantity = 1;
        }

        public ItemBuilder named(String name){
            this.item.name = name;
            return this;
        }

        public ItemBuilder withSku(long sku){
            this.item.sku = sku;
            return this;
        }

        public ItemBuilder withQuantity(int quantity){
            this.item.quantity = quantity;
            return this;
        }

        public Item build(){
            return item;
        }
    }

    public static class ConfirmationBuilder{

        private Confirmation confirm;

        public ConfirmationBuilder(){
            confirm = new Confirmation();
        }

        public Confirmation build(){
            return confirm;
        }

        public ConfirmationBuilder forOrder(Order order){
            confirm.orderId = order.orderId;
            confirm.customerId = order.customerId;

            return this;
        }

        public ConfirmationBuilder estimatedCompletion(Date completion){
            confirm.estimatedCompletion = completion.getTime();
            return this;
        }
    }
}
