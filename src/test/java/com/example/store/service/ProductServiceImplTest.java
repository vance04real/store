package com.example.store.service;

import com.example.store.entity.Product;
import com.example.store.exception.handler.ProductNotFoundException;
import com.example.store.i18n.MessageKeys;
import com.example.store.mapper.ProductMapper;
import com.example.store.model.ProductDTO;
import com.example.store.repository.ProductRepository;
import com.example.store.service.impl.ProductServiceImpl;

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
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        LocaleContextHolder.setLocale(Locale.ENGLISH);
    }

    @AfterEach
    void tearDown() {
        LocaleContextHolder.resetLocaleContext();
    }

    @Test
    void getProductById_shouldReturnDto_whenFound() {
        // Arrange
        Long id = 42L;
        Product entity = new Product();
        entity.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(entity));
        ProductDTO dto = mock(ProductDTO.class);
        when(productMapper.productToProductDTO(entity)).thenReturn(dto);

        // Act
        ProductDTO result = service.getProductById(id);

        // Assert
        assertThat(result).isSameAs(dto);
        verify(productRepository, times(1)).findById(id);
        verify(productMapper, times(1)).productToProductDTO(entity);
        verifyNoMoreInteractions(productRepository, productMapper);
    }

    // Arrange-Act-Assert: getProductById not found -> throws with localized message (placeholder {0})
    @Test
    void getProductById_shouldThrowProductNotFoundException_withLocalizedMessage_whenNotFound() {
        // Arrange
        Long id = 99L;
        when(productRepository.findById(id)).thenReturn(Optional.empty());
        when(messageSource.getMessage(eq(MessageKeys.PRODUCT.NOT_FOUND), any(), eq(Locale.ENGLISH)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1, Object[].class);
                    return "Product with id " + args[0] + " not found";
                });

        // Act + Assert
        assertThatThrownBy(() -> service.getProductById(id))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessage("Product with id 99 not found");
        verify(productRepository).findById(id);
        verify(messageSource).getMessage(eq(MessageKeys.PRODUCT.NOT_FOUND), any(), eq(Locale.ENGLISH));
        verifyNoMoreInteractions(productRepository, productMapper, messageSource);
    }

    // Arrange-Act-Assert: getProductById null id -> exception (behavioral)
    @Test
    void getProductById_shouldThrowException_whenIdIsNull() {
        // Arrange
        Long id = null;

        // Act + Assert
        assertThatThrownBy(() -> service.getProductById(id)).isInstanceOf(RuntimeException.class); // NPE or similar
        verifyNoInteractions(productMapper);
    }

    // Arrange-Act-Assert: getAllProducts maps and interacts correctly
    @Test
    void getAllProducts_shouldMapEntitiesToDtos_andCallMapperForEach() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id"));
        Product p1 = new Product();
        p1.setId(1L);
        Product p2 = new Product();
        p2.setId(2L);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(p1, p2), pageable, 2));
        ProductDTO d1 = mock(ProductDTO.class);
        ProductDTO d2 = mock(ProductDTO.class);
        when(productMapper.productToProductDTO(p1)).thenReturn(d1);
        when(productMapper.productToProductDTO(p2)).thenReturn(d2);

        // Act
        Page<ProductDTO> result = service.getAllProducts(pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(d1, d2);
        verify(productRepository, times(1)).findAll(pageable);
        verify(productMapper, times(1)).productToProductDTO(p1);
        verify(productMapper, times(1)).productToProductDTO(p2);
    }

    // Arrange-Act-Assert: getAllProducts with empty page
    @Test
    void getAllProducts_shouldReturnEmptyPage_whenNoData() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 5);
        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        // Act
        Page<ProductDTO> result = service.getAllProducts(pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        verify(productRepository).findAll(pageable);
        verifyNoInteractions(productMapper);
    }

    // Arrange-Act-Assert: createProduct validation for blank descriptions (i18n)
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void createProduct_shouldThrowIllegalArgumentException_withLocalizedMessage_whenDescriptionBlank(String desc) {
        // Arrange
        ProductDTO productDTO = mock(ProductDTO.class);
        when(productDTO.getDescription()).thenReturn(desc);
        when(messageSource.getMessage(eq(MessageKeys.PRODUCT.DESCRIPTION_REQUIRED), any(), eq(Locale.ENGLISH)))
                .thenReturn("Description is required");

        // Act + Assert
        assertThatThrownBy(() -> service.createProduct(productDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Description is required");
        verify(messageSource).getMessage(eq(MessageKeys.PRODUCT.DESCRIPTION_REQUIRED), any(), eq(Locale.ENGLISH));
        verifyNoInteractions(productRepository, productMapper);
    }

    // Arrange-Act-Assert: createProduct happy path with mapper and repo interactions
    @Test
    void createProduct_shouldSave_andReturnMappedDto_whenValid() {
        // Arrange
        ProductDTO inputDto = mock(ProductDTO.class);
        when(inputDto.getDescription()).thenReturn("Phone");

        Product mappedEntity = new Product();
        when(productMapper.productDTOToProduct(inputDto)).thenReturn(mappedEntity);

        Product savedEntity = new Product();
        savedEntity.setId(5L);
        when(productRepository.save(mappedEntity)).thenReturn(savedEntity);

        ProductDTO outputDto = mock(ProductDTO.class);
        when(productMapper.productToProductDTO(savedEntity)).thenReturn(outputDto);

        ArgumentCaptor<ProductDTO> dtoCaptor = ArgumentCaptor.forClass(ProductDTO.class);
        ArgumentCaptor<Product> entityCaptor = ArgumentCaptor.forClass(Product.class);

        // Act
        ProductDTO result = service.createProduct(inputDto);

        // Assert
        assertThat(result).isSameAs(outputDto);
        verify(productMapper).productDTOToProduct(dtoCaptor.capture());
        assertThat(dtoCaptor.getValue()).isSameAs(inputDto);
        verify(productRepository).save(entityCaptor.capture());
        assertThat(entityCaptor.getValue()).isSameAs(mappedEntity);
        verify(productMapper).productToProductDTO(savedEntity);
    }

    // Arrange-Act-Assert: createProduct repository error propagation
    @Test
    void createProduct_shouldPropagateRepositoryException_whenSaveFails() {
        // Arrange
        ProductDTO inputDto = mock(ProductDTO.class);
        when(inputDto.getDescription()).thenReturn("TV");
        Product mappedEntity = new Product();
        when(productMapper.productDTOToProduct(inputDto)).thenReturn(mappedEntity);
        when(productRepository.save(mappedEntity)).thenThrow(new DataAccessResourceFailureException("DB down"));

        // Act + Assert
        assertThatThrownBy(() -> service.createProduct(inputDto))
                .isInstanceOf(DataAccessResourceFailureException.class)
                .hasMessageContaining("DB down");
    }
}
