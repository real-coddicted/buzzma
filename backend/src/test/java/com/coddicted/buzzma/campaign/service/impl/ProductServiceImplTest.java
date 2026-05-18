package com.coddicted.buzzma.campaign.service.impl;

import static com.coddicted.buzzma.campaign.service.impl.Fixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

import com.coddicted.buzzma.campaign.entity.Product;
import com.coddicted.buzzma.campaign.persistence.ProductRepository;
import com.coddicted.buzzma.shared.exception.NotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

  @Mock private ProductRepository mockProductRepository;
  private ProductServiceImpl productService;

  @BeforeEach
  void setUp() {
    this.productService = new ProductServiceImpl(this.mockProductRepository);
  }

  @Test
  void testGetByIdWhenFound() {
    doReturn(Optional.of(PRODUCT_1)).when(this.mockProductRepository).findById(PRODUCT_ID);

    final Product result = this.productService.getById(PRODUCT_ID);

    assertEquals(PRODUCT_1, result);
  }

  @Test
  void testGetByIdWhenNotFound() {
    doReturn(Optional.empty()).when(this.mockProductRepository).findById(PRODUCT_ID);

    final NotFoundException ex =
        assertThrows(NotFoundException.class, () -> this.productService.getById(PRODUCT_ID));
    assertEquals("Campaign not found: " + PRODUCT_ID, ex.getMessage());
  }

  @Test
  void testCreate() {
    doReturn(PRODUCT_1).when(this.mockProductRepository).save(PRODUCT_1);

    final Product result = this.productService.create(PRODUCT_1);

    assertEquals(PRODUCT_1, result);
  }
}
