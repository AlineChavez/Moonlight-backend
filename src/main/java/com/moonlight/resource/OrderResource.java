package com.moonlight.resource;

import com.moonlight.exception.ApiException;
import com.moonlight.model.Order;
import com.moonlight.model.OrderItem;
import com.moonlight.model.Product;
import com.moonlight.repository.JpaOrderRepository;
import com.moonlight.repository.JpaProductRepository;
import com.moonlight.repository.OrderRepository;
import com.moonlight.repository.ProductRepository;
import com.moonlight.security.AuthContext;
import com.moonlight.security.JwtUtil;
import com.moonlight.service.OrderService;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    // Must mirror the multipliers the catalog UI shows next to each size,
    // otherwise what a customer sees in the cart won't match what gets charged.
    private static final Map<String, Double> SIZE_MULTIPLIERS = Map.of(
            "small", 0.85,
            "medium", 1.0,
            "large", 1.2
    );

    private static final OrderRepository orderRepository = new JpaOrderRepository();
    private static final ProductRepository productRepository = new JpaProductRepository();
    private final OrderService orderService = new OrderService(orderRepository);
    private final AuthContext authContext = new AuthContext(new JwtUtil());

    @POST
    public Response create(Map<String, Object> body,
                           @HeaderParam("Authorization") String authHeader) {
        Long userId = authContext.requireUserId(authHeader);

        Object rawItemsObj = body.get("items");
        if (!(rawItemsObj instanceof List<?> rawItems) || rawItems.isEmpty()) {
            throw ApiException.badRequest("El pedido debe tener al menos un producto");
        }

        List<OrderItem> items = rawItems.stream().map(raw -> {
            if (!(raw instanceof Map<?, ?> i)) {
                throw ApiException.badRequest("Formato de producto inválido");
            }
            Long productId;
            int quantity;
            try {
                productId = Long.parseLong(String.valueOf(i.get("id")));
                quantity = Integer.parseInt(String.valueOf(i.get("quantity")));
            } catch (NumberFormatException e) {
                throw ApiException.badRequest("id y quantity deben ser numéricos");
            }
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> ApiException.notFound("Producto no encontrado: " + productId));
            if (!product.isAvailable()) {
                throw ApiException.badRequest("Producto no disponible: " + product.getName());
            }
            String size = i.get("size") == null ? "medium" : i.get("size").toString();
            Double multiplier = SIZE_MULTIPLIERS.get(size);
            if (multiplier == null) {
                throw ApiException.badRequest("Tamaño inválido: " + size);
            }
            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setSize(size);
            item.setQuantity(quantity);
            // Price always derives from the server-side catalog price and size multiplier,
            // never from a client-submitted price, otherwise a caller could submit an
            // arbitrary price for a real product id.
            item.setPrice(round2(product.getPrice() * multiplier));
            return item;
        }).toList();

        Order order = orderService.create(userId, items);
        return Response.status(201).entity(order).build();
    }

    @GET
    @Path("/my")
    public Response getMyOrders(@HeaderParam("Authorization") String authHeader) {
        Long userId = authContext.requireUserId(authHeader);
        List<Order> orders = orderService.getMyOrders(userId);
        return Response.ok(orders).build();
    }

    @GET
    public Response getAll(@HeaderParam("Authorization") String authHeader) {
        authContext.requireAdmin(authHeader);
        List<Order> orders = orderService.getAll();
        return Response.ok(orders).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long id,
                            @HeaderParam("Authorization") String authHeader) {
        Long userId = authContext.requireUserId(authHeader);
        Order order = orderService.getById(id);
        if (!order.getUserId().equals(userId)) {
            throw ApiException.forbidden("No tienes permiso para ver este pedido");
        }
        return Response.ok(order).build();
    }

    @PATCH
    @Path("/{id}/cancel")
    public Response cancel(@PathParam("id") Long id, @HeaderParam("Authorization") String authHeader) {
        Long userId = authContext.requireUserId(authHeader);
        Order order = orderService.cancel(userId, id);
        return Response.ok(order).build();
    }

    @PATCH
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") Long id, Map<String, String> body,
                                  @HeaderParam("Authorization") String authHeader) {
        authContext.requireAdmin(authHeader);
        String status = body.get("status");
        if (status == null || status.isBlank()) {
            throw ApiException.badRequest("El estado es requerido");
        }
        Order order = orderService.updateStatus(id, status);
        return Response.ok(order).build();
    }

    private static double round2(double value) {
        return Math.round(value * 100) / 100.0;
    }
}
