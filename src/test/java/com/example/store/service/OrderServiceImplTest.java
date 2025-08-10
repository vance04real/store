package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.handler.ProductNotFoundException;
import com.example.store.i18n.MessageKeys;
import com.example.store.mapper.OrderMapper;
import com.example.store.model.OrderDTO;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;
import com.example.store.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.*;

import java.util.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private OrderServiceImpl service;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getOrderById_shouldReturnDto_whenFound() {
        // Arrange
        Long id = 10L;
        Order order = new Order();
        when(orderRepository.findById(id)).thenReturn(Optional.of(order));
        OrderDTO dto = mock(OrderDTO.class);
        when(orderMapper.orderToOrderDTO(order)).thenReturn(dto);

        // Act
        OrderDTO result = service.getOrderById(id);

        // Assert
        assertThat(result).isSameAs(dto);
        verify(orderRepository).findById(id);
        verify(orderMapper).orderToOrderDTO(order);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFound_withLocalizedMessage_whenNotFound() {
        // Arrange
        Long id = 123L;
        when(orderRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.ORDER.NOT_FOUND), any(), eq(Locale.ENGLISH)))
                .thenAnswer(inv -> "Order " + ((Object[]) inv.getArgument(1))[0] + " not found");

        // Act + Assert
        assertThatThrownBy(() -> service.getOrderById(id))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessage("Order 123 not found");
        verify(orderRepository).findById(id);
        verify(messageSource).getMessage(eq(MessageKeys.ORDER.NOT_FOUND), any(), eq(Locale.ENGLISH));
    }

    @Test
    void getOrderById_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.getOrderById(null))
                .isInstanceOf(RuntimeException.class);
        verifyNoInteractions(orderMapper);
    }

    @Test
    void getAllOrders_shouldMapPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2);
        Order o1 = new Order();
        o1.setId(1L);
        Order o2 = new Order();
        o2.setId(2L);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(o1, o2), pageable, 2));
        OrderDTO d1 = new OrderDTO().id(1L).description("Order 1");
        OrderDTO d2 = new OrderDTO().id(2L).description("Order 2");
        when(orderMapper.orderToOrderDTO(o1)).thenReturn(d1);
        when(orderMapper.orderToOrderDTO(o2)).thenReturn(d2);

        // Act
        Page<OrderDTO> result = service.getAllOrders(pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(d1, d2);
        verify(orderRepository).findAll(pageable);
        verify(orderMapper).orderToOrderDTO(o1);
        verify(orderMapper).orderToOrderDTO(o2);
    }

    @Test
    void getAllOrders_shouldReturnEmpty_whenNoData() {
        Pageable pageable = PageRequest.of(1, 10);
        when(orderRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));
        Page<OrderDTO> result = service.getAllOrders(pageable);
        assertThat(result.getContent()).isEmpty();
        verify(orderRepository).findAll(pageable);
        verifyNoInteractions(orderMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createOrder_shouldThrow_whenDescriptionBlank(String desc) {
        // Arrange
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn(desc);
        when(messageSource.getMessage(eq(MessageKeys.ORDER.DESCRIPTION_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("Order description required");

        // Act + Assert
        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Order description required");
        verify(messageSource).getMessage(eq(MessageKeys.ORDER.DESCRIPTION_REQUIRED), any(), eq(Locale.ENGLISH));
        verifyNoInteractions(orderRepository, orderMapper, productRepository, customerRepository);
    }

    @Test
    void createOrder_shouldThrow_whenCustomerIdNull() {
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("desc");
        when(dto.getCustomerId()).thenReturn(null);
        when(messageSource.getMessage(eq(MessageKeys.ORDER.CUSTOMER_ID_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("Customer id required");

        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Customer id required");
        verify(messageSource).getMessage(eq(MessageKeys.ORDER.CUSTOMER_ID_REQUIRED), any(), eq(Locale.ENGLISH));
        verifyNoInteractions(orderRepository, orderMapper, productRepository, customerRepository);
    }

    @Test
    void createOrder_shouldThrow_whenProductsNullOrEmpty() {
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("desc");
        when(dto.getCustomerId()).thenReturn(1L);
        when(dto.getProductIds()).thenReturn(null);
        when(messageSource.getMessage(eq(MessageKeys.ORDER.PRODUCTS_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("Products required");

        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Products required");
        verify(messageSource).getMessage(eq(MessageKeys.ORDER.PRODUCTS_REQUIRED), any(), eq(Locale.ENGLISH));
    }

    @Test
    void createOrder_shouldThrow_whenDuplicateProductIds() {
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("desc");
        when(dto.getCustomerId()).thenReturn(1L);
        // Simulate duplicates using a mocked Set
        @SuppressWarnings("unchecked")
        Set<Long> productIds = mock(Set.class);
        when(productIds.stream()).thenReturn(java.util.stream.Stream.of(2L, 2L));
        when(productIds.size()).thenReturn(2);
        when(dto.getProductIds()).thenReturn(productIds);
        when(messageSource.getMessage(eq(MessageKeys.ORDER.PRODUCTS_DUPLICATE), any(), eq(Locale.ENGLISH)))
                .thenReturn("Duplicate products");

        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duplicate products");
        verify(messageSource).getMessage(eq(MessageKeys.ORDER.PRODUCTS_DUPLICATE), any(), eq(Locale.ENGLISH));
    }

    @Test
    void createOrder_shouldThrowProductNotFound_whenSomeProductsMissing() {
        // Arrange
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("desc");
        when(dto.getCustomerId()).thenReturn(7L);
        Set<Long> requested = new LinkedHashSet<>(Arrays.asList(1L, 2L, 3L));
        when(dto.getProductIds()).thenReturn(requested);

        Order mappedOrder = new Order();
        when(orderMapper.orderDTOToOrder(dto)).thenReturn(mappedOrder);

        Customer cust = new Customer();
        when(customerRepository.findById(7L)).thenReturn(Optional.of(cust));

        // Only one product found (missing 2,3)
        Product pr1 = new Product();
        pr1.setId(1L);
        when(productRepository.findAllById(requested)).thenReturn(List.of(pr1));
        when(messageSource.getMessage(eq(MessageKeys.PRODUCT.MULTIPLE_NOT_FOUND), any(), eq(Locale.ENGLISH)))
                .thenAnswer(inv -> {
                    Object[] args = inv.getArgument(1, Object[].class);
                    return "Missing products: " + args[0];
                });

        // Act + Assert
        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Missing products: [2, 3]");

        verify(customerRepository).findById(7L);
        verify(productRepository).findAllById(requested);
        verify(messageSource).getMessage(eq(MessageKeys.PRODUCT.MULTIPLE_NOT_FOUND), any(), eq(Locale.ENGLISH));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldThrowCustomerNotFound_whenCustomerMissing() {
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("desc");
        when(dto.getCustomerId()).thenReturn(99L);
        when(dto.getProductIds()).thenReturn(Set.of(1L));

        when(orderMapper.orderDTOToOrder(dto)).thenReturn(new Order());
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CUSTOMER.NOT_FOUND), any(), eq(Locale.ENGLISH)))
                .thenAnswer(inv -> "Customer " + ((Object[]) inv.getArgument(1))[0] + " not found");

        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer 99 not found");
        verify(customerRepository).findById(99L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrder_shouldSave_andReturnDto_whenValid() {
        // Arrange
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("ok");
        when(dto.getCustomerId()).thenReturn(3L);
        when(dto.getProductIds()).thenReturn(Set.of(10L, 20L));

        Order mapped = new Order();
        when(orderMapper.orderDTOToOrder(dto)).thenReturn(mapped);

        Customer customer = new Customer();
        customer.setId(3L);
        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));

        Product p1 = new Product();
        p1.setId(10L);
        Product p2 = new Product();
        p2.setId(20L);
        when(productRepository.findAllById(anyCollection())).thenReturn(List.of(p1, p2));

        Order saved = new Order();
        saved.setId(111L);
        saved.setCustomer(customer);
        saved.setProducts(List.of(p1, p2));
        when(orderRepository.save(mapped)).thenReturn(saved);

        OrderDTO output = mock(OrderDTO.class);
        when(orderMapper.orderToOrderDTO(saved)).thenReturn(output);

        // Act
        OrderDTO result = service.createOrder(dto);

        // Assert
        assertThat(result).isSameAs(output);
        verify(orderMapper).orderDTOToOrder(dto);
        verify(customerRepository).findById(3L);
        verify(productRepository).findAllById(anyCollection());
        verify(orderRepository).save(mapped);
        verify(orderMapper).orderToOrderDTO(saved);
    }

    @Test
    void createOrder_shouldPropagateRepositoryException_whenSaveFails() {
        OrderDTO dto = mock(OrderDTO.class);
        when(dto.getDescription()).thenReturn("ok");
        when(dto.getCustomerId()).thenReturn(3L);
        when(dto.getProductIds()).thenReturn(Set.of(10L));

        Order mapped = new Order();
        when(orderMapper.orderDTOToOrder(dto)).thenReturn(mapped);

        Customer customer = new Customer();
        when(customerRepository.findById(3L)).thenReturn(Optional.of(customer));
        Product p = new Product();
        p.setId(10L);
        when(productRepository.findAllById(anyCollection())).thenReturn(List.of(p));

        when(orderRepository.save(mapped)).thenThrow(new DataAccessResourceFailureException("write failure"));

        assertThatThrownBy(() -> service.createOrder(dto))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("write failure");
    }
}
