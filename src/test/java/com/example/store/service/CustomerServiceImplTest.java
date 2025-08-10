package com.example.store.service;

import com.example.store.entity.Customer;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.i18n.MessageKeys;
import com.example.store.mapper.CustomerMapper;
import com.example.store.model.CustomerDTO;
import com.example.store.repository.CustomerRepository;
import com.example.store.service.impl.CustomerServiceImpl;

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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private CustomerServiceImpl service;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getCustomerById_shouldReturnDto_whenFound() {
        // Arrange
        Long id = 5L;
        Customer entity = new Customer();
        entity.setId(id);
        when(customerRepository.findById(id)).thenReturn(Optional.of(entity));
        CustomerDTO dto = mock(CustomerDTO.class);
        when(customerMapper.customerToCustomerDTO(entity)).thenReturn(dto);

        // Act
        CustomerDTO result = service.getCustomerById(id);

        // Assert
        assertThat(result).isSameAs(dto);
        verify(customerRepository).findById(id);
        verify(customerMapper).customerToCustomerDTO(entity);
    }

    @Test
    void getCustomerById_shouldThrowCustomerNotFound_withLocalizedMessage_whenNotFound() {
        // Arrange
        Long id = 77L;
        when(customerRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.CUSTOMER.NOT_FOUND), any(), eq(Locale.ENGLISH)))
                .thenAnswer(inv -> "Customer " + ((Object[]) inv.getArgument(1))[0] + " not found");

        // Act + Assert
        assertThatThrownBy(() -> service.getCustomerById(id))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessage("Customer 77 not found");
        verify(customerRepository).findById(id);
        verify(messageSource).getMessage(eq(MessageKeys.CUSTOMER.NOT_FOUND), any(), eq(Locale.ENGLISH));
    }

    @Test
    void getCustomerById_shouldThrow_whenIdNull() {
        assertThatThrownBy(() -> service.getCustomerById(null)).isInstanceOf(RuntimeException.class);
        verifyNoInteractions(customerMapper);
    }

    @Test
    void getAllCustomers_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Customer c1 = new Customer();
        c1.setId(1L);
        Customer c2 = new Customer();
        c2.setId(2L);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(c1, c2), pageable, 2));
        CustomerDTO d1 = new CustomerDTO().id(1L).firstName("John").lastName("Doe");
        CustomerDTO d2 = new CustomerDTO().id(2L).firstName("Jane").lastName("Smith");
        when(customerMapper.customerToCustomerDTO(c1)).thenReturn(d1);
        when(customerMapper.customerToCustomerDTO(c2)).thenReturn(d2);

        Page<CustomerDTO> result = service.getAllCustomers(pageable);

        assertThat(result.getContent()).containsExactly(d1, d2);
        verify(customerRepository).findAll(pageable);
        verify(customerMapper).customerToCustomerDTO(c1);
        verify(customerMapper).customerToCustomerDTO(c2);
    }

    @Test
    void getAllCustomers_shouldReturnEmpty_whenNoData() {
        Pageable pageable = PageRequest.of(1, 10);
        when(customerRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));
        Page<CustomerDTO> result = service.getAllCustomers(pageable);
        assertThat(result.getContent()).isEmpty();
        verify(customerRepository).findAll(pageable);
        verifyNoInteractions(customerMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createCustomer_shouldThrow_whenFirstNameBlank(String firstName) {
        CustomerDTO dto = mock(CustomerDTO.class);
        when(dto.getFirstName()).thenReturn(firstName);
        when(messageSource.getMessage(eq(MessageKeys.CUSTOMER.FIRST_NAME_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("First name required");

        assertThatThrownBy(() -> service.createCustomer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("First name required");
        verify(messageSource).getMessage(eq(MessageKeys.CUSTOMER.FIRST_NAME_REQUIRED), any(), eq(Locale.ENGLISH));
        verifyNoInteractions(customerRepository, customerMapper);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createCustomer_shouldThrow_whenLastNameBlank(String lastName) {
        CustomerDTO dto = mock(CustomerDTO.class);
        when(dto.getFirstName()).thenReturn("John");
        when(dto.getLastName()).thenReturn(lastName);
        when(messageSource.getMessage(eq(MessageKeys.CUSTOMER.LAST_NAME_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("Last name required");

        assertThatThrownBy(() -> service.createCustomer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Last name required");
        verify(messageSource).getMessage(eq(MessageKeys.CUSTOMER.LAST_NAME_REQUIRED), any(), eq(Locale.ENGLISH));
        verifyNoInteractions(customerRepository, customerMapper);
    }

    @Test
    void createCustomer_shouldSave_andReturnDto_whenValid() {
        CustomerDTO input = mock(CustomerDTO.class);
        when(input.getFirstName()).thenReturn("Jane");
        when(input.getLastName()).thenReturn("Doe");

        Customer mapped = new Customer();
        when(customerMapper.customerDTOToCustomer(input)).thenReturn(mapped);

        Customer saved = new Customer();
        saved.setId(888L);
        when(customerRepository.save(mapped)).thenReturn(saved);

        CustomerDTO output = mock(CustomerDTO.class);
        when(customerMapper.customerToCustomerDTO(saved)).thenReturn(output);

        CustomerDTO result = service.createCustomer(input);

        assertThat(result).isSameAs(output);
        verify(customerMapper).customerDTOToCustomer(input);
        verify(customerRepository).save(mapped);
        verify(customerMapper).customerToCustomerDTO(saved);
    }

    @Test
    void createCustomer_shouldPropagateRepositoryException_whenSaveFails() {
        CustomerDTO input = mock(CustomerDTO.class);
        when(input.getFirstName()).thenReturn("Jane");
        when(input.getLastName()).thenReturn("Doe");

        Customer mapped = new Customer();
        when(customerMapper.customerDTOToCustomer(input)).thenReturn(mapped);

        when(customerRepository.save(mapped)).thenThrow(new DataAccessResourceFailureException("db fail"));

        assertThatThrownBy(() -> service.createCustomer(input))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("db fail");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void searchCustomers_shouldThrow_whenTermBlank(String term) {
        Pageable pageable = PageRequest.of(0, 10);
        when(messageSource.getMessage(eq(MessageKeys.CUSTOMER.SEARCH_TERM_EMPTY), any(), eq(Locale.ENGLISH)))
                .thenReturn("Search term required");

        assertThatThrownBy(() -> service.searchCustomers(term, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Search term required");
        verify(messageSource).getMessage(eq(MessageKeys.CUSTOMER.SEARCH_TERM_EMPTY), any(), eq(Locale.ENGLISH));
    }

    @Test
    void searchCustomers_shouldReturnMappedPage_whenValid() {
        Pageable pageable = PageRequest.of(0, 2);
        String term = "ja";
        Customer c1 = new Customer();
        when(customerRepository.findByNameContaining(term, pageable))
                .thenReturn(new PageImpl<>(List.of(c1), pageable, 1));
        CustomerDTO d1 = mock(CustomerDTO.class);
        when(customerMapper.customerToCustomerDTO(c1)).thenReturn(d1);

        Page<CustomerDTO> result = service.searchCustomers(term, pageable);

        assertThat(result.getContent()).containsExactly(d1);
        verify(customerRepository).findByNameContaining(term, pageable);
        verify(customerMapper).customerToCustomerDTO(c1);
    }
}
