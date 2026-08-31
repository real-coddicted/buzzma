package com.coddicted.buzzma.campaign.entity;

import com.coddicted.buzzma.shared.common.AuditEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "products")
@EntityListeners(AuditEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Product {
  @Id
  @GeneratedValue
  @UuidGenerator
  @Column(name = "id", updatable = false, nullable = false)
  UUID id;

  @Column(name = "name", nullable = false)
  String name;

  @Column(name = "brand_name", nullable = false)
  String brandName;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "image_urls", columnDefinition = "jsonb", nullable = false)
  List<URL> imageUrls;

  /**
   * The primary product image — the first entry of {@code image_urls}. Read-only, derived in SQL,
   * so every existing single-image read path (MapStruct mappers, the JPQL summary projection) keeps
   * working unchanged while multi-image support is rolled out.
   */
  @Formula("(image_urls ->> 0)")
  URL imageUrl;

  @Column(name = "product_link", nullable = false)
  URL productLink;

  @Column(name = "price_paise", nullable = false)
  BigInteger pricePaise;
}
