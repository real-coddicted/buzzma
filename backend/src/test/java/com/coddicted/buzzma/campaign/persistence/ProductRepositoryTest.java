package com.coddicted.buzzma.campaign.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.coddicted.buzzma.campaign.entity.Product;
import jakarta.persistence.EntityManager;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class ProductRepositoryTest {

  @Container @ServiceConnection
  static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired private ProductRepository productRepository;
  @Autowired private EntityManager entityManager;

  @Test
  void imageUrlFormulaReturnsFirstElementOfImageUrls()
      throws MalformedURLException, URISyntaxException {
    final UUID id =
        this.productRepository
            .save(
                Product.builder()
                    .name("Test product")
                    .brandName("Test brand")
                    .imageUrls(
                        List.of(
                            url("https://example.com/one.jpg"), url("https://example.com/two.jpg")))
                    .productLink(url("https://example.com/product"))
                    .pricePaise(BigInteger.valueOf(10000))
                    .build())
            .getId();
    this.entityManager.flush();
    this.entityManager.clear();

    final Product reloaded = this.productRepository.findById(id).orElseThrow();

    assertEquals(url("https://example.com/one.jpg"), reloaded.getImageUrl());
  }

  @Test
  void imageUrlFormulaIsNullWhenImageUrlsIsEmpty()
      throws MalformedURLException, URISyntaxException {
    final UUID id =
        this.productRepository
            .save(
                Product.builder()
                    .name("Test product")
                    .brandName("Test brand")
                    .imageUrls(List.of())
                    .productLink(url("https://example.com/product"))
                    .pricePaise(BigInteger.valueOf(10000))
                    .build())
            .getId();
    this.entityManager.flush();
    this.entityManager.clear();

    final Product reloaded = this.productRepository.findById(id).orElseThrow();

    assertNull(reloaded.getImageUrl());
  }

  private static URL url(final String value) throws MalformedURLException, URISyntaxException {
    return new URI(value).toURL();
  }
}
